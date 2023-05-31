package com.lepu.demo.ui.notifications

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.Bp2WifiConfig
import com.lepu.blepro.ble.data.Bp2WifiDevice
import com.lepu.blepro.ble.data.ventilator.StatisticsFile
import com.lepu.blepro.event.*
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.data.EcnData
import com.lepu.demo.util.DateUtil
import java.util.*

class VentilatorViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner, context: Context) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileList)
            .observe(owner) {
                val data = it.data as VentilatorBleResponse.RecordList
                val names = arrayListOf<String>()
                for (file in data.list) {
                    names.add(file.recordName)
                }
                _fileNames.value = names
                _info.value = names.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadingFileProgress)
            .observe(owner) {
                _process.value = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileComplete)
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
                    VentilatorBleCmd.GET_WIFI_LIST -> {
                        handler.postDelayed({
                            LpBleUtil.bp2GetWifiDevice(it.model)
                        }, 5000)
                    }
                    VentilatorBleCmd.GET_WIFI_CONFIG -> {
                        noWifi.value = true
                    }
                }
                when (it.type) {
                    LpBleCmd.TYPE_FILE_NOT_FOUND -> _toast.value = context.getString(R.string.file_not_found)
                    LpBleCmd.TYPE_FILE_READ_FAILED -> _toast.value = context.getString(R.string.read_error)
                    LpBleCmd.TYPE_FILE_WRITE_FAILED -> _toast.value = context.getString(R.string.write_file_error)
                    LpBleCmd.TYPE_FIRMWARE_UPDATE_FAILED -> _toast.value = context.getString(R.string.software_upgrade_error)
                    LpBleCmd.TYPE_LANGUAGE_UPDATE_FAILED -> _toast.value = context.getString(R.string.language_upgrade_error)
                    LpBleCmd.TYPE_PARAM_ILLEGAL -> _toast.value = context.getString(R.string.param_illegal)
                    LpBleCmd.TYPE_PERMISSION_DENIED -> _toast.value = context.getString(R.string.permission_denied)
                    LpBleCmd.TYPE_DECRYPT_FAILED -> {
                        _toast.value = context.getString(R.string.decrypt_failed)
                        LpBleUtil.disconnect(false)
                    }
                    LpBleCmd.TYPE_DEVICE_BUSY -> _toast.value = context.getString(R.string.device_busy)
                    LpBleCmd.TYPE_CMD_FORMAT_ERROR -> _toast.value = context.getString(R.string.cmd_format_error)
                    LpBleCmd.TYPE_CMD_NOT_SUPPORTED -> _toast.value = context.getString(R.string.cmd_not_support)
                    LpBleCmd.TYPE_NORMAL_ERROR -> _toast.value = context.getString(R.string.normal_error)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiList)
            .observe(owner) {
                val data = it.data as Bp2WifiDevice
                _wifiDevice.value = data
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfig)
            .observe(owner) {
                val data = it.data as Bp2WifiConfig
                _wifiConfig.value = data
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWifiConfig)
            .observe(owner) {
                val data = it.data as Boolean
                if (data) {
                    handler.postDelayed({
                        LpBleUtil.bp2GetWifiConfig(it.model)
                    }, 1000)
                }
            }
        /*LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReset)
            .observe(owner) {
                _reset.value = true
            }*/
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorFactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfo)
            .observe(owner) {
                val data = it.data as VentilatorBleResponse.VersionInfo
                Log.d("VentilatorViewModel", "VersionInfo : $data")
            }
    }

}