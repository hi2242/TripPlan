package com.example.tripplan.fragment

//data class SubmitModel(
//    val name : String = "",   // 이름
//    val gender : String = "",   // 성별
//    val nickname : String = "",     // 닉네임
//    val age : String = "",      // 나이
//    val kakaoId : String = "", // 카카오톡 ID
//    val MBTI : String = "",     // MBTI
//    val car : String = "",      // 자차 유무
//    val criminal : String = "",      // 범죄 경력 회보서
//    val uid : String = "",      // uid
//    val writerUid : String ="", // 작성자의 uid
//    val time : String = ""      // 현재시간
//)
data class SubmitModel(
    val name: String = "",
    val gender: String = "",
    val nickname: String = "",
    val age: String = "",
    val kakaoId: String = "",
    val mbti: String = "",
    val prefMBTI : String = "",     // 선호하는 MBTI
    val prefAge : String = "",      // 선호하는 나이차
    val region : String = "",      // 도시
    val region2 : String = "",      // 세부 지역(시군구)
    val date: String = "",
    val style: String = "",
    val expense : String = "",      // 여행 경비
    val car: String = "",
    val criminal: String = "",
    val email: String = "",
    val writerEmail: String = "",
    val time: String = ""
)
