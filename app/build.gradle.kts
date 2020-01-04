import com.android.build.VariantOutput.FilterType
import com.palantir.gradle.gitversion.VersionDetails
import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.net.URI

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
    maven { url = URI.create("https://jitpack.io") }
    maven { url = URI.create("https://oss.sonatype.org/content/repositories/snapshots/") }
}

fun versionDetails() = (extra["versionDetails"] as groovy.lang.Closure<*>)() as VersionDetails
fun gitVersion() = (extra["gitVersion"] as groovy.lang.Closure<*>)() as String

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId = "com.kanedias.holywarsoo"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 4
        versionName = "1.1.2"
        vectorDrawables.useSupportLibrary = true
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
            signingConfig = signingConfigs.getByName("release")
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

    flavorDimensions("purity")
    productFlavors {
        create("fdroid") {
            setDimension("purity")
        }

        create("googleplay") {
            setDimension("purity")
        }
    }

    applicationVariants.all {
        outputs.forEach { output ->
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
    implementation("androidx.core:core-ktx:1.1.0")                               // kotlin support for androidx
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")           // constaint layout view
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.0.0")       // swipe-to-refresh layout view
    implementation("androidx.cardview:cardview:1.0.0")                           // snappy cardview for lists
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-rc03")        // coroutines lifecycle scopes
    implementation("androidx.preference:preference:1.1.0")                       // preference fragment compatibility
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")              // view-model providers
    implementation("com.google.android.material:material:1.2.0-alpha03")         // Material design support lib
    implementation("com.jakewharton:butterknife:10.2.0")                         // Annotation processor
    implementation("com.squareup.okhttp3:okhttp:3.14.0")                         // android http client
    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1")          // cookie support
    implementation("com.github.stfalcon:stfalcon-imageviewer:0.1.0")             // embedded image viewer
    implementation("com.r0adkll:slidableactivity:2.1.0")                         // fragment swipe right to go back action

    implementation("ch.acra:acra-mail:${rootProject.extra["acraVersion"]}")                             // crash handler
    implementation("ch.acra:acra-dialog:${rootProject.extra["acraVersion"]}")

    implementation("io.noties.markwon:core:${rootProject.extra["markwonVersion"]}")                     // markdown rendering
    implementation("io.noties.markwon:image-glide:${rootProject.extra["markwonVersion"]}")
    implementation("io.noties.markwon:html:${rootProject.extra["markwonVersion"]}")
    implementation("io.noties.markwon:ext-tables:${rootProject.extra["markwonVersion"]}")
    implementation("io.noties.markwon:ext-strikethrough:${rootProject.extra["markwonVersion"]}")

    implementation("org.jsoup:jsoup:1.12.1")                                          // HTML parser

    kapt("com.jakewharton:butterknife-compiler:10.2.0")

    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
