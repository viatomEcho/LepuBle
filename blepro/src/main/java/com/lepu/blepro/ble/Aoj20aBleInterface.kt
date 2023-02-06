package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.aoj20a.*
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt

/**
 * aoj20a体温计：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取列表
 * 4.删除数据
 * receive:
 * 1.体温数据测量正常结果
 * 2.测量错误结果
 */
class Aoj20aBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Aoj20aBleInterface"

    private var deviceInfo = DeviceInfo()
    private var result = TempResult()
    private var tempList = arrayListOf<Record>()
    private var errorResult = ErrorResult()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Aoj20aBleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Aoj20aBleManager(context)
            LepuBleLog.d(tag, "!isManagerInitialized, manager.create done")
        }
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "manager.connect done")
            }
            .enqueue()
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Aoj20aBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived received : ${bytesToHex(response.bytes)}")
        when (response.cmd) {
            Aoj20aBleCmd.MSG_SET_TIME -> {
                LepuBleLog.d(tag, "model:$model,MSG_SET_TIME => success " + bytesToHex(response.bytes))
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aSetTime).post(InterfaceEvent(model, true))
            }
            Aoj20aBleCmd.MSG_GET_RT_DATA -> {
                if (response.len <= 0 || response.content.size < 3) {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_RT_DATA => null " + bytesToHex(response.bytes))
                    return
                }
                LepuBleLog.d(tag, "model:$model,MSG_GET_RT_DATA => success " + bytesToHex(response.bytes))
                val info = Aoj20aBleResponse.TempRtData(response.content)

                result.temp = info.temp
                result.mode = info.mode
                result.modeMess = info.modeMsg

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData).post(InterfaceEvent(model, result))
            }
            Aoj20aBleCmd.MSG_GET_HISTORY_DATA -> {
                LepuBleLog.d(tag, "model:$model,MSG_GET_HISTORY_DATA => success " + bytesToHex(response.bytes))
                if (response.len != 0 && response.content.size > 7) {
                    val info = Aoj20aBleResponse.TempRecord(response.content)
                    val record = Record()
                    record.num = info.num
                    record.year = info.year
                    record.month = info.month
                    record.day = info.day
                    record.hour = info.hour
                    record.minute = info.minute
                    record.temp = info.temp
                    tempList.add(record)
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempList).post(InterfaceEvent(model, tempList))
                }
            }
            Aoj20aBleCmd.MSG_DELETE_HISTORY_DATA -> {
                LepuBleLog.d(tag, "model:$model,MSG_DELETE_HISTORY_DATA => success " + bytesToHex(response.bytes))
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeleteData).post(InterfaceEvent(model, true))
            }
            Aoj20aBleCmd.MSG_GET_DEVICE_DATA -> {
                if (response.len <= 0 || response.content.size < 3) {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_DATA => null " + bytesToHex(response.bytes))
                    return
                }
                LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_DATA => success " + bytesToHex(response.bytes))
                val info = Aoj20aBleResponse.DeviceData(response.content)

                deviceInfo.mode = info.mode
                deviceInfo.modeMess = info.modeMsg
                deviceInfo.battery = info.battery
                deviceInfo.version = info.versionMsg

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeviceData).post(InterfaceEvent(model, deviceInfo))
            }
            Aoj20aBleCmd.MSG_ERROR_CODE -> {
                if (response.len <= 0 || response.content.isEmpty()) {
                    LepuBleLog.d(tag, "model:$model,MSG_ERROR_CODE => null " + bytesToHex(response.bytes))
                    return
                }
                LepuBleLog.d(tag, "model:$model,MSG_ERROR_CODE => success " + bytesToHex(response.bytes))
                val info = Aoj20aBleResponse.ErrorMsg(response.content)

                errorResult.code = info.code
                errorResult.codeMess = info.codeMsg

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg).post(InterfaceEvent(model, errorResult))

            }
        }


    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 5) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-4) {
            if (bytes[i] != 0xAA.toByte() || bytes[i+1] != 0x01.toByte()) {
                continue@loop
            }
            // need content length
            var len = byte2UInt(bytes[i+3])
            if (bytes[i+2] == Aoj20aBleCmd.MSG_GET_RT_DATA.toByte()) {
                len = 3
            } else if (bytes[i+2] == Aoj20aBleCmd.MSG_ERROR_CODE.toByte()) {
                len = 1
            }
            if (i+5+len > bytes.size) {
                continue@loop
            }
            val temp: ByteArray = bytes.copyOfRange(i, i+5+len)
            if (temp.last() == Aoj20aBleCmd.getLastByte(temp)) {
                val bleResponse = Aoj20aBleResponse.BleResponse(temp)
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+5+len == bytes.size) null else bytes.copyOfRange(i+5+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    override fun getInfo() {
        sendCmd(Aoj20aBleCmd.getDeviceData())
        LepuBleLog.e(tag, "getInfo")
    }

    override fun syncTime() {
        sendCmd(Aoj20aBleCmd.setTime())
        LepuBleLog.e(tag, "syncTime")
    }

    override fun getFileList() {
        tempList.clear()
        sendCmd(Aoj20aBleCmd.getHistoryData())
        LepuBleLog.e(tag, "getFileList")
    }

    fun deleteData() {
        sendCmd(Aoj20aBleCmd.deleteHistoryData())
        LepuBleLog.e(tag, "deleteData")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

    override fun getRtData() {
        sendCmd(Aoj20aBleCmd.getRtData())
        LepuBleLog.e(tag, "getRtData")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealReadFile not yet implemented")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset not yet implemented")
    }

    override fun factoryReset() {
        LepuBleLog.e(tag, "factoryReset not yet implemented")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryResetAll not yet implemented")
    }

}