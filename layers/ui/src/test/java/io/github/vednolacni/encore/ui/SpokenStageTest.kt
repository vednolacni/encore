package io.github.vednolacni.encore.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.vednolacni.encore.art.SyntheticCover
import io.github.vednolacni.encore.color.Appearance
import io.github.vednolacni.encore.domain.TimeText
import io.github.vednolacni.encore.domain.TrackInfo
import io.github.vednolacni.encore.ui.stage.Stage
import io.github.vednolacni.encore.ui.stage.StageModel
import io.github.vednolacni.encore.ui.stage.StageTexts
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import io.github.vednolacni.encore.Pictures
import io.github.vednolacni.encore.domain.Catalog
import org.junit.experimental.categories.Category

/**
 * Audiobooks and podcasts on the stage: the chapter in the album's place, the bar over the chapter,
 * speed and sleep timer in words.
 */
@Category(Pictures::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-land-xhdpi")
class SpokenStageTest {
    private fun shot(name: String, model: StageModel) =
        captureRoboImage("build/screenshots/spoken/$name.png") { Fixtures.Still { Stage(model) } }

    private val book = Catalog.book
    private val episode = Catalog.episode

    private fun spokenModel(track: TrackInfo, position: Long, lead: String?, appearance: Appearance = Appearance.DARK, next: TrackInfo? = null) =
        Fixtures.spokenModel(track, position, lead, appearance, next)

    private fun lead(pace: String?) = pace

    @Test fun audiobook() = shot("audiobook", spokenModel(book, 2_554_000L, lead(TimeText.speed(1.25f, Fixtures.texts))))

    @Test fun audiobookNumbered() = shot("audiobook_numbered", spokenModel(book, 4_100_000L, lead(StageTexts.sleep(Fixtures.at(23, 10), Fixtures.texts, Fixtures.zone))))

    @Test fun audiobookPaper() = shot("audiobook_paper", spokenModel(book, 2_554_000L, lead(TimeText.speed(1.5f, Fixtures.texts)), appearance = Appearance.LIGHT))

    @Test fun podcast() = shot("podcast", spokenModel(episode, 1_163_000L, lead(StageTexts.sleep(Fixtures.at(23, 10), Fixtures.texts, Fixtures.zone)), next = Catalog.nextEpisode))
}
