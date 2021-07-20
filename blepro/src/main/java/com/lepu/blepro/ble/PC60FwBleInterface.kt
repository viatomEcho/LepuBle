package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.cmd.PC60FwBleResponse.PC60FwResponse.Companion.TYPE_SPO2_PARAM
import com.lepu.blepro.ble.cmd.PC60FwBleResponse.PC60FwResponse.Companion.TYPE_SPO2_WAVE
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog


class PC60FwBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "PC60FwBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = PC60FwBleManager(context)
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
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

            val token = bytes[i + 2]
            val length = bytes[i + 3]
            if (length < 0) {
                continue@loop
            }
            if (i + 4 + length > bytes.size) {
//                continue@loop
                return bytes.copyOfRange(i, bytes.size)
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 4 + length)

            val bleResponse = PC60FwBleResponse.PC60FwResponse(temp)
            onResponseReceived(bleResponse)
            val tempBytes: ByteArray? = bytes.copyOfRange(i + 4 + length, bytes.size)
            return hasResponse(tempBytes)
        }
        return bytesLeft
    }



    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: PC60FwBleResponse.PC60FwResponse) {
//        if (response.token == TOKEN_EPI_F0 && response.type == TYPE_BATTERY_LEVEL){
//           PC60FwBleResponse.Battery(response.bytes).let {
//               LiveEventBus.get(InterfaceEvent.PC60Fw.EventPC60FwBattery).post(InterfaceEvent(model, it ))
//           }
//
//        }

        if (response.token == TOKEN_PO_0F && response.type == TYPE_SPO2_PARAM){
            PC60FwBleResponse.RtDataParam(response.bytes).let {
                LiveEventBus.get(InterfaceEvent.PC60Fw.EventPC60FwRtDataParam).post(InterfaceEvent(model, it))
            }

        }

        if (response.token == TOKEN_PO_0F && response.type == TYPE_SPO2_WAVE){
            PC60FwBleResponse.RtDataWave(response.bytes).let {
                LiveEventBus.get(InterfaceEvent.PC60Fw.EventPC60FwRtDataWave).post(InterfaceEvent(model, it))
            }

        }
    }


    override fun getInfo() {
    }

    override fun syncTime() {
    }

    override fun getRtData() {
    }

    override fun getFileList() {
    }

    override fun dealReadFile(userId: String, fileName: String) {
    }

    override fun resetDeviceInfo() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }


}