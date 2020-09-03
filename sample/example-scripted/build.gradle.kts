apply(plugin = "com.starter.application.android")
apply(plugin = "com.starter.easylauncher")

val implementation by configurations

dependencies {
    implementation(project(":adaptive-support"))
}
