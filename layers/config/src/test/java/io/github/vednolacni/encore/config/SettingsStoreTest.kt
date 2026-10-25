package io.github.vednolacni.encore.config

import android.content.Context
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import io.github.vednolacni.encore.data.ha.HaCredentials
import io.github.vednolacni.encore.domain.Condition
import io.github.vednolacni.encore.domain.OwnValue
import io.github.vednolacni.encore.domain.QuickAction
import java.lang.reflect.Modifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Tokens are kept sealed: written sealed, read back opened, and old plain ones sealed on the next start. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SettingsStoreTest {
    /** Seals by reversing behind a mark, and notes what it was given to open: what the store holds. */
    private class FakeBox(private val sealing: Boolean = true, private val opening: Boolean = true) : SecretBox {
        val stored = mutableListOf<String>()
        override fun seal(plain: String) = if (sealing) "sealed:" + plain.reversed() else plain
        override fun open(stored: String): String? {
            this.stored += stored
            if (!opening) return null
            return if (isPlain(stored)) stored else stored.removePrefix("sealed:").reversed()
        }
        override fun isPlain(stored: String) = !stored.startsWith("sealed:")
    }

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `tokens are stored sealed, read back as they were, and only kept for their address`(): Unit = runBlocking {
        val box = FakeBox()
        val store = SettingsStore(context, box)
        val signedIn = Settings().withHa("http://192.168.1.5:8123", HaCredentials.SignedIn("refresh-secret", "http://192.168.1.23:8765/"))
            .withMa("http://192.168.1.5:8095", "ma-secret")
            .copy(entityId = "media_player.living_room")
        store.write(signedIn)
        val back = store.settings.first()
        assertThat(back.haCredentials).isEqualTo(HaCredentials.SignedIn("refresh-secret", "http://192.168.1.23:8765/"))
        assertThat(back.maToken).isEqualTo("ma-secret")
        // What the store holds is sealed: no secret in it as it is.
        assertThat(box.stored).isNotEmpty()
        assertThat(box.stored.none { it.contains("secret") }).isTrue()
        assertThat(box.stored.all { it.startsWith("sealed:") }).isTrue()

        // Another address without its credentials drops the old ones.
        store.write(back.withHa("http://10.0.0.9:8123", null))
        assertThat(store.settings.first().haCredentials).isNull()
    }

    @Test
    fun `every setting is read back as it was stored`(): Unit = runBlocking {
        val changed = Settings(
            haUrl = "http://192.168.1.5:8123",
            haToken = "long-lived",
            haRefreshToken = "refresh",
            haClientId = "http://192.168.1.23:8765/",
            entityId = "media_player.kitchen",
            otherSpeaker = "media_player.living_room",
            sunEntity = "sun.home",
            sleepAfterMinutes = 45,
            sleepAction = SleepAction.CLOSE,
            showClock = false,
            remoteControl = false,
            showProgressBar = false,
            showNext = false,
            showAlbum = false,
            showGroup = false,
            volumeDisplay = VolumeDisplay.ALWAYS,
            showValues = false,
            intensity = Intensity.STRONG,
            volumeKeys = true,
            appearance = AppearanceMode.DARK,
            lyrics = true,
            maUrl = "http://192.168.1.5:8095",
            maToken = "ma-token",
            tvSpeaker = true,
            background = BackgroundMode.WALL,
            motion = MotionLevel.LIVELY,
            roomLights = RoomLights("light.left", "light.top", "light.right"),
            roomLightBrightness = RoomLightBrightness.P90,
            lightsOnlyOn = true,
            spokenSkip = false,
            quickActions = listOf(QuickAction("Ungroup", "script.ungroup", condition = Condition("media_player.kitchen", "group_members", Condition.Test.ABOVE, "1"))),
            ownValues = listOf(OwnValue("Outside", "sensor.outside_temperature")),
            updateCheck = true,
        )
        // A setting the store forgets would come back as its default, so every one differs from it here.
        val defaults = Settings()
        for (field in Settings::class.java.declaredFields.filter { !Modifier.isStatic(it.modifiers) }) {
            field.isAccessible = true
            assertWithMessage("${field.name} is set apart from its default").that(field.get(changed)).isNotEqualTo(field.get(defaults))
        }
        val store = SettingsStore(context, FakeBox())
        store.write(changed)
        assertThat(store.settings.first()).isEqualTo(changed)
    }

    @Test
    fun `bringing a store up to date touches nothing without a plain token, not even one that cannot be opened now`() {
        // The keystore cannot open the sealed token at this moment: it must stay as it is.
        val store = SettingsStore(context, FakeBox(opening = false))
        val token = stringPreferencesKey("ha_token")
        val url = stringPreferencesKey("ha_url")
        val p = mutablePreferencesOf(token to "sealed:terces", url to "http://192.168.1.5:8123")
        store.migrate(p)
        assertThat(p.asMap().mapKeys { it.key.name }).containsExactly(
            "ha_token", "sealed:terces",
            "ha_url", "http://192.168.1.5:8123",
        )
    }

    @Test
    fun `tokens stored by an earlier version are sealed on the next start`(): Unit = runBlocking {
        // An earlier version wrote the token as it is.
        SettingsStore(context, FakeBox(sealing = false))
            .write(Settings().withHa("http://192.168.1.5:8123", HaCredentials.LongLived("long-lived-secret")))

        val box = FakeBox()
        val store = SettingsStore(context, box)
        assertThat(store.settings.first().haCredentials).isEqualTo(HaCredentials.LongLived("long-lived-secret"))
        assertThat(box.stored).contains("long-lived-secret")
        store.migrate()
        box.stored.clear()
        assertThat(store.settings.first().haCredentials).isEqualTo(HaCredentials.LongLived("long-lived-secret"))
        assertThat(box.stored).containsExactly("sealed:" + "long-lived-secret".reversed())
    }

    @Test
    fun `without a keystore secrets pass through as they are`() {
        // The JVM has no Android keystore: the box keeps the app working, unsealed.
        val box = KeystoreSecretBox()
        assertThat(box.open(box.seal("token"))).isEqualTo("token")
    }
}
