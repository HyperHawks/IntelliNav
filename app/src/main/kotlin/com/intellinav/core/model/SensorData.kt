package com.intellinav.core.model

import kotlin.math.atan2
import kotlin.math.asin
import kotlin.math.PI

data class Vector3D(
  val x: Float = 0f,
  val y: Float = 0f,
  val z: Float = 0f,
) {
  fun magnitude(): Float {
    val sumOfSquares: Float = (x * x) + (y * y) + (z * z)
    return kotlin.math.sqrt(x = sumOfSquares)
  }

  fun normalized(): Vector3D {
    val mag: Float = magnitude()
    if (mag <= 1e-6f) return Vector3D(x = 0f, y = 0f, z = 0f)
    return Vector3D(
      x = x / mag,
      y = y / mag,
      z = z / mag,
    )
  }
}

data class Quaternion4D(
  val w: Float = 1f,
  val x: Float = 0f,
  val y: Float = 0f,
  val z: Float = 0f,
) {
  fun toEulerAngles(): EulerAngles {
    val sinrCosp: Float = 2.0f * (w * x + y * z)
    val cosrCosp: Float = 1.0f - 2.0f * (x * x + y * y)
    val roll: Float = atan2(y = sinrCosp, x = cosrCosp)

    val sinp: Float = 2.0f * (w * y - z * x)
    val pitch: Float = if (kotlin.math.abs(n = sinp) >= 1f) {
      val sign: Float = kotlin.math.sign(x = sinp)
      (sign * (PI / 2.0)).toFloat()
    } else {
      asin(x = sinp)
    }

    val sinyCosp: Float = 2.0f * (w * z + x * y)
    val cosyCosp: Float = 1.0f - 2.0f * (y * y + z * z)
    val yaw: Float = atan2(y = sinyCosp, x = cosyCosp)

    val rollDeg: Float = (roll * 180.0f / PI.toFloat())
    val pitchDeg: Float = (pitch * 180.0f / PI.toFloat())
    val yawDeg: Float = ((yaw * 180.0f / PI.toFloat()) + 360.0f) % 360.0f

    return EulerAngles(
      rollDegrees = rollDeg,
      pitchDegrees = pitchDeg,
      yawDegrees = yawDeg,
    )
  }
}

data class EulerAngles(
  val rollDegrees: Float = 0f,
  val pitchDegrees: Float = 0f,
  val yawDegrees: Float = 0f,
)

data class ImuSample(
  val timestampNs: Long,
  val linearAcceleration: Vector3D = Vector3D(),
  val angularVelocity: Vector3D = Vector3D(),
  val magneticField: Vector3D = Vector3D(),
  val orientation: Quaternion4D = Quaternion4D(),
  val isStationary: Boolean = false,
)
