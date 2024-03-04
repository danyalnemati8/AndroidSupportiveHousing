package com.example.bluetoothapplication.ui.dashboard

class ESPData private constructor() {
    var lastDetected = 0
    var movementStatus: String? = null
    var isMotionDetected = false
    var isProximityDetected = false
    var isLightDetected = false
    var isRoomOccupied = false
        private set
    var lightIntensity = 0f
    var distance = 0
    var vibrationIntensity = 0f
    var vibrationBaseline = 0f
    var lightBaseline = 0f
    var proximityBaseline = 0
    var lightBaselineOff = 0f
    var lightingStatus: String? = null
    var connectedDevices = 0

    fun setRoomStatus(roomStatus: Boolean) {
        isRoomOccupied = roomStatus
    }

    val activeSensors: Int
        get() {
            var res = 0
            if (isMotionDetected) {
                res++
            }
            if (isProximityDetected) {
                res++
            }
            if (isLightDetected) {
                res++
            }
            return res
        }

    fun getRoomStatus(): String {
        return if (isRoomOccupied) "Occupied" else "Unoccupied"
    }

    companion object {
        val instance = ESPData()
    }
}
