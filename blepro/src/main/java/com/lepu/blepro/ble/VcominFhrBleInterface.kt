package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.FhrData
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt

/**
 *
 * 蓝牙操作
 */

class VcominFhrBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "VcominFhrBleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Sp20BleManager(context)
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
        LepuBleLog.d(tag, "received : ${bytesToHex(response)}")

        val cmd = byte2UInt(response[2])
        val hr1 = byte2UInt(response[3])
        val hr2 = byte2UInt(response[4])
        val hr = if (hr2 != 0) hr2 else hr1

        LepuBleLog.d(tag, "received cmd : $cmd")
        LepuBleLog.d(tag, "received hr : $hr")
        LepuBleLog.d(tag, "received hr1 : $hr1")
        LepuBleLog.d(tag, "received hr2 : $hr2")

        val data = FhrData()
        data.hr1 = hr1
        data.hr2 = hr2

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
    }

    /**
     * get device info
     */
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