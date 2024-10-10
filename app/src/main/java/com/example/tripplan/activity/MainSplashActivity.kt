package com.example.tripplan.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.tripplan.R
import com.example.tripplan.databinding.ActivitySplashBinding

class MainSplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding
//    // 애니메이션을 처리하기 위한 러너블
//    private val mRunnable: Runnable = Runnable {
//        if (!isFinishing) {
//            // 비행기를 위로 움직인다.
//            slideUp(binding.airplane, 3000)
//            // 애니메이션이 끝난 후 슬로건 애니메이션 실행
//            Handler(Looper.getMainLooper()).postDelayed({
//                val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
//                binding.slogan.startAnimation(fadeInAnimation)
//            }, 3000) // 비행기 애니메이션과 동일한 시간 설정
//        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainSplashActivity", "onCreate called")  // 로그 추가
        // ViewBinding 초기화
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)  // 바인딩된 레이아웃을 사용

        // 뷰가 배치된 후에 애니메이션을 실행하기 위해 ViewTreeObserver 사용
        binding.airplane.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 애니메이션 실행
                slideDownThenUp(binding.airplane, 500, 1000)

                // 한 번만 실행되도록 리스너 제거
                binding.airplane.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        // 애니메이션이 끝난 후 슬로건 애니메이션 실행
        Handler(Looper.getMainLooper()).postDelayed({
            val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            binding.slogan.startAnimation(fadeInAnimation)
            binding.palmtree.startAnimation(fadeInAnimation)
            binding.planetakeoff.startAnimation(fadeInAnimation)
            binding.traveler.startAnimation(fadeInAnimation)
            Log.d("MainSplashActivity", "Slogan animation started")
        }, 2000) // 비행기 애니메이션과 동일한 시간 설정

        // 애니메이션이 끝난 후 MainActivity로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("MainSplashActivity", "Starting MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
            finish() // 현재 액티비티 종료
        }, 5000) // 총 대기 시간 (비행기 애니메이션 + 슬로건 애니메이션)
    }
    private fun slideDownThenUp(view: View, downTime: Int, upTime: Int) {
        // 살짝 아래로 내려가는 애니메이션 (TranslateY +50dp 정도)
        val slideDown = TranslateAnimation(0f, 0f, 0f, 50f)
        slideDown.duration = downTime.toLong()
        slideDown.fillAfter = true

        // 빠르게 위로 올라가는 애니메이션 (AccelerateInterpolator로 가속)
        val slideUp = TranslateAnimation(0f, 0f, 50f, -(view.height * 4).toFloat())
        slideUp.duration = upTime.toLong()
        slideUp.interpolator = AccelerateInterpolator(2.0f) // 빠르게 가속
        slideUp.fillAfter = true

        // slideDown 애니메이션이 끝난 후 slideUp 애니메이션 시작
        slideDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                view.startAnimation(slideUp) // 내려간 후 위로 빠르게 올라감
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        view.startAnimation(slideDown)
    }
//    private fun slideDown(view: View, time: Int) {
//        val animation = TranslateAnimation(0f, 0f, 0f, -(view.height * 4).toFloat())
//
//        animation.duration = time.toLong()
//        animation.fillAfter = true
//
//        view.startAnimation(animation)
//    }
//    private fun slideUp(view: View, time: Int) {
//        val animation = TranslateAnimation(0f, 0f, 0f, -(view.height * 4).toFloat())
//
//        animation.duration = time.toLong()
//        animation.fillAfter = true
//
//        view.startAnimation(animation)
//    }
}