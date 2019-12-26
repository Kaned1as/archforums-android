buildscript {
    val kotlinVersion: String by extra("1.3.61")

    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
    }
}

val kotlinVersion: String by extra("1.3.61")
val markwonVersion: String by extra("4.2.0")

allprojects {
    repositories {
        google()
        jcenter()

    }
}
