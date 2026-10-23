plugins {
    id("encore.android.library")
    id("encore.compose")
    id("encore.robolectric")
}

android {
    namespace = "io.github.vednolacni.encore.data"
}

dependencies {
    implementation(projects.domain)
    implementation(projects.json)
    implementation(projects.sendspin)
    implementation(projects.sound)
    // Its API hands out flows, takes OkHttp's clients and speaks Home Assistant's JSON.
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.okhttp)

    testImplementation(projects.domainTesting)
    testImplementation(libs.okhttp.mockwebserver)
}
