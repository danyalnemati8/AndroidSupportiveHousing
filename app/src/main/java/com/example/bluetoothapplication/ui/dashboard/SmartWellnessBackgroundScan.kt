package com.example.bluetoothapplication.ui.dashboard

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.bluetoothapplication.BluetoothGattHelper
import com.example.bluetoothapplication.R


class SmartWellnessBackgroundScan : Service() {


    //Caution: If you target Android 8.0 (API level 26) or higher and post a notification without
    // specifying a notification channel, the notification doesn't appear and the system logs an error.
    //https://developer.android.com/develop/ui/views/notifications/channels#kotlin"
    //https://www.geeksforgeeks.org/notifications-in-kotlin/

    private var SERVICE_NOTIFICATION_ID = "BACKGROUND_BLE_SCAN"
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothScanner : BluetoothLeScanner
    private lateinit var notification : Notification

    override fun onCreate() {
        super.onCreate()
        Log.i("BluetoothScan","Entered Oncreate")
        bluetoothAdapter = (this.getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter!!
        showNotification()
      }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext,"Launching Smart Alert Background Scan",Toast.LENGTH_SHORT).show()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(notification != null){
                startForeground(1,notification)
                beginScan()
            }
        else{
                Log.i("BluetoothScan","Something is fishy in SmartWellnessBackgroundScan")
                Toast.makeText(applicationContext,"Stopping Smart Alert Background Scan",Toast.LENGTH_SHORT).show()
            }
        }
        return START_STICKY
    }

    private fun beginScan() {
        bluetoothScanner = bluetoothAdapter.bluetoothLeScanner
        Toast.makeText(applicationContext,"Beginning ESP32 Scan",Toast.LENGTH_SHORT).show()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED){
            bluetoothScanner.startScan(scanCallBack)
          }
    }

    private val scanCallBack: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                if (device != null && device.name != null && device.name == "ESP32") {
                    Log.i("BluetoothScan",device.name.toString())
                    stopScan()
                    val bluetoothGattHelper = BluetoothGattHelper(device.name.toString(),"beb5483e-36e1-4688-b7f5-ea07361b26a8","4fafc201-1fb5-459e-8fcc-c5c9c331914b")
                    Log.i("BluetoothScan","Got the Object")
                    val gatt = device?.connectGatt(applicationContext,true,  bluetoothGattHelper.gattCallback)

                }
            }
        }
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(applicationContext, "Scan failed with error code: $errorCode", Toast.LENGTH_LONG).show()
        }
    }
    fun stopScan(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothScanner.stopScan(scanCallBack)
        }
    }

    private fun showNotification() {
        createNotificationChannel()
        var builder = NotificationCompat.Builder(this, SERVICE_NOTIFICATION_ID)
            .setSmallIcon(R.drawable.baseline_bluetooth_searching_24)
            .setContentTitle("Smart Medical Alert")
            .setContentText("Background Scan for Smart Medical Alert goes on in background")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notification = builder
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager != null) {
            notificationManager.notify(1, notification)
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Smart Wellness"
            val descriptionText = "Smart wellness notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(SERVICE_NOTIFICATION_ID, name, importance)
            mChannel.description = descriptionText
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
            }
        }

    override fun onBind(intent: Intent): IBinder {
            TODO("Return the communication channel to the service.")
        }

    override fun onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
            bluetoothScanner.stopScan(scanCallBack)
    }
}