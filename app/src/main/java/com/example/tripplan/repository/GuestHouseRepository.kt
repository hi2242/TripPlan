package com.example.tripplan.repository

import com.example.tripplan.model.GuestHouseModel
import com.example.tripplan.network.AreaRetrofitInstance
import com.example.tripplan.network.GuestHouseRetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class GuestHouseRepository {
    suspend fun getGuestHouse(category: CategoryGuestHouse, query: String): GuestHouseModel =
        withContext(IO) {
            when (category) {
                is CategoryGuestHouse.SEARCHKEYWORD -> GuestHouseRetrofitInstance.service.getGuestHouse(keyword = query)
            }
        }
}