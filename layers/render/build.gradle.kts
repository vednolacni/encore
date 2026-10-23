plugins {
    id("encore.android.library")
    id("encore.compose")
    id("encore.robolectric")
}

android {
    namespace = "io.github.vednolacni.encore.render"
}

dependencies {
    implementation(projects.art)
    implementation(projects.color)
    implementation(projects.math)
    implementation(libs.androidx.core.ktx)
    implementation(libs.zxing.core)
}
