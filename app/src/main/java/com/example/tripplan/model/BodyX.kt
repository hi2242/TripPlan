package com.example.tripplan.model

import com.google.gson.annotations.SerializedName

data class BodyX(
    @SerializedName("items")
    val items: ItemsX?,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int
)