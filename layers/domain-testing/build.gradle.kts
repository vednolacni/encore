plugins {
    id("encore.android.library")
}

android {
    namespace = "io.github.vednolacni.encore.domain.testing"
}

dependencies {
    implementation(projects.artTesting)
    implementation(projects.domain)
}
