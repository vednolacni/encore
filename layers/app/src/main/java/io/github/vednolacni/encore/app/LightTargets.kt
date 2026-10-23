package io.github.vednolacni.encore.app

import io.github.vednolacni.encore.config.Settings
import io.github.vednolacni.encore.domain.SourceState
import io.github.vednolacni.encore.domain.FieldClock
import io.github.vednolacni.encore.domain.Screen
import io.github.vednolacni.encore.domain.SunClock
import io.github.vednolacni.encore.lights.CoverLightColors
import io.github.vednolacni.encore.lights.LightSync
import io.github.vednolacni.encore.math.smoothstep
import io.github.vednolacni.encore.render.FieldScene

/**
 * What the lights are to show: the colors of the scene on stage, one color for each light,
 * worked out once per scene.
 */
internal class LightTargets(
    private val fieldClock: FieldClock,
    private val now: () -> Long,
) {
    /** The colors for a cover's field, made once per field (a new object fades the lights to it). */
    private var coverColors: Pair<Any, CoverLightColors>? = null

    /** The target for the lights now, or null when they have nothing to show. */
    fun target(s: Settings, ui: UiState, stage: Screen.Stage?, playing: Boolean): LightSync.Target? {
        if (!s.roomLights.isSet) return null
        // By the sun: full brightness by day, dimmer from dusk, when the room around the TV is dark.
        val night = night(ui.source)
        if (stage == null) return null
        val shown = ui.stage
        val field = shown?.scene?.field ?: return null
        return LightSync.Target(
            lights = s.roomLights,
            level = s.roomLightBrightness,
            colors = coverColors(field, s.motion.speed > 0f),
            song = shown.track.key,
            playing = playing,
            night = night,
            warmth = night,
            motion = s.motion.speed,
            fieldTime = fieldClock::now,
            onlyOn = s.lightsOnlyOn,
        )
    }

    private fun coverColors(field: FieldScene, moving: Boolean): CoverLightColors {
        val key = field to moving
        coverColors?.takeIf { it.first == key }?.let { return it.second }
        return CoverLightColors(field, moving).also { coverColors = key to it }
    }

    /** 0 by day to 1 at night, by the sun (Home Assistant's elevation, else the local clock): how far the lights dim. */
    private fun night(src: SourceState): Float {
        val elevation = src.sunElevation ?: SunClock.elevationAt(now())
        return smoothstep(DAY_ELEVATION, NIGHT_ELEVATION, elevation.toFloat())
    }

    companion object {
        /** The lights are at full brightness with the sun this high (degrees), at their night level from this low. */
        const val DAY_ELEVATION = 6f
        const val NIGHT_ELEVATION = -8f
    }
}
