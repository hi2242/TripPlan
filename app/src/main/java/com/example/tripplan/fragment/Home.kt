package com.example.tripplan.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.tripplan.activity.HomeActivity
import com.example.tripplan.board.BoardInsideActivity
import com.example.tripplan.board.BoardListLVAdapter
import com.example.tripplan.board.BoardModel
import com.example.tripplan.FBRef
import com.example.tripplan.KaKaoAuthViewModel
import com.example.tripplan.KaKaoAuthViewModel.Companion
import com.example.tripplan.R
import com.example.tripplan.User
import com.example.tripplan.UserInfo
import com.example.tripplan.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Home : Fragment() {
    private val TAG = Home::class.java.simpleName

    private val boardDataList = mutableListOf<BoardModel>()
    private val guestDataList = mutableListOf<GuestModel>()
    private val combinedDataList = mutableListOf<Any>() // 통합 리스트

    private lateinit var boardRVAdapter : BoardListLVAdapter

    private lateinit var binding : FragmentHomeBinding
    private lateinit var mDbRef: DatabaseReference //Firebase 데이터베이스 참조 객체
    private lateinit var mAuth: FirebaseAuth //Firebase 인증 객체

    // 키값 넣어주는 리스트
    private val boardKeyList = mutableListOf<String>()
    private val guestKeyList = mutableListOf<String>()
    private val combinedKeyList = mutableListOf<String>() // 통합 키 리스트


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 어뎁터와 ListView 연결
        boardRVAdapter = BoardListLVAdapter(combinedDataList, "Board")
        binding.boardListView.adapter = boardRVAdapter

        getFBBoardData()
        getFBGuestData()

        // SwipeRefreshLayout 새로고침 리스너 설정
        binding.swipeRefreshLayoutHome.setOnRefreshListener {
            refreshData()
        }

        // 플로팅 버튼 가시성 설정
        (activity as? HomeActivity)?.showFab()

        // 리스트뷰 아이템 클릭시
        binding.boardListView.setOnItemClickListener { parent, view, position, id ->
            val item = combinedDataList[position]

            if (item is BoardModel) {
                val intent = Intent(context, BoardInsideActivity::class.java)
                intent.putExtra("key", combinedKeyList[position])   // key값 넘겨줌
                startActivity(intent)
            } else if (item is GuestModel) {
                val intent = Intent(context, GuestInsideActivity::class.java)
                intent.putExtra("key", combinedKeyList[position])   // key값 넘겨줌
                startActivity(intent)
            }
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기본적으로 chip_all을 선택 상태로 설정
        binding.chipAll.isChecked = true

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            filterDataBasedOnChipSelection() // 필터링 함수 호출
        }
        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        // Kakao 유저 프로필 정보를 가져오는 함수 호출
        getKakaoUserProfile()
        // Organ 유저 프로필 정보를 가져오는 함수 호출
        getOrganUserProfile()
    }


    override fun onResume() {
        super.onResume()
        (activity as? HomeActivity)?.showFab()
    }

    override fun onPause() {
        super.onPause()
        // 버튼을 숨기지 않음
    }

    private fun refreshData() {
        // 데이터를 다시 가져옵니다
        getFBBoardData()
        getFBGuestData()

        // 새로고침 완료 처리
        binding.swipeRefreshLayoutHome.isRefreshing = false
    }
    private fun getFBBoardData() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                boardDataList.clear()
                boardKeyList.clear()

                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(BoardModel::class.java)
                    if (item != null) {
                        boardDataList.add(item)
                        boardKeyList.add(dataModel.key.toString())
                    }
                }
                filterDataBasedOnChipSelection()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.addValueEventListener(postListener)
    }

    private fun getFBGuestData() {
        val guestListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                guestDataList.clear()
                guestKeyList.clear()

                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(GuestModel::class.java)
                    if (item != null) {
                        guestDataList.add(item)
                        guestKeyList.add(dataModel.key.toString())
                    }
                }
                filterDataBasedOnChipSelection()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.guestMatchingRef.addValueEventListener(guestListener)
    }
    private fun filterDataBasedOnChipSelection() {
        // 먼저 combinedDataList와 combinedKeyList를 모두 초기화합니다.
        combinedDataList.clear()
        combinedKeyList.clear()

        // Chip 상태에 따른 데이터 필터링
        when (binding.chipGroup.checkedChipId) {
            R.id.chip_all -> {
                // chip_all 상태: boardDataList와 guestDataList를 모두 추가
                combinedDataList.addAll(boardDataList)
                combinedDataList.addAll(guestDataList)
                combinedKeyList.addAll(boardKeyList)
                combinedKeyList.addAll(guestKeyList)
            }
            R.id.chip_company -> {
                // chip_company 상태: boardDataList만 추가
                combinedDataList.addAll(boardDataList)
                combinedKeyList.addAll(boardKeyList)
            }
            R.id.chip_guest -> {
                // chip_guest 상태: guestDataList만 추가
                combinedDataList.addAll(guestDataList)
                combinedKeyList.addAll(guestKeyList)
            }
        }
        // 시간 순서대로 정렬 (최신순으로 정렬)
        val sortedCombinedDataList = combinedDataList.zip(combinedKeyList).sortedByDescending { pair ->
            val (data, _) = pair
            when (data) {
                is BoardModel -> data.timestamp // BoardModel의 timestamp로 정렬
                is GuestModel -> data.timestamp // GuestModel의 timestamp로 정렬
                else -> 0L // 기타 경우 0으로 설정 (정렬에 영향 없음)
            }
        }

        // 정렬된 데이터를 다시 combinedDataList와 combinedKeyList에 대입
        combinedDataList.clear()
        combinedKeyList.clear()

        sortedCombinedDataList.forEach { (data, key) ->
            combinedDataList.add(data)
            combinedKeyList.add(key)
        }

        // 어댑터에 데이터 변경 알림
        boardRVAdapter.notifyDataSetChanged()
//        combinedDataList.reverse()
//        combinedKeyList.reverse()
//        boardRVAdapter.notifyDataSetChanged()
    }

