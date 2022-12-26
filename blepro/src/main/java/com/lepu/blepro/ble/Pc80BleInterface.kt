package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt

/**
 * pc80b心电设备：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取电量
 * receive:
 * 1.实时心电
 * 2.接收历史文件内容
 * 心电采样率：实时150HZ，存储150HZ
 * 心电增益：n * 1 / 330 = n * 0.003030303030303-----330倍
 */
class Pc80BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "PC80BleInterface"

    private var curFile: PC80BleResponse.RtRecordData? = null
    private var transType = 0

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Pc80BleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Pc80BleManager(context)
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
    private fun onResponseReceived(response: PC80BleResponse.PC80Response) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        when(response.cmd) {
            Pc80BleCmd.HEARTBEAT -> {
                LepuBleLog.d(tag, "model:$model,HEARTBEAT => success")
                if (response.content.isEmpty()) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val batLevel = (response.content[0].toUInt() and 0xFFu).toInt()
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bBatLevel).post(InterfaceEvent(model, batLevel))
            }

            Pc80BleCmd.TIME_SET -> {
                LepuBleLog.d(tag, "model:$model,TIME_SET => success")
                syncTime()
            }

            Pc80BleCmd.GET_INFO -> {
                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LepuBleLog.d(tag, "model:$model,GET_INFO response.len => " + response.len)
                if (response.content.size < 3) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val info = PC80BleResponse.DeviceInfo(response.content, response.len)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo).post(InterfaceEvent(model, info))
            }

            // 查询设备信息无应答
            Pc80BleCmd.GET_RATE -> {
                LepuBleLog.d(tag, "model:$model,GET_RATE => success")
                LepuBleLog.d(tag, "model:$model,GET_RATE response.len => " + response.len)
            }

            Pc80BleCmd.VERSION_SET -> {
                LepuBleLog.d(tag, "model:$model,TRANS_SET => success")
//                sendCmd(PC80BleCmd.versionSet(PC80BleCmd.ACK))
            }


            // 建立会话应答
            Pc80BleCmd.TRANS_SET -> {
                LepuBleLog.d(tag, "model:$model,TRANS_SET => success")
                val data = PC80BleResponse.TransSet(response.content)
                // 1---传输文件 0---连续测量模式实时
                transType = data.transType
                sendCmd(Pc80BleCmd.responseTransSet(Pc80BleCmd.ACK))
                if (transType == 1) {
                    curFile = null
                    curFile = PC80BleResponse.RtRecordData(Pc80BleCmd.SCP_ECG_LENGTH, 0)
                }
            }

            Pc80BleCmd.DATA_MESS -> {
                LepuBleLog.d(tag, "model:$model,DATA_MESS => success")
                LepuBleLog.d(tag, "model:$model,DATA_MESS response.len => " + response.len)
                if (transType == 1) {
                    curFile?.apply {
                        this.addContent(response.content)
                        LepuBleLog.d(tag, "model:$model,DATA_MESS info seqNo => " + this.seqNo)

                        val nowSize: Long = (this.index).toLong()
                        val size: Long = nowSize * 100
                        val poSize: Int = (size).div(this.fileSize).toInt()
                        LepuBleLog.d(tag, "model:$model,DATA_MESS info poSize => $poSize")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress).post(InterfaceEvent(model, poSize))
                        LepuBleLog.d(tag, "model======:${response.len}:${this.index}:${this.fileSize}")
                        sendCmd(Pc80BleCmd.responseDataMess(this.seqNo, Pc80BleCmd.ACK))
                        if (response.len == 1) {
                            if (this.index != this.fileSize) {
                                LepuBleLog.d(tag, "model:$model,DATA_MESS EventPc80bReadFileError")
                                sendCmd(Pc80BleCmd.responseDataMess(this.seqNo, Pc80BleCmd.NAK))
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileError).post(InterfaceEvent(model, true))
                            } else {
                                LepuBleLog.d(tag, "model:$model,DATA_MESS EventPC80BReadFileComplete")
                                sendCmd(Pc80BleCmd.responseDataMess(this.seqNo, Pc80BleCmd.ACK))
                                val scpEcgFile = PC80BleResponse.ScpEcgFile(this.content)
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileComplete).post(InterfaceEvent(model, scpEcgFile))
                            }
                        }
                    }
                } else if (transType == 0) {
                    if (response.len == 1) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousDataEnd).post(InterfaceEvent(model, true))
                        LepuBleLog.d(tag, "DATA_MESS response.len:${response.len}")
                        return
                    }
                    val data = PC80BleResponse.RtContinuousData(response.content)
                    sendCmd(Pc80BleCmd.responseDataMess(data.seqNo, Pc80BleCmd.ACK))
                    LepuBleLog.d(tag, "model:$model,DATA_MESS => RtContinuousData $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bContinuousData).post(InterfaceEvent(model, data))
                }
            }

            Pc80BleCmd.TRACK_DATA_MESS -> {
                if (response.content.size < 6) {
                    LepuBleLog.e(tag, "response.size:${response.content.size} error")
                    return
                }
                val info = PC80BleResponse.RtTrackData(response.content)
                LepuBleLog.d(tag, "model:$model,TRACK_DATA_MESS => success $info")
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
            if (temp.last() == CrcUtil.calCRC8Pc(temp)) {
                val bleResponse = PC80BleResponse.PC80Response(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }

    override fun getInfo() {
        sendCmd(Pc80BleCmd.getInfo())
        LepuBleLog.d(tag, "getInfo")
    }

    override fun syncTime() {
        sendCmd(Pc80BleCmd.setTime())
        LepuBleLog.d(tag, "syncTime")
    }

    fun sendHeartbeat() {
        sendCmd(Pc80BleCmd.sendHeartbeat())
        LepuBleLog.d(tag, "sendHeartbeat")
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

}