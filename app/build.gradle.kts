import com.android.build.VariantOutput.FilterType
import com.palantir.gradle.gitversion.VersionDetails
import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
    id("com.android.application")

    id("com.palantir.git-version").version("0.11.0")
    id("com.github.triplet.play").version("2.6.1")

    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

fun versionDetails() = (extra["versionDetails"] as groovy.lang.Closure<*>)() as VersionDetails
fun gitVersion() = (extra["gitVersion"] as groovy.lang.Closure<*>)() as String

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId = "holywarsoo.kanedias.com"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {

        create("release") {
            storeFile = file("misc/signing.keystore")
            storePassword = System.getenv("SIGNING_KEYSTORE_PASSWORD")
            keyAlias = "release-key"
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    splits {
        abi {
            setEnable(true)
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            setUniversalApk(false)
        }
    }

    applicationVariants.forEach { variant ->
        variant.outputs.forEach { output ->
            val outputApk = output as ApkVariantOutputImpl

            // user-supplied code
            val versionCode = android.defaultConfig.versionCode!!

            // code based on ABI
            val versionCodes = mapOf(
                "universal" to 0,
                "armeabi-v7a" to 1,
                "arm64-v8a" to 2,
                "x86" to 3,
                "x86_64" to 4
            )

            val abiVersionCode = versionCodes[outputApk.getFilter(FilterType.ABI) ?: "universal"]!!

            // code based on track
            val gitVersionCode = versionDetails().commitDistance

            val playVersionCode = when (play.track) {
                "internal" -> 0
                "alpha" -> 1
                "beta" -> 2
                "production" -> 4
                else -> 3
            }

            outputApk.versionCodeOverride = versionCode * 10000 + playVersionCode * 1000 + gitVersionCode * 10 + abiVersionCode
            outputApk.versionNameOverride = gitVersion().replace(".dirty", "")
        }
    }
}


kapt {
    useBuildCache = true
}

play {
    serviceAccountCredentials = file("misc/android-publisher-account.json")
    track = when (project.hasProperty("releaseType")) {
        true -> project.property("releaseType").toString()
        false -> "alpha"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8", "1.3.61"))
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.cardview:cardview:1.0.0")                           // snappy cardview for lists
    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-alpha01")
    implementation("com.google.android.material:material:1.2.0-alpha02")         // Material design support lib
    implementation("com.jakewharton:butterknife:10.2.0")                         // Annotation processor
    implementation("com.squareup.okhttp3:okhttp:3.14.0")                         // android http client
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.0.0")       // swipe-to-refresh layout

    implementation("org.jsoup:jsoup:1.12.1")                                          // HTML parser

    kapt("com.jakewharton:butterknife-compiler:10.2.0")

    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
