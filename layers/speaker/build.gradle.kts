import io.github.vednolacni.encore.gradle.reads

plugins {
    id("encore.android.library")
    id("encore.robolectric")
}

android {
    namespace = "io.github.vednolacni.encore.speaker"

    testOptions {
        unitTests.all {
            // The speaker against aiosendspin, the server Music Assistant runs (ci/sendspin_server.py):
            // -Pencore.sendspin=/path/to/a/python/with/aiosendspin
            providers.gradleProperty("encore.sendspin").orNull?.let { python -> it.systemProperty("encore.sendspin", python) }
            it.reads("encore.sendspinServer", layout.settingsDirectory.file("ci/sendspin_server.py"))
        }
    }
}

dependencies {
    implementation(projects.config)
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.sendspin)
    implementation(projects.sound)
    // The speaker runs in the scope it is given, on OkHttp's client, keeps its state in a DataStore
    // and tells how it stands as a flow.
    api(libs.androidx.datastore.preferences)
    api(libs.kotlinx.coroutines.core)
    api(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
}
