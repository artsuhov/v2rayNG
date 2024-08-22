plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.v2ray.ang"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.v2ray.ang"
        minSdk = 21
        targetSdk = 34
        versionCode = 583
        versionName = "1.8.38"
        multiDexEnabled = true
        splits {
            abi {
                isEnable = true
                include(
                    "arm64-v8a",
                    "armeabi-v7a",
                    "x86_64",
                    "x86"
                )
                isUniversalApk = true
            }
        }

    }

    signingConfigs {
        create("release") {
            storeFile = file("release_keystore")
            storePassword = "123123"
            keyAlias = "123"
            keyPassword = "123123"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false

        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    applicationVariants.all {
        val variant = this
        val versionCodes =
            mapOf("armeabi-v7a" to 4, "arm64-v8a" to 4, "x86" to 4, "x86_64" to 4, "universal" to 4)

        variant.outputs
            .map { it as com.android.build.gradle.internal.api.ApkVariantOutputImpl }
            .forEach { output ->
                val abi = if (output.getFilter("ABI") != null)
                    output.getFilter("ABI")
                else
                    "universal"

                output.outputFileName = "v2rayNG_${variant.versionName}_${abi}.apk"
                if (versionCodes.containsKey(abi)) {
                    output.versionCodeOverride = (1000000 * versionCodes[abi]!!).plus(variant.versionCode)
                } else {
                    return@forEach
                }
            }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
    testImplementation(libs.junit)

    implementation(libs.flexbox)
    // Androidx
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.cardview)
    implementation(libs.preference.ktx)
    implementation(libs.recyclerview)
    implementation(libs.fragment.ktx)
    implementation(libs.multidex)
    implementation(libs.viewpager2)

    // Androidx ktx
    implementation(libs.activity.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    //kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.mmkv.static)
    implementation(libs.gson)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxpermissions)
    implementation(libs.toastcompat)
    implementation(libs.editorkit)
    implementation(libs.language.base)
    implementation(libs.language.json)
    implementation(libs.quickie.bundled)
    implementation(libs.core)
    implementation(libs.work.runtime.ktx)
    implementation(libs.work.multiprocess)
}

val myArgument: String? by project

val preAssembleRelease by tasks.registering(Exec::class) {
    doFirst {
        println("Starting preAssembleRelease task")
        println("myArgument value: $myArgument")
        
        val stringsFile = file("/workspace/v2rayNG/V2rayNG/app/src/main/res/values/strings.xml")
        println("strings.xml exists: ${stringsFile.exists()}")
        println("strings.xml path: ${stringsFile.absolutePath}")
        
        if (!myArgument.isNullOrBlank()) {
            // Экранируем специальные символы в myArgument
            val escapedArgument = myArgument!!.replace("&", "\\&").replace("|", "\\|")
            
            // Используем более универсальный синтаксис sed
            val sedCommand = "sed -i.bak 's|VAS3K_SUB_URL|$escapedArgument|g' ${stringsFile.absolutePath}"
            println("Executing command: $sedCommand")
            
            commandLine("sh", "-c", sedCommand)
        } else {
            println("myArgument is null or blank, skipping sed command")
        }
    }
    
    doLast {
        println("preAssembleRelease task completed")
    }
}    
tasks.whenTaskAdded {
    if (name == "assembleRelease") {
        dependsOn(preAssembleRelease)
    }
}

