package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import java.util.*
import kotlin.experimental.inv

/**
 * author: wujuan
 * created on: 2021/2/26 13:57
 * description:
 */
class LeW3BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LeW3BleInterface"
    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Er1BleManager(context)
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
    private var userId: String? = null
    var curFile: LeW3File? = null

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
        sendCmd(LeW3BleCmd.readFileStart(fileName.toByteArray(), 0))
    }

    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val respPkg = Er2BleResponse(temp)
                onResponseReceived(respPkg)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(
                    i + 8 + len,
                    bytes.size
                )

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }


    private fun onResponseReceived(respPkg: Er2BleResponse) {
        when(respPkg.cmd) {
            LeW3BleCmd.CMD_RETRIEVE_DEVICE_INFO -> {
                if (respPkg.data==null) return
                val info = LepuDevice(respPkg.data)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3Info).post(InterfaceEvent(model, info))

                if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }
            }
            LeW3BleCmd.CMD_SET_TIME -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3SetTime).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            LeW3BleCmd.CMD_BOUND_DEVICE -> {
                if (respPkg.data==null) return
                LepuBleLog.d(tag, "model:$model,CMD_BOUND_DEVICE => success")
                if (respPkg.data[0].toInt() == 0) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3BoundDevice).post(
                        InterfaceEvent(
                            model,
                            true
                        )
                    )
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3BoundDevice).post(
                        InterfaceEvent(
                            model,
                            false
                        )
                    )
                }
            }
            LeW3BleCmd.CMD_GET_CONFIG -> {
                if (respPkg.data==null) return
                LepuBleLog.d(tag, "model:$model,CMD_GET_CONFIG => success")
                val config = LeW3Config(respPkg.data)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3GetConfig).post(
                    InterfaceEvent(
                        model,
                        config
                    )
                )
            }
            LeW3BleCmd.CMD_SET_CONFIG -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_CONFIG => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3SetConfig).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            LeW3BleCmd.CMD_RESET -> {

                LepuBleLog.d(tag, "model:$model,CMD_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3Reset).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            LeW3BleCmd.CMD_FACTORY_RESET -> {

                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3FactoryReset).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            LeW3BleCmd.CMD_FACTORY_RESET_ALL -> {

                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET_ALL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3FactoryResetAll).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            LeW3BleCmd.CMD_GET_REAL_TIME_DATA -> {
                if (respPkg.data==null) return
                val rtData = LeW3RtData(respPkg.data)

                LepuBleLog.d(tag, "model:$model,CMD_GET_REAL_TIME_DATA => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3RtData).post(
                    InterfaceEvent(
                        model,
                        rtData
                    )
                )
            }
            LeW3BleCmd.CMD_LIST_FILE -> {
                LepuBleLog.d(tag, "model:$model,CMD_LIST_FILE => success, $respPkg")
                curFileName = com.lepu.blepro.utils.toString(respPkg.data.copyOfRange(1, respPkg.data.size))
                LepuBleLog.d(tag, "model:$model, curFileName == $curFileName")
                LepuBleLog.d(tag, "model:$model, fileName == " + respPkg.data[0].toInt())
                sendCmd(LeW3BleCmd.readFileStart(curFileName?.toByteArray(), 0))
            }
            LeW3BleCmd.CMD_START_READ_FILE -> {
                if (respPkg.data==null) return
                LepuBleLog.d(tag, "model:$model,CMD_START_READ_FILE => success, $respPkg")
                if (respPkg.pkgType == 0x01.toByte()) {
                    curFile =  curFileName?.let {
                        LeW3File(model, it, toUInt(respPkg.data))
                    }
                    sendCmd(LeW3BleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(tag, "read file failed：${respPkg.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3ReadFileError).post(
                        InterfaceEvent(
                            model,
                            true
                        )
                    )
                }

            }
            LeW3BleCmd.CMD_READ_FILE_CONTENT -> {
                if (respPkg.data==null) return
                curFile?.apply {

                    LepuBleLog.d(tag, "CMD_READ_FILE_CONTENT: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(Er1BleCmd.readFileEnd())
                        return
                    }

                    this.addContent(respPkg.data)
                    LepuBleLog.d(tag, "read file：${this.fileName}   => ${this.index + offset} / ${this.fileSize}")
                    LepuBleLog.d(tag, "read file：${((this.index+ offset) * 1000).div(this.fileSize) }")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3ReadingFileProgress).post(InterfaceEvent(model, ((this.index+ offset) * 1000).div(this.fileSize) ))

                    if (this.index < this.fileSize) {
                        sendCmd(LeW3BleCmd.readFileData(this.index))
                    } else {
                        sendCmd(LeW3BleCmd.readFileEnd())
                    }
                }
            }
            LeW3BleCmd.CMD_END_READ_FILE -> {
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                if (curFileName?.equals("ecgrecord.list") == true) {
                    curFileName = null// 一定要放在发通知之前
                    curFile?.let {
                        if (it.index < it.fileSize ){
                            if ((isCancelRF || isPausedRF) ) return
                        }else {
                            val fileArray = LeW3FileList(it.content)

                            LepuBleLog.d(tag, "model:$model,CMD_LIST_FILE => success")
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3FileList).post(
                                InterfaceEvent(
                                    model,
                                    fileArray
                                )
                            )
                        }
                    }?: LepuBleLog.d(tag, "READ_FILE_END  model:$model,  curFile error!!")
                } else {
                    curFileName = null// 一定要放在发通知之前
                    curFile?.let {
                        if (it.index < it.fileSize ){
                            if ((isCancelRF || isPausedRF) ) return
                        }else {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3ReadFileComplete)
                                .post(InterfaceEvent(model, it))
                        }
                    }?: LepuBleLog.d(tag, "READ_FILE_END  model:$model,  curFile error!!")
                }
                curFile = null
            }
        }
    }

    fun setConfig(addr: String, port: Int) {
        sendCmd(LeW3BleCmd.setConfig(addr.toByteArray(), port))
    }
    fun getConfig() {
        sendCmd(LeW3BleCmd.getConfig())
    }

    fun boundDevice() {
        sendCmd(LeW3BleCmd.boundDevice())
    }

    override fun getInfo() {
        sendCmd(LeW3BleCmd.getDeviceInfo())
    }

    override fun syncTime() {
        sendCmd(LeW3BleCmd.setTime())
    }

    override fun getRtData() {
        sendCmd(LeW3BleCmd.getRtData())
    }

    override fun getFileList() {
        offset = 0
        isCancelRF = false
        isPausedRF = false
        sendCmd(LeW3BleCmd.listFiles())
    }
    fun deleteFile(fileName: String) {
        sendCmd(LeW3BleCmd.deleteFile(fileName.toByteArray()))
    }

    override fun factoryReset() {
        sendCmd(LeW3BleCmd.factoryReset())
    }

    override fun reset() {
        sendCmd(LeW3BleCmd.reset())
    }

    override fun factoryResetAll() {
        sendCmd(LeW3BleCmd.factoryResetAll())
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        readFile(userId, fileName)
    }



}