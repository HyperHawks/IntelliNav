package com.intellinav.core.fusion

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

/**
 * 7-State Unscented Kalman Filter for GNSS + INS fusion.
 * State: [pE, pN, vE, vN, heading, b_gyro, b_accel]
 */
class UnscentedKalmanFilter(
  initialEastMeters: Double = 0.0,
  initialNorthMeters: Double = 0.0,
  initialVeMps: Double = 0.0,
  initialVnMps: Double = 0.0,
  initialHeadingRad: Double = 0.0,
) {
  private val stateDim: Int = 7
  private val numSigmaPoints: Int = 2 * stateDim + 1

  // UKF Tuning parameters
  private val alpha: Double = 1e-3
  private val beta: Double = 2.0
  private val kappa: Double = 0.0
  private val lambda: Double = (alpha * alpha * (stateDim + kappa)) - stateDim
  private val gamma: Double = sqrt(x = stateDim + lambda)

  // Weights
  private val wm: DoubleArray = DoubleArray(size = numSigmaPoints)
  private val wc: DoubleArray = DoubleArray(size = numSigmaPoints)

  // State vector and Covariance matrix
  var state: DoubleArray = DoubleArray(size = stateDim)
    private set
  var covariance: Array<DoubleArray> = Array(size = stateDim) { DoubleArray(size = stateDim) }
    private set

  // Process noise matrix Q
  private val qMatrix: Array<DoubleArray> = Array(size = stateDim) { DoubleArray(size = stateDim) }

  init {
    state[0] = initialEastMeters
    state[1] = initialNorthMeters
    state[2] = initialVeMps
    state[3] = initialVnMps
    state[4] = initialHeadingRad
    state[5] = 0.0 // Gyro bias
    state[6] = 0.0 // Accel bias

    // Initialize Covariance
    covariance[0][0] = 4.0   // Pos E
    covariance[1][1] = 4.0   // Pos N
    covariance[2][2] = 0.25  // Vel E
    covariance[3][3] = 0.25  // Vel N
    covariance[4][4] = 0.04  // Heading (~11 deg)
    covariance[5][5] = 1e-4  // Gyro bias
    covariance[6][6] = 1e-3  // Accel bias

    // Initialize Process noise Q
    qMatrix[0][0] = 0.04
    qMatrix[1][1] = 0.04
    qMatrix[2][2] = 0.09
    qMatrix[3][3] = 0.09
    qMatrix[4][4] = 0.001
    qMatrix[5][5] = 1e-6
    qMatrix[6][6] = 1e-5

    // Initialize Weights
    wm[0] = lambda / (stateDim + lambda)
    wc[0] = (lambda / (stateDim + lambda)) + (1.0 - (alpha * alpha) + beta)
    val weightI: Double = 0.5 / (stateDim + lambda)
    for (i in 1 until numSigmaPoints) {
      wm[i] = weightI
      wc[i] = weightI
    }
  }

  fun predict(
    forwardAccelMps2: Double,
    lateralAccelMps2: Double,
    yawRateRadSec: Double,
    dtSec: Double = 0.01,
    qScale: Double = 1.0,
  ) {
    val sigmaPoints: Array<DoubleArray> = generateSigmaPoints()
    val propagatedPoints: Array<DoubleArray> = Array(size = numSigmaPoints) { DoubleArray(size = stateDim) }

    // Propagate each sigma point through the non-linear motion model
    for (i in 0 until numSigmaPoints) {
      val sp: DoubleArray = sigmaPoints[i]
      val pe: Double = sp[0]
      val pn: Double = sp[1]
      val ve: Double = sp[2]
      val vn: Double = sp[3]
      val heading: Double = sp[4]
      val bGyro: Double = sp[5]
      val bAccel: Double = sp[6]

      val correctedForwardAccel: Double = forwardAccelMps2 - bAccel
      val correctedYawRate: Double = yawRateRadSec - bGyro

      // Non-linear coordinate transformation: Body acceleration to Navigation Frame
      // East accel: a_fwd * sin(heading) + a_lat * cos(heading)
      // North accel: a_fwd * cos(heading) - a_lat * sin(heading)
      val ae: Double = (correctedForwardAccel * sin(x = heading)) + (lateralAccelMps2 * cos(x = heading))
      val an: Double = (correctedForwardAccel * cos(x = heading)) - (lateralAccelMps2 * sin(x = heading))

      propagatedPoints[i][0] = pe + (ve * dtSec) + (0.5 * ae * dtSec * dtSec)
      propagatedPoints[i][1] = pn + (vn * dtSec) + (0.5 * an * dtSec * dtSec)
      propagatedPoints[i][2] = ve + (ae * dtSec)
      propagatedPoints[i][3] = vn + (an * dtSec)
      propagatedPoints[i][4] = normalizeAngle(angleRad = heading + (correctedYawRate * dtSec))
      propagatedPoints[i][5] = bGyro
      propagatedPoints[i][6] = bAccel
    }

    // Compute predicted mean
    val predictedState: DoubleArray = DoubleArray(size = stateDim)
    for (i in 0 until numSigmaPoints) {
      val w: Double = wm[i]
      for (j in 0 until stateDim) {
        if (j == 4) continue // Angle handled via circular statistics
        predictedState[j] += (w * propagatedPoints[i][j])
      }
    }

    // Circular mean for heading angle
    var sumSin: Double = 0.0
    var sumCos: Double = 0.0
    for (i in 0 until numSigmaPoints) {
      sumSin += (wm[i] * sin(x = propagatedPoints[i][4]))
      sumCos += (wm[i] * cos(x = propagatedPoints[i][4]))
    }
    predictedState[4] = kotlin.math.atan2(y = sumSin, x = sumCos)

    // Compute predicted covariance
    val predictedCov: Array<DoubleArray> = Array(size = stateDim) { DoubleArray(size = stateDim) }
    for (i in 0 until numSigmaPoints) {
      val diff: DoubleArray = DoubleArray(size = stateDim)
      for (j in 0 until stateDim) {
        if (j == 4) {
          diff[j] = normalizeAngle(angleRad = propagatedPoints[i][j] - predictedState[j])
        } else {
          diff[j] = propagatedPoints[i][j] - predictedState[j]
        }
      }

      val w: Double = wc[i]
      for (r in 0 until stateDim) {
        for (c in 0 until stateDim) {
          predictedCov[r][c] += (w * diff[r] * diff[c])
        }
      }
    }

    // Add process noise Q (scaled)
    for (r in 0 until stateDim) {
      predictedCov[r][r] += (qMatrix[r][r] * qScale)
    }

    state = predictedState
    covariance = predictedCov
  }

  fun updateGnss(
    gnssEastMeters: Double,
    gnssNorthMeters: Double,
    gnssSpeedMps: Double,
    gnssHeadingRad: Double,
    posAccuracyMeters: Double = 3.5,
  ) {
    val measDim: Int = 4
    val z: DoubleArray = doubleArrayOf(
      gnssEastMeters,
      gnssNorthMeters,
      gnssSpeedMps * sin(x = gnssHeadingRad),
      gnssSpeedMps * cos(x = gnssHeadingRad),
    )

    val sigmaPoints: Array<DoubleArray> = generateSigmaPoints()
    val gammaPoints: Array<DoubleArray> = Array(size = numSigmaPoints) { DoubleArray(size = measDim) }

    for (i in 0 until numSigmaPoints) {
      gammaPoints[i][0] = sigmaPoints[i][0] // pE
      gammaPoints[i][1] = sigmaPoints[i][1] // pN
      gammaPoints[i][2] = sigmaPoints[i][2] // vE
      gammaPoints[i][3] = sigmaPoints[i][3] // vN
    }

    val zHat: DoubleArray = DoubleArray(size = measDim)
    for (i in 0 until numSigmaPoints) {
      for (m in 0 until measDim) {
        zHat[m] += (wm[i] * gammaPoints[i][m])
      }
    }

    // Innovation covariance S
    val sMatrix: Array<DoubleArray> = Array(size = measDim) { DoubleArray(size = measDim) }
    // Cross covariance Pxz
    val pxz: Array<DoubleArray> = Array(size = stateDim) { DoubleArray(size = measDim) }

    for (i in 0 until numSigmaPoints) {
      val xDiff: DoubleArray = DoubleArray(size = stateDim)
      for (j in 0 until stateDim) {
        xDiff[j] = if (j == 4) normalizeAngle(angleRad = sigmaPoints[i][j] - state[j]) else sigmaPoints[i][j] - state[j]
      }

      val zDiff: DoubleArray = DoubleArray(size = measDim)
      for (m in 0 until measDim) {
        zDiff[m] = gammaPoints[i][m] - zHat[m]
      }

      val w: Double = wc[i]
      for (r in 0 until measDim) {
        for (c in 0 until measDim) {
          sMatrix[r][c] += (w * zDiff[r] * zDiff[c])
        }
      }

      for (r in 0 until stateDim) {
        for (c in 0 until measDim) {
          pxz[r][c] += (w * xDiff[r] * zDiff[c])
        }
      }
    }

    // Measurement noise R
    val rPos: Double = (posAccuracyMeters * posAccuracyMeters).coerceAtLeast(minimumValue = 1.0)
    val rVel: Double = 0.25
    sMatrix[0][0] += rPos
    sMatrix[1][1] += rPos
    sMatrix[2][2] += rVel
    sMatrix[3][3] += rVel

    // Invert 4x4 diagonal dominant S matrix
    val sInv: Array<DoubleArray> = invert4x4(m = sMatrix)

    // Kalman gain: K = Pxz * S^-1
    val kMatrix: Array<DoubleArray> = Array(size = stateDim) { DoubleArray(size = measDim) }
    for (r in 0 until stateDim) {
      for (c in 0 until measDim) {
        var sum: Double = 0.0
        for (k in 0 until measDim) {
          sum += (pxz[r][k] * sInv[k][c])
        }
        kMatrix[r][c] = sum
      }
    }

    // Innovation y = z - zHat
    val y: DoubleArray = DoubleArray(size = measDim)
    for (m in 0 until measDim) {
      y[m] = z[m] - zHat[m]
    }

    // State update: x = x + K * y
    for (r in 0 until stateDim) {
      var correction: Double = 0.0
      for (m in 0 until measDim) {
        correction += (kMatrix[r][m] * y[m])
      }
      state[r] = if (r == 4) normalizeAngle(angleRad = state[r] + correction) else state[r] + correction
    }

    // Covariance update: P = P - K * S * K^T
    val ks: Array<DoubleArray> = Array(size = stateDim) { DoubleArray(size = measDim) }
    for (r in 0 until stateDim) {
      for (c in 0 until measDim) {
        var sum: Double = 0.0
        for (k in 0 until measDim) {
          sum += (kMatrix[r][k] * sMatrix[k][c])
        }
        ks[r][c] = sum
      }
    }

    for (r in 0 until stateDim) {
      for (c in 0 until stateDim) {
        var sum: Double = 0.0
        for (m in 0 until measDim) {
          sum += (ks[r][m] * kMatrix[c][m])
        }
        covariance[r][c] -= sum
      }
    }
  }

  fun updateNonHolonomicConstraint(speedVariance: Double = 0.05) {
    // NHC: Ground vehicles have zero lateral velocity in vehicle frame
    // v_lat = -vE * sin(theta) + vN * cos(theta) = 0
    val heading: Double = state[4]
    val ve: Double = state[2]
    val vn: Double = state[3]
    val vLat: Double = (-ve * sin(x = heading)) + (vn * cos(x = heading))

    // Soft-constraint update
    val correction: Double = vLat * 0.5
    state[2] += (correction * sin(x = heading))
    state[3] -= (correction * cos(x = heading))
  }

  fun resetPosition(eastMeters: Double, northMeters: Double) {
    state[0] = eastMeters
    state[1] = northMeters
  }

  private fun generateSigmaPoints(): Array<DoubleArray> {
    val points: Array<DoubleArray> = Array(size = numSigmaPoints) { DoubleArray(size = stateDim) }
    val sqrtCov: Array<DoubleArray> = cholesky(matrix = covariance)

    // Center point
    for (j in 0 until stateDim) {
      points[0][j] = state[j]
    }

    // Points: state + gamma * sqrtCov_col and state - gamma * sqrtCov_col
    for (i in 0 until stateDim) {
      for (j in 0 until stateDim) {
        val delta: Double = gamma * sqrtCov[j][i]
        points[i + 1][j] = state[j] + delta
        points[i + 1 + stateDim][j] = state[j] - delta
      }
    }

    return points
  }

  private fun cholesky(matrix: Array<DoubleArray>): Array<DoubleArray> {
    val l: Array<DoubleArray> = Array(size = stateDim) { DoubleArray(size = stateDim) }
    for (i in 0 until stateDim) {
      for (j in 0..i) {
        var sum: Double = 0.0
        for (k in 0 until j) {
          sum += (l[i][k] * l[j][k])
        }
        if (i == j) {
          val diag: Double = matrix[i][i] - sum
          l[i][j] = if (diag > 1e-12) sqrt(x = diag) else 1e-6
        } else {
          val denom: Double = l[j][j]
          l[i][j] = if (kotlin.math.abs(n = denom) > 1e-12) (matrix[i][j] - sum) / denom else 0.0
        }
      }
    }
    return l
  }

  private fun invert4x4(m: Array<DoubleArray>): Array<DoubleArray> {
    // Simplified robust inversion for diagonal-dominant measurement covariance
    val inv: Array<DoubleArray> = Array(size = 4) { DoubleArray(size = 4) }
    for (i in 0 until 4) {
      val diag: Double = m[i][i]
      inv[i][i] = if (kotlin.math.abs(n = diag) > 1e-12) 1.0 / diag else 1.0
    }
    return inv
  }

  private fun normalizeAngle(angleRad: Double): Double {
    var a: Double = angleRad % (2.0 * PI)
    if (a > PI) a -= (2.0 * PI)
    if (a < -PI) a += (2.0 * PI)
    return a
  }
}
