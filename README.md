# IntelliNav: AI-Powered Dead Reckoning & GNSS Fusion Engine

[![Smart India Hackathon 2026](https://img.shields.io/badge/SIH-2026-blue.svg)](https://sih.gov.in)
[![TEKATHON 5.0](https://img.shields.io/badge/TEKATHON-5.0--IntelliNav-orange.svg)]()
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Edge%20C%2B%2B-green.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202024.12.01-4285F4.svg)]()

> A lightweight, edge-deployable software engine and companion mobile app that turns a smartphone's IMU (accelerometer, gyroscope, magnetometer) into a self-contained inertial navigation system, switching to dead reckoning within milliseconds of a GNSS blackout.

---

## 🚀 Overview

In dense urban canyons, tunnels, underpasses, and flyovers across India, standard GNSS/GPS navigation frequently drops or freezes. For **260M+ two-wheeler riders**, logistics freight corridors, and emergency ambulance fleets, this leads to missed exits, dangerous last-second lane changes, and delayed mission-critical dispatches.

**IntelliNav** solves this with zero extra hardware:
- **No OBD-II or dedicated INS hardware required**: Uses the IMU already present in any modern Android smartphone.
- **AI-Driven Kinematic Estimator**: Deep neural kinematic estimator predicting vehicle speed and acceleration directly from noisy 100 Hz IMU signals.
- **7-State Unscented Kalman Filter (UKF)**: Non-linear sensor fusion of GNSS fixes, inertial dead reckoning, and dynamically tuned process noise covariances.
- **Offline Map Matching (HMM + Viterbi)**: Snaps drifting trajectories onto local OpenStreetMap (OSM) graphs with Non-Holonomic Constraints (NHC) enforcing zero lateral velocity ($v_{lat} \approx 0$).
- **Sub-20ms Instant Handover**: Millisecond-level transition to dead reckoning upon GNSS blackout, with seamless reacquisition when satellite signals recover.

---

## 🏛️ System Architecture

```
[Smartphone IMU (100 Hz)] ---> [AI Noise & Bias Filter] ---> [GNSS + INS Fusion (UKF)] ---> [Map Matching (OSM + NHC)] ---> [Continuous Position Output]
                                                                        ^
[Standard GNSS / NMEA] -------------------------------------------------+
```

### 5-Stage Processing Pipeline:
1. **Data Acquisition**: 100 Hz synchronized sampler across Accelerometer, Gyroscope, Magnetometer, and GNSS streams.
2. **Calibration & ZUPT**: 1st-order IIR Butterworth noise filtering, per-device bias estimation, and Zero-Velocity Update (ZUPT) detection when stationary.
3. **AI Estimator**: Neural kinematic model predicting forward velocity and adaptive measurement covariance $R_k$.
4. **UKF Fusion**: 7-state Unscented Kalman Filter ($\mathbf{x} = [p_E, p_N, v_E, v_N, \theta, b_\omega, b_a]^T$) using Merwe Scaled Unscented Transformation.
5. **Map Matching Engine**: Offline OSM road network graph using Hidden Markov Model (HMM) and Viterbi decoding with Non-Holonomic Constraints.

---

## 🔬 Research & References

1. **Onyekpe, Palade, Kanarachos & Szkolnik**, *"Inertial and Odometry Benchmark Dataset for Ground Vehicle Positioning,"* Data in Brief, 2021 (IO-VNBD dataset).
2. **Brossard, Barrau & Bonnabel**, *"AI-IMU Dead-Reckoning,"* IEEE Transactions on Intelligent Vehicles, 2020.
3. **OpenStreetMap (OSM)**: Offline road network topological graph parser.
4. **u-blox Technical Note**: Multipath mitigation and urban-canyon GNSS accuracy benchmark.
5. **FilterPy**: Unscented Kalman Filter reference implementation.
6. **Hidden Markov Model (HMM)** map matching literature with Viterbi decoding.

---

## 📱 Tech Stack

- **Language**: Kotlin 2.1.0 (Strict Google Internal Style, 2-space indentation, explicit typing, member imports)
- **UI Framework**: Jetpack Compose (BOM 2024.12.01) + Material3
- **Async & Concurrency**: Kotlin Coroutines & Flows
- **Architecture**: Clean Architecture + MVI/MVVM

---

## 👥 Hackathon Team
- **Smart India Hackathon 2026**
- **Handle**: `@TEKATHON-5.0-IntelliNav`

## 👨‍💻 Contributors
- **Aryan Saini** ([@thecannycodes](https://github.com/thecannycodes)) - Lead Developer / AI Integration
