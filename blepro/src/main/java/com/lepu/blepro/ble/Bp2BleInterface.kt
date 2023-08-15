package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.Bp2BleCmd.*
import com.lepu.blepro.ble.cmd.Bp2BleResponse
import com.lepu.blepro.ble.cmd.LepuBleResponse
import com.lepu.blepro.ble.cmd.LpBleCmd
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.CrcUtil.calCRC8
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
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Bp2BleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Bp2BleManager(context)
            LepuBleLog.d(tag, "!isManagerInitialized, manager.create done")
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
            if (temp.size < 7) {
                continue@loop
            }
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
            LpBleCmd.ENCRYPT -> {
                val decrypt = EncryptUtil.LepuDecrypt(bleResponse.content, lepuEncryptKey)
                if (decrypt.size < 4) {
                    LepuBleLog.d(tag, "model:$model,ENCRYPT => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                    return
                }
                LepuBleLog.d(tag, "model:$model,ENCRYPT => success, decrypt: ${bytesToHex(decrypt)}")
                val data = LepuBleResponse.EncryptInfo(decrypt)
                aesEncryptKey = data.key
                isEncryptMode = true
                LepuBleLog.d(tag, "model:$model,ENCRYPT => success, data: $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Encrypt).post(InterfaceEvent(model, data))
            }
            LpBleCmd.GET_INFO -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 38) {
                        LepuBleLog.d(tag, "model:$model,GET_INFO => decrypt.size < 38, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,GET_INFO => success, decrypt: ${bytesToHex(decrypt)}")
                    if (device.name == null) {
                        Bp2DeviceInfo(decrypt, "")
                    } else {
                        Bp2DeviceInfo(decrypt, device.name)
                    }
                } else {
                    if (bleResponse.content.size < 38) {
                        LepuBleLog.d(tag, "model:$model,GET_INFO => response.content.size < 38")
                        return
                    }
                    if (device.name == null) {
                        Bp2DeviceInfo(bleResponse.content, "")
                    } else {
                        Bp2DeviceInfo(bleResponse.content, device.name)
                    }
                }
                LepuBleLog.d(tag, "model:$model,GET_INFO => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info).post(InterfaceEvent(model, data))
            }
            LpBleCmd.SET_TIME -> {
                //同步时间
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,SET_TIME => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime).post(InterfaceEvent(model, true))
            }
            LpBleCmd.GET_FILE_LIST -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.isEmpty()) {
                        LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => decrypt.isEmpty(), decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success, decrypt: ${bytesToHex(decrypt)}")
                    if (device.name == null) {
                        KtBleFileList(decrypt, "")
                    } else {
                        KtBleFileList(decrypt, device.name)
                    }
                } else {
                    if (bleResponse.content.isEmpty()) {
                        LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => response.content.isEmpty()")
                        return
                    }
                    if (device.name == null) {
                        KtBleFileList(bleResponse.content, "")
                    } else {
                        KtBleFileList(bleResponse.content, device.name)
                    }
                }
                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList).post(InterfaceEvent(model, data))
            }

            LpBleCmd.READ_FILE_START -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                //检查返回是否异常
                if (bleResponse.type != 0x01.toByte()){
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                curSize = 0
                fileContent = null
                fileSize = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    toUInt(decrypt)
                } else {
                    toUInt(bleResponse.content)
                }
                LepuBleLog.d(tag, "download file $fileName READ_FILE_START fileSize == $fileSize")
                if (fileSize <= 0) {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                } else {
                    sendCmd(LpBleCmd.readFileData(0, aesEncryptKey))
                }
            }

            LpBleCmd.READ_FILE_DATA -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_DATA => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                    LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }
                //检查返回是否异常
                if (bleResponse.type != 0x01.toByte()){
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_DATA => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                val part = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    curSize += decrypt.size
                    fileContent = add(fileContent, decrypt)
                    Bp2FilePart(fileName, fileSize, curSize)
                } else {
                    curSize += bleResponse.len
                    fileContent = add(fileContent, bleResponse.content)
                    Bp2FilePart(fileName, fileSize, curSize)
                }
                LepuBleLog.d(tag, "download file $fileName READ_FILE_DATA curSize == $curSize | fileSize == $fileSize")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress).post(InterfaceEvent(model, part))
                if (curSize < fileSize) {
                    sendCmd(LpBleCmd.readFileData(curSize, aesEncryptKey))
                } else {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                }
            }

            LpBleCmd.READ_FILE_END -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_END => success")

                if (isCancelRF || isPausedRF){
                    LepuBleLog.d(tag, "已经取消/暂停下载 isCancelRF = $isCancelRF, isPausedRF = $isPausedRF" )
                    return
                }
                //检查返回是否异常
                if (bleResponse.type != 0x01.toByte()){
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                fileContent?.let {
                    if (curSize != fileSize) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError).post(InterfaceEvent(model, true))
                    } else {
                        val file = if (device.name == null) {
                            Bp2BleFile(fileName, it, "")
                        } else {
                            Bp2BleFile(fileName, it, device.name)
                        }
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete).post(InterfaceEvent(model, file))
                    }
                } ?: kotlin.run {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError).post(InterfaceEvent(model, true))
                }
            }

            //实时波形
            RT_DATA -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 31) {
                        LepuBleLog.d(tag, "model:$model,RT_DATA => decrypt.size < 31, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,RT_DATA => success, decrypt: ${bytesToHex(decrypt)}")
                    Bp2BleRtData(decrypt)
                } else {
                    if (bleResponse.content.size < 31) {
                        LepuBleLog.d(tag, "model:$model,RT_DATA => response.len < 31")
                        return
                    }
                    Bp2BleRtData(bleResponse.content)
                }
                LepuBleLog.d(tag, "model:$model,RT_DATA => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData).post(InterfaceEvent(model, data))
            }
            //实时状态
            RT_STATE -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 7) {
                        LepuBleLog.d(tag, "model:$model,RT_STATE => decrypt.size < 7, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,RT_STATE => success, decrypt: ${bytesToHex(decrypt)}")
                    Bp2BleRtData(decrypt)
                } else {
                    if (bleResponse.content.size < 7) {
                        LepuBleLog.d(tag, "model:$model,RT_STATE => response.len < 7")
                        return
                    }
                    Bp2BleRtState(bleResponse.content)
                }
                LepuBleLog.d(tag, "model:$model,RT_STATE => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2State).post(InterfaceEvent(model, data))

            }

            SET_CONFIG -> {
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,SET_CONFIG => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                LepuBleLog.d(tag, "model:$model,CMD_BP2_SET_SWITCHER_STATE => success")

                //心跳音开关
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfigResult).post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfigResult).post(InterfaceEvent(model, 1))
                }
            }
            GET_CONFIG -> {
                //获取返回的开关状态
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfigError).post(InterfaceEvent(model, true))
                } else {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                        if (decrypt.size < 24) {
                            LepuBleLog.d(tag, "model:$model,GET_CONFIG => decrypt.size < 24, decrypt: ${bytesToHex(decrypt)}")
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfigError).post(InterfaceEvent(model, true))
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_CONFIG => success, decrypt: ${bytesToHex(decrypt)}")
                        Bp2Config(decrypt)
                    } else {
                        if (bleResponse.content.size < 24) {
                            LepuBleLog.d(tag, "model:$model,GET_CONFIG => response.len < 24")
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfigError).post(InterfaceEvent(model, true))
                            return
                        }
                        Bp2Config(bleResponse.content)
                    }
                    LepuBleLog.d(tag, "model:$model,CMD_BP2_CONFIG => success, data : $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfigResult).post(InterfaceEvent(model, data))
                }
            }
            LpBleCmd.RESET -> {
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,RESET => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                LepuBleLog.d(tag, "model:$model,CMD_RESET => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Reset).post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Reset).post(InterfaceEvent(model, 1))
                }
            }

            LpBleCmd.FACTORY_RESET -> {
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset).post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset).post(InterfaceEvent(model, 1))
                }
            }

            LpBleCmd.FACTORY_RESET_ALL -> {
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET_ALL => success")

                //重置
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryResetAll).post(InterfaceEvent(model, 0))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryResetAll).post(InterfaceEvent(model, 1))
                }
            }

            SWITCH_STATE -> {
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,SWITCH_STATE => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                LepuBleLog.d(tag, "model:$model,SWITCH_STATE => success")
                //切换状态
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SwitchState).post(InterfaceEvent(model, false))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SwitchState).post(InterfaceEvent(model, true))
                }
            }

            GET_PHY_STATE -> {
                if (bleResponse.type != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError).post(InterfaceEvent(model, false))
                } else {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                        if (decrypt.size < 4) {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError).post(InterfaceEvent(model, false))
                            LepuBleLog.d(tag, "model:$model,GET_PHY_STATE => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_PHY_STATE => success, decrypt: ${bytesToHex(decrypt)}")
                        Bp2BlePhyState(decrypt)
                    } else {
                        if (bleResponse.content.size < 4) {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError).post(InterfaceEvent(model, false))
                            LepuBleLog.d(tag, "model:$model,GET_PHY_STATE => response.len < 4")
                            return
                        }
                        Bp2BlePhyState(bleResponse.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_PHY_STATE => success, data : $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyState).post(InterfaceEvent(model, data))
                }
            }

            SET_PHY_STATE -> {
                LepuBleLog.d(tag, "model:$model,SET_PHY_STATE => success")
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 4) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError).post(InterfaceEvent(model, false))
                        LepuBleLog.d(tag, "model:$model,SET_PHY_STATE => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,SET_PHY_STATE => success, decrypt: ${bytesToHex(decrypt)}")
                    Bp2BlePhyState(decrypt)
                } else {
                    if (bleResponse.content.size < 4) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetPhyStateError).post(InterfaceEvent(model, false))
                        LepuBleLog.d(tag, "model:$model,SET_PHY_STATE => response.len < 4")
                        return
                    }
                    Bp2BlePhyState(bleResponse.content)
                }
                LepuBleLog.d(tag, "model:$model,SET_PHY_STATE => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetPhyState).post(InterfaceEvent(model, data))
            }
            LpBleCmd.BURN_FACTORY_INFO -> {
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                if (bleResponse.type != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2BurnFactoryInfo).post(InterfaceEvent(model, false))
                } else {
                    LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2BurnFactoryInfo).post(InterfaceEvent(model, true))
                }
            }
            CMD_0X40 -> {
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,CMD_0X40 => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                LepuBleLog.d(tag, "model:$model,CMD_0X40 => success")
                if (bleResponse.type != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model,CMD_0X40 => failed")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetCmd0x40).post(InterfaceEvent(model, false))
                } else {
                    LepuBleLog.d(tag, "model:$model,CMD_0X40 => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetCmd0x40).post(InterfaceEvent(model, true))
                }
            }
        }

    }
    // 密钥交换
    fun encrypt(id: String) {
        val encrypt = EncryptUtil.LepuEncrypt(EncryptUtil.getAccessToken(id), lepuEncryptKey)
        sendCmd(LpBleCmd.encrypt(encrypt, ByteArray(0)))
        LepuBleLog.d(tag, "encrypt...lepuEncryptKey: ${bytesToHex(lepuEncryptKey)}")
        LepuBleLog.d(tag, "encrypt...encrypt: ${bytesToHex(encrypt)}")
        val decrypt = EncryptUtil.LepuDecrypt(encrypt, lepuEncryptKey)
        LepuBleLog.d(tag, "encrypt...decrypt: ${bytesToHex(decrypt)}")
    }
    override fun getInfo() {
        LepuBleLog.d(tag, "getInfo...")
        sendCmd(LpBleCmd.getInfo(aesEncryptKey))
    }

    override fun syncTime() {
        LepuBleLog.d(tag, "syncTime...")
        sendCmd(LpBleCmd.setTime(aesEncryptKey))
    }

    //实时波形命令
    override fun getRtData() {
        LepuBleLog.d(tag, "getRtData...")
        sendCmd(getRtData(aesEncryptKey))
    }

    //实时状态命令
    fun getRtState() {
        LepuBleLog.d(tag, "getRtState...")
        sendCmd(getRtState(aesEncryptKey))
    }

    fun getPhyState() {
        LepuBleLog.d(tag, "getPhyState...")
        sendCmd(getPhyState(aesEncryptKey))
    }
    fun setPhyState(state: Bp2BlePhyState) {
        LepuBleLog.d(tag, "setPhyState...")
        sendCmd(setPhyState(state.getDataBytes(), aesEncryptKey))
    }

    fun setConfig(config: Bp2Config) {
        sendCmd(setConfig(config.getDataBytes(), aesEncryptKey))
        LepuBleLog.d(tag, "setConfig...config:$config")
    }

    fun setConfig(switch: Boolean, volume: Int){
        sendCmd(setConfig(switch, volume, aesEncryptKey))
        LepuBleLog.d(tag, "setConfig...switch:$switch, volume:$volume")
    }
     fun getConfig(){
         sendCmd(getConfig(aesEncryptKey))
         LepuBleLog.d(tag, "getConfig...")
    }

    override fun getFileList() {
        sendCmd(LpBleCmd.getFileList(aesEncryptKey))
        LepuBleLog.d(tag, "getFileList...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(LpBleCmd.readFileStart(fileName.toByteArray(), 0, aesEncryptKey))
        LepuBleLog.d(tag, "dealReadFile...userId:$userId, fileName:$fileName")
    }
    override fun reset() {
        sendCmd(LpBleCmd.reset(aesEncryptKey))
        LepuBleLog.d(tag, "reset...")
    }

    override fun factoryReset() {
        sendCmd(LpBleCmd.factoryReset(aesEncryptKey))
        LepuBleLog.d(tag, "factoryReset...")
    }

    override fun factoryResetAll() {
        sendCmd(LpBleCmd.factoryResetAll(aesEncryptKey))
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
        sendCmd(switchState(state, aesEncryptKey))
        LepuBleLog.e("switchState===$state")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(LpBleCmd.burnFactoryInfo(config.convert2Data(), aesEncryptKey))
        LepuBleLog.d(tag, "burnFactoryInfo...")
    }

    // 定制BP2A_Sibel
    fun cmd0x40(key: Boolean, measure: Boolean) {
        sendCmd(cmd0x40(key, measure, aesEncryptKey))
    }

}