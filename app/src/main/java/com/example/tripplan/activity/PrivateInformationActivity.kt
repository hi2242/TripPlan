package com.example.tripplan.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.GuestMatchingFragment
import com.example.tripplan.databinding.ActivityRegisterBinding
import com.example.tripplan.databinding.PrivateInformationBinding
import com.example.tripplan.matematching.MateMatchingFragment

class PrivateInformationActivity : AppCompatActivity() {
    private lateinit var binding: PrivateInformationBinding
    private lateinit var key : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PrivateInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        key = intent.getStringExtra("key").toString()
        with(binding) {
            if (key == "RegisterActivity") {
                detailInfo.text = "여정은 서비스 회원가입, 고지사항 전달 등을 위해 아래와 같이 개인정보를 수집, 이용합니다.\n\n\n" +
                        "- 아이디, 비밀번호 (수집 목적: 기관 식별 및 기관 서비스 제공, 보유 기간: 기관 회원 탈퇴 시 까지)\n\n" +
                        "- 전화번호 (수집 목적: 고지사항 전달, 보유 기간: 기관 회원 탈퇴 시 까지)\n\n\n" +
                        "*귀하는 여정의 서비스 이용에 필요한 최소한의 개인정보 수집, 이용에 동의하지 않을 수 있으나, 동의를 거부 할 경우 서비스 이용이 불가합니다."

            }
            else if (key == "FirstUserInfoActivity") {
                detailInfo.text = "여정은 서비스 회원가입, 고지사항 전달 등을 위해 아래와 같이 개인정보를 수집, 이용합니다.\n\n\n" +
                        "- 이름, 성별, 나이, 카카오톡 ID, 사는 지역 (수집 목적: 회원 식별 및 서비스 제공, 보유 기간: 회원 탈퇴 시 까지)\n\n" +
                        "- 범죄 경력 회보서 (수집 목적: 회원의 범죄 이력 조회, 보유 기간: 회원 탈퇴 시 까지)\n\n\n" +
                        "*귀하는 여정의 서비스 이용에 필요한 최소한의 개인정보 수집, 이용에 동의하지 않을 수 있으나, 동의를 거부 할 경우 서비스 이용이 불가합니다."
            }
            else if (key == "SubmitGuestActivity") {
                detailInfo.text = "여정은 매칭 서비스 이용, 고지사항 전달 등을 위해 아래와 같이 개인정보를 수집, 이용합니다.\n\n\n" +
                        "- 이름, 성별, 나이, 카카오톡 ID, 사는 지역 (수집 목적: 회원 식별 및 서비스 제공, 보유 기간: 회원 탈퇴 시 까지)\n\n" +
                        "- 범죄 경력 회보서 (수집 목적: 회원의 범죄 이력 조회, 보유 기간: 회원 탈퇴 시 까지)\n\n\n" +
                        "*귀하는 여정의 서비스 이용에 필요한 최소한의 개인정보 수집, 이용에 동의하지 않을 수 있으나, 동의를 거부 할 경우 서비스 이용이 불가합니다."

            }
            else if (key == "MateMatchingFragment") {
                detailInfo.text = "여정은 매칭 서비스 이용, 고지사항 전달 등을 위해 아래와 같이 개인정보를 수집, 이용합니다.\n\n\n" +
                        "- 이름, 성별, 나이, 카카오톡 ID, 사는 지역 (수집 목적: 회원 식별 및 서비스 제공, 보유 기간: 회원 탈퇴 시 까지)\n\n" +
                        "- 범죄 경력 회보서 (수집 목적: 회원의 범죄 이력 조회, 보유 기간: 회원 탈퇴 시 까지)\n\n\n" +
                        "*귀하는 여정의 서비스 이용에 필요한 최소한의 개인정보 수집, 이용에 동의하지 않을 수 있으나, 동의를 거부 할 경우 서비스 이용이 불가합니다."

            }
            else if (key == "GuestMatchingFragment") {
                detailInfo.text = "여정은 매칭 서비스 이용, 고지사항 전달 등을 위해 아래와 같이 개인정보를 수집, 이용합니다.\n\n\n" +
                        "- 이름, 성별, 나이, 카카오톡 ID, 사는 지역 (수집 목적: 회원 식별 및 서비스 제공, 보유 기간: 회원 탈퇴 시 까지)\n\n" +
                        "- 범죄 경력 회보서 (수집 목적: 회원의 범죄 이력 조회, 보유 기간: 회원 탈퇴 시 까지)\n\n\n" +
                        "*귀하는 여정의 서비스 이용에 필요한 최소한의 개인정보 수집, 이용에 동의하지 않을 수 있으나, 동의를 거부 할 경우 서비스 이용이 불가합니다."
            }
            btnAgree.setOnClickListener {
                val intent = Intent()
                intent.putExtra("agreed", true)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            btnDisagree.setOnClickListener {
                val intent = Intent()
                intent.putExtra("agreed", false)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

    }
}