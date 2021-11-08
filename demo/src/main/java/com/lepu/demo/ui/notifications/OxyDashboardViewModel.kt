package com.lepu.demo.ui.notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OxyDashboardViewModel : ViewModel() {


    val dataSrc: MutableLiveData<IntArray> by lazy {
        MutableLiveData<IntArray>()
    }

    val pr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val spo2: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}