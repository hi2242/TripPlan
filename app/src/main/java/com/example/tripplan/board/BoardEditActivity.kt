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
import com.bumptech.glide.Glide
import com.example.tripplan.FBAuth
import com.example.tripplan.FBRef
import com.example.tripplan.R
import com.example.tripplan.databinding.ActivityBoardEditBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class BoardEditActivity : AppCompatActivity() {

    // key값 선언
    private lateinit var key : String

    private lateinit var binding : ActivityBoardEditBinding

    private val TAG = BoardEditActivity::class.java.simpleName

    // uid 가져오기위해 초기화?
    private lateinit var writeUid : String

    val storage = Firebase.storage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_edit)


        // BoardInsideActivity에서 보낸 key값 받아오기
        key = intent.getStringExtra("key").toString()


        // 게시글에서 쓴 데이터 가져와서 레아이웃에 표현하기
        getEditBoardData(key)
        // 게시글에서 쓴 이미지 가져와서 레이아웃에 표현하기
        getEditImageData(key)

        // editBtn버튼 눌렀을때
        binding.editBtn.setOnClickListener {
            editBoardData(key)
        }

        // imageArea 이미지 눌렀을때
        binding.imageArea.setOnClickListener {

            // 갤러리로 이동
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)

        }

    }

    // 수정버튼 눌렀을때 수정되는 함수
    private fun editBoardData(key : String){
        // 업데이트할 데이터 필드
        val updates = mapOf(
            "title" to binding.titleArea.text.toString(),       // 업데이트할 제목
            "content" to binding.contentArea.text.toString(),   // 업데이트할 내용
            "url" to binding.urlArea.text.toString(),           // 업데이트할 URL
        )
        // 데이터 집어넣기
        FBRef.boardRef
            .child(key)
            .updateChildren(updates)

        // 이미지 업로드
        imageUpload(key)

        Toast.makeText(this, "수정완료", Toast.LENGTH_SHORT).show()

        finish()  // 엑티비티 종료
    }

    // 이미지 다운로드 함수 (게시글에서 쓴 이미지 가져와서 레이아웃에 표현하기)
    private fun getEditImageData(key : String){

        // 이미지 다운로드

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".jpg")

        // ImageView in your Activity
        val imageViewFromFB = binding.imageArea

        storageReference.downloadUrl.addOnCompleteListener (OnCompleteListener { task ->

            // 이미지 업로드 성공
            if(task.isSuccessful){
                // Glide를 사용하여 task에서 이미지 직접 다운로드
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

                // 이미지 업로드 실패
            }else{

            }
        })

    }

    // board데이터 받아오는 함수 (게시글에서 쓴 데이터 가져와서 레아이웃에 표현하기)
    private fun getEditBoardData(key : String){

        // 데이터 가져오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {


                // try문에서 에러발생하면 catch문 실행
                try {

                    //데이터 받아오기
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)

                    // 레이아읏과 연결
                    binding.titleArea.setText(dataModel!!.title)   // titleArea와 BoardModel 연결
                    binding.contentArea.setText(dataModel.content)  // contentArea와 BoardModel 연결
                    binding.urlArea.setText(dataModel.url)
                    // uid가져옴(글쓴사람의 uid)
                    writeUid = dataModel.uid

                } catch (e: Exception){
                    Log.d(TAG, "삭제완료")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // board안에있는 key값을 가져오기
        FBRef.boardRef.child(key).addValueEventListener(postListener)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 갤러리에서 데이터 받아오기
        if(resultCode == RESULT_OK && requestCode == 100){

            // 받아온 데이터를 레이아웃에 표시(갤러리 데이터가 imageArea에 표시)
            binding.imageArea.setImageURI(data?.data)
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
}