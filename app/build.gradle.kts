plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.foliolib.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.foliolib.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API keys (to be added to local.properties)
        buildConfigField("String", "GOOGLE_BOOKS_API_KEY", "\"${project.findProperty("GOOGLE_BOOKS_API_KEY") ?: ""}\"")
        buildConfigField("String", "ISBNDB_API_KEY", "\"${project.findProperty("ISBNDB_API_KEY") ?: ""}\"")
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
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation(libs.androidx.appcompat)

    // Hilt - Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)

    // Room - Local Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit & OkHttp - Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Moshi - JSON parsing
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)

    // Coil - Image Loading
    implementation(libs.coil.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle & ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // DataStore - Preferences
    implementation(libs.datastore.preferences)

    // WorkManager - Background tasks
    implementation(libs.work.runtime.ktx)

    // CameraX - ISBN Scanner
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // Barcode Scanning
    implementation(libs.zxing.core)
    implementation(libs.mlkit.barcode)

    // Accompanist - Compose utilities
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)

    // Lottie - Animations
    implementation(libs.lottie.compose)

    // Timber - Logging
    implementation(libs.timber)

    // Shimmer - Loading effect
    implementation(libs.compose.shimmer)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}