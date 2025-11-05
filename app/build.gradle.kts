import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.vhn.doan"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.vhn.doan"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Đọc API keys từ local.properties
        val localProperties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))

            // Thêm Cloudinary API keys vào BuildConfig
            val cloudinaryApiKey = localProperties.getProperty("cloudinary.api.key", "")
            val cloudinaryApiSecret = localProperties.getProperty("cloudinary.api.secret", "")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"$cloudinaryApiKey\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$cloudinaryApiSecret\"")

            // Thêm Firebase Auth key vào BuildConfig
            val firebaseAuthKey = localProperties.getProperty("firebase.auth.key", "")
            buildConfigField("String", "FIREBASE_AUTH_KEY", "\"$firebaseAuthKey\"")

            // Thêm OpenAI API key vào BuildConfig
            val openaiApiKey = localProperties.getProperty("openai.api.key", "")
            buildConfigField("String", "OPENAI_API_KEY", "\"$openaiApiKey\"")
        }

        // Cấu hình MultiDex
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Cấu hình hỗ trợ 16KB page size cho native libraries
        ndk {
            // Đảm bảo alignment cho tất cả các architectures
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    // Cấu hình packaging để hỗ trợ 16KB page size alignment (Updated for AGP 8.1+)
    packaging {
        jniLibs {
            // Sử dụng cấu hình mới cho 16KB page size alignment
            useLegacyPackaging = false
        }

        // Loại bỏ các file không cần thiết để giảm kích thước APK
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt"
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Tối ưu hóa cho production build
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3
        }

        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        // Nâng cấp Java toolchain lên Java 11
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        // Cho phép sử dụng các tính năng Java 8+
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    // Cập nhật Firebase BoM lên phiên bản mới
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Các Firebase libraries khác giữ nguyên
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage") // Giữ lại cho avatar người dùng
    implementation("com.google.firebase:firebase-database")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Thêm thư viện MultiDex
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)

    // Thêm SwipeRefreshLayout cho FavoriteFragment
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Thêm RecyclerView nếu chưa có
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Thêm CardView cho layout items
    implementation("androidx.cardview:cardview:1.0.0")


    implementation("androidx.work:work-runtime:2.9.0")
    implementation(libs.core)

    // Thêm thư viện Desugaring để hỗ trợ tính năng Java 8+ trên các thiết bị cũ
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Thư viện mã hóa dữ liệu
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Thư viện Glide để load ảnh
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Thêm OkHttp và Gson cho Chat API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Thêm dependencies cho chức năng video ngắn
    // CircleImageView cho avatar tròn
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Glide cho load ảnh/video
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ExoPlayer cho video playback (tùy chọn thay thế VideoView)
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")

    // Thêm Cloudinary Android SDK
    implementation("com.cloudinary:cloudinary-android:2.8.0")
}