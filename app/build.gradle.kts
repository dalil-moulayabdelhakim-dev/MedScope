plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.dldroid.medscope"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dldroid.medscope"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.database)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation (libs.uploadservice)

    //animation-------------------------------------------------------------------------------------
    implementation(libs.lottie)
    //code genirator--------------------------------------------------------------------------------
    implementation(libs.zxing.android.embedded)
    implementation (libs.core)
    //code scanner----------------------------------------------------------------------------------
    implementation (libs.zxing.android.embedded)
    implementation (libs.code.scanner)
    //database--------------------------------------------------------------------------------------
    implementation (libs.volley)
    //uploader--------------------------------------------------------------------------------------
    implementation (libs.retrofit)
    //biometric-------------------------------------------------------------------------------------
    implementation (libs.androidx.biometric)
    //view pager2-----------------------------------------------------------------------------------
    implementation (libs.androidx.viewpager2)
    implementation (libs.dotsindicator)
    //swipe to refresh------------------------------------------------------------------------------
    implementation(libs.androidx.swiperefreshlayout)
    //graph view------------------------------------------------------------------------------------
    implementation ("com.jjoe64:graphview:4.2.2")
}