package com.example.tripplan.repository

sealed class Category(val value: String) {
    object CITY : Category("도시 코드")
    object DISTRICT : Category("시군구 코드")
}