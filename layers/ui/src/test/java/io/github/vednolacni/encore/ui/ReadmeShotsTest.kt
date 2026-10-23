package io.github.vednolacni.encore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.vednolacni.encore.art.SyntheticCover
import io.github.vednolacni.encore.color.Appearance
import io.github.vednolacni.encore.config.LyricsChoice
import io.github.vednolacni.encore.config.QuickContext
import io.github.vednolacni.encore.config.QuickMenu
import io.github.vednolacni.encore.domain.TrackInfo
import io.github.vednolacni.encore.lights.CoverLightColors
import io.github.vednolacni.encore.lights.LightColor
import io.github.vednolacni.encore.lights.LightColors
import io.github.vednolacni.encore.lights.LightSlot
import io.github.vednolacni.encore.render.ArtworkScene
import io.github.vednolacni.encore.text.TestTexts
import io.github.vednolacni.encore.ui.stage.Stage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.TimeZone
import io.github.vednolacni.encore.Pictures
import io.github.vednolacni.encore.domain.Catalog
import org.junit.experimental.categories.Category

/**
 * The screens in the README's pictures, in English; beside each song the colors the lights
 * behind the TV would show (a JSON file). `tools/make_readme_pictures.py` sets them in a room
 * and saves the pictures to docs/screenshots/ when the look changes.
 */
@Category(Pictures::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-land-xhdpi")
class ReadmeShotsTest {
    @Before
    fun berlin() {
        TimeZone.setDefault(TimeZone.getTimeZone(Fixtures.zone))
    }

    private val en = TestTexts.english()

    private fun cover(c: SyntheticCover) = Catalog.SCHEME + c.name

    private val polarNight = TrackInfo("Polar Night", listOf("Ida Holm"), "Northern Lights", 2025, 258_000, artworkUrl = cover(SyntheticCover.AURORA))
    private val desertSong = TrackInfo("Desert Song", listOf("Amal Haddad"), "Cairo Nights", 2016, 289_000, artworkUrl = cover(SyntheticCover.TERRACOTTA))

    // The first picture's song: the app's own name over its tagline.
    private val encore = TrackInfo("Encore", listOf("Lumen Drive"), "See what you\u2019re listening to.", null, 302_000, artworkUrl = cover(SyntheticCover.AURORA))

    private fun shot(name: String, content: @androidx.compose.runtime.Composable () -> Unit) =
        captureRoboImage("build/screenshots/readme/$name.png") { Fixtures.Still(en, content) }

    /** The stage with its buttons under the bar, the song's words [lyrics] on or off. */
    @androidx.compose.runtime.Composable
    private fun Player(model: io.github.vednolacni.encore.ui.stage.StageModel, lyrics: LyricsChoice) {
        val rows = QuickMenu.rows(io.github.vednolacni.encore.config.Settings(), QuickContext(music = true, lyrics = lyrics), en).filter { it.item in QuickMenu.PLAYER_BUTTONS }
        Box(Modifier.fillMaxSize()) {
            Stage(model)
            io.github.vednolacni.encore.ui.stage.PlayerButtons(io.github.vednolacni.encore.app.QuickState(rows, -1, compact = true), model.scene)
        }
    }

    private val words = Fixtures.lyrics(Catalog.nightLyrics)

    /** The lights' colors for a picture: left, middle (above the TV), right; linear sRGB and the light's own xy. */
    private fun lights(name: String, colors: LightColors, warmth: Float) {
        val rgb = FloatArray(3)
        val json = LightSlot.entries.joinToString(",\n", "{\"lights\": [\n", "\n]}\n") { slot ->
            colors.color(slot, 0.0, rgb)
            val xy = LightColor.of(rgb, warmth)
            "  {\"slot\": \"${slot.name}\", \"rgb\": [${rgb[0]}, ${rgb[1]}, ${rgb[2]}], \"xy\": [${xy.x}, ${xy.y}], \"dim\": ${LightColor.dimFor(rgb)}}"
        }
        File("build/screenshots/readme/$name.json").apply { parentFile?.mkdirs() }.writeText(json)
    }

    private fun coverLights(name: String, scene: ArtworkScene, warmth: Float) = lights(name, CoverLightColors(scene.field!!, moving = false), warmth)

    /** The first picture: a song with its words at night on the dark wall, the room lit in its colors. */
    @Test
    fun nowPlaying() {
        val scene = Fixtures.scene(encore)
        shot("now-playing") {
            Player(Fixtures.lyricsModel(encore, words, position = { 81_000L }, scene = scene, clock = en.clock(Fixtures.at(21, 47), Fixtures.zone), group = "Living room", t = en), LyricsChoice.ON)
        }
        coverLights("now-playing", scene, warmth = 1f)
    }

    /** The first picture for a light page: the same song by day on the light wall (the lights are off by day). */
    @Test
    fun nowPlayingLight() {
        val scene = Fixtures.scene(encore, appearance = Appearance.LIGHT)
        shot("now-playing-light") {
            Player(Fixtures.lyricsModel(encore, words, position = { 81_000L }, appearance = Appearance.LIGHT, scene = scene, clock = en.clock(Fixtures.at(14, 47), Fixtures.zone), group = "Living room", t = en), LyricsChoice.ON)
        }
    }

    /** One song twice: on the light wall by day and on the dark wall at night. */
    @Test
    fun dayAndNight() {
        val day = Fixtures.scene(desertSong, appearance = Appearance.LIGHT)
        shot("day") {
            Player(Fixtures.model(desertSong, next = polarNight, positionMs = 61_000L, clock = en.clock(Fixtures.at(14, 15), Fixtures.zone), group = "Living room", scene = day, t = en, appearance = Appearance.LIGHT), LyricsChoice.OFF)
        }
        val night = Fixtures.scene(desertSong)
        shot("night") {
            Player(Fixtures.model(desertSong, next = polarNight, positionMs = 61_000L, clock = en.clock(Fixtures.at(22, 30), Fixtures.zone), group = "Living room", scene = night, t = en), LyricsChoice.OFF)
        }
        coverLights("day", day, warmth = 0f)
        coverLights("night", night, warmth = 1f)
    }
}
