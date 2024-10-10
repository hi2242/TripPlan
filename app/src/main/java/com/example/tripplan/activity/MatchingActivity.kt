package com.example.tripplan.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.tripplan.MatchingPageAdapter
import com.example.tripplan.R
import com.example.tripplan.databinding.ActivityMatchingBinding
import com.google.android.material.tabs.TabLayout

class MatchingActivity : AppCompatActivity() {
    private lateinit var matchBinding: ActivityMatchingBinding
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: MatchingPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_guesthouse)
        matchBinding = ActivityMatchingBinding.inflate(layoutInflater)
        setContentView(matchBinding.root)

        adapter = MatchingPageAdapter(supportFragmentManager, lifecycle)

        with(matchBinding) {
            tabLayout.addTab(tabLayout.newTab().setText("메이트 매칭"))
            tabLayout.addTab(tabLayout.newTab().setText("게스트 매칭"))

            viewPager2.adapter = adapter

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        viewPager2.currentItem = tab.position
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            })

            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabLayout.selectTab(tabLayout.getTabAt(position))
                }
            })
        }

        adapter = MatchingPageAdapter(supportFragmentManager, lifecycle)
    }
}