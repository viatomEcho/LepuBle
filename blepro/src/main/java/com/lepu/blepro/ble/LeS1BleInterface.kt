package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import kotlin.experimental.inv

/**
 *
 * 蓝牙操作
 */

class LeS1BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LeS1BleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = OxyBleManager(context)
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



    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    var curSize: Int = 0
    var fileSize: Int = 0
    var fileContent: ByteArray? = null
    var fileList: LeS1BleResponse.FileList? = null
    private var userId: String? = null

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
        sendCmd(LeS1BleCmd.readFileStart(fileName.toByteArray(), 0)) // 读开始永远是0
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: LeS1BleResponse.BleResponse) {
//        LepuBleLog.d(TAG, "received: ${response.cmd}")
        when(response.cmd) {
            LeS1BleCmd.GET_INFO -> {
                val info = LepuDevice(response.content)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success $info")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1Info).post(
                    InterfaceEvent(model, info)
                )
                if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }

            }

            LeS1BleCmd.RT_DATA -> {
                val rtData = LeS1BleResponse.RtData(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1RtData).post(InterfaceEvent(model, rtData))
                LepuBleLog.d(tag, "model:$model,RT_DATA => success $rtData")
            }

            LeS1BleCmd.READ_FILE_LIST -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_LIST => success")
                if (response.len == 0) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1NoFile).post(InterfaceEvent(model, true))
                    return
                }
                fileList = LeS1BleResponse.FileList(response.content)
                LepuBleLog.d(tag, "model:$model,READ_FILE_LIST => fileList ${fileList.toString()}")
                fileList?.let {
                    readFile("", it.fileList[0], 0)
                }
            }

            LeS1BleCmd.READ_FILE_START -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(LeS1BleCmd.readFileEnd())
                    return
                }

                if (response.pkgType == 0x01.toByte()) {
                    fileContent = null
                    fileSize = toUInt(response.content)
                    if (fileSize == 0) {
                        sendCmd(LeS1BleCmd.readFileEnd())
                    } else {
                        sendCmd(LeS1BleCmd.readFileData(offset))
                    }
                } else {
                    LepuBleLog.d(tag, "read file failed：${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadFileError).post(InterfaceEvent(model, true))
                }
            }

            LeS1BleCmd.READ_FILE_DATA -> {
                LepuBleLog.d(tag, "READ_FILE_DATA: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, curSize = $curSize")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(LeS1BleCmd.readFileEnd())
                    return
                }
                curSize += response.len
                fileContent = add(fileContent, response.content)

                val nowSize: Long = curSize.toLong()
                val size :Long= nowSize * 1000
                val poSize :Int= (size).div(this.fileSize).toInt()
                LepuBleLog.d(tag, "read file poSize：$poSize")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadingFileProgress).post(InterfaceEvent(model, poSize))

                if (curSize  < fileSize) {
                    sendCmd(LeS1BleCmd.readFileData(curSize)) // 每次读的偏移量，相对于文件总长度的
                } else {
                    sendCmd(LeS1BleCmd.readFileEnd())
                }
            }

            LeS1BleCmd.READ_FILE_END -> {
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                curSize = 0
                if (fileContent == null) fileContent = ByteArray(0)

                if (isCancelRF || isPausedRF){
                    LepuBleLog.d(tag, "已经取消/暂停下载 isCancelRF = $isCancelRF, isPausedRF = $isPausedRF" )
                    return
                }

                fileContent?.let {
                    if (it.isNotEmpty()) {
                        val data = LeS1BleResponse.BleFile(it)
                        LepuBleLog.d(tag, "read file finished：$data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadFileComplete).post(InterfaceEvent(model, data))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadFileError).post(InterfaceEvent(model, true))
                    }
                }
            }

            LeS1BleCmd.RESET -> {
                LepuBleLog.d(tag, "model:$model,RESET => success")
                if (response.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1Reset).post(InterfaceEvent(model, false))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1Reset).post(InterfaceEvent(model, true))
                }
            }

            LeS1BleCmd.FACTORY_RESET -> {
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")
                if (response.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ResetFactory).post(InterfaceEvent(model, false))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ResetFactory).post(InterfaceEvent(model, true))
                }
            }
            LeS1BleCmd.SET_TIME -> {
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")
                if (response.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1SetTime).post(InterfaceEvent(model, false))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1SetTime).post(InterfaceEvent(model, true))
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
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
            val len = toUInt(bytes.copyOfRange(i+5, i+7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = LeS1BleResponse.BleResponse(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
        sendCmd(LeS1BleCmd.getInfo())
    }

    override fun syncTime() {
        sendCmd(LeS1BleCmd.setTime())
    }


    override fun reset() {
        sendCmd(LeS1BleCmd.reset())
    }

    override fun factoryReset() {
        sendCmd(LeS1BleCmd.factoryReset())
    }

    override fun factoryResetAll() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
        sendCmd(LeS1BleCmd.getRtData())
    }
    
    /**
     * get file list
     */
    override fun getFileList() {
        sendCmd(LeS1BleCmd.getFileList())
    }


}