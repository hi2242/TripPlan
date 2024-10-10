package com.example.tripplan.fragment

data class GuestModel(
    val name : String = "",   // 이름
    val gender : String = "",   // 성별
    val nickname : String = "",     // 닉네임
    val age : String = "",      // 나이
    val kakaoId : String = "", // 카카오톡 ID
    val mbti : String = "",     // MBTI
    val prefMbti: String = "", // 선호 MBTI
    val prefAge: String = "", // 선호 나이 차
    val region : String = "",      // 도시
    val region2 : String = "",      // 세부 지역(시군구)
    val nameGuestHouse : String = "",      // 게스트 하우스 이름
    val date : String = "",      // 여행 날짜
    val style : String = "",      // 게스트 스타일
    val expense : String = "",      // 여행 경비
    val car : String = "",      // 자차 유무
    val criminal : String = "",      // 범죄 경력 회보서
    val email : String = "",      // kakao email
    val time : String = "",     // 현재시간
    val timestamp: Long = 0L,    // 게시글 생성 시간(밀리초)
    val thumbnailUrl: String = "" // 썸네일 이미지 URL
)
