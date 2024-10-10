package com.example.tripplan.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.FBRef
import com.example.tripplan.UserInfo
import com.example.tripplan.databinding.ActivityHistoryBinding
import com.example.tripplan.fragment.GuestModel
import com.example.tripplan.fragment.SubmitModel
import com.example.tripplan.matematching.CompatibilityCalculatorInGuest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class GuestHistoryActivity: AppCompatActivity() {
    private val TAG = GuestHistoryActivity::class.java.simpleName
    private lateinit var binding : ActivityHistoryBinding
    // key값 선언
    private lateinit var guestData: GuestModel
    private lateinit var submitData: SubmitModel
    private var realCompatibilityKey = ""
    private var partnerEmail: String = ""
    private lateinit var key : String
    private lateinit var _key : String
    private lateinit var __key : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnRetryMatching.text = "거절 하기"
        // History에서 보낸 key 데이터 받아오기
        key = intent.getStringExtra("__key").toString()
        _key = intent.getStringExtra("item").toString()
        __key = intent.getStringExtra("_key").toString()

        // 데이터 로드 호출 (Firebase에서 데이터 가져와서 궁합 계산)
        calculateAndSaveCompatibilityScores()

        with(binding) {
            btnMatching.setOnClickListener {
                UserInfo.email?.let { it1 ->
                    FBRef.compatibilityCalInGuestRef.child("${guestData.email}_${submitData.email}").child("매칭 여부").child(
                        it1
                    ).setValue("Confirm")
                }
                // 매칭 여부를 Firebase에서 읽어오기
                FBRef.compatibilityCalInGuestRef.child("${guestData.email}_${submitData.email}").child("매칭 여부").child("${guestData.email}").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // snapshot을 Map으로 가져와서 필요한 값을 확인
                        val matchingStatus = snapshot.getValue(String::class.java) ?: ""

                        when (matchingStatus) {
                            "Cancel" -> {
                                Toast.makeText(
                                    this@GuestHistoryActivity,
                                    "거절했던 상대입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                partnerKakaoId.text = "상대방 카카오톡 아이디: ${submitData.kakaoId}"
                                Toast.makeText(
                                    this@GuestHistoryActivity,
                                    "상대방의 카카오톡 아이디를 확인하세요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    }
                })
            }
            btnRetryMatching.setOnClickListener {
                UserInfo.email?.let { it1 ->
                    FBRef.compatibilityCalInGuestRef.child("${guestData.email}_${submitData.email}").child("매칭 여부").child(
                        it1
                    ).setValue("Cancel")
                }
                Toast.makeText(this@GuestHistoryActivity, "매칭이 거절 되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPartnerEmail() {
        val currentUserEmail = UserInfo.email

        // compatibility 노드에서 email 두 개가 '_'로 이어진 항목을 가져옴
        val compatibilityListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val key = snapshot.key.toString()
                    val emails = key.split("_")

                    // 내 email가 포함되어 있는지 확인
                    if (emails.contains(currentUserEmail)) {
                        // 내 email가 포함된 경우, 짝이 지어진 상대방 email 추출
                        val potentialPartnerEmail =
                            if (emails[0] == currentUserEmail) emails[1] else emails[0]
                        partnerEmail = potentialPartnerEmail
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.compatibilityCalInGuestRef.addListenerForSingleValueEvent(compatibilityListener)
    }
//    private fun getMatchingData(partnerEmail: String) {
//        val currentUserEmail = UserInfo.email
//        val submitListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                var found = false
//                for (snapshot in dataSnapshot.children) {
//                    val dataModel = snapshot.getValue(SubmitModel::class.java)
//                    Log.d("abcd123222222222", dataModel.toString())
//                    if (dataModel != null) {
//                        this@GuestHistoryActivity.partnerEmail = dataModel.email
//                    }
//
//                    // 두 가지 경우의 compatibility key를 확인
//                    val compatibilityKey1 = "${currentUserEmail}_${partnerEmail}"
//                    val compatibilityKey2 = "${partnerEmail}_${currentUserEmail}"
//                    Log.d("abcd123444com", compatibilityKey1)
//                    Log.d("abcd123444com", compatibilityKey2)
//
//                    // Compatibility Score를 Firebase에서 직접 읽어오기
//                    // 첫 번째 키로 시도
//                    var compatibilityRef = FBRef.compatibilityCalInGuestRef.child(compatibilityKey1).child("매칭 여부")
//                    realCompatibilityKey = compatibilityKey1
//                    compatibilityRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(matchingSnapshot: DataSnapshot) {
//                            if (!matchingSnapshot.exists()) {
//                                realCompatibilityKey = compatibilityKey2
//                                // 첫 번째 키가 존재하지 않으면 두 번째 키로 시도
//                                compatibilityRef =
//                                    FBRef.compatibilityCalInGuestRef.child(compatibilityKey2)
//                                        .child("매칭 여부")
//                                compatibilityRef.addListenerForSingleValueEvent(this)
//                                return
//                            }
//                        }
//                        override fun onCancelled(databaseError: DatabaseError) {
//                            Log.w(TAG, "loadScore:onCancelled", databaseError.toException())
//                        }
//                    })
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FBRef.submitGuestMatchingRef.addListenerForSingleValueEvent(submitListener)
//    }
    private fun calculateAndSaveCompatibilityScores() {
        // Firebase에서 GuestModel 데이터를 가져옴
        FBRef.guestMatchingRef.child(__key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(guestSnapshot: DataSnapshot) {
                guestData = guestSnapshot.getValue(GuestModel::class.java)!!

                // Firebase에서 SubmitModel (상대방 신청글) 데이터를 가져옴
                FBRef.submitGuestMatchingRef.child(__key).child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(submitSnapshot: DataSnapshot) {
                        submitData = submitSnapshot.getValue(SubmitModel::class.java)!!


                        // CompatibilityCalculator 인스턴스 생성
                        val compatibilityCalculator = CompatibilityCalculatorInGuest()

                        // 궁합 점수 계산
                        val compatibilityScore = compatibilityCalculator.calculateCompatibilityInGuest(
                            guestData,
                            submitData
                        )

                        // 계산된 궁합 점수를 Firebase에 저장
                        val scoreRef =
                            FBRef.compatibilityCalInGuestRef.child("${guestData.email}_${submitData.email}").child("Score")
                        scoreRef.setValue(compatibilityScore).addOnSuccessListener {
                            Log.d("Firebase", "Compatibility score saved successfully for ${guestData.email}_${submitData.email}.")
                            updateUI(guestData, submitData, compatibilityScore)
                        }.addOnFailureListener { exception ->
                            Log.e("Firebase", "Error saving compatibility score for ${guestData.email}_${submitData.email}", exception)
                        }
                        val scoreRefYN = FBRef.compatibilityCalInGuestRef.child("${guestData.email}_${submitData.email}")
                            .child("매칭 여부")
                        scoreRefYN.child("${UserInfo.email}").setValue("")
                        // push()를 붙이면 새 고유 키를 추가 하고 붙이지 않으면 고유 키 값이 추가 되지 않는다.
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }


    // 궁합도 계산 후 UI에 업데이트하는 함수
    private fun updateUI(guest: GuestModel, submission: SubmitModel, compatibilityScore: Double) {
        runOnUiThread {
            binding.apply {
                partnerNickname.text = "상대방: ${submission.nickname}"
                partnerMbti.text = "MBTI: ${submission.mbti}"
                partnerAge.text = "나이: ${submission.age}"
                partnerGender.text = "성별: ${submission.gender}"
                partnerTravelPeriod.text = "여행 기간: ${submission.date}"

                compatibilityProgressBar.max = 100
//                compatibilityProgressBar.progress = compatibilityScore.toInt()
                // 프로그레스바 애니메이션 설정
                setProgressWithAnimation(compatibilityScore.toInt())
//                compatibilityPercent.text = "궁합도: $compatibilityScore%"
//                compatibilityDescription.text = "궁합도 설명: ${compatibilityDescription}"
                compatibilityPercent.text = "${compatibilityScore}점/100점"
                compatibilityDescription.text = getCompatibilityDescription(compatibilityScore)
            }
        }
    }
    // 궁합도 점수에 따른 설명
    private fun getCompatibilityDescription(score: Double): String {
        return when {
            score >= 80 -> "완벽한 궁합!\n서로의 모든 점에서 잘 맞아, 함께하는 모든 순간이 즐거울 거예요!"
            score >= 60 -> "좋은 궁합이예요!\n서로 잘 맞는 부분이 많아, 좋은 메이트가 될 것 같아요."
            score >= 40 -> "평균적인 궁합이예요.\n서로 이해하고 배려한다면 좋은 메이트가 될 수 있을 것 같아요."
            score >= 20 -> "고민 해봐야 할 궁합이예요.\n서로의 차이를 이해하는 것이 중요할 것 같아요."
            else -> "음...안 맞는 궁합이네요.\n매칭을 추천하지 않아요.."
        }
    }
    // 프로그레스바 애니메이션 설정 함수
    @SuppressLint("ObjectAnimatorBinding")
    private fun setProgressWithAnimation(progressTo: Int) {
        val progressBar = binding.compatibilityProgressBar // binding을 사용하여 ProgressBar 연결
        val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, progressTo)
        progressAnimator.duration = 1000 // 애니메이션 지속 시간
        progressAnimator.start()
    }
}