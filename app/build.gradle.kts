plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.runningapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.runningapp"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets {
        getByName("main") {
            java {
                srcDirs("src/main/java", "src/main/kotlin+java/db", "src/main/java/2")
            }
        }
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:x.x.x")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-android:x.x.x")
//    implementation( libs.lifecycle.extensions)
    implementation( libs.lifecycle.runtime.ktx)
    implementation(libs.appcompat)
    implementation(project(":app:shared"))


    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)


    implementation("androidx.wear:wear:1.3.0")

    // Add support for wearable specific inputs
    implementation("androidx.wear:wear-input:1.1.0")


    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    project(":app:shared")
}