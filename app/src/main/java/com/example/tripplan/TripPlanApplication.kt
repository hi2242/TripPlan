package com.example.tripplan

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class TripPlanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 다크 모드 비활성화 설정
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
