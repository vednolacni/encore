package io.github.hazymorning.klangbild.app

import com.google.common.truth.Truth.assertThat
import io.github.hazymorning.klangbild.config.RoomLights
import io.github.hazymorning.klangbild.config.Settings
import io.github.hazymorning.klangbild.domain.FieldClock
import org.junit.Test

class LightTargetsTest {
    private val targets = LightTargets(FieldClock { 0L }) { 0L }

    @Test
    fun `without lights or a song on stage the lights have nothing to show`() {
        assertThat(targets.target(Settings(), UiState(), null, playing = false)).isNull()
        val lights = Settings(roomLights = RoomLights("light.left", "light.middle", "light.right"))
        assertThat(targets.target(lights, UiState(), null, playing = false)).isNull()
    }
}
