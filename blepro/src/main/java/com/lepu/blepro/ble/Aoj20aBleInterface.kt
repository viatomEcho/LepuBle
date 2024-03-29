package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt

/**
 *
 * 蓝牙操作
 */

class Aoj20aBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Aoj20aBleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Aoj20aBleManager(context)
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "Device Init")
            }
            .enqueue()
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Aoj20aBleResponse.BleResponse) {

        when (response.cmd) {
            Aoj20aBleCmd.MSG_SET_TIME -> {
                LepuBleLog.d(tag, "model:$model,MSG_SET_TIME => success " + bytesToHex(response.bytes))
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aSetTime).post(InterfaceEvent(model, true))
            }
            Aoj20aBleCmd.MSG_TEMP_MEASURE -> {
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,MSG_TEMP_MEASURE => null " + bytesToHex(response.bytes))
                    return
                }
                LepuBleLog.d(tag, "model:$model,MSG_TEMP_MEASURE => success " + bytesToHex(response.bytes))
                val info = Aoj20aBleResponse.TempRtData(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRtData).post(InterfaceEvent(model, info))
            }
            Aoj20aBleCmd.MSG_GET_HISTORY_DATA -> {
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_HISTORY_DATA => null " + bytesToHex(response.bytes))
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aNoTempRecord).post(InterfaceEvent(model, true))
                    return
                }
                LepuBleLog.d(tag, "model:$model,MSG_GET_HISTORY_DATA => success " + bytesToHex(response.bytes))
                val info = Aoj20aBleResponse.TempRecord(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempRecord).post(InterfaceEvent(model, info))
            }
            Aoj20aBleCmd.MSG_DELETE_HISTORY_DATA -> {
                LepuBleLog.d(tag, "model:$model,MSG_DELETE_HISTORY_DATA => success " + bytesToHex(response.bytes))
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeleteData).post(InterfaceEvent(model, true))
            }
            Aoj20aBleCmd.MSG_GET_DEVICE_DATA -> {
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_DATA => null " + bytesToHex(response.bytes))
                    return
                }
                LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_DATA => success " + bytesToHex(response.bytes))
                val info = Aoj20aBleResponse.DeviceData(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeviceData).post(InterfaceEvent(model, info))
            }
            Aoj20aBleCmd.MSG_ERROR_CODE -> {
                if (response.len == 0) {
                    LepuBleLog.d(tag, "model:$model,MSG_ERROR_CODE => null " + bytesToHex(response.bytes))
                    return
                }
                LepuBleLog.d(tag, "model:$model,MSG_ERROR_CODE => success " + bytesToHex(response.bytes))
                val info = Aoj20aBleResponse.ErrorMsg(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempErrorMsg).post(InterfaceEvent(model, info))

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
            if (bytes[i+2] == Aoj20aBleCmd.MSG_TEMP_MEASURE.toByte()) {
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

    /**
     * get device info
     */
    override fun getInfo() {
        sendCmd(Aoj20aBleCmd.getDeviceData())
    }

    override fun syncTime() {
        sendCmd(Aoj20aBleCmd.setTime())
    }

    /**
     * get file list
     */
    override fun getFileList() {
        sendCmd(Aoj20aBleCmd.getHistoryData())
    }

    fun deleteData() {
        sendCmd(Aoj20aBleCmd.deleteHistoryData())
    }

    fun tempMeasure() {
        sendCmd(Aoj20aBleCmd.tempMeasure())
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
    }

    override fun dealReadFile(userId: String, fileName: String) {

    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

}