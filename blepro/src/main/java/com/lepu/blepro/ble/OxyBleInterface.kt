package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.BleCRC
import com.lepu.blepro.ble.cmd.OxyBleCmd
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.FactoryConfig
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv


class OxyBleInterface(model: Int): BleInterface(model) {
    
    private val tag: String = "OxyBleInterface"

    lateinit var settingType: Array<String>

    var curFileName: String? = null
    var curFile: OxyBleResponse.OxyFile? = null

    private var userId: String? = null
    

    var isPpgRt: Boolean = false

    var isPiRt: Boolean = true


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
                val bleResponse = OxyBleResponse.OxyResponse(temp)
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
    private fun onResponseReceived(response: OxyBleResponse.OxyResponse) {
        LepuBleLog.d(tag, "Response: $curCmd, ${response.content.toHex()}")
        if (curCmd == -1) {
            return
        }

        when (curCmd) {
            OxyBleCmd.OXY_CMD_PARA_SYNC -> {
                clearTimeout()

                LepuBleLog.d(tag, "model:$model, OXY_CMD_PARA_SYNC => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo).post(InterfaceEvent(model, true))

            }

            OxyBleCmd.OXY_CMD_BOX_INFO -> {
                clearTimeout()
                val boxInfo = LepuDevice(response.content.copyOfRange(1, response.len))
                LepuBleLog.d(tag, "model:$model, OXY_CMD_BOX_INFO => success $boxInfo")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyBoxInfo).post(InterfaceEvent(model, boxInfo))
            }

            OxyBleCmd.OXY_CMD_INFO -> {

                clearTimeout()
                val info = OxyBleResponse.OxyInfo(response.content)

                if (runRtImmediately) {
                    runRtTask()
                    runRtImmediately = false
                }
                LepuBleLog.d(tag, "model:$model, OXY_CMD_INFO => success $info")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo).post(InterfaceEvent(model, info))

            }

            // 1.4.1固件版本之前没有PI 有波形
            OxyBleCmd.OXY_CMD_RT_WAVE -> {
                clearTimeout()

                if (response.content.size < 13) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtWaveRes)
                        .post(InterfaceEvent(model, true))
                    return
                }

                val rtWave = OxyBleResponse.RtWave(response.content)

                //发送实时数据
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtData).post(InterfaceEvent(model, rtWave))

            }

            // 有pi 没有波形
            OxyBleCmd.OXY_CMD_RT_PARAM -> {
                clearTimeout()
                if (response.len < 12) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamRes)
                        .post(InterfaceEvent(model, true))
                    return
                }
                val rtParam = OxyBleResponse.RtParam(response.content)
                //发送实时数据
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamData).post(InterfaceEvent(model, rtParam))

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
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileError).post(InterfaceEvent(model, true))
                    LepuBleLog.d(tag, "model:$model, 读文件失败：${response.content.toHex()}")
                }
            }
            OxyBleCmd.OXY_CMD_PPG_RT_DATA -> {
                //ppg
                clearTimeout()

                if (response.content.size > 10) {
                    val ppgData = OxyBleResponse.PPGData(response.content)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgData)
                        .post(InterfaceEvent(model, ppgData))
                }else{
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyPpgRes)
                        .post(InterfaceEvent(model, true))
                }
            }

            OxyBleCmd.OXY_CMD_READ_CONTENT -> {
                clearTimeout()

                curFile?.apply {

                    this.addContent(response.content)

                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadingFileProgress).post(InterfaceEvent(model, (curFile!!.index * 1000).div(curFile!!.fileSize)))
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
                curFileName = null // 一定要放在发通知之前

                curFile?.let {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileComplete).post(InterfaceEvent(model, it))
                } ?: LepuBleLog.d(tag, "model:$model,  curFile error!!")

                curFile = null

            }

            OxyBleCmd.OXY_CMD_FACTORY_RESET -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model,  OXY_CMD_FACTORY_RESET => success")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyFactoryReset).post(InterfaceEvent(model, true))
            }

            OxyBleCmd.OXY_CMD_BURN_FACTORY_INFO -> {
                clearTimeout()
                LepuBleLog.d(tag, "model:$model,  OXY_CMD_BURN_FACTORY_INFO => success")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyBurnFactoryInfo).post(InterfaceEvent(model, true))
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
        LepuBleLog.d(tag, "getRtData...isPpgRt = $isPpgRt")
        if (isPpgRt){
            getPpgRT()
            return
        }
        if (isPiRt){
            sendOxyCmd(OxyBleCmd.OXY_CMD_RT_PARAM, OxyBleCmd.getRtParam())
            return
        }

        sendOxyCmd(OxyBleCmd.OXY_CMD_RT_WAVE, OxyBleCmd.getRtWave())// 无法支持1.4.1之前获取pi
    }

    fun burnFactoryInfo(config: FactoryConfig) {
        sendOxyCmd(OxyBleCmd.OXY_CMD_BURN_FACTORY_INFO, OxyBleCmd.burnFactoryInfo(config.convert2Data()))
    }

    fun getRtParam() {
        sendOxyCmd(OxyBleCmd.OXY_CMD_RT_PARAM, OxyBleCmd.getRtParam())
    }

    fun getRtWave() {
        sendOxyCmd(OxyBleCmd.OXY_CMD_RT_WAVE, OxyBleCmd.getRtWave())
    }

    fun getPpgRT(){
        sendOxyCmd(OxyBleCmd.OXY_CMD_PPG_RT_DATA, OxyBleCmd.getPpgRt())
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.curFileName = fileName
        this.userId = userId
        LepuBleLog.d(tag, "$userId 将要读取文件 $curFileName")
//        20201210095928
//        AA03FC00000F003230323031323130303935393238004C
        sendOxyCmd(OxyBleCmd.OXY_CMD_READ_START, OxyBleCmd.readFileStart(fileName))
    }

    fun getBoxInfo() {
        sendOxyCmd(OxyBleCmd.OXY_CMD_BOX_INFO, OxyBleCmd.getBoxInfo())
    }

    override fun syncTime() {
        settingType = arrayOf(OxyBleCmd.SYNC_TYPE_TIME)
        sendOxyCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.syncTime())
    }

    fun updateSetting(type: String, value: Any) {
        settingType = arrayOf(type)
        val data = value as Int
        if (settingType[0] == OxyBleCmd.SYNC_TYPE_ALL_SW) {
            updateSetting(arrayOf(OxyBleCmd.SYNC_TYPE_OXI_SWITCH, OxyBleCmd.SYNC_TYPE_HR_SWITCH, OxyBleCmd.SYNC_TYPE_MT_SW, OxyBleCmd.SYNC_TYPE_IV_SW),
                intArrayOf(data, data, data, data))
        } else {
            sendOxyCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.updateSetting(type, data))
        }
    }
    fun updateSetting(type: Array<String>, value: IntArray) {
        settingType = type
        sendOxyCmd(OxyBleCmd.OXY_CMD_PARA_SYNC, OxyBleCmd.updateSetting(type, value))
    }

    override fun getFileList() {
        LepuBleLog.e(tag, "getFileList Not yet implemented")
    }

    override fun getInfo() {
        sendOxyCmd(OxyBleCmd.OXY_CMD_INFO, OxyBleCmd.getInfo())
    }

    override fun factoryReset() {
        sendOxyCmd(OxyBleCmd.OXY_CMD_FACTORY_RESET, OxyBleCmd.factoryReset())
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryReset Not yet implemented")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset Not yet implemented")

    }

    override fun dealContinueRF(userId: String, fileName: String) {
       LepuBleLog.e(tag, "o2 暂不支持断点下载")
    }



}