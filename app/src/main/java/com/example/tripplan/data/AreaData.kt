package com.example.tripplan.data

import android.os.Parcelable
import com.example.tripplan.model.AreaModel
import com.example.tripplan.model.ItemX
import kotlinx.parcelize.Parcelize

@Parcelize
data class AreaData(
    val areaCodeForCity: String?,// 지역 코드
    val nameForCity: String?,// 지역 이름
    val numForCity: Int?// 지역 번호
) : Parcelable

fun ItemX.toAreaData() = AreaData(
    areaCodeForCity = this.code,
    nameForCity = this.name,
    numForCity = this.rnum
)

fun AreaModel.toAreaDataList(): List<AreaData>? {
    return response?.body?.items?.item?.map { it.toAreaData() }?.toList()
}