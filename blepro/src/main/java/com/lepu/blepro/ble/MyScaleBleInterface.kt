package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.utils.*

/**
 *
 * 蓝牙操作
 */

class MyScaleBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "MyScaleBleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = MyScaleBleManager(context)
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
    private fun onResponseReceived(response: MyScaleBleResponse.MyScaleResponse) {
        LepuBleLog.d(tag, "received cmd : " + bytesToHex(response.bytes))

        when(response.cmd) {
            MyScaleBleCmd.WEIGHT_DATA -> {
                LepuBleLog.d(tag, "model:$model,WEIGHT_DATA => success")
                var info = MyScaleBleResponse.WeightData(response.content)
                LepuBleLog.d(tag, "model:$model,WEIGHT_DATA => info.toString() == " + info.toString())
            }
            MyScaleBleCmd.IMPEDANCE_DATA -> {
                LepuBleLog.d(tag, "model:$model,IMPEDANCE_DATA => success")
                var info = MyScaleBleResponse.ImpedanceData(response.content)
                LepuBleLog.d(tag, "model:$model,WEIGHT_DATA => info.toString() == " + info.toString())
            }
            MyScaleBleCmd.UNSTABLE_DATA -> {
                LepuBleLog.d(tag, "model:$model,UNSTABLE_DATA => success")
            }
            MyScaleBleCmd.OTHER_DATA -> {
                LepuBleLog.d(tag, "model:$model,OTHER_DATA => success")
            }
            MyScaleBleCmd.HISTORY_DATA -> {
                LepuBleLog.d(tag, "model:$model,HISTORY_DATA => success")
            }
        }

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 4) {
            return bytes
        }
        LepuBleLog.d(tag, "Device Init  " + bytesToHex(bytes))

        loop@ for (i in 0 until bytes.size-3) {
            if (bytes[i] != 0xAC.toByte() || bytes[i+1] != 0x27.toByte()) {
                continue@loop
            }

            val len = 16
            if (i+4+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)

            val crc = (temp.last().toUInt() and 0xFFu).toInt()

            if (crc == CrcUtil.calMyScaleCHK(temp)) {
                val bleResponse = MyScaleBleResponse.MyScaleResponse(temp)
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
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