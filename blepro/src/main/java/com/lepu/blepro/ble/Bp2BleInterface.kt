package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.Bp2BleCmd
import com.lepu.blepro.ble.cmd.Bp2BleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil.calCRC8
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * author: wujuan
 * created on: 2021/2/5 15:13
 * description:
 */
class Bp2BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Bp2BleInterface"


    var isRtSateStop: Boolean = true

    private  var rStateTask: RtSateTask = RtSateTask()
    private val rtStateHandler = Handler(Looper.getMainLooper())

    inner class RtSateTask : Runnable {
        override fun run() {
            LepuBleLog.d(tag, "RtSateTask running...")
            if (state) {
                rtStateHandler.postDelayed(rStateTask, delayMillis)
                if (!isRtSateStop) Bp2BleCmd.BPMCmd.getBpState() else LepuBleLog.d(tag, "isRtSateStop = $isRtSateStop")
            }else {
                LepuBleLog.d(tag, "ble state = false !!!!")
            }

        }
    }
    fun runRtSateTask() {
        LepuBleLog.d(tag, "runRtSateTask start..." )
        rtStateHandler.removeCallbacks(rStateTask)
        isRtSateStop = false
        rtStateHandler.postDelayed(rStateTask, 500)
        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStateStart).post(model)
    }

    fun stopRtStateTask(){
        LepuBleLog.d(tag, "stopRtStateTask start..." )
        isRtSateStop = true
        rtStateHandler.removeCallbacks(rStateTask)
        LiveEventBus.get<Int>(EventMsgConst.RealTime.EventRealTimeStateStop).post(model)

    }


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
    var fileSize: Int = 0
    var fileName:String=""
    var curSize: Int = 0
    var fileContent : ByteArray? = null

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != bytes[i+2].inv()) {
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
        LepuBleLog.d(tag, "onResponseReceived : " + bytes.cmd)

        when (bytes.cmd) {
            Bp2BleCmd.BPMCmd.CMD_INFO -> {
                val info = Bp2DeviceInfo(bytes.content, device.name)
                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info)
                    .post(InterfaceEvent(model, info))
                if (runRtImmediately) {
                    runRtTask()
                    runRtSateTask()
                    runRtImmediately = false
                }

            }
            Bp2BleCmd.BPMCmd.CMD_SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,MSG_TYPE_SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime)
                    .post(InterfaceEvent(model, true))
            }
            Bp2BleCmd.BPMCmd.CMD_FILE_LIST -> {

                LepuBleLog.d(tag, "model:$model,MSG_TYPE_GET_BP_STATE => success")
                //发送实时state : byte
                if (bytes.content.size > 0) {
                    val list = KtBleFileList(bytes.content, device.name)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList)
                        .post(InterfaceEvent(model, list))
                }
            }

            Bp2BleCmd.BPMCmd.CMD_FILE_READ_START -> {
                fileContent = null;
                fileSize = toUInt(bytes.content.copyOfRange(0, 4))
                Log.d(tag, "download file $fileName CMD_FILE_READ_START fileSize == $fileSize")
                if (fileSize == 0) {
                    sendCmd(Bp2BleCmd.BPMCmd.fileReadEnd())
                } else {
                    sendCmd(Bp2BleCmd.BPMCmd.fileReadPkg(0))
                }
            }

            Bp2BleCmd.BPMCmd.CMD_FILE_READ_PKG -> {
                curSize += bytes.len
                val part = Bp2FilePart(fileName, fileSize, curSize)
                fileContent = add(fileContent, bytes.content)
                Log.d(
                    tag,
                    "download file $fileName CMD_FILE_READ_PKG curSize == $curSize | fileSize == $fileSize"
                )

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress)
                    .post(InterfaceEvent(model, part))
                if (curSize < fileSize) {
                    sendCmd(Bp2BleCmd.BPMCmd.fileReadPkg(curSize))
                } else {
                    sendCmd(Bp2BleCmd.BPMCmd.fileReadEnd())
                }
            }

            Bp2BleCmd.BPMCmd.CMD_FILE_READ_END -> {
                curSize = 0
                if (fileContent == null) fileContent = ByteArray(0)
                if (fileContent!!.isNotEmpty()) {
                    val file: Bp2BleFile = Bp2BleFile(fileName, fileContent!!, device.name)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete)
                        .post(InterfaceEvent(model, file))
                } else {
                    //取消下载？？？
                    //   BleMsgUtils.broadcastMsg(mService!!, BleMsg.MSG_DOWNLOAD, BleMsg.CODE_CANCEL)
//                    LiveEventBus.get(InterfaceEvent.BP2.EventBp2ReadFileComplete).post(InterfaceEvent(model, file))
                }

            }

            //实时波形
            Bp2BleCmd.BPMCmd.CMD_BP2_RT_DATA -> {
                val rtData = Bp2Response.RtData(bytes.content)
                rtData.wave.waveFs?.let {
                    Er1DataController.receive(it)
                }

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData)
                    .post(InterfaceEvent(model, rtData))
            }
            //实时状态
            Bp2BleCmd.BPMCmd.CMD_BP2_RT_STATE -> {
                val rtState = Bp2BleRtState(bytes.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2State)
                    .post(InterfaceEvent(model, rtState))


            }

            Bp2BleCmd.BPMCmd.CMD_BP2_SET_SWITCHER_STATE -> {
                //心跳音开关
                if (bytes.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSetConfigResult)
                        .post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSetConfigResult)
                        .post(InterfaceEvent(model, 1))
                }
            }
            Bp2BleCmd.BPMCmd.CMD_BP2_CONFIG -> {
                //获取返回的开关状态
                if (bytes.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
                        .post(InterfaceEvent(model, 0))
                } else {
                    if (bytes.content != null && bytes.content.size > 24 && bytes.content[24].toInt() == 1) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
                            .post(InterfaceEvent(model, 1))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
                            .post(InterfaceEvent(model, 0))
                    }
                }
            }
            Bp2BleCmd.BPMCmd.CMD_RESET -> {
                //重置
                if (bytes.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset)
                        .post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset)
                        .post(InterfaceEvent(model, 1))
                }
            }

            Bp2BleCmd.BPMCmd.SWITCH_STATE ->{
                //切换状态
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSwitchState).post(InterfaceEvent(model, true))
            }




        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        if (!isRtSateStop)
            stopRtStateTask()
        super.onDeviceDisconnected(device, reason)
    }

    fun startBp() {
        LepuBleLog.d(tag, "getInfo...")
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_START_BP))
    }
     fun stopBp() {
        LepuBleLog.d(tag, "getInfo...")
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_STOP_BP))
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

    //实时波形命令
    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
        sendCmd(Bp2BleCmd.BPMCmd.getRtData())
