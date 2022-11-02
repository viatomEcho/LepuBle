package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
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
import java.nio.charset.StandardCharsets

/**
 * 指甲血氧设备：
 * send:
 * 1.获取设备信息
 * 2.实时血氧使能开关
 * receive:
 * 1.实时电量
 * 2.实时血氧
 * 3.实时工作状态
 * 血氧采样率：参数1HZ，波形50HZ
 */
class Pc60FwBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "Pc60FwBleInterface"
    private var pc60FwDevice = BoDeviceInfo()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = when (model) {
            Bluetooth.MODEL_PC60FW,
            Bluetooth.MODEL_POD_1W,
            Bluetooth.MODEL_S5W,
            Bluetooth.MODEL_S6W,
            Bluetooth.MODEL_S6W1,
            Bluetooth.MODEL_S7W,
            Bluetooth.MODEL_S7BW,
            Bluetooth.MODEL_PF_10,
            Bluetooth.MODEL_PF_10AW,
            Bluetooth.MODEL_PF_10AW1,
            Bluetooth.MODEL_PF_10BW,
            Bluetooth.MODEL_PF_10BW1,
            Bluetooth.MODEL_PC_60NW_1,
            Bluetooth.MODEL_PC_60B,
            Bluetooth.MODEL_POD2B,
            Bluetooth.MODEL_PF_20,
            Bluetooth.MODEL_PF_20AW,
            Bluetooth.MODEL_PF_20B,
            Bluetooth.MODEL_OXYSMART -> Pc60FwBleManager(context)
            else -> Pc6nBleManager(context)
        }
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
                .useAutoConnect(false) // true:可能自动重连， 程序代码还在执行扫描
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "manager.connect done")
                    if (model == Bluetooth.MODEL_PC_60NW || model == Bluetooth.MODEL_PC60NW_BLE) {
                        enableRtData(Pc60FwBleCmd.EnableType.OXY_PARAM, true)
                        enableRtData(Pc60FwBleCmd.EnableType.OXY_WAVE, true)
                    }
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
        LepuBleLog.d(tag, "onResponseReceived bytes: ${bytesToHex(response.bytes)}")
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
                        device.name?.let { it1 ->
                            pc60FwDevice.deviceName = it1
                        }
                        pc60FwDevice.hardwareV = it.hardwareV
                        pc60FwDevice.softwareV = it.softwareV
                        LepuBleLog.d(tag, "it.deviceName == " + it.deviceName)
                        LepuBleLog.d(tag, "it.hardwareV == " + it.hardwareV)
                        LepuBleLog.d(tag, "it.softwareV == " + it.softwareV)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo).post(InterfaceEvent(model, pc60FwDevice))
                    }
                }
                Pc60FwBleCmd.MSG_GET_CODE.toByte() -> {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_CODE => success")
                    val data = com.lepu.blepro.utils.toString(response.content)
                    pc60FwDevice.branchCode = data
                    LepuBleLog.d(tag, "toString == $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwGetCode).post(InterfaceEvent(model, data))
                }
                Pc60FwBleCmd.MSG_SET_CODE.toByte() -> {
                    LepuBleLog.d(tag, "model:$model,MSG_SET_CODE => success")
                    if (toUInt(response.content) == 1) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwSetCode).post(InterfaceEvent(model, true))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwSetCode).post(InterfaceEvent(model, false))
                    }
                }

            }
        } else if (response.token == TOKEN_PO_0F){
            when (response.type) {
                Pc60FwBleCmd.MSG_GET_DEVICE_INFO_0F.toByte() -> {
                    LepuBleLog.d(tag, "model:$model,MSG_GET_DEVICE_INFO_0F => success")
                    PC60FwBleResponse.DeviceInfo0F(response.content).let {
                        pc60FwDevice.deviceName = it.deviceName
                        device.name?.let { it1 ->
                            pc60FwDevice.deviceName = it1
                        }
                        pc60FwDevice.hardwareV = it.hardwareV
                        pc60FwDevice.softwareV = it.softwareV
                        LepuBleLog.d(tag, "it.deviceName == " + it.deviceName)
                        LepuBleLog.d(tag, "it.hardwareV == " + it.hardwareV)
                        LepuBleLog.d(tag, "it.softwareV == " + it.softwareV)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo).post(InterfaceEvent(model, pc60FwDevice))
                    }
                }
                TYPE_SPO2_PARAM -> {
                    LepuBleLog.d(tag, "model:$model,EventPC60FwRtDataParam => success")
                    PC60FwBleResponse.RtDataParam(response.content).let {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataParam).post(InterfaceEvent(model, it))
                        LepuBleLog.d(tag, "it.pi == " + it.pi)
                        LepuBleLog.d(tag, "it.pr == " + it.pr)
                        LepuBleLog.d(tag, "it.spo2 == " + it.spo2)
                        LepuBleLog.d(tag, "it.isProbeOff == " + it.isProbeOff)
                        LepuBleLog.d(tag, "it.isPulseSearching == " + it.isPulseSearching)
                        LepuBleLog.d(tag, "it.battery == " + it.battery)
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
                Pc60FwBleCmd.MSG_HEARTBEAT.toByte() -> {
                    LepuBleLog.d(tag, "model:$model,MSG_HEARTBEAT => success")
                    Handler().postDelayed({
                        sendHeartbeat()
                    }, 5000)
                }
            }
        }
    }

    fun enableRtData(type: Int, enable: Boolean) {
        sendCmd(Pc60FwBleCmd.enableSwitch(type, enable))
        LepuBleLog.d(tag, "enableRtData type:$type, enable:$enable")
    }

    override fun getInfo() {
        getBranchCode()
        if (model == Bluetooth.MODEL_PC_60NW || model == Bluetooth.MODEL_PC60NW_BLE) {
            sendCmd(Pc60FwBleCmd.getInfo0F())
            sendHeartbeat()
        } else {
            sendCmd(Pc60FwBleCmd.getSn())
            sendCmd(Pc60FwBleCmd.getInfoF0())
        }
        LepuBleLog.d(tag, "getInfo")
    }

    fun getBranchCode() {
        sendCmd(Pc60FwBleCmd.getCode())
        LepuBleLog.d(tag, "getBranchCode")
    }
    fun setBranchCode(code: String) {
        if (code.length > 8) {
            sendCmd(Pc60FwBleCmd.setCode(code.substring(0, 8).toByteArray(StandardCharsets.US_ASCII)))
        } else {
            sendCmd(Pc60FwBleCmd.setCode(code.toByteArray(StandardCharsets.US_ASCII)))
        }
        LepuBleLog.d(tag, "setBranchCode")
    }

    private fun sendHeartbeat() {
        sendCmd(Pc60FwBleCmd.sendHeartbeat(5))
    }
    override fun syncTime() {
        LepuBleLog.e(tag, "syncTime not yet implemented")
    }

    override fun getRtData() {
        LepuBleLog.e(tag, "getRtData not yet implemented")
    }

    override fun getFileList() {
        LepuBleLog.e(tag, "getFileList not yet implemented")
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

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }


}