package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.BleCRC
import com.lepu.blepro.ble.cmd.CheckmePodBleCmd
import com.lepu.blepro.ble.cmd.CheckmePodBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

class CheckmePodBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "CheckmePodBleInterface"

    var curFileName: String? = null
    var curFile: CheckmePodBleResponse.OxiTFile? = null

    /**
     * 是否需要发送实时指令，不会停止实时任务
     */

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = OxyBleManager(context)
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
                .useAutoConnect(false) // true:可能自动重连， 程序代码还在执行扫描
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "Device Init")
                }
                .enqueue()
    }

    private fun sendOxyCmd(cmd: Int, bs: ByteArray){
        LepuBleLog.d(tag, "sendOxyCmd $cmd")

        if (curCmd != -1) {
            // busy
            LepuBleLog.d(tag, "busy: " + cmd.toString() + "\$curCmd =>" + java.lang.String.valueOf(curCmd))
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
                val bleResponse = CheckmePodBleResponse.BleResponse(temp)
//                Log.d(TAG, "get response: " + temp.toHex())
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i + 8 + len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)

                LepuBleLog.d("hasResponse", "end")

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }



    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: CheckmePodBleResponse.BleResponse) {
        LepuBleLog.d(tag, "Response: $curCmd, ${response.content.toHex()}")
        if (curCmd == -1) {
            return
        }

        when (curCmd) {
            CheckmePodBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model, OXY_CMD_PARA_SYNC => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodSetTime).post(
                    InterfaceEvent(model, true)
                )
            }

            CheckmePodBleCmd.OXY_CMD_INFO -> {

                clearTimeout()
                val info = CheckmePodBleResponse.DeviceInfo(response.content)

                if (runRtImmediately) {
                    runRtTask()
                    runRtImmediately = false
                }
                LepuBleLog.d(tag, "model:$model, OXY_CMD_INFO => success $info")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodDeviceInfo).post(InterfaceEvent(model, info))
            }

            CheckmePodBleCmd.OXY_CMD_READ_START -> {
                clearTimeout()

                if (response.state) {
                    val fileSize = toUInt(response.content)

                    LepuBleLog.d(tag, "model:$model, 文件大小：${fileSize}  文件名：$curFileName")
                    curFileName?.let {

                        curFile = CheckmePodBleResponse.OxiTFile(curFileName!!, fileSize)
                        sendOxyCmd(CheckmePodBleCmd.OXY_CMD_READ_CONTENT, CheckmePodBleCmd.readFileContent())
                    }

                } else {
                    LepuBleLog.d(tag, "model:$model, 读文件失败：${response.content.toHex()}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListError).post(
                        InterfaceEvent(model, true)
                    )
                }
            }
            CheckmePodBleCmd.OXY_CMD_RT_DATA -> {
                clearTimeout()
                if (response.len < 22) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtDataError)
                        .post(InterfaceEvent(model, true))
                    return
                }
                val data = CheckmePodBleResponse.RtData(response.content)
                LepuBleLog.d(tag, "model:$model, OXY_CMD_RT_DATA => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodRtData).post(
                    InterfaceEvent(model, data)
                )
            }

            CheckmePodBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()

                curFile?.apply {

                    this.addContent(response.content)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListProgress).post(InterfaceEvent(model, (curFile!!.index * 100).div(curFile!!.fileSize)))
                    LepuBleLog.d(tag, "model:$model, 读文件中：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")

                    if (this.index < this.fileSize) {
                        sendOxyCmd(CheckmePodBleCmd.OXY_CMD_READ_CONTENT, CheckmePodBleCmd.readFileContent())
                    } else {
                        sendOxyCmd(CheckmePodBleCmd.OXY_CMD_READ_END, CheckmePodBleCmd.readFileEnd())
                    }
                }
            }
            CheckmePodBleCmd.OXY_CMD_READ_END -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model, 读文件完成: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                curFileName = null // 一定要放在发通知之前

                curFile?.let {
                    val data = CheckmePodBleResponse.FileList(it.fileContent)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodFileList).post(
                        InterfaceEvent(model, data)
                    )
                    LepuBleLog.d(tag, "model:$model,  FileList $data")

                } ?: run {
                    LepuBleLog.d(tag, "model:$model,  curFile error!!")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListError)
                        .post(InterfaceEvent(model, true))
                }

                curFile = null

            }

            else -> {
                clearTimeout()
            }
        }
    }

    private fun clearTimeout() {
        curCmd = -1
    }

    /**
     * 注意默认获取实时参数
     */
    override fun getRtData() {
        sendOxyCmd(CheckmePodBleCmd.OXY_CMD_RT_DATA, CheckmePodBleCmd.getRtData())// 无法支持1.4.1之前获取pi
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.curFileName = fileName
        LepuBleLog.d(tag, "$userId 将要读取文件 $curFileName")
        sendOxyCmd(CheckmePodBleCmd.OXY_CMD_READ_START, CheckmePodBleCmd.readFileStart(fileName))
    }

    override fun syncTime() {
        sendOxyCmd(CheckmePodBleCmd.OXY_CMD_PARA_SYNC, CheckmePodBleCmd.syncTime())
    }

    fun updateSetting(type: String, value: Any) {
        sendOxyCmd(CheckmePodBleCmd.OXY_CMD_PARA_SYNC, CheckmePodBleCmd.updateSetting(type, value as Int))

    }
    override fun getFileList() {
        dealReadFile("", "oxi_T.dat")
    }

    override fun getInfo() {
        sendOxyCmd(CheckmePodBleCmd.OXY_CMD_INFO, CheckmePodBleCmd.getInfo())
    }

    override fun factoryReset() {
        LepuBleLog.e(tag, "factoryReset Not yet implemented")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryResetAll Not yet implemented")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset Not yet implemented")

    }

    override fun dealContinueRF(userId: String, fileName: String) {
       LepuBleLog.e(tag, "o2 暂不支持断点下载")
    }



}