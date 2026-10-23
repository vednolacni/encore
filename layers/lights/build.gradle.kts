plugins {
    id("encore.android.library")
}

android {
    namespace = "io.github.vednolacni.encore.lights"
}

dependencies {
    implementation(projects.color)
    implementation(projects.config)
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.math)
    implementation(projects.render)
    // The lights run in the scope they are given.
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlinx.coroutines.test)
}
