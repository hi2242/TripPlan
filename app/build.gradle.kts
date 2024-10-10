import com.android.build.gradle.ProguardFiles.getDefaultProguardFile
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")

}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())

android {
    namespace = "com.example.tripplan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tripplan"
        minSdk = 24
        targetSdk = 34
        // Version Code가 이전 파일보다 낮게 설정되어 있습니다.의 경우 versionCode를 업데이트
        // versionName은 스토어에 보일 업데이트 버전
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", properties.getProperty("KAKAO_NATIVE_APP_KEY"))
        buildConfigField("String", "API_KEY", properties.getProperty("API_KEY"))
        // manifest에서 쓰기 위해 넣어주는 것
        resValue("string", "KAKAO_OAUTH_HOST", properties.getProperty("kakao_oauth_host"))
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson Converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // For Debugging
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation ("com.google.android.material:material:1.9.0")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-storage")
    implementation ("com.firebaseui:firebase-ui-storage:7.2.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation ("de.hdodenhof:circleimageview:3.1.0") // 둥근 프로필 사진
    implementation ("com.kakao.sdk:v2-all:2.20.3") // 전체 모듈 설치, 2.11.0 버전부터 지원
    implementation ("com.kakao.sdk:v2-user:2.20.3") // 카카오 로그인 API 모듈
    implementation ("com.kakao.sdk:v2-share:2.20.3") // 카카오톡 공유 API 모듈
    implementation ("com.kakao.sdk:v2-talk:2.20.3") // 카카오톡 채널, 카카오톡 소셜, 카카오톡 메시지 API 모듈
    implementation ("com.kakao.sdk:v2-friend:2.20.3") // 피커 API 모듈
    implementation ("com.kakao.sdk:v2-navi:2.20.3") // 카카오내비 API 모듈
    implementation ("com.kakao.sdk:v2-cert:2.20.3")
    implementation ("androidx.fragment:fragment-ktx:1.8.2")
    implementation(libs.firebase.database) // 카카오톡 인증 서비스 API 모듈
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.19")
    implementation(libs.firebase.auth) // gif
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.14.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.0")
    // Swiperefreshlayout
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // 원스토어
    implementation("com.onestorecorp.sdk:sdk-licensing:2.0.0")
    implementation("com.onestorecorp.sdk:sdk-configuration-kr:1.0.0")
}