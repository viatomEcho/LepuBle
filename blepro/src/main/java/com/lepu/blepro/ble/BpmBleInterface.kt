package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.BpmBleCmd
import com.lepu.blepro.ble.cmd.BpmBleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex

/**
 * bpm血压计：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取列表记录
 * 4.获取设备状态
 * 5.开始/结束测量
 * receive:
 * 1.血压正常测量结果
 * 2.错误测量结果
 */
class BpmBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "BpmBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = BpmBleManager(context)
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

    // 数据记录下载标志
    private var isUserAEnd: Boolean = true
    private var isUserBEnd: Boolean = true

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 5) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-4) {
            if (bytes[i] != 0x02.toByte()) {
                continue@loop
            }

            if (bytes[i + 1] != 0x40.toByte()) {
                continue@loop
            }

            if (bytes[i + 2] != 0xdd.toByte()) {
                continue@loop
            }

            val length = bytes[i + 3]
            if(length < 0) {
                continue@loop
            }
            if (i + 5 + length > bytes.size) {
                return bytes.copyOfRange(i, bytes.size)
            }
//            val cmd = bytes[i + 4]  // 数据标志位
            val temp: ByteArray = bytes.copyOfRange(i, i + 5 + length)
            if (temp.last() == BpmBleCmd.calcNum(temp)) {
                onResponseReceived(temp)

                val tempBytes: ByteArray? = if (i + 5 + length == bytes.size) null else bytes.copyOfRange(i + 5 + length, bytes.size)
                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }


    private fun onResponseReceived(bytes: ByteArray) {
        LepuBleLog.d(tag, " onResponseReceived : " + bytesToHex(bytes))
        when(BpmBleCmd.getMsgType(bytes)) {
            BpmBleCmd.BPMCmd.MSG_TYPE_GET_INFO -> {
                //设备信息
                val deviceInfo = if (device.name == null) {
                    BpmDeviceInfo(bytes, "")
                } else {
                    BpmDeviceInfo(bytes, device.name)
                }

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmInfo).post(InterfaceEvent(model, deviceInfo))
                // 本版本注释，测试通过后删除
                /*if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }*/

            }
            BpmBleCmd.BPMCmd.MSG_TYPE_SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,MSG_TYPE_SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmSyncTime).post(InterfaceEvent(model, true))
            }
            BpmBleCmd.BPMCmd.MSG_TYPE_GET_BP_STATE -> {

                //发送实时state : byte
                val data = BpmBleResponse.RtState(bytes)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmState).post(InterfaceEvent(model, data))
                LepuBleLog.d(tag, "model:$model,MSG_TYPE_GET_BP_STATE => success $data")
            }
            BpmBleCmd.BPMCmd.MSG_TYPE_GET_RECORDS -> {
                //获取留存记录

                val data = BpmCmd(bytes)
                BpmBleResponse.RecordData(bytes).let {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordData).post(InterfaceEvent(model, it))
                    LepuBleLog.d(tag, "model:$model,MSG_TYPE_GET_RECORDS => success $it")

                    if (data.type == 0xB3.toByte()) {
                        if (bytes[11] == 0x00.toByte()) {
                           isUserAEnd = true
                        }
                        if (bytes[11] == 0x01.toByte()) {
                           isUserBEnd = true
                        }
                        if (isUserAEnd && isUserBEnd) {
                            //AB都读取完成
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordEnd).post(InterfaceEvent(model, true))
                        }
                    }
               }

            }
            BpmBleCmd.BPMCmd.MSG_TYPE_GET_RESULT -> {
                // 返回测量数据
                val data = BpmBleResponse.RecordData(bytes)
                LepuBleLog.d(tag, "model:$model,MSG_TYPE_GET_RESULT => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureResult).post(InterfaceEvent(model, data))
            }
            BpmBleCmd.BPMCmd.MSG_TYPE_ERROR_RESULT -> {
                // 返回测量数据错误
                val data = BpmBleResponse.ErrorResult(bytes)
                LepuBleLog.d(tag, "model:$model,MSG_TYPE_ERROR_RESULT => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmMeasureErrorResult).post(InterfaceEvent(model, data))
            }
            else -> {
                //实时指标
                val data = BpmBleResponse.RtData(bytes)
                LepuBleLog.d(tag, "model:$model,EventBpmRtData => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRtData).post(InterfaceEvent(model, data))
            }
        }
    }

    override fun getInfo() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_GET_INFO))
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_SET_TIME))
        LepuBleLog.d(tag, "syncTime...")
    }

    override fun getRtData() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_GET_BP_STATE))
        LepuBleLog.d(tag, "getRtData...device state")
    }

    override fun getFileList() {
        isUserAEnd = false
        isUserBEnd = false
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_GET_RECORDS))
        LepuBleLog.d(tag, "getFileList...")
    }

    fun getBpmFileList(map: HashMap<String, Any>){
        isUserAEnd = false
        isUserBEnd = false
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_GET_RECORDS, map))
        LepuBleLog.d(tag, "getBpmFileList...")
    }

    fun getRtState() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_GET_BP_STATE))
        LepuBleLog.d(tag, "getRtState...device state")
    }

    fun startBp() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_START_BP))
        LepuBleLog.d(tag, "startBp...")
    }

    fun stopBp() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_STOP_BP))
        LepuBleLog.d(tag, "stopBp...")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset Not yet implemented")
    }
    override fun factoryReset() {
        LepuBleLog.e(tag, "factoryReset Not yet implemented")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryResetAll Not yet implemented")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealReadFile Not yet implemented")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF Not yet implemented")
    }


}