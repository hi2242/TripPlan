package com.example.tripplan.board

data class BoardModel(
    val title : String = "",	// 제목
    val content : String = "",	// 내용
    val uid : String = "",      // uid
    val time : String = "",     // 현재 시간(표시용)
    val url: String = "",       // URL
    val timestamp: Long = 0L,   // 게시글 생성 시간(밀리초)
    val nickname: String = ""   // 작성자 닉네임
)