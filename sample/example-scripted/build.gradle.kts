plugins {
    id("com.starter.application.android")
    id("com.starter.easylauncher")
}

android {
    namespace = "com.example.scripted"
    buildTypes {
        named("release") {
            signingConfig = signingConfigs.findByName("debug")
        }
    }
}

dependencies {
    implementation(project(":adaptive-support"))
}
