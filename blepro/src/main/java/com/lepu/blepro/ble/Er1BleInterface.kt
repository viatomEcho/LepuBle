package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.BleCRC
import com.lepu.blepro.ble.cmd.er1.Er1BleResponse
import com.lepu.blepro.ble.cmd.er1.Er1BleCmd
import com.lepu.blepro.ble.data.Er1DataController
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 *
 * 蓝牙操作
 */

class Er1BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Er1BleInterface"

    override fun initManager(context: Context, device: BluetoothDevice) {
        manager = Er1BleManager(context)
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



    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    var curFile: Er1BleResponse.Er1File? = null
    var fileList: Er1BleResponse.Er1FileList? = null
    private var userId: String? = null


    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
        sendCmd(Er1BleCmd.readFileStart(fileName.toByteArray(), this.offset))
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Er1BleResponse.Er1Response) {
//        LepuBleLog.d(TAG, "received: ${response.cmd}")
        when(response.cmd) {
            Er1BleCmd.GET_INFO -> {
                val info = LepuDevice(response.content)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get(InterfaceEvent.ER1.EventEr1Info).post(InterfaceEvent(model, info))

                if (runRtImmediately) {
                    runRtTask()
                    runRtImmediately = false
                }

            }

            Er1BleCmd.RT_DATA -> {
                val rtData = Er1BleResponse.RtData(response.content)

                Er1DataController.receive(rtData.wave.wFs)
                LepuBleLog.d(tag, "model:$model,RT_DATA => success")
                LiveEventBus.get(InterfaceEvent.ER1.EventEr1RtData).post(InterfaceEvent(model, rtData))
            }

            Er1BleCmd.READ_FILE_LIST -> {
                fileList = Er1BleResponse.Er1FileList(response.content)
                LepuBleLog.d(tag, "model:$model,READ_FILE_LIST => success, ${fileList.toString()}")
                fileList?.let {
                    LiveEventBus.get(InterfaceEvent.ER1.EventEr1FileList).post(InterfaceEvent(model,it.toString()))
                }

            }

            Er1BleCmd.READ_FILE_START -> {
                if (response.pkgType == 0x01.toByte()) {
                    curFile =  curFileName?.let {
                        Er1BleResponse.Er1File(model, it, toUInt(response.content), userId!!)
                    }
                    sendCmd(Er1BleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(tag, "read file failed：${response.pkgType}")
                    LiveEventBus.get(InterfaceEvent.ER1.EventEr1ReadFileError).post(InterfaceEvent(model, true))

                }
            }

            Er1BleCmd.READ_FILE_DATA -> {
                curFile?.apply {

                    LepuBleLog.d(tag, "READ_FILE_DATA: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(Er1BleCmd.readFileEnd())
                        return
                    }

                    this.addContent(response.content, offset)
                    LepuBleLog.d(tag, "read file：${this.fileName}   => ${this.index + offset} / ${this.fileSize}")
                    LepuBleLog.d(tag, "read file：${((this.index+ offset) * 1000).div(this.fileSize) }")
                    LiveEventBus.get(InterfaceEvent.ER1.EventEr1ReadingFileProgress).post(InterfaceEvent(model, ((this.index+ offset) * 1000).div(this.fileSize) ))

                    if (this.index + offset < this.fileSize) {
                        sendCmd(Er1BleCmd.readFileData(this.index))
                    } else {
                        sendCmd(Er1BleCmd.readFileEnd())
                    }
                }
            }

            Er1BleCmd.READ_FILE_END -> {
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                curFileName = null// 一定要放在发通知之前
                curFile?.let {
                    if (isCancelRF || isPausedRF) return
                     LiveEventBus.get(InterfaceEvent.ER1.EventEr1ReadFileComplete).post(InterfaceEvent(model, it))
                }?: LepuBleLog.d(tag, "READ_FILE_END  model:$model,  curFile error!!")
                curFile = null
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
                val bleResponse = Er1BleResponse.Er1Response(temp)
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
        sendCmd(Er1BleCmd.getInfo())
    }

    override fun syncTime() {
    }


    override fun resetDeviceInfo() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
        sendCmd(Er1BleCmd.getRtData())
    }


    /**
     * get file list
     */
    override fun getFileList() {
        sendCmd(Er1BleCmd.getFileList())
    }


}