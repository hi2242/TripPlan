package com.example.tripplan.board

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.tripplan.CommentLVAdapter
import com.example.tripplan.CommentModel
import com.example.tripplan.FBAuth
import com.example.tripplan.FBRef
import com.example.tripplan.R
import com.example.tripplan.databinding.ActivityBoardInsideBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage


class BoardInsideActivity : AppCompatActivity() {

    private val TAG = BoardInsideActivity::class.java.simpleName

    private lateinit var binding : ActivityBoardInsideBinding

    // key값 선언
    private lateinit var key : String

    private val commentDataList = mutableListOf<CommentModel>()

    private lateinit var commentAdapter : CommentLVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        //다크모드 비활성화
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_inside)

        // 햄버거바 클릭시
        binding.boardSettingIcon.setOnClickListener {

            // 다이얼로그 띄우기
            showDialog()
        }

        // Home에서 보낸 key데이터 받아오기
        key = intent.getStringExtra("key").toString()

        getBoardData(key)
        getImageData(key)
        binding.urlArea.paintFlags = binding.urlArea.paintFlags or Paint.UNDERLINE_TEXT_FLAG // 밑줄 추가
        // 댓글 입력버튼 눌렀을때
        binding.commentBtn.setOnClickListener {
            insertComment(key)
        }

        // 어뎁터와 ListView 연결
        commentAdapter= CommentLVAdapter(commentDataList)
        binding.commentLV.adapter = commentAdapter

        setupLinks()

        getCommentData(key)
    }

    // board데이터 받아오는 함수
    private fun getBoardData(key : String){
        // 데이터 가져오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // try문에서 에러발생하면 catch문 실행
                try {
                    //데이터 받아오기
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)
                    // 레이아읏과 연결
                    binding.titleArea.text = dataModel!!.title   // titleArea와 BoardModel 연결
                    binding.textArea.text = dataModel!!.content  // textArea와 BoardModel 연결
                    binding.timeArea.text = dataModel!!.time     // timeArea와 BoardModel 연결
                    val url = dataModel.url
                    if (url.isNullOrBlank()) {
                        binding.urlArea.visibility = View.GONE
                    } else {
                        binding.urlArea.text = url
                        binding.urlArea.visibility = View.VISIBLE
                        binding.urlArea.setOnClickListener {
                            openWebView(url)
                        }
                    }
                    val myUid = FBAuth.getUid()  // 현재 내 uid
                    val writerUid = dataModel.uid // 글쓴사람의 uid
                    // 글쓴사람만 게시글 수정, 삭제 가능하도록
                    // 현재 내 uid와 글쓴사람의 uid가 같다면
                    if(myUid.equals(writerUid)){
                        // 햄버거버튼 보이게
                        binding.boardSettingIcon.isVisible = true

                    }else{
                        //햄버거버튼 안보이게
                        binding.boardSettingIcon.isVisible = false

                    }

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

    // 이미지 다운로드 함수(게시글 작성한 이미지 클릭했을때 보이게)
    private fun getImageData(key : String){

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child("board_images/$key.jpg")

        // ImageView in your Activity
        val imageViewFromFB = binding.getImageArea

        storageReference.downloadUrl.addOnCompleteListener (OnCompleteListener { task ->

            // 이미지 업로드 성공
            if(task.isSuccessful){
                // Glide를 사용하여 task에서 이미지 직접 다운로드
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

                // 이미지 업로드 실패
            }else{
                // 이미지를 업로드하지 않았을때는 getImageArea를 보이지않도록
                binding.getImageArea.isVisible = false
            }
        })

    }

    // 웹뷰 보이게하는 함수ㅂ
    private fun openWebView(url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    // 다이얼로그창 띄우는 함수
    private fun showDialog(){

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.board_custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("게시글 수정,삭제")

        val alertDialog = mBuilder.show()

        // 수정버튼 눌렀을때
        alertDialog.findViewById<Button>(R.id.editBtn)?.setOnClickListener {

            // BoardEditActivity로 이동
            val intent = Intent(this, BoardEditActivity::class.java)

            // BoardEditActivity로 key값 넘겨줌
            intent.putExtra("key", key)

            startActivity(intent)
        }
        // 삭제버튼 눌렀을때
        alertDialog.findViewById<Button>(R.id.removeBtn)?.setOnClickListener {

            // "정말 삭제하겠습니다?" 다이얼로그 추가
            val builder = AlertDialog.Builder(this)
            builder.setTitle("게시글 삭제")
                .setMessage("정말로 삭제하시겠습니까? 삭제하시면 복구할수없습니다")

                .setPositiveButton("네",
                    DialogInterface.OnClickListener{ dialog, id ->

                        // 파이어베이스에 board안에 key값을 찾아와서 삭제
                        FBRef.boardRef.child(key).removeValue()
                        Toast.makeText(this, "삭제완료", Toast.LENGTH_SHORT).show()
                        finish()    // 엑티비티 종료
                    })
                .setNegativeButton("아니오",
                    DialogInterface.OnClickListener{ dialog, id ->
                        finish()
                    })

            // 다이얼로그 띄워주기
            builder.show()
        }
    }

    // 파이어베이스에 입력한 댓글 저장하는 함수
    private fun insertComment(key : String) {

        // FBAuth에서 닉네임을 비동기적으로 가져옴
        FBAuth.getUserNickname { nickname ->
            val commentText = binding.commentArea.text.toString()
            val commentTime = FBAuth.getTime()  // 현재 시간 가져오기

            val comment = CommentModel(
                commentTitle = commentText,
                commentTime = commentTime,
                userNickname = nickname
            )

            FBRef.commentRef    // comment
                .child(key)     // Boardkey
                .push()         // Commentkey
                .setValue(comment)

            Toast.makeText(this, "댓글 입력완료", Toast.LENGTH_SHORT).show()
            // 텍스트 지워줌
            binding.commentArea.setText("")
        }
    }
    // CommentData 받아오는(가져오는) 함수
    private fun getCommentData(key : String){

        // 데이터 가져오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // 겹쳐서 출력되는 현상 방지 (데이터 초기화)
                commentDataList.clear()

                for (dataModel in dataSnapshot.children){

                    // 데이터 받아오기
                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)   // commentDataList에 데이터 하나씩 넣어줌

                }

                commentAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.commentRef.child(key).addValueEventListener(postListener)

    }

    private fun setupLinks() {
        // 텍스트 영역에서 URL 자동 링크화
        val textView = binding.textArea
        LinkifyCompat.addLinks(textView, Linkify.WEB_URLS)
        textView.movementMethod = LinkMovementMethod.getInstance()

        // 텍스트에 링크를 클릭했을 때 처리
        textView.setOnClickListener {
            val text = textView.text.toString()
            val url = extractUrl(text)

            if (url != null) {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }
        }
    }

    private fun extractUrl(text: String): String? {
        val urlPattern = "http[s]?://[a-zA-Z0-9./?=_-]+".toRegex()
        return urlPattern.find(text)?.value
    }


}