import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.samueljuma.firebaseinaction"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    val props = gradleLocalProperties(rootDir, providers)

    defaultConfig {
        applicationId = "com.samueljuma.firebaseinaction"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val webClientId = props.getProperty("WEB_CLIENT_ID") ?: ""
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file(props.getProperty("KEYSTORE_PATH"))
            storePassword = props.getProperty("KEYSTORE_PASSWORD")
            keyAlias = props.getProperty("KEY_ALIAS")
            keyPassword = props.getProperty("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // TODO: re-enable before publishing; needs `firebase login` for mapping file upload
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    packaging {
        resources {
            excludes += setOf("/*.prof", "/*.profm")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    flavorDimensions += "environment"

    productFlavors {
        val emulatorHost = props.getProperty("emulator.host", "10.0.2.2")

        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("Boolean", "USE_EMULATOR", "true")
            buildConfigField("String", "EMULATOR_HOST", "\"$emulatorHost\"")

        }
        create("prod") {
            dimension = "environment"
            buildConfigField("Boolean", "USE_EMULATOR", "false")
            buildConfigField("String", "EMULATOR_HOST", "\"\"")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.bundles.koin)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose )
    implementation(libs.timber)

    // For Sign In With Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.identity)

    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.splash.screen)

    implementation(libs.bundles.lifecycle)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    //Work Manager
    implementation(libs.work.runtime.ktx)
    implementation(libs.coil.compose)
}