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
import com.lepu.blepro.ble.data.Pc6nDeviceInfo
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt


class Pc6nBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "Pc6nBleInterface"
    private var pc6nDevice = Pc6nDeviceInfo()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Pc6nBleManager(context)
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
            if (temp.last() == CrcUtil.calCRC8PC(temp)) {
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
        if (response.token == TOKEN_EPI_F0 && response.type == TYPE_BATTERY_LEVEL){
            LepuBleLog.d(tag, "model:$model,EventPC60FwBattery => success")
           PC60FwBleResponse.Battery(response.content).let {
               LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwBattery).post(InterfaceEvent(model, it))
               LepuBleLog.d(tag, "it.batteryLevel == " + it.batteryLevel)
           }
        }
        if (response.token == TOKEN_EPI_F0 && response.type == TYPE_DEVICE_SN){
            LepuBleLog.d(tag, "model:$model,TYPE_DEVICE_SN => success")
            pc6nDevice.sn = com.lepu.blepro.utils.toString(response.content)
            LepuBleLog.d(tag, "toString == " + com.lepu.blepro.utils.toString(response.content))
        }
        if (response.token == TOKEN_EPI_F0 && response.type == TYPE_DEVICE_INFO){
            LepuBleLog.d(tag, "model:$model,TYPE_DEVICE_INFO => success")
            PC60FwBleResponse.DeviceInfo(response.content).let {
                pc6nDevice.deviceName = it.deviceName
                pc6nDevice.hardwareV = it.hardwareV
                pc6nDevice.softwareV = it.softwareV
                LepuBleLog.d(tag, "it.deviceName == " + it.deviceName)
                LepuBleLog.d(tag, "it.hardwareV == " + it.hardwareV)
                LepuBleLog.d(tag, "it.softwareV == " + it.softwareV)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo).post(InterfaceEvent(model, pc6nDevice))
            }
        }

        if (response.token == TOKEN_PO_0F && response.type == TYPE_SPO2_PARAM){
            LepuBleLog.d(tag, "model:$model,EventPC60FwRtDataParam => success")
            PC60FwBleResponse.RtDataParam(response.content).let {
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataParam).post(InterfaceEvent(model, it))
                LepuBleLog.d(tag, "it.pi == " + it.pi)
                LepuBleLog.d(tag, "it.pr == " + it.pr)
                LepuBleLog.d(tag, "it.spo2 == " + it.spo2)
            }
        }

        if (response.token == TOKEN_PO_0F && response.type == TYPE_SPO2_WAVE){
            LepuBleLog.d(tag, "model:$model,EventPC60FwRtDataWave => success")
            LepuBleLog.d(tag, "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content))
            PC60FwBleResponse.RtDataWave(response.content).let {
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwRtDataWave).post(InterfaceEvent(model, it))
            }
        }
    }


    override fun getInfo() {
        sendCmd(Pc6nBleCmd.getDeviceSN())
        sendCmd(Pc6nBleCmd.getDeviceInfo())
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