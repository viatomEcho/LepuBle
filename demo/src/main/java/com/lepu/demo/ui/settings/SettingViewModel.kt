package com.lepu.demo.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.demo.data.DeviceFactoryData

open class SettingViewModel : ViewModel() {

    val _toast = MutableLiveData<String>().apply {
        value = null
    }
    var toast: LiveData<String> = _toast

    var cmdStr = ""
    var switchState = false
    var state = 0

    val _deviceFactoryData = MutableLiveData<DeviceFactoryData>().apply {
        value = null
    }
    var deviceFactoryData: LiveData<DeviceFactoryData> = _deviceFactoryData

    var config: Any? = null
}