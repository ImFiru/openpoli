import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
}

kotlin {
    // Android library target. The application wrapper lives in :androidApp,
    // because AGP 9 no longer allows com.android.application on a KMP module.
    android {
        namespace = "dev.openpoli.app.shared"
        compileSdk {
            version = release(37) {
                minorApiLevel = 1
            }
        }
        minSdk = 24

        withHostTest {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            // Compose's deprecated `compose.components.uiToolingPreview` accessor,
            // replaced by the artifact it points at.
            implementation("org.jetbrains.compose.ui:ui-tooling-preview:${libs.versions.compose.get()}")
            implementation(libs.compose.material3)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        getByName("desktopMain") {
            dependencies {
                // compose.desktop.currentOs is deprecated; Compose 1.13's desktop
                // ui artifact carries the skiko runtimes for every desktop OS and
                // packaging strips the non-target ones.
                implementation("org.jetbrains.compose.ui:ui:${libs.versions.compose.get()}")
                implementation(libs.ktor.client.cio)
                // Supplies Dispatchers.Main on the JVM; without it viewModelScope
                // throws "Module with the Main dispatcher is missing".
                implementation(libs.kotlinx.coroutines.swing)
                // Ktor's CIO engine logs through SLF4J; without a provider it warns.
                implementation(libs.slf4j.simple)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

// Room 3 generates code per target; KSP only, never KAPT.
dependencies {
    add("kspAndroid", libs.room3.compiler)
    add("kspIosArm64", libs.room3.compiler)
    add("kspIosSimulatorArm64", libs.room3.compiler)
    add("kspDesktop", libs.room3.compiler)
}

compose.desktop {
    application {
        mainClass = "dev.openpoli.app.MainKt"
    }
}
