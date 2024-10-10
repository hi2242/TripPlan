package com.example.tripplan.activity

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.FBRef
import com.example.tripplan.KaKaoAuthViewModel.Companion.TAG
import com.example.tripplan.R
import com.example.tripplan.UserInfo
import com.example.tripplan.databinding.ActivityEditInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.regex.Pattern

class EditInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditInfoBinding
    private lateinit var mDbRef: DatabaseReference // Firebase Database 참조 객체
    private lateinit var mAuth: FirebaseAuth // Firebase 인증 객체
    var CheckNick: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 인증 및 데이터베이스 초기화
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        Log.d(TAG, "Firebase 인증 및 데이터베이스 초기화 완료")

        // 루트 레이아웃 클릭 시 키보드 숨기기
        with(binding) {
            rootLayout.setOnClickListener {
                hideKeyboard()
                Log.d(TAG, "키보드 숨기기")
            }

            // 닉네임 중복 확인 버튼 클릭 리스너
            btnCheckNickReg.setOnClickListener {
                val nickname = newNickname.text.toString().trim()
                Log.d(TAG, "닉네임 중복 확인 클릭: $nickname")
                val nickPattern = "^[ㄱ-ㅣ가-힣]*$"
                if (nickname == "") {
                    Toast.makeText(
                        this@EditInfoActivity,
                        "변경하실 닉네임을 입력해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else{
                    if (Pattern.matches(nickPattern, nickname)) {
                        checkNicknameInFirebase(nickname) { isDuplicate ->
                            if (!isDuplicate) {
                                CheckNick = true
                                Toast.makeText(this@EditInfoActivity, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "사용 가능한 닉네임: $nickname")
                            } else {
                                Toast.makeText(this@EditInfoActivity, "이미 존재하는 닉네임입니다.", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "중복된 닉네임: $nickname")
                            }
                        }
                    } else {
                        Toast.makeText(this@EditInfoActivity, "닉네임 형식이 옳지 않습니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "잘못된 닉네임 형식: $nickname")
                    }
                }
            }

            // MBTI 수정 버튼 클릭 리스너
            newMBTI.setOnClickListener {
                Log.d(TAG, "MBTI 수정 버튼 클릭")
                showMBTIDialog()
            }

            // 생년월일 수정 버튼 클릭 리스너
            newAge.setOnClickListener {
                Log.d(TAG, "생년월일 수정 버튼 클릭")
                showDatePickerDialog()
            }

            // 정보 수정 버튼 클릭 리스너
            btnEditInfo.setOnClickListener {
                val name = newName.text.toString().trim()
                val nickname = newNickname.text.toString().trim()
                val phoneNumber = newPhoneNumber.text.toString().trim()
                val kakaoId = newKakaoId.text.toString().trim()
                val age = newAge.text.toString().trim()
                val mbti = newMBTI.text.toString().trim()
                val car = when (newCar.checkedRadioButtonId) {
                    R.id.radioCarYes -> "있음"
                    R.id.radioCarNo -> "없음"
                    else -> ""
                }
                Log.d(TAG, "정보 수정 버튼 클릭 - 이름: $name, 닉네임: $nickname, 전화번호: $phoneNumber, 카카오톡 ID: $kakaoId, 나이: $age, MBTI: $mbti, 자차 여부: $car")

                // 업데이트할 정보를 담기 위한 맵 생성
                val updates = mutableMapOf<String, Any>()

                // 변경된 정보만 맵에 추가
                if (name.isNotEmpty()) updates["name"] = name
                if (nickname.isNotEmpty()) updates["nickname"] = nickname
                if (phoneNumber.isNotEmpty()) updates["phoneNumber"] = phoneNumber
                if (kakaoId.isNotEmpty()) updates["kakaoId"] = kakaoId
                if (age.isNotEmpty()) updates["age"] = age
                if (mbti.isNotEmpty()) updates["mbti"] = mbti
                if (car.isNotEmpty()) updates["car"] = car

                // 업데이트할 정보가 있는 경우만 Firebase에 반영
                if (updates.isNotEmpty()) {
                    Log.d(TAG, "업데이트할 정보: $updates")
                    updateKakaoUserInfo(updates)
                } else {
                    Toast.makeText(this@EditInfoActivity, "변경할 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "변경할 정보가 없음")
                }
            }

            // 뒤로 가기 버튼
            btnEditInfoBack.setOnClickListener {
                Log.d(TAG, "뒤로 가기 버튼 클릭")
                onBackPressed()
            }
        }
    }

    private fun updateKakaoUserInfo(updates: Map<String, Any>) {
        // FirebaseAuth를 통해 현재 로그인된 사용자의 이메일을 가져옴
        val email = UserInfo.email
        if (email.isNullOrEmpty()) {
            Log.e(TAG, "이메일 정보 없음")
            return
        }
        // Firebase에서 이메일을 키로 사용하기 위해 '.'를 ','로 변환
        val emailKey = email.replace(".", ",")
        Log.d(TAG, "이메일 키: $emailKey")

        // Firebase Database에 사용자 정보 업데이트
        FBRef.kakaoUserRef.child(emailKey).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "정보가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "정보가 성공적으로 변경되었습니다.")
                finish() // 정보 변경 후 화면 종료
            }
            .addOnFailureListener {
                Toast.makeText(this, "정보 변경에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "정보 변경에 실패했습니다.", it)
            }
    }

    // MBTI 다이얼로그
    private fun showMBTIDialog() {
        val mbtiOptions = arrayOf(
            "ISTJ", "ISFJ", "INFJ", "INTJ",
            "ISTP", "ISFP", "INFP", "INTP",
            "ESTP", "ESFP", "ENFP", "ENTP",
            "ESTJ", "ESFJ", "ENFJ", "ENTJ"
        )
        AlertDialog.Builder(this)
            .setTitle("MBTI 선택")
            .setItems(mbtiOptions) { _, which ->
                val selectedMBTI = mbtiOptions[which]
                findViewById<TextView>(R.id.newMBTI).text = selectedMBTI
                Log.d(TAG, "선택된 MBTI: $selectedMBTI")
            }
            .show()
    }

    // 생년월일 달력
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val birthDate = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            binding.newAge.text = "${age}세"
            Log.d(TAG, "선택된 생년월일: $selectedYear-${selectedMonth + 1}-$selectedDay, 계산된 나이: $age")
        }, year, month, day).show()
    }

    // Firebase에서 닉네임 중복 확인
    private fun checkNicknameInFirebase(nickname: String, callback: (Boolean) -> Unit) {
        FBRef.kakaoUserRef.get().addOnSuccessListener { dataSnapshot ->
            var isDuplicate = false
            for (snapshot in dataSnapshot.children) {
                val userNick = snapshot.child("nickname").getValue(String::class.java)
                if (userNick == nickname) {
                    isDuplicate = true
                    break
                }
            }
            callback(isDuplicate)
            Log.d(TAG, "닉네임 중복 확인 결과: $isDuplicate")
        }.addOnFailureListener {
            Log.e(TAG, "닉네임 중복 확인 실패", it)
            callback(false)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        Log.d(TAG, "키보드 숨기기")
    }
}
