package com.example.tripplan.matematching

data class MateModel(
    val name : String = "",   // 이름
    val gender : String = "",   // 성별
    val nickname : String = "",     // 닉네임
    val age : String = "",      // 나이
    val kakaoId : String = "", // 카카오톡 ID
    val mbti : String = "",     // MBTI
    val prefMBTI : String = "",     // 선호하는 MBTI
    val prefGender : String = "",      // 선호하는 성별
    val prefAge : String = "",      // 선호하는 나이차
    val region : String = "",      // 도시
    val region2 : String = "",      // 세부 지역(시군구)
    val destination : String = "",      // 목적지
    val destination2: String = "", // 목적지(시,군,구)
    val date : String = "",      // 여행 기간
    val style : String = "",      // 여행 스타일
    val expense : String = "",      // 여행 경비
    val car : String = "",      // 자차 유무
    val criminal : String = "",      // 범죄 경력 회보서
    val email : String = "",      // kakao email
    val time : String = "",      // 현재시간
    val timestamp: Long = 0L,    // 게시글 생성 시간(밀리초)
    val seenEmail: String = "" // 이미 지나간 이메일
)
