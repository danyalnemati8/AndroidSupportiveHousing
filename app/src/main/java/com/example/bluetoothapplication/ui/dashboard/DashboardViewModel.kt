package com.example.bluetoothapplication.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

   /* private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text
*/
    private val _notification = MutableLiveData<String>()

    val notification: LiveData<String>
        get() = _notification

    fun updateNotification(newText: String) {
        _notification.value = newText
    }
}