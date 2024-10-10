package com.example.tripplan

data class User(
    var nick: String = "",
    var email: String = "",
    var phone: String = "",
    var explain: String = "",
    var uId: String = "",
    var isAuthorized: Boolean = false,
    var profileImageUrl: String = "" // 프로필 이미지 URL 추가
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "", "", false, "")
}
