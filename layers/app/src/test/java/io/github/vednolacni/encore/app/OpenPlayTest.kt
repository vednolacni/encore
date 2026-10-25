package io.github.vednolacni.encore.app

import com.google.common.truth.Truth.assertThat
import io.github.vednolacni.encore.config.Settings
import io.github.vednolacni.encore.domain.LinkState
import io.github.vednolacni.encore.domain.PlayerSnapshot
import io.github.vednolacni.encore.domain.PlayerStatus
import io.github.vednolacni.encore.domain.SourceState
import io.github.vednolacni.encore.domain.TrackInfo
import org.junit.Test

/** Music as Encore opens: only once Home Assistant has told, only for an idle speaker, only once. */
class OpenPlayTest {
    private var clock = 1_000L
    private val open = OpenPlay { clock }
    private val settings = Settings(maUrl = "http://192.168.1.5:8095", maToken = "t")

    private fun state(status: PlayerStatus, ma: Boolean = true, link: LinkState = LinkState.CONNECTED) =
        SourceState(link, player = PlayerSnapshot(status, track = TrackInfo("Lied", listOf("A")).takeIf { status == PlayerStatus.PLAYING }, isMusicAssistant = ma))

    @Test
    fun `an idle speaker gets music once Home Assistant has told, and only once`() {
        open.arm()
        assertThat(open.wants(SourceState(LinkState.CONNECTING), settings)).isFalse()
        assertThat(open.wants(SourceState(LinkState.CONNECTED), settings)).isFalse()
        assertThat(open.wants(state(PlayerStatus.IDLE), settings)).isTrue()
        assertThat(open.wants(state(PlayerStatus.IDLE), settings)).isFalse()
        open.arm()
        assertThat(open.wants(state(PlayerStatus.OFF), settings)).isTrue()
    }

    @Test
    fun `music that plays or waits paused is left as it is, and so is a speaker without Music Assistant`() {
        for (status in listOf(PlayerStatus.PLAYING, PlayerStatus.PAUSED, PlayerStatus.BUFFERING, PlayerStatus.UNAVAILABLE)) {
            open.arm()
            assertThat(open.wants(state(status), settings)).isFalse()
            // It has decided: a later idle speaker is no opening.
            assertThat(open.wants(state(PlayerStatus.IDLE), settings)).isFalse()
        }
        open.arm()
        assertThat(open.wants(state(PlayerStatus.IDLE, ma = false), settings)).isFalse()
    }

    @Test
    fun `nothing starts with the setting off, without Music Assistant, late, or when not armed`() {
        assertThat(open.wants(state(PlayerStatus.IDLE), settings)).isFalse()
        open.arm()
        assertThat(open.wants(state(PlayerStatus.IDLE), settings.copy(playOnOpen = false))).isFalse()
        open.arm()
        assertThat(open.wants(state(PlayerStatus.IDLE), Settings())).isFalse()
        open.arm()
        clock += OpenPlay.WAIT_MS + 1
        assertThat(open.wants(state(PlayerStatus.IDLE), settings)).isFalse()
        open.arm()
        open.disarm()
        assertThat(open.wants(state(PlayerStatus.IDLE), settings)).isFalse()
    }
}
