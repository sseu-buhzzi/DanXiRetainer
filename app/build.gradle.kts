plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	kotlin("plugin.serialization") version "2.1.21"
}

android {
	namespace = "com.buhzzi.danxiretainer"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.buhzzi.danxiretainer"
		minSdk = 29
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}

	android {
		buildFeatures {
			compose = true
		}
	}
}

dependencies {
	implementation(libs.duktape.android)

	implementation(libs.coil.compose)

	implementation(libs.ktor.client.core)
	implementation(libs.ktor.client.cio)
	implementation(libs.ktor.client.content.negotiation)
	implementation(libs.ktor.serialization.kotlinx.json)
	implementation(libs.ktor.client.auth)
	implementation(libs.ktor.client.logging)

	implementation(libs.guava)

	implementation(libs.androidx.datastore.preferences)
	implementation(libs.androidx.media)
	implementation(libs.androidx.navigation.compose)
	implementation(libs.androidx.security.crypto)

	val composeBom = platform("androidx.compose:compose-bom:2025.05.01")
	implementation(composeBom)
	androidTestImplementation(composeBom)

	// Material Design 3
	implementation(libs.androidx.material3)

	// Android Studio Preview support
	implementation(libs.androidx.ui.tooling.preview)
	debugImplementation(libs.androidx.ui.tooling)

	// UI Tests
	androidTestImplementation(libs.androidx.ui.test.junit4)
	debugImplementation(libs.androidx.ui.test.manifest)

	// Optional - Add full set of material icons
	implementation(libs.androidx.material.icons.extended)
	// Optional - Add window size utils
	implementation(libs.androidx.adaptive)

	// Optional - Integration with activities
	implementation(libs.androidx.activity.compose)
	// Optional - Integration with ViewModels
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	// Optional - Integration with LiveData
	implementation(libs.androidx.runtime.livedata)
	// Optional - Integration with RxJava
	implementation(libs.androidx.runtime.rxjava2)

	implementation(libs.kotlin.stdlib)

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}
