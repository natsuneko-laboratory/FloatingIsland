rootProject.name = "floating-island"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = uri("https://maven.minecraftforge.net/"))
        maven(url = uri("https://maven.minecraftforge.net/")) { name = "Minecraft Forge" }

    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}
