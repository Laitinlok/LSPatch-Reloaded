val defaultManagerPackageName: String by rootProject.extra
val apiCode: Int by rootProject.extra
val verCode: Int by rootProject.extra
val verName: String by rootProject.extra
val coreVerCode: Int by rootProject.extra
val coreVerName: String by rootProject.extra

plugins {
    alias(libs.plugins.agp.app)
    alias(lspatch.plugins.google.devtools.ksp)
    alias(lspatch.plugins.rikka.tools.refine)
    alias(lspatch.plugins.kotlin.android)
    alias(lspatch.plugins.compose.compiler)
    id("kotlin-parcelize")
    kotlin("kapt")
    alias(lspatch.plugins.googleHiltAndroid)
}

android {
    defaultConfig {
        applicationId = defaultManagerPackageName
    }

    androidResources {
        noCompress.add(".so")
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            proguardFiles("proguard-rules-debug.pro")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        all {
            sourceSets[name].assets.srcDirs(rootProject.projectDir.resolve("out/assets/$name"))
        }
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    namespace = "org.lsposed.lspatch"

    applicationVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}

afterEvaluate {
    android.applicationVariants.forEach { variant ->
        val variantLowered = variant.name.lowercase()
        val variantCapped = variant.name.replaceFirstChar { it.uppercase() }

        task<Copy>("copy${variantCapped}Assets") {
            dependsOn(":meta-loader:copy$variantCapped")
            dependsOn(":patch-loader:copy$variantCapped")
            tasks["merge${variantCapped}Assets"].dependsOn(this)

            into("$buildDir/intermediates/assets/$variantLowered/merge${variantCapped}Assets")
            from("${rootProject.projectDir}/out/assets/${variant.name}")
        }

        task<Copy>("build$variantCapped") {
            dependsOn(tasks["assemble$variantCapped"])
            from(variant.outputs.map { it.outputFile })
            into("${rootProject.projectDir}/out/$variantLowered")
            rename(".*.apk", "manager-v$verName-$verCode-$variantLowered.apk")
        }
    }
}

dependencies {
    implementation(projects.patch)
    implementation(projects.services.daemonService)
    implementation(projects.share.android)
    implementation(projects.share.java)
    implementation(platform(lspatch.androidx.compose.bom))

    implementation(lspatch.google.hilt.android)
    kapt(lspatch.google.hilt.android.compiler)
    implementation(lspatch.androidx.hilt.navigation.compose)

    annotationProcessor(lspatch.androidx.room.compiler)
    compileOnly(lspatch.rikka.hidden.stub)
    debugImplementation(lspatch.androidx.compose.ui.tooling)
    debugImplementation(lspatch.androidx.customview)
    debugImplementation(lspatch.androidx.customview.poolingcontainer)
    implementation(lspatch.androidx.activity.compose)
    implementation(lspatch.androidx.compose.material)
    implementation(lspatch.androidx.compose.material.icons.extended)
    implementation(lspatch.androidx.compose.material3)
    implementation(lspatch.androidx.compose.ui)
    implementation(lspatch.androidx.compose.ui.tooling.preview)
    implementation(lspatch.androidx.core.ktx)
    implementation(lspatch.androidx.lifecycle.viewmodel.compose)
    implementation(lspatch.androidx.navigation.compose)
    implementation(libs.androidx.preference)
    implementation(lspatch.androidx.room.ktx)
    implementation(lspatch.androidx.room.runtime)
    implementation(libs.material)
    implementation(libs.gson)
    implementation(lspatch.rikka.shizuku.api)
    implementation(lspatch.rikka.shizuku.provider)
    implementation(lspatch.rikka.refine)
    implementation(libs.appiconloader)
    ksp(lspatch.androidx.room.compiler)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}