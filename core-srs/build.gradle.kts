// Модуль алгоритма повторения: чистый Kotlin, без Android.
// Поэтому здесь плагин kotlin-jvm, а не com.android.library.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    testImplementation(libs.junit)
}
