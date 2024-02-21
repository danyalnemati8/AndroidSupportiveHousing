package com.example.bluetoothapplication.ui.home

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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    val devices = ArrayList<BluetoothDevice>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val startSmartPillScan = view?.findViewById<Button>(R.id.startSmartPill)

        startSmartPillScan?.setOnClickListener{
            startBackgroundScan()
        }
        return root
    }

    fun startBackgroundScan(){
        //Get the adapter
        val bluetoothAdapter: BluetoothAdapter? = (requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
       // bluetoothLeScanner.startScan()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}