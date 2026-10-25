package io.github.vednolacni.encore.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Test

/** Encore's releases on a stand-in for GitHub. */
class ReleasesTest {
    private val server = MockWebServer().apply { start() }
    private val base = server.url("/").toString().trimEnd('/')
    private val releases = Releases(OkHttpClient(), api = base, site = base)

    @After
    fun stop() = server.close()

    @Test
    fun `the newest release is asked for by its tag, and only a version this app reads counts`(): Unit = runBlocking {
        server.enqueue(MockResponse.Builder().body("""{"tag_name":"v2026.11.0","draft":false,"prerelease":false}""").build())
        assertThat(releases.latest()).isEqualTo("v2026.11.0")
        val request = server.takeRequest()
        assertThat(request.url.encodedPath).isEqualTo("/repos/vednolacni/encore/releases/latest")
        assertThat(request.headers["Accept"]).isEqualTo("application/vnd.github+json")
        assertThat(request.headers["Authorization"]).isNull()
        server.enqueue(MockResponse.Builder().body("""{"tag_name":"6.4.0"}""").build())
        assertThat(releases.latest()).isNull()
        server.enqueue(MockResponse.Builder().code(403).build())
        assertThrows(HttpStatusException::class.java) { runBlocking { releases.latest() } }
    }

    @Test
    fun `the app of a release comes from its download`(): Unit = runBlocking {
        server.enqueue(MockResponse.Builder().body("apk").build())
        var got = ""
        assertThat(releases.download("v2026.11.0") { got = it.readBytes().decodeToString() }).isTrue()
        assertThat(got).isEqualTo("apk")
        assertThat(server.takeRequest().url.encodedPath).isEqualTo("/vednolacni/encore/releases/download/v2026.11.0/encore.apk")
        server.enqueue(MockResponse.Builder().code(404).build())
        assertThat(releases.download("v2026.11.1") { }).isFalse()
        assertThrows(IllegalArgumentException::class.java) { runBlocking { releases.download("../other") { } } }
    }
}
