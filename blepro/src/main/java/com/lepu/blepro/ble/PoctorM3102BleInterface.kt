package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.PoctorM3102Data
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*

/**
 *
 * 蓝牙操作
 */

class PoctorM3102BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "PoctorM3102BleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = PoctorM3102BleManager(context)
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
    }

    override fun syncTime() {
    }

    override fun getInfo() {
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

    override fun getRtData() {
    }

}