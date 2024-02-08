package com.avengers.presentation.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceUtil {

    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radius = 6371000 // 지구 반지름(미터 단위)
        val distanceLatitude = Math.toRadians(lat2 - lat1)
        val distanceLongitude = Math.toRadians(lon2 - lon1)

        val a = (sin(distanceLatitude / 2) * sin(distanceLatitude / 2)) +
                (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                        sin(distanceLongitude / 2) * sin(distanceLongitude / 2))

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radius * c
    }
}