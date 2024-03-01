package com.example.bluetoothapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid


@SuppressLint("MissingPermission")
class MyBluetoothDevice(device: BluetoothDevice?, uuids: MutableList<ParcelUuid?>?) {
    var device: BluetoothDevice? = null
    private var uuids: MutableList<ParcelUuid?>?

    init {
        this.device = device
        this.uuids = uuids
    }

    fun getUuids(): MutableList<ParcelUuid?>? {
        return uuids
    }

    fun setUuids(uuids: MutableList<ParcelUuid?>) {
        this.uuids = uuids
    }

    override fun equals(o: Any?): Boolean {
        val otherDevice = o as MyBluetoothDevice?
        return if (getUuids() == null || otherDevice!!.getUuids() == null) {
            device == otherDevice!!.device
        } else device == otherDevice.device && getUuids() != null && otherDevice.getUuids() != null && getUuids() == otherDevice.getUuids()
    }
}
