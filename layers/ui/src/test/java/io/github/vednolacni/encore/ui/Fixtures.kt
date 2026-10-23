package io.github.vednolacni.encore.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.vednolacni.encore.app.AppFixtures
import io.github.vednolacni.encore.app.StageData
import io.github.vednolacni.encore.color.Appearance
import io.github.vednolacni.encore.domain.PlayerStatus
import io.github.vednolacni.encore.domain.TrackInfo
import io.github.vednolacni.encore.domain.TrackText
import io.github.vednolacni.encore.render.ArtworkScene
import io.github.vednolacni.encore.render.ScreenSpec
import io.github.vednolacni.encore.ui.stage.StageModel
import io.github.vednolacni.encore.ui.stage.StageTexts
import io.github.vednolacni.encore.ui.stage.StageTheater
import io.github.vednolacni.encore.ui.stage.StatusLine
import io.github.vednolacni.encore.text.LocalTexts
import io.github.vednolacni.encore.text.Texts

/**
 * The screens' test content: the controller's ([AppFixtures]), plus the stage's models and the
 * theaters that move them.
 */
object Fixtures : AppFixtures() {
    fun model(
        track: TrackInfo,
        next: TrackInfo? = null,
        status: PlayerStatus = PlayerStatus.PLAYING,
        positionMs: Long? = track.durationMs?.let { it / 3 },
        screen: ScreenSpec = ScreenSpec.TV_1080,
        clock: String? = "21:47",
        group: String? = null,
        t: Texts = texts,
        lead: String? = StageTexts.lead(status, muted = false, volumeFlash = false, volume = null, group = group, volumeAlways = false, t = t),
        appearance: Appearance = Appearance.DARK,
        scene: ArtworkScene = scene(track, screen, appearance),
        album: Boolean = true,
        progress: Boolean = true,
        linkDown: Boolean = false,
        values: List<String> = emptyList(),
    ): StageModel {
        val text = TrackText.from(track, t[io.github.vednolacni.encore.text.R.string.artists_others])
        return StageModel(
            scene = scene,
            text = if (album) text else text.copy(album = null),
            status = status,
            durationMs = track.durationMs,
            positionMs = { positionMs },
            year = track.year.takeIf { album },
            progress = progress,
            line = StageTexts.line(linkDown, track, t),
            next = StageTexts.next(next, t),
            nextTitle = StageTexts.nextTitle(next, t),
            statusLine = StatusLine(lead, clock, values),
        )
    }

    /**
     * A book or an episode at [position] (ms in its own time): the chapter in the album's place,
     * the bar over the chapter, the next chapter (or [next]) after it; [pace] in the status line.
     */
    fun spokenModel(
        track: TrackInfo,
        position: Long,
        pace: String?,
        appearance: Appearance = Appearance.DARK,
        next: TrackInfo? = null,
        coverField: Boolean = false,
        group: String? = "Wohnzimmer",
        t: Texts = texts,
    ): StageModel {
        val chapter = io.github.vednolacni.encore.domain.ChapterPlace.at(track, position) { n ->
            t.format(io.github.vednolacni.encore.text.R.string.chapter_number, n)
        }
        val lead = StageTexts.lead(PlayerStatus.PLAYING, muted = false, volumeFlash = false, volume = null, group = group, volumeAlways = false, t = t, pace = pace)
        return StageModel(
            scene = scene(track, appearance = appearance, coverField = coverField),
            text = TrackText.from(track, t[io.github.vednolacni.encore.text.R.string.artists_others]),
            status = PlayerStatus.PLAYING,
            durationMs = chapter?.let { it.endMs - it.startMs } ?: track.durationMs,
            positionMs = { position },
            next = chapter?.next ?: StageTexts.next(next, t),
            nextTitle = if (chapter?.next != null) null else StageTexts.nextTitle(next, t),
            statusLine = StatusLine(lead, "21:47"),
            chapter = chapter?.label,
            rangeStartMs = chapter?.startMs ?: 0L,
        )
    }

    /**
     * The stage with the song's [lyrics] at [position] (ms), as the root builds it: the song under
     * the cover, the progress bar across the screen unless it is hidden ([progress]).
     */
    fun lyricsModel(
        track: TrackInfo,
        lyrics: io.github.vednolacni.encore.domain.Lyrics,
        position: () -> Long? = { 84_000L },
        status: PlayerStatus = PlayerStatus.PLAYING,
        appearance: Appearance = Appearance.DARK,
        coverField: Boolean = false,
        screen: ScreenSpec = ScreenSpec.TV_1080,
        scene: ArtworkScene = scene(track, screen, appearance, coverField),
        clock: String? = "21:47",
        group: String? = "Wohnzimmer",
        t: Texts = texts,
        progress: Boolean = true,
        linkDown: Boolean = false,
    ): StageModel {
        val lead = StageTexts.lead(status, muted = false, volumeFlash = false, volume = null, group = group, volumeAlways = false, t = t)
        return StageModel(
            scene = scene,
            text = TrackText.from(track, t[io.github.vednolacni.encore.text.R.string.artists_others]),
            status = status,
            durationMs = track.durationMs,
            positionMs = position,
            year = track.year,
            progress = progress,
            line = StageTexts.line(linkDown, track, t),
            next = StageTexts.next(track, t),
            nextTitle = StageTexts.nextTitle(track, t),
            statusLine = StatusLine(lead, clock),
            lyrics = lyrics,
        )
    }

    /** Content rendered as a still: no loops of its own, so the capture can wait for idle. */
    @Composable
    fun Still(content: @Composable () -> Unit) = Still(texts, content)

    /** A still in [t]'s words. */
    @Composable
    fun Still(t: Texts, content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalStill provides true, LocalTexts provides t, content = content)
    }

    /** The animated stage (pause dimming, buffering pulse) for one stage, frozen in time. */
    @Composable
    fun Theater(
        stage: StageData,
        status: PlayerStatus,
        next: TrackInfo? = null,
        intensity: Float = 1f,
        group: String? = null,
        muted: Boolean = false,
        linkDown: Boolean = false,
        partyCode: String? = null,
    ) {
        val lead = StageTexts.lead(status, muted, volumeFlash = false, volume = null, group = group, volumeAlways = false, t = texts)
        StageTheater(
            stage = stage,
            status = status,
            modelFor = { s -> model(s.track, next = next, status = status, scene = s.scene, lead = lead) },
            intensity = { intensity },
            dimmed = linkDown,
            partyCode = partyCode,
        )
    }
}
