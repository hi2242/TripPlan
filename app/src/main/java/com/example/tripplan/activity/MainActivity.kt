package com.example.tripplan.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.tripplan.FBRef
import com.example.tripplan.KaKaoAuthViewModel
import com.example.tripplan.KaKaoAuthViewModel.Companion.TAG
import com.example.tripplan.LoadingDialog
import com.example.tripplan.databinding.ActivityMainBinding
import com.example.tripplan.loginForAdmin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class MainActivity : AppCompatActivity() {
    // 카카오 로그인 페이지
    private val kakaoAuthViewModel: KaKaoAuthViewModel by viewModels()
    private lateinit var mainBinding: ActivityMainBinding

//    override fun onResume() {
//        super.onResume()
//
//        // 카카오 로그인 세션 확인
//        kakaoAuthViewModel.checkKakaoLoginStatus()
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        // 카카오 로그인 페이지
        super.onCreate(savedInstanceState)
//        Log.d("KeyHash", getKeyHash(this@MainActivity) ?: "Key hash not found")
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        enableEdgeToEdge()

        with(mainBinding) {
            loginButtonForKakao.setOnClickListener {
                kakaoAuthViewModel.handleKaKaoLogin(this@MainActivity)
            }

            // 성공 이벤트 관찰
            kakaoAuthViewModel.loginSuccessEvent.observe(this@MainActivity, Observer { email ->
                email?.let { saveEmailToFirebase(it) }
            })
            val intent = Intent(this@MainActivity, loginForAdmin::class.java)
            loginButtonForAdmin.setOnClickListener {
                startActivity(intent)
                finish() // MainActivity를 종료하여 뒤로 가기 시 돌아오지 않도록 함
            }
        }
    }

    private fun saveEmailToFirebase(email: String) {
        val kakaoUserRef = FBRef.kakaoUserRef
        val dialog = LoadingDialog(this@MainActivity)

        // 로딩 다이얼로그 시작
        dialog.show()
        // Firebase에서 이메일 존재 여부 확인
        val emailKey = email.replace(".", ",")
        kakaoUserRef.child(emailKey).get().addOnSuccessListener { dataSnapshot ->
            if (!dataSnapshot.exists()) {
                // 이메일이 존재하지 않으면 새로운 사용자로 처리
                Log.d(TAG, "New email, saving to Firebase: $email")
                // Firebase에 이메일을 키로 사용하여 빈 데이터를 설정
                kakaoUserRef.child(emailKey).setValue(mapOf("placeholder" to "value")).addOnCompleteListener { task ->
                    dialog.dismiss() // 작업 완료 후 로딩 다이얼로그 닫기
                    if (task.isSuccessful) {
                        // 정보 저장 성공
                        val intent = Intent(this@MainActivity, FirstUserInfoActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 Activity 종료
                    } else {
                        // 정보 저장 실패
                        Toast.makeText(this@MainActivity, "정보 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // 이메일이 이미 존재하는 경우
                Log.d(TAG, "Email exists, navigating to HomeActivity")
                dialog.dismiss() // 작업 완료 후 로딩 다이얼로그 닫기
                Toast.makeText(this@MainActivity, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
                finish() // 현재 Activity 종료
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to check email existence", it)
        }
    }



//    fun getKeyHash(context: Context): String? {
//        try {
//            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
//            for (signature in packageInfo.signatures) {
//                val md: MessageDigest = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
//            }
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//        } catch (e: NoSuchAlgorithmException) {
//            e.printStackTrace()
//        }
//        return null
//    }

}