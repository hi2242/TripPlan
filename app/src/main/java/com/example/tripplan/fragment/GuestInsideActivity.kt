package com.example.tripplan.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.tripplan.activity.SubmitGuestActivity
import com.example.tripplan.FBRef
import com.example.tripplan.KaKaoAuthViewModel
import com.example.tripplan.KaKaoAuthViewModel.Companion
import com.example.tripplan.UserInfo
import com.example.tripplan.board.WebViewActivity
import com.example.tripplan.data.GuestHouseData
import com.example.tripplan.databinding.ActivityGuestDetailBinding
import com.example.tripplan.detail.DetailAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class GuestInsideActivity : AppCompatActivity() {

    private val TAG = GuestInsideActivity::class.java.simpleName
    private val submitDataList = mutableListOf<SubmitModel>()
    private lateinit var binding: ActivityGuestDetailBinding
    private lateinit var storageRef: StorageReference
    // key값 선언
    private lateinit var leftTime: String
    private lateinit var key: String
    private lateinit var writerEmail: String
    private lateinit var hereLink: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 사용
        binding = ActivityGuestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }
        // 신청하기 버튼
        binding.submitBtnInGuest.setOnClickListener {
            // SubmitGuestActivity로 이동
            if (leftTime == "available") {
                val intent = Intent(this, SubmitGuestActivity::class.java)
                intent.putExtra("_key", key)   // key값 넘겨줌
                startActivity(intent)
            }
            else if (leftTime == "30unavailable") {
                Toast.makeText(this@GuestInsideActivity, "마감 30분 미만이므로 신청할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
            else if (leftTime == "unavailable") {
                Toast.makeText(this@GuestInsideActivity, "이미 마감되었습니다.", Toast.LENGTH_SHORT).show()            }
        }

        // Home에서 보낸 key 데이터 받아오기
        key = intent.getStringExtra("key").toString()

        getGuestData(key)
    }

    // board데이터 받아오는 함수
    private fun getGuestData(key : String){

        // 데이터 가져오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                // try문에서 에러발생하면 catch문 실행
                try {

                    //데이터 받아오기
                    val dataModel = dataSnapshot.getValue(GuestModel::class.java)
                    dataModel?.let {
                        val myEmail = UserInfo.email  // 현재 내 email
                        writerEmail = dataModel.email // 글쓴사람의 email
                        // Firebase Storage 초기화
                        storageRef = FirebaseStorage.getInstance().reference
                        getKakaoUserInfoFromFirebase(writerEmail)
                        // 레이아읏과 연결
                        binding.guestNickname.text = dataModel!!.nickname   // titleArea와 BoardModel 연결
                        binding.guestAge.text = dataModel!!.age  // textArea와 BoardModel 연결
                        binding.guestDate.text = dataModel!!.date     // timeArea와 BoardModel 연결
                        // 프로필 사진 받아와서 넣기

                        // 스타일을 줄바꿈으로 나누어 표시
                        val styles = it.style.split(", ") // 스타일이 쉼표로 구분된 경우
                        val stylesFormatted = styles.joinToString(separator = "\n") { style -> "- $style" } // 스타일 목록 형식화



                        val currentTime = System.currentTimeMillis()
                        val timeDifference = 3 * 24 * 60 * 60 * 1000 - (currentTime - dataModel.timestamp) // 3일을 밀리초로 변환
                        val thirtyMinutesInMillis = 30 * 60 * 1000 // 30분을 밀리초로 변환
                        // 글쓴사람이면 신청 버튼 숨기기, 아니면 활성화
                        if (myEmail != writerEmail) {
                            if (timeDifference > thirtyMinutesInMillis) {
                                // 마감 시간이 30분 이상 남았을 때 신청 버튼 활성화
                                binding.submitBtnInGuest.isVisible = true
                                leftTime = "available"
                            } else if (timeDifference in 1 until thirtyMinutesInMillis) {
                                // 마감 30분 미만일 경우 버튼 비활성화 및 토스트 메시지
                                binding.submitBtnInGuest.isVisible = true
                                leftTime = "30unavailable"
                            } else {
                                // 이미 마감된 경우
                                binding.submitBtnInGuest.isVisible = true
                                leftTime = "unavailable"
                            }
                        } else {
                            binding.submitBtnInGuest.isVisible = false
                        }
                        hereLink = "https://www.yeogi.com/domestic-accommodations?searchType=KEYWORD&keyword=${it.nameGuestHouse}&autoKeyword=&personal=2&freeForm=true"

                        // 탭에 표시할 내용 설정
                        val detailList = listOf(
                            "닉네임 : ${it.nickname}\n\n나이 : ${it.age}\n\n성별 : ${it.gender}\n\nMBTI : ${it.mbti}\n\n" +
                                    "여행 날짜 : ${it.date}", // 소개
                            "저는 다음과 같은 스타일의 룸메이트를 원합니다.\n$stylesFormatted", // 원하는 게스트 스타일
                            "게스트하우스 이름 : ${it.nameGuestHouse}" // 게스트하우스 정보

                        )

                        // ViewPager에 DetailAdapter 연결
                        binding.viewPager.adapter = DetailAdapter(detailList)
                        // TabLayout 설정
                        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                            tab.text = when (position) {
                                0 -> "소개"
                                1 -> "원하는 게스트 스타일"
                                2 -> "게스트하우스 정보"
                                else -> null
                            }
                        }.attach()
                    }
                    // 탭 클릭 리스너 설정
                    binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            if (tab?.position == 2) { // "여기어때" 탭 클릭
                                val intent = Intent(this@GuestInsideActivity, WebViewActivity::class.java)
                                intent.putExtra("url", hereLink)
                                startActivity(intent)
                            }
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) {}
                        override fun onTabReselected(tab: TabLayout.Tab?) {}
                    })
                }
                catch (e: Exception){
                    Log.d(TAG, "삭제완료")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // board안에있는 key값을 가져오기
        FBRef.guestMatchingRef.child(key).addValueEventListener(postListener)

    }
    // 카카오 유저 이메일/닉네임/프로필 사진 표시
    private fun getKakaoUserInfoFromFirebase(email: String) {
        if (email.isNullOrEmpty()) {
            Log.e(KaKaoAuthViewModel.TAG, "이메일 정보 없음")
            return
        }

        FBRef.kakaoUserRef.child(email).get().addOnSuccessListener { snapshot ->
            val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

            // 프로필 이미지 URL이 존재하면 이미지 로드
            if (profileImageUrl != null) {
                Glide.with(this)
                    .load(profileImageUrl)
                    .into(binding.guestIv)
                Log.d(KaKaoAuthViewModel.TAG, "프로필 이미지 URL: $profileImageUrl")
            } else {
                Log.w(KaKaoAuthViewModel.TAG, "프로필 이미지 URL 정보 없음")
            }
        }.addOnFailureListener {
            Log.e(KaKaoAuthViewModel.TAG, "유저 정보 가져오기 실패", it)
        }
    }

}