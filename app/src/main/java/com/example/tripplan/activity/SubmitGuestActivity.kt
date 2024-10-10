package com.example.tripplan.activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.tripplan.FBAuth
import com.example.tripplan.FBRef
import com.example.tripplan.fragment.GuestModel
import com.example.tripplan.fragment.SubmitModel
import com.example.tripplan.KaKaoAuthViewModel.Companion.TAG
import com.example.tripplan.R
import com.example.tripplan.UserInfo
import com.example.tripplan.data.AreaData
import com.example.tripplan.databinding.ActivitySubmitGuestBinding
import com.example.tripplan.matematching.MateMatchingViewModel
import com.example.tripplan.repository.Category
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import java.util.Calendar

class SubmitGuestActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_SUBMIT_INFO = 1001 // 고유한 정수 값으로 설정
    }
    private lateinit var imageUri: Uri
    private lateinit var binding : ActivitySubmitGuestBinding
    private lateinit var key: String
    private var writerEmail: String? = null
    private lateinit var viewModel: MateMatchingViewModel // ViewModel 사용
    private var selectedCityAreaCode: String? = ""
    private var isDialogShowing = false
    // 이미지 선택을 위한 ActivityResultLauncher를 등록
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "Unknown File"
            binding.criminalInSubmit.text = fileName
            // 여기서 선택된 이미지를 처리합니다. 예를 들어, 이미지를 ImageView에 설정할 수 있습니다.
            // imageView.setImageURI(imageUri)

            // 만약 선택한 이미지를 서버에 업로드하거나 다른 작업을 원한다면 여기에 코드 작성
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitGuestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // ViewModel 초기화
        viewModel = ViewModelProvider(this).get(MateMatchingViewModel::class.java)

        // Home에서 보낸 key 데이터 받아오기
        key = intent.getStringExtra("_key").toString()
        Log.d("key123", "_key :  ${key}")
        getGuestData(key) // 데이터 로드 호출

        with(binding) {
            genderInSubmit.setOnClickListener {
                hideKeyboard() // 키보드 숨기기
                radioDialog(R.array.genders, binding.genderInSubmit, "성별을 선택하세요.")
            }


            dateInSubmit.setOnClickListener {
                hideKeyboard() // 키보드 숨기기
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // 첫 번째로 시작일 선택
                DatePickerDialog(this@SubmitGuestActivity, { _, startYear, startMonth, startDay ->
                    // 시작일을 선택하고, 종료일을 선택하기 위해 다시 DatePickerDialog를 띄움
                    DatePickerDialog(this@SubmitGuestActivity, { _, endYear, endMonth, endDay ->
                        // 시작일과 종료일을 TextView에 표시
                        dateInSubmit.setText(
                            "$startYear-${(startMonth + 1).toString().padStart(2, '0')}-${startDay.toString().padStart(2, '0')} ~ " +
                                    "$endYear-${(endMonth + 1).toString().padStart(2, '0')}-${endDay.toString().padStart(2, '0')}"
                        )
                    }, year, month, day).apply {
                        datePicker.minDate = Calendar.getInstance().apply {
                            set(startYear, startMonth, startDay)
                        }.timeInMillis
                    }.show()
                }, year, month, day).show()
            }

            MBTIInSubmit.setOnClickListener {
                hideKeyboard() // 키보드 숨기기
                radioDialog(R.array.MBTI, binding.MBTIInSubmit, "MBTI를 선택하세요.")
            }
            dateInSubmit.setOnClickListener {
                // 오늘 날짜를 기준으로 minDate를 설정하기 위한 Calendar 객체
                val today = Calendar.getInstance()

                // 오늘 날짜 이후만 선택 가능하게 설정
                val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.from(today.timeInMillis))  // 오늘 날짜 이전은 선택 불가

                // MaterialDatePicker를 통한 날짜 범위 선택기
                val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select Date Range")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

                // 날짜 선택 완료 후 리스너
                dateRangePicker.addOnPositiveButtonClickListener { selection ->
                    val startDate = selection.first ?: return@addOnPositiveButtonClickListener
                    val endDate = selection.second ?: return@addOnPositiveButtonClickListener

                    // 선택된 날짜를 DateTime으로 변환
                    val calendar = Calendar.getInstance()

                    calendar.timeInMillis = startDate
                    val startYear = calendar.get(Calendar.YEAR)
                    val startMonth = calendar.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 1 더해줌
                    val startDay = calendar.get(Calendar.DAY_OF_MONTH)

                    calendar.timeInMillis = endDate
                    val endYear = calendar.get(Calendar.YEAR)
                    val endMonth = calendar.get(Calendar.MONTH) + 1
                    val endDay = calendar.get(Calendar.DAY_OF_MONTH)

                    // TextView에 선택된 날짜 범위를 설정
                    dateInSubmit.text = "$startYear-${startMonth.toString().padStart(2, '0')}-${
                        startDay.toString().padStart(2, '0')
                    } ~ " +
                            "$endYear-${endMonth.toString().padStart(2, '0')}-${
                                endDay.toString().padStart(2, '0')
                            }"
                }

                // 날짜 범위 선택기 표시
                dateRangePicker.show(supportFragmentManager, dateRangePicker.toString())
            }


            styleInSubmit.setOnClickListener {
                hideKeyboard()
                multiChooseDialog(R.array.styleGuest, binding.styleInSubmit, "게스트 스타일을 선택하세요.")
            }
            prefMBTIInSubmit.setOnClickListener {
                hideKeyboard() // 키보드 숨기기
                radioDialog(R.array.MBTI, binding.prefMBTIInSubmit, "선호하는 MBTI를 선택하세요.")
            }
            prefAgeInSubmit.setOnClickListener {
                hideKeyboard() // 키보드 숨기기
                radioDialog(R.array.differAge, binding.prefAgeInSubmit, "선호하는 나이 차를 선택하세요.")
            }
            regionInSubmit.setOnClickListener {
                hideKeyboard()
                showCitySelectionDialog(R.array.CITY, binding.regionInSubmit, "도시를 선택하세요.")
            }
            regionInSubmit2.setOnClickListener {
                hideKeyboard()
                if (!isDialogShowing && !selectedCityAreaCode.isNullOrEmpty()) {
                    showDistrictSelectionDialog(binding.regionInSubmit2, "세부 지역(시군구)을 선택하세요.")
                } else {
                    // 도시 선택이 안 되었을 경우 경고 메시지
                    Toast.makeText(this@SubmitGuestActivity, "먼저 도시를 선택하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            criminalInSubmit.setOnClickListener {
                hideKeyboard() // 키보드 숨기기
                openImageChooser()
            }
            btnPrivateInfo.setOnClickListener {
                // RegisterActivity에서 PrivateInformationActivity를 호출
                val intent = Intent(this@SubmitGuestActivity, PrivateInformationActivity::class.java)
                intent.putExtra("key", "SubmitGuestActivity")
                startActivityForResult(intent, REQUEST_CODE_SUBMIT_INFO)
            }
            helpIcon.setOnClickListener {
                showHelpDialog()
            }

            btnMatchingInSubmit.setOnClickListener {
                val name = nameInSubmit.text.toString()
                val gender = genderInSubmit.text.toString()
                val nickname = nicknameInSubmit.text.toString()
                val age = ageInSubmit.text.toString()
                val kakaoId = kakaoInSubmit.text.toString()
                val MBTI = MBTIInSubmit.text.toString()
                val prefMBTI = prefMBTIInSubmit.text.toString()
                val prefAge = prefAgeInSubmit.text.toString()
                val region = regionInSubmit.text.toString()
                val region2 = regionInSubmit2.text.toString()
                val date = dateInSubmit.text.toString()
                val style = styleInSubmit.text.toString()
                val expense = moneyInSubmit.text.toString()
                val car = carInSubmit.findViewById<RadioButton>(carInSubmit.checkedRadioButtonId)?.text.toString()
                val criminal = criminalInSubmit.text.toString()
                val email = UserInfo.email
                val time = FBAuth.getTime()
                val private = btnPrivateInfo.text.toString().trim()

                // Check for empty values
                val emptyFields = mutableListOf<String>()
                if (name.isEmpty()) emptyFields.add("이름")
                if (gender.trim() == "성별") {
                    emptyFields.add("성별")
                }
                if (nickname.isEmpty()) emptyFields.add("별명")
                if (age.isEmpty()) emptyFields.add("나이")
                if (kakaoId.isEmpty()) emptyFields.add("카카오톡 아이디")
                if (MBTI.trim() == "나의 MBTI") {
                    emptyFields.add("MBTI")
                }
                if (prefMBTI.trim() == "선호 MBTI") {
                    emptyFields.add("선호 MBTI")
                }
                if (region.trim() == "사는 지역(도시)") {
                    emptyFields.add("사는 지역(도시)")
                }
                if (region2.trim() == "사는 지역(시군구)") {
                    emptyFields.add("사는 지역(시군구)")
                }
                if (prefAge.trim() == "선호 나이 차") {
                    emptyFields.add("선호 나이 차")
                }
                if (expense.isEmpty()) emptyFields.add("여행 경비")
                if (car.trim().isEmpty()) emptyFields.add("자차 유무")
                if (criminal.trim() == "이미지 첨부") {
                    emptyFields.add("범죄 경력 회보서")
                }

                // Show a toast message if there are empty fields
                if (emptyFields.isNotEmpty()) {
                    val missingFields = emptyFields.joinToString(", ")
                    Toast.makeText(
                        this@SubmitGuestActivity,
                        "$missingFields 항목을 입력해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                else if (private == "동의하지 않습니다." || private == "개인정보 수집, 이용 안내") {
                    Toast.makeText(this@SubmitGuestActivity, "개인 정보 수집에 동의해 주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                FBRef.submitGuestMatchingRef
                    .child(key)
                    .push()
                    .setValue(
                        email?.let { it1 ->
                            writerEmail?.let { it2 ->
                                SubmitModel(name, gender, nickname, age, kakaoId, MBTI, prefMBTI, prefAge, region, region2, date, style, expense,
                                    car, criminal, it1,
                                    it2, time)
                            }
                        }
                    )

                imageUri?.let { uri ->
                    uploadImageToFirebase(uri) // 이미지 업로드 함수 호출
                }

                Toast.makeText(this@SubmitGuestActivity, "매칭 게시글 업로드 완료", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    this@SubmitGuestActivity.finish()
                }, Toast.LENGTH_SHORT.toLong())
            }
        }
    }
    // 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SUBMIT_INFO && resultCode == RESULT_OK) {
            val agreed = data?.getBooleanExtra("agreed", false) ?: false
            if (agreed) {
                binding.btnPrivateInfo.text = "동의합니다."
            } else {
                binding.btnPrivateInfo.text = "동의하지 않습니다."
            }
        }
    }


    fun radioDialog(resArray: Int, targetTextView: TextView, title: String) {
        val dataList = arrayListOf(*this@SubmitGuestActivity.resources.getStringArray(resArray))

        var checkedItem = 0

        val builder = AlertDialog.Builder(this@SubmitGuestActivity)
        builder.setTitle(title)

        builder.setNegativeButton("취소", null)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            targetTextView.text = dataList[checkedItem]
        }

        builder.setSingleChoiceItems(dataList.toTypedArray(), checkedItem) { dialogInterface: DialogInterface, position: Int ->
            checkedItem = position
        }

        builder.show()
    }

    private fun openImageChooser() {
        // 이미지 선택기 실행
        pickImageLauncher.launch("image/*")
    }

    // 이미지 업로드 함수
    private fun uploadImageToFirebase(uri: Uri) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("범죄 경력 회보서(게스트 매칭 신청자)/${System.currentTimeMillis()}.png")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                Log.d("ImageUpload", "Image upload succeeded")
            }
            .addOnFailureListener { exception ->
                Log.e("ImageUpload", "Image upload failed: ${exception.message}")
                Toast.makeText(this@SubmitGuestActivity, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
    fun multiChooseDialog(resArray: Int, targetTextView: TextView, title: String) {
        hideKeyboard() // 키보드 숨기기
        val dataList = resources.getStringArray(resArray).toList() // 문자열 배열을 리스트로 변환
        val builder = AlertDialog.Builder(this) // 'this'는 액티비티 컨텍스트

        builder.setTitle(title)
        val boolArray = BooleanArray(dataList.size) { false }

        // 선택된 항목의 개수를 추적
        var selectedCount = 0

        builder.setMultiChoiceItems(dataList.toTypedArray(), boolArray) { _, which, isChecked ->
            // 선택 상태 변경 시 selectedCount 업데이트
            if (isChecked) {
                selectedCount++
            } else {
                selectedCount--
            }

            // 선택된 항목이 3개를 초과하는 경우 체크를 해제하고 경고 메시지를 보여줌
            if (selectedCount > 3) {
                boolArray[which] = false
                selectedCount--
                Toast.makeText(this, "최대 3개의 항목만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("취소", null)
        builder.setPositiveButton("확인") { _, _ ->
            if (selectedCount < 3) {
                // 선택된 항목이 3개 미만일 때 경고 메시지
                Toast.makeText(this, "3개의 항목을 선택해야 합니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 선택된 항목을 TextView에 표시
                val selectedItems = mutableListOf<String>()
                for (idx in boolArray.indices) {
                    if (boolArray[idx]) {
                        selectedItems.add(dataList[idx])
                    }
                }
                targetTextView.text = selectedItems.joinToString(", ")
            }
        }

        builder.show()
    }

    // board데이터 받아오는 함수
    private fun getGuestData(key : String){

        // 데이터 가져오기
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // try문에서 에러발생하면 catch문 실행
                try {

                    //데이터 받아오기
                    val dataModel = dataSnapshot.getValue(GuestModel::class.java)
                    writerEmail = dataModel?.email // 글쓴사람의 uid
//                    val dataModel = dataSnapshot.getValue(SubmitModel::class.java)
//                    writerUid = dataModel?.writerUid // 글쓴사람의 uid
//                    writerUid?.let {
//                        val writerUided = FBAuth.getUid()
//                    }

                } catch (e: Exception){
                    Log.d(TAG, "Error fetching guest data: ${e.message}")
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // GuestMatching안에있는 key값을 가져오기
        FBRef.guestMatchingRef.child(key).addValueEventListener(postListener)

    }
    private fun hideKeyboard() {
        // 현재 포커스가 있는 뷰 가져오기
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showCitySelectionDialog(resArray: Int, targetTextView: TextView, title: String) {
        hideKeyboard() // 키보드 숨기기
        val dataList = this.resources.getStringArray(resArray)
        val cityCodeMap = this.resources.getStringArray(R.array.areaCode)
        var checkedItem = 0

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        builder.setNegativeButton("취소", null)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            // 선택된 도시명과 코드 설정
            val selectedCity = dataList[checkedItem]
            targetTextView.text = selectedCity
            selectedCityAreaCode = cityCodeMap[checkedItem] ?: ""
            binding.regionInSubmit2.text = "사는 지역(시군구)"
            // 세부 지역 목록을 가져오기 위해 API 호출
            viewModel.getAreaList(Category.DISTRICT, selectedCityAreaCode!!)
        }

        // 도시 목록을 선택할 수 있는 다이얼로그 생성
        builder.setSingleChoiceItems(
            dataList,
            checkedItem
        ) { dialogInterface: DialogInterface, position: Int ->
            checkedItem = position
        }

        builder.show()

    }
    private fun showDistrictSelectionDialog(targetTextView: TextView, title: String) {
        hideKeyboard() // 키보드 숨기기
        var checkedItem = -1

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        builder.setNegativeButton("취소", null)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            targetTextView.text = viewModel.areaDataList.value?.get(checkedItem)?.nameForCity
//            selectedCityAreaCode = ""
//            viewModel.getAreaList(Category.CITY, selectedCityAreaCode.toString())
            viewModel.areaDataList.observe(this) { areaDataList: List<AreaData>? ->
                // UI 업데이트 작업
                if (areaDataList != null) {
                    Log.d("MateMatchingFragment", "AreaDataList: $areaDataList")
                    // 예: 데이터가 업데이트되면 UI에 적용하는 로직 추가
                }
            }
        }

        builder.setSingleChoiceItems(viewModel.areaDataList.value?.map { it.nameForCity }
            ?.toTypedArray(), checkedItem) { dialogInterface: DialogInterface, position: Int ->
            checkedItem = position
        }

        builder.show()

    }
    private fun showHelpDialog() {
        AlertDialog.Builder(this@SubmitGuestActivity)
            .setTitle("도움말") // 다이얼로그 제목
            .setMessage(R.string.help_icon_desc_guest) // 다이얼로그 메시지
            .setPositiveButton("확인", null) // 확인 버튼
            .show()
    }
}