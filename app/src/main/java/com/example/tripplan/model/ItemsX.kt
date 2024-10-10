package com.example.tripplan.model

import com.google.gson.annotations.SerializedName

data class ItemsX(
    @SerializedName("item")
    val item: List<ItemX>
)