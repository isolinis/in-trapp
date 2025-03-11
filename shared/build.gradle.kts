import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            resources.srcDirs("src/commonMain/resources") // Añadir recursos de commonMain
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.json)
                implementation(libs.ktor.client.serialization)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }



        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.lifecycle.viewmodel.compose) // Para ViewModel
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // Motor HTTP para iOS
        }


    }
}

android {
    namespace = "com.example.intrapp"
    compileSdk = 35
    defaultConfig {
        minSdk = 28
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        getByName("main") {
            // Asegura que los recursos comunes están incluidos en el APK
            assets.srcDirs("src/commonMain/resources")
        }
    }
}