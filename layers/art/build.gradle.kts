plugins {
    id("encore.android.library")
}

android {
    namespace = "io.github.vednolacni.encore.art"

    testOptions {
        unitTests.all {
            // ChladniSheetTest draws every sand figure there; as the tests' output, the sheet comes
            // back with them from the build cache.
            it.outputs.dir(layout.buildDirectory.dir("screenshots/art"))
        }
    }
}
