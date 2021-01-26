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
    val _state = MutableLiveData<IntArray>().apply {
        value = intArrayOf(DeviceHelper.State.UNBOUND, DeviceHelper.State.UNBOUND)
    }

    val _device = MutableLiveData<Array<Bluetooth?>>().apply {
        value = arrayOfNulls(2)
    }

    var state: LiveData<IntArray> = _state
    var device : LiveData<Array<Bluetooth?>> = _device



}