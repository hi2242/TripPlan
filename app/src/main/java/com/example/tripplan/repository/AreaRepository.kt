package com.example.tripplan.repository

import com.example.tripplan.model.AreaModel
import com.example.tripplan.network.AreaRetrofitInstance
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class AreaRepository {
    suspend fun getArea(category: Category, query: String): AreaModel = withContext(IO) {
        when (category) {
            is Category.CITY -> AreaRetrofitInstance.service.getArea(areaCode = query)
            is Category.DISTRICT -> AreaRetrofitInstance.service.getArea(areaCode = query)
        }
    }
}