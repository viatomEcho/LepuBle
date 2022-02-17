package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.Bp2BleCmd
import com.lepu.blepro.ble.cmd.Bp2BleCmd.BPMCmd.*
import com.lepu.blepro.ble.cmd.Bp2BleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil.calCRC8
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.toUInt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Runnable
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
                if (!isRtSateStop) getBpState() else LepuBleLog.d(tag, "isRtSateStop = $isRtSateStop")
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


    private fun onResponseReceived(bleResponse: Bp2BleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived : " + bleResponse.cmd)

        if (curCmd == -1) {
            LepuBleLog.e(tag, "onResponseReceived error curCmd = -1")
            return
        }
        clearCmdTimeout()
        when (bleResponse.cmd) {
            CMD_INFO -> {
                LepuBleLog.d(tag, "model:$model,CMD_INFO => success")

                val info = Bp2DeviceInfo(bleResponse.content, device.name)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info)
                    .post(InterfaceEvent(model, info))
                if (runRtImmediately) {
                    runRtTask()
                    runRtSateTask()
                    runRtImmediately = false
                }

            }
            CMD_SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,CMD_SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime)
                    .post(InterfaceEvent(model, true))
            }
            CMD_FILE_LIST -> {

                LepuBleLog.d(tag, "model:$model,CMD_FILE_LIST => success")
                //发送实时state : byte
                if (bleResponse.content.isNotEmpty()) {
                    val list = KtBleFileList(bleResponse.content, device.name)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList)
                        .post(InterfaceEvent(model, list))
                }
            }

            CMD_FILE_READ_START -> {
                LepuBleLog.d(tag, "model:$model,CMD_FILE_READ_START => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(MSG_TYPE_READ_END, fileReadEnd())
                    return
                }

                //检查返回是否异常
                if (bleResponse.type != Bp2BleResponse.BleResponse.TYPE_RESPONSE){

                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, CMD_FILE_READ_START => error")

                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError)
                        .post(InterfaceEvent(model, fileName))
                    return
                }

                fileContent = null
                fileSize = toUInt(bleResponse.content.copyOfRange(0, 4))
                LepuBleLog.d(tag, "download file $fileName CMD_FILE_READ_START fileSize == $fileSize")
                if (fileSize == 0) {
                    sendCmd(MSG_TYPE_READ_END, fileReadEnd())
                } else {
                    sendCmd(MSG_TYPE_READ_PKG, fileReadPkg(0))
                }
            }

            CMD_FILE_READ_PKG -> {
                LepuBleLog.d(tag, "model:$model,CMD_FILE_READ_PKG => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(MSG_TYPE_READ_END, fileReadEnd())
                    return
                }


                curSize += bleResponse.len
                val part = Bp2FilePart(fileName, fileSize, curSize)
                fileContent = add(fileContent, bleResponse.content)
                LepuBleLog.d(tag, "download file $fileName CMD_FILE_READ_PKG curSize == $curSize | fileSize == $fileSize")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress)
                    .post(InterfaceEvent(model, part))
                if (curSize < fileSize) {
                    sendCmd(MSG_TYPE_READ_PKG, fileReadPkg(curSize))
                } else {
                    sendCmd(MSG_TYPE_READ_END, fileReadEnd())
                }
            }

            CMD_FILE_READ_END -> {
                LepuBleLog.d(tag, "model:$model,CMD_FILE_READ_END => success")

                curSize = 0
                if (fileContent == null) fileContent = ByteArray(0)

                if (isCancelRF || isPausedRF){
                    LepuBleLog.d(tag, "已经取消/暂停下载 isCancelRF = $isCancelRF, isPausedRF = $isPausedRF" )
                    return
                }

                fileContent?.let {
                    if (it.isNotEmpty()) {
                        val file = Bp2BleFile(fileName, it, device.name)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete)
                            .post(InterfaceEvent(model, file))
                    }
                }


            }

            //实时波形
            CMD_BP2_RT_DATA -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_RT_DATA => success")

                if (bleResponse.content.size > 31) {
                    val rtData = Bp2BleRtData(bleResponse.content)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData)
                        .post(InterfaceEvent(model, rtData))
                }else{
                    Log.d(tag, "bytes.content.size < 31")
                }
            }
            //实时状态
            CMD_BP2_RT_STATE -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_RT_STATE => success")

                val rtState = Bp2BleRtState(bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2State)
                    .post(InterfaceEvent(model, rtState))

            }

            CMD_BP2_SET_SWITCHER_STATE -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_SET_SWITCHER_STATE => success")

                //心跳音开关
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSetConfigResult)
                        .post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSetConfigResult)
                        .post(InterfaceEvent(model, 1))
                }
            }
            CMD_BP2_CONFIG -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_CONFIG => success")

                //获取返回的开关状态
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
                        .post(InterfaceEvent(model, 0))
                } else {
                    if (bleResponse.content.size > 24 && bleResponse.content[24].toInt() == 1) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
                            .post(InterfaceEvent(model, 1))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpGetConfigResult)
                            .post(InterfaceEvent(model, 0))
                    }
                }
            }
            CMD_RESET -> {
                LepuBleLog.d(tag, "model:$model,CMD_RESET => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Reset)
                        .post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Reset)
                        .post(InterfaceEvent(model, 1))
                }
            }

            CMD_FACTORY_RESET -> {
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset)
                        .post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset)
                        .post(InterfaceEvent(model, 1))
                }
            }

            CMD_FACTORY_RESET_ALL -> {
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET_ALL => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryResetAll)
                        .post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryResetAll)
                        .post(InterfaceEvent(model, 1))
                }
            }

            SWITCH_STATE -> {
                LepuBleLog.d(tag, "model:$model,SWITCH_STATE => success")
                //切换状态
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSwitchState)
                        .post(InterfaceEvent(model, false))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBpSwitchState).post(InterfaceEvent(model, true))
                }
            }




        }

    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        if (!isRtSateStop)
            stopRtStateTask()
        super.onDeviceDisconnected(device, reason)
    }



    fun sendCmd(cmd: Int, bs: ByteArray) {
        if (curCmd == MSG_TYPE_BP2_RT_DATA && isRtStop  || curCmd == MSG_TYPE_BP2_RT_STATE && isRtSateStop){
            //实时已经停止, 实时切换到下载文件调用停止实时后应该延迟发送获取文件列表
            curCmd = -1
        }

        if (curCmd != -1) {
            // busy
            LepuBleLog.e(tag, "cmd busy:: to send cmd = $cmd, str = ${getCmdStr(cmd)}, curCmd => $curCmd")
            return
        }


        if (!super.sendCmd(bs)) return // 发送指令被拦截，不往下执行
        this.curCmd = cmd

        cmdTimeout = GlobalScope.launch {
            delay(3000)
            // timeout
            if (curCmd != -1){
                LepuBleLog.e(tag, "cmd timeout: $curCmd, str = ${getCmdStr(curCmd)}")
                LiveEventBus.get<InterfaceEvent>(EventMsgConst.Cmd.EventCmdResponseTimeOut).post(InterfaceEvent(model, curCmd))
            }
            curCmd = -1

        }




    }

    override fun getInfo() {
        LepuBleLog.d(tag, "getInfo...")
        sendCmd(MSG_TYPE_GET_INFO, Bp2BleCmd.getCmd(MSG_TYPE_GET_INFO))
    }

    override fun syncTime() {
        LepuBleLog.d(tag, "syncTime...")
        sendCmd(MSG_TYPE_SET_TIME, Bp2BleCmd.getCmd(MSG_TYPE_SET_TIME))
    }

    //实时波形命令
    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
        sendCmd(MSG_TYPE_BP2_RT_DATA, Bp2BleCmd.BPMCmd.getRtData())
    }

    //实时状态命令
    fun getRtBpState() {
        LepuBleLog.d(tag, "getRtState...")
        sendCmd(MSG_TYPE_BP2_RT_STATE, Bp2BleCmd.BPMCmd.getRtBpState())
    }


    fun setConfig(switch:Boolean, volume: Int){
        sendCmd(MSG_TYPE_SET_SWITCHER_STATE, Bp2BleCmd.BPMCmd.setConfig(switch, volume))
    }
     fun getConfig(){
        sendCmd(MSG_TYPE_GET_CONFIG, Bp2BleCmd.BPMCmd.getConfig())
    }

    override fun getFileList() {
        sendCmd(MSG_TYPE_GET_BP_FILE_LIST, Bp2BleCmd.getCmd(MSG_TYPE_GET_BP_FILE_LIST))
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(MSG_TYPE_READ_START, getFileStart(fileName.toByteArray(), 0))
    }
    override fun reset() {
        sendCmd(MSG_TYPE_RESET, Bp2BleCmd.BPMCmd.reset())
    }

    override fun factoryReset() {
        sendCmd(MSG_TYPE_FACTORY_RESET, Bp2BleCmd.BPMCmd.factoryReset())
    }

    override fun factoryResetAll() {
        sendCmd(MSG_TYPE_FACTORY_RESET_ALL, Bp2BleCmd.BPMCmd.factoryResetAll())
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
        LepuBleLog.e("enter  switchState： SWITCH_STATE===$state")

        sendCmd(MSG_TYPE_SWITCH_STATE, Bp2BleCmd.BPMCmd.switchState(state))
    }



}