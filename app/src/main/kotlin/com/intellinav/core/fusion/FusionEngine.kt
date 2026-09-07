package com.intellinav.core.fusion

import com.intellinav.core.ai.AiKinematicEstimator
import com.intellinav.core.ai.KinematicInferenceResult
import com.intellinav.core.model.GeoPoint
import com.intellinav.core.model.GnssFix
import com.intellinav.core.model.ImuSample
import com.intellinav.core.model.NavigationMode
import com.intellinav.core.model.NavigationState
import com.intellinav.core.model.Position2D
import com.intellinav.core.model.VehicleProfile
import com.intellinav.core.model.Velocity2D
import com.intellinav.core.sensor.CalibrationEngine
import com.intellinav.core.sensor.CoordinateTransformer
import com.intellinav.core.sensor.ZuptDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.sqrt

class FusionEngine(
  private var vehicleProfile: VehicleProfile = VehicleProfile.TwoWheeler,
  private val referenceOrigin: GeoPoint = GeoPoint(latitude = 19.0760, longitude = 72.8777),
) {
  private val calibrationEngine: CalibrationEngine = CalibrationEngine()
  private val zuptDetector: ZuptDetector = ZuptDetector()
  private val transformer: CoordinateTransformer = CoordinateTransformer()
  private var aiEstimator: AiKinematicEstimator = AiKinematicEstimator(vehicleProfile = vehicleProfile)
  private val ukf: UnscentedKalmanFilter = UnscentedKalmanFilter()

  private val _navigationState: MutableStateFlow<NavigationState> = MutableStateFlow(
    value = NavigationState(
      geoPoint = referenceOrigin,
      mode = NavigationMode.GNSS_FUSED,
    ),
  )
  val navigationState: Flow<NavigationState> = _navigationState.asStateFlow()

  private var blackoutStartTimeMs: Long = 0L
  private var isCurrentlyInBlackout: Boolean = false
  private var lastImuTimestampNs: Long = 0L

  fun setVehicleProfile(profile: VehicleProfile) {
    vehicleProfile = profile
    aiEstimator = AiKinematicEstimator(vehicleProfile = profile)
  }

  fun processImu(sample: ImuSample) {
    val dtSec: Double = if (lastImuTimestampNs == 0L) {
      0.01
    } else {
      ((sample.timestampNs - lastImuTimestampNs) / 1e9).coerceIn(minimumValue = 0.001, maximumValue = 0.1)
    }
    lastImuTimestampNs = sample.timestampNs

    // 1. Calibration and vibration filter
    val calibratedSample: ImuSample = calibrationEngine.process(rawSample = sample)

    // 2. Stationary ZUPT detection
    val isStationary: Boolean = zuptDetector.isStationary(sample = calibratedSample)
    if (isStationary) {
      calibrationEngine.updateStationaryBiases(
        accel = calibratedSample.linearAcceleration,
        gyro = calibratedSample.angularVelocity,
      )
    }

    // 3. AI Kinematic Inference (speed & acceleration from IMU without OBD-II)
    val inference: KinematicInferenceResult = aiEstimator.infer(
      sample = calibratedSample.copy(isStationary = isStationary),
      dtSec = dtSec.toFloat(),
    )

    // 4. UKF Prediction step
    val forwardAccel: Double = inference.estimatedForwardAccelMps2.toDouble()
    val lateralAccel: Double = calibratedSample.linearAcceleration.x.toDouble()
    val yawRate: Double = calibratedSample.angularVelocity.z.toDouble()

    ukf.predict(
      forwardAccelMps2 = forwardAccel,
      lateralAccelMps2 = lateralAccel,
      yawRateRadSec = yawRate,
      dtSec = dtSec,
      qScale = inference.processNoiseScale.toDouble(),
    )

    // 5. Non-holonomic constraint enforcement (zero lateral velocity)
    ukf.updateNonHolonomicConstraint()

    // 6. Build current navigation state
    val ukfState: DoubleArray = ukf.state
    val eastMeters: Double = ukfState[0]
    val northMeters: Double = ukfState[1]
    val currentGeo: GeoPoint = referenceOrigin.offsetMeters(eastMeters = eastMeters, northMeters = northMeters)
    val headingRad: Double = ukfState[4]
    val headingDeg: Float = (((headingRad * 180.0 / PI) + 360.0) % 360.0).toFloat()

    val traceCov: Double = (ukf.covariance[0][0] + ukf.covariance[1][1]).coerceAtLeast(minimumValue = 0.1)
    val confRadius: Float = sqrt(x = traceCov).toFloat()

    val currentMode: NavigationMode = when {
      isStationary -> NavigationMode.STATIONARY_ZUPT
      isCurrentlyInBlackout -> NavigationMode.DEAD_RECKONING_TUNNEL
      confRadius > 15f -> NavigationMode.DEGRADED_URBAN_CANYON
      else -> NavigationMode.GNSS_FUSED
    }

    val blackoutElapsedMs: Long = if (isCurrentlyInBlackout) {
      System.currentTimeMillis() - blackoutStartTimeMs
    } else {
      0L
    }

    _navigationState.value = NavigationState(
      timestampNs = sample.timestampNs,
      geoPoint = currentGeo,
      localPosition = Position2D(eastMeters = eastMeters, northMeters = northMeters),
      velocity = Velocity2D(vx = ukfState[2], vy = ukfState[3]),
      headingDegrees = headingDeg,
      mode = currentMode,
      confidenceRadiusMeters = confRadius,
      timeInBlackoutMs = blackoutElapsedMs,
      forwardAccelerationMps2 = inference.estimatedForwardAccelMps2,
    )
  }

  fun processGnss(fix: GnssFix) {
    if (!fix.isValid) {
      // GNSS Blackout detected!
      if (!isCurrentlyInBlackout) {
        isCurrentlyInBlackout = true
        blackoutStartTimeMs = System.currentTimeMillis()
      }
      return
    }

    // GNSS is healthy
    if (isCurrentlyInBlackout) {
      // Reacquisition!
      isCurrentlyInBlackout = false
      blackoutStartTimeMs = 0L
    }

    // Convert GNSS lat/lon to local meters relative to reference origin
    val distNorth: Double = (fix.latitude - referenceOrigin.latitude) * 111319.9
    val distEast: Double = (fix.longitude - referenceOrigin.longitude) * (111319.9 * kotlin.math.cos(x = referenceOrigin.latitude * PI / 180.0))
    val headingRad: Double = fix.bearingDegrees * PI / 180.0

    ukf.updateGnss(
      gnssEastMeters = distEast,
      gnssNorthMeters = distNorth,
      gnssSpeedMps = fix.speedMps.toDouble(),
      gnssHeadingRad = headingRad,
      posAccuracyMeters = fix.horizontalAccuracyMeters.toDouble(),
    )

    aiEstimator.syncWithGnssSpeed(speedMps = fix.speedMps)
  }

  fun getUkf(): UnscentedKalmanFilter = ukf
}
// Sensor Fusion UKF Update
