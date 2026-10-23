package io.github.vednolacni.encore.config

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.vednolacni.encore.domain.QuickAction
import io.github.vednolacni.encore.text.R
import io.github.vednolacni.encore.text.TestTexts
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class QuickMenuTest {
    private val de = TestTexts.german
    private val lights = Settings(roomLights = RoomLights(left = "light.left", center = null, right = null))

    @Test
    fun `shuffle stands where the sleep timer stood, only where the player can shuffle`() {
        val rows = QuickMenu.rows(lights, QuickContext(music = true, favorite = FavoriteState.NONE, shuffle = false), de)
        assertThat(rows.map { it.item }).containsExactly(QuickItem.FAVORITE, QuickItem.LIGHTS, QuickItem.SHUFFLE, QuickItem.SETTINGS).inOrder()
        assertThat(rows.first { it.item == QuickItem.SHUFFLE }.value).isEqualTo("Aus")
        assertThat(QuickMenu.rows(lights, QuickContext(music = true, shuffle = true), de).first { it.item == QuickItem.SHUFFLE }.value).isEqualTo("An")
        // A player that cannot shuffle, and no music: no entry.
        assertThat(QuickMenu.rows(lights, QuickContext(music = true, shuffle = null), de).map { it.item }).doesNotContain(QuickItem.SHUFFLE)
        assertThat(QuickMenu.rows(lights, QuickContext(music = false, shuffle = true), de).map { it.item }).doesNotContain(QuickItem.SHUFFLE)
    }

    @Test
    fun `the song's words come first, only with Music Assistant and a song`() {
        val ctx = QuickContext(music = true, favorite = FavoriteState.NONE, shuffle = false, lyrics = LyricsChoice.ON)
        val rows = QuickMenu.rows(lights, ctx, de)
        assertThat(rows.map { it.item }).containsExactly(QuickItem.LYRICS, QuickItem.FAVORITE, QuickItem.LIGHTS, QuickItem.SHUFFLE, QuickItem.SETTINGS).inOrder()
        assertThat(rows[0].label).isEqualTo("Songtext")
        assertThat(rows[0].value).isEqualTo("An")
        assertThat(rows[0].muted).isFalse()
        val off = QuickMenu.rows(lights, ctx.copy(lyrics = LyricsChoice.OFF), de)[0]
        assertThat(off.value).isEqualTo("Aus")
        assertThat(off.muted).isFalse()
        // On, but this song has no synced words: still On, said quietly (a word of its own read
        // like the words switched off).
        val none = QuickMenu.rows(lights, ctx.copy(lyrics = LyricsChoice.NONE), de)[0]
        assertThat(none.value).isEqualTo("An")
        assertThat(none.muted).isTrue()
        assertThat(QuickMenu.rows(lights, ctx.copy(lyrics = LyricsChoice.NONE), TestTexts.english())[0].value).isEqualTo("On")
        // No other entry is ever quiet.
        assertThat(QuickMenu.rows(lights, ctx.copy(lyrics = LyricsChoice.NONE), de).filter { it.muted }.map { it.item }).containsExactly(QuickItem.LYRICS)
        // Its place is as wide as any of its values.
        assertThat(rows[0].values).containsExactly("An", "Aus")
        // Without Music Assistant, or without a song: no entry.
        assertThat(QuickMenu.rows(lights, ctx.copy(lyrics = null), de).map { it.item }).doesNotContain(QuickItem.LYRICS)
        assertThat(QuickMenu.rows(lights, ctx, TestTexts.english())[0].label).isEqualTo("Lyrics")
    }

    @Test
    fun `the party code stands with what the screen shows, only while a party plays here`() {
        val ctx = QuickContext(music = true, favorite = FavoriteState.NONE, shuffle = false, lyrics = LyricsChoice.OFF, party = false)
        val rows = QuickMenu.rows(lights, ctx, de)
        assertThat(rows.map { it.item }).containsExactly(
            QuickItem.LYRICS, QuickItem.PARTY, QuickItem.FAVORITE, QuickItem.LIGHTS, QuickItem.SHUFFLE, QuickItem.SETTINGS,
        ).inOrder()
        assertThat(rows[1].label).isEqualTo("Party-Code")
        assertThat(rows[1].value).isEqualTo("Aus")
        assertThat(QuickMenu.rows(lights, ctx.copy(party = true), de)[1].value).isEqualTo("An")
        assertThat(QuickMenu.rows(lights, ctx, TestTexts.english())[1].label).isEqualTo("Party code")
        // No party here, or no song on the stage to stand beside the code: no entry.
        assertThat(QuickMenu.rows(lights, ctx.copy(party = null), de).map { it.item }).doesNotContain(QuickItem.PARTY)
        assertThat(QuickMenu.rows(lights, ctx.copy(music = false), de).map { it.item }).doesNotContain(QuickItem.PARTY)
    }

    @Test
    fun `the song's words and the way into the settings are the player's buttons`() {
        val ctx = QuickContext(music = true, favorite = FavoriteState.NONE, shuffle = false, lyrics = LyricsChoice.ON)
        val buttons = QuickMenu.rows(lights, ctx, de).filter { it.item in QuickMenu.PLAYER_BUTTONS }
        assertThat(buttons.map { it.label }).containsExactly("Songtext", "Einstellungen").inOrder()
    }

    @Test
    fun `the sleep timer lives in the settings, not in the quick settings`() {
        assertThat(QuickItem.entries.map { it.label }).doesNotContain(R.string.entry_sleep_timer)
        assertThat(SettingsMenu.entries).contains(MenuEntry.SLEEP_TIMER)
    }

    @Test
    fun `the way into all settings comes last, after the buttons of one's own`() {
        val buttons = lights.copy(quickActions = listOf(QuickAction("Gruppe", "script.group")))
        val rows = QuickMenu.rows(buttons, QuickContext(actions = mapOf(0 to ActionState.READY)), de)
        assertThat(rows.map { it.item }).containsExactly(QuickItem.LIGHTS, QuickItem.ACTION, QuickItem.SETTINGS).inOrder()
        assertThat(rows.last().label).isEqualTo("Einstellungen")
        assertThat(rows.last().value).isEqualTo("Öffnen")
    }
}
