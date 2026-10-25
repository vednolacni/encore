package io.github.vednolacni.encore

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import io.github.vednolacni.encore.app.NowPlayingController
import io.github.vednolacni.encore.app.Updates
import io.github.vednolacni.encore.config.KeystoreSecretBox
import io.github.vednolacni.encore.config.SettingsStore
import io.github.vednolacni.encore.config.UpdateStore
import io.github.vednolacni.encore.data.Releases
import io.github.vednolacni.encore.data.ha.LocalNetwork
import io.github.vednolacni.encore.speaker.SpeakerStore
import io.github.vednolacni.encore.text.Localizer
import io.github.vednolacni.encore.text.PlatformLanguageStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/** Manual dependency graph. The app is small enough not to need a DI framework. */
class AppGraph(context: Context) {
    private val app = context.applicationContext

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val http: OkHttpClient = OkHttpClient.Builder()
        // Covers come again (the album's next song, a step back): kept on disk as the servers allow.
        .cache(Cache(File(context.cacheDir, "http"), HTTP_CACHE_BYTES))
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        // WebSocket keep-alive is handled by HA pings; OkHttp answers server pings itself.
        .addInterceptor(LocalNetwork.CleartextGuard())
        // The guard checks every redirect hop, too: a picture link must not lead to plain HTTP outside the home.
        .addNetworkInterceptor(LocalNetwork.CleartextGuard())
        .build()

    /** For requests that carry a token: no redirects, so a token never goes to another host than its own. */
    val tokenHttp: OkHttpClient = http.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        // What a token fetches is not written to disk.
        .cache(null)
        .build()

    val settings = SettingsStore(context.applicationContext)

    /** The words in the language chosen: the system's, or one from the menu. */
    val localizer = Localizer(context.applicationContext, PlatformLanguageStore(context.applicationContext))

    /** This TV's speaker while its service runs ([SpeakerService]). */
    val speakerHub = SpeakerHub()

    /** Encore's own new versions, from its releases on GitHub. */
    private val releases = Releases(http)
    private val updateStore = UpdateStore(app)
    private val updates = Updates(
        scope, BuildConfig.VERSION_NAME,
        latest = releases::latest,
        install = ::install,
        recall = updateStore::recall,
        remember = updateStore::remember,
        now = System::currentTimeMillis,
        log = { Log.i(NowPlayingController.TAG, it) },
    )

    val controller = NowPlayingController(
        scope, http, tokenHttp, settings, localizer,
        speaker = speakerHub.status,
        pairingToken = { speakerHub.speaker?.pairingToken },
        ownSound = { speakerHub.speaker?.sound },
        updates = updates,
    )

    /** The new version, kept until Android's installer has read it. */
    private val update = File(File(app.cacheDir, "updates"), "encore.apk")

    /**
     * Downloads the release [tag] to where the file provider hands it to Android's installer,
     * which asks the person first (and, the first time, for leave to install apps from Encore).
     */
    private suspend fun install(tag: String): Boolean {
        val downloaded = withContext(Dispatchers.IO) {
            update.parentFile?.mkdirs()
            releases.download(tag) { apk -> update.outputStream().use { apk.copyTo(it) } }
        }
        if (!downloaded) return false
        val view = Intent(Intent.ACTION_VIEW)
            .setDataAndType(FileProvider.getUriForFile(app, "${app.packageName}.updates", update), "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return withContext(Dispatchers.Main) {
            try {
                app.startActivity(view)
                true
            } catch (_: ActivityNotFoundException) {
                false
            }
        }
    }

    /** What the speaker keeps (its key, its pairings), opened when it is first needed. */
    val speakerStore: SpeakerStore by lazy { SpeakerStore(app, KeystoreSecretBox()) }

    init {
        // What an earlier version stored is brought up to date now rather than on the next change.
        scope.launch { settings.migrate() }
        // A download left from an earlier day has been installed or turned down.
        scope.launch(Dispatchers.IO) {
            if (System.currentTimeMillis() - update.lastModified() > UPDATE_KEPT_MS) update.delete()
        }
    }
}

class EncoreApp : Application() {
    lateinit var graph: AppGraph
        private set

    /**
     * A new language (chosen in the system's settings, or the system's own), and the system's
     * 12 or 24 hour clock: the texts are made anew, on the screen and on the setup page.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        graph.localizer.refresh()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Nothing shows in the background: the covers and the scenes go, and are made again on return.
        if (level >= TRIM_MEMORY_UI_HIDDEN) graph.controller.releaseMemory()
    }

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
        val refresh = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = graph.localizer.refresh()
        }
        // Both are protected broadcasts that only the system sends. Not ContextCompat: below API 33
        // it needs a signature permission of the app's own (see the manifest).
        val system = IntentFilter().apply {
            // Sent when the system's 12 or 24 hour setting changes, too.
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_LOCALE_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(refresh, system, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(refresh, system)
        }
        // When the network comes back (Wi-Fi hiccup), reconnect right away instead of
        // waiting for the backoff timer.
        getSystemService(ConnectivityManager::class.java)?.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    graph.controller.networkAvailable()
                    graph.speakerHub.speaker?.retryNow()
                }
            },
        )
    }
}

val Context.graph: AppGraph get() = (applicationContext as EncoreApp).graph

/** Room for covers on disk. */
private const val HTTP_CACHE_BYTES = 32L * 1024 * 1024

/** A new version downloaded this long ago (ms) has been installed or turned down. */
private const val UPDATE_KEPT_MS = 24 * 3_600_000L
