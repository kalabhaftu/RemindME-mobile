import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    android {
        minSdk = 24
        compileSdk = 35
        namespace = "com.kyant.backdrop"
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    jvm("desktop")

    js(IR) {
        browser()
    }
    wasmJs {
        browser()
    }

    macosArm64()
    iosArm64("iosArm64")
    iosSimulatorArm64("iosSimulatorArm64")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.graphics)
                implementation(libs.kyant.shapes)
                implementation("org.jetbrains:annotations:26.1.0")
            }
        }

        val skikoMain by creating {
            dependsOn(commonMain)
        }

        val desktopMain by getting {
            dependsOn(skikoMain)
        }

        val macosArm64Main by getting {
            dependsOn(skikoMain)
        }

        val iosMain by creating {
            dependsOn(skikoMain)
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val jsMain by getting {
            dependsOn(skikoMain)
        }

        val wasmJsMain by getting {
            dependsOn(skikoMain)
        }

        all {
            languageSettings.enableLanguageFeature("ContextParameters")
        }
    }
}

