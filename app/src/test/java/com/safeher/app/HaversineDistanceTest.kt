package com.safeher.app

import com.safeher.app.data.remote.places.NearbyHelpDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HaversineDistanceTest {

    private val dataSource = NearbyHelpDataSource()

    @Test
    fun calculateDistance_sameCoordinates_returnsZero() {
        val dist = dataSource.calculateHaversineDistance(28.6139, 77.2090, 28.6139, 77.2090)
        assertEquals(0.0, dist, 0.1)
    }

    @Test
    fun calculateDistance_knownLocations_calculatesCorrectRange() {
        // Delhi (28.6139, 77.2090) to Mumbai (19.0760, 72.8777) approx 1150 km
        val distMeters = dataSource.calculateHaversineDistance(28.6139, 77.2090, 19.0760, 72.8777)
        val distKm = distMeters / 1000.0

        assertTrue("Distance should be approx 1150 km, was $distKm", distKm in 1140.0..1170.0)
    }
}
