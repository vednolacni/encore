plugins {
    id("encore.android.library")
    id("encore.compose")
    id("encore.robolectric")
}

android {
    namespace = "io.github.vednolacni.encore.domain"
}

dependencies {
    implementation(projects.json)
    implementation(projects.text)
    // The values and conditions of one's own read Home Assistant's JSON as it comes.
    api(libs.kotlinx.serialization.json)

    testImplementation(projects.textTesting)
}
