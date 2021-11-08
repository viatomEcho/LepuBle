package com.lepu.demo.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.demo.ble.LpBleUtil

class DashboardViewModel : ViewModel() {


    // draw ecg
    val dataSrc: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }

    val hr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}