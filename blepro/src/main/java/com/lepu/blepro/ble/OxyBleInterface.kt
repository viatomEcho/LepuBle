package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.BleCRC
import com.lepu.blepro.ble.cmd.OxyBleCmd
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toHex
import com.lepu.blepro.utils.toUInt
import java.util.*
import kotlin.concurrent.schedule
import kotlin.experimental.inv


class OxyBleInterface(model: Int): BleInterface(model) {


    private var curCmd: Int = 0
    /**
     * 是否需要发送实时指令，不会停止实时任务
     */
    private var mIsNeedRtCmd = true


    override fun initManager(context: Context, device: BluetoothDevice) {
        manager = OxyBleManager(context)
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
                .useAutoConnect(true)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d("Device Init")
                }
                .enqueue()
    }


    override fun getRtData() {
        if (mIsNeedRtCmd) {
            sendOxyCmd(OxyBleCmd.OXY_CMD_RT_DATA,OxyBleCmd.getRtWave())
        }
    }


    var curFileName: String? = null
    var curFile: OxyBleResponse.OxyFile? = null

    lateinit var userId: String

    override fun readFile(userId: String, fileName: String) {
        this.curFileName = fileName
        this.userId = userId
        LepuBleLog.d("$userId 将要读取文件 $curFileName" )
//        20201210095928
//        AA03FC00000F003230323031323130303935393238004C
        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_START, OxyBleCmd.readFileStart(fileName))
    }


    private fun sendOxyCmd(cmd: Int, bs: ByteArray){
        if (curCmd != 0) {
            // busy
            LepuBleLog.d("busy: $cmd =>$curCmd")
            return
        }
        sendCmd(bs)
        curCmd = cmd
    }

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size - 7) {
            if (bytes[i] != 0x55.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
//            Log.d(TAG, "want bytes length: $len")
            if (i + 8 + len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = OxyBleResponse.OxyResponse(temp)
//                Log.d(TAG, "get response: " + temp.toHex())
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i + 8 + len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }



    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: OxyBleResponse.OxyResponse) {
        LepuBleLog.d("Response: $curCmd, ${response.content.toHex()}")
        if (curCmd == 0) {
            return
        }

        when (curCmd) {
            OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()
                getInfo()
                LiveEventBus.get(EventMsgConst.Oxy.EventOxySyncDeviceInfo).post(true)
                LepuBleLog.d("同步完成")
            }

            OxyBleCmd.OXY_CMD_INFO -> {
                clearTimeout()
                val info = OxyBleResponse.OxyInfo(response.content)

                LiveEventBus.get(EventMsgConst.Oxy.EventOxyInfo).post(info)
                LepuBleLog.d("发送 info")
                // 即可开启实时任务
                runOxyRt()

            }

            OxyBleCmd.OXY_CMD_RT_DATA -> {
                clearTimeout()
                val rtWave = OxyBleResponse.RtWave(response.content)
                //发送实时数据
                LiveEventBus.get(EventMsgConst.Oxy.EventOxyRtData).post(rtWave)

            }
            OxyBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()
                if (response.state) {
                    val fileSize = toUInt(response.content)

                    LepuBleLog.d("文件大小：${fileSize}  文件名：$curFileName")
                    curFileName?.let {

                        curFile = OxyBleResponse.OxyFile(curFileName!!, fileSize, userId)
                        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    }

                } else {
                    LiveEventBus.get(EventMsgConst.Oxy.EventOxyReadFileError).post(true)
                    LepuBleLog.d("读文件失败：${response.content.toHex()}")
                }
            }

            OxyBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()
                curFile?.apply {

                    this.addContent(response.content)

                    LiveEventBus.get(EventMsgConst.Oxy.EventOxyReadingFileProgress).post((curFile!!.index * 1000).div(curFile!!.fileSize) )
                    LepuBleLog.d("读文件中：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")

                    if (this.index < this.fileSize) {
                        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    } else {
                        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_END, OxyBleCmd.readFileEnd())
                    }
                }
            }
            OxyBleCmd.OXY_CMD_READ_END -> {
                clearTimeout()
                LepuBleLog.d("读文件完成: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                curFileName = null

                LiveEventBus.get(EventMsgConst.Oxy.EventOxyReadFileComplete).post(curFile)
                curFile = null

            }

            OxyBleCmd.OXY_CMD_RESET -> {
                clearTimeout()
                LiveEventBus.get(EventMsgConst.Oxy.EventOxyResetDeviceInfo).post(true)
            }

            else -> {
                clearTimeout()
            }
        }
    }

    private fun clearTimeout() {
        curCmd = 0
    }


    override fun syncData(type: String, value: Int) {
        sendOxyCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.syncData(type, value))
    }

    override fun getInfo() {
        sendOxyCmd(OxyBleCmd.OXY_CMD_INFO,OxyBleCmd.getInfo())
    }

    override fun resetDeviceInfo() {
        sendOxyCmd(OxyBleCmd.OXY_CMD_RESET, OxyBleCmd.resetDeviceInfo())
    }



    override fun onDeviceReady(device: BluetoothDevice) {
        super.onDeviceReady(device)
        Timer().schedule(500) {
            syncData(OxyBleCmd.SYNC_TYPE_TIME,0)
        }
    }

    fun runOxyRt(){
        super.runRtTask(1000)
        LiveEventBus.get(EventMsgConst.RealTime.EventRealTimeStop).post(false)
    }

    fun oxyStopRT(){
        super.stopRtTask()
        LiveEventBus.get(EventMsgConst.RealTime.EventRealTimeStop).post(true)
    }

}