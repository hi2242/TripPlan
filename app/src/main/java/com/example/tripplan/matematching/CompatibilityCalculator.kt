package com.example.tripplan.matematching

import android.util.Log
import kotlin.math.abs

class CompatibilityCalculator {

    fun calculateCompatibility(userMate: MateModel, otherMate: MateModel): Double {
        var score = 0.0

        // 선호 MBTI가 일치하는지 확인 (하나씩 차이날 때 점수 계산)
        val mbtiScore = calculateMBTIScore(userMate.mbti, otherMate.mbti)
        score += mbtiScore
        Log.d("CompatibilityCalculator", "MBTI Score: $mbtiScore, Total Score: $score")

        // 선호 성별과 맞는지 확인 (성별 선호 '무관' 점수 포함)
        val genderScore = calculateGenderScore(userMate.prefGender, otherMate.gender, otherMate.prefGender, userMate.gender)
        score += genderScore
        Log.d("CompatibilityCalculator", "Gender Score: $genderScore, Total Score: $score")

        // 나이 차이가 선호 범위 안에 있는지 확인
        val ageDifference = try {
            abs(userMate.age.toInt() - otherMate.age.toInt())
        } catch (e: NumberFormatException) {
            Log.e("CompatibilityCalculator", "Error parsing age", e)
            0
        }
        val ageScore = calculateAgeScore(ageDifference, userMate.prefAge, otherMate.prefAge)
        score += ageScore
        Log.d("CompatibilityCalculator", "Age Difference: $ageDifference, Age Score: $ageScore, Total Score: $score")

        // 도시 및 세부 지역 점수 계산
        val locationScore = when {
            userMate.region.equals(otherMate.region, ignoreCase = true) &&
                    userMate.region2.equals(otherMate.region2, ignoreCase = true) -> 15.0
            userMate.region.equals(otherMate.region, ignoreCase = true) -> 7.5
            else -> 5.0
        }
        score += locationScore
        Log.d("CompatibilityCalculator", "Location Score: $locationScore, Total Score: $score")

        // 여행 스타일 점수 계산
        val userStyles = userMate.style.split(",").toSet()
        val otherStyles = otherMate.style.split(",").toSet()
        val commonStyles = userStyles.intersect(otherStyles).size
        val styleScore = commonStyles * 7.0
        score += styleScore
        Log.d("CompatibilityCalculator", "Common Styles: $commonStyles, Style Score: $styleScore, Total Score: $score")

        // 여행 경비 점수 계산
        val expenseDifference = try {
            abs(userMate.expense.toInt() - otherMate.expense.toInt())
        } catch (e: NumberFormatException) {
            Log.e("CompatibilityCalculator", "Error parsing expense", e)
            0
        }
        // 경비 차이에 따른 점수 계산
        val expenseScore = when (expenseDifference) {
            in 0..10 -> 10.0 // 0~10 만원 차이: 최대 점수 10점
            in 11..20 -> 7.5 // 10~20 만원 차이: 7점
            in 21..30 -> 5.0 // 20~30 만원 차이: 10점
            in 31..40 -> 2.5 // 30~40 만원 차이: 10점
            else -> 0.0 // 40 만원 초과: 0점
        }
        score += expenseScore
        // 로그 출력 (옵션)
        Log.d("CompatibilityCalculator", "Expense difference: $expenseDifference, Expense score: $expenseScore")

        // 자차 유무 점수 계산
        val carScore = when {
            userMate.car.equals("있음", ignoreCase = true) && otherMate.car.equals("있음", ignoreCase = true) -> 4.0
            userMate.car.equals("있음", ignoreCase = true) || otherMate.car.equals("있음", ignoreCase = true) -> 2.0
            else -> 0.0
        }
        score += carScore
        Log.d("CompatibilityCalculator", "Car Score: $carScore, Total Score: $score")

        return score.coerceIn(0.0, 100.0) // 0~100 점수로 보장
    }

    private fun calculateMBTIScore(userMBTI: String, otherMBTI: String): Double {
        var score = 0.0
        val matchingLetters = userMBTI.zip(otherMBTI).count { it.first.equals(it.second, ignoreCase = true) }
        score = when (matchingLetters) {
            4 -> 10.0
            3 -> 9.0
            2 -> 8.0
            1 -> 7.0
            else -> 0.0
        }
        return score
    }

    private fun calculateGenderScore(userPrefGender: String, otherGender: String, otherPrefGender: String, userGender: String): Double {
        return when {
            userPrefGender == "무관" && otherPrefGender == "무관" -> 15.0
            userPrefGender == otherGender && otherPrefGender == userGender -> 15.0
            userPrefGender == otherGender || otherPrefGender == userGender -> 7.5
            else -> 0.0
        }
    }

    private fun calculateAgeScore(ageDifference: Int, userPrefAge: String, otherPrefAge: String): Double {
        val userPrefRange = parsePreferredAgeRange(userPrefAge)
        val otherPrefRange = parsePreferredAgeRange(otherPrefAge)

        return when {
            // 둘 다 '무관'이면 만점
            userPrefAge == "무관" && otherPrefAge == "무관" -> 20.0
            // 한 명이 '무관'이고 나머지 한 명의 선호 나이 차이에 맞으면 만점
            userPrefAge == "무관" && ageDifference <= otherPrefRange -> 20.0
            otherPrefAge == "무관" && ageDifference <= userPrefRange -> 20.0
            // 한 명이 '무관'이고 나머지 한 명의 선호 나이 차이에 맞지 않으면 10점
            userPrefAge == "무관" && ageDifference > otherPrefRange -> 10.0
            otherPrefAge == "무관" && ageDifference > userPrefRange -> 10.0
            // 둘 다 선호 나이차가 맞물리면 만점
            ageDifference <= userPrefRange && ageDifference <= otherPrefRange -> 20.0
            // 둘 중 한명이라도 선호 나이차가 맞물리면 10점
            ageDifference <= userPrefRange || ageDifference <= otherPrefRange -> 10.0
            else -> 0.0
        }
    }

    private fun parsePreferredAgeRange(prefAge: String): Int {
        return when (prefAge) {
            "동갑" -> 0
            "3살 차이 이하" -> 3
            "5살 차이 이하" -> 5
            "7살 차이 이하" -> 7
            "10살 차이 이하" -> 10
            "무관" -> Int.MAX_VALUE // 나이 차이에 상관 없음
            else -> 0
        }
    }
}
