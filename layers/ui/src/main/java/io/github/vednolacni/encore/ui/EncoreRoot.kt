package io.github.vednolacni.encore.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import io.github.vednolacni.encore.app.StageData
import io.github.vednolacni.encore.app.UiState
import io.github.vednolacni.encore.app.QuickState
import io.github.vednolacni.encore.render.ArtworkScene
import io.github.vednolacni.encore.color.Appearance
import io.github.vednolacni.encore.config.MotionLevel
import io.github.vednolacni.encore.config.VolumeDisplay
import io.github.vednolacni.encore.domain.MediaKind
import io.github.vednolacni.encore.domain.PlayerStatus
import io.github.vednolacni.encore.domain.Screen
import io.github.vednolacni.encore.domain.SpeakerLabel
import io.github.vednolacni.encore.domain.TimeText
import io.github.vednolacni.encore.domain.TrackText
import io.github.vednolacni.encore.text.R
import io.github.vednolacni.encore.render.ScreenSpec
import io.github.vednolacni.encore.setup.SetupInfo
import io.github.vednolacni.encore.text.LocalTexts
import io.github.vednolacni.encore.text.texts
import io.github.vednolacni.encore.ui.stage.FieldTime
import io.github.vednolacni.encore.ui.stage.LocalFieldTime
import io.github.vednolacni.encore.ui.stage.PlayedLines
import io.github.vednolacni.encore.ui.stage.PlayerButtons
import io.github.vednolacni.encore.ui.stage.StageModel
import io.github.vednolacni.encore.ui.stage.StageTexts
import io.github.vednolacni.encore.ui.stage.StageTheater
import io.github.vednolacni.encore.ui.stage.StatusLine
import kotlinx.coroutines.delay

/** A wall clock that ticks every [periodMs] (the UI needs minutes, the drift whole pixels). */
@Composable
fun rememberWallClock(periodMs: Long = 5_000, now: () -> Long = System::currentTimeMillis): State<Long> =
    produceState(initialValue = now()) {
        while (true) {
            value = now()
            delay(periodMs)
        }
    }

/** What the root needs from the outside world. */
class RootHooks(
    /** The position of the song with this key, or null once the player reports another. */
    val positionOf: (String) -> Long?,
    /** Local time until which hidden parts (time, place, progress) show again after a key press. */
    val peekUntil: State<Long> = mutableLongStateOf(0L),
    val now: () -> Long = System::currentTimeMillis,
    /** The color field's time (seconds): it runs while music plays, at the motion setting's speed. */
    val fieldTime: () -> Double = { 0.0 },
)

/**
 * The whole screen. Switches between stage, rest and setup with a slow fade through the
 * dark, fades in from black on start (no white flash, ever) and lets everything drift a
 * few pixels for OLED care.
 */
@Composable
fun EncoreRoot(ui: UiState, screen: ScreenSpec, hooks: RootHooks, modifier: Modifier = Modifier) {
    val wall = rememberWallClock(now = hooks.now)
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(1_800, easing = Motion.Dimmer)) }

    // The first seconds after a start stay dark instead of flashing "Connecting":
    // usually the music is there before anyone could read that line.
    val quiet by produceState(initialValue = true) {
        delay(QUIET_START_MS)
        value = false
    }
    val content = contentKey(ui, quiet)
    CompositionLocalProvider(LocalTexts provides (ui.texts ?: texts())) {
        Root(ui, screen, hooks, wall, entrance, content, modifier)
    }
}

@Composable
private fun Root(ui: UiState, screen: ScreenSpec, hooks: RootHooks, wall: State<Long>, entrance: Animatable<Float, *>, content: Content, modifier: Modifier) {
    val density = LocalDensity.current.density
    val t = texts()
    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer {
                alpha = entrance.value
                val (dx, dy) = Motion.drift(wall.value, DRIFT_DP * density)
                translationX = dx.toFloat()
                translationY = dy.toFloat()
            },
    ) {
        // Animate between kinds of screens only (stage/rest/setup). The latest content
        // per kind is kept, so a track change stays inside the stage (its own transition)
        // and an outgoing screen keeps showing what it showed.
        val latest = remember { HashMap<String, Content>() }
        latest[content.key] = content
        // Once a switch has faded out, the screens left behind go.
        LaunchedEffect(content.key) {
            delay(SCREEN_SWITCH_MS)
            latest.keys.retainAll(setOf(content.key))
        }
        AnimatedContent(
            targetState = content.key,
            transitionSpec = {
                fadeIn(tween(1_300, delayMillis = 900, easing = Motion.Dimmer)) togetherWith
                    fadeOut(tween(900, easing = Motion.Dimmer))
            },
            label = "screen",
        ) { k ->
            when (val c = latest[k] ?: Content.Dark) {
                is Content.StageContent -> StageOnScreen(ui, c.stage, hooks, wall) { status, modelFor, intensity, linkDown ->
                    StageTheater(stage = c.stage, status = status, modelFor = modelFor, intensity = intensity, dimmed = linkDown, partyCode = ui.partyCode)
                }
                is Content.RestContent -> RestScreen(
                    reason = c.reason,
                    // A group is named here too: "Living room + 5 · Paused".
                    speaker = c.speaker?.let { name -> SpeakerLabel.group(ui.source.group, t).takeIf { ui.display.group } ?: name },
                    entityId = ui.entityId,
                    nowMs = wall.value,
                    intensity = ui.display.intensity.factor,
                    appearance = c.appearance,
                )
                is Content.SetupContent -> {
                    val scene = rememberOffMain(c.info, screen, c.appearance) { SetupArt.scene(c.info, screen, c.appearance) }
                    if (scene != null) SetupScreen(c.reason, c.info, scene) else Box(Modifier.fillMaxSize())
                }
                Content.Dark -> Box(Modifier.fillMaxSize())
            }
        }
        // The quick settings rise from the bottom edge over whatever shows; the settings menu lies
        // over everything. Both come and go on their own. While only the player's buttons are
        // chosen among, they light on the stage instead.
        QuickBarOverlay(ui.quick?.takeUnless { it.compact }, ui.appearance)
        MenuOverlay(ui.menu, ui.appearance)
    }
}

