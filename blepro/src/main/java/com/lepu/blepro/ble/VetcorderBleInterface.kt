package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.VetcorderInfo
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*

/**
 *
 * 蓝牙操作
 */

class VetcorderBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "VetcorderBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Er1BleManager(context)
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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: VetcorderInfo) {
        LepuBleLog.d(tag, "received: $response")
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Vetcorder.EventVetcorderInfo).post(InterfaceEvent(model, response))

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 4) {
            return bytes
        }

        LepuBleLog.d(tag, "model:$model,hasResponse => " + bytesToHex(bytes))

        loop@ for (i in 0 until bytes.size-3) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != 0x5A.toByte()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+2, i+3))
            LepuBleLog.d(tag, "want bytes length: $len")
            if (i+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+len)
//            if (temp.last() == CrcUtil.calCRC8Pc(temp)) {
                val bleResponse = VetcorderInfo(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+len == bytes.size) null else bytes.copyOfRange(i+len, bytes.size)

                return hasResponse(tempBytes)
//            }
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
        sendCmd(byteArrayOf(0x01))
    }

    override fun syncTime() {
    }


    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
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