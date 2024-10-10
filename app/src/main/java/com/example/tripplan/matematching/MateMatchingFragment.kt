package com.example.tripplan.matematching

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tripplan.FBAuth
import com.example.tripplan.FBRef
import com.example.tripplan.R
import com.example.tripplan.UserInfo
import com.example.tripplan.activity.PrivateInformationActivity
import com.example.tripplan.data.AreaData
import com.example.tripplan.databinding.FragmentMateMatchingBinding
import com.example.tripplan.repository.Category
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.util.Calendar

class MateMatchingFragment : Fragment() {
    companion object {
        private const val REQUEST_CODE_MATE_INFO = 1001 // 고유한 정수 값으로 설정
    }
    private lateinit var binding: FragmentMateMatchingBinding
    private lateinit var imageUri: Uri
    private val viewModel by viewModels<MateMatchingViewModel>()
    private var selectedCityAreaCode: String? = ""
    private var selectedCityAreaCode2: String? = ""
    private var isDialogShowing = false


    // 이미지 선택을 위한 ActivityResultLauncher를 등록
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "Unknown File"
                binding.criminalInMate.text = fileName
                // 여기서 선택된 이미지를 처리합니다. 예를 들어, 이미지를 ImageView에 설정할 수 있습니다.
                // imageView.setImageURI(imageUri)

                // 만약 선택한 이미지를 서버에 업로드하거나 다른 작업을 원한다면 여기에 코드 작성
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnMatchingInMate.setOnClickListener {
                val name = nameInMate.text.toString()
                val gender = genderInMate.text.toString()
                val nickname = nicknameInMate.text.toString()
                val age = ageInMate.text.toString()
                val kakaoId = kakaoInMate.text.toString()
                val mbti = MBTIInMate.text.toString()
                val prefMBTI = prefMBTIInMate.text.toString()
                val prefGender = prefGenderInMate.text.toString()
                val prefAge = prefAgeInMate.text.toString()
                val region = regionInMate.text.toString()
                val region2 = regionInMate2.text.toString()
                val destination = destinationInMate.text.toString()
                val destination2 = destinationInMate2.text.toString()
                val date = dateInMate.text.toString()
                val style = styleInMate.text.toString()
                val expense = moneyInMate.text.toString()
                val car =
                    carInMate.findViewById<RadioButton>(carInMate.checkedRadioButtonId)?.text.toString()
                val criminal = criminalInMate.text.toString()
                val email = UserInfo.email
                val time = FBAuth.getTime()
                val timestamp = System.currentTimeMillis() // 현재 시간
                val seenEmail = ""
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
                if (mbti.trim() == "나의 MBTI") {
                    emptyFields.add("나의 MBTI")
                }
                if (prefMBTI.trim() == "선호 MBTI") {
                    emptyFields.add("선호 MBTI")
                }
                if (prefGender.trim() == "선호 성별") {
                    emptyFields.add("선호 성별")
                }
                if (prefAge.trim() == "선호 나이차") {
                    emptyFields.add("선호 나이차")
                }
                if (region.trim() == "사는 지역(도시)") {
                    emptyFields.add("도시")
                }
                if (region2.trim() == "사는 지역(시군구)") {
                    emptyFields.add("세부 지역")
                }
                if (destination.trim() == "목적 지역(도시)") {
                    emptyFields.add("목적지")
                }
                if (destination2.trim() == "목적 지역(시군구)") {
                    emptyFields.add("세부 목적지")
                }
                if (date.trim() == "여행 날짜") {
                    emptyFields.add("여행 날짜")
                }
                if (style.trim() == "여행 스타일 (최대 3개)") {
                    emptyFields.add("여행 스타일")
                }
                if (expense.isEmpty()) emptyFields.add("예산")
                if (car.trim().isEmpty()) emptyFields.add("자차 유무")
                if (criminal.trim() == "이미지 첨부") {
                    emptyFields.add("범죄 경력 회보서")
                }

                // Show a toast message if there are empty fields
                if (emptyFields.isNotEmpty()) {
                    val missingFields = emptyFields.joinToString(", ")
                    Toast.makeText(
                        requireContext(),
                        "$missingFields 항목을 입력해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                else if (private == "동의하지 않습니다." || private == "개인정보 수집, 이용 안내") {
                    Toast.makeText(requireContext(),"개인 정보 수집에 동의해 주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                FBRef.mateMatchingRef
                    .push()
                    .setValue(email?.let { it1 ->
                        MateModel(
                            name,
                            gender,
                            nickname,
                            age,
                            kakaoId,
                            mbti,
                            prefMBTI,
                            prefGender,
                            prefAge,
                            region,
                            region2,
                            destination,
                            destination2,
                            date,
                            style,
                            expense,
                            car,
                            criminal,
                            it1,
                            time,
                            timestamp,
                            seenEmail
                        )
                    })

                uploadImageToFirebase(imageUri)

                // 메시지 띄우기
                Toast.makeText(requireContext(), "매칭 신청 완료", Toast.LENGTH_SHORT).show()

                // 궁합도 계산 후에 저장하기
                calculateAndSaveCompatibilityScores()

                // Use Handler to delay finishing the Activity
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) {
                        requireActivity().finish() // 현재 Activity 종료
                    }
                }, Toast.LENGTH_SHORT.toLong())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMateMatchingBinding.inflate(inflater, container, false)

        with(binding) {
            genderInMate.setOnClickListener {
                radioDialog(R.array.genders, binding.genderInMate, "성별을 선택하세요.")
            }
            MBTIInMate.setOnClickListener {
                radioDialog(R.array.MBTI, binding.MBTIInMate, "MBTI를 선택하세요.")
            }
            prefMBTIInMate.setOnClickListener {
                radioDialog(R.array.MBTI, binding.prefMBTIInMate, "선호하는 MBTI를 선택하세요.")
            }
            prefGenderInMate.setOnClickListener {
                radioDialog(R.array.genders, binding.prefGenderInMate, "선호하는 성별을 선택하세요.")
            }
            prefAgeInMate.setOnClickListener {
                radioDialog(R.array.differAge, binding.prefAgeInMate, "선호하는 나이차를 선택하세요.")
            }
            regionInMate.setOnClickListener {
                showCitySelectionDialog(R.array.CITY, binding.regionInMate, "도시를 선택하세요.")
            }
            regionInMate2.setOnClickListener {
                if (!isDialogShowing && !selectedCityAreaCode.isNullOrEmpty()) {
                    showDistrictSelectionDialog(binding.regionInMate2, "세부 지역(시군구)을 선택하세요.")
                } else {
                    // 도시 선택이 안 되었을 경우 경고 메시지
                    Toast.makeText(requireContext(), "먼저 도시를 선택하세요.", Toast.LENGTH_SHORT).show()
                }
            }
            btnPrivateInfo.setOnClickListener {
                // RegisterActivity에서 PrivateInformationActivity를 호출
                val intent = Intent(requireContext(), PrivateInformationActivity::class.java)
                intent.putExtra("key", "MateMatchingFragment")
                startActivityForResult(intent, REQUEST_CODE_MATE_INFO)
            }

            destinationInMate.setOnClickListener {
                showDestinationSelectionDialog(
                    R.array.CITY,
                    binding.destinationInMate,
                    "도시를 선택하세요."
                )
            }
            destinationInMate2.setOnClickListener {
                if (!isDialogShowing && !selectedCityAreaCode2.isNullOrEmpty()) {
                    showDestinationDistrictSelectionDialog(
                        binding.destinationInMate2,
                        "목적지(시군구)을 선택하세요."
                    )
                } else {
                    // 도시 선택이 안 되었을 경우 경고 메시지
                    Toast.makeText(requireContext(), "먼저 도시를 선택하세요.", Toast.LENGTH_SHORT).show()
                }
            }

            dateInMate.setOnClickListener {
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
                    dateInMate.text = "$startYear-${startMonth.toString().padStart(2, '0')}-${
                        startDay.toString().padStart(2, '0')
                    } ~ " +
                            "$endYear-${endMonth.toString().padStart(2, '0')}-${
                                endDay.toString().padStart(2, '0')
                            }"
                }

