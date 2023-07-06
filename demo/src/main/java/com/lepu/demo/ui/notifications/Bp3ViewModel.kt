package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Bp3BleCmd
import com.lepu.blepro.ble.cmd.ResponseError
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.BpData
import com.lepu.demo.util.DataConvert

class Bp3ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3Reset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3FactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3FactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetWifiList)
            .observe(owner) {
                val data = it.data as Bp2WifiDevice
                _wifiDevice.value = data
            }
        LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError)
            .observe(owner) {
                when (it.cmd) {
                    Bp3BleCmd.GET_WIFI_LIST -> {
                        handler.postDelayed({
                            LpBleUtil.bp2GetWifiDevice(it.model)
                        }, 3000)
                    }
                    Bp3BleCmd.GET_WIFI_CONFIG -> {
                        noWifi.value = true
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SetWifiConfig)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    handler.postDelayed({
                        LpBleUtil.bp2GetWifiConfig(it.model)
                    }, 3000)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetWifiConfig)
            .observe(owner) {
                val data = it.data as Bp2WifiConfig
                _wifiConfig.value = data
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetFileList)
            .observe(owner) {
                val data = it.data as KtBleFileList
                val names = arrayListOf<String>()
                for (fileName in data.fileNameList) {
                    names.add(fileName)
                }
                _fileNames.value = names
                _info.value = names.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadFileError)
            .observe(owner) {
                _toast.value = "读文件错误"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadingFileProgress)
            .observe(owner) {
                val data = it.data as Int
                _info.value = "读文件进度：$data %"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadFileComplete)
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
    }

}