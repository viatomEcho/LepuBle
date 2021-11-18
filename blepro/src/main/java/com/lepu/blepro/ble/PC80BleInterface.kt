package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import java.util.*

/**
 *
 * 蓝牙操作
 */

class PC80BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "PC80BleInterface"

    private lateinit var context: Context

    private var curFile: PC80BleResponse.RtRecordData? = null

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = PC80BleManager(context)
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
    private fun onResponseReceived(response: PC80BleResponse.PC80Response) {
        LepuBleLog.d(tag, "received: ${response.cmd}")
        when(response.cmd) {
            PC80BleCmd.HEARTBEAT -> {
                LepuBleLog.d(tag, "model:$model,HEARTBEAT => success")
                val batLevel = (response.content[0].toUInt() and 0xFFu).toInt()
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bBatLevel).post(InterfaceEvent(model, batLevel))
            }

            PC80BleCmd.TIME_SET -> {
                LepuBleLog.d(tag, "model:$model,TIME_SET => success")
                syncTime()
            }

            PC80BleCmd.GET_INFO -> {
                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LepuBleLog.d(tag, "model:$model,GET_INFO response.len => " + response.len)
                val info = PC80BleResponse.DeviceInfo(response.content, response.len)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo).post(InterfaceEvent(model, info))
            }

            // 查询设备信息无应答
            PC80BleCmd.GET_RATE -> {
                LepuBleLog.d(tag, "model:$model,GET_RATE => success")
                LepuBleLog.d(tag, "model:$model,GET_RATE response.len => " + response.len)
            }

            // 建立会话应答
            PC80BleCmd.TRANS_SET -> {
                LepuBleLog.d(tag, "model:$model,TRANS_SET => success")
                sendCmd(PC80BleCmd.responseTransSet(PC80BleCmd.ACK))
                curFile = null
                curFile = PC80BleResponse.RtRecordData(PC80BleCmd.SCP_ECG_LENGTH, 0)
            }

            PC80BleCmd.DATA_MESS -> {
                LepuBleLog.d(tag, "model:$model,DATA_MESS => success")
                LepuBleLog.d(tag, "model:$model,DATA_MESS response.len => " + response.len)
                curFile?.apply {
                    this.addContent(response.content)
                    LepuBleLog.d(tag, "model:$model,DATA_MESS info seqNo => " + this.seqNo)

                    val nowSize: Long = (this.index).toLong()
                    val size :Long= nowSize * 100
                    val poSize :Int= (size).div(this.fileSize).toInt()
                    LepuBleLog.d(tag, "model:$model,DATA_MESS info poSize => $poSize")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress).post(InterfaceEvent(model, poSize))

                    if (response.len == 1) {
                        if (this.index != this.fileSize){
                            LepuBleLog.d(tag, "model:$model,DATA_MESS EventEr1ReadFileError")
                            sendCmd(PC80BleCmd.responseDataMess(this.seqNo, PC80BleCmd.NAK))
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileError).post(InterfaceEvent(model, true))
                        }else {
                            LepuBleLog.d(tag, "model:$model,DATA_MESS EventPC80BReadFileComplete")
                            sendCmd(PC80BleCmd.responseDataMess(this.seqNo, PC80BleCmd.ACK))
                            val scpEcgFile = PC80BleResponse.ScpEcgFile(this.content)
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileComplete).post(InterfaceEvent(model, scpEcgFile))
                        }
                    }
                }
            }

            PC80BleCmd.TRACK_DATA_MESS -> {
                LepuBleLog.d(tag, "model:$model,TRACK_DATA_MESS => success")
                val info = PC80BleResponse.RtTrackData(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bTrackData).post(InterfaceEvent(model, info))
            }

        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes
        if (bytes == null || bytes.size < 4) {
            return bytes
        }
        loop@ for (i in 0 until bytes.size-3) {
            if (bytes[i] != 0xA5.toByte()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+2, i+3))
            if (i+4+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)
            if (temp.last() == CrcUtil.calCRC8PC(temp)) {
                val bleResponse = PC80BleResponse.PC80Response(temp)
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
        sendCmd(PC80BleCmd.getInfo())
    }

    override fun syncTime() {
        sendCmd(PC80BleCmd.setTime())
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

    fun sendHeartbeat() {
        sendCmd(PC80BleCmd.sendHeartbeat())
    }

}