buildscript{
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48") // Hilt
        classpath("com.google.gms:google-services:4.3.15") // Google Services
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.8") // Firebase Crashlytics
        classpath("com.google.firebase:perf-plugin:1.4.1") // Firebase Performance Monitoring
    }


}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("com.android.library") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
}


