package io.github.vednolacni.encore.app

import io.github.vednolacni.encore.art.SyntheticCover
import io.github.vednolacni.encore.art.SyntheticCovers
import io.github.vednolacni.encore.color.Appearance
import io.github.vednolacni.encore.domain.Catalog
import io.github.vednolacni.encore.domain.TrackInfo
import io.github.vednolacni.encore.render.ArtworkScene
import io.github.vednolacni.encore.render.SceneBuilder
import io.github.vednolacni.encore.render.ScreenSpec
import io.github.vednolacni.encore.text.TestTexts
import io.github.vednolacni.encore.text.Texts

/**
 * Test content up to the controller: the test catalog's tracks, the scenes made of their covers,
 * and the stages and menus the controller hands the screens. The controller's
 * tests and the screens' each have a `Fixtures` built on it; the screens' adds their models and
 * theaters.
 */
abstract class AppFixtures {
    val tracks = Catalog.tracks

    /** The words of most shots: German, as the catalog's music and rooms are. */
    val texts: Texts get() = TestTexts.german

    /** A 4K panel with the UI at 4K (xxxhdpi), to check crispness and the true cover scale. */
    val TV_4K = ScreenSpec(3840, 2160, 4f)

    /** Renders use Berlin time on 8 October 2026. */
    val zone: java.time.ZoneId = java.time.ZoneId.of("Europe/Berlin")

    fun at(hour: Int, minute: Int, day: Int = 8): Long =
        java.time.ZonedDateTime.of(2026, 10, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    fun track(cover: SyntheticCover): TrackInfo = tracks.first { it.artworkUrl == Catalog.SCHEME + cover.name }

    val radio: TrackInfo get() = tracks.first { it.artworkUrl == null }

    /** What comes next in most shots. */
    val spiegel = TrackInfo("Spiegel im Spiegel", listOf("Arvo Pärt"))

    /** An English song to the night drive's cover, for the English shots of the words. */
    val night = TrackInfo("Night drive", listOf("Lumen Drive"), "Neon roads", 2024, 251_000, artworkUrl = Catalog.SCHEME + SyntheticCover.NIGHT_DRIVE.name)

    /** Synced words from LRC. */
    fun lyrics(lrc: String): io.github.vednolacni.encore.domain.Lyrics = io.github.vednolacni.encore.domain.Lrc.parse(lrc)!!

    fun scene(
        track: TrackInfo,
        screen: ScreenSpec = ScreenSpec.TV_1080,
        appearance: Appearance = Appearance.DARK,
        coverField: Boolean = false,
    ): ArtworkScene {
        val cover = track.artworkUrl?.removePrefix(Catalog.SCHEME)?.let { SyntheticCover.valueOf(it) }
        val bmp = cover?.let { SyntheticCovers.render(it) }
        return SceneBuilder.build(track.key, bmp, track.artSeed, screen, appearance, coverField = coverField)
    }

    fun stage(
        track: TrackInfo,
        screen: ScreenSpec = ScreenSpec.TV_1080,
        appearance: Appearance = Appearance.DARK,
        coverField: Boolean = false,
    ) = StageData(track, scene(track, screen, appearance, coverField = coverField))

    /** The settings menu opened on [entry]'s group, the cursor on [entry]. */
    fun menu(
        entry: io.github.vednolacni.encore.config.MenuEntry,
        settings: io.github.vednolacni.encore.config.Settings,
        ctx: io.github.vednolacni.encore.config.MenuContext = io.github.vednolacni.encore.config.MenuContext(io.github.vednolacni.encore.domain.LinkState.CONNECTED),
        t: Texts = texts,
    ): MenuState {
        val section = entry.section
        val rows = io.github.vednolacni.encore.config.SettingsMenu.rows(settings, ctx, t, section)
        return MenuState(rows, io.github.vednolacni.encore.config.SettingsMenu.entriesOf(section).indexOf(entry), section = section)
    }

    /** The settings menu on its groups, the cursor on [cursor]. */
    fun menuGroups(cursor: Int = 0, t: Texts = texts) =
        MenuState(io.github.vednolacni.encore.config.SettingsMenu.groups(t), cursor)
}
