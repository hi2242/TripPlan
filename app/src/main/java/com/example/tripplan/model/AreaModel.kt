package com.example.tripplan.model

import com.example.tripplan.data.AreaData
import com.google.gson.annotations.SerializedName

data class AreaModel(
    @SerializedName("response")
    val response: ResponseX?
)
//{
//    fun toAreaData(): List<AreaData>? {
//        val responseBody = response?.body ?: return emptyList()
//        val items = responseBody.items?.item ?: return emptyList()
//
//        return items.map { item ->
//            AreaData(
//                areaCodeForCity = item.code,
//                nameForCity = item.name
//            )
//        }
//    }

//    data class AreaData(
//        val code: String,
//        val name: String
//    )
//    private fun List<ItemX>.toAreaDataList(): AreaData {
//        val items = this
//        val areaCode = items.find { it.code == "AREA" }?.code ?: ""
//        val cityName = items.find { it.name == "CITY" }?.name ?: ""
//
//
//        return AreaData(
//            areaCode = areaCode,
//            cityName = cityName
//        )
//    }
//}