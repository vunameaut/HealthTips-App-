plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.vhn.doan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vhn.doan"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Cấu hình MultiDex
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Cấu hình Java 8+
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        // Cho phép sử dụng các tính năng Java 8
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
}