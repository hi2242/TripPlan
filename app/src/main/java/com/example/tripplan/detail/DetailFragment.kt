package com.example.tripplan.detail

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.text.util.LinkifyCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tripplan.FBRef
import com.example.tripplan.R
import com.example.tripplan.board.WebViewActivity
import com.example.tripplan.data.GuestHouseData
import com.example.tripplan.databinding.FragmentDetailBinding
import com.example.tripplan.databinding.FragmentGuestMatchingBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val guestHouseData = arguments?.getParcelable("guesthouse_data", GuestHouseData::class.java)
    private lateinit var hereLink: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            val detailList = arguments?.getParcelable("guesthouse_data", GuestHouseData::class.java)?.let {
                Glide.with(this@DetailFragment)
                    .load(it.imageUrl)
//                    .placeholder(R.drawable.ic_open_yak)
                    .into(binding.guestIv)

//                guestNameTv.text = it.titleForGuestHouse
//                guestCode.text = it.addressForGuestHouse
                // guestVendor.text = it.telForGuestHouse

                // 여기어때 링크 저장
                hereLink = "https://www.yeogi.com/domestic-accommodations?searchType=KEYWORD&keyword=${it.titleForGuestHouse}&autoKeyword=&personal=2&freeForm=true"

                listOf(
                    "숙소명 : ${it.titleForGuestHouse}\n\n전화번호 : ${it.telForGuestHouse}", // 소개
                    "위치 : ${it.addressForGuestHouse}\n\n상세 위치 : ${it.addressForGuestHouseDistrict}", // 오시는길
                    "수정일 : ${it.modifyTimeForGuestHouse}\n\n등록일 : ${it.createdTime}", // 주의사항
                    "게스트 하우스 이름 : ${it.titleForGuestHouse}"  // 여기어때
                )

            } ?: emptyList()
            viewPager.adapter = DetailAdapter(detailList)
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            submitBtn.setOnClickListener {
                val resultIntent = Intent().apply {
                    arguments?.getParcelable("guesthouse_data", GuestHouseData::class.java)?.let {
                        Log.d("GuestMatchingFragment", "DetailFragment : ${it.titleForGuestHouse}")
                        putExtra(
                            "selected_data",
                            it.titleForGuestHouse)
                        putExtra("thumbnailUrl", it.imageUrl)
                    }
                }
//                arguments?.getParcelable("guesthouse_data", GuestHouseData::class.java)?.let {
//                    parentFragmentManager.setFragmentResult("requestKey", Bundle().apply {
//                        putParcelable("selected_data", it)
//                    })
//                }
                requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                requireActivity().finish() // SearchActivity 종료
            }


            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "소개"
                    1 -> "오시는길"
                    2 -> "주의사항"
                    3 -> "여기어때"
                    else -> null
                }
            }.attach()
        }
        // 탭 클릭 리스너 설정
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 3) { // "여기어때" 탭 클릭
                    val intent = Intent(requireContext(), WebViewActivity::class.java)
                    intent.putExtra("url", hereLink)
                    startActivity(intent)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    private fun setupLinks() {
//        // 텍스트 영역에서 URL 자동 링크화
//        val textView = binding.tabLayout
//        LinkifyCompat.addLinks(textView, Linkify.WEB_URLS)
//        textView.movementMethod = LinkMovementMethod.getInstance()
//
//        // 텍스트에 링크를 클릭했을 때 처리
//        textView.setOnClickListener {
//            val text = textView.text.toString()
//            val url = extractUrl(text)
//
//            if (url != null) {
//                val intent = Intent(this, WebViewActivity::class.java)
//                intent.putExtra("url", url)
//                startActivity(intent)
//            }
//        }
//    }
//
//    private fun extractUrl(text: String): String? {
//        val urlPattern = "http[s]?://[a-zA-Z0-9./?=_-]+".toRegex()
//        return urlPattern.find(text)?.value
//    }
}