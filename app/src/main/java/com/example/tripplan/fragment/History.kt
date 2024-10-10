package com.example.tripplan.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripplan.activity.HistoryActivity
import com.example.tripplan.board.BoardListLVAdapter
import com.example.tripplan.FBRef
import com.example.tripplan.R
import com.example.tripplan.matematching.MateModel
import com.example.tripplan.UserInfo
import com.example.tripplan.activity.HomeActivity
import com.example.tripplan.activity.MatchingGuestActivity
import com.example.tripplan.board.BoardModel
import com.example.tripplan.databinding.BoardItemLastBinding
import com.example.tripplan.databinding.BoardListItemBinding
import com.example.tripplan.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class History : Fragment() {
    private val TAG = History::class.java.simpleName

    private val mateDataList = mutableListOf<MateModel>()
    private val guestDataList = mutableListOf<GuestModel>()
    private val submitDataList = mutableListOf<SubmitModel>()
    private val combinedDataList = mutableListOf<Any>() // 통합 리스트

    private lateinit var boardRVAdapter : BoardListLVAdapter

    private lateinit var binding : FragmentHistoryBinding
    private lateinit var _binding: BoardItemLastBinding // 두 번째 바인딩
    // 키값 넣어주는 리스트
    private val mateKeyList = mutableListOf<String>()
    private val guestKeyList = mutableListOf<String>()
    private val submitKeyList = mutableListOf<String>()
    private val combinedKeyList = mutableListOf<String>() // 통합 키 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        _binding = BoardItemLastBinding.inflate(inflater, container, false)

        // 어뎁터와 ListView 연결
        boardRVAdapter = BoardListLVAdapter(combinedDataList, "Board")
        binding.historyListView.adapter = boardRVAdapter

        getFBMateData()
        getFBGuestData()
//        getFBSubmitData()

        // SwipeRefreshLayout 새로고침 리스너 설정
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        // 리스트뷰 아이템 클릭시
        binding.historyListView.setOnItemClickListener { parent, view, position, id ->
            val item = combinedDataList[position]

            // 클릭된 아이템 뷰에서 expirationTimeArea의 텍스트를 가져옵니다.
            val expirationTimeTextView = view.findViewById<TextView>(R.id.expirationTimeArea) // 텍스트뷰 ID를 정확히 입력해야 합니다.
            val expirationTime = expirationTimeTextView.text.toString() // 텍스트 값을 문자열로 가져옵니다.

            if (item is MateModel) {
                Log.d("abcd1234555", expirationTime)
                if (expirationTime != "마감 완료") { // 마감 시간이 지나지 않았을 때
                    Toast.makeText(context, "아직 메이트 매칭 날짜가 되지 않았습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(context, HistoryActivity::class.java)
                    intent.putExtra("__key", combinedKeyList[position]) // key값 넘겨줌
                    startActivity(intent)
                }
            }
            else if (item is GuestModel) {
                val intent = Intent(context, MatchingGuestActivity::class.java)
                intent.putExtra("__key", combinedKeyList[position])   // key값 넘겨줌
                startActivity(intent)
            }
//            else if (item is SubmitModel) {
//                val intent = Intent(context, HistoryActivity::class.java)
//                intent.putExtra("__key", combinedKeyList[position])   // key값 넘겨줌
//                startActivity(intent)
//            }
        }

        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val blindView = view.findViewById<View>(R.id.blind_view)

        // 카카오 유저 확인
        if (isKakaoUser()) {
            // 카카오 유저일 경우 블라인드 해제
            blindView.visibility = View.GONE
        } else {
            // 카카오 유저가 아닐 경우 블라인드 표시 (유저가 아무것도 못 보게)
            blindView.visibility = View.VISIBLE
            Toast.makeText(context, "일반인 유저만 이용 가능 합니다.", Toast.LENGTH_SHORT).show()
        }

        // 기본적으로 chip_all을 선택 상태로 설정
        binding.chipAll.isChecked = true

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            filterDataBasedOnChipSelection() // 필터링 함수 호출
        }
    }

    private fun isKakaoUser(): Boolean {
        // 카카오 유저 확인 로직. UserInfo.kakaoId가 null이 아니면 카카오 유저로 간주
        return UserInfo.email != null
    }

    private fun refreshData() {
        // 데이터를 다시 가져옵니다
        getFBMateData()
        getFBGuestData()
//        getFBSubmitData()

        // 새로고침 완료 처리
        binding.swipeRefreshLayout.isRefreshing = false
    }

    // 메이트 매칭 데이터 가져오는 함수
    private fun getFBMateData(){
        val currentUserEmail = UserInfo.email
        val mateListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 데이터 초기화
                mateDataList.clear()
                mateKeyList.clear()
                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(MateModel::class.java)
                    if (item != null && item.email == currentUserEmail) { // 현재 사용자 email와 일치하는 데이터만 추가
                        mateDataList.add(item)
                        mateKeyList.add(dataModel.key.toString())
                    }
                }

                filterDataBasedOnChipSelection()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.mateMatchingRef.addValueEventListener(mateListener)
    }

    // 게스트 매칭 데이터 가져오는 함수
    private fun getFBGuestData(){
        val currentUserEmail = UserInfo.email
        val guestListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                guestDataList.clear()
                guestKeyList.clear()

                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(GuestModel::class.java)
                    if (item != null && item.email == currentUserEmail) {
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
    // 게스트 신청 데이터 가져오는 함수
    private fun getFBSubmitData(){
        val currentUserEmail = UserInfo.email
        val submitListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                submitDataList.clear()
                submitKeyList.clear()

//                for (parentNode in dataSnapshot.children) { // 최상위 노드를 먼저 탐색
//                    for (childNode in parentNode.children) { // 그 하위 노드를 다시 탐색
//                        val item = childNode.getValue(SubmitModel::class.java)
//                        if (item != null && item.email == currentUserEmail) { // 현재 사용자 email와 일치하는 데이터만 추가
//                            submitDataList.add(item)
//                            submitKeyList.add(childNode.key.toString()) // 하위 노드의 key 값을 가져옴
//                        }
//                    }
//                }
//                filterDataBasedOnChipSelection()
//            }
                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(SubmitModel::class.java)
                    if (item != null && item.email == currentUserEmail) { // 현재 사용자 email와 일치하는 데이터만 추가
                        submitDataList.add(item)
                        submitKeyList.add(dataModel.key.toString())
                    }
                }
                filterDataBasedOnChipSelection()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.submitGuestMatchingRef.addValueEventListener(submitListener)
    }
    private fun filterDataBasedOnChipSelection() {
        // 먼저 combinedDataList와 combinedKeyList를 모두 초기화합니다.
        combinedDataList.clear()
        combinedKeyList.clear()

        // Chip 상태에 따른 데이터 필터링
        when (binding.chipGroup.checkedChipId) {
            R.id.chip_all -> {
                // chip_all 상태: boardDataList와 guestDataList를 모두 추가
                combinedDataList.addAll(mateDataList)
                combinedDataList.addAll(guestDataList)
                combinedKeyList.addAll(mateKeyList)
                combinedKeyList.addAll(guestKeyList)
            }
            R.id.chip_mate -> {
                // chip_company 상태: boardDataList만 추가
                combinedDataList.addAll(mateDataList)
                combinedKeyList.addAll(mateKeyList)
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
                is MateModel -> data.timestamp // BoardModel의 timestamp로 정렬
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
    }
    // 데이터를 최신순으로 정렬하고 동기화하는 함수
    private fun synchronizeData() {
        combinedDataList.clear()
        combinedDataList.addAll(mateDataList)
        combinedDataList.addAll(guestDataList)
        combinedDataList.addAll(submitDataList)

        combinedKeyList.clear()
        combinedKeyList.addAll(mateKeyList)
        combinedKeyList.addAll(guestKeyList)
        combinedKeyList.addAll(submitKeyList)

        combinedDataList.reverse()
        combinedKeyList.reverse()

        boardRVAdapter.notifyDataSetChanged()
    }
}