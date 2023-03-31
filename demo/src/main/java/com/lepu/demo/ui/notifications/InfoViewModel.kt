package com.lepu.demo.ui.notifications

import android.os.Handler
import androidx.lifecycle.*
import com.lepu.blepro.ble.data.*
import com.lepu.demo.data.*

open class InfoViewModel : ViewModel() {

    val handler = Handler()

    val _info = MutableLiveData<String>().apply {
        value = null
    }
    var info: LiveData<String> = _info
    val _process = MutableLiveData<Int>().apply {
        value = null
    }
    var process: LiveData<Int> = _process
    val _readNextFile = MutableLiveData<Boolean>().apply {
        value = null
    }
    var readNextFile: LiveData<Boolean> = _readNextFile
    val _readFileError = MutableLiveData<Boolean>().apply {
        value = null
    }
    var readFileError: LiveData<Boolean> = _readFileError
    val _fileNames = MutableLiveData<ArrayList<String>>().apply {
        value = null
    }
    var fileNames: LiveData<ArrayList<String>> = _fileNames
    val _ecgData = MutableLiveData<EcgData>().apply {
        value = null
    }
    var ecgData: LiveData<EcgData> = _ecgData
    val _oxyData = MutableLiveData<OxyData>().apply {
        value = null
    }
    var oxyData: LiveData<OxyData> = _oxyData
    val _bpData = MutableLiveData<BpData>().apply {
        value = null
    }
    var bpData: LiveData<BpData> = _bpData
    val _ecnData = MutableLiveData<EcnData>().apply {
        value = null
    }
    var ecnData: LiveData<EcnData> = _ecnData
    val _reset = MutableLiveData<Boolean>().apply {
        value = null
    }
    var reset: LiveData<Boolean> = _reset
    val _factoryReset = MutableLiveData<Boolean>().apply {
        value = null
    }
    var factoryReset: LiveData<Boolean> = _factoryReset
    val _factoryResetAll = MutableLiveData<Boolean>().apply {
        value = null
    }
    var factoryResetAll: LiveData<Boolean> = _factoryResetAll

    // wifi
    val _wifiDevice = MutableLiveData<Bp2WifiDevice>().apply {
        value = null
    }
    var wifiDevice: LiveData<Bp2WifiDevice> = _wifiDevice
    val _wifiConfig = MutableLiveData<Bp2WifiConfig>().apply {
        value = null
    }
    var wifiConfig: LiveData<Bp2WifiConfig> = _wifiConfig
    var noWifi = MutableLiveData<Boolean>()


    fun getEcgData(recordingTime: Long, fileName: String, wave: ByteArray, shortData: ShortArray, duration: Int) : EcgData {
        val data = EcgData()
        data.recordingTime = recordingTime
        data.fileName = fileName
        data.data = wave
        data.shortData = shortData
        data.duration = duration
        return data
    }
}