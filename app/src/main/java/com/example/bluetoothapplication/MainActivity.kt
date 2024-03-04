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
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notGrantedPermissions : List<String>
    //private val bluetoothGattHelper = BluetoothGattHelper()
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private var crashApp by Delegates.notNull<Boolean>()

    var REQUEST_BLUETOOTH_SCAN = 3
    val permissionList = listOf<String>(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Start with check bluetooth permissions and location permissions
        scanForBluetoothWithPermissions()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //scanForBluetoothWithPermissions()
        }else {
            //scanForBluetooth()
        }



    }
    // Checking if permissions already exist or writing those permissions
    private fun scanForBluetoothWithPermissions(){
        Log.i("BluetoothScan","Checking for permissions")
            notGrantedPermissions=permissionList.filterNot { permission->
            ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED
        }
        Log.i("BluetoothScan",notGrantedPermissions.toString())
        if(notGrantedPermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), 123)
            BluetoothPermissionResult.launch(notGrantedPermissions.toTypedArray())
           // displayRequestPermissionToastMessage()
        }else{
            Toast.makeText(this, "Bluetooth and location permission granted", Toast.LENGTH_SHORT).show()
            enableBluetoothandLocation()
        }
    }

    // Code to write the toast message.
    /*private fun displayRequestPermissionToastMessage(){
        AlertDialog.Builder(this).setTitle("Requesting Bluetooth and Location Permissions")
            .setMessage("Bluetooth and location permissions are needed to connect to IoT Devices ")
            .setNegativeButton("Cancel"){
                dialog,_->Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setPositiveButton("OK"){_,_->
                ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), 123)
                BluetoothPermissionResult.launch(notGrantedPermissions.toTypedArray())
            }
            .show()
    }*/
    private val BluetoothPermissionResult=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permissionMap->
        if (permissionMap.all { it.value }){
            Toast.makeText(this, "Media permissions granted", Toast.LENGTH_SHORT).show()
            enableBluetoothandLocation()
        }else{
            Toast.makeText(this, "Media permissions not granted!", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableBluetoothandLocation() {
        Log.i("BluetoothScan","Now actually Enabling BluetoothScan")
        val bluetoothAdapter: BluetoothAdapter? = (this.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
       /* enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this,"Bluetooth has been enabled successfully",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Bluetooth has not been enabled successfully",Toast.LENGTH_SHORT).show()
            }*/
/*
        fun Context.bluetoothAdapter(): BluetoothAdapter? =
            (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
       */ if(bluetoothAdapter != null) {
            Log.i("BluetoothScan","Got Bluetooth Adapter")
            crashApp = true
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 123)

                    //Action Point to fix this deprecation in future.
            //Need to flow logs -> some stupidity is there

           /* val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                //bluetoothAdapter.enable()
            }
            bluetoothAdapter.enable()
            }*/
        }
    }






    //https://stackoverflow.com/questions/70475132/using-bluetoothmanager-and-getadapater
    //chatgpt as one method was deprecated\
    //Tried permission it went to nothing.-<:((
}