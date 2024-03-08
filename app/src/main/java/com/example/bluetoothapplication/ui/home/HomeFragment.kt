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
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
//import com.example.bluetoothapplication.BackgroundScan
import com.example.bluetoothapplication.GattConnection
import com.example.bluetoothapplication.MyBluetoothDevice
import com.example.kotlinconversionsupportivehousing.IBackgroundScan
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID

class HomeFragment : Fragment(), IBackgroundScan {

    private var _binding: FragmentHomeBinding? = null
    val devices = ArrayList<BluetoothDevice>()

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
            TimePickerDialog(requireContext(), style, onTimeSetListener, hour, minute, true)
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    private fun launchBackgroundScan() {
        Log.i("launchBackgroundScan", "launchBackgroundScan currently running")
//        startBackgroundScan = Intent(requireContext(), BackgroundScan::class.java)
        startBackgroundScan = Intent(requireContext(), BackgroundScan::class.java)
        startBackgroundScan!!.putExtra("service_uuid", SERVICE_UUID)
        startBackgroundScan!!.putExtra("characteristic_uuid", CHARACTERISTIC_UUID)
        requireContext().bindService(startBackgroundScan!!, mConnection, AppCompatActivity.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(startBackgroundScan)
        }else{
            requireContext().startService(startBackgroundScan)
        }
//        serviceInstance?.registerClient(this@HomeFragment)


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
//            val gatt = device?.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
//        val gatt = device?.connectGatt(this, false, smartWellnessGatt.gattCallback, BluetoothDevice.TRANSPORT_LE)
        // context issue
        val gatt = device?.connectGatt(requireContext(), false, pillDispenserGatt.gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

//        val startSmartPillScan = view?.findViewById<Button>(R.id.startSmartPill)
//
//        startSmartPillScan?.setOnClickListener{
//            startBackgroundScan()
//        }

//        pillDispinserService = findViewById<Button>(R.id.startSmartPill)
        pillDispinserService = binding.startSmartPill
        pillDispinserService.setOnClickListener(View.OnClickListener {
            initiateBackgroundScan = true
            launchBackgroundScan()
        })

        setSunday = binding.Sunday
        setSunday.setOnClickListener(View.OnClickListener { setTime("Sunday") })
        setMonday = binding.Monday
        setMonday.setOnClickListener(View.OnClickListener { setTime("Monday") })
        setTuesday = binding.Tuesday
        setTuesday.setOnClickListener(View.OnClickListener { setTime("Tuesday") })
        setWednesday = binding.Wednesday
        setWednesday.setOnClickListener(View.OnClickListener { setTime("Wednesday") })
        setThursday = binding.Thursday
        setThursday.setOnClickListener(View.OnClickListener { setTime("Thursday") })
        setFriday = binding.Friday
        setFriday.setOnClickListener(View.OnClickListener { setTime("Friday") })
        setSaturday = binding.Saturday
        setSaturday.setOnClickListener(View.OnClickListener { setTime("Saturday") })

        return root
    }


    fun startBackgroundScan(){
        //Get the adapter
        val bluetoothAdapter: BluetoothAdapter? = (requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
//        bluetoothLeScanner.startScan()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}