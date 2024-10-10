package com.example.tripplan.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.activity.HistoryActivity
import com.example.tripplan.board.BoardListLVAdapter
import com.example.tripplan.FBRef
import com.example.tripplan.UserInfo
import com.example.tripplan.databinding.ActivityMatchingGuestListBinding
import com.example.tripplan.fragment.SubmitModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class MatchingGuestActivity : AppCompatActivity() {
    private val TAG = MatchingGuestActivity::class.java.simpleName
    private lateinit var key: String
    private lateinit var specificEmail: String
    private val guestSubmitDataList = mutableListOf<SubmitModel>()
    private val combinedDataList = mutableListOf<Any>() // 통합 리스트

    private lateinit var boardRVAdapter : BoardListLVAdapter

    private lateinit var binding : ActivityMatchingGuestListBinding
    // 키값 넣어주는 리스트

    private val guestSubmitKeyList = mutableListOf<String>()
    private val combinedKeyList = mutableListOf<String>() // 통합 키 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchingGuestListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Home에서 보낸 key 데이터 받아오기
        key = intent.getStringExtra("__key").toString()

        // Initialize the adapter with an empty list
        boardRVAdapter = BoardListLVAdapter(combinedDataList, "Board")
        binding.matchingGuestListView.adapter = boardRVAdapter

        // Get data from Firebase
        getFBSubmitData()

        // Setup SwipeRefreshLayout listener
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        // Setup ListView item click listener
        binding.matchingGuestListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val intent = Intent(this, GuestHistoryActivity::class.java)
                intent.putExtra("item", specificEmail)
                intent.putExtra("_key", key)
                intent.putExtra("__key", combinedKeyList[position])
                startActivity(intent)
            }
    }
    private fun refreshData() {
        // 데이터를 다시 가져옵니다
        getFBSubmitData()

        // 새로고침 완료 처리
        binding.swipeRefreshLayout.isRefreshing = false
    }

    // 게스트 신청 데이터 가져오는 함수
    private fun getFBSubmitData(){
        val currentUserEmail = UserInfo.email
        val guestListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 데이터 초기화
                guestSubmitDataList.clear()
                guestSubmitKeyList.clear()

                for (dataModel in dataSnapshot.children) {
                    val item = dataModel.getValue(SubmitModel::class.java)
                    Log.d("abcd1234", item.toString())
                    if (currentUserEmail != null) {
                        Log.d("abcd1234", currentUserEmail)
                    }
                    if (item != null && item.writerEmail == currentUserEmail) { // 현재 사용자 email와 일치하는 데이터만 추가
                        specificEmail = item.email
                        guestSubmitDataList.add(item)
                        guestSubmitKeyList.add(dataModel.key.toString())
                    }
                }

                synchronizeData()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        Log.d("abcd1234", key)
        FBRef.submitGuestMatchingRef.child(key).addValueEventListener(guestListener)
        Log.d("abcd1234", guestListener.toString())
    }

    // 데이터를 최신순으로 정렬하고 동기화하는 함수
    private fun synchronizeData() {
        combinedDataList.clear()
        combinedDataList.addAll(guestSubmitDataList)

        combinedKeyList.clear()
        combinedKeyList.addAll(guestSubmitKeyList)

        combinedDataList.reverse()
        combinedKeyList.reverse()

        boardRVAdapter.notifyDataSetChanged()
    }
}