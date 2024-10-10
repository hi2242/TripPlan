package com.example.tripplan.network

import com.example.tripplan.BuildConfig.API_KEY
import com.example.tripplan.model.GuestHouseModel
import retrofit2.http.GET
import retrofit2.http.Query

// 배포시에는 안전하게 보완 적용 필요
// 공공 데이터 포탈에서 발급 받은 자신만의 API키를 입력해 주세요.

interface GuestHouseService {

    @GET("searchKeyword1?&serviceKey=$API_KEY")
    suspend fun getGuestHouse(
        @Query("contentTypeId") contentTypeId: String = "32",
        @Query("listYN") listYN: String = "Y",
        @Query("arrange") arrange: String = "A",
        @Query("keyword") keyword: String = "",
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("MobileOS") mobileOS: String = "AND",
        @Query("MobileApp") mobileAPP: String = "TripPlan",
        @Query("_type") type: String = "json"
    ): GuestHouseModel
}