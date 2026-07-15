import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.FilterConfiguration
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.util.Base64
import java.util.Properties

private val Project.android get() = extensions.getByName<ApplicationExtension>("android")

private lateinit var metadata: Properties
private lateinit var localProperties: Properties

fun Project.requireMetadata(): Properties {
    if (!::metadata.isInitialized) {
        metadata = Properties().apply {
            load(rootProject.file("nb4a.properties").inputStream())
        }
    }
    return metadata
}

fun Project.previewVersionName(): String {
    val verName = requireMetadata().getProperty("VERSION_NAME")
    val formatter = java.text.SimpleDateFormat("yyyyMMdd-HHmm").apply {
        timeZone = java.util.TimeZone.getTimeZone("Asia/Jakarta")
    }
    val buildDate = formatter.format(java.util.Date())
    return "pre-$verName-$buildDate"
}

fun Project.requireLocalProperties(): Properties {
    if (!::localProperties.isInitialized) {
        localProperties = Properties()

        val base64 = System.getenv("LOCAL_PROPERTIES")
        if (!base64.isNullOrBlank()) {
            localProperties.load(Base64.getDecoder().decode(base64).inputStream())
        } else if (project.rootProject.file("local.properties").exists()) {
            localProperties.load(rootProject.file("local.properties").inputStream())
        }
    }
    return localProperties
}

fun Project.setupCommon() {
    android.apply {
        buildToolsVersion = "36.0.0"
        compileSdk = 37
        ndkVersion = "27.3.13750724"
        defaultConfig {
            minSdk = 24
            targetSdk = 37
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                if (System.getenv("nkmr_minify") == "0") {
                    isShrinkResources = false
                    isMinifyEnabled = false
                }
            }
            getByName("debug") {
                applicationIdSuffix = "debug"
                isDebuggable = true
                isJniDebuggable = true
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        lint {
            showAll = true
            checkAllWarnings = true
            checkReleaseBuilds = true
            warningsAsErrors = true
            textOutput = project.file("build/lint.txt")
            htmlOutput = project.file("build/lint.html")
        }
        packaging {
            resources.excludes.addAll(
                listOf(
                    "**/*.kotlin_*",
                    "/META-INF/*.version",
                    "/META-INF/native/**",
                    "/META-INF/native-image/**",
                    "/META-INF/INDEX.LIST",
                    "DebugProbesKt.bin",
                    "com/**",
                    "org/**",
                    "**/*.java",
                    "**/*.proto",
                    "okhttp3/**"
                )
            )
        }
    }

    (extensions.findByName("kotlin") as? KotlinAndroidProjectExtension)?.compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

fun Project.setupAppCommon() {
    setupCommon()

    val lp = requireLocalProperties()
    val keystorePwd = lp.getProperty("KEYSTORE_PASS") ?: System.getenv("KEYSTORE_PASS")
    val alias = lp.getProperty("ALIAS_NAME") ?: System.getenv("ALIAS_NAME")
    val pwd = lp.getProperty("ALIAS_PASS") ?: System.getenv("ALIAS_PASS")

    android.apply {
        if (keystorePwd != null) {
            signingConfigs {
                create("release") {
                    storeFile = rootProject.file("release.keystore")
                    storePassword = keystorePwd
                    keyAlias = alias
                    keyPassword = pwd
                    enableV1Signing = true
                    enableV2Signing = true
                    enableV3Signing = true
                }
            }
        }
        buildTypes {
            val key = signingConfigs.findByName("release")
            if (key != null) {
                getByName("release").signingConfig = key
                getByName("debug").signingConfig = key
            }
        }
    }
}

fun Project.setupApp() {
    val pkgName = requireMetadata().getProperty("PACKAGE_NAME")
    val verName = requireMetadata().getProperty("VERSION_NAME")
    val verCode = (requireMetadata().getProperty("VERSION_CODE").toInt()) * 5
    
    android.apply {
        defaultConfig {
            applicationId = pkgName
            versionCode = verCode
            versionName = verName
            buildConfigField("String", "PRE_VERSION_NAME", "\"\"")
        }
    }
    setupAppCommon()

    android.apply {
        buildTypes {
            getByName("release") {
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    file("proguard-rules.pro")
                )
            }
        }

        splits.abi {
            reset()
            isEnable = true
            isUniversalApk = false
            include("armeabi-v7a")
            include("arm64-v8a")
            include("x86")
            include("x86_64")
        }

        flavorDimensions += "vendor"
        productFlavors {
            create("oss")
            create("fdroid")
            create("play")
            create("preview") {
                buildConfigField(
                    "String",
                    "PRE_VERSION_NAME",
                    "\"${previewVersionName()}\""
                )
            }
        }

        listOf("Arm64", "Arm", "X64", "X86").forEach { abi ->
            tasks.register("assemble${abi}FdroidRelease") {
                dependsOn("assembleFdroidRelease")
            }
        }

        sourceSets.getByName("main").apply {
            jniLibs.directories.add("executableSo")
        }
    }

    val androidComponents = extensions.getByName<ApplicationAndroidComponentsExtension>("androidComponents")
    androidComponents.onVariants { variant ->
        val flavor = variant.flavorName ?: ""
        val buildType = variant.buildType ?: ""
        
        val versionString = if (flavor == "preview") {
            previewVersionName()
        } else {
            if (flavor.isNotEmpty()) "$flavor-v$verName" else "v$verName"
        }

        variant.outputs.forEach { output ->
            val abi = output.filters.find { it.filterType == FilterConfiguration.FilterType.ABI }?.identifier
            val abiSuffix = if (abi != null) "-$abi" else ""
            
            val appName = "HatsuneBox"
            val newApkName = "$appName-$versionString$abiSuffix-$buildType.apk"
            
            output.outputFileName.set(newApkName)
        }
    }
}
