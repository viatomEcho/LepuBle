package com.lepu.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.blepro.ble.cmd.OxyBleResponse

/**
 * @ClassName MainViewModel
 * @Description TODO
 * @Author wujuan
 * @Date 2021/11/8 10:10
 */
class MainViewModel: ViewModel() {
    val _bleState = MutableLiveData<Boolean>().apply {
        value = false
    }
    val bleState: LiveData<Boolean> = _bleState

    val _model = MutableLiveData<Int>()
    val model: LiveData<Int> = _model


    val _oxyInfo = MutableLiveData<OxyBleResponse.OxyInfo>()
    val oxyInfo: LiveData<OxyBleResponse.OxyInfo> = _oxyInfo
}