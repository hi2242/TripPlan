package com.example.tripplan.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tripplan.FBRef
import com.example.tripplan.KaKaoAuthViewModel.Companion.TAG
import com.example.tripplan.activity.MainActivity
import com.example.tripplan.R
import com.example.tripplan.User
import com.example.tripplan.UserInfo
import com.example.tripplan.activity.EditInfoActivity
import com.example.tripplan.activity.InquiryActivity
import com.example.tripplan.databinding.FragmentInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Info : Fragment() {
    private lateinit var binding: FragmentInfoBinding
    private lateinit var mDbRef: DatabaseReference //Firebase 데이터베이스 참조 객체
    private lateinit var mAuth: FirebaseAuth //Firebase 인증 객체

    private lateinit var userEmailTextView: TextView
    private lateinit var userNickTextView: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(inflater, container, false)

        return binding.root

        }

    private val PICK_IMAGE_REQUEST = 71
    private lateinit var storageRef: StorageReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Firebase 인증 초기화
        mAuth = FirebaseAuth.getInstance()
        // Firebase Realtime Database 초기화
        mDbRef = FirebaseDatabase.getInstance().reference
        // Firebase Storage 초기화
        storageRef = FirebaseStorage.getInstance().reference

        // 프로필 이미지 클릭 리스너
        binding.profileImage.setOnClickListener {
            pickImageFromGallery()
        }
        // 개인정보 수정
        view.findViewById<Button>(R.id.btnEditInfo).setOnClickListener {
            val intent = Intent(requireContext(), EditInfoActivity::class.java)
            startActivity(intent)
        }
        // 로그아웃
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
        // 문의하기 진입
        view.findViewById<Button>(R.id.btnInquire).setOnClickListener {
            val intent = Intent(requireContext(), InquiryActivity::class.java)
            startActivity(intent)
        }

        // 회원 탈퇴
        view.findViewById<Button>(R.id.btnWithdraw).setOnClickListener {
            confirmAccountDeletion()
        }

        userNickTextView = view.findViewById(R.id.userNick)
        userEmailTextView = view.findViewById(R.id.userEmail)

        isKakaoUser { isKakao ->
            if (isKakao) {
                binding.btnLogout.visibility = View.GONE
                binding.middleLine.visibility = View.GONE
                binding.btnWithdraw.visibility = View.GONE
                getKakaoUserInfoFromFirebase()
            }
            else {
                loadOrganData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 정보 수정 후 실시간 업데이트
        isKakaoUser { isKakao ->
            if (isKakao) {
                getKakaoUserInfoFromFirebase()
            }
            else {
                loadOrganData()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val filePath = data.data
            isKakaoUser { isKakao ->
                if (isKakao) {
                    uploadKakaoUserImageToFirebase(filePath)
                } else {
                    uploadImageToFirebase(filePath)
                }
            }
        }
    }


    // 기관 프로필 사진 업로드
    private fun uploadImageToFirebase(imageUri: Uri?) {
        val userId = mAuth.currentUser?.uid ?: return
        val fileRef = storageRef.child("profile_images/$userId.jpg")

        val uploadTask = fileRef.putFile(imageUri!!)
        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // 이미지 다운로드 URL을 Firebase Realtime Database에 저장
                FBRef.userRef.child(userId).child("profileImageUrl").setValue(uri.toString()).addOnCompleteListener {
                    // 업로드 후 프로필 이미지 즉시 로드
                    Glide.with(this)
                        .load(uri.toString())
                        .circleCrop() // 동그란 이미지로 자르기
                        .into(binding.profileImage)

                    Log.d(TAG, "프로필 이미지 즉시 반영됨: $uri")
                }
            }
        }.addOnFailureListener {
            // 업로드 실패 처리
        }
    }

    // 카카오 유저 프로필 사진 업로드
    private fun uploadKakaoUserImageToFirebase(imageUri: Uri?) {
        val email = UserInfo.email ?: return
        val emailKey = email.replace(".", ",")
        val fileRef = storageRef.child("kakao_profile_images/$emailKey.jpg")

        val uploadTask = fileRef.putFile(imageUri!!)
        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // 이미지 다운로드 URL을 Firebase Realtime Database에 저장
                FBRef.kakaoUserRef.child(emailKey).child("profileImageUrl").setValue(uri.toString()).addOnCompleteListener {
                    // 업로드 후 프로필 이미지 즉시 로드
                    Glide.with(this)
                        .load(uri.toString())
                        .circleCrop() // 동그란 이미지로 자르기
                        .into(binding.profileImage)

                    Log.d(TAG, "프로필 이미지 즉시 반영됨: $uri")
                }
            }.addOnFailureListener {
                Log.e(TAG, "프로필 이미지 URL 저장 실패")
            }
        }.addOnFailureListener {
            Log.e(TAG, "이미지 업로드 실패")
        }
    }
    // 카카오 유저 이메일/닉네임/프로필 사진 표시
    private fun getKakaoUserInfoFromFirebase() {
        val email = UserInfo.email
        if (email.isNullOrEmpty()) {
            Log.e(TAG, "이메일 정보 없음")
            return
        }
        val emailKey = email.replace(".", ",")

        FBRef.kakaoUserRef.child(emailKey).get().addOnSuccessListener { snapshot ->
            val userNick = snapshot.child("nickname").getValue(String::class.java)
            val userEmail = email.replace(",", ".")
            val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

            userEmailTextView.text = userEmail
            Log.d(TAG, "이메일: $userEmail")

            if (userNick != null) {
                userNickTextView.text = userNick
                Log.d(TAG, "닉네임: $userNick")
            } else {
                Log.w(TAG, "닉네임 정보 없음")
            }

            // 프로필 이미지 URL이 존재하면 이미지 로드
            if (profileImageUrl != null) {
                Glide.with(this)
                    .load(profileImageUrl)
                    .circleCrop() // 동그란 이미지로 자르기
                    .into(binding.profileImage)
                Log.d(TAG, "프로필 이미지 URL: $profileImageUrl")
            } else {
                Log.w(TAG, "프로필 이미지 URL 정보 없음")
            }
        }.addOnFailureListener {
            Log.e(TAG, "유저 정보 가져오기 실패", it)
        }
    }
    // 기관 아이디/닉네임/프로필 사진 표시
    private fun loadOrganData() {
        val userId = mAuth.currentUser?.uid ?: return
        FBRef.userRef.child(userId).get().addOnSuccessListener { snapshot ->
            val userNick = snapshot.child("nick").getValue(String::class.java)
            val userEmail = snapshot.child("email").getValue(String::class.java)
            val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

            userEmailTextView.text = userEmail
            Log.d(TAG, "이메일: $userEmail")

            if (userNick != null) {
                userNickTextView.text = userNick
                Log.d(TAG, "닉네임: $userNick")
            } else {
                Log.w(TAG, "닉네임 정보 없음")
            }

            // 프로필 이미지 URL이 존재하면 이미지 로드
            if (profileImageUrl != null) {
                Glide.with(this)
                    .load(profileImageUrl)
                    .circleCrop() // 동그란 이미지로 자르기
                    .into(binding.profileImage)
                Log.d(TAG, "프로필 이미지 URL: $profileImageUrl")
            } else {
                Log.w(TAG, "프로필 이미지 URL 정보 없음")
            }
        }.addOnFailureListener {
            Log.e(TAG, "유저 정보 가져오기 실패", it)
        }
//        val userId = mAuth.currentUser?.uid ?: return
//        FBRef.userRef.child(userId).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    val user = snapshot.getValue(User::class.java)
//                    if (user != null) {
//                        binding.userEmail.text = user.email
//                        binding.userNick.text = user.nick
//                        // 기관이면 '개인 정보 수정' 안 보이게
//                        binding.btnEditInfo.visibility = View.GONE
//                        binding.view.visibility = View.GONE
//                        user.profileImageUrl?.let { url ->
//                            Glide.with(this@Info)
//                                .load(url)
//                                .circleCrop() // 동그란 이미지로 자르기
//                                .into(binding.profileImage)
//                        }
//                    }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                // 데이터 로드 실패 처리
//            }
//        })
    }

    //로그아웃
    private fun logout() {
        // Create and show confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃하시겠습니까?")
            .setNegativeButton("확인") { dialog, which ->
                // Log out and navigate to MainActivity
                mAuth.signOut()
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            .setPositiveButton("취소", null)
            .show()
    }
    // 카카오 유저인지 확인
    private fun isKakaoUser(callback: (Boolean) -> Unit) {
        val email = UserInfo.email
        val emailKey = email?.replace(".", ",")

        // Firebase에서 카카오 유저로 저장되어 있는지 확인
        if (emailKey != null) {
            FBRef.kakaoUserRef.child(emailKey).get().addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()) {
                    // 카카오 유저로 존재하면 true 반환
                    callback(true)
                } else {
                    // 카카오 유저가 존재하지 않으면 false 반환
                    callback(false)
                }
            }.addOnFailureListener {
                Log.e(TAG, "유저 정보 확인 실패", it)
                callback(false)
            }
        } else {
            // 기관 유저라면
            callback(false)
        }
    }

    // 회원 탈퇴 다이얼로그
    private fun confirmAccountDeletion() {
        AlertDialog.Builder(requireContext())
            .setTitle("회원 탈퇴")
            .setMessage("정말 회원 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
            .setNegativeButton("확인") { dialog, which ->
                isKakaoUser { isKakao ->
                    if (isKakao) {
                        deleteKakaoAccount() // 카카오 유저 탈퇴
                    } else {
                        deleteOrganAccount() // 기관 유저 탈퇴
                    }
                }
            }
            .setPositiveButton("취소", null)
            .show()
    }

    // 기관 회원 탈퇴 함수
    private fun deleteOrganAccount() {
        val user = mAuth.currentUser ?: return
        val userId = user.uid

        // Delete user data from Firebase Realtime Database
        FBRef.userRef.child(userId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Delete user account from Firebase Authentication
                user.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        // Navigate to MainActivity after successful deletion
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        // Handle failure of user deletion
                    }
                }
            } else {
                // Handle failure of data removal
            }
        }
    }

    // 카카오 유저 탈퇴 함수
    private fun deleteKakaoAccount() {
        val email = UserInfo.email ?: return
        val emailKey = email.replace(".", ",")

        Log.d(TAG, "카카오 유저 데이터 삭제 시작: $emailKey")

        // 카카오 유저 데이터 삭제
        FBRef.kakaoUserRef.child(emailKey).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "카카오 유저 데이터 삭제 성공")
                // MainActivity로 이동
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Log.d(TAG, "MainActivity로 이동")
                requireActivity().finish()
            }
        }
    }
}