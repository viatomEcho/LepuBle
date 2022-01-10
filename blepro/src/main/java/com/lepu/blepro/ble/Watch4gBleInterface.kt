package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.Er2DeviceInfo
import com.lepu.blepro.ble.data.Watch4gFile
import com.lepu.blepro.ble.data.Watch4gFileList
import com.lepu.blepro.ble.data.Watch4gRtData
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
class Watch4gBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Watch4gBleInterface"
    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Watch4gBleManager(context)
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
    var curFile: Er2File? = null
    var curFile4g: Watch4gFile? = null

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
        sendCmd(Watch4gBleCmd.readFileStart(fileName.toByteArray(), 0))
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
            Watch4gBleCmd.CMD_RETRIEVE_DEVICE_INFO -> {
                if (respPkg.data==null) return
                val info = Er2DeviceInfo(device.name, device.address, respPkg.data)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Info).post(InterfaceEvent(model, info))

                if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }
            }
            Watch4gBleCmd.CMD_SET_TIME -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetTime).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Watch4gBleCmd.CMD_BOUND_DEVICE -> {
                if (respPkg.data==null) return
                LepuBleLog.d(tag, "model:$model,CMD_BOUND_DEVICE => success")
                if (respPkg.data[0].toInt() == 0) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2BoundDevice).post(
                        InterfaceEvent(
                            model,
                            true
                        )
                    )
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2BoundDevice).post(
                        InterfaceEvent(
                            model,
                            false
                        )
                    )
                }
            }
            Watch4gBleCmd.CMD_SET_SWITCHER_STATE -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_SWITCHER_STATE => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetSwitcherState).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Watch4gBleCmd.CMD_GET_CONFIG -> {
                if (respPkg.data==null) return
                LepuBleLog.d(tag, "model:$model,CMD_GET_CONFIG => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2GetConfig).post(
                    InterfaceEvent(
                        model,
                        respPkg.data
                    )
                )
            }
            Watch4gBleCmd.CMD_SET_CONFIG -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_CONFIG => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetConfig).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Watch4gBleCmd.CMD_RESET -> {

                LepuBleLog.d(tag, "model:$model,CMD_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Reset).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Watch4gBleCmd.CMD_FACTORY_RESET -> {

                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryReset).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Watch4gBleCmd.CMD_FACTORY_RESET_ALL -> {

                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET_ALL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryResetAll).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Watch4gBleCmd.CMD_GET_REAL_TIME_DATA -> {
                if (respPkg.data==null) return
                val rtData = Watch4gRtData(respPkg.data)

                LepuBleLog.d(tag, "model:$model,CMD_GET_REAL_TIME_DATA => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2RtData).post(
                    InterfaceEvent(
                        model,
                        rtData
                    )
                )
            }
            Watch4gBleCmd.CMD_LIST_FILE -> {
//                val fileArray = Watch4gFileList(respPkg.data)
                /*val fileArray = Er2FileList(respPkg.data)

                LepuBleLog.d(tag, "model:$model,CMD_LIST_FILE => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList).post(
                    InterfaceEvent(
                        model,
                        fileArray
                    )
                )*/
            }
            Watch4gBleCmd.CMD_START_READ_FILE -> {
                if (respPkg.data==null) return
                LepuBleLog.d(tag, "model:$model,CMD_START_READ_FILE => success, $respPkg")
                if (respPkg.pkgType == 0x01.toByte()) {
                    if (curFileName?.equals("ecgrecord.list") == true) {
                        curFile4g =  curFileName?.let {
                            Watch4gFile(model, it, toUInt(respPkg.data))
                        }
                    } else {
                        curFile =  curFileName?.let {
                            Er2File(model, it, toUInt(respPkg.data), userId!!)
                        }
                    }
                    sendCmd(Watch4gBleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(tag, "read file failed：${respPkg.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileError).post(
                        InterfaceEvent(
                            model,
                            true
                        )
                    )
                }

            }
            Watch4gBleCmd.CMD_READ_FILE_CONTENT -> {
                if (respPkg.data==null) return
                if (curFileName?.equals("ecgrecord.list") == true) {
                    curFile4g?.apply {

                        LepuBleLog.d(tag, "CMD_READ_FILE_CONTENT: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                        //检查当前的下载状态
                        if (isCancelRF || isPausedRF) {
                            sendCmd(Er1BleCmd.readFileEnd())
                            return
                        }

                        this.addContent(respPkg.data)
//                        LepuBleLog.d(tag, "read file：${this.fileName}   => ${this.index + offset} / ${this.fileSize}")
//                        LepuBleLog.d(tag, "read file：${((this.index+ offset) * 1000).div(this.fileSize) }")

                        if (this.index < this.fileSize) {
                            sendCmd(Watch4gBleCmd.readFileData(this.index))
                        } else {
                            sendCmd(Watch4gBleCmd.readFileEnd())
                        }
                    }
                } else {
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
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadingFileProgress).post(InterfaceEvent(model, ((this.index+ offset) * 1000).div(this.fileSize) ))

                        if (this.index < this.fileSize) {
                            sendCmd(Watch4gBleCmd.readFileData(this.index))
                        } else {
                            sendCmd(Watch4gBleCmd.readFileEnd())
                        }
                    }
                }
            }
            Watch4gBleCmd.CMD_END_READ_FILE -> {
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                if (curFileName?.equals("ecgrecord.list") == true) {
                    curFileName = null// 一定要放在发通知之前
                    curFile4g?.let {
                        if (it.index < it.fileSize ){
                            if ((isCancelRF || isPausedRF) ) return
                        }else {
                            val fileArray =
                                Watch4gFileList(it.content)

                            LepuBleLog.d(tag, "model:$model,CMD_LIST_FILE => success")
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList).post(
                                InterfaceEvent(
                                    model,
                                    fileArray
                                )
                            )
                        }
                    }?: LepuBleLog.d(tag, "READ_FILE_END  model:$model,  curFile error!!")
                    curFile4g = null
                } else {
                    curFileName = null// 一定要放在发通知之前
                    curFile?.let {
                        if (it.index < it.fileSize ){
                            if ((isCancelRF || isPausedRF) ) return
                        }else {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileComplete)
                                .post(InterfaceEvent(model, it))
                        }
                    }?: LepuBleLog.d(tag, "READ_FILE_END  model:$model,  curFile error!!")
                    curFile = null
                }
            }

            else -> {

            }
        }
    }
    fun setSwitcherState(hrFlag: Boolean) {
        sendCmd(Watch4gBleCmd.setSwitcherState(hrFlag))
    }

    fun setConfig(addr: String, port: Int) {
        sendCmd(Watch4gBleCmd.setConfig(addr.toByteArray(), port))
    }
    fun getConfig() {
        sendCmd(Watch4gBleCmd.getConfig())
    }

    fun boundDevice() {
        sendCmd(Watch4gBleCmd.boundDevice())
    }

    override fun getInfo() {
        sendCmd(Watch4gBleCmd.getDeviceInfo())
    }

    override fun syncTime() {
        sendCmd(Watch4gBleCmd.setTime())
    }

    override fun getRtData() {
        sendCmd(Watch4gBleCmd.getRtData())
    }

    override fun getFileList() {
        offset = 0
        isCancelRF = false
        isPausedRF = false
        curFileName = "ecgrecord.list"
        sendCmd(Watch4gBleCmd.readFileStart(curFileName?.toByteArray(), 0))
//        sendCmd(Watch4gBleCmd.listFiles())
    }
    fun deleteFile(fileName: String) {
        sendCmd(Watch4gBleCmd.deleteFile(fileName.toByteArray()))
    }

    override fun factoryReset() {
        sendCmd(Watch4gBleCmd.factoryReset())
    }

    override fun reset() {
        sendCmd(Watch4gBleCmd.reset())
    }

    override fun factoryResetAll() {
        sendCmd(Watch4gBleCmd.factoryResetAll())
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        readFile(userId, fileName)
    }



}