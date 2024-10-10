// FirebaseService.kt

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FirebaseService {
    @POST("generateCustomToken")
    @FormUrlEncoded
    suspend fun generateCustomToken(
        @Field("accessToken") accessToken: String
    ): CustomTokenResponse
}

// Retrofit 인스턴스 생성
object RetrofitClient {
    private const val BASE_URL = "https://your-server-url.com/"

    val FirebaseService: FirebaseService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirebaseService::class.java)
    }
}

// 데이터 클래스
data class CustomTokenResponse(val token: String)
