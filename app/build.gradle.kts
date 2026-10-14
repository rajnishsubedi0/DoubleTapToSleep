plugins {
    id("com.android.application")
}

android {
    namespace = "com.rkant.doubletapsleep"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rkant.doubletapsleep"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    compileOnly(files("libs/XposedBridgeApi-82.jar"))
}
