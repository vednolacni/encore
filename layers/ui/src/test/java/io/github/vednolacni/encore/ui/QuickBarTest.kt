package io.github.vednolacni.encore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.vednolacni.encore.app.QuickState
import io.github.vednolacni.encore.app.UiState
import io.github.vednolacni.encore.art.SyntheticCover
import io.github.vednolacni.encore.color.Appearance
import io.github.vednolacni.encore.config.ActionState
import io.github.vednolacni.encore.domain.QuickAction
import io.github.vednolacni.encore.config.QuickKey
import io.github.vednolacni.encore.config.RoomLightBrightness
import io.github.vednolacni.encore.config.FavoriteState
import io.github.vednolacni.encore.config.RoomLights
import io.github.vednolacni.encore.config.QuickContext
import io.github.vednolacni.encore.config.QuickItem
import io.github.vednolacni.encore.config.QuickMenu
import io.github.vednolacni.encore.config.Settings
import io.github.vednolacni.encore.domain.LinkState
import io.github.vednolacni.encore.domain.SourceState
import io.github.vednolacni.encore.domain.PlayerStatus
import io.github.vednolacni.encore.domain.Screen
import io.github.vednolacni.encore.ui.stage.Stage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.TimeZone
import io.github.vednolacni.encore.Pictures
import org.junit.experimental.categories.Category

/** The quick settings along the bottom edge: over the stage on black and on paper. */
@Category(Pictures::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-land-xhdpi")
class QuickBarTest {
    @Before
    fun berlin() {
        TimeZone.setDefault(TimeZone.getTimeZone(Fixtures.zone))
    }

    private val withLights = Settings(roomLights = RoomLights(left = "light.links", center = "light.mitte", right = "light.rechts"), roomLightBrightness = RoomLightBrightness.P50)

    private fun state(focus: QuickItem, ctx: QuickContext, s: Settings = withLights): QuickState {
        val rows = QuickMenu.rows(s, ctx, Fixtures.texts)
        return QuickState(rows, rows.indexOfFirst { it.item == focus })
    }

    private val playing = QuickContext(music = true, favorite = FavoriteState.NONE, shuffle = false)

    @Test
    fun overTheStage() {
        val track = Fixtures.track(SyntheticCover.NIGHT_DRIVE)
        captureRoboImage("build/screenshots/quick/stage_dark.png") {
            Fixtures.Still {
                Box(Modifier.fillMaxSize()) {
                    Stage(Fixtures.model(track, next = Fixtures.spiegel))
                    QuickBar(state(QuickItem.LIGHTS, playing), Appearance.DARK)
                }
            }
        }
        captureRoboImage("build/screenshots/quick/stage_dark_favorite.png") {
            Fixtures.Still {
                Box(Modifier.fillMaxSize()) {
                    Stage(Fixtures.model(track, next = Fixtures.spiegel))
                    QuickBar(state(QuickItem.FAVORITE, playing.copy(favorite = FavoriteState.DONE, shuffle = true)), Appearance.DARK)
                }
            }
        }
        val light = Fixtures.track(SyntheticCover.BAUHAUS)
        captureRoboImage("build/screenshots/quick/stage_light.png") {
            Fixtures.Still {
                Box(Modifier.fillMaxSize()) {
                    Stage(Fixtures.model(light, next = Fixtures.spiegel, appearance = Appearance.LIGHT))
                    QuickBar(state(QuickItem.SHUFFLE, playing), Appearance.LIGHT)
                }
            }
        }
        // Without lights and without favorites (no Music Assistant): the way into the settings alone.
        captureRoboImage("build/screenshots/quick/stage_few.png") {
            Fixtures.Still {
                Box(Modifier.fillMaxSize()) {
                    Stage(Fixtures.model(track, next = Fixtures.spiegel))
                    QuickBar(state(QuickItem.SETTINGS, QuickContext(music = true), Settings()), Appearance.DARK)
                }
            }
        }
    }

    /** Buttons of one's own: they come after the built-in entries; a row too long for the room glides to the chosen one. */
    @Test
    fun withButtonsOfOnesOwn() {
        val buttons = withLights.copy(
            quickActions = listOf(
                QuickAction("Lautsprecher trennen", "script.ungroup_speakers"),
                QuickAction("Kinolicht", "scene.kino"),
                QuickAction("Partymodus", "input_boolean.party"),
            ),
        )
        val ctx = playing.copy(
            actions = mapOf(0 to ActionState.READY, 1 to ActionState.DONE, 2 to ActionState.ON),
        )
        val track = Fixtures.track(SyntheticCover.NIGHT_DRIVE)
        fun shot(name: String, focus: QuickKey, t: io.github.vednolacni.encore.text.Texts = Fixtures.texts) {
            val rows = QuickMenu.rows(buttons, ctx, t)
            captureRoboImage("build/screenshots/quick/$name.png") {
                Fixtures.Still(t) {
                    Box(Modifier.fillMaxSize()) {
                        Stage(Fixtures.model(track, next = Fixtures.spiegel, t = t))
                        QuickBar(QuickState(rows, rows.indexOfFirst { it.key == focus }), Appearance.DARK)
                    }
                }
            }
        }
        shot("buttons_start", QuickKey(QuickItem.FAVORITE))
        shot("buttons_first", QuickKey(QuickItem.ACTION, 0))
        shot("buttons_end", QuickKey(QuickItem.ACTION, 2))
        shot("buttons_end_en", QuickKey(QuickItem.ACTION, 2), io.github.vednolacni.encore.text.TestTexts.english())
    }
}
