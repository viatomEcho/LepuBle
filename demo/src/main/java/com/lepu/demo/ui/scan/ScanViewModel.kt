package com.lepu.demo.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.ble.DeviceHelper

/**
 * author: wujuan
 * created on: 2021/1/21 16:46
 * description:
 */
class ScanViewModel: ViewModel() {
    val _state = MutableLiveData<Int>().apply {
        value = DeviceHelper.State.UNBOUND
    }

    val _device = MutableLiveData<Bluetooth>()

    var state: LiveData<Int> = _state
    var device : LiveData<Bluetooth> = _device
}