package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.LeBp2wBleCmd
import com.lepu.blepro.ble.cmd.LeBp2wBleCmd.*
import com.lepu.blepro.ble.cmd.LepuBleResponse
import com.lepu.blepro.ble.cmd.LpBleCmd
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.CrcUtil.calCRC8
import kotlin.experimental.inv

/**
 * lpbp2w心电血压计：
 * send:
 * 1.同步时间(utc时间)
 * 2.获取设备信息
 * 3.获取实时心电、血压
 * 4.获取/配置参数
 * 5.获取/设置设备状态
 * 6.获取文件列表
 * 7.下载文件内容
 * 8.恢复出厂设置
 * 9.获取路由
 * 10.获取/配置WiFi
 * 11.写用户文件
 * 12.获取文件crc
 * 心电采样率：实时250HZ，存储125HZ
 * 血压采样率：实时50HZ，存储50HZ
 * 心电增益：n * 0.003098-----322.7888960619755倍
 */
class LeBp2wBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "LeBp2wBleInterface"

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
    var fileName:String = ""
    var fileType:Int = 0
    var curSize: Int = 0
    var fileContent : ByteArray? = null
    var userList: LeBp2wUserList? = null
    var chunkSize: Int = 200  // 每次写文件大小

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
                val bleResponse = LepuBleResponse.BleResponse(temp)
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i + 8 + len, bytes.size)


                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }


    private fun onResponseReceived(bleResponse: LepuBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${bleResponse.cmd}, bytes: ${bytesToHex(bleResponse.bytes)}")

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
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wEncrypt).post(InterfaceEvent(model, data))
            }
            LpBleCmd.GET_INFO -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 38) {
                        LepuBleLog.d(tag, "model:$model,GET_INFO => decrypt.size < 38, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,GET_INFO => success, decrypt: ${bytesToHex(decrypt)}")
                    LepuDevice(decrypt)
                } else {
                    if (bleResponse.len < 38) {
                        LepuBleLog.d(tag, "model:$model,GET_INFO => response.len < 38")
                        return
                    }
                    LepuDevice(bleResponse.content)
                }
                LepuBleLog.d(tag, "model:$model,GET_INFO => success, data: $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wInfo).post(InterfaceEvent(model, data))
            }

            LpBleCmd.SET_UTC_TIME -> {
                if (isEncryptMode) {
                    LepuBleLog.d(tag, "model:$model,SET_UTC_TIME => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey))}")
                }
                //同步时间
                LepuBleLog.d(tag, "model:$model,SET_UTC_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSyncUtcTime).post(InterfaceEvent(model, true))
            }

            RT_STATE -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 7) {
                        LepuBleLog.d(tag, "model:$model,RT_STATE => decrypt.size < 7, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,RT_STATE => success, decrypt: ${bytesToHex(decrypt)}")
                    Bp2BleRtState(decrypt)
                } else {
                    if (bleResponse.content.size < 7) {
                        LepuBleLog.d(tag, "model:$model,RT_STATE => response.content.size < 7")
                        return
                    }
                    Bp2BleRtState(bleResponse.content)
                }
                // 主机状态
                LepuBleLog.d(tag, "model:$model,RT_STATE => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wRtState).post(InterfaceEvent(model, data))
            }

            //----------------------------读文件--------------------------
            LpBleCmd.READ_FILE_START -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                //检查返回是否异常
                if (bleResponse.pkgType != 0x01) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError).post(InterfaceEvent(model, fileName))
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
                if (bleResponse.pkgType != 0x01) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_DATA => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError).post(InterfaceEvent(model, fileName))
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

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadingFileProgress).post(InterfaceEvent(model, part))
                if (curSize < fileSize) {
                    sendCmd(LpBleCmd.readFileData(curSize, aesEncryptKey))
                } else {
                    sendCmd(LpBleCmd.readFileEnd(aesEncryptKey))
                }
            }
            LpBleCmd.READ_FILE_END -> {
                if (isCancelRF || isPausedRF){
                    LepuBleLog.d(tag, "已经取消/暂停下载 isCancelRF = $isCancelRF, isPausedRF = $isPausedRF" )
                    return
                }
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,READ_FILE_END => success")

                fileContent?.let {
                    if (curSize != fileSize) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError).post(InterfaceEvent(model, fileName))
                    } else {
                        if (fileName.endsWith(".list")) {
                            val data = if (device.name == null) {
                                Bp2BleFile(fileName, it, "")
                            } else {
                                Bp2BleFile(fileName, it, device.name)
                            }
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFileList).post(InterfaceEvent(model, data))
                        } else {
                            val data = if (device.name == null) {
                                LeBp2wEcgFile(fileName, it, "")
                            } else {
                                LeBp2wEcgFile(fileName, it, device.name)
                            }
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileComplete).post(InterfaceEvent(model, data))
                        }
                    }
                } ?: kotlin.run {
                    if (fileName.endsWith(".list")) {
                        val data = if (device.name == null) {
                            Bp2BleFile(fileName, byteArrayOf(0, fileType.toByte(), 0, 0, 0, 0, 0, 0, 0, 0), "")
                        } else {
                            Bp2BleFile(fileName, byteArrayOf(0, fileType.toByte(), 0, 0, 0, 0, 0, 0, 0, 0), device.name)
                        }
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFileList).post(InterfaceEvent(model, data))
                    } else {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReadFileError).post(InterfaceEvent(model, fileName))
                    }
                }
            }

            //--------------------------写文件--------------------------
            LpBleCmd.WRITE_FILE_START -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, WRITE_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,WRITE_FILE_START => success")
                if (fileSize <= 0) {
                    sendCmd(LpBleCmd.writeFileEnd(aesEncryptKey))
                } else {
                    curSize = if (fileSize < chunkSize) {
                        sendCmd(LpBleCmd.writeFileData(userList?.getDataBytes()?.copyOfRange(0, fileSize), aesEncryptKey))
                        fileSize
                    } else {
                        sendCmd(LpBleCmd.writeFileData(userList?.getDataBytes()?.copyOfRange(0, chunkSize), aesEncryptKey))
                        chunkSize
                    }
                }
            }
            LpBleCmd.WRITE_FILE_DATA -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, WRITE_FILE_DATA => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,WRITE_FILE_DATA => success")
                if (curSize < fileSize) {

                    if (fileSize - curSize < chunkSize) {
                        sendCmd(LpBleCmd.writeFileData(userList?.getDataBytes()?.copyOfRange(curSize, fileSize), aesEncryptKey))
                        curSize = fileSize
                    } else {
                        sendCmd(LpBleCmd.writeFileData(userList?.getDataBytes()?.copyOfRange(curSize, curSize + chunkSize), aesEncryptKey))
                        curSize += chunkSize
                    }

                } else {
                    sendCmd(LpBleCmd.writeFileEnd(aesEncryptKey))
                }

                val part = Bp2FilePart(fileName, fileSize, curSize)
                LepuBleLog.d(tag, "write file $fileName WRITE_FILE_DATA curSize == $curSize | fileSize == $fileSize")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WritingFileProgress).post(InterfaceEvent(model, part))

            }
            LpBleCmd.WRITE_FILE_END -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, WRITE_FILE_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,WRITE_FILE_END => success")
                val crc = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    FileListCrc(FileType.USER_TYPE, decrypt)
                } else {
                    FileListCrc(FileType.USER_TYPE, bleResponse.content)
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WriteFileComplete).post(InterfaceEvent(model, crc))
                curSize = 0
            }

            //实时波形
            RT_DATA -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 32) {
                        LepuBleLog.d(tag, "model:$model,RT_DATA => decrypt.size < 32, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,RT_DATA => success, decrypt: ${bytesToHex(decrypt)}")
                    Bp2BleRtData(decrypt)
                } else {
                    if (bleResponse.content.size < 32) {
                        LepuBleLog.d(tag, "model:$model,RT_DATA => response.content.size < 32")
                        return
                    }
                    Bp2BleRtData(bleResponse.content)
                }
                LepuBleLog.d(tag, "model:$model,RT_DATA => success, data.rtWave : ${data.rtWave}, data.rtState : ${data.rtState}")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wRtData).post(InterfaceEvent(model, data))
            }

            SET_CONFIG -> {
                if (bleResponse.pkgType != 0x01) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetConfig).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SET_CONFIG => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,SET_CONFIG => success")
                //心跳音开关
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetConfig).post(InterfaceEvent(model, true))
            }

            GET_CONFIG -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 27) {
                        LepuBleLog.d(tag, "model:$model,GET_CONFIG => decrypt.size < 27, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,GET_CONFIG => success, decrypt: ${bytesToHex(decrypt)}")
                    Bp2Config(decrypt)
                } else {
                    if (bleResponse.content.size < 27) {
                        LepuBleLog.d(tag, "model:$model,GET_CONFIG => response.len < 27")
                        return
                    }
                    Bp2Config(bleResponse.content)
                }
                LepuBleLog.d(tag, "model:$model,GET_CONFIG => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetConfig).post(InterfaceEvent(model, data))
            }

            LpBleCmd.RESET -> {
                if (bleResponse.pkgType != 0x01) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReset).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,RESET => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wReset).post(InterfaceEvent(model, true))
            }

            LpBleCmd.FACTORY_RESET -> {
                if (bleResponse.pkgType != 0x01) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFactoryReset).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFactoryReset).post(InterfaceEvent(model, true))
            }

            LpBleCmd.FACTORY_RESET_ALL -> {
                if (bleResponse.pkgType != 0x01) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFactoryResetAll).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wFactoryResetAll).post(InterfaceEvent(model, true))
            }

            SWITCH_STATE ->{
                if (bleResponse.pkgType != 0x01) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSwitchState).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SWITCH_STATE => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,SWITCH_STATE => success")
                //切换状态
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSwitchState).post(InterfaceEvent(model, true))
            }

            GET_WIFI_ROUTE -> {
                LepuBleLog.d(tag, "model:$model,GET_WIFI_ROUTE => success")
                LepuBleLog.d(tag, "model:$model,bytesToHex == " + bytesToHex(bleResponse.content))
                if (bleResponse.pkgType == 0xFF) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiScanning).post(InterfaceEvent(model, true))
                } else {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                        if (decrypt.isEmpty()) {
                            LepuBleLog.d(tag, "model:$model,GET_WIFI_ROUTE => decrypt.isEmpty(), decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_ROUTE => success, decrypt: ${bytesToHex(decrypt)}")
                        Bp2WifiDevice(decrypt)
                    } else {
                        if (bleResponse.content.isEmpty()) {
                            LepuBleLog.d(tag, "model:$model,GET_WIFI_ROUTE => response.content.isEmpty()")
                            return
                        }
                        Bp2WifiDevice(bleResponse.content)
                    }
                    LepuBleLog.d(tag, "model:$model, GET_WIFI_ROUTE => success, data :  $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2WifiDevice).post(InterfaceEvent(model, data))
                }
            }
            // 国内bp2WiFi不适用
            LpBleCmd.GET_FILE_LIST -> {
                val data = LeBp2wBleList(bleResponse.content)
                LepuBleLog.d(tag, "model:$model, GET_FILE_LIST => success, data : $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wList).post(InterfaceEvent(model, data))
            }

            GET_WIFI_CONFIG -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.isEmpty()) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => decrypt.isEmpty(), decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success, decrypt: ${bytesToHex(decrypt)}")
                    Bp2WifiConfig(decrypt)
                } else {
                    if (bleResponse.content.isEmpty()) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => response.content.isEmpty()")
                        return
                    }
                    Bp2WifiConfig(bleResponse.content)
                }
                LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success, data : $data")
                LepuBleLog.d(tag, "model:$model,bytesToHex == " + bytesToHex(bleResponse.content))
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetWifiConfig).post(InterfaceEvent(model, data))
            }

            SET_WIFI_CONFIG -> {
                if (bleResponse.pkgType != 0x01) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetWifiConfig).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => error")
                    return
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSetWifiConfig).post(InterfaceEvent(model, true))
                LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success")
            }

            GET_ECG_LIST_CRC -> {
                val crc = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.isEmpty()) {
                        LepuBleLog.d(tag, "GET_ECG_LIST_CRC decrypt.isEmpty()")
                        return
                    }
                    FileListCrc(FileType.ECG_TYPE, decrypt)
                } else {
                    if (bleResponse.content.isEmpty()) {
                        LepuBleLog.d(tag, "GET_ECG_LIST_CRC bleResponse.content.isEmpty()")
                        return
                    }
                    FileListCrc(FileType.ECG_TYPE, bleResponse.content)
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc).post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_ECG_LIST_CRC => success")
            }
            GET_BP_LIST_CRC -> {
                val crc = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.isEmpty()) {
                        LepuBleLog.d(tag, "GET_BP_LIST_CRC decrypt.isEmpty()")
                        return
                    }
                    FileListCrc(FileType.BP_TYPE, decrypt)
                } else {
                    if (bleResponse.content.isEmpty()) {
                        LepuBleLog.d(tag, "GET_BP_LIST_CRC bleResponse.content.isEmpty()")
                        return
                    }
                    FileListCrc(FileType.BP_TYPE, bleResponse.content)
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc).post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_BP_LIST_CRC => success")
            }
            GET_USER_LIST_CRC -> {
                val crc = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.isEmpty()) {
                        LepuBleLog.d(tag, "GET_USER_LIST_CRC decrypt.isEmpty()")
                        return
                    }
                    FileListCrc(FileType.USER_TYPE, decrypt)
                } else {
                    if (bleResponse.content.isEmpty()) {
                        LepuBleLog.d(tag, "GET_USER_LIST_CRC bleResponse.content.isEmpty()")
                        return
                    }
                    FileListCrc(FileType.USER_TYPE, bleResponse.content)
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetFileListCrc).post(InterfaceEvent(model, crc))
                LepuBleLog.d(tag, "model:$model,GET_USER_LIST_CRC => success")
            }
            LpBleCmd.DELETE_FILE -> {
                if (bleResponse.pkgType != 0x01) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wDeleteFile).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,DELETE_FILE => error")
                    return
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wDeleteFile).post(InterfaceEvent(model, true))
                LepuBleLog.d(tag, "model:$model,DELETE_FILE => success")
            }
            GET_WIFI_VERSION -> {
                val data = if (isEncryptMode) {
                    val decrypt = EncryptUtil.AesDecrypt(bleResponse.content, aesEncryptKey)
                    if (decrypt.size < 3) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_VERSION => decrypt.size < 3, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,GET_WIFI_VERSION => success, decrypt: ${bytesToHex(decrypt)}")
                    "${byte2UInt(decrypt[3])}.${byte2UInt(decrypt[2])}.${byte2UInt(decrypt[1])}.${byte2UInt(decrypt[0])}"
                } else {
                    if (bleResponse.content.size < 3) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_VERSION => response.len < 3")
                        return
                    }
                    "${byte2UInt(bleResponse.content[3])}.${byte2UInt(bleResponse.content[2])}.${byte2UInt(bleResponse.content[1])}.${byte2UInt(bleResponse.content[0])}"
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wGetWifiVersion).post(InterfaceEvent(model, data))
                LepuBleLog.d(tag, "model:$model,GET_WIFI_VERSION => success, data : $data")
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
        sendCmd(LpBleCmd.getInfo(aesEncryptKey))
        sendCmd(getWifiVersion(aesEncryptKey))
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(LpBleCmd.setUtcTime(aesEncryptKey))
        LepuBleLog.d(tag, "syncTime...")
    }

    fun syncUtcTime() {
        sendCmd(LpBleCmd.setUtcTime(aesEncryptKey))
        LepuBleLog.d(tag, "syncUtcTime..." + bytesToHex(LpBleCmd.setUtcTime(aesEncryptKey)))
    }

    fun deleteFile() {
        sendCmd(LpBleCmd.deleteFile(aesEncryptKey))
        LepuBleLog.d(tag, "deleteFile...")
    }

    //实时波形命令
    override fun getRtData() {
        sendCmd(LeBp2wBleCmd.getRtData(aesEncryptKey))
        LepuBleLog.d(tag, "getRtData...")
    }

    fun setConfig(config: Bp2Config){
        sendCmd(setConfig(config.getDataBytes(), aesEncryptKey))
        LepuBleLog.d(tag, "setConfig...config:$config")
    }
     fun getConfig(){
         sendCmd(LeBp2wBleCmd.getConfig(aesEncryptKey))
         LepuBleLog.d(tag, "getConfig...")
    }

    override fun getFileList() {
        sendCmd(LpBleCmd.getFileList(aesEncryptKey))
        LepuBleLog.d(tag, "getFileList...")
    }

    fun getFileListCrc(fileType: Int) {
        sendCmd(LeBp2wBleCmd.getFileListCrc(fileType, aesEncryptKey))
        LepuBleLog.d(tag, "getFileListCrc...fileType:$fileType")
    }

    fun getFileList(fileType: Int) {
        this.fileType = fileType
        when (fileType) {
            FileType.ECG_TYPE -> {
                fileName = "bp2ecg.list"
            }
            FileType.BP_TYPE -> {
                fileName = "bp2nibp.list"
            }
            FileType.USER_TYPE -> {
                fileName = "user.list"
            }
        }
        sendCmd(LpBleCmd.readFileStart(fileName.toByteArray(), 0, aesEncryptKey))
        LepuBleLog.d(tag, "getFileList... fileName == $fileName, fileType:$fileType")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(LpBleCmd.readFileStart(fileName.toByteArray(), 0, aesEncryptKey))
        LepuBleLog.d(tag, "dealReadFile... fileName == $fileName, userId:$userId")
    }

    fun writeUserList(userList: LeBp2wUserList) {
        this.userList = userList
        fileSize = userList.getDataBytes().size
        fileName = "user.list"
        sendCmd(LpBleCmd.writeFileStart(fileName.toByteArray(), 0, fileSize, aesEncryptKey))
        LepuBleLog.d(tag, "writeUserList... fileName == $fileName, fileSize == $fileSize")
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
        sendCmd(LeBp2wBleCmd.switchState(state, aesEncryptKey))
        LepuBleLog.e("enter  switchState： SWITCH_STATE===$state")
    }

    /**
     * 获取wifi路由
     */
    fun getWifiDevice() {
        sendCmd(getWifiRoute(0, aesEncryptKey))
        LepuBleLog.d(tag, "getWifiDevice...")
    }

    /**
     * 配置要连接的wifi
     */
    fun setWifiConfig(config: Bp2WifiConfig) {
        sendCmd(setWifiConfig(config.getDataBytes(), aesEncryptKey))
        LepuBleLog.d(tag, "setWifiConfig...config:$config")
    }

    /**
     * 获取当前配置的wifi
     */
    fun getWifiConfig() {
        sendCmd(LeBp2wBleCmd.getWifiConfig(aesEncryptKey))
        LepuBleLog.d(tag, "getWifiConfig...")
    }

    fun getRtState() {
        sendCmd(LeBp2wBleCmd.getRtState(aesEncryptKey))
        LepuBleLog.d(tag, "getRtState...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

}