private sealed interface Content {
    val key: String

    data class StageContent(val stage: StageData) : Content {
        // One stage for all tracks: track changes are animated inside the theater.
        override val key = "stage"
    }

    data class RestContent(val reason: io.github.vednolacni.encore.domain.RestReason, val speaker: String?, val appearance: Appearance) : Content {
        // A new wall fades through the dark like a new screen.
        override val key = "rest:$reason:$appearance"
    }

    data class SetupContent(val reason: io.github.vednolacni.encore.domain.SetupReason, val appearance: Appearance, val info: SetupInfo?) : Content {
        // Leaving, the page keeps its code and address: the server has stopped by then (until
        // 2026.10 they blanked while it faded).
        override val key = "setup:$reason:$appearance"
    }

    data object Dark : Content {
        override val key = "dark"
    }
}

private const val QUIET_START_MS = 4_000L

/**
 * How far the whole screen drifts against burn-in (dp): enough to soften every edge on an
 * OLED, too little to see anything stand off the middle.
 */
private const val DRIFT_DP = 6f

/** A switch of screens is over after this long (900 ms out, then 1.3 s in after 900 ms), with room to spare. */
private const val SCREEN_SWITCH_MS = 3_000L

private fun contentKey(ui: UiState, quietStart: Boolean): Content = when (val s = ui.screen) {
    // Once its first scene is made; until then what showed before stays.
    is Screen.Stage -> ui.stage?.let { Content.StageContent(it) } ?: Content.Dark
    is Screen.Rest ->
        if (quietStart && s.reason == io.github.vednolacni.encore.domain.RestReason.CONNECTING) Content.Dark
        else Content.RestContent(s.reason, s.speaker, ui.appearance)
    is Screen.Setup -> Content.SetupContent(s.reason, ui.appearance, ui.setup)
}

/** The stage's theater, given the player's status, how to build a model, the light setting and a lost link. */
private typealias Theater = @Composable (
    status: PlayerStatus,
    modelFor: (StageData) -> StageModel,
    intensity: () -> Float,
    linkDown: Boolean,
) -> Unit

