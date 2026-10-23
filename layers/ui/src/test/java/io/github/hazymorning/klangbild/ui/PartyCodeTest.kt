package io.github.hazymorning.klangbild.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import io.github.hazymorning.klangbild.art.SyntheticCover
import io.github.hazymorning.klangbild.color.Appearance
import io.github.hazymorning.klangbild.domain.PlayerStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

/**
 * A party's code where the cover stands, for the guests to scan from the couch. Read from
 * rendered pixels, it gives the party's link back where the cover stands, on both walls, on the cover's colors and in a pause, and nothing of the cover shows through it.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-land-xhdpi")
class PartyCodeTest {
    @get:Rule
    val compose = composeRule()

    private var shown by mutableStateOf<(@Composable () -> Unit)?>(null)

    /** Renders [content] as a still, kept as `party/[name].png`. */
    private fun render(name: String, content: @Composable () -> Unit): Bitmap {
        if (shown == null) compose.setContent { shown?.let { Fixtures.Still(it) } }
        shown = content
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/screenshots/party/$name.png")
        return compose.onRoot().captureToImage().asAndroidBitmap()
    }

    /** What a phone's camera reads in the code within [box] (px: x0, y0, x1, y1). */
    private fun scan(bmp: Bitmap, box: IntArray): String {
        val w = box[2] - box[0]
        val h = box[3] - box[1]
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, box[0], box[1], w, h)
        val image = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(w, h, pixels)))
        return QRCodeReader().decode(image, mapOf(DecodeHintType.TRY_HARDER to true)).text
    }

    /** The largest difference (levels, any channel) between the pixels in [box] and its first one. */
    private fun spread(bmp: Bitmap, box: IntArray): Int {
        val first = bmp.getPixel(box[0], box[1])
        var worst = 0
        for (y in box[1] until box[3]) for (x in box[0] until box[2]) {
            val p = bmp.getPixel(x, y)
            for (shift in intArrayOf(16, 8, 0)) worst = maxOf(worst, abs(((p shr shift) and 0xFF) - ((first shr shift) and 0xFF)))
        }
        return worst
    }

    private val song = Fixtures.track(SyntheticCover.BAUHAUS)

    /** The artwork at 2 px a dp (290 dp at 88 dp from the left and 44 dp from the top), inside its corners. */
    private val single = intArrayOf(176 + 16, 88 + 16, 756 - 16, 668 - 16)

    @Test
    fun `where the cover stands, the code gives the party's link`() {
        for (wall in Appearance.entries) for (colors in listOf(false, true)) {
            val name = "single_${wall.name.lowercase()}" + if (colors) "_colors" else ""
            val bmp = render(name) {
                Fixtures.Theater(
                    Fixtures.stage(song, appearance = wall, coverField = colors),
                    PlayerStatus.PLAYING,
                    next = Fixtures.spiegel,
                    group = "Wohnzimmer",
                    partyCode = LINK,
                )
            }
            assertWithMessage(name).that(scan(bmp, single)).isEqualTo(LINK)
            // Above the code its card is plain: nothing of the cover shows through.
            assertWithMessage("$name: above the code").that(spread(bmp, intArrayOf(single[0], single[1], single[2], single[1] + 40))).isAtMost(2)
        }
    }

    @Test
    fun `in a pause the code dims with the cover and still reads`() {
        val bmp = render("single_paused") {
            Fixtures.Theater(Fixtures.stage(song), PlayerStatus.PAUSED, next = Fixtures.spiegel, group = "Wohnzimmer", partyCode = LINK)
        }
        assertThat(scan(bmp, single)).isEqualTo(LINK)
    }

    private companion object {
        /** A party's link as Music Assistant hands it out with remote access (the longer kind). */
        const val LINK = "https://app.music-assistant.io/?remote_id=K7QF2M9XW3&join=Q4WZ8TNP"
    }
}
