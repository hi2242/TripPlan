package com.example.tripplan

import com.google.firebase.Firebase
import com.google.firebase.database.database

class FBRef {
    companion object {
        private val database = Firebase.database
        // 기관 유저
        val userRef = database.getReference("user")
        // 파이어베이스에 comment
        val commentRef = database.getReference("comment")
        // 파이어베이스 데이터베이스에 데이터를 올리는 작업
        val boardRef = database.getReference("board")
        // 메이트 매칭 서비스
        val mateMatchingRef = database.getReference("mateMatching")
        // 메이트 매칭 궁합도 계산
        val compatibilityCalRef = database.getReference("compatibility")
        // 게스트 매칭 궁합도 계산
        val compatibilityCalInGuestRef = database.getReference("compatibilityInGuest")
        // 게스트 매칭 서비스
        val guestMatchingRef = database.getReference("guestMatching")
        // 게스트 매칭 신청
        val submitGuestMatchingRef = database.getReference("submitGuestMatching")
        // 카카오 로그인 및 매칭 정보 일회성 기입
        val kakaoUserRef = database.getReference("kakaoUser")
    }
}