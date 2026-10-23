plugins {
    id("encore.android.library")
    id("encore.compose")
}

android {
    namespace = "io.github.vednolacni.encore.color"
}

dependencies {
    // The wall turns with the sun, also by the clock alone (CoverPaletteTest).
    testImplementation(projects.domain)
}
