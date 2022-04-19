package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.cmd.PC60FwBleResponse.PC60FwResponse.Companion.TYPE_BATTERY_LEVEL
import com.lepu.blepro.ble.cmd.PC60FwBleResponse.PC60FwResponse.Companion.TYPE_DEVICE_INFO
import com.lepu.blepro.ble.cmd.PC60FwBleResponse.PC60FwResponse.Companion.TYPE_DEVICE_SN
import com.lepu.blepro.ble.cmd.PC60FwBleResponse.PC60FwResponse.Companion.TYPE_SPO2_PARAM
import com.lepu.blepro.ble.cmd.PC60FwBleResponse.PC60FwResponse.Companion.TYPE_SPO2_WAVE
import com.lepu.blepro.ble.cmd.PC60FwBleResponse.PC60FwResponse.Companion.TYPE_WORKING_STATUS
import com.lepu.blepro.ble.data.BoDeviceInfo
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.CrcUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt


class Pc60FwBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "Pc60FwBleInterface"
    private var pc60FwDevice = BoDeviceInfo()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = if (model == Bluetooth.MODEL_PC60FW
            || model == Bluetooth.MODEL_OXYSMART
            || model == Bluetooth.MODEL_POD_1W
            || model == Bluetooth.MODEL_PC_60NW
            || model == Bluetooth.MODEL_PC_60B
            || model == Bluetooth.MODEL_POD2B) {
            Pc60FwBleManager(context)
        } else {
            Pc6nBleManager(context)
        }
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
                .useAutoConnect(false) // true:可能自动重连， 程序代码还在执行扫描
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "Device Init")
                }
                .enqueue()
    }

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes
        if (bytes == null || bytes.size < 6) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size - 5) {
            if (bytes[i] != 0xAA.toByte()) {
                continue@loop
            }

            if (bytes[i + 1] != 0x55.toByte()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+3, i+4))
            if ((len < 0) || (i+4+len > bytes.size)) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)
            if (temp.last() == CrcUtil.calCRC8Pc(temp)) {
                val bleResponse = PC60FwBleResponse.PC60FwResponse(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: PC60FwBleResponse.PC60FwResponse) {
        if (response.token == TOKEN_EPI_F0) {
            when (response.type) {
                TYPE_BATTERY_LEVEL -> {
                    LepuBleLog.d(tag, "model:$model,EventPC60FwBattery => success")
                    PC60FwBleResponse.Battery(response.content).let {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwBattery).post(InterfaceEvent(model, it))
                        LepuBleLog.d(tag, "it.batteryLevel == " + it.batteryLevel)
                    }
                }
                TYPE_DEVICE_SN -> {
                    LepuBleLog.d(tag, "model:$model,TYPE_DEVICE_SN => success")
                    pc60FwDevice.sn = com.lepu.blepro.utils.toString(response.content)
                    LepuBleLog.d(tag, "toString == " + com.lepu.blepro.utils.toString(response.content))
                }
                TYPE_DEVICE_INFO -> {
                    LepuBleLog.d(tag, "model:$model,TYPE_DEVICE_INFO => success")
                    PC60FwBleResponse.DeviceInfo(response.content).let {
                        pc60FwDevice.deviceName = it.deviceName
                        pc60FwDevice.hardwareV = it.hardwareV
                        pc60FwDevice.softwareV = it.softwareV
                        LepuBleLog.d(tag, "it.deviceName == " + it.deviceName)
                        LepuBleLog.d(tag, "it.hardwareV == " + it.hardwareV)
                        LepuBleLog.d(tag, "it.softwareV == " + it.softwareV)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo).post(InterfaceEvent(model, pc60FwDevice))
                    }
                }
            }
        } else if (response.token == TOKEN_PO_0F){
            when (response.type) {
                TYPE_SPO2_PARAM -> {
                    LepuBleLog.d(tag, "model:$model,EventPC60FwRtDataParam => success")
                    PC60FwBleResponse.RtDataParam(response.content).let {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataParam).post(InterfaceEvent(model, it))
                        LepuBleLog.d(tag, "it.pi == " + it.pi)
                        LepuBleLog.d(tag, "it.pr == " + it.pr)
                        LepuBleLog.d(tag, "it.spo2 == " + it.spo2)
                    }
                }
                TYPE_SPO2_WAVE -> {
                    LepuBleLog.d(tag, "model:$model,EventPC60FwRtDataWave => success")
                    LepuBleLog.d(tag, "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content))
                    PC60FwBleResponse.RtDataWave(response.content).let {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataWave).post(InterfaceEvent(model, it))
                    }
                }
                TYPE_WORKING_STATUS -> {
                    PC60FwBleResponse.WorkingStatus(response.content).let {
                        LepuBleLog.d(tag, "model:$model,WORK_STATUS_DATA => success $it")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwWorkingStatus).post(InterfaceEvent(model, it))
                    }
                }
                Pc60FwBleCmd.MSG_ENABLE_PARAM.toByte() -> {
                    LepuBleLog.d(tag, "model:$model,MSG_ENABLE_PARAM => success ${bytesToHex(response.content)}")
                }
                Pc60FwBleCmd.MSG_ENABLE_WAVE.toByte() -> {
                    LepuBleLog.d(tag, "model:$model,MSG_ENABLE_WAVE => success ${bytesToHex(response.content)}")
                }
                Pc60FwBleCmd.MSG_IR_RED_FREQ.toByte() -> {
                    PC60FwBleResponse.OriginalData(response.content).let {
                        LepuBleLog.d(tag, "model:$model,MSG_IR_RED_FREQ => success $it")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwOriginalData).post(InterfaceEvent(model, it))
                    }
                }
            }
        }
    }

    fun enableRtData(type: Int, enable: Boolean) {
        sendCmd(Pc60FwBleCmd.enableSwitch(type, enable))
    }

    override fun getInfo() {
        sendCmd(Pc60FwBleCmd.getSn())
        sendCmd(Pc60FwBleCmd.getInfo())
    }

    override fun syncTime() {
    }

    override fun getRtData() {
    }

    override fun getFileList() {
    }

    override fun dealReadFile(userId: String, fileName: String) {
    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }


}