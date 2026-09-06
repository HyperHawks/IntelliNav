package com.intellinav.core.sensor

import com.intellinav.core.model.ImuSample
import com.intellinav.core.model.Vector3D

class CalibrationEngine(
  private val filterCutoffAlpha: Float = 0.25f,
) {
  private var filteredAccel: Vector3D = Vector3D()
  private var filteredGyro: Vector3D = Vector3D()
  private var accelBias: Vector3D = Vector3D()
  private var gyroBias: Vector3D = Vector3D()
  private var isInitialized: Boolean = false

  fun process(rawSample: ImuSample): ImuSample {
    if (!isInitialized) {
      filteredAccel = rawSample.linearAcceleration
      filteredGyro = rawSample.angularVelocity
      isInitialized = true
      return rawSample
    }

    // 1st-order IIR low-pass filter: y[k] = alpha * x[k] + (1 - alpha) * y[k-1]
    filteredAccel = Vector3D(
      x = (filterCutoffAlpha * rawSample.linearAcceleration.x) + ((1f - filterCutoffAlpha) * filteredAccel.x),
      y = (filterCutoffAlpha * rawSample.linearAcceleration.y) + ((1f - filterCutoffAlpha) * filteredAccel.y),
      z = (filterCutoffAlpha * rawSample.linearAcceleration.z) + ((1f - filterCutoffAlpha) * filteredAccel.z),
    )

    filteredGyro = Vector3D(
      x = (filterCutoffAlpha * rawSample.angularVelocity.x) + ((1f - filterCutoffAlpha) * filteredGyro.x),
      y = (filterCutoffAlpha * rawSample.angularVelocity.y) + ((1f - filterCutoffAlpha) * filteredGyro.y),
      z = (filterCutoffAlpha * rawSample.angularVelocity.z) + ((1f - filterCutoffAlpha) * filteredGyro.z),
    )

    // Bias-corrected output
    val calibratedAccel: Vector3D = Vector3D(
      x = filteredAccel.x - accelBias.x,
      y = filteredAccel.y - accelBias.y,
      z = filteredAccel.z - accelBias.z,
    )

    val calibratedGyro: Vector3D = Vector3D(
      x = filteredGyro.x - gyroBias.x,
      y = filteredGyro.y - gyroBias.y,
      z = filteredGyro.z - gyroBias.z,
    )

    return rawSample.copy(
      linearAcceleration = calibratedAccel,
      angularVelocity = calibratedGyro,
    )
  }

  fun updateStationaryBiases(accel: Vector3D, gyro: Vector3D, learningRate: Float = 0.05f) {
    gyroBias = Vector3D(
      x = gyroBias.x + (learningRate * (gyro.x - gyroBias.x)),
      y = gyroBias.y + (learningRate * (gyro.y - gyroBias.y)),
      z = gyroBias.z + (learningRate * (gyro.z - gyroBias.z)),
    )
    accelBias = Vector3D(
      x = accelBias.x + (learningRate * (accel.x - accelBias.x)),
      y = accelBias.y + (learningRate * (accel.y - accelBias.y)),
      z = accelBias.z + (learningRate * (accel.z - accelBias.z)),
    )
  }

  fun getGyroBias(): Vector3D = gyroBias

  fun getAccelBias(): Vector3D = accelBias
}
