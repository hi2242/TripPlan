package com.example.tripplan.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tripplan.fragment.SearchFragment
import com.example.tripplan.R
import com.example.tripplan.databinding.ActivitySearchGuesthouseBinding

class SearchActivity : AppCompatActivity() {
    // private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySearchGuesthouseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchGuesthouseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            // Fragment를 동적으로 추가하는 방법
            val fragment = SearchFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }


//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_DETAIL && resultCode == Activity.RESULT_OK) {
//            // val selectedData = data?.getParcelableExtra("selected_data", GuestHouseData::class.java)
//            // GuestMatchingFragment에 전달할 데이터를 처리합니다.
//            data?.getParcelableExtra("selected_data", GuestHouseData::class.java)?.let {
//                val intent = Intent(this, MatchingActivity::class.java).apply {
//                    putExtra("selected_data", it.titleForGuestHouse)
//                }
//            }
//            startActivity(intent)
//            finish() // SearchActivity 종료
//        }
//    }
//
//    companion object {
//        const val REQUEST_CODE_DETAIL = 1001
//    }
}
