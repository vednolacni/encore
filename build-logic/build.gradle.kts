plugins {
    `kotlin-dsl`
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    // Only compiled against: the plugins themselves come from the main build, which declares them.
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.kotlin.gradle.plugin.api)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "encore.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "encore.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("compose") {
            id = "encore.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("robolectric") {
            id = "encore.robolectric"
            implementationClass = "RobolectricConventionPlugin"
        }
    }
}
