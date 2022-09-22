package com.lepu.demo.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    // er3
    val dataEcgSrc1: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc2: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc3: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc4: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc5: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc6: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc7: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc8: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc9: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc10: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc11: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }
    val dataEcgSrc12: MutableLiveData<FloatArray> by lazy {
        MutableLiveData<FloatArray>()
    }

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