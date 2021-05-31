package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.Bp2BleCmd
import com.lepu.blepro.ble.cmd.Bp2BleResponse
import com.lepu.blepro.ble.data.BpmCmd
import com.lepu.blepro.ble.data.BpmDeviceInfo
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil.calCRC8
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import com.viatom.ktble.ble.objs.Bp2DeviceInfo
import com.viatom.ktble.ble.objs.KtBleFileList

/**
 * author: wujuan
 * created on: 2021/2/5 15:13
 * description:
 */
class Bp2BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Bp2BleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Bp2BleManager(context)
        manager.isUpdater = isUpdater
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



    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
            if (i+8+len > bytes.size) {
                return bytes.copyOfRange(i, bytes.size)
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == calCRC8(temp)) {
                val bleResponse = Bp2BleResponse.BleResponse(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)
                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }


    private fun onResponseReceived(bytes: Bp2BleResponse.BleResponse) {
        LepuBleLog.d(tag, " onResponseReceived : " + bytes.cmd)
        when(bytes.cmd) {
            Bp2BleCmd.BPMCmd.CMD_INFO -> {
                val info = Bp2DeviceInfo(bytes.content)
                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get(InterfaceEvent.BP2.EventBp2Info).post(InterfaceEvent(model, info))


            }
            Bp2BleCmd.BPMCmd.CMD_SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,MSG_TYPE_SET_TIME => success")
                LiveEventBus.get(InterfaceEvent.BP2.EventBp2SyncTime).post(InterfaceEvent(model, true))
            }
            Bp2BleCmd.BPMCmd.CMD_FILE_LIST -> {

                LepuBleLog.d(tag, "model:$model,MSG_TYPE_GET_BP_STATE => success")
                //发送实时state : byte
                val list  = KtBleFileList(bytes.content)
                LiveEventBus.get(InterfaceEvent.BP2.EventBp2FileList).post(InterfaceEvent(model, list))
            }
//            Bp2BleCmd.BPMCmd.MSG_TYPE_GET_RECORDS -> {
//                //获取留存记录
//                LepuBleLog.d(tag, "model:$model,MSG_TYPE_GET_RECORDS => success")
//
//               BpmCmd(bytes).let {
//                   LiveEventBus.get(InterfaceEvent.BPM.EventBpmRecordData).post(InterfaceEvent(model, it ))
//
//                   if (it.type == 0xB3.toByte()) {
//                       if (bytes[11] == 0x00.toByte()) {
//                           isUserAEnd = true
//                       }
//                       if (bytes[11] == 0x01.toByte()) {
//                           isUserBEnd = true
//                       }
//                       if (isUserAEnd && isUserBEnd) {
//                           //AB都读取完成
//                           LiveEventBus.get(InterfaceEvent.BPM.EventBpmRecordEnd).post(InterfaceEvent(model, true ))
//                       }
//                   }
//               }
//
//            }
//            Bp2BleCmd.BPMCmd.MSG_TYPE_GET_RESULT -> {
//                // 返回测量数据
//                LiveEventBus.get(InterfaceEvent.BPM.EventBpmMeasureResult).post(InterfaceEvent(model, BpmCmd(bytes) ))
//            }
//            else -> {
//                //实时指标
//                LiveEventBus.get(InterfaceEvent.BPM.EventBpmRtData).post(InterfaceEvent(model, BpmCmd(bytes) ))
//
//            }
        }
    }



    override fun getInfo() {
        LepuBleLog.d(tag, "getInfo...")
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_GET_INFO))
    }

    override fun syncTime() {
        LepuBleLog.d(tag, "syncTime...")
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_SET_TIME))
    }

    override fun updateSetting(type: String, value: Any) {
    }


    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_GET_BP_STATE))
    }

    override fun getFileList() {
    }

    fun getBpmFileList( map: HashMap<String, Any>){
        isUserAEnd = false
        isUserBEnd = false
        LepuBleLog.d(tag, "getBpmFileList...")
       sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_GET_RECORDS, map))
    }

    override fun dealReadFile(userId: String, fileName: String) {
    }
    override fun resetDeviceInfo() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }



    fun startBp() {
        LepuBleLog.d(tag, "startBp...")
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_START_BP))
    }

    fun stopBp() {
        LepuBleLog.d(tag, "stopBp...")
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_STOP_BP))
    }





}