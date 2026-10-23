package io.github.hazymorning.klangbild.config

import com.google.common.truth.Truth.assertThat
import io.github.hazymorning.klangbild.domain.EntityId
import org.junit.Test

class LaunchExtrasTest {
    @Test
    fun `stored extras update settings, invalid values are ignored`() {
        val s = LaunchExtras.applyTo(
            Settings(),
            mapOf(
                "ha_url" to "192.168.1.5:8123",
                "token" to "  abc.def  ",
                "entity" to "media_player.wohnzimmer",
                "sleep_minutes" to "15",
                "sleep_action" to "beenden",
                "clock" to "false",
            ),
        )
        assertThat(s.haUrl).isEqualTo("http://192.168.1.5:8123")
        assertThat(s.haToken).isEqualTo("abc.def")
        assertThat(s.entityId).isEqualTo("media_player.wohnzimmer")
        assertThat(s.sleepAfterMinutes).isEqualTo(15)
        assertThat(s.sleepAction).isEqualTo(SleepAction.CLOSE)
        assertThat(s.showClock).isFalse()
        assertThat(s.isConfigured).isTrue()

        val unchanged = LaunchExtras.applyTo(s, mapOf("entity" to "Wohnzimmer!", "sleep_minutes" to 0, "ha_url" to ""))
        assertThat(unchanged).isEqualTo(s)
    }

    @Test
    fun `a token never follows an address it was not made for`() {
        val s = Settings(haUrl = "http://192.168.1.5:8123", haToken = "ha", maUrl = "http://192.168.1.5:8095", maToken = "ma")
        // Another app on the TV must not be able to send the stored tokens elsewhere.
        val moved = LaunchExtras.applyTo(s, mapOf("ha_url" to "http://192.168.1.66:8123", "ma_url" to "192.168.1.66:8095"))
        assertThat(moved.haUrl).isEqualTo("http://192.168.1.66:8123")
        assertThat(moved.haToken).isNull()
        assertThat(moved.maUrl).isEqualTo("http://192.168.1.66:8095")
        assertThat(moved.maToken).isNull()
        assertThat(LaunchExtras.applyTo(s, mapOf("ha_url" to "192.168.1.5:8123")).haToken).isEqualTo("ha")
        val both = LaunchExtras.applyTo(s, mapOf("ma_url" to "mass.local:8095", "ma_token" to "neu"))
        assertThat(both.maUrl).isEqualTo("http://mass.local:8095")
        assertThat(both.maToken).isEqualTo("neu")
        assertThat(both.hasMusicAssistant).isTrue()
    }

    @Test
    fun `display options from extras`() {
        val s = LaunchExtras.applyTo(
            Settings(),
            mapOf(
                "time_line" to "aus",
                "next" to false,
                "album" to "0",
                "group" to "nein",
                "volume" to "immer",
                "light" to "dezent",
                "volume_keys" to "ja",
                "remote" to "off",
                "values" to "off",
            ),
        )
        assertThat(s.showProgressBar).isFalse()
        assertThat(s.showNext).isFalse()
        assertThat(s.showAlbum).isFalse()
        assertThat(s.showGroup).isFalse()
        assertThat(s.volumeDisplay).isEqualTo(VolumeDisplay.ALWAYS)
        assertThat(s.intensity).isEqualTo(Intensity.SUBTLE)
        assertThat(s.volumeKeys).isTrue()
        assertThat(s.remoteControl).isFalse()
        assertThat(s.showValues).isFalse()
        assertThat(LaunchExtras.applyTo(s, mapOf("light" to "kräftig", "volume" to "bei_änderung")).let { it.intensity to it.volumeDisplay })
            .isEqualTo(Intensity.STRONG to VolumeDisplay.ON_CHANGE)
        assertThat(LaunchExtras.applyTo(s, mapOf("light" to "grell", "volume" to "laut"))).isEqualTo(s)
        assertThat(LaunchExtras.hasStoredExtras(mapOf("light" to "aus"))).isTrue()
    }

