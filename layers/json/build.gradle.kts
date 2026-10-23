plugins {
    id("encore.android.library")
}

android {
    namespace = "io.github.vednolacni.encore.json"
}

dependencies {
    // Its functions read the elements of kotlinx.serialization's JSON.
    api(libs.kotlinx.serialization.json)
}
