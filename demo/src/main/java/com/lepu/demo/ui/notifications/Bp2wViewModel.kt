package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.*
import com.lepu.demo.util.DataConvert

class Bp2wViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFileList)
            .observe(owner) { event ->
                (event.data as KtBleFileList).let {
                    val names = arrayListOf<String>()
                    for (fileName in it.fileNameList) {
                        names.add(fileName)
                    }
                    _fileNames.value = names
                    _info.value = names.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Bp2FilePart).let {
                    _process.value = (it.percent.times(100)).toInt()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileComplete)
            .observe(owner) { event ->
                (event.data as Bp2BleFile).let {
                    if (it.type == 2) {
                        val data = Bp2EcgFile(it.content)
                        val temp = getEcgData(data.measureTime.toLong(), it.name, data.waveData, DataConvert.getBp2ShortArray(data.waveData), data.recordingTime)
                        _ecgData.value = temp
                    } else if (it.type == 1) {
                        val data = Bp2BpFile(it.content)
                        val temp = BpData()
                        temp.fileName = it.name
                        temp.sys = data.sys
                        temp.dia = data.dia
                        temp.pr = data.pr
                        temp.mean = data.mean
                        _bpData.value = temp
                    }
                    _readNextFile.value = true
                }
            }
        /*LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2WifiScanning)
            .observe(owner) {
                handler.postDelayed({
                    LpBleUtil.bp2GetWifiDevice(it.model)
                }, 1000)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2WifiDevice)
            .observe(owner) {
                val data = it.data as Bp2WifiDevice
                _wifiDevice.value = data
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetWifiConfig)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    handler.postDelayed({
                        LpBleUtil.bp2GetWifiConfig(it.model)
                    }, 1000)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetWifiConfig)
            .observe(owner) {
                val data = it.data as Bp2WifiConfig
                _wifiConfig.value = data
            }*/
    }

}