//        sendCmd(Bp2BleCmd.BPMCmd.getRtState())//实时状态需不需要？
    }
    //实时状态命令
    fun getRtState() {
        LepuBleLog.d(tag, "getRtData...")
        sendCmd(Bp2BleCmd.BPMCmd.getRtState())
    }


    /**
     *
     */
    fun resetAll(){
        sendCmd(Bp2BleCmd.BPMCmd.resetAll())
    }
     fun setConfig(switch:Boolean){
        sendCmd(Bp2BleCmd.BPMCmd.setConfig(switch))
    }
     fun getConfig(){
        sendCmd(Bp2BleCmd.BPMCmd.getConfig())
    }

    override fun getFileList() {
        sendCmd(Bp2BleCmd.getCmd(Bp2BleCmd.BPMCmd.MSG_TYPE_GET_BP_FILE_LIST))
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(Bp2BleCmd.BPMCmd.getFileStart(fileName.toByteArray(), 0))
    }
    override fun resetDeviceInfo() {
    }

    override fun factoryReset() {
        sendCmd(Bp2BleCmd.BPMCmd.resetAll())
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }


    /**
     * 0：进入血压测量
     * 1：进入心电测量
     * 2：进入历史回顾
     * 3：进入开机预备状态
     * 4：关机
     * 5：进入理疗模式
     */
    fun switchState(state: Int){
        LepuBleLog.e("SWITCH_STATE===$state")

        sendCmd(Bp2BleCmd.BPMCmd.switchState(state))
    }










}