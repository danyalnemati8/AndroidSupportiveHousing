package com.example.bluetoothapplication.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
    private val _textNotification = MutableLiveData<String>()
    val textNotification: LiveData<String>
        get() = _textNotification

    fun updateNotificationText(newText: String) {
        _textNotification.value = newText
    }

}