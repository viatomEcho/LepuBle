package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.VcominData
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*

/**
 * vcomin胎心仪：
 * receive:
 * 1.实时心率
 */
class VcominFhrBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "VcominFhrBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (!isManagerInitialized()) {
            LepuBleLog.e(tag, "manager is not initialized")
            manager = Sp20BleManager(context)
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
    private fun onResponseReceived(response: ByteArray) {
        LepuBleLog.d(tag, "onResponseReceived bytes: ${bytesToHex(response)}")
        if (response.size < 5) {
            LepuBleLog.e(tag, "response.size:${response.size} error")
            return
        }
        val data = VcominData(response)
        LepuBleLog.d(tag, "received data : $data")
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VCOMIN.EventVcominRtHr).post(InterfaceEvent(model, data))
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 3) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-2) {
            if (bytes[i] != 0x55.toByte() || bytes[i+1] != 0xAA.toByte()) {
                continue@loop
            }

            // need content length
            val len = 2

            if (i+3+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+3+len)
            onResponseReceived(temp)

            val tempBytes: ByteArray? = if (i+3+len == bytes.size) null else bytes.copyOfRange(i+3+len, bytes.size)

            return hasResponse(tempBytes)
        }

        return bytesLeft
    }

    override fun syncTime() {
        LepuBleLog.e(tag, "syncTime not yet implemented")
    }

    override fun getInfo() {
        LepuBleLog.e(tag, "getInfo not yet implemented")
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

    override fun getRtData() {
        LepuBleLog.e(tag, "getRtData not yet implemented")
    }

    override fun getFileList() {
        LepuBleLog.e(tag, "getFileList not yet implemented")
    }

}