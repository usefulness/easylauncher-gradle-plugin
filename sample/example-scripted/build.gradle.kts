plugins {
    id("com.starter.application.android")
    id("com.starter.easylauncher")
}

android {
    namespace = "com.example.scripted"
}

val implementation by configurations

dependencies {
    implementation(project(":adaptive-support"))
}
