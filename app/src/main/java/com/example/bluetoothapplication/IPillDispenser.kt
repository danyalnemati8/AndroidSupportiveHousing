package com.example.kotlinconversionsupportivehousing

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt

interface IPillDispenser {

    fun sendData(gatt: BluetoothGatt)

}