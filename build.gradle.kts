// Top-level build file for FSD SPIN.
// Open this folder in Android Studio and let it sync Gradle.
//
// NOTE: with Kotlin 2.0+, the Compose compiler is the Kotlin plugin
// "org.jetbrains.kotlin.plugin.compose" (version = Kotlin version).
// The shorter id "org.jetbrains.kotlin.compose" does not exist and
// fails plugin resolution on Gradle — that was the run #10 failure.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
