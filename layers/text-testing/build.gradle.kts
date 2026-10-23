plugins {
    id("encore.android.library")
}

android {
    namespace = "io.github.vednolacni.encore.text.testing"
}

dependencies {
    implementation(projects.text)
    implementation(libs.androidx.test.core)
}
