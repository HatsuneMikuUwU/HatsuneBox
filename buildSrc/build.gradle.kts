plugins {
    `kotlin-dsl`
}

apply(from = "../repositories.gradle.kts")

dependencies {
    implementation("com.android.tools.build:gradle:9.2.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
}
