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
//        LepuBleLog.d(tag, "received cmd : ${response.cmd}")
        //音频数据 byte[5]=0x0a：a55a0013 0a 911e12b34e19b790808088879a06b0 900000  23
        //设备数据 byte[5]=0x04：a55a000f 04 0450363533050500030006014a4200         20
//        LepuBleLog.d(tag, "received len : ${response.len}")
//        LepuBleLog.d(tag, "received content : ${bytesToHex(response.content)}")
//        LepuBleLog.d(tag, "received bytes : ${bytesToHex(response.bytes)}")
//        LepuBleLog.d(tag, "received bytes.size : ${response.bytes.size}")

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

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 5) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-4) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != 0x5A.toByte()) {
                continue@loop
            }

            // need content length
            var len = ((bytes[i+2].toUInt() and 0x0Fu).toInt() shl 8) + ((bytes[i+3].toUInt() and 0xFFu).toInt())  // 大端模式
            if (len == 15) {
                len += 1
            }

            if (i+4+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)

            // 16位校验有问题 暂时不进行校验
//            val crc16 = toUInt(bytes.copyOfRange(bytes.size - 2, bytes.size))  // 小端模式
//            val crc16 = ((bytes[bytes.size - 2].toUInt() and 0x0Fu).toInt() shl 8) + ((bytes[bytes.size - 1].toUInt() and 0xFFu).toInt())  // 大端模式
//            LepuBleLog.d(tag, "crc16 : ${crc16}")
//            LepuBleLog.d(tag, "CrcUtil.calCRC16(temp) : ${CrcUtil.calCRC16(temp)}")
//            if (crc16 == CrcUtil.calCRC16(temp)) {
                val bleResponse = FhrBleResponse.FhrResponse(temp)
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
//            }
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