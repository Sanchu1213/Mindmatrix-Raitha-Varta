plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

import java.util.Properties

fun Project.resolveSecret(name: String): String {
    val fromGradleProperty = providers.gradleProperty(name).orNull
    if (!fromGradleProperty.isNullOrBlank()) return fromGradleProperty.trim()

    val fromEnv = System.getenv(name)
    if (!fromEnv.isNullOrBlank()) return fromEnv.trim()

    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        val props = Properties()
        localPropsFile.inputStream().use { props.load(it) }
        val fromLocal = props.getProperty(name)
        if (!fromLocal.isNullOrBlank()) return fromLocal.trim()
    }
    return ""
}

android {
    namespace = "com.example.raitha_varta"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.raitha_varta"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val dataGovInKey = project.resolveSecret("DATA_GOV_IN_API_KEY")
        val agmarknetResourceId = project.resolveSecret("AGMARKNET_RESOURCE_ID")
        val predictionApiKey = project.resolveSecret("PREDICTION_API_KEY")
        val weatherApiKey = project.resolveSecret("WEATHER_API_KEY")
        val geminiApiKey = project.resolveSecret("GEMINI_API_KEY")

        buildConfigField("String", "DATA_GOV_IN_API_KEY", "\"$dataGovInKey\"")
        buildConfigField("String", "AGMARKNET_RESOURCE_ID", "\"$agmarknetResourceId\"")
        buildConfigField("String", "PREDICTION_API_KEY", "\"$predictionApiKey\"")
        buildConfigField("String", "WEATHER_API_KEY", "\"$weatherApiKey\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.datastore.preferences)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coil & Glide
    implementation(libs.coil.compose)
    implementation(libs.glide.compose)

    // Retrofit & Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)

    // Ktor (Explicitly required for Gemini SDK to prevent NoClassDefFoundError)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Gemini
    implementation(libs.google.generativeai)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.ml.modeldownloader)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    // Firebase App Check — protects API keys
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)

    // Markdown
    implementation(libs.compose.markdowntext)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
