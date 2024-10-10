package com.example.tripplan.board

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.tripplan.FBRef
import com.example.tripplan.KaKaoAuthViewModel
import com.example.tripplan.KaKaoAuthViewModel.Companion.TAG
import com.example.tripplan.fragment.GuestModel
import com.example.tripplan.matematching.MateModel
import com.example.tripplan.R
import com.example.tripplan.data.GuestHouseData
import com.example.tripplan.data.toGuestHouseDataList
import com.example.tripplan.fragment.GuestMatchingViewModel
import com.example.tripplan.fragment.SubmitModel
import com.example.tripplan.repository.CategoryGuestHouse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Calendar

// BoardModel 데이터모델을 받아옴
class BoardListLVAdapter(val items: MutableList<Any>, val source: String) : BaseAdapter() {

    override fun getCount(): Int {
        // boardList의 사이즈만큼 리턴
        return items
            .size
    }

    override fun getItem(position: Int): Any {
        // boardList를 클릭한값 리턴
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.board_item_last, parent, false)
        }

        val currentItem = items[position]

        if (currentItem is BoardModel) {
            FBRef.userRef.child(currentItem.uid).get().addOnSuccessListener { snapshot ->
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                // 프로필 이미지 URL이 존재하면 이미지 로드
                if (profileImageUrl != null) {
                    parent?.context?.let {
                        view?.findViewById<ImageView>(R.id.profileImage)?.let { it1 ->
                            Glide.with(it) // 여기서 parent의 context를 사용
                                .load(profileImageUrl)
                                .circleCrop() // 동그란 이미지로 자르기
                                .into(it1)
                        }
                    }
                    Log.d(KaKaoAuthViewModel.TAG, "프로필 이미지 URL: $profileImageUrl")
                } else {
                    Log.w(KaKaoAuthViewModel.TAG, "프로필 이미지 URL 정보 없음")
                }
            }
            FBRef.boardRef.get().addOnSuccessListener { snapshot ->
                for (boardSnapshot in snapshot.children) {
                    val thumbnailImageUrl = boardSnapshot.child("thumbnailImageUrl").getValue(String::class.java)
                    val nonameUid = boardSnapshot.child("uid").getValue(String::class.java)
                    val time = boardSnapshot.child("time").getValue(String::class.java)
                    // 여기서 key를 기반으로 데이터를 처리하거나 UI에 반영

                    // UID와 시간값이 currentItem과 동일한지 확인
                    if (currentItem.uid == nonameUid && currentItem.time == time) {
                        // 썸네일 이미지 URL이 있는 경우 해당 이미지를 로드
                        if (thumbnailImageUrl != null) {
                            parent?.context?.let {
                                view?.findViewById<ImageView>(R.id.thumbnail)?.let { imageView ->
                                    Glide.with(it)
                                        .load(thumbnailImageUrl)
                                        .into(imageView)
                                }
                            }
                            Log.d(TAG, "썸네일 이미지 로드: $thumbnailImageUrl")
                        } else {
                            // 썸네일 이미지가 없는 경우 기본 이미지 설정
                            parent?.context?.let {
                                view?.findViewById<ImageView>(R.id.thumbnail)?.let { imageView ->
                                    Glide.with(it)
                                        .load(R.drawable.rounded_rectangle_thumbnail) // 기본 이미지 설정
                                        .into(imageView)
                                }
                            }
                            Log.w(TAG, "썸네일 이미지 없음, 기본 이미지 사용")
                        }
                    }
                }
            }

            // 제목 설정
            view?.findViewById<TextView>(R.id.titleArea)?.let { title ->
                title.text = currentItem.title
            }
            // 내용 설정
            view?.findViewById<TextView>(R.id.contentArea)?.let { content ->
                content.text = currentItem.content
            }
            // 게시글 작성 시간 설정
            view?.findViewById<TextView>(R.id.timeArea)?.let { time ->
                time.text = currentItem.time
            }

            // 마감 시간 영역 숨기기
            view?.findViewById<TextView>(R.id.expirationTimeArea)?.let { expirationTime ->
                expirationTime.visibility = View.GONE
            }

            view?.findViewById<ConstraintLayout>(R.id.itemView)?.let { itemConstraintLayoutView ->
                itemConstraintLayoutView.setBackgroundResource(R.drawable.border_black)
            }

            view?.findViewById<TextView>(R.id.nicknameArea)?.let { nickname ->
                nickname.text = currentItem.nickname
            }

        } else if (currentItem is GuestModel) {
            FBRef.kakaoUserRef.child(currentItem.email).get().addOnSuccessListener { snapshot ->
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                // 프로필 이미지 URL이 존재하면 이미지 로드
                if (profileImageUrl != null) {
                    parent?.context?.let {
                        view?.findViewById<ImageView>(R.id.profileImage)?.let { it1 ->
                            Glide.with(it) // 여기서 parent의 context를 사용
                                .load(profileImageUrl)
                                .circleCrop() // 동그란 이미지로 자르기
                                .into(it1)
                        }
                    }
                    Log.d(KaKaoAuthViewModel.TAG, "프로필 이미지 URL: $profileImageUrl")
                } else {
                    Log.w(KaKaoAuthViewModel.TAG, "프로필 이미지 URL 정보 없음")
                }
            }.addOnFailureListener {
                Log.e(KaKaoAuthViewModel.TAG, "유저 정보 가져오기 실패", it)
            }
            val thumbnailUrl = currentItem.thumbnailUrl
            if (thumbnailUrl.isNotEmpty()) {
                // 썸네일 이미지를 가져와서 표시
                parent?.context?.let {
                    view?.findViewById<ImageView>(R.id.thumbnail)?.let { it1 ->
                        Glide.with(it)
                            .load(thumbnailUrl) // GuestModel에서 가져온 썸네일 URL 사용
                            .into(it1)
                    }
                }
            }
            view?.findViewById<TextView>(R.id.titleArea)?.let { name ->
                name.text = currentItem.nameGuestHouse
            }
            view?.findViewById<TextView>(R.id.contentArea)?.let { age ->
                age.text = currentItem.age
            }

            view?.findViewById<TextView>(R.id.timeArea)?.let { time ->
                time.text = currentItem.time
            }

            // 마감 시간 표시
            view?.findViewById<TextView>(R.id.expirationTimeArea)?.let { expirationTime ->
                expirationTime.visibility = View.VISIBLE
                val remainingTime = calculateRemainingTime(currentItem.timestamp)
                expirationTime.text = remainingTime
            }

            view?.findViewById<ConstraintLayout>(R.id.itemView)?.let { itemConstraintLayoutView ->
                itemConstraintLayoutView.setBackgroundResource(R.drawable.border_black)
            }

            view?.findViewById<TextView>(R.id.nicknameArea)?.let { nickname ->
                nickname.text = currentItem.nickname
            }
        } else if (currentItem is MateModel) {
            // 썸네일 설정: 기본 이미지 사용
            parent?.context?.let {
                view?.findViewById<ImageView>(R.id.thumbnail)?.let { imageView ->
                    Glide.with(it)
                        .load(R.drawable.rounded_rectangle_thumbnail) // 기본 이미지 설정
                        .into(imageView)
                }
            }

            FBRef.kakaoUserRef.child(currentItem.email).get().addOnSuccessListener { snapshot ->
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                // 프로필 이미지 URL이 존재하면 이미지 로드
                if (profileImageUrl != null) {
                    parent?.context?.let {
                        view?.findViewById<ImageView>(R.id.profileImage)?.let { it1 ->
                            Glide.with(it) // 여기서 parent의 context를 사용
                                .load(profileImageUrl)
                                .circleCrop() // 동그란 이미지로 자르기
                                .into(it1)
                        }
                    }
                    Log.d(KaKaoAuthViewModel.TAG, "프로필 이미지 URL: $profileImageUrl")
                } else {
                    Log.w(KaKaoAuthViewModel.TAG, "프로필 이미지 URL 정보 없음")
                }
            }.addOnFailureListener {
                Log.e(KaKaoAuthViewModel.TAG, "유저 정보 가져오기 실패", it)
            }
            view?.findViewById<TextView>(R.id.titleArea)?.let { name ->
                name.text = "메이트 매칭"
            }

            view?.findViewById<TextView>(R.id.contentArea)?.let { age ->
                age.text = currentItem.destination
            }

            view?.findViewById<TextView>(R.id.timeArea)?.let { time ->
                time.text = currentItem.time
            }

            // 마감 시간 표시
            view?.findViewById<TextView>(R.id.expirationTimeArea)?.let { expirationTime ->
                expirationTime.visibility = View.VISIBLE
                val remainingTimeMate = calculateRemainingTimeMate(currentItem.timestamp)
                expirationTime.text = remainingTimeMate
            }

            view?.findViewById<ConstraintLayout>(R.id.itemView)?.let { itemConstraintLayoutView ->
                itemConstraintLayoutView.setBackgroundResource(R.drawable.border_black)
            }

            view?.findViewById<TextView>(R.id.nicknameArea)?.let { nickname ->
                nickname.text = currentItem.nickname
            }
        } else if (currentItem is SubmitModel) {

            FBRef.kakaoUserRef.child(currentItem.email).get().addOnSuccessListener { snapshot ->
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                // 프로필 이미지 URL이 존재하면 이미지 로드
                if (profileImageUrl != null) {
                    parent?.context?.let {
                        view?.findViewById<ImageView>(R.id.profileImage)?.let { it1 ->
                            Glide.with(it) // 여기서 parent의 context를 사용
                                .load(profileImageUrl)
                                .circleCrop() // 동그란 이미지로 자르기
                                .into(it1)
                        }
                    }
                    Log.d(KaKaoAuthViewModel.TAG, "프로필 이미지 URL: $profileImageUrl")
                } else {
                    Log.w(KaKaoAuthViewModel.TAG, "프로필 이미지 URL 정보 없음")
                }
            }.addOnFailureListener {
                Log.e(KaKaoAuthViewModel.TAG, "유저 정보 가져오기 실패", it)
            }
            view?.findViewById<TextView>(R.id.titleArea)?.let { name ->
                name.text = "게스트 신청"
            }

            view?.findViewById<TextView>(R.id.timeArea)?.let { time ->
                time.text = currentItem.time
            }

            view?.findViewById<ConstraintLayout>(R.id.itemView)?.let { itemConstraintLayoutView ->
                itemConstraintLayoutView.setBackgroundResource(R.drawable.border_black)
            }

            view?.findViewById<TextView>(R.id.nicknameArea)?.let { nickname ->
                nickname.text = currentItem.nickname
            }
        }

        return view!!
    }

    private fun calculateRemainingTime(postTimestamp: Long): SpannableString {
        val currentTime = System.currentTimeMillis()
        val timeDifference = 3 * 24 * 60 * 60 * 1000 - (currentTime - postTimestamp) // 3일을 밀리초로 변환

        val color = if (timeDifference > 0) Color.RED else Color.GRAY

        return if (timeDifference > 0) {
            val daysLeft = timeDifference / (24 * 60 * 60 * 1000)
            val hoursLeft = (timeDifference / (60 * 60 * 1000)) % 24

            val timeText = when {
                daysLeft > 0 -> "마감 ${daysLeft}일 전"
                hoursLeft > 0 -> "마감 ${hoursLeft}시간 전"
                else -> "곧 마감!!"
            }
            styleTimeLeftText(timeText, color)
        } else {
            styleTimeLeftText("마감 완료", color)

        }
    }

    private fun calculateRemainingTimeMate(postTimestamp: Long): SpannableString {
        val currentTime = System.currentTimeMillis()
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = postTimestamp

        // 현재 몇 번째 주인지 계산
        val currentWeekOfYear = currentCalendar.get(Calendar.WEEK_OF_YEAR)

        // 현재 주의 토요일 날짜 구하기
        currentCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)

        // 짝수 주일 경우: 이번 주 토요일
        // 홀수 주일 경우: 다음 주 토요일
        if (currentWeekOfYear % 2 != 0) {
            // 현재 주가 홀수 주인 경우 다음 주 토요일로 설정
            currentCalendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val saturdayTimestamp = currentCalendar.timeInMillis
        Log.d("RemainingTime", "Saturday timestamp: $saturdayTimestamp") // 로그 추가

        // 마감 기한까지 남은 시간 계산
        val timeDifference = saturdayTimestamp - currentTime
        Log.d("RemainingTime", "Time difference: $timeDifference") // 로그 추가

        val color = if (timeDifference > 0) Color.RED else Color.GRAY

        return if (timeDifference <= 0) { // 마감이 지났을 때 "마감 완료"로 표시
            styleTimeLeftText("마감 완료", color)
        } else {
            val daysLeft = timeDifference / (24 * 60 * 60 * 1000)
            val hoursLeft = (timeDifference / (60 * 60 * 1000)) % 24

            val timeText = when {
                daysLeft > 0 -> "마감 ${daysLeft}일 전"
                hoursLeft > 0 -> "마감 ${hoursLeft}시간 전"
                else -> "곧 마감!!"
            }
            styleTimeLeftText(timeText, color)
        }
    }



    private fun styleTimeLeftText(timeLeftText: String, color: Int): SpannableString {
        val spannableString = SpannableString(timeLeftText)
        spannableString.setSpan(
            ForegroundColorSpan(color),
            0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }
}