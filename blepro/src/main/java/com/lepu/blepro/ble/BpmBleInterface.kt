package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.bpm.BpmBleCmd
import com.lepu.blepro.ble.data.BpmCmd
import com.lepu.blepro.ble.data.BpmDeviceInfo
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex

/**
 * author: wujuan
 * created on: 2021/2/5 15:13
 * description:
 */
class BpmBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "BpmBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice) {
        manager = BpmBleManager(context)
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
                .useAutoConnect(false)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "Device Init")
                }
                .enqueue()
    }


    // 数据记录下载标志
    private var isUserAEnd: Boolean = true
    private var isUserBEnd: Boolean = true

    /**
     * 是否开启自动获取设备实时数据的标志, true开启，false关闭
     * 获取文件列表和下载文件的时候会设置为关闭状态
     */
    var syncState = true

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
            val cmd = bytes[i + 4]  // 数据标志位
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
                val deviceInfo = BpmDeviceInfo(bytes, device.name)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get(InterfaceEvent.BPM.EventBpmInfo).post(InterfaceEvent(model, deviceInfo))

                if (runRtImmediately) {
                    runRtTask()
                    runRtImmediately = false
                }
            }
            BpmBleCmd.BPMCmd.MSG_TYPE_SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,MSG_TYPE_SET_TIME => success")
                LiveEventBus.get(InterfaceEvent.BPM.EventBpmSyncTime).post(InterfaceEvent(model, true))
            }
            BpmBleCmd.BPMCmd.MSG_TYPE_GET_BP_STATE -> {

                LepuBleLog.d(tag, "model:$model,MSG_TYPE_GET_BP_STATE => success")
                //发送实时state : byte
                LiveEventBus.get(InterfaceEvent.BPM.EventBpmRtData).post(InterfaceEvent(model, bytes))
            }
            BpmBleCmd.BPMCmd.MSG_TYPE_GET_RECORDS -> {
                //获取留存记录
                LepuBleLog.d(tag, "model:$model,MSG_TYPE_GET_RECORDS => success")

               BpmCmd(bytes).let {
                   LiveEventBus.get(InterfaceEvent.BPM.EventBpmRecordData).post(InterfaceEvent(model, it ))

                   if (it.type == 0xB3.toByte()) {
                       if (bytes[11] == 0x00.toByte()) {
                           isUserAEnd = true
                       }
                       if (bytes[11] == 0x01.toByte()) {
                           isUserBEnd = true
                       }
                       if (isUserAEnd && isUserBEnd) {
                           //AB都读取完成
                           LiveEventBus.get(InterfaceEvent.BPM.EventBpmRecordEnd).post(InterfaceEvent(model, true ))
                           syncState = true
                       }
                   }
               }

            }
            BpmBleCmd.BPMCmd.MSG_TYPE_GET_RESULT -> {
                // 返回测量数据
                LiveEventBus.get(InterfaceEvent.BPM.EventBpmMeasureResult).post(InterfaceEvent(model, BpmCmd(bytes) ))
            }
            else -> {
                //实时指标
                LiveEventBus.get(InterfaceEvent.BPM.EventBpmMeasureResult).post(InterfaceEvent(model, BpmCmd(bytes) ))

            }
        }
    }



    override fun getInfo() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_GET_INFO))
    }

    override fun syncTime() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_SET_TIME))
    }

    override fun updateSetting(type: String, value: Any) {
    }


    override fun getRtData() {
        sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_GET_BP_STATE))
    }

    override fun getFileList() {
    }

    fun getBpmFileList(model: Int, map: HashMap<String, Any>){
        if(!syncState) {
            return
        } else {
            syncState = false
        }
        isUserAEnd = false
        isUserBEnd = false
       sendCmd(BpmBleCmd.getCmd(BpmBleCmd.BPMCmd.MSG_TYPE_GET_RECORDS, map))

    }

    override fun dealReadFile(userId: String, fileName: String) {
    }
    override fun resetDeviceInfo() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }
    override fun onDeviceReady(device: BluetoothDevice) {
        super.onDeviceReady(device)
        syncTime()
    }





}