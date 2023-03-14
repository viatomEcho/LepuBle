package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * 呼吸机：
 * send:
 * 1.同步时间/UTC时间
 * 2.获取设备信息
 * 3.获取实时数据
 * 4.设置系统开关
 * 5.设置心率、温度高低阈值
 * 6.设置温度单位
 * 7.复位
 * 8.恢复出厂设置
 * 9.恢复生产状态
 * 10.烧录信息
 */
class R20BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "R20BleInterface"

    private var fileName = ""
    private var fileId = 0
    private var fileSize = 0
    private var fileContent: ByteArray? = null

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Er1BleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Er1BleManager(context)
            LepuBleLog.d(tag, "!isManagerInitialized, manager.create done")
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
    private fun onResponseReceived(response: R20BleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        when(response.cmd) {
            R20BleCmd.ECHO -> {
                LepuBleLog.d(tag, "model:$model,ECHO => success ${bytesToHex(response.content)}")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.R20.EventR20EchoData).post(InterfaceEvent(model, response))
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != bytes[i+2].inv()) {
                continue@loop
            }

            // seqNo
            /*if (bytes[i+1].toInt() == LeResSBleCmd.FILE_READ_PKG) {
                if (bytes[i+4].toInt() != (LeResSBleCmd.seqNo-1)) {
                    continue@loop
                }
            }*/

            // need content length
            val len = toUInt(bytes.copyOfRange(i+5, i+7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)
            if (temp.size < 7) {
                continue@loop
            }
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = R20BleResponse.BleResponse(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        LepuBleLog.d(tag, "syncTime...")
    }

    override fun reset() {
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryReset() {
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun factoryResetAll() {
        LepuBleLog.d(tag, "factoryResetAll...")
    }

    /**
     * get real-time data
     */
    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
        LepuBleLog.d(tag, "dealContinueRF...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        LepuBleLog.d(tag, "dealReadFile...")
    }

    /**
     * get file list
     */
    override fun getFileList() {
        LepuBleLog.d(tag, "getFileList...")
    }

    fun echo(data: ByteArray) {
        sendCmd(R20BleCmd.echo(data))
        LepuBleLog.d(tag, "echo...")
    }

}