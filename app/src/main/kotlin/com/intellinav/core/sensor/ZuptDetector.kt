package com.intellinav.core.sensor

import com.intellinav.core.model.ImuSample
import kotlin.math.sqrt

class ZuptDetector(
  private val windowSize: Int = 15,
  private val accelVarianceThreshold: Float = 0.08f,
  private val gyroNormThreshold: Float = 0.06f,
) {
  private val accelWindow: ArrayDeque<Float> = ArrayDeque(windowSize)
  private val gyroWindow: ArrayDeque<Float> = ArrayDeque(windowSize)

  fun isStationary(sample: ImuSample): Boolean {
    val accelMag: Float = sample.linearAcceleration.magnitude()
    val gyroNorm: Float = sample.angularVelocity.magnitude()

    if (accelWindow.size >= windowSize) accelWindow.removeFirst()
    if (gyroWindow.size >= windowSize) gyroWindow.removeFirst()

    accelWindow.addLast(element = accelMag)
    gyroWindow.addLast(element = gyroNorm)

    if (accelWindow.size < windowSize) return false

    val accelVariance: Float = calculateVariance(values = accelWindow)
    val meanGyro: Float = gyroWindow.sum() / gyroWindow.size.toFloat()

    if (accelVariance < accelVarianceThreshold && meanGyro < gyroNormThreshold) return true
    return false
  }

  private fun calculateVariance(values: List<Float>): Float {
    if (values.isEmpty()) return 0f
    val mean: Float = values.sum() / values.size.toFloat()
    var sumSqDiff: Float = 0f
    for (v in values) {
      val diff: Float = v - mean
      sumSqDiff += (diff * diff)
    }
    return sumSqDiff / values.size.toFloat()
  }

  fun reset() {
    accelWindow.clear()
    gyroWindow.clear()
  }
}
