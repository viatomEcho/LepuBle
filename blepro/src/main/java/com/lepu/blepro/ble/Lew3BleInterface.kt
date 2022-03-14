package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import java.util.*
import kotlin.experimental.inv

/**
 * author: chenyongfeng
 * created on: 2022/1/11 16:24
 * description:
 */
class Lew3BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Lew3BleInterface"
    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Lew3BleManager(context)
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
    var fileListName: String? = null
    var curFileName: String? = null
    private var userId: String? = null
    var curFile: Lew3BleResponse.EcgFile? = null

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
        sendCmd(Lew3BleCmd.readFileStart(fileName.toByteArray(), 0))
    }

    @OptIn(ExperimentalUnsignedTypes::class)
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
                val respPkg = Lew3BleResponse.BleResponse(temp)
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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Lew3BleResponse.BleResponse) {
        LiveEventBus.get<String>(EventMsgConst.Cmd.EventCmdResponseContent).post(bytesToHex(response.bytes))
        when(response.cmd) {
            Lew3BleCmd.GET_INFO -> {
                if (response.len == 0) return
                val info = LepuDevice(response.content)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3Info).post(InterfaceEvent(model, info))

                if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }
            }

            Lew3BleCmd.SET_TIME -> {

                LepuBleLog.d(tag, "model:$model,SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3SetTime).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }

            Lew3BleCmd.BOUND_DEVICE -> {
                if (response.len == 0) return
                LepuBleLog.d(tag, "model:$model,BOUND_DEVICE => success")
                if (response.content[0].toInt() == 0) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3BoundDevice).post(
                        InterfaceEvent(
                            model,
                            true
                        )
                    )
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3BoundDevice).post(
                        InterfaceEvent(
                            model,
                            false
                        )
                    )
                }
            }

            Lew3BleCmd.GET_CONFIG -> {
                if (response.len == 0) return
                LepuBleLog.d(tag, "model:$model,GET_CONFIG => success")
                val config = Lew3Config(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3GetConfig).post(
                    InterfaceEvent(
                        model,
                        config
                    )
                )
            }

            Lew3BleCmd.SET_SERVER -> {

                LepuBleLog.d(tag, "model:$model,SET_SERVER => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3SetServer).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }

            Lew3BleCmd.SYSTEM_SETTINGS -> {
                LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SETTINGS => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3SystemSettings).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }

            Lew3BleCmd.RESET -> {

                LepuBleLog.d(tag, "model:$model,RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3Reset).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Lew3BleCmd.FACTORY_RESET -> {

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3FactoryReset).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Lew3BleCmd.FACTORY_RESET_ALL -> {

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3FactoryResetAll).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }

            Lew3BleCmd.RT_DATA -> {
                if (response.len == 0) return
                val rtData = Lew3BleResponse.RtData(response.content)

                LepuBleLog.d(tag, "model:$model,RT_DATA => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3RtData).post(
                    InterfaceEvent(
                        model,
                        rtData
                    )
                )
            }

            Lew3BleCmd.GET_FILE_LIST -> {
                if (response.len == 0) return
                fileListName = trimStr(com.lepu.blepro.utils.toString(response.content.copyOfRange(1, response.content.size)))
                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success")
                curFileName = fileListName
                LepuBleLog.d(tag, "model:$model, curFileName == $curFileName")
                LepuBleLog.d(tag, "model:$model, size == " + response.content[0].toInt())
                sendCmd(Lew3BleCmd.readFileStart(curFileName?.toByteArray(), 0))
            }

            Lew3BleCmd.READ_FILE_START -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => success")
                if (response.pkgType == 0x01.toByte()) {
                    curFile =  curFileName?.let {
                        Lew3BleResponse.EcgFile(model, it, toUInt(response.content))
                    }
                    sendCmd(Lew3BleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(tag, "read file failed：${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3ReadFileError).post(
                        InterfaceEvent(
                            model,
                            true
                        )
                    )
                }

            }
            Lew3BleCmd.READ_FILE_DATA -> {
                curFile?.apply {

                    LepuBleLog.d(tag, "READ_FILE_DATA: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(Lew3BleCmd.readFileEnd())
                        return
                    }
                    // 心电文件数据不知为何有空数据的文件
                    if (response.len == 0) {
                        sendCmd(Lew3BleCmd.readFileEnd())
                        return
                    }

                    this.addContent(response.content)
                    LepuBleLog.d(tag, "read file：${this.fileName}   => ${this.index + offset} / ${this.fileSize}")
                    LepuBleLog.d(tag, "read file：${((this.index+ offset) * 1000).div(this.fileSize) }")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3ReadingFileProgress).post(InterfaceEvent(model, ((this.index+ offset) * 1000).div(this.fileSize) ))

                    if (this.index < this.fileSize) {
                        sendCmd(Lew3BleCmd.readFileData(this.index))
                    } else {
                        sendCmd(Lew3BleCmd.readFileEnd())
                    }
                }
            }
            Lew3BleCmd.READ_FILE_END -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_END => success")
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                if (curFileName?.equals(fileListName) == true) {
                    curFileName = null// 一定要放在发通知之前
                    curFile?.let {
                        if (it.index < it.fileSize ) {
                            if ((isCancelRF || isPausedRF) ) return
                        } else {
                            val list =
                                Lew3BleResponse.FileList(it.content)

                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3FileList).post(
                                InterfaceEvent(
                                    model,
                                    list
                                )
                            )
                        }
                    }?: LepuBleLog.d(tag, "READ_FILE_END EventLew3FileList  model:$model,  curFile error!!")
                } else {
                    curFileName = null// 一定要放在发通知之前
                    curFile?.let {
                        if (it.index < it.fileSize ){
                            if ((isCancelRF || isPausedRF) ) return
                        }else {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3ReadFileComplete)
                                .post(InterfaceEvent(model, it))
                        }
                    }?: LepuBleLog.d(tag, "READ_FILE_END EventLew3ReadFileComplete  model:$model,  curFile error!!")
                }
                curFile = null
            }

            Lew3BleCmd.GET_BATTERY -> {
                if (response.len == 0) return
                LepuBleLog.d(tag, "model:$model,GET_BATTERY => success")
                val data = KtBleBattery(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew3.EventLew3BatteryInfo)
                    .post(InterfaceEvent(model, data))
            }
        }
    }

    fun systemSettings(on: Boolean) {
        sendCmd(Lew3BleCmd.systemSettings(on))
    }
    fun setServer(server: Lew3Config) {
        sendCmd(Lew3BleCmd.setServer(server.getDataBytes()))
    }
    fun getConfig() {
        sendCmd(Lew3BleCmd.getConfig())
    }

    fun getBattery() {
        sendCmd(Lew3BleCmd.getBattery())
    }

    fun boundDevice() {
        sendCmd(Lew3BleCmd.boundDevice())
    }

    override fun getInfo() {
        sendCmd(Lew3BleCmd.getDeviceInfo())
    }

    override fun syncTime() {
        sendCmd(Lew3BleCmd.setTime())
    }

    override fun getRtData() {
        sendCmd(Lew3BleCmd.getRtData())
    }

    override fun getFileList() {
        offset = 0
        isCancelRF = false
        isPausedRF = false
        sendCmd(Lew3BleCmd.listFiles())
    }
    fun deleteFile(fileName: String) {
        sendCmd(Lew3BleCmd.deleteFile(fileName.toByteArray()))
    }

    override fun factoryReset() {
        sendCmd(Lew3BleCmd.factoryReset())
    }

    override fun reset() {
        sendCmd(Lew3BleCmd.reset())
    }

    override fun factoryResetAll() {
        sendCmd(Lew3BleCmd.factoryResetAll())
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        readFile(userId, fileName)
    }



}