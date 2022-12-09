package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * lepod：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取实时心电
 * 4.获取列表
 * 5.下载文件内容
 * 6.恢复出厂设置
 * 7.获取/配置模式
 * 8.烧录
 * 心电采样率：实时250HZ，存储250HZ
 * 心电增益：n * 0.00244140625-----409.6倍
 */
class LepodBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LepodBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Er1BleManager(context)
        manager?.let {
            it.isUpdater = isUpdater
            it.setConnectionObserver(this)
            it.notifyListener = this
            it.connect(device)
                .useAutoConnect(false)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "manager.connect done")
                }
                .enqueue()
        } ?: kotlin.run {
            LepuBleLog.d(tag, "manager == null")
        }
    }

    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    var curFile: LepodBleResponse.BleFile? = null
    private var userId: String? = null

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: LepodBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        when(response.cmd) {
            LepodBleCmd.GET_INFO -> {
                val info = LepuDevice(response.content)
                LepuBleLog.d(tag, "model:$model,GET_INFO => success $info")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodInfo).post(InterfaceEvent(model, info))
            }

            LepodBleCmd.RT_PARAM -> {
                val rtParam = LepodBleResponse.RtParam(response.content)
                LepuBleLog.d(tag, "model:$model,RT_DATA => success ${rtParam}")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodRtData).post(InterfaceEvent(model, rtParam))
            }

            LepodBleCmd.RT_DATA -> {
                val rtData = LepodBleResponse.RtData(response.content)
                LepuBleLog.d(tag, "model:$model,RT_DATA => success ${rtData.wave}")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodRtData).post(InterfaceEvent(model, rtData))
            }

            LepodBleCmd.READ_FILE_LIST -> {
                val fileList = LepodBleResponse.FileList(response.content)
                LepuBleLog.d(tag, "model:$model,READ_FILE_LIST => success, $fileList")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodFileList).post(InterfaceEvent(model,fileList))
            }

            LepodBleCmd.READ_FILE_START -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(LepodBleCmd.readFileEnd())
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                if (response.pkgType == 0x01.toByte()) {
                    curFile = curFileName?.let {
                        LepodBleResponse.BleFile(model, it, toUInt(response.content), userId!!, offset)
                    }
                    sendCmd(LepodBleCmd.readFileData(offset))
                } else {
                    LepuBleLog.d(tag, "read file failed：${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodReadFileError).post(InterfaceEvent(model, true))
                }
            }

            LepodBleCmd.READ_FILE_DATA -> {
                curFile?.apply {

                    LepuBleLog.d(tag, "READ_FILE_DATA: paused = $isPausedRF, cancel = $isCancelRF, offset = ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(LepodBleCmd.readFileEnd())
                        LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                        return
                    }

                    this.addContent(response.content)
                    val nowSize: Long = (this.index).toLong()
                    val size :Long= nowSize * 100
                    val poSize :Int= (size).div(this.fileSize).toInt()
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodReadingFileProgress).post(InterfaceEvent(model,poSize))
                    LepuBleLog.d(tag, "read file：${this.fileName} => ${this.index } / ${this.fileSize} poSize : $poSize")

                    if (this.index < this.fileSize) {
                        sendCmd(LepodBleCmd.readFileData(this.index)) // 每次读的偏移量，相对于文件总长度的
                    } else {
                        sendCmd(LepodBleCmd.readFileEnd())
                    }
                }
            }

            LepodBleCmd.READ_FILE_END -> {
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                curFileName = null// 一定要放在发通知之前
                curFile?.let {
                    if (it.index < it.fileSize){
                        if ((isCancelRF || isPausedRF)) {
                            LepuBleLog.d(tag, "READ_FILE_END isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                            return
                        }
                    }else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodReadFileComplete).post(InterfaceEvent(model, it))
                    }
                }?: LepuBleLog.d(tag, "READ_FILE_END model:$model, curFile error!!")
                curFile = null
            }
            LepodBleCmd.GET_CONFIG -> {
                if (response.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model,GET_CONFIG => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodGetConfigError).post(InterfaceEvent(model, true))
                    return
                }
                LepuBleLog.d(tag, "model:$model,GET_CONFIG => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodGetConfig).post(InterfaceEvent(model, toUInt(response.content)))

            }
            LepodBleCmd.SET_CONFIG -> {
                LepuBleLog.d(tag, "model:$model,SET_CONFIG => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodSetConfig).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodSetConfig).post(InterfaceEvent(model, false))
                }
            }

            LepodBleCmd.RESET -> {
                LepuBleLog.d(tag, "model:$model,RESET => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodReset).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodReset).post(InterfaceEvent(model, false))
                }
            }

            LepodBleCmd.FACTORY_RESET -> {
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodFactoryReset).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodFactoryReset).post(InterfaceEvent(model, false))
                }
            }

            LepodBleCmd.FACTORY_RESET_ALL -> {
                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodFactoryResetAll).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodFactoryResetAll).post(InterfaceEvent(model, false))
                }
            }
            LepodBleCmd.SET_TIME -> {
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodSetTime).post(InterfaceEvent(model, true))
            }
            LepodBleCmd.BURN_FACTORY_INFO -> {
                LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodBurnFactoryInfo).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodBurnFactoryInfo).post(InterfaceEvent(model, false))
                }
            }
            LepodBleCmd.BURN_LOCK_FLASH -> {
                LepuBleLog.d(tag, "model:$model,BURN_LOCK_FLASH => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodBurnLockFlash).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodBurnLockFlash).post(InterfaceEvent(model, false))
                }
            }
            LepodBleCmd.START_ECG -> {
                LepuBleLog.d(tag, "model:$model,START_ECG => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodEcgStart).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodEcgStart).post(InterfaceEvent(model, false))
                }
            }
            LepodBleCmd.STOP_ECG -> {
                LepuBleLog.d(tag, "model:$model,STOP_ECG => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodEcgStop).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodEcgStop).post(InterfaceEvent(model, false))
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
                val bleResponse = LepodBleResponse.BleResponse(temp)
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
        sendCmd(LepodBleCmd.getInfo())
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(LepodBleCmd.setTime())
        LepuBleLog.d(tag, "syncTime...")
    }

    override fun reset() {
        sendCmd(LepodBleCmd.reset())
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryReset() {
        sendCmd(LepodBleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun factoryResetAll() {
        sendCmd(LepodBleCmd.factoryResetAll())
        LepuBleLog.d(tag, "factoryResetAll...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
        LepuBleLog.d(tag, "setVibrateConfig...userId:$userId, fileName:$fileName")
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
        sendCmd(LepodBleCmd.getRtData())
        LepuBleLog.d(tag, "getRtData...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        sendCmd(LepodBleCmd.readFileStart(fileName.toByteArray(), 0)) // 读开始永远是0
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
    }

    override fun getFileList() {
        sendCmd(LepodBleCmd.getFileList())
        LepuBleLog.d(tag, "getFileList...")
    }

    fun getRtParam() {
        sendCmd(LepodBleCmd.getRtParam())
        LepuBleLog.d(tag, "getRtParam...")
    }

    fun getConfig(){
        sendCmd(LepodBleCmd.getConfig())
        LepuBleLog.d(tag, "getConfig...")
    }
    fun setConfig(mode: Int){
        sendCmd(LepodBleCmd.setConfig(mode))
        LepuBleLog.d(tag, "setConfig...mode:$mode")
    }

    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(LepodBleCmd.burnFactoryInfo(config.convert2Data()))
        LepuBleLog.d(tag, "burnFactoryInfo...config:$config")
    }
    fun burnLockFlash() {
        sendCmd(LepodBleCmd.burnLockFlash())
        LepuBleLog.d(tag, "burnLockFlash...")
    }

    fun startEcg() {
        sendCmd(LepodBleCmd.startEcg())
        LepuBleLog.d(tag, "startEcg...")
    }
    fun stopEcg() {
        sendCmd(LepodBleCmd.stopEcg())
        LepuBleLog.d(tag, "stopEcg...")
    }

}