package com.example.tripplan.network

import com.example.tripplan.BuildConfig
import com.example.tripplan.BuildConfig.API_KEY
import com.example.tripplan.model.AreaModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// 배포시에는 안전하게 보완 적용 필요
// 공공 데이터 포탈에서 발급 받은 자신만의 API키를 입력해 주세요.


interface AreaService {
    @GET("areaCode1?serviceKey=${API_KEY}")
    suspend fun getArea(
//        @Query("serviceKey") apiKey: String = BuildConfig.API_KEY,
        @Query("areaCode") areaCode: String? = "", // 지역 코드
        @Query("numOfRows") numOfRows: Int = 20,
        @Query("pageNo") pageNo: Int = 1,
        @Query("MobileOS") mobileOS: String = "AND",
        @Query("MobileApp") mobileAPP: String = "TripPlan",  // 여기를 확인
        @Query("_type") type: String = "json"
    ): AreaModel

}