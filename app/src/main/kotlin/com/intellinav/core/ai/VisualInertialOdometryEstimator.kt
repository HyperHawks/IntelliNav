package com.intellinav.core.ai

import com.intellinav.core.model.Vector3D
import kotlin.math.sqrt

data class VisualOdometryUpdate(
  val forwardDisplacementMeters: Float,
  val yawChangeRad: Float,
  val opticalConfidence: Float,
)

/**
 * Visual-Inertial Fusion (VIO) secondary estimator as outlined in Slide 4.
 * Uses lightweight sparse optical flow from smartphone camera to constrain
 * dead-reckoning drift in extended long-distance tunnels (>1 km).
 */
class VisualInertialOdometryEstimator(
  private val frameWidth: Int = 640,
  private val frameHeight: Int = 480,
) {
  private var isCameraActive: Boolean = false
  private var accumulatedDistanceMeters: Float = 0f

  fun setEnabled(enabled: Boolean) {
    isCameraActive = enabled
  }

  fun isEnabled(): Boolean = isCameraActive

  fun processOpticalFlow(
    meanFlowX: Float,
    meanFlowY: Float,
    flowVariance: Float,
    dtSec: Float = 0.033f, // 30 FPS camera feed
  ): VisualOdometryUpdate {
    if (!isCameraActive) {
      return VisualOdometryUpdate(
        forwardDisplacementMeters = 0f,
        yawChangeRad = 0f,
        opticalConfidence = 0f,
      )
    }

    // Convert pixel motion into metric road displacement
    // In forward vehicle camera, vertical optical flow correlates with forward motion
    val focalLengthPixels: Float = 550f
    val cameraHeightMeters: Float = 1.2f

    val forwardSpeedEst: Float = (meanFlowY * cameraHeightMeters / focalLengthPixels) / dtSec
    val clampedSpeed: Float = forwardSpeedEst.coerceIn(minimumValue = 0f, maximumValue = 45f)
    val displacement: Float = clampedSpeed * dtSec
    accumulatedDistanceMeters += displacement

    val yawChange: Float = -(meanFlowX / focalLengthPixels)
    val confidence: Float = (1.0f / (1.0f + flowVariance)).coerceIn(minimumValue = 0.1f, maximumValue = 0.95f)

    return VisualOdometryUpdate(
      forwardDisplacementMeters = displacement,
      yawChangeRad = yawChange,
      opticalConfidence = confidence,
    )
  }

  fun reset() {
    accumulatedDistanceMeters = 0f
  }
}
