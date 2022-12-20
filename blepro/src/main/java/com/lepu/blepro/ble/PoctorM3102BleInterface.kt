package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.PoctorM3102Data
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*

/**
 * 三合一血糖、血酮、尿酸
 * receive：
 * 1.测量数据自动上发
 */
class PoctorM3102BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "PoctorM3102BleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (!isManagerInitialized()) {
            LepuBleLog.e(tag, "manager is not initialized")
            manager = PoctorM3102BleManager(context)
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
        val data = PoctorM3102Data(response)
        LepuBleLog.d(tag, "onResponseReceived bytes : $data")
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PoctorM3102.EventPoctorM3102Data).post(InterfaceEvent(model, data))
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {

        if (bytes == null || bytes.size < 2) {
            return bytes
        }

        loop@ for (i in bytes.indices) {
            if (bytes.last() != 0x0A.toByte() || bytes[bytes.size-2] != 0x0D.toByte()) {
                continue@loop
            }
            onResponseReceived(bytes)
            return null
        }

        return bytes

    }

    override fun getFileList() {
//        sendCmd(byteArrayOf(0xAA.toByte()))
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

}