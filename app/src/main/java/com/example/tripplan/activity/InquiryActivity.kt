package com.example.tripplan.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.FBAuth
import com.example.tripplan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class InquiryActivity : AppCompatActivity() {

    private lateinit var mDbRef: DatabaseReference // Firebase Database 참조 객체
    private lateinit var mAuth: FirebaseAuth // Firebase 인증 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inquiry)

        // Firebase 인증 및 데이터베이스 초기화
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        // 문의 제목과 내용 입력 필드
        val inquiryTitle = findViewById<EditText>(R.id.inquiryTitle)
        val inquiryContent = findViewById<EditText>(R.id.inquiryContent)

        // 제출 버튼 클릭 리스너
        findViewById<Button>(R.id.btnSubmitInquiry).setOnClickListener {
            val title = inquiryTitle.text.toString().trim()
            val content = inquiryContent.text.toString().trim()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                submitInquiry(title, content)
            } else {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 뒤로 가기 버튼
        findViewById<ImageButton>(R.id.btnInquireBack).setOnClickListener {
            onBackPressed()
        }
    }

    private fun submitInquiry(title: String, content: String) {
        val userId = mAuth.currentUser?.uid ?: return
        val inquiryId = mDbRef.child("inquiries").push().key ?: return
        val inquiryData = mapOf(
            "userId" to userId,
            "title" to title,
            "content" to content,
            "time" to FBAuth.getTime()
        )

        // Firebase Database에 문의 내용 저장
        mDbRef.child("inquiries").child(inquiryId).setValue(inquiryData)
            .addOnSuccessListener {
                Toast.makeText(this, "문의가 성공적으로 제출되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 문의 제출 후 화면 종료
            }
            .addOnFailureListener {
                Toast.makeText(this, "문의 제출에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
    }
}
