package com.intellinav.core.sensor

import com.intellinav.core.model.ImuSample
import com.intellinav.core.model.Quaternion4D
import com.intellinav.core.model.Vector3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorPipelineTest {

  @Test
  fun testZuptStationaryDetection() {
    val zuptDetector: ZuptDetector = ZuptDetector(
      windowSize = 10,
      accelVarianceThreshold = 0.05f,
      gyroNormThreshold = 0.05f,
    )

    // Feed 15 samples of near-zero motion
    var detectedStationary: Boolean = false
    for (i in 0 until 15) {
      val sample: ImuSample = ImuSample(
        timestampNs = i * 10_000_000L,
        linearAcceleration = Vector3D(x = 0.005f, y = 0.002f, z = 0.001f),
        angularVelocity = Vector3D(x = 0.001f, y = 0.001f, z = 0.002f),
      )
      detectedStationary = zuptDetector.isStationary(sample = sample)
    }

    assertTrue(detectedStationary)
  }

  @Test
  fun testZuptMovingVehicleNotStationary() {
    val zuptDetector: ZuptDetector = ZuptDetector(
      windowSize = 10,
      accelVarianceThreshold = 0.05f,
      gyroNormThreshold = 0.05f,
    )

    // Feed 15 samples of high acceleration and turning
    var detectedStationary: Boolean = false
    for (i in 0 until 15) {
      val sample: ImuSample = ImuSample(
        timestampNs = i * 10_000_000L,
        linearAcceleration = Vector3D(x = 1.2f, y = 2.5f, z = 0.4f),
        angularVelocity = Vector3D(x = 0.01f, y = 0.02f, z = 0.35f),
      )
      detectedStationary = zuptDetector.isStationary(sample = sample)
    }

    assertFalse(detectedStationary)
  }

  @Test
  fun testCalibrationEngineSmoothing() {
    val calibrationEngine: CalibrationEngine = CalibrationEngine(filterCutoffAlpha = 0.2f)

    val firstSample: ImuSample = ImuSample(
      timestampNs = 0L,
      linearAcceleration = Vector3D(x = 10f, y = 0f, z = 0f),
    )
    val processed1: ImuSample = calibrationEngine.process(rawSample = firstSample)
    assertEquals(10f, processed1.linearAcceleration.x, 0.01f)

    // Sudden spike should be attenuated by the 1st-order IIR filter
    val spikeSample: ImuSample = ImuSample(
      timestampNs = 10_000_000L,
      linearAcceleration = Vector3D(x = 20f, y = 0f, z = 0f),
    )
    val processed2: ImuSample = calibrationEngine.process(rawSample = spikeSample)
    // alpha * 20 + 0.8 * 10 = 4 + 8 = 12
    assertEquals(12f, processed2.linearAcceleration.x, 0.1f)
  }

  @Test
  fun testCoordinateTransformerIdentity() {
    val transformer: CoordinateTransformer = CoordinateTransformer()
    val vector: Vector3D = Vector3D(x = 1.0f, y = 2.0f, z = 3.0f)
    val identityQuat: Quaternion4D = Quaternion4D(w = 1f, x = 0f, y = 0f, z = 0f)

    val rotated: Vector3D = transformer.rotateBodyToNavigation(
      vector = vector,
      orientation = identityQuat,
    )

    assertEquals(1.0f, rotated.x, 1e-4f)
    assertEquals(2.0f, rotated.y, 1e-4f)
    assertEquals(3.0f, rotated.z, 1e-4f)
  }
}