/** What the [stage] on screen needs: the status line's words, the models, the light setting. Then [theater] draws. */
@Composable
private fun StageOnScreen(ui: UiState, stage: StageData, hooks: RootHooks, wall: State<Long>, theater: Theater) {
    val stageScreen = ui.screen as? Screen.Stage
    // While a new cover loads, the previous stage stays; status follows the live state.
    // A lost connection (after the grace period) or a muted speaker dims the stage.
    val linkDown = stageScreen?.linkDown == true
    val status = stageScreen?.status ?: PlayerStatus.PAUSED
    val display = ui.display
    val player = ui.source.player
    val muted = player?.muted == true
    // A key press brings hidden parts back for a few seconds.
    val peekUntil = hooks.peekUntil.value
    var peek by remember { mutableStateOf(false) }
    LaunchedEffect(peekUntil) {
        val left = peekUntil - hooks.now()
        if (left > 0) {
            peek = true
            delay(left)
        }
        peek = false
    }
    val t = texts()
    val clock = if (display.clock || peek) t.clock(wall.value) else null
    val group = SpeakerLabel.group(ui.source.group, t).takeIf { display.group || peek }
    val volume = SpeakerLabel.volume(player?.volume, muted, t)
    // How it plays: when the sleep timer pauses it, else a book's or an episode's speed.
    val spoken = stageScreen?.track?.kind?.spoken == true
    val pace = ui.sleepAt?.let { StageTexts.sleep(it, t) } ?: TimeText.speed(player?.speed ?: 1f, t)?.takeIf { spoken }
    val own = ui.source.speaker != null && ui.source.speaker == ui.source.ownSpeaker
    val volumeAlways = StageTexts.volumeAlways(display.volume == VolumeDisplay.ALWAYS, own, player?.volume)
    val lead = StageTexts.lead(status, muted, ui.volumeFlash, volume, group, volumeAlways = volumeAlways, t = t, pace = pace)
    // The values of one's own, after the time (hidden by setting, back for a moment on a key).
    val statusLine = StatusLine(lead, clock, ui.values.takeIf { display.values || peek }.orEmpty())
    // A station has no next song to tell (what its queue holds next is another station).
    val nextTrack = stageScreen?.next?.takeIf { display.next && stageScreen.track.kind != MediaKind.RADIO }
    val chapter = ui.chapter
    // A stage still on screen after its song has ended (the next scene still being made, or its
    // lines leaving) keeps its own time, next and chapter; only the song that plays reads them live.
    val playingKey = stageScreen?.track?.key
    val playingNow by rememberUpdatedState(playingKey)
    val played = remember { PlayedLines() }
    val modelFor: (StageData) -> StageModel = remember(playingKey, status, nextTrack, statusLine, linkDown, display, peek, chapter, t) {
        { s: StageData ->
            val key = s.track.key
            val plays = key == playingKey
            val text = TrackText.from(s.track, t[R.string.artists_others])
            // A book or an episode with chapters: the chapter in the album's place, the bar over
            // the chapter, the next chapter after it.
            val ch = played.chapter(key, plays, chapter)
            val after = played.next(key, plays, nextTrack)
            val nextChapter = ch?.next?.takeIf { display.next }
            // The song's words: the song itself stands under the cover.
            val lyrics = s.lyrics
            StageModel(
                scene = s.scene,
                text = if (display.album) text else text.copy(album = null),
                status = status,
                durationMs = ch?.let { it.endMs - it.startMs } ?: s.track.durationMs,
                // Read for this song by name: the player reports the next a moment before the screen shows it.
                positionMs = { played.position(key, key == playingNow) { hooks.positionOf(key) } },
                year = s.track.year.takeIf { display.album },
                // The bar stands across the screen, apart from the words: it shows as set.
                progress = display.progressBar || peek,
                line = StageTexts.line(linkDown, s.track, t),
                next = when {
                    lyrics != null -> StageTexts.next(s.track, t)
                    else -> nextChapter ?: StageTexts.next(after, t)
                },
                nextTitle = when {
                    lyrics != null -> StageTexts.nextTitle(s.track, t)
                    nextChapter != null -> null
                    else -> StageTexts.nextTitle(after, t)
                },
                statusLine = statusLine,
                chapter = ch?.label,
                rangeStartMs = ch?.startMs ?: 0L,
                lyrics = lyrics,
            )
        }
    }
    // Changing the light's strength in the menu turns it like a dimmer.
    val intensity by animateFloatAsState(
        targetValue = display.intensity.factor,
        animationSpec = tween(1_200, easing = Motion.Dimmer),
        label = "light-setting",
    )
    // The cover's colors move only while music plays (a pause holds them, like everything else).
    val moving = display.motion != MotionLevel.OFF && status == PlayerStatus.PLAYING && !linkDown
    val fieldTime = remember(moving, hooks) { FieldTime(hooks.fieldTime, moving) }
    // The bar shows when its length is known and nothing stands in its place, by setting or for a moment.
    val showsLyrics = stage.lyrics != null
    val barShown = (display.progressBar || peek) && !linkDown && (chapter != null || stage.track.durationMs != null)
    // One tick for the bar, the colors and the words, so they are redrawn in the same frames. With
    // nothing to redraw, or with the settings covering the stage, it stops.
    val tick = rememberStageTick(
        fast = moving || status == PlayerStatus.PLAYING || status == PlayerStatus.BUFFERING,
        running = (moving || barShown || showsLyrics) && ui.menu == null,
    )
    CompositionLocalProvider(LocalFieldTime provides fieldTime, LocalStageTick provides tick) {
        theater(status, modelFor, { intensity }, linkDown)
    }
    // The player's buttons stand on the stage, apart from the song, and stay through a change of songs.
    val buttons = ui.buttons
    val kept = remember { arrayOfNulls<Pair<QuickState, ArtworkScene>>(1) }
    if (buttons != null) kept[0] = buttons to stage.scene
    val buttonsShown by animateFloatAsState(if (buttons != null) 1f else 0f, tween(Motion.SOFT_SWAP_MS, easing = Motion.Sine), label = "player-buttons")
    kept[0]?.takeIf { buttonsShown > 0f }?.let { (b, scene) -> PlayerButtons(b, scene, alpha = { buttonsShown }) }
}
