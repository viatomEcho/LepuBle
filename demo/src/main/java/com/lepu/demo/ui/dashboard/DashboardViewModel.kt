package com.lepu.demo.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {


    // ecg
    val dataEcgSrc: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val ecgHr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    // bp
    val ps: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val sys: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val dia: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val mean: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val bpPr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    // oxy
    val dataOxySrc: MutableLiveData<IntArray> by lazy {
        MutableLiveData<IntArray>()
    }
    val oxyPr: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val spo2: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val pi: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }
}