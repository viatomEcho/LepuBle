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
 * er1心电贴：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取实时心电
 * 4.获取列表
 * 5.下载文件内容
 * 6.恢复出厂设置
 * 7.获取/配置参数
 * 8.烧录
 * 心电采样率：实时125HZ，存储125HZ
 * 心电增益：n * (1.0035 * 1800) / (4096 * 178.74) = n * 0.0024672217239426-----405.3142002989537倍
 */
class Er1BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Er1BleInterface"

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
                LepuBleLog.d(tag, "manager.connect done")
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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Er1BleResponse.Er1Response) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        when(response.cmd) {
            Er1BleCmd.GET_INFO -> {
                val info = LepuDevice(response.content)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Info).post(InterfaceEvent(model, info))
                // 本版本注释，测试通过后删除
                /*if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }*/

            }

            Er1BleCmd.RT_DATA -> {
                val rtData = Er1BleResponse.RtData(response.content)

                Er1DataController.receive(rtData.wave.wFs)
                LepuBleLog.d(tag, "model:$model,RT_DATA => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1RtData).post(InterfaceEvent(model, rtData))
            }

            Er1BleCmd.READ_FILE_LIST -> {
                fileList = Er1BleResponse.Er1FileList(response.content)
                LepuBleLog.d(tag, "model:$model,READ_FILE_LIST => success, ${fileList.toString()}")
                fileList?.let {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList).post(InterfaceEvent(model,it.toString()))
                }

            }

            Er1BleCmd.READ_FILE_START -> {
                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(Er1BleCmd.readFileEnd())
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                if (response.pkgType == 0x01.toByte()) {
                    curFile = curFileName?.let {
                        Er1BleResponse.Er1File(model, it, toUInt(response.content), userId!!, offset)
                    }
                    sendCmd(Er1BleCmd.readFileData(offset))
                } else {
                    LepuBleLog.d(tag, "read file failed：${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileError).post(InterfaceEvent(model, true))

                }
            }

            Er1BleCmd.READ_FILE_DATA -> {
                curFile?.apply {

                    LepuBleLog.d(tag, "READ_FILE_DATA: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(Er1BleCmd.readFileEnd())
                        LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                        return
                    }

                    this.addContent(response.content)
                    LepuBleLog.d(tag, "read file：${this.fileName}   => ${this.index } / ${this.fileSize}")
                    val nowSize: Long = (this.index).toLong()
                    val size :Long= nowSize * 1000
                    val poSize :Int= (size).div(this.fileSize).toInt()
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadingFileProgress).post(InterfaceEvent(model,poSize))

                    if (this.index < this.fileSize) {
                        sendCmd(Er1BleCmd.readFileData(this.index)) // 每次读的偏移量，相对于文件总长度的
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
                    if (it.index < it.fileSize){
                        if ((isCancelRF || isPausedRF)) {
                            LepuBleLog.d(tag, "READ_FILE_END isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                            return
                        }
                    }else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileComplete).post(InterfaceEvent(model, it))
                    }
                }?: LepuBleLog.d(tag, "READ_FILE_END  model:$model,  curFile error!!")
                curFile = null
            }
            Er1BleCmd.VIBRATE_CONFIG -> {
                if (response.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model,VIBRATE_CONFIG => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1GetConfigError).post(InterfaceEvent(model, true))
                    return
                }
                LepuBleLog.d(tag, "model:$model,VIBRATE_CONFIG => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1VibrateConfig).post(InterfaceEvent(model, response.content))

            }
            Er1BleCmd.SET_VIBRATE_STATE -> {
                LepuBleLog.d(tag, "model:$model,SET_SWITCHER_STATE => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetSwitcherState).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetSwitcherState).post(InterfaceEvent(model, false))
                }
            }

            Er1BleCmd.RESET -> {
                LepuBleLog.d(tag, "model:$model,RESET => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Reset).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Reset).post(InterfaceEvent(model, false))
                }
            }

            Er1BleCmd.FACTORY_RESET -> {
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactory).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactory).post(InterfaceEvent(model, false))
                }
            }

            Er1BleCmd.FACTORY_RESET_ALL -> {
                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactoryAll).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactoryAll).post(InterfaceEvent(model, false))
                }
            }
            Er1BleCmd.SET_TIME -> {
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetTime).post(InterfaceEvent(model, true))
            }
            Er1BleCmd.BURN_FACTORY_INFO -> {
                LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1BurnFactoryInfo).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1BurnFactoryInfo).post(InterfaceEvent(model, false))
                }
            }
            Er1BleCmd.BURN_LOCK_FLASH -> {
                LepuBleLog.d(tag, "model:$model,BURN_LOCK_FLASH => success")
                if (response.pkgType == 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1BurnLockFlash).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1BurnLockFlash).post(InterfaceEvent(model, false))
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
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(Er1BleCmd.setTime())
        LepuBleLog.d(tag, "syncTime...")
    }

    override fun reset() {
        sendCmd(Er1BleCmd.reset())
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryReset() {
        sendCmd(Er1BleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun factoryResetAll() {
        sendCmd(Er1BleCmd.factoryResetAll())
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
        sendCmd(Er1BleCmd.getRtData())
        LepuBleLog.d(tag, "getRtData...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        sendCmd(Er1BleCmd.readFileStart(fileName.toByteArray(), 0)) // 读开始永远是0
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
    }

    /**
     * get file list
     */
    override fun getFileList() {
        sendCmd(Er1BleCmd.getFileList())
        LepuBleLog.d(tag, "getFileList...")
    }

    fun getVibrateConfig(){
        sendCmd(Er1BleCmd.getVibrateConfig())
        LepuBleLog.d(tag, "getVibrateConfig...")
    }
    fun setVibrateConfig(switcher: Boolean, threshold1: Int, threshold2: Int){
        sendCmd(Er1BleCmd.setVibrate(switcher, threshold1, threshold2))
        LepuBleLog.d(tag, "setVibrateConfig...switcher:$switcher, threshold1:$threshold1, threshold2:$threshold2")
    }

    fun setVibrateConfig(switcher: Boolean, vector: Int, motionCount: Int, motionWindows: Int){
        sendCmd(Er1BleCmd.setSwitcher(switcher, vector,motionCount, motionWindows))
        LepuBleLog.d(tag, "setVibrateConfig...switcher:$switcher, vector:$vector, motionCount:$motionCount, motionWindows:$motionWindows")
    }

    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(Er1BleCmd.burnFactoryInfo(config.convert2Data()))
        LepuBleLog.d(tag, "burnFactoryInfo...config:$config")
    }
    fun burnLockFlash() {
        sendCmd(Er1BleCmd.burnLockFlash())
        LepuBleLog.d(tag, "burnLockFlash...")
    }

}