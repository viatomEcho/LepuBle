package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.BleCRC
import com.lepu.blepro.ble.cmd.OxyBleCmd
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.OxyDataController
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toHex
import com.lepu.blepro.utils.toUInt
import java.util.*
import kotlin.concurrent.schedule
import kotlin.experimental.inv


class OxyBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "OxyBleInterface"



    var curFileName: String? = null
    var curFile: OxyBleResponse.OxyFile? = null

    private var userId: String? = null


    private var curCmd: Int = 0
    /**
     * 是否需要发送实时指令，不会停止实时任务
     */


    override fun initManager(context: Context, device: BluetoothDevice) {
        manager = OxyBleManager(context)
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
                .useAutoConnect(false) // true:可能自动重连， 程序代码还在执行扫描
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "Device Init")
                }
                .enqueue()
    }


    override fun getRtData() {
       sendOxyCmd(OxyBleCmd.OXY_CMD_RT_DATA,OxyBleCmd.getRtWave())
    }


    override fun readFile(userId: String, fileName: String) {
        this.curFileName = fileName
        this.userId = userId
        LepuBleLog.d(tag, "$userId 将要读取文件 $curFileName" )
//        20201210095928
//        AA03FC00000F003230323031323130303935393238004C
        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_START, OxyBleCmd.readFileStart(fileName))
    }


    private fun sendOxyCmd(cmd: Int, bs: ByteArray){
        LepuBleLog.d(tag, "sendOxyCmd $cmd" )
        if (curCmd != 0) {
            // busy
            LepuBleLog.d(tag, "busy: $cmd =>$curCmd")
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
        LepuBleLog.d(tag, "Response: $curCmd, ${response.content.toHex()}")
        if (curCmd == 0) {
            return
        }

        when (curCmd) {
            OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()

                LepuBleLog.d(tag, "model:$model, OXY_CMD_PARA_SYNC => success")
                LiveEventBus.get(InterfaceEvent.Oxy.EventOxySyncDeviceInfo).post(InterfaceEvent(model, true))

            }

            OxyBleCmd.OXY_CMD_INFO -> {
                clearTimeout()
                val info = OxyBleResponse.OxyInfo(response.content)

                LepuBleLog.d(tag, "model:$model, OXY_CMD_INFO => success")
                LiveEventBus.get(InterfaceEvent.Oxy.EventOxyInfo).post(InterfaceEvent(model, info))


                if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }

            }

            OxyBleCmd.OXY_CMD_RT_DATA -> {
                clearTimeout()
                val rtWave = OxyBleResponse.RtWave(response.content)

                OxyDataController.receive(rtWave.wFs)
                //发送实时数据
                LiveEventBus.get(InterfaceEvent.Oxy.EventOxyRtData).post(InterfaceEvent(model, rtWave))

            }
            OxyBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()

                if (response.state) {
                    val fileSize = toUInt(response.content)

                    LepuBleLog.d(tag, "model:$model, 文件大小：${fileSize}  文件名：$curFileName")
                    curFileName?.let {

                        curFile = userId?.let { it1 -> OxyBleResponse.OxyFile(model, curFileName!!, fileSize, it1) }
                        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    }

                } else {
                    LiveEventBus.get(InterfaceEvent.Oxy.EventOxyReadFileError).post(InterfaceEvent(model, true))
                    LepuBleLog.d(tag, "model:$model, 读文件失败：${response.content.toHex()}")
                }
            }

            OxyBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()

                curFile?.apply {

                    this.addContent(response.content)

                    LiveEventBus.get(InterfaceEvent.Oxy.EventOxyReadingFileProgress).post(InterfaceEvent(model,(curFile!!.index * 1000).div(curFile!!.fileSize) ))
                    LepuBleLog.d(tag, "model:$model, 读文件中：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")

                    if (this.index < this.fileSize) {
                        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_CONTENT, OxyBleCmd.readFileContent())
                    } else {
                        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_END, OxyBleCmd.readFileEnd())
                    }
                }
            }
            OxyBleCmd.OXY_CMD_READ_END -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model, 读文件完成: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                curFileName = null

                curFile?.let {
                    LiveEventBus.get(InterfaceEvent.Oxy.EventOxyReadFileComplete).post(InterfaceEvent(model, it))
                }?: LepuBleLog.d(tag, "model:$model,  curFile error!!")

                curFile = null

            }

            OxyBleCmd.OXY_CMD_RESET -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model,  OXY_CMD_RESET => success")

                LiveEventBus.get(InterfaceEvent.Oxy.EventOxyResetDeviceInfo).post(InterfaceEvent(model, true))
            }

            else -> {
                clearTimeout()
            }
        }
    }

    private fun clearTimeout() {
        curCmd = 0
    }


    override fun syncTime() {
        sendOxyCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.syncTime())
    }

    override fun updateSetting(type: String, value: Any) {
        sendOxyCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.updateSetting(type,value as Int))

    }
    override fun getFileList() {
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
            syncTime()
        }
    }

}