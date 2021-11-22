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

class Bpw1BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Bpw1BleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Bpw1BleManager(context)
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
    private fun onResponseReceived(response: Bpw1BleResponse.Bpw1Response) {
        LepuBleLog.d(tag, "received cmd : " + bytesToHex(response.bytes))

    }

    var tempBytes = ByteArray(23)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null) return bytes

        val bleResponse = Bpw1BleResponse.Bpw1Response(bytes)
        onResponseReceived(bleResponse)

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