package com.example.tripplan.model

import com.google.gson.annotations.SerializedName

data class ResponseX(
    @SerializedName("body")
    val body: BodyX?,
    @SerializedName("header")
    val header: HeaderX
)