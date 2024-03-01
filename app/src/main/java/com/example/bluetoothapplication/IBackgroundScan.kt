package com.example.kotlinconversionsupportivehousing

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult


interface IBackgroundScan {
    fun onDeviceScan(result: ScanResult?)

    // Found ESP32
    fun onTargetDeviceFound(targetDevice: BluetoothDevice?)
}
