package com.example.tripplan.data

import android.os.Parcelable
import com.example.tripplan.model.AreaModel
import com.example.tripplan.model.ItemX
import com.example.tripplan.model.ItemXX
import kotlinx.parcelize.Parcelize

//@Parcelize
//data class DetailData(
//    val parkingForGH: String?,// 주차 시설
//    val nameForCity: String?,// 지역 이름
//    val numForCity: Int?// 지역 번호
//) : Parcelable
//
//fun ItemXX.toDetailData() = AreaData(
//)
//
//fun AreaModel.toAreaDataList(): List<AreaData>? {
//    return response?.body?.items?.item?.map { it.toAreaData() }?.toList()
//}