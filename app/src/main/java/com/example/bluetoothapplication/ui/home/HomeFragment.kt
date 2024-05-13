package com.example.bluetoothapplication.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bluetoothapplication.databinding.FragmentHomeBinding
import androidx.appcompat.app.*
import com.example.bluetoothapplication.R
import android.app.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.app.ActivityCompat
//import com.example.bluetoothapplication.BackgroundScan
import com.example.bluetoothapplication.GattConnection
import com.example.bluetoothapplication.MyBluetoothDevice
import com.example.kotlinconversionsupportivehousing.IBackgroundScan
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HomeFragment : Fragment(), IBackgroundScan {

    private var _binding: FragmentHomeBinding? = null
    val devices = ArrayList<BluetoothDevice>()
    private lateinit var textNotification: TextView
    private lateinit var homeViewModel: HomeViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //    ESP-01 UUIDs
//    val CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8a07361b26a8"
//    val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//    val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    // ESP-02 UUIDs
//    val CHARACTERISTIC_UUID = "83755cbd-e485-4153-ac8b-ce260afd3697"
//    val SERVICE_UUID = "adee10c3-91dd-43aa-ab9b-052eb63d456c"
//    val DESCRIPTOR_UUID = "4d6ec567-93f0-4541-8152-81b35dc5cb8b"

    //    BLANK
//    val CHARACTERISTIC_UUID = "681F827F-D00E-4307-B77A-F38014D6CC5F"
//    val SERVICE_UUID = "3BED005E-75B7-4DE6-B877-EAE81B0FC93F"
//    val DESCRIPTOR_UUID = "013B54B2-5520-406A-87F5-D644AD3E0565"

//    Pill Dispenser
        //  val CHARACTERISTIC_UUID = "B3E39CF1-B4D5-4F0A-88DE-6EDE9ABE2BD2"
    //val SERVICE_UUID = "B3E39CF0-B4D5-4F0A-88DE-6EDE9ABE2BD2"
    //val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"
    var CHARACTERISTIC_UUID = "ed8b2def-6b4c-48d2-b257-7832d2f2e929"
    var SERVICE_UUID = "3d7ceefa-7f25-4321-b966-b2ccb9f33256"
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
    var schedule = mutableMapOf("Sun" to "N/A", "Mon" to "N/A", "Tue" to "N/A", "Wed" to "N/A", "Thu" to "N/A", "Fri" to "N/A", "Sat" to "N/A")

    // Bluetooth Gatt Variables
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
    val permissionList = listOf<String>(
        Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: com.example.bluetoothapplication.ui.home.BackgroundScan.MyBinder = service as com.example.bluetoothapplication.ui.home.BackgroundScan.MyBinder
            serviceInstance = binder.getInstance()
            serviceInstance?.registerClient(this@HomeFragment)
            Log.i("SERVICE BIND", "Success")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i("SERVICE BIND", "Disconnected")
        }
    }

    val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.i("BluetoothScan onConnectionStateChange", "onConnectionStateChange invoked")
            Log.i("BluetoothScan onConnectionStateChange Device Status", newState.toString() + "")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BluetoothScan", "Connected to device. Proceeding with service discovery");
                activity?.runOnUiThread {
                    val toast = Toast.makeText(requireContext(), "Successfully connected to device", Toast.LENGTH_LONG)
                    //toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()

                }
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    gatt.requestMtu(512)
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Successfully dis-connected from device", Toast.LENGTH_LONG).show()
                }
                Log.i("Device info", "Disconnecting bluetooth device...")
                gatt.disconnect()
                gatt.close()
            } else {
                Log.i("BluetoothScan", "Could not find any connection state change")
            }
        }
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.i("BluetoothScan GattConnection", "onMtuChanged invoked")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BluetoothScan MTU Request","MTU request Success")
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    gatt.discoverServices()
                }
            } else {
                Log.i("BluetoothScan MTU Request","MTU request failed")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            Log.i(" GattConnection", "onCharacteristicChanged invoked")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.i("BluetoothScan", "onServicesDiscovered invoked")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(UUID.fromString(SERVICE_UUID))
                Log.i("BluetoothScan", "Successfully discovered services of target device")
                if (service != null) {
                    Log.i("BluetoothScan", "Service is not null.")
                    Log.i("BluetoothScan Service", "Found service with UUID: " + service.getUuid().toString());
                    val characteristics = service.characteristics
                    for (c in characteristics) {
                        Log.i("BluetoothScan Characteristic", c.uuid.toString())
                    }
                    val discoveredCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                    if (discoveredCharacteristic != null) {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            gatt.readCharacteristic(discoveredCharacteristic)
                        }
                        if (gatt.setCharacteristicNotification(discoveredCharacteristic, true)) {
                            Log.i("Set characteristic notification", "Success!")
                            Log.i("Characteristic property flags", discoveredCharacteristic.properties.toString())
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

        override fun onCharacteristicRead(gatt: BluetoothGatt, discoveredCharacteristic: BluetoothGattCharacteristic, value : ByteArray, status: Int) {
            Log.i("GattConnection", "onCharacteristicRead with byte array invoked")
            val value = String(value)
            Log.i("Read data", "Received data: $value")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = discoveredCharacteristic.value
                val value = String(data, StandardCharsets.UTF_8)
                Log.i("Read data", "Received data: $value")
            }
            try {
                Log.i("Smart data","this is smart data")
                val json = JSONObject(value)
                val lastDetected = json.getInt("lastDetected")
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )
                val notificationText = "Alert: No motion detected for $lastDetected on $currentDateTime"
                Log.i("last detected",lastDetected.toString())
                activity ?.runOnUiThread {
                    Toast.makeText(requireContext(), "Alert: No motion detected for $lastDetected minutes" , Toast.LENGTH_LONG).show()
                   // textNotification.text =notificationText
                    homeViewModel.updateNotificationText(notificationText)
                }
               if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("BluetoothScan","Disconnecting Gatt")
                    gatt.disconnect()
                    gatt.close()}

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.i("GattConnection", "onCharacteristicWrite invoked")
            Log.i("GattConnection", "onCharacteristicWrite invoked: " + characteristic.uuid)
            val data = characteristic.value
            val info = if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            ) {
                gatt.readCharacteristic(characteristic)
            } else {

            }
            info.toString()
            val value = String(data)
            Log.i("Read data", "Received data: $value")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = characteristic.value
                val value = String(data, StandardCharsets.UTF_8)
                Log.i("Readdata", "Received data: $value")

            }
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
                val dayAbbreviated = day.substring(0,3)
                if(hour>12){
                    hour %= 12
                    schedule[dayAbbreviated] = String.format(Locale.getDefault(), "%02d:%02d PM", hour, minute)
                } else{
                    schedule[dayAbbreviated] = String.format(Locale.getDefault(), "%02d:%02d AM", hour, minute)
                }
                Log.i("Schedule", convertData())
                sendToDevice(convertData())
            }
        val style = android.R.style.Theme_Holo_Light_Dialog_NoActionBar
        val timePickerDialog =
            TimePickerDialog(requireContext(), style, onTimeSetListener, hour, minute, true)
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    fun convertData(): String {

        var newScheduleFormat = ""
        for ((key, value) in schedule) {
            newScheduleFormat += key + " " + value + " "
        }
        return newScheduleFormat.trimEnd()
    }

    private fun launchBackgroundScan() {
        Log.i("launchBackgroundScan", "launchBackgroundScan currently running")
        startBackgroundScan = Intent(requireContext(), BackgroundScan::class.java)
        startBackgroundScan!!.putExtra("service_uuid", SERVICE_UUID)
        startBackgroundScan!!.putExtra("characteristic_uuid", CHARACTERISTIC_UUID)
        requireContext().bindService(startBackgroundScan!!, mConnection, AppCompatActivity.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(startBackgroundScan)
        }else{
            requireContext().startService(startBackgroundScan)
        }
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
        val gatt = device?.connectGatt(requireContext(), false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    private fun updateNotificationText(text: String) {
        homeViewModel.updateNotificationText(text)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome

        homeViewModel.textNotification.observe(viewLifecycleOwner) {
            textNotification.text = it
        }
        textNotification = binding.showNotification

        pillDispinserService = binding.startSmartPill
        pillDispinserService.setOnClickListener(View.OnClickListener {
            initiateBackgroundScan = true
            launchBackgroundScan()
        })

        setSunday = binding.Sunday
        setSunday.setOnClickListener(View.OnClickListener { setTime("Sun") })
        setMonday = binding.Monday
        setMonday.setOnClickListener(View.OnClickListener { setTime("Mon") })
        setTuesday = binding.Tuesday
        setTuesday.setOnClickListener(View.OnClickListener { setTime("Tue") })
        setWednesday = binding.Wednesday
        setWednesday.setOnClickListener(View.OnClickListener { setTime("Wed") })
        setThursday = binding.Thursday
        setThursday.setOnClickListener(View.OnClickListener { setTime("Thu") })
        setFriday = binding.Friday
        setFriday.setOnClickListener(View.OnClickListener { setTime("Fri") })
        setSaturday = binding.Saturday
        setSaturday.setOnClickListener(View.OnClickListener { setTime("Sat") })

        return root
    }


    fun startBackgroundScan(){
        val bluetoothAdapter: BluetoothAdapter? = (requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}