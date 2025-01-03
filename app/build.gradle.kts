plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.googleService)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.softcraft.rutaxpressapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.softcraft.rutaxpressapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Obtenenemos el API-KEY desde gradle.properties
        val mapsApiKey: String = project.findProperty("MAPS_API_KEY") as String? ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true  // Habilitamos la generación de campos BuildConfig
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.firebaseAuth)
    implementation(libs.firebaseStorage)
    implementation(libs.firebaseStore)
    implementation(libs.firebaseAnalitics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.cloudinaryAndroid)
    implementation(libs.glide)
    implementation(libs.retrofit)
    implementation(libs.corrutines)
    implementation(libs.gson)
    implementation(libs.firebase.crashlytics)
    implementation(libs.play.services.cast.tv)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.maps)
    implementation(libs.places)
    implementation(libs.socket)
}