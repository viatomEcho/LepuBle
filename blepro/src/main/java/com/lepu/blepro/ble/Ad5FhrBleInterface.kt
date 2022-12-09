package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.Ad5Data
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ad5胎心仪：
 * send:
 * 1.实时心率使能开关
 * receive:
 * 1.实时心率
 */
class Ad5FhrBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Ad5FhrBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = if (model == Bluetooth.MODEL_VTM_AD5) {
            Ad5FhrBleManager(context)
        } else {
            MdFhrBleManager(context)
        }
        manager?.let {
            it.isUpdater = isUpdater
            it.setConnectionObserver(this)
            it.notifyListener = this
            it.connect(device)
                .useAutoConnect(false)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "manager.connect done")
                    enableRtData(true)
                }
                .enqueue()
        } ?: kotlin.run {
            LepuBleLog.d(tag, "manager == null")
        }
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: ByteArray) {
        LepuBleLog.d(tag, "onResponseReceived received : ${bytesToHex(response)}")
        val data = Ad5Data(response)
        LepuBleLog.d(tag, "received data : $data")
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AD5.EventAd5RtHr).post(InterfaceEvent(model, data))
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
        LepuBleLog.d(tag, "enableRtData enable:$enable")
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