plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.layoutfinal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.layoutfinal"
        minSdk = 24
        targetSdk = 35
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
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.media3.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.github.wendykierp:JTransforms:3.1")
    implementation(libs.androidx.constraintlayout.v214)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.recyclerview:recyclerview:1.2.1") // Use the latest version

    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Or the latest version
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Or the latest version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1") // Or the latest version for coroutines

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")



    // Jetpack Compose
}