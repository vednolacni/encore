package io.github.vednolacni.encore.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.github.vednolacni.encore.config.Settings
import io.github.vednolacni.encore.data.NowPlayingSource
import io.github.vednolacni.encore.data.RemoteCommand
import io.github.vednolacni.encore.domain.LinkState
import io.github.vednolacni.encore.domain.PlayerSnapshot
import io.github.vednolacni.encore.domain.PlayerStatus
import io.github.vednolacni.encore.domain.Screen
import io.github.vednolacni.encore.domain.SourceState
import io.github.vednolacni.encore.domain.TrackInfo
import io.github.vednolacni.encore.text.AppLanguage
import io.github.vednolacni.encore.text.TestTexts
import io.github.vednolacni.encore.text.Texts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * A jump with the remote belongs to the item it was made for: when another item plays before the
 * jump is sent, the jump goes with its item, and the next one never lands on its place. A held
 * key seeks the farther the longer it is held, and sends the place it reached once let go.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SeekingTest {
    private val test = TestScope(StandardTestDispatcher())
    private val a = TrackInfo("First chapter", listOf("Reader"), durationMs = 1_200_000L)
    private val b = TrackInfo("Second chapter", listOf("Reader"), durationMs = 1_500_000L)

    /** What the player was asked to jump to. */
    private val seeks = mutableListOf<Long>()

    private val source = object : NowPlayingSource {
        override val state: StateFlow<SourceState> = MutableStateFlow(SourceState(LinkState.CONNECTED))
        override fun start() = Unit
        override fun stop() = Unit
        override suspend fun send(command: RemoteCommand) = Unit
        override suspend fun seek(positionMs: Long) {
            seeks += positionMs
        }
    }

    private fun at(track: TrackInfo, positionMs: Long) =
        SourceState(LinkState.CONNECTED, player = PlayerSnapshot(PlayerStatus.PAUSED, track, positionMs = positionMs))

    private var ui = UiState(screen = Screen.Stage(a, PlayerStatus.PAUSED, null), source = at(a, 600_000L))

    private val host = object : ControllerHost {
        override val lock: Any = this
        override val scope = test
        override val now: () -> Long = { test.testScheduler.currentTime }
        override val settings get() = Settings()
        override val ui: UiState get() = this@SeekingTest.ui
        override val source: NowPlayingSource = this@SeekingTest.source
        override val texts: Texts = TestTexts.english()
        override var language: AppLanguage = AppLanguage.ENGLISH
        override val speaker = io.github.vednolacni.encore.domain.SpeakerLink.OFF
        override fun recompute() = Unit
        override fun showMenu() = Unit
        override fun openMenu() = Unit
        override fun applySettings(updated: Settings) = Unit
        override fun send(command: RemoteCommand) = Unit
        override fun requestSetup(show: Boolean) = Unit
    }

    private val seeking = Seeking(host)

    @Test
    fun aJumpIsSentOnceThePressesStop() {
        seeking.skip(+1)
        assertThat(seeking.position(host.now())).isEqualTo(630_000L)
        test.advanceTimeBy(Seeking.SETTLE_MS + 100)
        test.runCurrent()
        assertThat(seeks).containsExactly(630_000L)
    }

    @Test
    fun aJumpGoesWithItsItemWhenAnotherPlaysBeforeItIsSent() {
        seeking.skip(+1)
        // The next item starts (the phone, or the item ended) before the jump is sent.
        ui = ui.copy(screen = Screen.Stage(b, PlayerStatus.PAUSED, null), source = at(b, 0L))
        seeking.onReport(ui.source, 0L, host.now())
        test.advanceTimeBy(Seeking.SETTLE_MS + 100)
        test.runCurrent()
        assertThat(seeks).isEmpty()
        assertThat(seeking.position(host.now())).isEqualTo(0L)
    }

    @Test
    fun aHeldKeySeeksFartherTheLongerItIsHeldAndSendsThePlaceOnceLetGo() {
        seeking.hold(+1, 0)
        assertThat(seeking.position(host.now())).isEqualTo(600_000L)
        // Ten seconds a second at first, then faster: 15 s after a second, 75 s after three.
        seeking.hold(+1, 1_000)
        assertThat(seeking.position(host.now())).isEqualTo(615_000L)
        seeking.hold(+1, 3_000)
        assertThat(seeking.position(host.now())).isEqualTo(675_000L)
        assertThat(Seeking.held(4_000)).isEqualTo(115_000L)
        // Nothing goes to the player while the key is held, and the place reached goes as it is let go.
        test.advanceTimeBy(Seeking.SETTLE_MS + 100)
        test.runCurrent()
        assertThat(seeks).isEmpty()
        seeking.letGo()
        test.runCurrent()
        assertThat(seeks).containsExactly(675_000L)
    }

    @Test
    fun aHeldKeyStopsAtTheItemsStart() {
        seeking.hold(-1, 0)
        seeking.hold(-1, 20_000)
        assertThat(seeking.position(host.now())).isEqualTo(0L)
    }

    @Test
    fun aJumpOnItsWayIsShownForItsOwnItemOnly() {
        seeking.skip(+1)
        assertThat(seeking.position(host.now(), at(b, 5_000L))).isEqualTo(5_000L)
        assertThat(seeking.position(host.now(), at(a, 600_000L))).isEqualTo(630_000L)
    }
}
