package com.intellinav.core.fusion

import com.intellinav.core.ai.AiKinematicEstimator
import com.intellinav.core.ai.KinematicInferenceResult
import com.intellinav.core.model.FixQuality
import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.GnssFix
import com.intellinav.core.model.ImuSample
import com.intellinav.core.model.NavigationMode
import com.intellinav.core.model.NavigationState
import com.intellinav.core.model.Vector3D
import com.intellinav.core.model.VehicleProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class UkfFusionTest {

  @Test
  fun testUkfPredictionAndGnssCorrection() {
    val ukf: UnscentedKalmanFilter = UnscentedKalmanFilter(
      initialEastMeters = 0.0,
      initialNorthMeters = 0.0,
      initialVeMps = 0.0,
      initialVnMps = 10.0,
      initialHeadingRad = 0.0,
    )

    for (i in 0 until 100) {
      ukf.predict(
        forwardAccelMps2 = 0.0,
        lateralAccelMps2 = 0.0,
        yawRateRadSec = 0.0,
        dtSec = 0.01,
      )
    }

    assertEquals(10.0, ukf.state[1], 0.5)

    val priorNorthVar: Double = ukf.covariance[1][1]
    ukf.updateGnss(
      gnssEastMeters = 0.1,
      gnssNorthMeters = 10.2,
      gnssSpeedMps = 10.0,
      gnssHeadingRad = 0.0,
      posAccuracyMeters = 2.0,
    )

    assertTrue(ukf.covariance[1][1] < priorNorthVar)
  }

  @Test
  fun testTunnelBlackoutContinuousDeadReckoning() {
    val engine: FusionEngine = FusionEngine(
      vehicleProfile = VehicleProfile.PassengerCar,
      referenceOrigin = GeoPoint(latitude = 19.0760, longitude = 72.8777),
    )

    val healthyFix: GnssFix = GnssFix(
      timestampMs = System.currentTimeMillis(),
      latitude = 19.0760,
      longitude = 72.8777,
      speedMps = 20.0f,
      bearingDegrees = 0.0f,
      horizontalAccuracyMeters = 2.5f,
      quality = FixQuality.STANDARD_GPS,
      isBlackout = false,
    )
    engine.processGnss(fix = healthyFix)

    val blackoutFix: GnssFix = GnssFix(
      timestampMs = System.currentTimeMillis(),
      latitude = 19.0760,
      longitude = 72.8777,
      speedMps = 0.0f,
      bearingDegrees = 0.0f,
      horizontalAccuracyMeters = 150.0f,
      quality = FixQuality.NO_FIX,
      isBlackout = true,
    )
    engine.processGnss(fix = blackoutFix)

    var currentTimeNs: Long = 1_000_000_000L
    for (i in 0 until 500) {
      currentTimeNs += 10_000_000L
      val imuSample: ImuSample = ImuSample(
        timestampNs = currentTimeNs,
        linearAcceleration = Vector3D(x = 0.0f, y = 0.0f, z = 0.0f),
        angularVelocity = Vector3D(x = 0.0f, y = 0.0f, z = 0.0f),
        isStationary = false,
      )
      engine.processImu(sample = imuSample)
    }

    val finalState: NavigationState = runBlocking { engine.navigationState.first() }

    assertEquals(NavigationMode.DEAD_RECKONING_TUNNEL, finalState.mode)

    val distanceTraveled: Double = finalState.localPosition.northMeters
    assertTrue(
      "Expected distance traveled in tunnel > 70m, but was: $distanceTraveled",
      distanceTraveled > 70.0,
    )
  }

  @Test
  fun testAiKinematicEstimatorInfersSpeedFromAccel() {
    val estimator: AiKinematicEstimator = AiKinematicEstimator(
      vehicleProfile = VehicleProfile.TwoWheeler,
      windowSize = 10,
    )

    var result: KinematicInferenceResult? = null
    for (i in 0 until 50) {
      val sample: ImuSample = ImuSample(
        timestampNs = i * 10_000_000L,
        linearAcceleration = Vector3D(x = 0f, y = 1.5f, z = 0f),
        angularVelocity = Vector3D(x = 0f, y = 0f, z = 0f),
      )
      result = estimator.infer(sample = sample, dtSec = 0.01f)
    }

    assertTrue(result != null)
    assertTrue(result!!.estimatedSpeedMps > 0.3f)
  }
}
