package io.github.vednolacni.encore.ui

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.vednolacni.encore.app.StageData
import io.github.vednolacni.encore.art.SyntheticCover
import io.github.vednolacni.encore.color.Appearance
import io.github.vednolacni.encore.domain.PlayerStatus
import io.github.vednolacni.encore.render.ScreenSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The song that plays, told again with only a new link to its picture, changes nothing on screen:
 * not a frame differs (until 2026.10 it ran a cross-fade that dimmed the words and drew the cover
 * twice).
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-land-mdpi")
class NewLinkTest {
    @get:Rule
    val compose = composeRule()

    private val mdpi = ScreenSpec(960, 540, 1f)
    private val link = "http://ha.local:8123/api/media_player_proxy/media_player.wz?token=2&cache=c1"

    @Test
    fun theStageStaysAsItIs() {
        val track = Fixtures.track(SyntheticCover.TERRACOTTA)
        val scene = Fixtures.scene(track, mdpi, Appearance.DARK)
        var stage by mutableStateOf(StageData(track, scene))
        compose.mainClock.autoAdvance = false
        compose.setContent { Fixtures.Theater(stage, PlayerStatus.PLAYING, group = "Wohnzimmer") }
        compose.mainClock.advanceTimeBy(3_000)
        val steady = capture()
        stage = StageData(track.copy(artworkFallbackUrl = link), scene)
        var changed = 0
        repeat(70) {
            compose.mainClock.advanceTimeBy(16)
            if (!capture().sameAs(steady)) changed++
        }
        assertThat(changed).isEqualTo(0)
    }

    private fun capture(): Bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
}
