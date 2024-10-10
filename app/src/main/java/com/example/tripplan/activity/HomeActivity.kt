package com.example.tripplan.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.tripplan.R
import com.example.tripplan.UserInfo
import com.example.tripplan.board.BoardWriteActivity
import com.example.tripplan.activity.*
import com.example.tripplan.ViewPagerAdapter
import com.example.tripplan.databinding.ActivityHomeBinding


class HomeActivity : AppCompatActivity()  {
    private var backPressedTime: Long = 0
    // 로그인 후 Fragment
    private lateinit var homeBinding : ActivityHomeBinding
    private var isFabOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // 카카오 로그인 페이지
        super.onCreate(savedInstanceState)
        homeBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)
        initViewPager()
        initNavigation()

        with(homeBinding) {
            // 플로팅 버튼 클릭시 에니메이션 동작 기능
            fabMain.setOnClickListener {
                toggleFab()
            }

            // 사용자 유형에 따라 플로팅 버튼 클릭 이벤트 설정
            if (isKakaoUser()) {
                fabCitizen.setOnClickListener {
                    startActivity(Intent(this@HomeActivity, MatchingActivity::class.java))
                    Toast.makeText(this@HomeActivity, "일반인 모집", Toast.LENGTH_SHORT).show()
                }

                fabOrgan.setOnClickListener {
                    Toast.makeText(this@HomeActivity, "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                fabCitizen.setOnClickListener {
                    Toast.makeText(this@HomeActivity, "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }

                fabOrgan.setOnClickListener {
                    startActivity(Intent(this@HomeActivity, BoardWriteActivity::class.java))
                    Toast.makeText(this@HomeActivity, "기관/지자체 모집", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun isKakaoUser(): Boolean {
        // 카카오 유저 확인 로직. UserInfo.kakaoId가 null이 아니면 카카오 유저로 간주
        return UserInfo.email != null
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                // 두 번 눌렀을 때 앱 종료
                finishAffinity()  // 현재 액티비티를 포함한 모든 액티비티 종료
            } else {
                // 첫 번째 뒤로가기 눌렀을 때 메시지 표시
                Toast.makeText(this, "뒤로가기를 한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
            backPressedTime = System.currentTimeMillis()
        } else {
            super.onBackPressed()
        }
    }

    // 플로팅 액션 버튼 클릭시 동작하는 애니메이션 효과 세팅
    private fun toggleFab() {
        //Toast.makeText(this, "모집하기 : $isFabOpen", Toast.LENGTH_SHORT).show()

        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션 세팅
        if (isFabOpen) {
            ObjectAnimator.ofFloat(homeBinding.fabCitizen, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(homeBinding.fabOrgan, "translationY", 0f).apply { start() }
            homeBinding.fabMain.setImageResource(R.drawable.add)

            // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션 세팅
        } else {
            ObjectAnimator.ofFloat(homeBinding.fabCitizen, "translationY", -200f).apply { start() }
            ObjectAnimator.ofFloat(homeBinding.fabOrgan, "translationY", -400f).apply { start() }
            homeBinding.fabMain.setImageResource(R.drawable.add)
        }

        isFabOpen = !isFabOpen

    }

    private fun initViewPager() {
        val viewPager = homeBinding.viewPager
        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                homeBinding.bottomNavigationView.menu.getItem(position).isChecked = true

                if (position == 0) {
                    // HomeFragment일 때 플로팅 버튼 보이기
                    showFab()
                } else {
                    // 다른 Fragment일 때 플로팅 버튼 숨기기
                    hideFab()
                }
            }
        })
    }

    fun showFab() {
        homeBinding.fabMain.show()
        homeBinding.fabCitizen.show()
        homeBinding.fabOrgan.show()
    }

    fun hideFab() {
        homeBinding.fabMain.hide()
        homeBinding.fabCitizen.hide()
        homeBinding.fabOrgan.hide()
    }


    private fun initNavigation() {
        homeBinding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    homeBinding.viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }
                R.id.chat -> {
                    homeBinding.viewPager.currentItem = 1
                    return@setOnItemSelectedListener true
                }
                R.id.info -> {
                    homeBinding.viewPager.currentItem = 2
                    return@setOnItemSelectedListener true
                }
                else -> {
                    homeBinding.viewPager.currentItem = 3
                    return@setOnItemSelectedListener false
                }
            }
        }

    }

}