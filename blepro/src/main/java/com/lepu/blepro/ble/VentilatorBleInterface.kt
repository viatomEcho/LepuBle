package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.ble.data.ventilator.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.utils.EncryptUtil
import com.lepu.blepro.utils.EncryptUtil.LepuEncrypt
import com.lepu.blepro.utils.EncryptUtil.getAccessToken
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * 呼吸机：
 * send:
 * 1.同步时间/UTC时间
 * 2.获取设备信息
 * 3.获取实时数据
 * 7.复位
 * 8.恢复出厂设置
 * 9.恢复生产状态
 * 10.烧录信息
 */
class VentilatorBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "VentilatorBleInterface"

    private var userId = ""
    private var fileName = ""
    private var fileSize = 0
    private var fileContent = ByteArray(0)

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = Er1BleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = Er1BleManager(context)
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
            if (temp.size < 7) {
                continue@loop
            }
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = VentilatorBleResponse.BleResponse(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: VentilatorBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived len: ${response.len} cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
            val data = ResponseError()
            data.model = model
            data.cmd = response.cmd
            data.type = response.pkgType
            LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError).post(data)
            LepuBleLog.d(tag, "model:$model,ResponseError => $data")
            if (response.cmd == LpBleCmd.ECHO) {
                LiveEventBus.get<ByteArray>(EventMsgConst.Cmd.EventCmdResponseEchoData).post(response.bytes)
            }
        } else {
            when(response.cmd) {
                LpBleCmd.ECHO -> {
                    LepuBleLog.d(tag, "model:$model,ECHO => success ${bytesToHex(response.content)}")
                    LiveEventBus.get<ByteArray>(EventMsgConst.Cmd.EventCmdResponseEchoData).post(response.bytes)
                }
                LpBleCmd.SET_UTC_TIME -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_UTC_TIME => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_UTC_TIME => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetUtcTime).post(InterfaceEvent(model, true))
                }
                LpBleCmd.GET_INFO -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 38) {
                            LepuBleLog.d(tag, "model:$model,GET_INFO => decrypt.size < 38, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_INFO => success, decrypt: ${bytesToHex(decrypt)}")
                        LepuDevice(decrypt)
                    } else {
                        if (response.len < 38) {
                            LepuBleLog.d(tag, "model:$model,GET_INFO => response.len < 38")
                            return
                        }
                        LepuDevice(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_INFO => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfo).post(InterfaceEvent(model, data))
                }
                LpBleCmd.RESET -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,RESET => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReset).post(InterfaceEvent(model, true))
                }
                LpBleCmd.FACTORY_RESET -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorFactoryReset).post(InterfaceEvent(model, true))
                }
                LpBleCmd.GET_BATTERY -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 4) {
                            LepuBleLog.d(tag, "model:$model,GET_BATTERY => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_BATTERY => success, decrypt: ${bytesToHex(decrypt)}")
                        KtBleBattery(decrypt)
                    } else {
                        if (response.len < 4) {
                            LepuBleLog.d(tag, "model:$model,GET_BATTERY => response.len < 4")
                            return
                        }
                        KtBleBattery(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_BATTERY => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetBattery).post(InterfaceEvent(model, data))
                }
                LpBleCmd.BURN_FACTORY_INFO -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorBurnFactoryInfo).post(InterfaceEvent(model, true))
                }
                LpBleCmd.ENCRYPT -> {
                    val decrypt = EncryptUtil.LepuDecrypt(response.content, lepuEncryptKey)
                    if (decrypt.size < 4) {
                        LepuBleLog.d(tag, "model:$model,ENCRYPT => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,ENCRYPT => success, decrypt: ${bytesToHex(decrypt)}")
                    val data = VentilatorBleResponse.EncryptInfo(decrypt)
                    aesEncryptKey = data.key
                    isEncryptMode = true
                    LepuBleLog.d(tag, "model:$model,ENCRYPT => success, data: $data")
                    syncTime()
                }
                VentilatorBleCmd.DEVICE_BOUND -> {
                    // 0x00成功, 0x01失败, 0x02超时
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.isEmpty()) {
                            LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => decrypt.isEmpty()")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => success, decrypt: ${bytesToHex(decrypt)}")
                        toUInt(decrypt)
                    } else {
                        if (response.content.isEmpty()) {
                            LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => response.content.isEmpty()")
                            return
                        }
                        toUInt(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBound).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.DEVICE_UNBOUND -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,DEVICE_UNBOUND => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,DEVICE_UNBOUND => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceUnBound).post(InterfaceEvent(model, true))
                }
                VentilatorBleCmd.SET_USER_INFO -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_USER_INFO => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_USER_INFO => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetUserInfo).post(InterfaceEvent(model, true))
                }
                VentilatorBleCmd.GET_USER_INFO -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 81) {
                            LepuBleLog.d(tag, "model:$model,GET_USER_INFO => decrypt.size < 81, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_USER_INFO => success, decrypt: ${bytesToHex(decrypt)}")
                        UserInfo(decrypt)
                    } else {
                        UserInfo(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_USER_INFO => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetUserInfo).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.DOCTOR_MODE -> {
                    if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 2) {
                            LepuBleLog.d(tag, "model:$model,DOCTOR_MODE => decrypt.size < 2, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,DOCTOR_MODE => success, decrypt: ${bytesToHex(decrypt)}")
                        val data = VentilatorBleResponse.DoctorModeResult(decrypt)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorMode).post(InterfaceEvent(model, data))
                    }
                }
                VentilatorBleCmd.GET_WIFI_LIST -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.isEmpty()) {
                            LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => decrypt.isEmpty()")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => success, decrypt: ${bytesToHex(decrypt)}")
                        Bp2WifiDevice(decrypt)
                    } else {
                        if (response.content.isEmpty()) {
                            LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => response.content.isEmpty()")
                            return
                        }
                        Bp2WifiDevice(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiList).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.SET_WIFI_CONFIG -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWifiConfig).post(InterfaceEvent(model, true))
                }
                VentilatorBleCmd.GET_WIFI_CONFIG -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.isEmpty()) {
                            LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => decrypt.isEmpty()")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success, decrypt: ${bytesToHex(decrypt)}")
                        Bp2WifiConfig(decrypt)
                    } else {
                        if (response.content.isEmpty()) {
                            LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => response.content.isEmpty()")
                            return
                        }
                        Bp2WifiConfig(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfig).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.GET_VERSION_INFO -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 17) {
                            LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => decrypt.size < 17, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => success, decrypt: ${bytesToHex(decrypt)}")
                        VentilatorBleResponse.VersionInfo(decrypt)
                    } else {
                        if (response.content.size < 17) {
                            LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => response.content.size < 17")
                            return
                        }
                        VentilatorBleResponse.VersionInfo(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfo).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.GET_SYSTEM_SETTING -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 9) {
                            LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => decrypt.size < 9, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => success, decrypt: ${bytesToHex(decrypt)}")
                        SystemSetting(decrypt)
                    } else {
                        if (response.len < 9) {
                            LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => response.len < 9")
                            return
                        }
                        SystemSetting(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSetting).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.SET_SYSTEM_SETTING -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SETTING => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SETTING => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetSystemSetting).post(InterfaceEvent(model, true))
                }
                VentilatorBleCmd.GET_MEASURE_SETTING -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 10) {
                            LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => decrypt.size < 10, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => success, decrypt: ${bytesToHex(decrypt)}")
                        MeasureSetting(decrypt)
                    } else {
                        if (response.len < 10) {
                            LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => response.len < 10")
                            return
                        }
                        MeasureSetting(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSetting).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.SET_MEASURE_SETTING -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_MEASURE_SETTING => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_MEASURE_SETTING => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetMeasureSetting).post(InterfaceEvent(model, true))
                }
                VentilatorBleCmd.MASK_TEST -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 3) {
                            LepuBleLog.d(tag, "model:$model,MASK_TEST => decrypt.size < 3, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,MASK_TEST => success, decrypt: ${bytesToHex(decrypt)}")
                        VentilatorBleResponse.MaskTestResult(decrypt)
                    } else {
                        if (response.len < 3) {
                            LepuBleLog.d(tag, "model:$model,MASK_TEST => response.len < 3")
                            return
                        }
                        VentilatorBleResponse.MaskTestResult(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,MASK_TEST => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTest).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.GET_VENTILATION_SETTING -> {
                    if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 10) {
                            LepuBleLog.d(tag, "model:$model,GET_VENTILATION_SETTING => decrypt.size < 10, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_VENTILATION_SETTING => success, decrypt: ${bytesToHex(decrypt)}")
                        val data = VentilationSetting(decrypt)
                        LepuBleLog.d(tag, "model:$model, => success, data: $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSetting).post(InterfaceEvent(model, data))
                    }
                }
                VentilatorBleCmd.SET_VENTILATION_SETTING -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_VENTILATION_SETTING => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_VENTILATION_SETTING => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetVentilationSetting).post(InterfaceEvent(model, true))
                }
                VentilatorBleCmd.GET_WARNING_SETTING -> {
                    if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 10) {
                            LepuBleLog.d(tag, "model:$model,GET_WARNING_SETTING => decrypt.size < 10, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_WARNING_SETTING => success, decrypt: ${bytesToHex(decrypt)}")
                        val data = WarningSetting(decrypt)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSetting).post(InterfaceEvent(model, data))
                    }
                }
                VentilatorBleCmd.SET_WARNING_SETTING -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_WARNING_SETTING => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_WARNING_SETTING => success")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWarningSetting).post(InterfaceEvent(model, true))
                }
                VentilatorBleCmd.VENTILATION_SWITCH -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,VENTILATION_SWITCH => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                }
                VentilatorBleCmd.GET_FILE_LIST -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 12) {
                            LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => decrypt.size < 12, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success, decrypt: ${bytesToHex(decrypt)}")
                        VentilatorBleResponse.RecordList(decrypt)
                    } else {
                        if (response.len < 12) {
                            LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => response.len < 12")
                            return
                        }
                        VentilatorBleResponse.RecordList(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileList).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.READ_FILE_START -> {
                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(VentilatorBleCmd.readFileEnd(aesEncryptKey))
                        LepuBleLog.d(tag, "READ_FILE_START isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                        return
                    }
                    fileSize = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 4) {
                            LepuBleLog.d(tag, "model:$model,READ_FILE_START => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,READ_FILE_START => success, decrypt: ${bytesToHex(decrypt)}")
                        toUInt(decrypt)
                    } else {
                        if (response.len < 4) {
                            LepuBleLog.d(tag, "model:$model,READ_FILE_START => response.len < 4")
                            return
                        }
                        toUInt(response.content)
                    }
                    fileContent = if (offset == 0) {
                        ByteArray(0)
                    } else {
                        DownloadHelper.readFile(model, userId, fileName)
                    }
                    offset = fileContent.size
                    if (fileSize <= 0) {
                        sendCmd(VentilatorBleCmd.readFileEnd(aesEncryptKey))
                    } else {
                        sendCmd(VentilatorBleCmd.readFileData(offset, aesEncryptKey))
                    }
                }
                VentilatorBleCmd.READ_FILE_DATA -> {
                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(VentilatorBleCmd.readFileEnd(aesEncryptKey))
                        LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                        return
                    }
                    if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        LepuBleLog.d(tag, "model:$model,READ_FILE_DATA => success, decrypt: ${bytesToHex(decrypt)}")
                        offset += decrypt.size
                        DownloadHelper.writeFile(model, "", fileName, "dat", decrypt)
                        fileContent = fileContent.plus(decrypt)
                    } else {
                        offset += response.len
                        DownloadHelper.writeFile(model, "", fileName, "dat", response.content)
                        fileContent = fileContent.plus(response.content)
                    }
                    LepuBleLog.d(tag, "READ_FILE_DATA offset: $offset, fileSize: $fileSize")
                    val percent = offset.times(100).div(fileSize)
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadingFileProgress).post(InterfaceEvent(model, percent))
                    if (offset < fileSize) {
                        sendCmd(VentilatorBleCmd.readFileData(offset, aesEncryptKey))
                    } else {
                        sendCmd(VentilatorBleCmd.readFileEnd(aesEncryptKey))
                    }
                }
                VentilatorBleCmd.READ_FILE_END -> {
                    if (isCancelRF || isPausedRF) {
                        LepuBleLog.d(tag, "READ_FILE_END isCancelRF: $isCancelRF, isPausedRF: $isPausedRF, offset: $offset, fileSize: $fileSize")
                        return
                    }
                    if (fileContent.size < fileSize) {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError).post(InterfaceEvent(model, true))
                    } else {
                        val data = StatisticsFile(fileName, fileContent)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileComplete).post(InterfaceEvent(model, data))
                    }
                }
                VentilatorBleCmd.RT_STATE -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 4) {
                            LepuBleLog.d(tag, "model:$model,RT_STATE => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,RT_STATE => success, decrypt: ${bytesToHex(decrypt)}")
                        VentilatorBleResponse.RtState(decrypt)
                    } else {
                        if (response.len < 4) {
                            LepuBleLog.d(tag, "model:$model,RT_STATE => response.len < 4")
                            return
                        }
                        VentilatorBleResponse.RtState(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,RT_STATE => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtState).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.RT_PARAM -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 24) {
                            LepuBleLog.d(tag, "model:$model,RT_PARAM => decrypt.size < 24, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,RT_PARAM => success, decrypt: ${bytesToHex(decrypt)}")
                        VentilatorBleResponse.RtParam(decrypt)
                    } else {
                        if (response.len < 24) {
                            LepuBleLog.d(tag, "model:$model,RT_PARAM => response.len < 24")
                            return
                        }
                        VentilatorBleResponse.RtParam(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,RT_PARAM => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParam).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.EVENT -> {
                    val data = if (isEncryptMode) {
                        val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                        if (decrypt.size < 8) {
                            LepuBleLog.d(tag, "model:$model,EVENT => decrypt.size < 8, decrypt: ${bytesToHex(decrypt)}")
                            return
                        }
                        LepuBleLog.d(tag, "model:$model,EVENT => success, decrypt: ${bytesToHex(decrypt)}")
                        VentilatorBleResponse.Event(decrypt)
                    } else {
                        if (response.len < 8) {
                            LepuBleLog.d(tag, "model:$model,EVENT => response.len < 8")
                            return
                        }
                        VentilatorBleResponse.Event(response.content)
                    }
                    LepuBleLog.d(tag, "model:$model,EVENT => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEvent).post(InterfaceEvent(model, data))
                }
            }
        }
    }

    /**
     * get device info
     */
    override fun getInfo() {
        sendCmd(LpBleCmd.getInfo(aesEncryptKey))
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(LpBleCmd.setUtcTime(aesEncryptKey))
        LepuBleLog.d(tag, "syncTime...")
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
     * get real-time data
     */
    override fun getRtData() {
//        sendCmd(VentilatorBleCmd.getRtState(aesEncryptKey))
        sendCmd(VentilatorBleCmd.getRtParam(aesEncryptKey))
        LepuBleLog.d(tag, "getRtData...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        dealReadFile(userId, fileName)
        LepuBleLog.d(tag, "dealContinueRF...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.fileName = fileName
        LepuBleLog.d(tag, "dealReadFile...")
        sendCmd(VentilatorBleCmd.readFileStart(fileName.toByteArray(), 0, aesEncryptKey))
    }

    /**
     * get file list
     */
    override fun getFileList() {
        LepuBleLog.d(tag, "getFileList...")
    }
    fun getFileList(recordType: Int ,startTime: Long) {
        sendCmd(VentilatorBleCmd.getFileList(startTime, recordType, aesEncryptKey))
        LepuBleLog.d(tag, "getFileList...startTime: $startTime, recordType: $recordType")
    }
    fun echo(data: ByteArray) {
        sendCmd(LpBleCmd.echo(data, ByteArray(0)))
        LepuBleLog.d(tag, "echo...")
    }
    fun getBattery() {
        sendCmd(LpBleCmd.getBattery(aesEncryptKey))
        LepuBleLog.d(tag, "getBattery...")
    }
    fun burnFactoryInfo(config: FactoryConfig) {
        sendCmd(LpBleCmd.burnFactoryInfo(config.convert2Data(), aesEncryptKey))
        LepuBleLog.d(tag, "burnFactoryInfo...config: $config")
    }
    // 密钥交换
    fun encrypt(id: String) {
        val encrypt = LepuEncrypt(getAccessToken(id), lepuEncryptKey)
        sendCmd(LpBleCmd.encrypt(encrypt, ByteArray(0)))
        LepuBleLog.d(tag, "encrypt...lepuEncryptKey: ${bytesToHex(lepuEncryptKey)}")
        LepuBleLog.d(tag, "encrypt...encrypt: ${bytesToHex(encrypt)}")
        val decrypt = EncryptUtil.LepuDecrypt(encrypt, lepuEncryptKey)
        LepuBleLog.d(tag, "encrypt...decrypt: ${bytesToHex(decrypt)}")
    }
    // 绑定/解绑
    fun deviceBound(bound: Boolean) {
        sendCmd(VentilatorBleCmd.deviceBound(bound, aesEncryptKey))
    }
    // 设置用户信息
    fun setUserInfo(data: UserInfo) {
        sendCmd(VentilatorBleCmd.setUserInfo(data.getDataBytes(), aesEncryptKey))
    }
    // 获取用户信息
    fun getUserInfo() {
        sendCmd(VentilatorBleCmd.getUserInfo(aesEncryptKey))
    }
    // 进入医生模式
    fun doctorMode(pin: String, timestamp: Long) {
        if (isEncryptMode) {
            if (pin.length > 6) {
                sendCmd(VentilatorBleCmd.doctorMode(pin.toByteArray().copyOfRange(0, 6), timestamp, aesEncryptKey))
            } else {
                sendCmd(VentilatorBleCmd.doctorMode(pin.toByteArray(), timestamp, aesEncryptKey))
            }
        }
    }
    // 搜索WiFi列表
    fun getWifiList(deviceNum: Int) {
        sendCmd(VentilatorBleCmd.getWifiList(deviceNum, aesEncryptKey))
    }
    // 配置WiFi信息
    fun setWifiConfig(data: Bp2WifiConfig) {
        sendCmd(VentilatorBleCmd.setWifiConfig(data.getDataBytes(), aesEncryptKey))
    }
    // 获取WiFi信息
    fun getWifiConfig(option: Int) {
        sendCmd(VentilatorBleCmd.getWifiConfig(option, aesEncryptKey))
    }
    // 获取详细版本信息
    fun getVersionInfo() {
        sendCmd(VentilatorBleCmd.getVersionInfo(aesEncryptKey))
    }
    // 获取系统设置
    fun getSystemSetting() {
        sendCmd(VentilatorBleCmd.getSystemSetting(aesEncryptKey))
    }
    // 配置系统设置
    fun setSystemSetting(data: SystemSetting) {
        sendCmd(VentilatorBleCmd.setSystemSetting(data.getDataBytes(), aesEncryptKey))
    }
    // 获取测量设置
    fun getMeasureSetting() {
        sendCmd(VentilatorBleCmd.getMeasureSetting(aesEncryptKey))
    }
    // 配置测量设置
    fun setMeasureSetting(data: MeasureSetting) {
        sendCmd(VentilatorBleCmd.setMeasureSetting(data.getDataBytes(), aesEncryptKey))
    }
    // 佩戴测试
    fun maskTest(start: Boolean) {
        sendCmd(VentilatorBleCmd.maskTest(start, aesEncryptKey))
    }
    // 获取通气控制参数
    fun getVentilationSetting() {
        if (isEncryptMode) {
            sendCmd(VentilatorBleCmd.getVentilationSetting(aesEncryptKey))
        }
    }
    // 配置通气控制参数
    fun setVentilationSetting(data: VentilationSetting) {
        if (isEncryptMode) {
            sendCmd(VentilatorBleCmd.setVentilationSetting(data.getDataBytes(), aesEncryptKey))
        }
    }
    // 获取报警提示参数
    fun getWarningSetting() {
        if (isEncryptMode) {
            sendCmd(VentilatorBleCmd.getWarningSetting(aesEncryptKey))
        }
    }
    // 配置报警提示参数
    fun setWarningSetting(data: WarningSetting) {
        if (isEncryptMode) {
            sendCmd(VentilatorBleCmd.setWarningSetting(data.getDataBytes(), aesEncryptKey))
        }
    }
    // 启动/停止通气
    fun ventilationSwitch(start: Boolean) {
        if (isEncryptMode) {
            sendCmd(VentilatorBleCmd.ventilationSwitch(start, aesEncryptKey))
        }
    }
    // 实时状态获取
    fun getRtState() {
        sendCmd(VentilatorBleCmd.getRtState(aesEncryptKey))
    }
}