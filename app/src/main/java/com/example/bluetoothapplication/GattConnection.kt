package com.example.bluetoothapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.util.Log
import android.widget.Toast
import org.json.JSONException
import java.nio.charset.StandardCharsets
import java.util.UUID

class GattConnection(val deviceName: String) {

//    val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
//    val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//    val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    //    BLANK
    val CHARACTERISTIC_UUID = "681F827F-D00E-4307-B77A-F38014D6CC5F"
    val SERVICE_UUID = "3BED005E-75B7-4DE6-B877-EAE81B0FC93F"
    val DESCRIPTOR_UUID = "013B54B2-5520-406A-87F5-D644AD3E0565"

    var deviceGattConn:BluetoothGatt ?= null
    var deviceService: BluetoothGattService?= null

    @SuppressLint("MissingPermission")
    val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.i("$deviceName GattConnection", "onConnectionStateChange invoked")
            Log.i("Device Status", newState.toString() + "")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("Device info", "Discovering bluetooth services of target device...")
                gatt.requestMtu(512)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("Device info", "Disconnecting bluetooth device...")
                gatt.disconnect()
                gatt.close()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.i("$deviceName GattConnection", "onMtuChanged invoked")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("MTU Request", "MTU request success")
                gatt.discoverServices()
            } else {
                Log.i("MTU Request", "MTU request failed")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.i("$deviceName GattConnection", "onCharacteristicChanged invoked")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.i("$deviceName GattConnection", "onServicesDiscovered invoked")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(UUID.fromString(SERVICE_UUID))
                deviceGattConn = gatt
                Log.i("Device info", "Successfully discovered services of target device")
                if (service != null) {
                    Log.i("Service status", "Service is not null.")
                    val discoveredCharacteristic =
                        service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                    if (discoveredCharacteristic != null) {
                        gatt.readCharacteristic(discoveredCharacteristic)
                        if (gatt.setCharacteristicNotification(discoveredCharacteristic, true)) {
                            Log.i("Set characteristic notification", "Success!")
                            Log.i(
                                "Characteristic property flags",
                                discoveredCharacteristic.properties.toString()
                            )
                        } else {
                            Log.i("Set characteristic notification", "Failure!")
                        }
                    } else {
                        Log.i("Characteristic info", "Characteristic not found!")
                    }
                } else {
                    Log.i("Service info", "Service not found!")
                }
            } else {
                Log.i("Service Discovery", "Service discovery failed")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, discoveredCharacteristic: BluetoothGattCharacteristic, status: Int) {

            Log.i("$deviceName GattConnection", "onCharacteristicRead invoked")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = discoveredCharacteristic.value
                val value = String(data, StandardCharsets.UTF_8)
                Log.i("Read data", "Received data: $value")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, discoveredCharacteristic: BluetoothGattCharacteristic, value : ByteArray, status: Int) {
            Log.i("$deviceName GattConnection", "onCharacteristicRead with byte array invoked")
            val value = String(value)
            Log.i("Read data", "Received data: $value")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = discoveredCharacteristic.value
                val value = String(data, StandardCharsets.UTF_8)
                Log.i("Read data", "Received data: $value")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.i("$deviceName GattConnection", "onCharacteristicWrite invoked")
            Log.i("$deviceName GattConnection", "onCharacteristicWrite invoked: " + characteristic.uuid)
//                val toString = characteristic.toString()
            val data = characteristic.value
            val info = gatt.readCharacteristic(characteristic)
            info.toString()
            val value = String(data)
            Log.i("Read data", "Received data: $value")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = characteristic.value
                val value = String(data, StandardCharsets.UTF_8)
                Log.i("Read data", "Received data: $value")

            }
        }
    }

    fun getDeviceGatt(): BluetoothGatt? {
        return deviceGattConn
    }

}