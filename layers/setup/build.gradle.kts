plugins {
    id("encore.android.library")
    id("encore.compose")
    id("encore.robolectric")
}

android {
    namespace = "io.github.vednolacni.encore.setup"
}

dependencies {
    implementation(projects.config)
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.json)
    implementation(projects.render)
    implementation(projects.text)
    // The setup page runs in the scope it is given, tells its state as a flow, asks Home
    // Assistant with OkHttp and reads the scripts' fields as JSON.
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.okhttp)

    testImplementation(projects.textTesting)
    testImplementation(libs.okhttp.mockwebserver)
}
