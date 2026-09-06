package com.intellinav.core.sensor

import com.intellinav.core.model.Quaternion4D
import com.intellinav.core.model.Vector3D

class CoordinateTransformer {
  /**
   * Rotates a vector from phone body frame to global Earth navigation frame (East-North-Up)
   * using quaternion rotation: v' = q * v * q^-1
   */
  fun rotateBodyToNavigation(vector: Vector3D, orientation: Quaternion4D): Vector3D {
    val qw: Float = orientation.w
    val qx: Float = orientation.x
    val qy: Float = orientation.y
    val qz: Float = orientation.z

    // Compute rotation matrix elements from unit quaternion
    val r00: Float = 1.0f - 2.0f * (qy * qy + qz * qz)
    val r01: Float = 2.0f * (qx * qy - qz * qw)
    val r02: Float = 2.0f * (qx * qz + qy * qw)

    val r10: Float = 2.0f * (qx * qy + qz * qw)
    val r11: Float = 1.0f - 2.0f * (qx * qx + qz * qz)
    val r12: Float = 2.0f * (qy * qz - qx * qw)

    val r20: Float = 2.0f * (qx * qz - qy * qw)
    val r21: Float = 2.0f * (qy * qz + qx * qw)
    val r22: Float = 1.0f - 2.0f * (qx * qx + qy * qy)

    val rotatedX: Float = (r00 * vector.x) + (r01 * vector.y) + (r02 * vector.z)
    val rotatedY: Float = (r10 * vector.x) + (r11 * vector.y) + (r12 * vector.z)
    val rotatedZ: Float = (r20 * vector.x) + (r21 * vector.y) + (r22 * vector.z)

    return Vector3D(
      x = rotatedX,
      y = rotatedY,
      z = rotatedZ,
    )
  }
}
