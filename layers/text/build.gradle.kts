plugins {
    id("encore.android.library")
    id("encore.compose")
    id("encore.robolectric")
}

android {
    namespace = "io.github.vednolacni.encore.text"
}

dependencies {
    // The words come as a StateFlow and as a composition local.
    api(platform(libs.compose.bom))
    api(libs.compose.runtime)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.ui)

    testImplementation(projects.textTesting)
}
