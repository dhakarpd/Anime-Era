plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp.devtools)
}

android {
    namespace = "com.github.dhakarpd.animeera"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.github.dhakarpd.animeera"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    // Lifecycle ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // Compose integration for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coil (Network Image Loading)
    implementation(libs.coil.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Jetpack Compose ViewModel integration
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // for annotation processing

    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.room.testing)

    testImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.core.testing)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.mockwebserver)
    // MockK for unit tests
    testImplementation(libs.mockk)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
}