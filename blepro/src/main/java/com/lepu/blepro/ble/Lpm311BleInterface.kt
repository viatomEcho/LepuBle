package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.Lpm311Data
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import java.nio.charset.StandardCharsets

/**
 *
 * 蓝牙操作
 */

class Lpm311BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Lpm311BleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Lpm311BleManager(context)
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
        val data = Lpm311Data(response)
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LPM311.EventLpm311Data).post(InterfaceEvent(model, data))
        sendCmd("disconnect".toByteArray(StandardCharsets.US_ASCII))
        LepuBleLog.d(tag, "onResponseReceived $data")
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {

        if (bytes == null || bytes.size < 44) {
            return bytes
        }

        val len = 44

        val temp: ByteArray = bytes.copyOfRange(0, len)
        onResponseReceived(temp)

        val tempBytes: ByteArray? = if (len == bytes.size) null else bytes.copyOfRange(len, bytes.size)

        return hasResponse(tempBytes)

    }

    override fun getFileList() {
        sendCmd("connect".toByteArray(StandardCharsets.US_ASCII))
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