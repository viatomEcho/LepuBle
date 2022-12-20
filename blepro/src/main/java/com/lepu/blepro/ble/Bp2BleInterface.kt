package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.Bp2BleCmd
import com.lepu.blepro.ble.cmd.Bp2BleCmd.*
import com.lepu.blepro.ble.cmd.Bp2BleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.CrcUtil.calCRC8
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * bp2心电血压计，bp2a血压计，bp2t理疗血压计：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取实时心电、血压
 * 4.获取/配置参数
 * 5.获取/设置理疗参数
 * 6.获取/设置设备状态
 * 7.获取文件列表
 * 8.下载文件内容
 * 9.恢复出厂设置
 * 心电采样率：实时250HZ，存储125HZ
 * 血压采样率：实时50HZ，存储50HZ
 * 心电增益：n * 0.003098-----322.7888960619755倍
 */
class Bp2BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Bp2BleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (!isManagerInitialized()) {
            LepuBleLog.e(tag, "manager is not initialized")
            manager = Bp2BleManager(context)
        }
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

    var fileSize: Int = 0
    var fileName: String = ""
    var curSize: Int = 0
    var fileContent : ByteArray? = null

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
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
            if (i+8+len > bytes.size) {
                return bytes.copyOfRange(i, bytes.size)
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == calCRC8(temp)) {
                val bleResponse = Bp2BleResponse.BleResponse(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)


                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }


    private fun onResponseReceived(bleResponse: Bp2BleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived : " + bleResponse.cmd)

        when (bleResponse.cmd) {
            GET_INFO -> {
                LepuBleLog.d(tag, "model:$model,CMD_INFO => success")
                if (bleResponse.content.size < 38) {
                    LepuBleLog.e(tag, "response.size:${bleResponse.content.size} error")
                    return
                }
                val info = if (device.name == null) {
                    Bp2DeviceInfo(bleResponse.content, "")
                } else {
                    Bp2DeviceInfo(bleResponse.content, device.name)
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info).post(InterfaceEvent(model, info))
            }
            SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,CMD_SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime).post(InterfaceEvent(model, true))
            }
            GET_FILE_LIST -> {

                LepuBleLog.d(tag, "model:$model,CMD_FILE_LIST => success")
                //发送实时state : byte
                if (bleResponse.content.isNotEmpty()) {
                    val list = if (device.name == null) {
                        KtBleFileList(bleResponse.content, "")
                    } else {
                        KtBleFileList(bleResponse.content, device.name)
                    }
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList).post(InterfaceEvent(model, list))
                }
            }

            FILE_READ_START -> {
                LepuBleLog.d(tag, "model:$model,CMD_FILE_READ_START => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(fileReadEnd())
                    LepuBleLog.d(tag, "FILE_READ_START isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                //检查返回是否异常
                if (bleResponse.type != 0x01.toByte()){

                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, CMD_FILE_READ_START => error")

                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }

                fileContent = null
                fileSize = toUInt(bleResponse.content.copyOfRange(0, 4))
                LepuBleLog.d(tag, "download file $fileName CMD_FILE_READ_START fileSize == $fileSize")
                if (fileSize == 0) {
                    sendCmd(fileReadEnd())
                } else {
                    sendCmd(fileReadPkg(0))
                }
            }

            FILE_READ_PKG -> {
                LepuBleLog.d(tag, "model:$model,CMD_FILE_READ_PKG => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(fileReadEnd())
                    LepuBleLog.d(tag, "FILE_READ_PKG isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }


                curSize += bleResponse.len
                val part = Bp2FilePart(fileName, fileSize, curSize)
                fileContent = add(fileContent, bleResponse.content)
                LepuBleLog.d(tag, "download file $fileName CMD_FILE_READ_PKG curSize == $curSize | fileSize == $fileSize")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress).post(InterfaceEvent(model, part))
                if (curSize < fileSize) {
                    sendCmd(fileReadPkg(curSize))
                } else {
                    sendCmd(fileReadEnd())
                }
            }

            FILE_READ_END -> {
                LepuBleLog.d(tag, "model:$model,CMD_FILE_READ_END => success")

                curSize = 0
                if (fileContent == null) fileContent = ByteArray(0)

                if (isCancelRF || isPausedRF){
                    LepuBleLog.d(tag, "已经取消/暂停下载 isCancelRF = $isCancelRF, isPausedRF = $isPausedRF" )
                    return
                }

                fileContent?.let {
                    if (it.isNotEmpty()) {
                        val file = if (device.name == null) {
                            Bp2BleFile(fileName, it, "")
                        } else {
                            Bp2BleFile(fileName, it, device.name)
                        }
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete).post(InterfaceEvent(model, file))
                    }
                }


            }

            //实时波形
            RT_DATA -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_RT_DATA => success")

                if (bleResponse.content.size > 31) {
                    val rtData = Bp2BleRtData(bleResponse.content)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData).post(InterfaceEvent(model, rtData))
                } else {
                    Log.d(tag, "bytes.content.size < 31")
                }
            }
            //实时状态
            RT_STATE -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_RT_STATE => success")
                if (bleResponse.content.size < 7) {
                    LepuBleLog.e(tag, "response.size:${bleResponse.content.size} error")
                    return
                }
                val rtState = Bp2BleRtState(bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2State).post(InterfaceEvent(model, rtState))

            }

            SET_CONFIG -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_SET_SWITCHER_STATE => success")

                //心跳音开关
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfigResult).post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfigResult).post(InterfaceEvent(model, 1))
                }
            }
            GET_CONFIG -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_CONFIG => success")

                //获取返回的开关状态
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfigError).post(InterfaceEvent(model, true))
                } else {
                    if (bleResponse.content.size > 24) {
                        val data = Bp2Config(bleResponse.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfigResult).post(InterfaceEvent(model, data))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfigError).post(InterfaceEvent(model, true))
                    }
                }
            }
            RESET -> {
                LepuBleLog.d(tag, "model:$model,CMD_RESET => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Reset).post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Reset).post(InterfaceEvent(model, 1))
                }
            }

            FACTORY_RESET -> {
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset).post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset).post(InterfaceEvent(model, 1))
                }
            }

            FACTORY_RESET_ALL -> {
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET_ALL => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryResetAll).post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryResetAll).post(InterfaceEvent(model, 1))
                }
            }

            SWITCH_STATE -> {
                LepuBleLog.d(tag, "model:$model,SWITCH_STATE => success")
                //切换状态
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SwitchState).post(InterfaceEvent(model, false))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SwitchState).post(InterfaceEvent(model, true))
                }
            }

            GET_PHY_STATE -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_GET_PHY_STATE => success")
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError).post(InterfaceEvent(model, false))
                } else {
                    if (bleResponse.content.size < 4) {
                        LepuBleLog.e(tag, "response.size:${bleResponse.content.size} error")
                        return
                    }
                    val data = Bp2BlePhyState(bleResponse.content)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyState).post(InterfaceEvent(model, data))
                }
            }

            SET_PHY_STATE -> {
                LepuBleLog.d(tag, "model:$model,CMD_BP2_SET_PHY_STATE => success")
                if (bleResponse.content.size < 4) {
                    LepuBleLog.e(tag, "response.size:${bleResponse.content.size} error")
                    return
                }
                val data = Bp2BlePhyState(bleResponse.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetPhyState).post(InterfaceEvent(model, data))
            }


        }

    }

    override fun getInfo() {
        LepuBleLog.d(tag, "getInfo...")
        sendCmd(Bp2BleCmd.getInfo())
    }

    override fun syncTime() {
        LepuBleLog.d(tag, "syncTime...")
        sendCmd(setTime())
    }

    //实时波形命令
    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
        sendCmd(Bp2BleCmd.getRtData())
    }

    //实时状态命令
    fun getRtState() {
        LepuBleLog.d(tag, "getRtState...")
        sendCmd(Bp2BleCmd.getRtState())
    }

    fun getPhyState() {
        LepuBleLog.d(tag, "getPhyState...")
        sendCmd(Bp2BleCmd.getPhyState())
    }
    fun setPhyState(state: Bp2BlePhyState) {
        LepuBleLog.d(tag, "setPhyState...")
        sendCmd(setPhyState(state.getDataBytes()))
    }

    fun setConfig(config: Bp2Config) {
        sendCmd(Bp2BleCmd.setConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setConfig...config:$config")
    }

    fun setConfig(switch: Boolean, volume: Int){
        sendCmd(Bp2BleCmd.setConfig(switch, volume))
        LepuBleLog.d(tag, "setConfig...switch:$switch, volume:$volume")
    }
     fun getConfig(){
         sendCmd(Bp2BleCmd.getConfig())
         LepuBleLog.d(tag, "getConfig...")
    }

    override fun getFileList() {
        sendCmd(Bp2BleCmd.getFileList())
        LepuBleLog.d(tag, "getFileList...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(getFileStart(fileName.toByteArray(), 0))
        LepuBleLog.d(tag, "dealReadFile...userId:$userId, fileName:$fileName")
    }
    override fun reset() {
        sendCmd(Bp2BleCmd.reset())
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryReset() {
        sendCmd(Bp2BleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun factoryResetAll() {
        sendCmd(Bp2BleCmd.factoryResetAll())
        LepuBleLog.d(tag, "factoryResetAll...")
    }

    /**
     * 0：进入血压测量
     * 1：进入心电测量
     * 2：进入历史回顾
     * 3：进入开机预备状态
     * 4：关机
     * 5：进入理疗模式
     */
    fun switchState(state: Int){
        sendCmd(Bp2BleCmd.switchState(state))
        LepuBleLog.e("switchState===$state")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

}