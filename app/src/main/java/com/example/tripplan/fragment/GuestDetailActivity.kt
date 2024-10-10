//package com.example.tripplan.Fragment
//
//import android.app.Activity
//import android.content.Intent
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.isVisible
//import com.bumptech.glide.Glide
//import com.example.tripplan.Activity.SubmitGuestActivity
//import com.example.tripplan.FBAuth
//import com.example.tripplan.FBRef
//import com.example.tripplan.R
//import com.example.tripplan.data.GuestHouseData
//import com.example.tripplan.databinding.ActivityGuestDetailBinding
//import com.example.tripplan.detail.DetailAdapter
//import com.google.android.material.tabs.TabLayoutMediator
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.ValueEventListener
//
//class GuestDetailActivity : AppCompatActivity() {
//
//    private val TAG = GuestDetailActivity::class.java.simpleName
//
//    private lateinit var binding: ActivityGuestDetailBinding
//    private lateinit var key: String
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // ViewBinding 사용
//        binding = ActivityGuestDetailBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Home에서 보낸 key 데이터 받아오기
//        key = intent.getStringExtra("key").toString()
//
//        // Firebase 데이터 가져오기
//        getGuestData(key)
//
//        // 신청하기 버튼
//        binding.submitBtn.setOnClickListener {
//            val intent = Intent(this, SubmitGuestActivity::class.java)
//            startActivity(intent)
//        }
//
//        // 뒤로가기 버튼 설정
//        binding.backButton.setOnClickListener {
//            finish()
//        }
//    }
//
//    // Firebase에서 게스트 데이터를 가져오는 함수
//    private fun getGuestData(key: String) {
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                try {
//                    val dataModel = dataSnapshot.getValue(GuestModel::class.java)
//                    dataModel?.let {
//                        // UI에 데이터 반영
//                        binding.guestNickname.text = it.nickname
//                        binding.guestAge.text = it.age
//                        binding.guestDate.text = it.date
//
//                        // 프로필 사진 로드
//                        Glide.with(this@GuestDetailActivity)
//                            .load(it.imageUrl)
//                            .placeholder(R.drawable.ic_open_yak)
//                            .into(binding.guestIv)
//
//                        // 내 uid와 게시글 작성자 uid 비교
//                        val myUid = FBAuth.getUid()
//                        val writerUid = it.uid
//
//                        // 글쓴 사람만 게시글 수정, 삭제 가능
//                        binding.submitBtn.isVisible = myUid != writerUid
//
//                        // 탭에 표시할 내용 설정
//                        val detailList = listOf(
//                            "닉네임 : ${it.nickname}\n나이 : ${it.age}", // 소개
//                            "여행 날짜 : ${it.date}", // 게스트하우스 정보
//                            "저는 ${it.nickname}" // 원하는 게스트 스타일
//                        )
//
//                        // ViewPager에 DetailAdapter 연결
//                        binding.viewPager.adapter = DetailAdapter(detailList)
//
//                        // TabLayout 설정
//                        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
//                            tab.text = when (position) {
//                                0 -> "소개"
//                                1 -> "게스트하우스 정보"
//                                2 -> "원하는 게스트 스타일"
//                                else -> null
//                            }
//                        }.attach()
//                    }
//                } catch (e: Exception) {
//                    Log.d(TAG, "삭제완료")
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FBRef.guestMatchingRef.child(key).addValueEventListener(postListener)
//    }
//
////    private lateinit var binding: ActivityGuestDetailBinding
////    private lateinit var guestHouseData: GuestHouseData
////
////    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        binding = ActivityGuestDetailBinding.inflate(layoutInflater)
////        setContentView(binding.root)
////
////        // Intent로 전달받은 데이터 가져오기
////        guestHouseData = intent.getParcelableExtra("guesthouse_data", GuestHouseData::class.java) ?: return
////
////        setupUI()
////    }
////
////    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
////    private fun setupUI() {
////        with(binding) {
////            // 데이터를 UI에 반영
////            Glide.with(this@GuestDetailActivity)
////                .load(guestHouseData.imageUrl)
////                .placeholder(R.drawable.ic_open_yak)
////                .into(guestIv)
////
//////            guestNickname.text = guestHouseData.titleForGuestHouse
//////            guestAge.text = guestHouseData.addressForGuestHouse
//////            guestDate.text = guestHouseData.telForGuestHouse
////
//////            listOf(
//////                "숙소명 : ${it.titleForGuestHouse}\n전화번호 : ${it.telForGuestHouse}", // 소개
//////                "위치 : ${it.addressForGuestHouse}\n상세 위치 : ${it.addressForGuestHouseDistrict}", // 오시는길
//////                it.telForGuestHouse ?: "", // 편의시설
//////                "수정일 : ${it.modifyTimeForGuestHouse}\n등록일 : ${it.createdtime}"  // 주의사항
//////            )
////            val detailList = listOf(
////
////                "닉네임 : ${}",
////                "",
////                "저는 ${}" // 원하는 게스트 스타일
////                "닉네임 : ${guestHouseData.titleForGuestHouse}\n전화번호 : ${guestHouseData.telForGuestHouse}", // 소개
////                "위치 : ${guestHouseData.addressForGuestHouse}\n상세 위치 : ${guestHouseData.addressForGuestHouseDistrict}", // 오시는길
////                guestHouseData.telForGuestHouse ?: "", // 편의시설
////                "수정일 : ${guestHouseData.modifyTimeForGuestHouse}\n등록일 : ${guestHouseData.createdtime}"  // 주의사항
////            )
////            )
////
////            viewPager.adapter = DetailAdapter(detailList)
////
////            backButton.setOnClickListener {
////                finish()
////            }
////
////            submitBtn.setOnClickListener {
////                val resultIntent = Intent().apply {
////                    putExtra("selected_data", guestHouseData.titleForGuestHouse)
////                }
////                setResult(Activity.RESULT_OK, resultIntent)
////                finish()
////            }
////
////            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
////                tab.text = when (position) {
////                    0 -> "소개"
////                    1 -> "게스트하우스 정보"
////                    2 -> "원하는 게스트 스타일"
////                    else -> null
////                }
////            }.attach()
////        }
////    }
//}