                // 날짜 범위 선택기 표시
                dateRangePicker.show(parentFragmentManager, dateRangePicker.toString())
            }
            styleInMate.setOnClickListener {
                multiChooseDialog(R.array.styleTrip, binding.styleInMate, "여행 스타일을 선택하세요.")
            }
            criminalInMate.setOnClickListener {
                openImageChooser()
            }

            helpIcon.setOnClickListener {
                showHelpDialog()
            }
        }

        return binding.root
    }
    // 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MATE_INFO && resultCode == RESULT_OK) {
            val agreed = data?.getBooleanExtra("agreed", false) ?: false
            if (agreed) {
                binding.btnPrivateInfo.text = "동의합니다."
            } else {
                binding.btnPrivateInfo.text = "동의하지 않습니다."
            }
        }
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
            binding.regionInMate2.text = "사는 지역(시군구)"
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

    private fun showDestinationSelectionDialog(
        resArray: Int,
        targetTextView: TextView,
        title: String
    ) {
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
            selectedCityAreaCode2 = cityCodeMap[checkedItem] ?: ""
            binding.destinationInMate2.text = "목적 지역(시군구)"
            // 세부 지역 목록을 가져오기 위해 API 호출
            viewModel.getAreaList(Category.DISTRICT, selectedCityAreaCode2!!)
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
            targetTextView.text = viewModel.areaDataList.value?.get(checkedItem)?.nameForCity
//            selectedCityAreaCode = ""
//            viewModel.getAreaList(Category.CITY, selectedCityAreaCode.toString())
            viewModel.areaDataList.observe(viewLifecycleOwner) { areaDataList: List<AreaData>? ->
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

    private fun showDestinationDistrictSelectionDialog(targetTextView: TextView, title: String) {
        hideKeyboard() // 키보드 숨기기
        var checkedItem = -1

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        builder.setNegativeButton("취소", null)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            targetTextView.text = viewModel.areaDataList.value?.get(checkedItem)?.nameForCity
//            selectedCityAreaCode2 = ""
//            viewModel.getAreaList(Category.CITY, selectedCityAreaCode2.toString())
            viewModel.areaDataList.observe(viewLifecycleOwner) { areaDataList: List<AreaData>? ->
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

        builder.setSingleChoiceItems(
            dataList.toTypedArray(),
            checkedItem
        ) { dialogInterface: DialogInterface, position: Int ->
            checkedItem = position
        }

        builder.show()
    }

    @SuppressLint("SetTextI18n")
    fun multiChooseDialog(resArray: Int, targetTextView: TextView, title: String) {
        hideKeyboard() // 키보드 숨기기
        val dataList = arrayListOf(*requireContext().resources.getStringArray(resArray))
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        val boolArray = BooleanArray(dataList.size) { i -> false }

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
                Toast.makeText(requireContext(), "최대 3개의 항목만 선택할 수 있습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        builder.setNegativeButton("취소", null)
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
        // Fragment가 Activity에 연결되어 있는지 확인
        if (!isAdded) {
            Log.e("ImageUpload", "Fragment is not attached to an Activity")
            return
        }

        // 안전하게 context를 사용
        val context = context ?: return

        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("범죄 경력 회보서(메이트 매칭)/${System.currentTimeMillis()}.png")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                Log.d("ImageUpload", "Image upload succeeded")
            }
            .addOnFailureListener { exception ->
                Log.e("ImageUpload", "Image upload failed: ${exception.message}")
                Toast.makeText(context, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateAndSaveCompatibilityScores() {
        // MateModel 데이터가 저장된 Firebase 참조 가져오기
        val mateMatchingRef = FBRef.mateMatchingRef

        // Firebase에서 데이터 가져오기
        mateMatchingRef.get().addOnSuccessListener { snapshot ->
            Log.d("Firebase", "Data fetched successfully: ${snapshot.childrenCount} items")

            // 데이터 스냅샷을 MateModel 객체 리스트로 변환
            val mates = snapshot.children.mapNotNull { it.getValue(MateModel::class.java) }
            // 가져온 데이터 로그 출력
            Log.d(
                "Firebase",
                "Fetched mates: ${mates.joinToString(separator = "\n") { "Destination: ${it.destination}, Date: ${it.date}" }}"
            )

            // CompatibilityCalculator 인스턴스 생성
            val compatibilityCalculator = CompatibilityCalculator()

            // 모든 사용자 쌍을 비교하여 필터링된 쌍 찾기
            for (i in mates.indices) {
                val userMate = mates[i]
                for (j in i + 1 until mates.size) {
                    val otherMate = mates[j]
                    // 자기 자신과 비교하지 않기 위한 체크
                    if (userMate.email == otherMate.email) {
                        continue
                    }

                    val userDestination = userMate.destination.trim()
                    val userDate = userMate.date.trim()
                    val otherDestination = otherMate.destination.trim()
                    val otherDate = otherMate.date.trim()

                    Log.d("Firebase", "Comparing: ${userMate.email} with ${otherMate.email}")
                    Log.d("Firebase", "User destination: $userDestination, User date: $userDate")
                    Log.d(
                        "Firebase",
                        "Other destination: $otherDestination, Other date: $otherDate"
                    )

                    // 두 사용자 간의 목적지와 기간이 일치하는지 비교
                    if (userDestination == otherDestination && userDate == otherDate) {
                        Log.d(
                            "Firebase",
                            "Match found for: ${userMate.email} and ${otherMate.email}"
                        )

                        // 두 사용자 간의 궁합 점수 계산
                        val compatibilityScore =
                            compatibilityCalculator.calculateCompatibility(userMate, otherMate)
                        Log.d("Firebase", "Calculated compatibility score: $compatibilityScore")

                        // 계산된 궁합 점수를 Firebase에 저장
                        val scoreRef =
                            FBRef.compatibilityCalRef.child("${userMate.email}_${otherMate.email}").child("Score")
                        scoreRef.setValue(compatibilityScore).addOnSuccessListener {
                            Log.d(
                                "Firebase",
                                "Compatibility score saved successfully for ${userMate.email}_${otherMate.email}."
                            )
                        }.addOnFailureListener { exception ->
                            Log.e(
                                "Firebase",
                                "Error saving compatibility score for ${userMate.email}_${otherMate.email}",
                                exception
                            )
                        }
                        val scoreRefYN = FBRef.compatibilityCalRef.child("${userMate.email}_${otherMate.email}")
                            .child("매칭 여부")
                        scoreRefYN.child("${UserInfo.email}").setValue("")
                        // push()를 붙이면 새 고유 키를 추가 하고 붙이지 않으면 고유 키 값이 추가 되지 않는다.
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error fetching data", exception)
        }
    }

    fun Fragment.hideKeyboard() {
        // 현재 포커스가 있는 View를 가져옵니다.
        val view = requireActivity().currentFocus ?: return
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("도움말") // 다이얼로그 제목
            .setMessage(R.string.help_icon_desc_mate) // 다이얼로그 메시지
            .setPositiveButton("확인", null) // 확인 버튼
            .show()
    }
}