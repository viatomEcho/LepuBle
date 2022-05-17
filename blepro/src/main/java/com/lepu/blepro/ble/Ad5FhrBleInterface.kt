package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * 蓝牙操作
 */

class Ad5FhrBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Ad5FhrBleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Ad5FhrBleManager(context)
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "Device Init")
                enableRtData(true)
            }
            .enqueue()
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: ByteArray) {
        LepuBleLog.d(tag, "received : ${bytesToHex(response)}")

        val cmd = byte2UInt(response[2])
        val sn = trimStr(toString(response.copyOfRange(3, 10)))
        val hr1 = byte2UInt(response[11])
        val hr2 = byte2UInt(response[12])
        val hr = if (hr1 != 0) hr1 else hr2

        LepuBleLog.d(tag, "received cmd : $cmd")
        LepuBleLog.d(tag, "received sn : $sn")
        LepuBleLog.d(tag, "received hr1 : $hr1")
        LepuBleLog.d(tag, "received hr2 : $hr2")
        LepuBleLog.d(tag, "received hr : $hr")

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AD5.EventAd5RtHr).post(InterfaceEvent(model, hr))

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 5) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-4) {
            if (bytes[i] != 0xFF.toByte() || bytes[i+1] != 0xFC.toByte()) {
                continue@loop
            }

            // need content length
            val len = 13

            if (i+4+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)
            onResponseReceived(temp)

            val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

            return hasResponse(tempBytes)
        }

        return bytesLeft
    }

    fun enableRtData(enable: Boolean) {
        if (enable) {
            GlobalScope.launch {
                delay(2000)
                sendCmd(byteArrayOf(0xFF.toByte(), 0xFD.toByte(), 0, 0, 0, 0, 0, 0, 0, 0, 0xFC.toByte()))
            }
        } else {
            GlobalScope.launch {
                delay(2000)
                sendCmd(byteArrayOf(0xFF.toByte(), 0xFA.toByte(), 0x0A.toByte(), 0x0A.toByte()))
            }
        }
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