package io.github.vednolacni.encore.app

import io.github.vednolacni.encore.config.Settings
import io.github.vednolacni.encore.domain.LinkState
import io.github.vednolacni.encore.domain.PlayerStatus
import io.github.vednolacni.encore.domain.SourceState

/**
 * Music as Encore opens (setting "When opening"): armed when the app comes into sight, it decides
 * once Home Assistant has told how the speaker stands, within [WAIT_MS]. A Music Assistant
 * speaker that is idle or off then plays songs from the library at random; anything else, a song
 * playing or paused, is left as it is. It decides once per opening.
 */
internal class OpenPlay(private val now: () -> Long) {
    /** Until when it waits for the speaker's state; 0 while it is not armed. */
    private var armedUntil = 0L

    fun arm() {
        armedUntil = now() + WAIT_MS
    }

    fun disarm() {
        armedUntil = 0L
    }

    /** Whether [st] calls for music now, with the settings [s]; true at most once per [arm]. */
    fun wants(st: SourceState, s: Settings): Boolean {
        if (armedUntil == 0L) return false
        if (now() > armedUntil) {
            disarm()
            return false
        }
        // Until Home Assistant has told how the speaker stands, there is nothing to decide.
        if (st.link != LinkState.CONNECTED) return false
        val p = st.player ?: return false
        disarm()
        return s.playOnOpen && s.hasMusicAssistant && p.isMusicAssistant &&
            (p.status == PlayerStatus.IDLE || p.status == PlayerStatus.OFF)
    }

    companion object {
        /** Home Assistant usually tells within a second or two; a link that takes longer opens without music. */
        const val WAIT_MS = 20_000L
    }
}
