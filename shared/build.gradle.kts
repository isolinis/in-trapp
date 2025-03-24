import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.kotlin.gradle.dsl.JvmTarget




plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
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

    // Configuración moderna para iOS
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = true
            // Configuración esencial para recursos
            binaryOptions.apply {
                put("bundle_resources", "true")
                put("exportCommonResources", "true")
            }
        }
    }
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            // Fuerza la inclusión del archivo específico
            linkerOpts += "-resource-dir ${projectDir}/src/commonMain/resources"
            linkerOpts += "-include-binary ${projectDir}/src/commonMain/resources/videos/loginvideo.mp4"
        }
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.json)
                implementation(libs.ktor.client.serialization)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.components.resources)

            }
        }



        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.lifecycle.viewmodel.compose) // Para ViewModel
            implementation(libs.androidx.media3.exoplayer) 
            implementation(libs.androidx.media3.ui)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // Motor HTTP para iOS
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.runtime)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3-native-mt")
            implementation("org.jetbrains.kotlin:kotlin-stdlib")
            implementation("org.jetbrains.kotlinx:atomicfu:0.23.1")
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
            assets.srcDirs("src/commonMain/resources")
            //res.srcDirs("src/androidMain/res")
        }
    }

}


compose {
    resources {
        publicResClass = true // Hace que la clase Res sea pública
        packageOfResClass = "com.example.intrapp.generated.resources" // Define el paquete de la clase Res
        generateResClass = always // Genera la clase Res siempre
    }

}
// Solución alternativa para el error de sync
tasks.named("syncComposeResourcesForIos") {
    enabled = false // Desactiva la tarea problemática
}

// Crea una tarea alternativa para manejar recursos
tasks.register("manualSyncComposeResources") {
    dependsOn(":shared:linkDebugFrameworkIosArm64")
    dependsOn(":shared:linkDebugFrameworkIosX64")
    doLast {
        copy {
            from("src/iosMain/resources")
            into("build/compose/ios")
        }
    }
}