package com.example.bluetoothapplication

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bluetoothapplication.databinding.ActivityMainBinding
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.widget.Toast
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.bluetoothapplication.ui.dashboard.DashboardViewModel
import com.example.kotlinconversionsupportivehousing.IBackgroundScan
import com.example.kotlinconversionsupportivehousing.IPillDispenser
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), IBackgroundScan {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notGrantedPermissions : List<String>
    //private val bluetoothGattHelper = BluetoothGattHelper()
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    val dashboardViewModel: DashboardViewModel by viewModels()



    //    ESP-01 UUIDs
//    val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8a07361b26a8"
//    val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//    val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    // ESP-02 UUIDs
//    val CHARACTERISTIC_UUID = "83755cbd-e485-4153-ac8b-ce260afd3697"
//    val SERVICE_UUID = "adee10c3-91dd-43aa-ab9b-052eb63d456c"
//    val DESCRIPTOR_UUID = "4d6ec567-93f0-4541-8152-81b35dc5cb8b"

    //    BLANK
    val CHARACTERISTIC_UUID = "681F827F-D00E-4307-B77A-F38014D6CC5F"
    val SERVICE_UUID = "3BED005E-75B7-4DE6-B877-EAE81B0FC93F"
    val DESCRIPTOR_UUID = "013B54B2-5520-406A-87F5-D644AD3E0565"

//    Pill Dispenser
//    val CHARACTERISTIC_UUID = "B3E39CF1-B4D5-4F0A-88DE-6EDE9ABE2BD2"
//    val SERVICE_UUID = "B3E39CF0-B4D5-4F0A-88DE-6EDE9ABE2BD2"
//    val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    // Pill Dispenser Buttons
    lateinit var pillDispinserService: Button
    var initiateBackgroundScan = false

    // 7 Day Scheduler Variables
    lateinit var setSunday: Button
    lateinit var setMonday: Button
    lateinit var setTuesday: Button
    lateinit var setWednesday: Button
    lateinit var setThursday: Button
    lateinit var setFriday: Button
    lateinit var setSaturday: Button
    var hour = 0;
    var minute:Int = 0
    var schedule = mutableMapOf("Sunday" to "N/A", "Monday" to "N/A", "Tuesday" to "N/A", "Wednesday" to "N/A", "Thursday" to "N/A", "Friday" to "N/A", "Saturday" to "N/A")

    // Bluetooth Gatt Variablesison
    var deviceGatt: BluetoothGatt?= null
    var deviceService: BluetoothGattService?= null
    val pillDispenserGatt: GattConnection = GattConnection("Pill Dispenser")
    val smartWellnessGatt: GattConnection = GattConnection("Smart Wellness")

    // Background Scan
    private var startBackgroundScan: Intent? = null
    private var serviceInstance: BackgroundScan? = null
    val adapter = BluetoothAdapter.getDefaultAdapter()
    val bluetoothScanner = adapter.bluetoothLeScanner
    val serviceScannedDevices: MutableList<MyBluetoothDevice> = mutableListOf()
    val deviceNameMapping: MutableMap<String, MutableList<MyBluetoothDevice>> = mutableMapOf()

    var REQUEST_BLUETOOTH_SCAN = 3
    val permissionList = listOf<String>(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS)

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: BackgroundScan.MyBinder = service as BackgroundScan.MyBinder
            serviceInstance = binder.getInstance()
            serviceInstance?.registerClient(this@MainActivity)
            Log.i("SERVICE BIND", "Success")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i("SERVICE BIND", "Disconnected")
        }
    }


    @SuppressLint("MissingPermission")
    fun sendToDevice(message: String){

        Log.i("Send Data", "callback function was triggered")
        val s = message
        val charsetName = "UTF-16"
        val byteArray = s.toByteArray(StandardCharsets.UTF_8)

        deviceGatt = pillDispenserGatt.getDeviceGatt()
        deviceService = deviceGatt?.getService(UUID.fromString(SERVICE_UUID))

        if (deviceService != null) {
            val example: BluetoothGattCharacteristic = deviceService!!.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))

            if (example != null) {
                Log.i("Permission Value", example.permissions.toString() + "")
                example.value = byteArray
                deviceGatt?.writeCharacteristic(example)
                Log.i("Send Data", "the data was sent!")
            }
        }
    }

    fun setTime(day: String) {
        val onTimeSetListener =
            TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                schedule[day] = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                Log.i("Schedule", schedule.toString())
                sendToDevice(schedule.toString())
            }
        val style = android.R.style.Theme_Holo_Light_Dialog_NoActionBar
        val timePickerDialog =
            TimePickerDialog(this, style, onTimeSetListener, hour, minute, true)
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    private fun launchBackgroundScan() {
        Log.i("launchBackgroundScan", "launchBackgroundScan currently running")
        startBackgroundScan = android.content.Intent(this, BackgroundScan::class.java)
        startBackgroundScan!!.putExtra("service_uuid", SERVICE_UUID)
        startBackgroundScan!!.putExtra("characteristic_uuid", CHARACTERISTIC_UUID)
        startService(startBackgroundScan)
        bindService(startBackgroundScan!!, mConnection, BIND_AUTO_CREATE)
    }

    override fun onDeviceScan(result: android.bluetooth.le.ScanResult?) {
        var device: BluetoothDevice? = result?.getDevice()
        var uuids: kotlin.collections.MutableList<ParcelUuid?>? =
            result?.getScanRecord()?.getServiceUuids()
        addDeviceToList(device, uuids)
    }

    @SuppressLint("MissingPermission")
    open fun addDeviceToList(device: BluetoothDevice?, uuids: kotlin.collections.MutableList<ParcelUuid?>?) {
        if (((device != null) && (device.getName() != null) && !device.getName().isEmpty())) {
            if (!serviceScannedDevices.contains(MyBluetoothDevice(device, uuids))) {
                if ((device.getName() == "Pill Dispenser")) {
                    serviceScannedDevices?.add(0, MyBluetoothDevice(device, uuids))
                } else {
                    serviceScannedDevices?.add(MyBluetoothDevice(device, uuids))
                }
                if (deviceNameMapping.containsKey(device.getName())) {
                    deviceNameMapping?.get(device.getName())?.add(MyBluetoothDevice(device, uuids))
                } else {
                    val devicesList: MutableList<MyBluetoothDevice> = mutableListOf()
                    devicesList?.add(MyBluetoothDevice(device, uuids))
                    deviceNameMapping?.put(device.getName().uppercase(java.util.Locale.getDefault()), devicesList)
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onTargetDeviceFound(device: BluetoothDevice?) {
        Log.i("BACKGROUND SERVICE", "onTargetDeviceFound running")
        val gatt = device?.connectGatt(this, false, pillDispenserGatt.gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        scanForBluetoothWithPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 123){
            BluetoothPermissionResult.launch(notGrantedPermissions.toTypedArray())
        }else{
            Toast.makeText(this, "Bluetooth and location not permission granted", Toast.LENGTH_SHORT).show()
        }
    }
    private fun scanForBluetoothWithPermissions() {
        Log.i("BluetoothScan", "Checking for permissions")
        notGrantedPermissions = permissionList.filterNot { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        Log.i("BluetoothScan", notGrantedPermissions.toString())
        if (notGrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), 123)
            // displayRequestPermissionToastMessage()
        } else {
            Toast.makeText(this, "Bluetooth and location permission granted", Toast.LENGTH_SHORT).show()
            enableBluetoothandLocation()
        }
    }
        private val BluetoothPermissionResult=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissionMap->
            if (permissionMap.all { it.value }){
                Toast.makeText(this, "Bluetooth and Loca permissions granted", Toast.LENGTH_SHORT).show()
                enableBluetoothandLocation()
            }else{
                Toast.makeText(this, "Media permissions not granted!", Toast.LENGTH_SHORT).show()
            }
        }

        @SuppressLint("MissingPermission")
        private fun enableBluetoothandLocation() {
            Log.i("BluetoothScan","Now actually Enabling BluetoothScan")
            val bluetoothAdapter: BluetoothAdapter? = (this.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
             if(bluetoothAdapter != null) {
                Log.i("BluetoothScan","Got Bluetooth Adapter")
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 123)
            }
        }
    }