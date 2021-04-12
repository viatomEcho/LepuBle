import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.OxyBleManager
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.cmd.BpmBleCmd.MSG_TYPE_INVALID
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toHex

/**
 * author: wujuan
 * created on: 2021/3/31 10:18
 * description:
 */
class PC100BleInterface(model: Int): BleInterface(model) {

    private val tag: String = "PC100BleInterface"


    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = PC100BleManager(context)
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

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 6) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-5) {
            if (bytes[i] != 0xAA.toByte()) {
                continue@loop
            }

            if (bytes[i + 1] != 0x55.toByte()) {
                continue@loop
            }

            val token = bytes[i + 2]
            if(token == 0x10.toByte()) {
                continue@loop
            }

            val length = bytes[i + 3]
            if(length < 0) {
                continue@loop
            }

            if (i + 4 + length > bytes.size) {
//                continue@loop
                return bytes.copyOfRange(i, bytes.size)
            }
            val byteSize = bytes.size
            val indexTo = i + 4 + length
            val temp: ByteArray = bytes.copyOfRange(i, indexTo)
            val crcValue = PC100CrcUtil.calCRC8ByCCITT(temp)
            val tempCrc = temp[temp.size - 1]
//            Log.d(TAG, "Response pc100 crcValue == $crcValue tempCrc == $tempCrc")
            if(tempCrc == crcValue) {
                val msgType = PC100Cmd.getMsgType(temp)
//                Log.d(TAG, "Response pc100 msgType == $msgType HEX == ${Utils.bytesToHex(temp)}")
                if(msgType == PC100Cmd.MSG_TYPE_INVALID) {
                    continue@loop
                } else {
                    val bleResponse = PC100Response.PcBleResponse(temp)

                    onResponseReceived(bleResponse)

//                    if(PC100Cmd.getMsgType(bleResponse.bytes) == PC100Cmd.CMD_TYPE_SPO2_WAVE_DATA) { // spo2 wave data
//                        BaseWaveData.addWave(bleResponse)
//                    } else {
//                        EventBus.getDefault().postSticky((PcEvent(bleResponse)))
//                    }
//
                    val tempBytes: ByteArray? = bytes.copyOfRange(i + 4 + length, bytes.size)
                    return hasResponse(tempBytes)
                }
            } else {
                continue@loop
            }
        }

        return bytesLeft
    }

    private fun onResponseReceived(response: PC100Response.PcBleResponse) {

        PC100Cmd.getMsgType(response.bytes).let {
            when(it){
                //设备信息
                PC100Cmd.CMD_TYPE_GET_DEVICE_INFO -> {
                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_GET_DEVICE_INFO => success")
                    if (runRtImmediately) {
                        runRtTask()
                        runRtImmediately = false
                    }
                    val info = PC100Response.PcDeviceInfo(response.bytes)

                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100Info).post(InterfaceEvent(model, info))

                }
                //导联脱落
                PC100Cmd.CMD_TYPE_SPO2_FINGER_OUT -> {

                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_SPO2_FINGER_OUT => success")
                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100FingerOut).post(InterfaceEvent(model, true))

                }
                // 实时血氧
                PC100Cmd.CMD_TYPE_SPO2_RT_DATA -> {

                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_SPO2_RT_DATA => success")

                    val spo2Param = PC100Spo2Param(response.bytes)
                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100Spo2RtData).post(InterfaceEvent(model, spo2Param))
                }


                //血氧获取状态
                PC100Cmd.CMD_TYPE_SPO2_GET_STATE -> {

                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_SPO2_GET_STATE => success")

                    val pcStatus = PC100Response.PcStatus(response.bytes)
                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100Spo2GetState).post(InterfaceEvent(model, pcStatus))

                }
                //实时血压
                PC100Cmd.CMD_TYPE_BP_RT_DATA -> {

                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_BP_RT_DATA => success")

                    val bpData = PC100Response.PcBpData(response.bytes)
                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100BpRtData).post(InterfaceEvent(model, bpData))
                }

                // 血压开始
                PC100Cmd.CMD_TYPE_BP_START -> {

                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_BP_START => success")

                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100BpStart).post(InterfaceEvent(model, true))

                }
                //血压结束
                PC100Cmd.CMD_TYPE_BP_END -> {
                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_BP_END => success")

                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100BpEnd).post(InterfaceEvent(model, true))

                }
                //血压结果
                PC100Cmd.CMD_TYPE_BP_GET_RESULT -> {
                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_BP_GET_RESULT => success")

                    val type = response.type
                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100BpResult).post(InterfaceEvent(model, type))
                }

                PC100Cmd.CMD_TYPE_BP_GET_STATE -> {

                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_BP_GET_STATE => success")

                }

                PC100Cmd.CMD_TYPE_SPO2_START -> {
                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_SPO2_START => success")

                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100Spo2Start).post(InterfaceEvent(model, true))


                }
                PC100Cmd.CMD_TYPE_SPO2_END -> {
                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_SPO2_END => success")

                    LiveEventBus.get(InterfaceEvent.PC100.EventPC100Spo2End).post(InterfaceEvent(model, true))


                }

                PC100Cmd.CMD_TYPE_SPO2_WAVE_DATA -> {
                    LepuBleLog.d(tag, "model:$model, CMD_TYPE_SPO2_WAVE_DATA => success")

                }
                else -> {
                    LepuBleLog.w(tag, "model:$model, MSG_TYPE_INVALID")
                }

            }

        }
    }


    override fun getInfo() {
    }

    override fun syncTime() {
    }

    override fun getRtData() {
    }

    override fun getFileList() {
    }

    override fun dealReadFile(userId: String, fileName: String) {
    }

    override fun resetDeviceInfo() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }
}