    @Test
    fun `the TV as a speaker is switched by an extra, never by a link`() {
        val on = LaunchExtras.applyTo(Settings(), mapOf("tv_speaker" to "on"))
        assertThat(on.tvSpeaker).isTrue()
        assertThat(LaunchExtras.applyTo(on, mapOf("tv_speaker" to false)).tvSpeaker).isFalse()
        assertThat(LaunchExtras.hasStoredExtras(mapOf("tv_speaker" to true))).isTrue()
        // A page in the TV's browser cannot make it a speaker.
        assertThat(LaunchExtras.fromLink(mapOf("tv_speaker" to "on"))).doesNotContainKey("tv_speaker")
        // While this TV is the speaker followed, an extra cannot take it away.
        val followed = Settings(entityId = EntityId.THIS_TV, tvSpeaker = true)
        assertThat(LaunchExtras.applyTo(followed, mapOf("tv_speaker" to false)).tvSpeaker).isTrue()
    }

    @Test
    fun `a link may set display options, never the connection`() {
        val extras = LaunchExtras.fromLink(
            mapOf("light" to "dezent", "album" to "aus", "ha_url" to "http://evil", "token" to "x", "entity" to "media_player.x", "setup" to "true"),
        )
        assertThat(extras.keys).containsExactly("light", "album")
        val s = LaunchExtras.applyTo(Settings(), extras)
        assertThat(s.intensity).isEqualTo(Intensity.SUBTLE)
        assertThat(s.showAlbum).isFalse()
        assertThat(s.haUrl).isNull()
        assertThat(LaunchExtras.session(extras)).isEqualTo(SessionOptions())
    }

    @Test
    fun `English names and values, with those of earlier versions still taken`() {
        val s = LaunchExtras.applyTo(
            Settings(),
            mapOf(
                "lights_left" to "light.tv_left", "lights_right" to "light.tv_right", "lights_level" to "70",
                "motion" to "slow", "background" to "plain", "light" to "subtle",
            ),
        )
        assertThat(s.roomLights).isEqualTo(RoomLights("light.tv_left", null, "light.tv_right"))
        assertThat(s.roomLightBrightness).isEqualTo(RoomLightBrightness.P70)
        assertThat(s.motion).isEqualTo(MotionLevel.GENTLE)
        assertThat(s.background).isEqualTo(BackgroundMode.WALL)
        assertThat(s.intensity).isEqualTo(Intensity.SUBTLE)
        // The German names of earlier versions: the same settings.
        val german = LaunchExtras.applyTo(
            Settings(),
            mapOf(
                "bars_left" to "light.tv_left", "bars_right" to "light.tv_right", "bar_level" to "70",
                "motion" to "langsam", "background" to "einfarbig", "light" to "dezent",
            ),
        )
        assertThat(german).isEqualTo(s)
        assertThat(LaunchExtras.hasStoredExtras(mapOf("lights_level" to "50"))).isTrue()
        assertThat(LaunchExtras.hasStoredExtras(mapOf("bar_level" to "50"))).isTrue()
        // A link may set the lights' level, never which lights.
        assertThat(LaunchExtras.fromLink(mapOf("lights_level" to "50", "lights_left" to "light.x")).keys)
            .containsExactly("lights_level")
    }

    @Test
    fun `session options`() {
        assertThat(LaunchExtras.session(mapOf("setup" to true))).isEqualTo(SessionOptions(showSetup = true))
        assertThat(LaunchExtras.session(mapOf("debug" to 1, "setup" to "ja")))
            .isEqualTo(SessionOptions(showSetup = true, debugOverlay = true))
        assertThat(LaunchExtras.hasStoredExtras(mapOf("debug" to true))).isFalse()
        assertThat(LaunchExtras.hasStoredExtras(mapOf("entity" to "media_player.x"))).isTrue()
    }
}
