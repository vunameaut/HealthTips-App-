import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
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

            // Thêm OpenAI API key vào BuildConfig (Backup)
            val openaiApiKey = localProperties.getProperty("openai.api.key", "")
            buildConfigField("String", "OPENAI_API_KEY", "\"$openaiApiKey\"")

            // Thêm Google Gemini API key vào BuildConfig (Currently Active)
            val geminiApiKey = localProperties.getProperty("gemini.api.key", "")
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
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

    // Cấu hình hỗ trợ 16 KB page size alignment
    androidResources {
        noCompress += "tflite"
    }

    // Cấu hình packaging để hỗ trợ 16KB page size alignment (Updated for AGP 8.1+)
    packaging {
        jniLibs {
            // QUAN TRỌNG: Bật useLegacyPackaging = false để hỗ trợ 16KB page alignment
            // Điều này đảm bảo native libraries được align đúng với 16KB boundaries
            useLegacyPackaging = false

            // Không nén native libraries để hỗ trợ 16KB alignment tốt hơn
            // keepDebugSymbols += listOf("**/*.so")
        }

        // Loại bỏ các file không cần thiết để giảm kích thước APK
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "/META-INF/*.kotlin_module"
            )

            // Merge duplicate resources
            pickFirsts += listOf(
                "lib/armeabi-v7a/libc++_shared.so",
                "lib/arm64-v8a/libc++_shared.so",
                "lib/x86/libc++_shared.so",
                "lib/x86_64/libc++_shared.so"
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

            // Đảm bảo 16KB page alignment cho release build
            ndk {
                debugSymbolLevel = "FULL"
            }
        }

        debug {
            isMinifyEnabled = false
            isDebuggable = true

            // Đảm bảo 16KB page alignment cho debug build
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        // Nâng cấp Java toolchain lên Java 11
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        // Cho phép sử dụng các tính năng Java 8+
        isCoreLibraryDesugaringEnabled = true
    }

    // Configure Java toolchain to use version 17
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

dependencies {
    // Cập nhật Firebase BoM lên phiên bản mới
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Các Firebase libraries khác giữ nguyên
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-storage") // Giữ lại cho avatar người dùng
    implementation("com.google.firebase:firebase-database")

    // Google Sign-In cho Firebase Authentication - Updated to latest version
    implementation("com.google.android.gms:play-services-auth:21.2.0")

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
    implementation(libs.firebase.messaging)

    // Thêm thư viện Desugaring để hỗ trợ tính năng Java 8+ trên các thiết bị cũ
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Thư viện mã hóa dữ liệu
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Thư viện Glide để load ảnh - Updated to latest version with 16KB support
    implementation("com.github.bumptech.glide:glide:5.0.5") {
        // Exclude các thư viện native có thể gây conflict
        exclude(group = "com.android.support")
    }
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")

    // Glide OkHttp3 integration (optional but recommended)
    implementation("com.github.bumptech.glide:okhttp3-integration:5.0.5")

    // Thêm OkHttp và Gson cho Chat API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Thêm dependencies cho chức năng video ngắn
    // CircleImageView cho avatar tròn
    implementation("de.hdodenhof:circleimageview:3.1.0")


    // ExoPlayer cho video playback (tùy chọn thay thế VideoView)
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")

    // Thêm Cloudinary Android SDK - Updated to latest version
    implementation("com.cloudinary:cloudinary-android:3.0.2") {
        // Exclude Fresco nếu có để tránh vấn đề 16KB page size
        exclude(group = "com.facebook.fresco", module = "fresco")
    }

    // ===== PERFORMANCE OPTIMIZATION DEPENDENCIES =====

    // 1. ViewModel & LiveData để giữ dữ liệu trong bộ nhớ
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime:2.6.2")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")

    // SavedStateHandle cho ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")

    // 2. Room Database để cache dữ liệu cục bộ
    implementation("androidx.room:room-runtime:2.6.0")
    annotationProcessor("androidx.room:room-compiler:2.6.0")
    // Room optional - RxJava3 support (nếu muốn dùng RxJava sau này)
    implementation("androidx.room:room-rxjava3:2.6.0")

    // 3. DataStore để thay thế SharedPreferences (hiện đại hơn)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.0.0")

    // 4. Retrofit + OkHttp Cache cho networking tối ưu
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp đã có rồi, chỉ cần cấu hình cache

    // 5. RxJava3 cho reactive programming (optional nhưng rất tốt cho performance)
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")

    // Javax Inject cho Dependency Injection
    implementation("javax.inject:javax.inject:1")
}