plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.luandev.saneam"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.luandev.saneam"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false // Mude para [true] ao gerar o .aab final
                // enable = true [Ative para a versão final que vai para a Play Store]
            }
            // Opcional: remove layouts e imagens XML que não estão sendo usados
            isShrinkResources = false // Mude para [true] junto com a otimização acima

            // Define as regras de quais códigos NÃO devem ser mexidos (muito importante para o Supabase)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "src/main/keepRules/rules.keep" // Caminho correto para o meu arquivo novo
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true // Garante que a classe BuildConfig seja gerada
    }
}

secrets {
    // Diz para o plugin ler explicitamente o seu arquivo local.properties
    propertiesFileName = "local.properties"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.googleid)
    implementation(libs.material)
    implementation(libs.bundles.supabase)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}