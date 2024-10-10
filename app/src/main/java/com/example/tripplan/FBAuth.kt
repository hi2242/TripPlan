package com.example.tripplan

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FBAuth {

    companion object{

        private lateinit var auth : FirebaseAuth

        fun getUid() : String{

            auth = FirebaseAuth.getInstance()

            //현재 사용자의 uid값 리턴
            return auth.currentUser?.uid.toString()
        }

        // 시간가져오는 함수
        fun getTime() : String{

            val currentDateTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)

            return dateFormat
        }

        // 닉네임 가져오는 함수
        fun getUserNickname(callback: (String) -> Unit) {
            auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid.toString()
            val ref = FirebaseDatabase.getInstance().getReference("user").child(uid)

            ref.child("nick").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nickname = dataSnapshot.getValue(String::class.java) ?: "Unknown"
                    callback(nickname)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 발생 시 기본 닉네임 설정
                    callback("Unknown")
                }
            })
        }

        // 사용자 권한 확인
        fun isUserAuthorized(userId: String, callback: (Boolean) -> Unit) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("user").child(userId).child("authorized").get().addOnSuccessListener { snapshot ->
                callback(snapshot.getValue(Boolean::class.java) == true)
            }.addOnFailureListener {
                callback(false)
            }
        }
    }
}