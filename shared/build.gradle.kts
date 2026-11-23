import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    // Room's documented KMP setup requires `expect object AppDatabaseConstructor`.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // Android library target. The application wrapper lives in :androidApp,
    // because AGP 9 no longer allows com.android.application on a KMP module.
    android {
        namespace = "dev.openpoli.app.shared"
        compileSdk {
            version =
                release(37) {
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
            // Compose's `compose.runtime/foundation/ui` accessors are deprecated;
            // reference the artifacts they expand to.
            implementation("org.jetbrains.compose.runtime:runtime:${libs.versions.compose.get()}")
            implementation("org.jetbrains.compose.foundation:foundation:${libs.versions.compose.get()}")
            implementation("org.jetbrains.compose.ui:ui:${libs.versions.compose.get()}")
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
                // ui artifact (already on commonMain) carries the skiko runtimes.
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

ksp {
    // Room exports its schema JSON here; commit it when migrations begin.
    arg("room.schemaLocation", "$projectDir/schemas")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    parallel = true
    source.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/commonTest/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/desktopMain/kotlin",
        ),
    )
}

ktlint {
    // Never lint generated KSP/Room code or Gradle output.
    filter {
        exclude { element ->
            element.file.path
                .replace('\\', '/')
                .contains("/build/")
        }
    }
}

// One command that means "everything must pass": tests plus lint.
tasks.named("check") {
    dependsOn("ktlintCheck", "detekt")
}

compose.desktop {
    application {
        mainClass = "dev.openpoli.app.MainKt"
    }
}
