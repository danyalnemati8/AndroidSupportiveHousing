package com.example.bluetoothapplication.ui.smartpot

class smartpotFragment {

    fun readWriteList() {
        var mutableList = mutableListOf<String>()
        mutableList.add("Sydney")
        mutableList.add("Tokyo")
        assert(mutableList.get(0) == "Sydney")
        mutableList = mutableListOf("Paris", "London")
        assert(mutableList.get(0) == "Paris")
    }
}