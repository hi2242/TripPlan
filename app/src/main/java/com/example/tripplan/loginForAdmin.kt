package com.example.tripplan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.activity.HomeActivity
import com.example.tripplan.activity.RegisterActivity
import com.example.tripplan.databinding.ActivityAdminBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class  loginForAdmin : AppCompatActivity() {
    // 관리자 로그인 페이지
    private lateinit var adminBinding: ActivityAdminBinding

    private lateinit var mAuth: FirebaseAuth
//    private lateinit var DB: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 관리자 로그인 페이지
        adminBinding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(adminBinding.root)

        mAuth = FirebaseAuth.getInstance()
//        DB = DBHelper(this)

        // 인증 초기화
        mAuth = Firebase.auth

        with(adminBinding) {
            // 로그인 버튼 클릭
            btnLogin!!.setOnClickListener {
                val user = editTextId!!.text.toString().trim()
                val pass = editTextPassword!!.text.toString().trim()

                // 빈칸 제출시 Toast
                if (user == "" || pass == "") {
                    Toast.makeText(this@loginForAdmin, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    login(user, pass)
                }
            }
            // 회원가입 버튼 클릭시
            btnRegister.setOnClickListener {
                val loginIntent = Intent(this@loginForAdmin, RegisterActivity::class.java)
                startActivity(loginIntent)
            }
        }
    }

    private fun login(email: String, password: String) {
        // 로딩 다이얼로그 생성
        val loadingDialog = LoadingDialog(this)

        // 로딩 다이얼로그 보여주기
        loadingDialog.show()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    user?.let {
                        // 권한 검사 시작
                        FBAuth.isUserAuthorized(it.uid) { isAuthorized ->
                            if (isAuthorized) {
                                Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish() // MainActivity를 종료하여 뒤로 가기 시 돌아오지 않도록 함
                            } else {
                                Toast.makeText(this, "권한이 없습니다. 관리자에게 문의하세요.", Toast.LENGTH_SHORT).show()
                            }
                            // 권한 검사 후 로딩 다이얼로그 닫기
                            loadingDialog.dismiss()
                        }
                    }
                } else {
                    // 로그인 실패 시 로딩 다이얼로그 닫기
                    loadingDialog.dismiss()
                    // 실패시 실행
                    Toast.makeText(this@loginForAdmin, "아이디와 비밀번호를 확인해 주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}