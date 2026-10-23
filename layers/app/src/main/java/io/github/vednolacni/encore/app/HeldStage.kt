package io.github.vednolacni.encore.app

import io.github.vednolacni.encore.domain.RestReason
import io.github.vednolacni.encore.domain.Screen
import io.github.vednolacni.encore.render.ArtworkScene

/** The stage kept from before the clock or the setup page, and what shows while a song waits for its first scene. */
internal object HeldStage {
    /**
     * Whether the stage kept from [before] must not come back for [stage]: after the clock or the
     * setup page, a song other than the one kept waits for its own scene, and so does the same
     * song when the wall has changed meanwhile (its scene no longer [fits]). Otherwise the song
     * before would come up first and the lights would take its colors, or the song would come
     * back on the old wall and cross over. While the stage shows, it stays until the next song's
     * scene is ready, and the change runs on it.
     */
    fun stale(before: UiState, fits: (ArtworkScene) -> Boolean, stage: Screen.Stage?): Boolean {
        if (stage == null || before.screen is Screen.Stage) return false
        val kept = before.stage ?: return false
        return kept.track.key != stage.track.key || !fits(kept.scene)
    }

    /**
     * What shows: [now], or, while its song has nothing to show yet (its first scene is being
     * made) after the clock, that clock as [before] showed it. So the screen never waits in the
     * dark, and the keys, the quick settings and the lights act on what shows. A screen that says
     * something is wrong, or the setup page, goes through the dark instead: what it said is no
     * longer so.
     */
    fun shown(before: UiState, now: UiState): UiState {
        if (!quiet(before) || now.screen !is Screen.Stage || now.stage != null) return now
        return now.copy(screen = before.screen)
    }

    /** The clock: paused, or nothing plays. */
    private fun quiet(before: UiState): Boolean {
        val reason = (before.screen as? Screen.Rest)?.reason
        return reason == RestReason.PAUSED || reason == RestReason.IDLE
    }
}
