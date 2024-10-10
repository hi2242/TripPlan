package com.example.tripplan

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class KaKaoAuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "KaKaoAuthViewModel"
    }

    // 로그인 성공 이벤트
    private val _loginSuccessEvent = MutableLiveData<String?>()
    val loginSuccessEvent: LiveData<String?> get() = _loginSuccessEvent

    fun handleKaKaoLogin(activity: Activity) {
        // 카카오계정으로 로그인 공통 callback 구성
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패: ${error.message}", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                // Firebase 인증 진행
                getUserInfo() // 로그인 성공 시 사용자 정보 요청
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)
                    // 사용자가 로그인 취소한 경우 처리
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    // 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    getUserInfo()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = callback)
        }
    }
    private fun getUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.i(TAG, "사용자 정보 요청 성공: ${user.kakaoAccount?.email}")
                // 이메일을 가져와 저장할 때
                UserInfo.email =
                    user.kakaoAccount?.email?.replace(".", ",") // Firebase에서는 '.'을 사용할 수 없으므로 ','로 대체
                _loginSuccessEvent.postValue(user.kakaoAccount?.email) // 이메일 정보 전달
                Log.d(TAG, "loginSuccessEvent triggered with email: ${user.kakaoAccount?.email}")
            }
        }
    }
    fun checkKakaoLoginStatus() {
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {
                Log.e(TAG, "카카오 로그인 세션이 만료되었거나 유효하지 않음", error)
                // 로그인이 풀렸다면 재로그인 처리
            } else if (tokenInfo != null) {
                Log.d(TAG, "카카오 로그인 세션이 유지되고 있음")
            }
        }
    }
//    private val context = getApplication<Application>().applicationContext

//    fun handleKaKaoLogin() {
//        // 로그인 조합 예제
//
//        // 카카오계정으로 로그인 공통 callback 구성
//        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
//        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
//            if (error != null) {
//                Log.e(TAG, "카카오계정으로 로그인 실패", error)
//            } else if (token != null) {
//                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
//                _loginSuccessEvent.postValue(Unit) // 로그인 성공 이벤트 발생
//            }
//        }
//
//        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
//        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
//            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
//                if (error != null) {
//                    Log.e(TAG, "카카오톡으로 로그인 실패", error)
//
//                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
//                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
//                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
//                        return@loginWithKakaoTalk
//                    }
//
//                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
//                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
//                } else if (token != null) {
//                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
//                    _loginSuccessEvent.postValue(Unit) // 로그인 성공 이벤트 발생
//                }
//            }
//        } else {
//            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
//        }
//    }

}