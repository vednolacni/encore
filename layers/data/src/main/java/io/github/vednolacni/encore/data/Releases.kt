package io.github.vednolacni.encore.data

import io.github.vednolacni.encore.domain.AppVersion
import io.github.vednolacni.encore.json.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * Encore's releases on GitHub, asked for without credentials: the newest one published (not a
 * beta or a draft, which GitHub leaves out), and its app. [api] and [site] are GitHub's own but
 * in tests. Nothing of it is kept in [http]'s cache.
 */
class Releases(http: OkHttpClient, private val api: String = API, private val site: String = SITE) {
    private val http = http.newBuilder()
        .cache(null)
        .readTimeout(READ_MS, TimeUnit.MILLISECONDS)
        .build()

    /** The newest release's tag ("v2026.11.0"); null when it names no version this app reads. */
    suspend fun latest(): String? {
        val request = Request.Builder()
            .url("$api/repos/$REPOSITORY/releases/latest")
            .header("Accept", "application/vnd.github+json")
            .build()
        return http.fetch(request, CHECK_MS) { response ->
            if (!response.isSuccessful) throw HttpStatusException("GitHub", response.code)
            val tag = (Json.parseToJsonElement(response.body.string()) as? JsonObject)?.string("tag_name")
            tag?.takeIf { AppVersion.parse(it) != null }
        }
    }

    /**
     * Downloads the app of the release [tag] (one [latest] named) and hands it to [write] as it
     * comes (on a background thread); false when GitHub doesn't have it.
     */
    suspend fun download(tag: String, write: (InputStream) -> Unit): Boolean {
        require(AppVersion.parse(tag) != null) { "not a release's tag: $tag" }
        val request = Request.Builder().url("$site/$REPOSITORY/releases/download/$tag/$APK").build()
        return http.fetch(request, DOWNLOAD_MS) { response ->
            if (!response.isSuccessful) return@fetch false
            response.body.byteStream().use(write)
            true
        }
    }

    private companion object {
        const val API = "https://api.github.com"
        const val SITE = "https://github.com"
        const val REPOSITORY = "vednolacni/encore"

        /** Every release's app has this name. */
        const val APK = "encore.apk"

        /** The question may take this long (ms)... */
        const val CHECK_MS = 20_000L

        /** ...the download this long, and wait this long for its next bytes. */
        const val DOWNLOAD_MS = 600_000L
        const val READ_MS = 30_000L
    }
}
