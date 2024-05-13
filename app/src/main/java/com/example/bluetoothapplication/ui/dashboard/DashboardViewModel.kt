package com.example.bluetoothapplication.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _notification = MutableLiveData<String>()
    val notification: LiveData<String>
        get() {
            return _notification
        }

    fun updateNotification(newText: String) {
        _notification.value = newText
    }
}