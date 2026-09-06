package com.intellinav.core.sensor

import com.intellinav.core.model.ImuSample
import com.intellinav.core.model.Quaternion4D
import com.intellinav.core.model.Vector3D
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

class MockSensorProvider {
  private var currentSpeedMps: Float = 16.6f // ~60 km/h
  private var currentHeadingRad: Float = 0f
  private var stepCount: Long = 0L

  fun generateSample(
    targetSpeedMps: Float = 16.6f,
    turnRateRadSec: Float = 0f,
    isStop: Boolean = false,
  ): ImuSample {
    stepCount++
    val dtSec: Float = 0.01f // 100 Hz = 10 ms step

    if (isStop) {
      currentSpeedMps = 0f
      val vibrationNoise: Float = (Random.nextFloat() - 0.5f) * 0.02f
      return ImuSample(
        timestampNs = System.nanoTime(),
        linearAcceleration = Vector3D(x = vibrationNoise, y = vibrationNoise, z = vibrationNoise),
        angularVelocity = Vector3D(x = vibrationNoise * 0.5f, y = vibrationNoise * 0.5f, z = vibrationNoise * 0.5f),
        orientation = eulerToQuaternion(yawRad = currentHeadingRad, pitchRad = 0f, rollRad = 0f),
        isStationary = true,
      )
    }

    // Smooth acceleration toward target speed
    val speedDiff: Float = targetSpeedMps - currentSpeedMps
    val forwardAccel: Float = (speedDiff * 0.5f).coerceIn(minimumValue = -4.0f, maximumValue = 3.0f)
    currentSpeedMps += (forwardAccel * dtSec)

    // Heading update
    currentHeadingRad += (turnRateRadSec * dtSec)

    // Lateral centripetal acceleration: a_lat = v * omega
    val lateralAccel: Float = currentSpeedMps * turnRateRadSec

    // Add realistic high-frequency engine/road vibration
    val engineVibrationX: Float = (Random.nextFloat() - 0.5f) * 0.15f
    val engineVibrationY: Float = (Random.nextFloat() - 0.5f) * 0.15f
    val engineVibrationZ: Float = (Random.nextFloat() - 0.5f) * 0.20f

    val accelBody: Vector3D = Vector3D(
      x = lateralAccel + engineVibrationX,
      y = forwardAccel + engineVibrationY,
      z = engineVibrationZ,
    )

    val gyroBody: Vector3D = Vector3D(
      x = (Random.nextFloat() - 0.5f) * 0.02f,
      y = (Random.nextFloat() - 0.5f) * 0.02f,
      z = turnRateRadSec + (Random.nextFloat() - 0.5f) * 0.01f,
    )

    return ImuSample(
      timestampNs = System.nanoTime(),
      linearAcceleration = accelBody,
      angularVelocity = gyroBody,
      orientation = eulerToQuaternion(yawRad = currentHeadingRad, pitchRad = 0f, rollRad = 0f),
      isStationary = false,
    )
  }

  private fun eulerToQuaternion(yawRad: Float, pitchRad: Float, rollRad: Float): Quaternion4D {
    val cy: Float = cos(x = yawRad * 0.5f)
    val sy: Float = sin(x = yawRad * 0.5f)
    val cp: Float = cos(x = pitchRad * 0.5f)
    val sp: Float = sin(x = pitchRad * 0.5f)
    val cr: Float = cos(x = rollRad * 0.5f)
    val sr: Float = sin(x = rollRad * 0.5f)

    val qw: Float = (cr * cp * cy) + (sr * sp * sy)
    val qx: Float = (sr * cp * cy) - (cr * sp * sy)
    val qy: Float = (cr * sp * cy) + (sr * cp * sy)
    val qz: Float = (cr * cp * sy) - (sr * sp * cy)

    return Quaternion4D(
      w = qw,
      x = qx,
      y = qy,
      z = qz,
    )
  }

  fun getCurrentSpeedMps(): Float = currentSpeedMps
  fun getCurrentHeadingRad(): Float = currentHeadingRad
}
