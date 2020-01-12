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

allprojects {

    dependencies {
        val kotlinVersion: String by extra("1.3.61")
        val acraVersion: String by extra("5.5.0")
        val markwonVersion: String by extra("4.2.1-SNAPSHOT")
        val roomVersion: String by extra("2.2.3")
    }

    repositories {
        google()
        jcenter()
    }
}
