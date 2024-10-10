package com.example.tripplan.board

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.tripplan.activity.SplashHomeActivity
import com.example.tripplan.FBAuth
import com.example.tripplan.FBRef
import com.example.tripplan.R
import com.example.tripplan.databinding.ActivityBoardWriteBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class BoardWriteActivity : AppCompatActivity() {
    private lateinit var binding : ActivityBoardWriteBinding
    private val TAG = BoardWriteActivity::class.java.simpleName
    val storage = Firebase.storage
    private var isImageUploade = false
    private var key: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_write)

        binding.writeBtn.setOnClickListener {

            // titleArea,contentArea,urlArea 값을 받아옴
            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val url = binding.urlArea.text.toString()
            // uid값 가져오기
            val uid = FBAuth.getUid()
            // tiem값 가져오기
            val time = FBAuth.getTime()
            val timestamp = System.currentTimeMillis() // 현재 시간
            // 닉네임값 가져오기

            // 비동기로 닉네임 가져오기
            FBAuth.getUserNickname { nickname ->
                // 닉네임을 가져온 후에 게시글 저장 처리
                Log.d(TAG, title)
                Log.d(TAG, content)
                Log.d(TAG, url)
                Log.d(TAG, "닉네임: $nickname")

                // 키값 받아오기 (키값 알아내기)
                key = FBRef.boardRef.push().key.toString()

                // 데이터 집어넣기 (닉네임 포함)
                FBRef.boardRef
                    .child(key)
                    .setValue(BoardModel(title, content, uid, time, url, timestamp, nickname))

                Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_SHORT).show()

                if (isImageUploade) {
                    // 이미지 업로드 (키값을 기준으로)
                    imageUpload(key)
                }

                // 엑티비티 종료
                finish()
            }
        }
        // imageArea 클릭시
        binding.imageArea.setOnClickListener {

            // 갤러리로 이동
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

            isImageUploade = true

        }
    }

    // 이미지 업로드 함수
    private fun imageUpload(key :String){
        val storageRef = storage.reference
        val fileRef = storageRef.child("board_images/$key.jpg")

        val imageView = binding.imageArea

        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // 이미지 업로드
        val uploadTask = fileRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // 업로드 실패 처리
            Log.e(TAG, "Image upload failed", it)
        }.addOnSuccessListener {
            // 업로드 성공시 다운로드 URL 가져오기
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // 이미지 다운로드 URL을 Firebase Realtime Database에 저장
                FBRef.boardRef.child(key).child("thumbnailImageUrl").setValue(uri.toString())
                    .addOnSuccessListener {
                        Log.d(TAG, "Thumbnail URL saved successfully")
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Failed to save thumbnail URL", e)
                    }
            }.addOnFailureListener { e ->
                // 다운로드 URL 가져오기 실패 처리
                Log.e(TAG, "Failed to retrieve download URL", e)
            }
        }

    }

//    private fun showLoadingScreen(targetActivity: String) {
//        val intent = Intent(this, SplashHomeActivity::class.java).apply {
//            putExtra("TARGET_ACTIVITY", targetActivity)
//        }
//        startActivity(intent)
//        finish()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 갤러리에서 데이터 받아오기
        if (resultCode == RESULT_OK && requestCode == 100) {

            // 받아온 데이터를 레이아웃에 표시(갤러리 데이터가 imageArea에 표시)
            binding.imageArea.setImageURI(data?.data)
        }
    }
    // 게시글 썸네일 업로드
    private fun uploadImageToFirebase(imageUri: Uri?) {
        val storageRef = storage.reference
        val fileRef = storageRef.child("board_images/$key.jpg")

        val uploadTask = fileRef.putFile(imageUri!!)
        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // 이미지 다운로드 URL을 Firebase Realtime Database에 저장
                FBRef.boardRef.child(key).child("thumbnailImageUrl").setValue(uri.toString())
            }
        }.addOnFailureListener {
            // 업로드 실패 처리
        }
    }

}