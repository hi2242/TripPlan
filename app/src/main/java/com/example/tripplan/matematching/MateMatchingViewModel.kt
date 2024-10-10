package com.example.tripplan.matematching

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripplan.KaKaoAuthViewModel.Companion.TAG
import com.example.tripplan.data.AreaData
import com.example.tripplan.data.toAreaDataList
import com.example.tripplan.repository.AreaRepository
import com.example.tripplan.repository.Category
import retrofit2.HttpException
import kotlinx.coroutines.launch

private const val TAG = "MateMatchingViewModel"

class MateMatchingViewModel(private val repository: AreaRepository = AreaRepository()) : ViewModel() {
    private val _areaDataList = MutableLiveData<List<AreaData>?>()
    val areaDataList get() = _areaDataList

    fun getAreaList(category: Category, query: String) {
        Log.d("MateMatchingViewModel", "Requesting area data with category: $category, query: $query")
        viewModelScope.launch {
            runCatching {
                repository.getArea(category, query)
            }.onSuccess {
                _areaDataList.value = it.toAreaDataList()
            }.onFailure {
                Log.e(TAG, "getAreaList() failed! : $it")
                if (it is HttpException) {
                    val errorJsonString = it.response()?.errorBody()?.string()
                    Log.e(TAG, "getAreaList() failed! : $errorJsonString")
                }
            }
        }
    }
}