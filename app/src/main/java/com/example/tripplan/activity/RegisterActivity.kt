package com.example.tripplan.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.tripplan.FBRef
//import com.example.tripplan.DBHelper
import java.util.regex.Pattern
import com.example.tripplan.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class RegisterActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_PRIVATE_INFO = 1001 // 고유한 정수 값으로 설정
    }
//    var DB: DBHelper?=null
    private lateinit var binding: ActivityRegisterBinding

    var CheckId:Boolean=false
    var CheckNick:Boolean=false
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        DB = DBHelper(this)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인증 초기화
        mAuth = Firebase.auth

//        // 데이터베이스 초기화
//        mDbRef = Firebase.database.reference

        with(binding) {
            btnCheckIdReg.setOnClickListener {
                val userEmail = editTextIdReg.text.toString()
                // 이메일 형식 패턴
                val idPattern = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$"

                if (userEmail.isEmpty()) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "아이디를 입력해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (Pattern.matches(idPattern, userEmail)) {
                        // Firebase에서 이메일로 중복 체크
                        checkIdAvailability(userEmail)
                    } else {
                        Toast.makeText(this@RegisterActivity, "이메일 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
//            // 아이디 중복확인
//            btnCheckIdReg.setOnClickListener {
//                val user = editTextIdReg.text.toString()
//                //이메일 형식 패턴
//                val idPattern = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$"
//
//                if (user == "") {
//                    Toast.makeText(
//                        this@RegisterActivity,
//                        "아이디를 입력해주세요.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }

//                else {
//                    if (Pattern.matches(idPattern, user)) {
//                        val checkUsername = DB!!.checkUser(user)
//                        if(checkUsername == false){
//                            CheckId = true
//                            Toast.makeText(this@RegisterActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
//                        }
//                        else {
//                            Toast.makeText(this@RegisterActivity, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    else {
//                        Toast.makeText(this@RegisterActivity, "이메일 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }

            // 닉네임 중복확인
            btnCheckNickReg.setOnClickListener {
                val nick = editTextNickReg.text.toString()
                if (nick.isEmpty()) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "닉네임을 입력해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    checkNicknameAvailability(nick)
                }
//            // 닉네임 중복확인
//            btnCheckNickReg.setOnClickListener {
//                val nick = editTextNickReg.text.toString()
//                val nickPattern = "^[ㄱ-ㅣ가-힣]*$"
//
//                if (nick == "") {
//                    Toast.makeText(
//                        this@RegisterActivity,
//                        "닉네임을 입력해주세요.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                else {
//                    if (Pattern.matches(nickPattern, nick)) {
//                        val checkNick = DB!!.checkNick(nick)
//                        if(checkNick == false){
//                            CheckNick = true
//                            Toast.makeText(this@RegisterActivity, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
//                        }
//                        else {
//                            Toast.makeText(this@RegisterActivity, "이미 존재하는 닉네임입니다.", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    else {
//                        Toast.makeText(this@RegisterActivity, "닉네임 형식이 옳지 않습니다.", Toast.LENGTH_SHORT).show()
//                    }
//                }
            }


            btnPrivateInfo.setOnClickListener {
                // RegisterActivity에서 PrivateInformationActivity를 호출
                val intent = Intent(this@RegisterActivity, PrivateInformationActivity::class.java)
                intent.putExtra("key", "RegisterActivity")
                startActivityForResult(intent, REQUEST_CODE_PRIVATE_INFO)
            }


            // 회원가입 버튼 클릭 시
            btnRegisterReg.setOnClickListener {
                val user = editTextIdReg.text.toString().trim()
                val pass = editTextPassReg.text.toString().trim()
                val repass = editTextRePassReg.text.toString().trim()
                val nick = editTextNickReg.text.toString().trim()
                val phone = editTextPhoneReg.text.toString().trim()
                val explain = editTextPhoneExp.text.toString().trim()
                val agree = btnPrivateInfo.text.toString().trim()
                val pwPattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z[0-9]]{8,15}$"
                val phonePattern = "^(\\+[0-9]+)?[0-9]{10,15}$"
                val private = btnPrivateInfo.text.toString().trim()

                if (user.isEmpty() || pass.isEmpty() || repass.isEmpty() || nick.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this@RegisterActivity, "회원정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (private == "동의하지 않습니다." || private == "개인정보 수집, 이용 안내") {
                    Toast.makeText(this@RegisterActivity, "개인 정보 수집에 동의해 주세요.", Toast.LENGTH_SHORT).show()
                } else if (pass != repass) {
                    Toast.makeText(this@RegisterActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                } else if (!CheckNick) {
                    Toast.makeText(this@RegisterActivity, "닉네임 중복확인을 해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    signUp(nick, user, pass, phone, explain)
                }
            }

        }
    }
    // 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PRIVATE_INFO && resultCode == RESULT_OK) {
            val agreed = data?.getBooleanExtra("agreed", false) ?: false
            if (agreed) {
                binding.btnPrivateInfo.text = "동의합니다."
            } else {
                binding.btnPrivateInfo.text = "동의하지 않습니다."
            }
        }
    }
    // 파이어베이스 회원가입 기능
    private fun signUp(nick: String, email: String, password: String, phone: String, explain: String) {

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 성공 시 데이터베이스에 사용자 추가
                    val uid = mAuth.currentUser?.uid ?: ""
                    addUserToDatabase(nick, email, phone, explain, uid)
                    Toast.makeText(this@RegisterActivity, "가입되었습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                } else {
                    Toast.makeText(this@RegisterActivity, "가입 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDatabase(nick: String, email: String, phone: String, explain: String, uId: String) {
        FBRef.userRef.child(uId).setValue(
            com.example.tripplan.User(
                nick,
                email,
                phone,
                explain,
                uId
            )
        )
    }
    // 아이디 중복 확인
    private fun checkIdAvailability(email: String) {
        FBRef.userRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegisterActivity, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
                        CheckId = false
                    } else {
                        Toast.makeText(this@RegisterActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                        CheckId = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "닉네임 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }
    // 닉네임 중복 확인
    private fun checkNicknameAvailability(nick: String) {
        FBRef.userRef.orderByChild("nick").equalTo(nick)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegisterActivity, "이미 존재하는 닉네임입니다.", Toast.LENGTH_SHORT).show()
                        CheckNick = false
                    } else {
                        Toast.makeText(this@RegisterActivity, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
                        CheckNick = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "닉네임 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }

}