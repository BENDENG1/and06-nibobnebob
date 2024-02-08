package com.avengers.presentation.ui.main.home

import com.avengers.nibobnebob.presentation.ui.main.home.model.UiRestaurantData
import com.avengers.presentation.util.DistanceUtil.haversineDistance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.math.abs


class ClusterManager @Inject constructor() {

    private val _clusteredMarkers = MutableStateFlow<List<Cluster>>(emptyList())
    val clusteredMarkers: StateFlow<List<Cluster>> = _clusteredMarkers

    fun updateCluster(
        markers: List<UiRestaurantData>,
        cameraLatitude: Double,
        cameraLongitude: Double,
        cameraRadius: Double,
    ) {
//        val visibleMarkers = getVisibleMarkers(markers,cameraLatitude,cameraLongitude,cameraRadius)
        val newClusters = clusterMarkers(markers, cameraRadius)
        _clusteredMarkers.value = newClusters
    }

    fun getClusterList(): List<Cluster> {
        return clusteredMarkers.value
    }

//    private fun getVisibleMarkers(
//        markersList: List<UiRestaurantData>,
//        cameraLatitude: Double,
//        cameraLongitude: Double,
//        cameraRadius: Double
//    ): List<UiRestaurantData> {
//        val visibleMarkers = mutableListOf<UiRestaurantData>()
//
//        for (marker in markersList) {
//            val distance = haversineDistance(
//                cameraLatitude,
//                cameraLongitude,
//                marker.latitude,
//                marker.longitude
//            )
//
//            if (distance <= cameraRadius) {
//                visibleMarkers.add(marker)
//            }
//        }
//
//        return visibleMarkers
//    }

    private fun clusterMarkers(
        markers: List<UiRestaurantData>,
        cameraRadius: Double
    ): List<Cluster> {
        val clusteredMarkers = mutableListOf<Cluster>()
        if(cameraRadius < 150)
            return emptyList()

        val radius = cameraRadius * 2
        val divisions = 10
        val divisionWidth = radius / divisions

        for (marker in markers) {
            var isClustered = false

            for (cluster in clusteredMarkers) {
                val distance = haversineDistance(
                    cluster.centerLatitude,
                    cluster.centerLongitude,
                    marker.latitude,
                    marker.longitude
                )

                if (distance < divisionWidth) {
                    val xDistance = abs(cluster.centerLatitude - marker.latitude)
                    val yDistance = abs(cluster.centerLongitude - marker.longitude)

                    val xDivision = (xDistance / divisionWidth).toInt()
                    val yDivision = (yDistance / divisionWidth).toInt()

                    val clusterIndex = xDivision * divisions + yDivision

                    cluster.markers.add(marker)
                    cluster.clusterId.add(clusterIndex)

                    isClustered = true
                    break
                }
            }

            if (!isClustered) {
                val xDivision = ((marker.latitude % divisionWidth) / divisionWidth).toInt()
                val yDivision = ((marker.longitude % divisionWidth) / divisionWidth).toInt()

                val clusterIndex = xDivision * divisions + yDivision

                val newCluster = Cluster(
                    marker.latitude,
                    marker.longitude,
                    mutableListOf(marker),
                    mutableListOf(clusterIndex)
                )

                clusteredMarkers.add(newCluster)
            }
        }

        return clusteredMarkers.filter { it.markers.size >= 5 }
    }

}