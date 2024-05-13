package com.example.bluetoothapplication.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.kotlinconversionsupportivehousing.IBackgroundScan
import com.example.kotlinconversionsupportivehousing.IRestartScan
import com.example.kotlinconversionsupportivehousing.helpers.NotificationHelper
import kotlin.reflect.typeOf


@SuppressLint("MissingPermission")
class BackgroundScan : Service(), IRestartScan {
    private var SERVICE_UUID: String? = null
    private var CHARACTERISTIC_UUID: String? = null
    private var notification: Notification? = null
    private val isGattConnected = false
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private val gatt: BluetoothGatt? = null
    private val mCharacteristic: BluetoothGattCharacteristic? = null
    private val mBinder: IBinder = MyBinder()
    private var callback: IBackgroundScan? = null
    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i("BACKGROUND SERVICE", "onScanResult running")
            super.onScanResult(callbackType, result)
            val device = result.device
            callback!!.onDeviceScan(result)
            if(device.name != null){
                Log.i("Scanned Devices", device.name)
            }
            if (device != null && device.name != null && device.name == "ESP32") {
                Log.i("BACKGROUND SERVICE", "scan stopped")
                scanner!!.stopScan(this)
                callback!!.onTargetDeviceFound(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(
                applicationContext,
                "Scan failed with error code: $errorCode",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    override fun restartScan() {
//        Toast.makeText(getApplicationContext(), "Resuming background scan in 2 minutes...", Toast.LENGTH_LONG).show();
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ scanner!!.startScan(scanCallback) }, 120000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(
            applicationContext,
            SERVICE_NOTIFICATION_ID,
            SERVICE_NOTIFICATION_NAME
        )
        notification = NotificationCompat.Builder(this, SERVICE_NOTIFICATION_ID)
            .setContentTitle("Smart Medical Alert")
            .setContentText("Ongoing BLE Scan")
            .build()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        Log.i("BACKGROUND SERVICE", "Background onCreate running")
    }

    private fun beginScan() {
        Log.i("BACKGROUND SERVICE", "Background beginScan running")
        scanner = bluetoothAdapter!!.bluetoothLeScanner
        scanner!!.flushPendingScanResults(scanCallback)
        Toast.makeText(applicationContext, "Beginning continuous scan...", Toast.LENGTH_LONG).show()
        scanner!!.startScan(scanCallback)
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent !=null){
            Toast.makeText(applicationContext, "Launching background Bluetooth scan...", Toast.LENGTH_LONG).show()
            SERVICE_UUID = intent.getStringExtra("service_uuid")
            CHARACTERISTIC_UUID = intent.getStringExtra("characteristic_uuid")
            if (notification != null) {
                startForeground(1, notification)
                // Start the continuous scan here
                beginScan()
            } else {
                Log.i("FOREGROUND SERVICE", "Error initializing notification object")
            }
            Log.i("BACKGROUND SERVICE", "Background onStartCommand running")
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        super.onUnbind(intent)
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.i("UNBIND SERVICE", "Success")
        return true
    }

    inner class MyBinder : Binder() {

        fun getInstance(): BackgroundScan? {
            return this@BackgroundScan
        }
    }

    fun registerClient(activity: Activity?) {
        callback = activity as IBackgroundScan?
    }

    fun stopServiceScan() {
        scanner!!.stopScan(scanCallback)
    }

    fun registerClient(fragment: HomeFragment?) {
        callback = fragment as IBackgroundScan?
    }

    companion object {
        var SERVICE_NOTIFICATION_ID = "BACKGROUND_BLE_SCAN"
        var SERVICE_NOTIFICATION_NAME = "ESP32 Continuous Scan"
    }
}
