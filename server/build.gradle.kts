plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    kotlin("plugin.serialization") version "2.3.20"
}

group = "me.repeater64.mpkseedfilter"
version = "1.0.0"
application {
    mainClass.set("me.repeater64.mpkseedfilter.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.server.cors.jvm)


    implementation("com.seedfinding:mc_feature:1.171.11")
    implementation("com.seedfinding:mc_math:1.171.0")
    implementation("com.seedfinding:mc_noise:1.171.1")
    implementation("com.seedfinding:mc_reversal:1.171.1")
    implementation("com.seedfinding:mc_terrain:1.171.1")
    implementation("com.seedfinding:mc_seed:1.171.2")
    implementation("com.seedfinding:mc_core:1.210.0")
    implementation("com.seedfinding:mc_biome:1.171.1")

    implementation("com.seedfinding:latticg:1.06@jar")

    implementation("Xinyuiii:BastionGenerator:1.0-repeater64")

    implementation("com.github.Kludwisz:FortressLoot:master-SNAPSHOT")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-okio:1.10.0")
    implementation("com.squareup.okio:okio:3.16.2")

    implementation("it.unimi.dsi:fastutil:8.2.1")
    implementation("com.mojang:datafixerupper:1.0.20")
}