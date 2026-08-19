plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.linguax.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Ensure CI/CD environment variables or Gradle properties are synced to .env
val envFile = rootProject.file(".env")
val resolvedSupabaseUrl = System.getenv("SUPABASE_URL")
    ?: System.getenv("VITE_SUPABASE_URL")
    ?: (project.findProperty("SUPABASE_URL") as? String)
    ?: (project.findProperty("VITE_SUPABASE_URL") as? String)

val resolvedSupabaseAnonKey = System.getenv("SUPABASE_ANON_KEY")
    ?: System.getenv("VITE_SUPABASE_ANON_KEY")
    ?: (project.findProperty("SUPABASE_ANON_KEY") as? String)
    ?: (project.findProperty("VITE_SUPABASE_ANON_KEY") as? String)

if (!resolvedSupabaseUrl.isNullOrBlank() || !resolvedSupabaseAnonKey.isNullOrBlank()) {
    val existingLines = if (envFile.exists()) envFile.readLines() else emptyList()
    val props = mutableMapOf<String, String>()
    existingLines.forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
            val key = trimmed.substringBefore("=").trim()
            val value = trimmed.substringAfter("=").trim()
            props[key] = value
        }
    }
    if (!resolvedSupabaseUrl.isNullOrBlank()) {
        props["SUPABASE_URL"] = resolvedSupabaseUrl.trim().removeSurrounding("\"").removeSurrounding("'")
        props["VITE_SUPABASE_URL"] = resolvedSupabaseUrl.trim().removeSurrounding("\"").removeSurrounding("'")
    }
    if (!resolvedSupabaseAnonKey.isNullOrBlank()) {
        props["SUPABASE_ANON_KEY"] = resolvedSupabaseAnonKey.trim().removeSurrounding("\"").removeSurrounding("'")
        props["VITE_SUPABASE_ANON_KEY"] = resolvedSupabaseAnonKey.trim().removeSurrounding("\"").removeSurrounding("'")
    }
    val content = props.entries.joinToString("\n") { "${it.key}=${it.value}" }
    envFile.writeText(content + "\n")
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Dependencies
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
