package com.example.tripplan

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.tripplan.BuildConfig.KAKAO_NATIVE_APP_KEY
import com.kakao.sdk.common.KakaoSdk

class loginForKakao : Application() {
    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, KAKAO_NATIVE_APP_KEY)

        // 다크 모드 비활성화 설정(자리 좀 빌릴게요)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}