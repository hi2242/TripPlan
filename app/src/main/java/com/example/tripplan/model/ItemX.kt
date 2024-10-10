package com.example.tripplan.model

import com.google.gson.annotations.SerializedName

data class ItemX(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("rnum")
    val rnum: Int
)