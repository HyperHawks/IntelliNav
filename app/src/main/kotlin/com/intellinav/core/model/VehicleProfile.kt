package com.intellinav.core.model

enum class VehicleType {
  TWO_WHEELER,
  PASSENGER_CAR,
  TRUCK_FREIGHT,
  EMERGENCY_AMBULANCE,
}

data class VehicleProfile(
  val type: VehicleType,
  val name: String,
  val maxSpeedMps: Float,
  val maxTurnRateRadSec: Float,
  val maxAccelerationMps2: Float,
  val massKg: Float,
  val defaultProcessNoiseScale: Float,
  val nhcStrictness: Float,
  val description: String,
) {
  companion object {
    val TwoWheeler: VehicleProfile = VehicleProfile(
      type = VehicleType.TWO_WHEELER,
      name = "Two-Wheeler (Motorcycle/Scooter)",
      maxSpeedMps = 33.3f,
      maxTurnRateRadSec = 1.2f,
      maxAccelerationMps2 = 4.5f,
      massKg = 150f,
      defaultProcessNoiseScale = 1.5f,
      nhcStrictness = 0.85f,
      description = "Optimized for 260M+ riders in India. Agility-tolerant UKF with lean vibration suppression.",
    )

    val PassengerCar: VehicleProfile = VehicleProfile(
      type = VehicleType.PASSENGER_CAR,
      name = "Passenger Car",
      maxSpeedMps = 45.0f,
      maxTurnRateRadSec = 0.7f,
      maxAccelerationMps2 = 3.5f,
      massKg = 1400f,
      defaultProcessNoiseScale = 1.0f,
      nhcStrictness = 0.95f,
      description = "Standard 4-wheeler kinematics with balanced dead-reckoning filters.",
    )

    val TruckFreight: VehicleProfile = VehicleProfile(
      type = VehicleType.TRUCK_FREIGHT,
      name = "Commercial Freight / Truck",
      maxSpeedMps = 25.0f,
      maxTurnRateRadSec = 0.35f,
      maxAccelerationMps2 = 1.8f,
      massKg = 12000f,
      defaultProcessNoiseScale = 0.6f,
      nhcStrictness = 0.99f,
      description = "Heavy inertia logistics profile. Stiff non-holonomic constraint prevents false lateral drift.",
    )

    val EmergencyAmbulance: VehicleProfile = VehicleProfile(
      type = VehicleType.EMERGENCY_AMBULANCE,
      name = "Emergency Fleet / Ambulance",
      maxSpeedMps = 48.0f,
      maxTurnRateRadSec = 0.9f,
      maxAccelerationMps2 = 4.2f,
      massKg = 2800f,
      defaultProcessNoiseScale = 1.3f,
      nhcStrictness = 0.92f,
      description = "High-priority rapid transit profile for tunnels and dense urban canyon corridors.",
    )

    val DefaultProfiles: List<VehicleProfile> = listOf(
      TwoWheeler,
      PassengerCar,
      TruckFreight,
      EmergencyAmbulance,
    )
  }
}
