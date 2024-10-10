package com.example.tripplan.activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.FBRef
import com.example.tripplan.KaKaoAuthViewModel.Companion.TAG
import com.example.tripplan.R
import com.example.tripplan.UserInfo
import com.example.tripplan.databinding.ActivityFirstUserInfoBinding
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import java.util.regex.Pattern

class FirstUserInfoActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_FIRST_USER_INFO = 1001 // 고유한 정수 값으로 설정
    }
    private lateinit var binding:ActivityFirstUserInfoBinding
    private val REQUEST_CODE_SELECT_IMAGE = 1001
    private var profileImageUrl: String? = null
    var CheckNick:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "FirstUserInfoActivity 시작")

        with(binding) {
            etAge.setOnClickListener {
                hideKeyboard()
                showDatePickerDialog()
            }
            // 프로필 사진 업로드
            etProfileImage.setOnClickListener {
                selectProfileImage()
            }
            // 닉네임 중복확인
            btnCheckNickReg.setOnClickListener {
                val nick = etNickname.text.toString()
                val nickPattern = "^[ㄱ-ㅣ가-힣]*$"

                if (nick == "") {
                    Toast.makeText(
                        this@FirstUserInfoActivity,
                        "닉네임을 입력해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    if (Pattern.matches(nickPattern, nick)) {
                        checkNicknameInFirebase(nick) { isDuplicate ->
                            if (!isDuplicate) {
                                CheckNick = true
                                Toast.makeText(this@FirstUserInfoActivity, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@FirstUserInfoActivity, "이미 존재하는 닉네임입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@FirstUserInfoActivity, "닉네임 형식이 옳지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            etGender.setOnClickListener {
                showGenderDialog()
                hideKeyboard()
            }

            etMBTI.setOnClickListener {
                showMBTIDialog()
                hideKeyboard()
            }
            btnPrivateInfo.setOnClickListener {
                btnPrivateInfo.setOnClickListener {
                    val intent = Intent(applicationContext, PrivateInformationActivity::class.java)
                    intent.putExtra("key", "FirstUserInfoActivity") // key값 넘겨줌
                    startActivity(intent)
                }
            }
            btnPrivateInfo.setOnClickListener {
                // RegisterActivity에서 PrivateInformationActivity를 호출
                val intent = Intent(this@FirstUserInfoActivity, PrivateInformationActivity::class.java)
                intent.putExtra("key", "FirstUserInfoActivity")
                startActivityForResult(intent, REQUEST_CODE_FIRST_USER_INFO)
            }

            btnSubmit.setOnClickListener {
                val name = etName.text.toString()
                val age = etAge.text.toString()
                val phone = etPhone.text.toString()
                val nickname = etNickname.text.toString()
                val gender = etGender.text.toString()
                val kakaoId = etKakaoId.text.toString()
                val mbti = etMBTI.text.toString()
                val car = etCar.findViewById<RadioButton>(etCar.checkedRadioButtonId)?.text.toString()
                val private = btnPrivateInfo.text.toString().trim()

                Log.d(TAG, "유저 정보 입력: name=$name, age=$age, phone=$phone, nickname=$nickname, " +
                        "gender=$gender, kakaoId=$kakaoId, mbti=$mbti, car=$car")

                if (name.isEmpty() || age.isEmpty() || phone.isEmpty() || nickname.isEmpty() ||
                    gender.isEmpty() || kakaoId.isEmpty() || mbti.isEmpty() || car.isEmpty() ||
                    profileImageUrl == null || mbti == "나의 MBTI" || car == "null") {
                    Log.w(TAG, "유저 정보가 불완전함, 저장 중지")
                    Toast.makeText(this@FirstUserInfoActivity, "모든 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else if (private == "동의하지 않습니다." || private == "개인정보 수집, 이용 안내") {
                    Toast.makeText(this@FirstUserInfoActivity, "개인 정보 수집에 동의해 주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                saveUserInfoToFirebase(name, age, phone, nickname, gender, kakaoId, mbti, car, profileImageUrl)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                binding.etProfileImage.setImageURI(uri)
                uploadProfileImage(uri)
            }
        }
        if (requestCode == REQUEST_CODE_FIRST_USER_INFO && resultCode == RESULT_OK) {
            val agreed = data?.getBooleanExtra("agreed", false) ?: false
            if (agreed) {
                binding.btnPrivateInfo.text = "동의합니다."
            } else {
                binding.btnPrivateInfo.text = "동의하지 않습니다."
            }
        }
    }

    private fun selectProfileImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    // 카카오유저 이미지 업로드
    private fun uploadProfileImage(imageUri: Uri) {
        val email = UserInfo.email ?: return
        val emailKey = email.replace(".", ",")
        val storageReference = FirebaseStorage.getInstance().reference.child("kakao_profile_images/$emailKey.jpg")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "프로필 사진 URI 가져오기 성공: $uri")
                    profileImageUrl = uri.toString() // 프로필 이미지 URL 임시 저장
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "프로필 사진 URI 가져오기 실패", exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "프로필 사진 업로드 실패", exception)
            }
    }

    // 유저 정보 파이어베이스 저장
    private fun saveUserInfoToFirebase(
        name: String,age: String,phone: String,nickname: String,gender: String,
        kakaoId: String,mbti: String,car: String,profileImageUrl: String?
    ) {
        val email = UserInfo.email
        if (email.isNullOrEmpty()) {
            Log.e(TAG, "이메일 정보 없음")
            Toast.makeText(this, "이메일이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val emailKey = email.replace(".", ",")

        val userInfo = mapOf(
            "name" to name,
            "age" to age,
            "phone" to phone,
            "nickname" to nickname,
            "gender" to gender,
            "kakaoId" to kakaoId,
            "mbti" to mbti,
            "car" to car,
            "profileImageUrl" to profileImageUrl
        )

        Log.d(TAG, "유저 정보 Firebase에 저장 시도: $userInfo")
        FBRef.kakaoUserRef.child(emailKey).setValue(userInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "유저 정보 Firebase에 저장 성공")
                Toast.makeText(this, "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Log.e(TAG, "유저 정보 Firebase에 저장 실패", task.exception)
                Toast.makeText(this, "정보 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 성별 다이얼로그
    private fun showGenderDialog() {
        val genderOptions = arrayOf("남자", "여자")
        AlertDialog.Builder(this)
            .setTitle("성별 선택")
            .setItems(genderOptions) { _, which ->
                val selectedGender = genderOptions[which]
                findViewById<TextView>(R.id.etGender).text = selectedGender
            }
            .show()
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
                findViewById<TextView>(R.id.etMBTI).text = selectedMBTI
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
            binding.etAge.text = "${age}세"
        }, year, month, day).show()
    }
    // 키보드 숨기기
    private fun hideKeyboard() {
        // 현재 포커스가 있는 뷰 가져오기
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
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
        }.addOnFailureListener {
            Log.e(TAG, "닉네임 중복 확인 실패", it)
            callback(false)
        }
    }
}
