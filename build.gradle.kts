val kotlin_version: String = "1.8.0"
val ktor_version: String = "2.2.2"
val sqldelight_version: String = "1.5.4"

plugins {
    application
    id("com.squareup.sqldelight") version "1.5.4"
    kotlin("multiplatform") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
}

group = "me.yasuaki640"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

sqldelight {
    database("Database") {
        packageName = "com.example"
        verifyMigrations = true
    }
}

kotlin {
    val target = "api"
    val hostOs = System.getProperty("os.name")
    val isArm: Boolean = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val apiTarget = when {
        hostOs == "Mac OS X" -> if (isArm) macosArm64(target) else macosX64(target)
        hostOs == "Linux" -> if (isArm) linuxArm64(target) else macosX64(target)
        isMingwX64 -> mingwX64(target)
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    apiTarget.apply {
        binaries {
            executable {
                entryPoint = "com.example.main"
                linkerOpts.add("-lsqlite3")
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            // SQLDelight ORM will be generated here
        }
        val apiMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:native-driver:$sqldelight_version")
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-cio:$ktor_version")
                implementation("io.ktor:ktor-server-call-id:$ktor_version")
                implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
        val apiTest by getting
    }
}
