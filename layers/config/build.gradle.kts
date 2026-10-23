plugins {
    id("encore.android.library")
    id("encore.compose")
    id("encore.robolectric")
}

android {
    namespace = "io.github.vednolacni.encore.config"
}

dependencies {
    implementation(projects.color)
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.json)
    implementation(projects.text)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.datastore.preferences)
    // The settings come as a flow; the own buttons and values are stored as JSON.
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    testImplementation(projects.textTesting)
}
