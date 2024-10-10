package com.example.tripplan.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.tripplan.FBRef
import com.example.tripplan.KaKaoAuthViewModel
import com.example.tripplan.matematching.MateModel
import com.example.tripplan.R
import com.example.tripplan.UserInfo
import com.example.tripplan.databinding.ActivityHistoryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class HistoryActivity: AppCompatActivity() {
    private val TAG = HistoryActivity::class.java.simpleName
    private lateinit var binding : ActivityHistoryBinding
    // key값 선언
    private lateinit var key : String
    private var myDestination: String = ""
    private var kakaoId: String = ""
    private var realCompatibilityKey = ""
    private var partnerEmail: String = ""
    // 이미 본 메이트들의 이메일을 저장하는 리스트
    private var seenMateEmails = mutableSetOf<String>()
    private var updatedSeenEmails:String = ""
    lateinit var compatibilityAgree: String // 초기화는 나중에 할당하기 위해 변수 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityHistoryBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_history)
        // History에서 보낸 key 데이터 받아오기
        key = intent.getStringExtra("__key").toString()
        Log.d("abcd1234", key)

        getMyDestination()
        // 매칭된 데이터 가져오기
        FBRef.mateMatchingRef.child(key).child("seenEmail").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Firebase에 이미 저장된 이메일 목록을 불러옵니다.
                val existingEmails = snapshot.getValue(String::class.java)?.split("_")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toMutableSet() ?: mutableSetOf()
                val lastEmail = existingEmails.lastOrNull()
                // 새로운 이메일이 이미 목록에 없는 경우 추가
                if (lastEmail != null) {
                    getMateData(lastEmail)
                }
                else {
                    // 다시 매칭 (이미 본 메이트를 제외하고 새 메이트 가져오기)
                    getMatchedData { hasMoreData ->
                        if (!hasMoreData) {
                            Toast.makeText(this@HistoryActivity, "더 이상 매칭할 수 있는 사람이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Failed to read seenEmail from Firebase", databaseError.toException())
            }
        })


        with(binding) {


            btnMatching.setOnClickListener {
                if (partnerEmail.isEmpty()) {
                    Toast.makeText(this@HistoryActivity, "매칭된 상대가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                UserInfo.email?.let { email ->
                    FBRef.compatibilityCalRef.child(realCompatibilityKey).child("매칭 여부").child(email).setValue("Confirm")
                }//                 매칭 여부를 Firebase에서 읽어오기
                val matchingStatus = compatibilityAgree
                Log.d("abcd321", compatibilityAgree)
                when (matchingStatus) {
                    "Confirm" -> {
                        partnerKakaoId.text = "상대방 카카오톡 아이디: $kakaoId"
                        Toast.makeText(this@HistoryActivity, "매칭이 완료 되었습니다. 상대방의 카카오톡 아이디를 확인하세요.", Toast.LENGTH_SHORT).show()
                    }
                    "Cancel" -> {
                        Toast.makeText(this@HistoryActivity, "상대방의 거절로 매칭에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@HistoryActivity, "상대방이 아직 매칭을 결정하지 않았습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
//                FBRef.compatibilityCalRef.child(realCompatibilityKey).child("매칭 여부").child(partnerEmail).addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        Log.d("abcd321", partnerEmail)
//                        // 데이터를 String으로 읽어옴
//                        val matchingStatus = snapshot.getValue(String::class.java)
//
//                        when (matchingStatus) {
//                            "Confirm" -> {
//                                partnerKakaoId.text = "상대방 카카오톡 아이디: $kakaoId"
//                                Toast.makeText(this@HistoryActivity, "매칭이 완료 되었습니다. 상대방의 카카오톡 아이디를 확인하세요.", Toast.LENGTH_SHORT).show()
//                            }
//                            "Cancel" -> {
//                                Toast.makeText(this@HistoryActivity, "상대방의 거절로 매칭에 실패했습니다.", Toast.LENGTH_SHORT).show()
//                            }
//                            else -> {
//                                Toast.makeText(this@HistoryActivity, "상대방이 아직 매칭을 결정하지 않았습니다.", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {
//                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//                    }
//                })
            }

            btnRetryMatching.setOnClickListener {
                if (partnerEmail.isEmpty()) {
                    Toast.makeText(this@HistoryActivity, "매칭된 상대가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                UserInfo.email?.let { email ->
                    FBRef.compatibilityCalRef.child(realCompatibilityKey).child("매칭 여부").child(email).setValue("Cancel")
                }
                // 현재 매칭에서 본 메이트 이메일 기록
//                val mateModel = MateModel() // 실제 사용 중인 MateModel 객체
                saveSeenMateEmail(partnerEmail)
                // 다시 매칭 (이미 본 메이트를 제외하고 새 메이트 가져오기)
                getMatchedData { hasMoreData ->
                    if (!hasMoreData) {
                        Toast.makeText(this@HistoryActivity, "더 이상 매칭할 수 있는 사람이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else Toast.makeText(this@HistoryActivity, "다시 매칭 되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    private fun saveSeenMateEmail(email: String) {
        // Firebase에서 기존 seenEmail 값을 불러옵니다.
        FBRef.mateMatchingRef.child(key).child("seenEmail").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Firebase에 이미 저장된 이메일 목록을 불러옵니다.
                val existingEmails = snapshot.getValue(String::class.java)?.split("_")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toMutableSet() ?: mutableSetOf()

                // 새로운 이메일이 이미 목록에 없는 경우 추가
                if (!existingEmails.contains(email)) {
                    existingEmails.add(email)
                }

                // 업데이트할 seenEmail 값을 콤마로 구분하여 저장
                updatedSeenEmails = existingEmails.joinToString("_")
                Log.d("abcd1234ex", existingEmails.toString())

                // Firebase에 업데이트
                FBRef.mateMatchingRef.child(key).child("seenEmail").setValue(updatedSeenEmails)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("abcd123mm", updatedSeenEmails)
                        } else {
                            Log.e(TAG, "Failed to update seenEmail in Firebase", task.exception)
                        }
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Failed to read seenEmail from Firebase", databaseError.toException())
            }
        })
    }

    // 1. compatibility에서 email 확인 후 mateMatching에서 데이터 가져오기
    private fun getMatchedData(callback: (Boolean) -> Unit) {
        val currentUserEmail = UserInfo.email
        var hasMoreData = false // 매칭할 수 있는 데이터가 있는지 여부

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

                        // 이미 본 메이트는 제외
                        if (!updatedSeenEmails.contains(potentialPartnerEmail)) {
                            hasMoreData = true
                            partnerEmail = potentialPartnerEmail
                            getMateData(partnerEmail)
                            break // 조건에 맞는 메이트를 찾았으니 루프 종료
                        }
                    }
                }
                callback(hasMoreData) // 매칭할 수 있는 데이터가 있는지 콜백 호출
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                callback(false) // 오류 발생 시 더 이상 매칭할 수 없음
            }
        }
        FBRef.compatibilityCalRef.addListenerForSingleValueEvent(compatibilityListener)
    }


    private fun getMyDestination() {
        // Firebase에서 key를 기반으로 데이터를 읽어오는 ValueEventListener를 설정합니다.
        val mateListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // key에 해당하는 데이터가 있는지 확인하고, destination 값을 가져옵니다.
                val data = dataSnapshot.child(key).getValue(MateModel::class.java)
                myDestination = data?.destination ?: "DefaultDestination"

                // 필요한 추가 작업 수행
                // 예를 들어, destination 값을 다른 곳에서 사용하거나 UI를 업데이트할 수 있습니다.
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        // Firebase에서 데이터 읽기
        FBRef.mateMatchingRef.addListenerForSingleValueEvent(mateListener)
    }

    private fun getMateData(partnerEmail: String) {
        val currentUserEmail = UserInfo.email
        val mateListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var found = false
                for (snapshot in dataSnapshot.children) {
                    val dataModel = snapshot.getValue(MateModel::class.java)


                    // 이미 본 메이트를 제외하고 처리
                    if (dataModel != null && dataModel.email == partnerEmail && dataModel.destination == myDestination) {
//                        val seenEmails = dataModel.seenEmail.split("_").map { it.trim() }
//                        if (seenEmails.contains(currentUserEmail)) {
//                            continue // 이미 본 메이트는 제외
//                        }
                        found = true
                        kakaoId = dataModel.kakaoId
                        with(binding) {
                            // ViewBinding을 통해 레이아웃에 데이터 바인딩
                            partnerNickname.text = "매칭상대 ${dataModel.nickname}님의 정보에요!"
                            partnerMbti.text = "MBTI: ${dataModel.mbti}"
                            partnerAge.text = "나이: ${dataModel.age}"
                            partnerGender.text = "성별: ${dataModel.gender}"
                            partnerTravelStyle.text = "여행 스타일: ${dataModel.style}"
                            partnerTravelPeriod.text = "여행 기간: ${dataModel.date}"
                            partnerTravelExpense.text = "여행 경비: ${dataModel.expense}만원"

                            // 두 가지 경우의 compatibility key를 확인
                            val compatibilityKey1 = "${UserInfo.email}_${partnerEmail}"
                            val compatibilityKey2 = "${partnerEmail}_${UserInfo.email}"

                            // Compatibility Score를 Firebase에서 직접 읽어오기
                            var compatibilityAgreeRef = FBRef.compatibilityCalRef.child(compatibilityKey1).child("매칭 여부").child("${partnerEmail}")
                            realCompatibilityKey = compatibilityKey1
                            compatibilityAgreeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(agreeSnapshot: DataSnapshot) {
                                    if (!agreeSnapshot.exists()) {
                                        realCompatibilityKey = compatibilityKey2
                                        // 첫 번째 키가 존재하지 않으면 두 번째 키로 시도
                                        compatibilityAgreeRef =
                                            FBRef.compatibilityCalRef.child(compatibilityKey2).child("매칭 여부").child("${partnerEmail}")
                                        compatibilityAgreeRef.addListenerForSingleValueEvent(this)
                                        return
                                    }
                                    compatibilityAgree = agreeSnapshot.getValue(String::class.java) ?: ""

                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.w(TAG, "loadScore:onCancelled", databaseError.toException())
                                }
                            })
                            // 첫 번째 키로 시도
                            var compatibilityRef = FBRef.compatibilityCalRef.child(compatibilityKey1).child("Score")
                            realCompatibilityKey = compatibilityKey1
                            compatibilityRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(scoreSnapshot: DataSnapshot) {
                                    if (!scoreSnapshot.exists()) {
                                        realCompatibilityKey = compatibilityKey2
                                        // 첫 번째 키가 존재하지 않으면 두 번째 키로 시도
                                        compatibilityRef =
                                            FBRef.compatibilityCalRef.child(compatibilityKey2).child("Score")
                                        compatibilityRef.addListenerForSingleValueEvent(this)
                                        return
                                    }

                                    var compatibilityScore = scoreSnapshot.getValue(Int::class.java) ?: 0

                                    // ProgressBar의 최대값을 설정합니다.
                                    compatibilityProgressBar.max = 100
                                    // Compatibility Score를 ProgressBar의 진행 상태로 설정합니다.
//                                    compatibilityProgressBar.progress = compatibilityScore
                                    // 프로그레스바 애니메이션 설정
                                    setProgressWithAnimation(compatibilityScore)
//                                    compatibilityPercent.text = "궁합도: $compatibilityScore%"
//                                    compatibilityDescription.text = getCompatibilityDescription(compatibilityScore)

                                    // Compatibility Score를 텍스트로 설정합니다.
                                    compatibilityPercent.text = "${compatibilityScore}점/100점"
//                                    compatibilityDescription.text =
//                                        "궁합도 설명: ${compatibilityDescription}"

                                    // 궁합도 설명 설정
                                    compatibilityDescription.text = getCompatibilityDescription(compatibilityScore)
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.w(TAG, "loadScore:onCancelled", databaseError.toException())
                                }
                            })
                        }
//                        FBRef.mateMatchingRef.child(key).child("seenEmail").setValue(updatedSeenEmails)
                        // 화면이 로드된 후, 바로 seenEmail에 추가
                        saveSeenMateEmail(partnerEmail)
                        Log.d("abcd123UUU", updatedSeenEmails)
                        break
                    }
                }

                if (!found) {
                    Log.d(TAG, "Partner email not found in the data.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.mateMatchingRef.addListenerForSingleValueEvent(mateListener)
    }

    // 궁합도 점수에 따른 설명
    private fun getCompatibilityDescription(score: Int): String {
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