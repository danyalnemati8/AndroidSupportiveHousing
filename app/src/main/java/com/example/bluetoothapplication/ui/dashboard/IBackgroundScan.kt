package com.example.bluetoothapplication.ui.dashboard

import android.bluetooth.BluetoothDevice

interface IBackgroundScan {
        fun onTargetDeviceFound(device : BluetoothDevice);
}