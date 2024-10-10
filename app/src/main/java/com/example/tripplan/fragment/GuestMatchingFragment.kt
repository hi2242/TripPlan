package com.example.tripplan

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.example.tripplan.activity.PrivateInformationActivity
import com.example.tripplan.activity.SearchActivity
import com.example.tripplan.data.AreaData
import com.example.tripplan.fragment.GuestMatchingViewModel
import com.example.tripplan.fragment.GuestModel
import com.example.tripplan.databinding.FragmentGuestMatchingBinding
import com.example.tripplan.matematching.MateMatchingViewModel
import com.example.tripplan.repository.Category
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.util.Calendar

class GuestMatchingFragment : Fragment() {
    companion object {
        private const val REQUEST_CODE_GUEST_INFO = 1001 // 고유한 정수 값으로 설정
    }
    private lateinit var binding: FragmentGuestMatchingBinding
    private lateinit var imageUri: Uri
    private val viewModel by viewModels<GuestMatchingViewModel>()
    private val viewModel2 by viewModels<MateMatchingViewModel>()
    private var selectedCityAreaCode: String? = ""
    private var isDialogShowing = false
    private lateinit var thumbnailUrl: String

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val selectedData = data?.getStringExtra("selected_data")
            val thumbnailGuestHouse = data?.getStringExtra("thumbnailUrl")
            Log.d("GuestMatchingFragment", "GuestMatchingFragment : ${selectedData}")
            binding.searchInGuest.text = selectedData
            if (thumbnailGuestHouse != null) {
                thumbnailUrl = thumbnailGuestHouse
            }
        }
    }

    // 이미지 선택을 위한 ActivityResultLauncher를 등록
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "Unknown File"
            binding.criminalInGuest.text = fileName
            // 여기서 선택된 이미지를 처리합니다. 예를 들어, 이미지를 ImageView에 설정할 수 있습니다.
            // imageView.setImageURI(imageUri)

            // 만약 선택한 이미지를 서버에 업로드하거나 다른 작업을 원한다면 여기에 코드 작성
        }
    }

    //    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnMatchingInGuest.setOnClickListener {
                val name = nameInGuest.text.toString()
                val gender = genderInGuest.text.toString()
                val nickname = nicknameInGuest.text.toString()
                val age = ageInGuest.text.toString()
                val kakaoId = kakaoInGuest.text.toString()
                val MBTI = MBTIInGuest.text.toString()
                val prefMbti = prefMBTIInGuest.text.toString()
                val prefAge = prefAgeInGuest.text.toString()
                val region = regionInGuest.text.toString()
                val region2 = regionInGuest2.text.toString()
                val nameGuestHouse = searchInGuest.text.toString()
                val date = dateInGuest.text.toString()
                val style = styleInGuest.text.toString()
                val expense = moneyInGuest.text.toString()
                val car = carInGuest.findViewById<RadioButton>(carInGuest.checkedRadioButtonId)?.text.toString()
                val criminal = criminalInGuest.text.toString()
                val email = UserInfo.email
                val time = FBAuth.getTime()
                val timestamp = System.currentTimeMillis() // 현재 시간
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
                if (prefMbti.trim() == "선호 MBTI") {
                    emptyFields.add("선호 MBTI")
                }
                if (region.trim() == "사는 지역(도시)") {
                    emptyFields.add("사는 지역(도시)")
                }
                if (region2.trim() == "사는 지역(시군구)") {
                    emptyFields.add("사는 지역(시군구)")
                }
                if (prefAge.trim() == "선호하는 나이 차") {
                    emptyFields.add("선호하는 나이 차")
                }
                if (nameGuestHouse.trim() == "게스트하우스 검색") {
                    emptyFields.add("게스트하우스 찾기")
                }
                if (date.trim() == "날짜") {
                    emptyFields.add("여행 날짜")
                }
                if (style.trim() == "게스트 스타일 (최대 3개)") {
                    emptyFields.add("게스트 스타일")
                }
                if (expense.isEmpty()) emptyFields.add("여행 경비")
                if (car.trim().isEmpty()) emptyFields.add("자차 유무")
                if (criminal.trim() == "이미지 첨부") {
                    emptyFields.add("범죄 경력 회보서")
                }

                // Show a toast message if there are empty fields
                if (emptyFields.isNotEmpty()) {
                    val missingFields = emptyFields.joinToString(", ")
                    Toast.makeText(requireContext(), "$missingFields 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else if (private == "동의하지 않습니다." || private == "개인정보 수집, 이용 안내") {
                    Toast.makeText(requireContext(), "개인 정보 수집에 동의해 주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                FBRef.guestMatchingRef
                    .push()
                    .setValue(
                        email?.let { it1 ->
                            GuestModel(name, gender, nickname, age, kakaoId, MBTI, prefMbti, prefAge, region, region2,
                                nameGuestHouse, date, style, expense, car, criminal, it1, time, timestamp, thumbnailUrl)
                        }
                    )

                imageUri?.let { uri ->
                    uploadImageToFirebase(uri) // 이미지 업로드 함수 호출
                }

                // 메시지 띄우기
                Toast.makeText(requireContext(), "매칭 게시글 업로드 완료", Toast.LENGTH_SHORT).show()

                // Use Handler to delay finishing the Activity
                Handler(Looper.getMainLooper()).postDelayed({
                    requireActivity().finish() // 현재 Activity 종료
                }, Toast.LENGTH_SHORT.toLong())
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGuestMatchingBinding.inflate(inflater, container, false)

        with(binding) {
            genderInGuest.setOnClickListener {
                radioDialog(R.array.genders, binding.genderInGuest, "성별을 선택하세요.")
            }
            MBTIInGuest.setOnClickListener {
                radioDialog(R.array.MBTI, binding.MBTIInGuest, "MBTI를 선택하세요.")
            }
            prefMBTIInGuest.setOnClickListener {
                radioDialog(R.array.MBTI, binding.prefMBTIInGuest, "MBTI를 선택하세요.")
            }
            prefAgeInGuest.setOnClickListener {
                radioDialog(R.array.differAge, binding.prefAgeInGuest, "MBTI를 선택하세요.")
            }
            regionInGuest.setOnClickListener {
                showCitySelectionDialog(R.array.CITY, binding.regionInGuest, "도시를 선택하세요.")
            }
            regionInGuest2.setOnClickListener {
                if (!isDialogShowing && !selectedCityAreaCode.isNullOrEmpty()) {
                    showDistrictSelectionDialog(binding.regionInGuest2, "세부 지역(시군구)을 선택하세요.")
                } else {
                    // 도시 선택이 안 되었을 경우 경고 메시지
                    Toast.makeText(requireContext(), "먼저 도시를 선택하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            searchInGuest.setOnClickListener {
                val intent = Intent(requireContext(), SearchActivity::class.java)
                resultLauncher.launch(intent)
            }
            btnPrivateInfo.setOnClickListener {
                // RegisterActivity에서 PrivateInformationActivity를 호출
                val intent = Intent(requireContext(), PrivateInformationActivity::class.java)
                intent.putExtra("key", "GuestMatchingFragment")
                startActivityForResult(intent, REQUEST_CODE_GUEST_INFO)
            }

            dateInGuest.setOnClickListener {
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
                    dateInGuest.text = "$startYear-${startMonth.toString().padStart(2, '0')}-${
                        startDay.toString().padStart(2, '0')
                    } ~ " +
                            "$endYear-${endMonth.toString().padStart(2, '0')}-${
                                endDay.toString().padStart(2, '0')
                            }"
                }

                // 날짜 범위 선택기 표시
                dateRangePicker.show(parentFragmentManager, dateRangePicker.toString())
            }

            styleInGuest.setOnClickListener {
                multiChooseDialog(R.array.styleGuest, binding.styleInGuest, "게스트 스타일을 선택하세요.")
            }
            criminalInGuest.setOnClickListener {
                openImageChooser()
            }
            helpIcon.setOnClickListener {
                showHelpDialog()
            }
        }

        return binding.root
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_mate_matching, container, false)
    }

    // 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GUEST_INFO && resultCode == RESULT_OK) {
            val agreed = data?.getBooleanExtra("agreed", false) ?: false
            if (agreed) {
                binding.btnPrivateInfo.text = "동의합니다."
            } else {
                binding.btnPrivateInfo.text = "동의하지 않습니다."
            }
        }
    }
    fun radioDialog(resArray: Int, targetTextView: TextView, title: String) {
        hideKeyboard() // 키보드 숨기기
        val dataList = arrayListOf(*requireContext().resources.getStringArray(resArray))

        var checkedItem = 0

        val builder = AlertDialog.Builder(requireContext())
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

    fun multiChooseDialog(resArray: Int, targetTextView: TextView, title: String){
        hideKeyboard() // 키보드 숨기기
        val dataList = arrayListOf(*requireContext().resources.getStringArray(resArray))
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        val boolArray = BooleanArray(dataList.size){i->false}

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
                Toast.makeText(requireContext(), "최대 3개의 항목만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("취소",null)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            if (selectedCount < 3) {
                // 선택된 항목이 3개 미만일 때 경고 메시지
                Toast.makeText(requireContext(), "3개의 항목을 선택해야 합니다.", Toast.LENGTH_SHORT).show()
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
    private fun openImageChooser() {
        hideKeyboard() // 키보드 숨기기
        // 이미지 선택기 실행
        pickImageLauncher.launch("image/*")
    }

    // 이미지 업로드 함수
    private fun uploadImageToFirebase(uri: Uri) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("범죄 경력 회보서(게스트 매칭)/${System.currentTimeMillis()}.png")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                Log.d("ImageUpload", "Image upload succeeded")
            }
            .addOnFailureListener { exception ->
                Log.e("ImageUpload", "Image upload failed: ${exception.message}")
                Toast.makeText(requireContext(), "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
    fun Fragment.hideKeyboard() {
        // 현재 포커스가 있는 View를 가져옵니다.
        val view = requireActivity().currentFocus ?: return
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun showCitySelectionDialog(resArray: Int, targetTextView: TextView, title: String) {
        hideKeyboard() // 키보드 숨기기
        val dataList = requireContext().resources.getStringArray(resArray)
        val cityCodeMap = requireContext().resources.getStringArray(R.array.areaCode)
        var checkedItem = 0

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        builder.setNegativeButton("취소", null)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            // 선택된 도시명과 코드 설정
            val selectedCity = dataList[checkedItem]
            targetTextView.text = selectedCity
            selectedCityAreaCode = cityCodeMap[checkedItem] ?: ""
            binding.regionInGuest2.text = "사는 지역(시군구)"
            // 세부 지역 목록을 가져오기 위해 API 호출
            viewModel2.getAreaList(Category.DISTRICT, selectedCityAreaCode!!)
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

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        builder.setNegativeButton("취소", null)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            targetTextView.text = viewModel2.areaDataList.value?.get(checkedItem)?.nameForCity
//            selectedCityAreaCode = ""
//            viewModel2.getAreaList(Category.CITY, selectedCityAreaCode.toString())
            viewModel2.areaDataList.observe(viewLifecycleOwner) { areaDataList: List<AreaData>? ->
                // UI 업데이트 작업
                if (areaDataList != null) {
                    Log.d("MateMatchingFragment", "AreaDataList: $areaDataList")
                    // 예: 데이터가 업데이트되면 UI에 적용하는 로직 추가
                }
            }
        }

        builder.setSingleChoiceItems(viewModel2.areaDataList.value?.map { it.nameForCity }
            ?.toTypedArray(), checkedItem) { dialogInterface: DialogInterface, position: Int ->
            checkedItem = position
        }

        builder.show()

    }
    private fun showHelpDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("도움말") // 다이얼로그 제목
            .setMessage(R.string.help_icon_desc_guest) // 다이얼로그 메시지
            .setPositiveButton("확인", null) // 확인 버튼
            .show()
    }
}