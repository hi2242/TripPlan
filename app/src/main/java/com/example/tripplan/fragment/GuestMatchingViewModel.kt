package com.example.tripplan.fragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripplan.KaKaoAuthViewModel.Companion.TAG
import com.example.tripplan.data.GuestHouseData
import com.example.tripplan.data.toGuestHouseDataList
import com.example.tripplan.repository.CategoryGuestHouse
import com.example.tripplan.repository.GuestHouseRepository
import retrofit2.HttpException
import kotlinx.coroutines.launch

class GuestMatchingViewModel(private val repository: GuestHouseRepository = GuestHouseRepository()) : ViewModel() {
    private val _GuestHouseDataList = MutableLiveData<List<GuestHouseData>?>()
    val GuestHouseDataList: LiveData<List<GuestHouseData>?> get() = _GuestHouseDataList

    fun getGuestHouseList(category: CategoryGuestHouse, query: String) {
        viewModelScope.launch {
            runCatching {
                repository.getGuestHouse(category, query)
            }.onSuccess {
                _GuestHouseDataList.value = it.toGuestHouseDataList()
            }.onFailure {
                Log.e(TAG, "getGuestHouseList() failed! : $it")
                if (it is HttpException) {
                    val errorJsonString = it.response()?.errorBody()?.string()
                    Log.e(TAG, "getGuestHouseList() failed! : $errorJsonString")
                }
            }
        }
    }
}