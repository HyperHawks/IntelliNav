plugins {
  alias(notation = libs.plugins.android.application)
  alias(notation = libs.plugins.kotlin.android)
  alias(notation = libs.plugins.kotlin.compose)
}

android {
  namespace = "com.intellinav"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.intellinav"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
    }
    debug {
      isDebuggable = true
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
  }
}

dependencies {
  // Core AndroidX
  implementation(dependencyNotation = libs.androidx.core.ktx)
  implementation(dependencyNotation = libs.androidx.lifecycle.runtime.ktx)
  implementation(dependencyNotation = libs.androidx.lifecycle.runtime.compose)
  implementation(dependencyNotation = libs.androidx.lifecycle.viewmodel.compose)

  // Jetpack Compose
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(dependencyNotation = composeBom)
  implementation(dependencyNotation = libs.androidx.activity.compose)
  implementation(dependencyNotation = libs.androidx.compose.ui)
  implementation(dependencyNotation = libs.androidx.compose.ui.graphics)
  implementation(dependencyNotation = libs.androidx.compose.ui.tooling.preview)
  implementation(dependencyNotation = libs.androidx.compose.material3)
  implementation(dependencyNotation = libs.androidx.compose.material.icons.extended)
  implementation(dependencyNotation = libs.androidx.navigation.compose)
  debugImplementation(dependencyNotation = libs.androidx.compose.ui.tooling)

  // Coroutines
  implementation(dependencyNotation = libs.kotlinx.coroutines.android)
  implementation(dependencyNotation = libs.kotlinx.coroutines.core)

  // Testing
  testImplementation(dependencyNotation = libs.junit)
  testImplementation(dependencyNotation = libs.kotlinx.coroutines.test)
  androidTestImplementation(dependencyNotation = libs.androidx.junit)
  androidTestImplementation(dependencyNotation = libs.androidx.espresso.core)
}
