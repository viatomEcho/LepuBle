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
                when (it.type) {
                    LpBleCmd.TYPE_FILE_NOT_FOUND -> _toast.value = "找不到文件"
                    LpBleCmd.TYPE_FILE_READ_FAILED -> _toast.value = "读文件失败"
                    LpBleCmd.TYPE_FILE_WRITE_FAILED -> _toast.value = "写文件失败"
                    LpBleCmd.TYPE_FIRMWARE_UPDATE_FAILED -> _toast.value = "固件升级失败"
                    LpBleCmd.TYPE_LANGUAGE_UPDATE_FAILED -> _toast.value = "语言包升级失败"
                    LpBleCmd.TYPE_PARAM_ILLEGAL -> _toast.value = "参数不合法"
                    LpBleCmd.TYPE_PERMISSION_DENIED -> _toast.value = "权限不足"
                    LpBleCmd.TYPE_DECRYPT_FAILED -> {
                        _toast.value = "解密失败，断开连接"
                        LpBleUtil.disconnect(false)
                    }
                    LpBleCmd.TYPE_DEVICE_BUSY -> _toast.value = "设备资源被占用/设备忙"
                    LpBleCmd.TYPE_CMD_FORMAT_ERROR -> _toast.value = "指令格式错误"
                    LpBleCmd.TYPE_CMD_NOT_SUPPORTED -> _toast.value = "不支持指令"
                    LpBleCmd.TYPE_NORMAL_ERROR -> _toast.value = "通用错误"
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