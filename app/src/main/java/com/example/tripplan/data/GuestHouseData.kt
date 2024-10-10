package com.example.tripplan.data

import android.os.Parcelable
import com.example.tripplan.model.AreaModel
import com.example.tripplan.model.GuestHouseModel
import com.example.tripplan.model.Item
import com.example.tripplan.model.ItemX
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GuestHouseData(
    val addressForGuestHouse: String?, // 주소
    val addressForGuestHouseDistrict: String?, // 상세 주소
    val modifyTimeForGuestHouse: String?, // 수정일
    val telForGuestHouse: String?, // 전화번호
    val titleForGuestHouse: String?, // 제목
    val imageUrl: String?, // 대표이미지(원본)
//    val areacode: String, // 도시 코드
//    val booktour: String, // 교과서 속 여행지 여부
//    val cat1: String, // 대분류
//    val cat2: String, // 중분류
//    val cat3: String, // 소분류
//    val contentid: String, // 콘텐츠 ID
//    val contenttypeid: String, // 콘텐츠 타입 ID
//    val cpyrhtDivCd: String, // 저작권 유형
    val createdTime: String // 등록일
//    val firstimage2: String, // 대표이미지(썸네일)
//    val mapx: String, // GPS X좌표
//    val mapy: String, // GPS Y좌표
//    val mlevel: String, // Map level
//    val sigungucode: String, // 시군구 코드

) : Parcelable

fun Item.toGuestHouseData() = GuestHouseData(
    addressForGuestHouse = this.addr1,
    addressForGuestHouseDistrict = this.addr2,
    modifyTimeForGuestHouse = this.modifiedtime,
    telForGuestHouse = this.tel,
    titleForGuestHouse = this.title,
    imageUrl = this.firstimage,
    createdTime = this.createdtime
)

fun GuestHouseModel.toGuestHouseDataList(): List<GuestHouseData> {
    return response.body.items.item.map { it.toGuestHouseData() }.toList()
}