//    // 게시글 데이터 가져오는 함수
//    private fun getFBBoardData(){
//
//        // 데이터 가져오기
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // 겹쳐서 출력되는 현상 방지 (데이터 초기화)
//                boardDataList.clear()
//                boardKeyList.clear()
//
//
//                for (dataModel in dataSnapshot.children) {
//                    val item = dataModel.getValue(BoardModel::class.java)
//                    if (item != null) {
//                        boardDataList.add(item)
//                        boardKeyList.add(dataModel.key.toString())
//                    }
//                }
//                synchronizeData()
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FBRef.boardRef.addValueEventListener(postListener)
//    }
//
//    // 게스트하우스 데이터 가져오는 함수
//    private fun getFBGuestData(){
//        val guestListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // 데이터 초기화
//                guestDataList.clear()
//                guestKeyList.clear()
//                for (dataModel in dataSnapshot.children) {
//                    val item = dataModel.getValue(GuestModel::class.java)
//                    if (item != null) {
//                        guestDataList.add(item)
//                        guestKeyList.add(dataModel.key.toString())
//                    }
//                }
//
//                synchronizeData()
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        FBRef.guestMatchingRef.addValueEventListener(guestListener)
//    }



    // 데이터를 최신순으로 정렬하고 동기화하는 함수
    private fun synchronizeData() {
        combinedDataList.clear()
        combinedDataList.addAll(boardDataList)
        combinedDataList.addAll(guestDataList)

        combinedKeyList.clear()
        combinedKeyList.addAll(boardKeyList)
        combinedKeyList.addAll(guestKeyList)

        combinedDataList.reverse()
        combinedKeyList.reverse()

        boardRVAdapter.notifyDataSetChanged()
    }

    // 카카오 유저 프로필 사진 표시
    private fun getKakaoUserProfile() {
        val email = UserInfo.email
        if (email.isNullOrEmpty()) {
            Log.e(KaKaoAuthViewModel.TAG, "이메일 정보 없음")
            return
        }
        val emailKey = email.replace(".", ",")

        FBRef.kakaoUserRef.child(emailKey).get().addOnSuccessListener { snapshot ->
            val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

            // 프로필 이미지 URL이 존재하면 이미지 로드
            if (profileImageUrl != null) {
                Glide.with(this)
                    .load(profileImageUrl)
                    .circleCrop() // 동그란 이미지로 자르기
                    .into(binding.profileImage)
                Log.d(KaKaoAuthViewModel.TAG, "프로필 이미지 URL: $profileImageUrl")
            } else {
                Log.w(KaKaoAuthViewModel.TAG, "프로필 이미지 URL 정보 없음")
            }
        }.addOnFailureListener {
            Log.e(KaKaoAuthViewModel.TAG, "유저 정보 가져오기 실패", it)
        }
    }
    // 기관 프로필 사진 표시
    private fun getOrganUserProfile() {
        val userId = mAuth.currentUser?.uid ?: return
        FBRef.userRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        user.profileImageUrl?.let { url ->
                            Glide.with(this@Home)
                                .load(url)
                                .circleCrop() // 동그란 이미지로 자르기
                                .into(binding.profileImage)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // 데이터 로드 실패 처리
            }
        })
    }
}