package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.Bp2WifiConfig
import com.lepu.blepro.ble.data.Bp2WifiDevice
import com.lepu.blepro.ble.data.r20.StatisticsFile
import com.lepu.blepro.event.*
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.EcnData
import com.lepu.demo.util.DateUtil
import java.util.*

class R20ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetFileList)
            .observe(owner) {
                val data = it.data as R20BleResponse.RecordList
                val names = arrayListOf<String>()
                for (file in data.list) {
                    if (data.type == 1) {
                        names.add("${DateUtil.stringFromDate(Date(file.measureTime*1000), "yyyyMMdd")}_day.stat")
                    } else if (data.type == 2) {
                        names.add("${DateUtil.stringFromDate(Date(file.measureTime*1000), "yyyyMMdd_HHmmss")}.stat")
                    }
                }
                _fileNames.value = names
                _info.value = names.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20ReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20ReadingFileProgress)
            .observe(owner) {
                _process.value = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20ReadFileComplete)
            .observe(owner) {
                val data = it.data as StatisticsFile
                val temp = EcnData()
                temp.fileName = data.fileName
                temp.data = data.bytes
                _ecnData.value = temp
                _readNextFile.value = true
            }
        LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError)
            .observe(owner) {
                when (it.cmd) {
                    R20BleCmd.GET_WIFI_LIST -> {
                        handler.postDelayed({
                            LpBleUtil.bp2GetWifiDevice(it.model)
                        }, 5000)
                    }
                    R20BleCmd.GET_WIFI_CONFIG -> {
                        noWifi.value = true
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetWifiList)
            .observe(owner) {
                val data = it.data as Bp2WifiDevice
                _wifiDevice.value = data
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetWifiConfig)
            .observe(owner) {
                val data = it.data as Bp2WifiConfig
                _wifiConfig.value = data
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20SetWifiConfig)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    handler.postDelayed({
                        LpBleUtil.bp2GetWifiConfig(it.model)
                    }, 1000)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20Reset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20FactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20GetVersionInfo)
            .observe(owner) {
                val data = it.data as R20BleResponse.VersionInfo
                _info.value = _info.value + "\n引导版本：${data.blV}，算法版本：${data.algV}\n蓝牙驱动版本：${data.bleV}"
            }
    }

}