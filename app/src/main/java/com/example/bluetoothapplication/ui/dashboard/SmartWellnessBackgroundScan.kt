package com.example.bluetoothapplication.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
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
    var callback : IBackgroundScan? = null
    private val mBinder: IBinder = MyBinder()


    override fun onCreate() {
        super.onCreate()
        Log.i("BluetoothScan","Entered Oncreate")
        bluetoothAdapter = (this.getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter!!
        showNotification()
      }


    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext,"Launching Smart Alert Background Scan",Toast.LENGTH_SHORT).show()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForeground(1,notification)
            beginScan()
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
                    callback?.onTargetDeviceFound(device)
                 }
            }
        }
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(applicationContext, "Scan failed with error code: $errorCode", Toast.LENGTH_LONG).show()
        }
    }
    fun stopScan(){
        Log.i("SmartWellnessBackgroundScan","Stop Scan Method")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothScanner.stopScan(scanCallBack)
        }
    }

    private fun showNotification() {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, SERVICE_NOTIFICATION_ID)
            .setSmallIcon(R.drawable.baseline_bluetooth_searching_24)
            .setContentTitle("Smart Medical Alert")
            .setContentText("Background Scan for Smart Medical Alert goes on in background")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notification = builder
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        super.onUnbind(intent)
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.i("UNBIND SERVICE", "Success")
        return true
    }
    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
            bluetoothScanner.stopScan(scanCallBack)
    }

     inner class MyBinder : Binder() {
        fun getService(): SmartWellnessBackgroundScan {
            return this@SmartWellnessBackgroundScan
        }
    }


    fun registerClient(fragment: Fragment) {
        if (fragment is DashboardFragment) {
            callback = fragment
        }
    }

    fun stopServiceScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothScanner.stopScan(scanCallBack)
        }
    }
}