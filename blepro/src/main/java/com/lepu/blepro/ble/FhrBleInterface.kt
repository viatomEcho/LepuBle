package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*

/**
 *
 * 蓝牙操作
 */

class FhrBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "FhrBleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = FhrBleManager(context)
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

    override fun dealReadFile(userId: String, fileName: String) {

    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: FhrBleResponse.FhrResponse) {
        LepuBleLog.d(tag, "received cmd : ${response.cmd}")
        //音频数据 byte[5]=0x0a：a55a0013 0a 911e12b34e19b790808088879a06b0 900000
        //设备数据 byte[5]=0x04：a55a000f 04 0450363533050500030006014a4200
        LepuBleLog.d(tag, "received len : ${response.len}")
        LepuBleLog.d(tag, "received content : ${bytesToHex(response.content)}")
        LepuBleLog.d(tag, "received bytes : ${bytesToHex(response.bytes)}")
        LepuBleLog.d(tag, "received bytes.size : ${response.bytes.size}")

        when (response.cmd) {
            0x04 -> {
                val info = FhrBleResponse.DeviceInfo(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.FHR.EventFhrDeviceInfo).post(InterfaceEvent(model, info))
            }
            0x0a -> {
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.FHR.EventFhrAudioData).post(InterfaceEvent(model, response.content))
            }
        }


    }

    var tempBytes = ByteArray(23)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null) return bytes

        if (bytes.size == 20) {
            if (bytes[4] == 0x04.toByte()) {
                val bleResponse = FhrBleResponse.FhrResponse(bytes)
                onResponseReceived(bleResponse)
            } else if (bytes[4] == 0x0A.toByte()) {
                for (i in bytes.indices)
                    tempBytes[i] = bytes[i]
            }
        } else if (bytes.size == 3) {
            for (i in bytes.indices)
                tempBytes[i+20] = bytes[i]
            val bleResponse = FhrBleResponse.FhrResponse(tempBytes)
            onResponseReceived(bleResponse)
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
    }

    override fun syncTime() {

    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
    }

    /**
     * get file list
     */
    override fun getFileList() {
    }

}