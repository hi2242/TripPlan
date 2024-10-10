package com.example.tripplan.repository

sealed class CategoryGuestHouse(val value: String) {
    object SEARCHKEYWORD : CategoryGuestHouse("상호명")
}