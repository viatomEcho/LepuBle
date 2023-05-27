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
import com.lepu.blepro.ble.data.ventilator.MeasureSetting
import com.lepu.blepro.ble.data.ventilator.StatisticsFile
import com.lepu.blepro.ble.data.ventilator.SystemSetting
import com.lepu.blepro.ble.data.ventilator.VentilationSetting
import com.lepu.blepro.ble.data.ventilator.WarningSetting
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.ext.ventilator.*
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

    private var deviceInfo = DeviceInfo()
    private var doctorModeResult = DoctorModeResult()
    private var wifiList = arrayListOf<Wifi>()
    private var wifiConfig = WifiConfig()
    private var versionInfo = VersionInfo()
    private var systemSetting = com.lepu.blepro.ext.ventilator.SystemSetting()
    private var measureSetting = com.lepu.blepro.ext.ventilator.MeasureSetting()
    private var ventilationSetting = com.lepu.blepro.ext.ventilator.VentilationSetting()
    private var warningSetting = com.lepu.blepro.ext.ventilator.WarningSetting()
    private var maskTestResult = MaskTestResult()
    private var recordList = RecordList()
    private var rtState = RtState()
    private var rtParam = RtParam()
    private var event = Event()
    private var statisticsFile = com.lepu.blepro.ext.ventilator.StatisticsFile()

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
//        if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
//            val data = ResponseError()
//            data.model = model
//            data.cmd = response.cmd
//            data.type = response.pkgType
//            LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError).post(data)
//            LepuBleLog.d(tag, "model:$model,ResponseError => $data")
//            if (response.cmd == LpBleCmd.ECHO) {
//                LiveEventBus.get<ByteArray>(EventMsgConst.Cmd.EventCmdResponseEchoData).post(response.bytes)
//            }
//        } else {
            when(response.cmd) {
                LpBleCmd.ECHO -> {
                    LepuBleLog.d(tag, "model:$model,ECHO => success ${bytesToHex(response.content)}")
                    LiveEventBus.get<ByteArray>(EventMsgConst.Cmd.EventCmdResponseEchoData).post(response.bytes)
                }
                LpBleCmd.SET_UTC_TIME -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_UTC_TIME => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_UTC_TIME => success, response.pkgType: ${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetUtcTime).post(InterfaceEvent(model, response.pkgType))
                }
                LpBleCmd.GET_INFO -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_INFO => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfoError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 38) {
                                LepuBleLog.d(tag, "model:$model,GET_INFO => decrypt.size < 38, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfoError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_INFO => success, decrypt: ${bytesToHex(decrypt)}")
                            LepuDevice(decrypt)
                        } else {
                            if (response.len < 38) {
                                LepuBleLog.d(tag, "model:$model,GET_INFO => response.len < 38")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfoError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuDevice(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,GET_INFO => success, data: $data")
                        deviceInfo.hwVersion = data.hwV
                        deviceInfo.swVersion = data.fwV
                        deviceInfo.btlVersion = data.btlV
                        deviceInfo.branchCode = data.branchCode
                        deviceInfo.fileVer = data.fileV
                        deviceInfo.spcpVer = data.protocolV
                        deviceInfo.snLen = data.snLen
                        deviceInfo.sn = data.sn
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetInfo).post(InterfaceEvent(model, deviceInfo))
                    }
                }
                LpBleCmd.RESET -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,RESET => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,RESET => success, response.pkgType: ${response.pkgType}")
//                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReset).post(InterfaceEvent(model, response.pkgType))
                }
                LpBleCmd.FACTORY_RESET -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success, response.pkgType: ${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorFactoryReset).post(InterfaceEvent(model, response.pkgType))
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
//                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetBattery).post(InterfaceEvent(model, data))
                }
                LpBleCmd.BURN_FACTORY_INFO -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,BURN_FACTORY_INFO => success")
//                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorBurnFactoryInfo).post(InterfaceEvent(model, true))
                }
                LpBleCmd.ENCRYPT -> {
                    val decrypt = EncryptUtil.LepuDecrypt(response.content, lepuEncryptKey)
                    if (decrypt.size < 4) {
                        LepuBleLog.d(tag, "model:$model,ENCRYPT => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEncrypt).post(InterfaceEvent(model, 238))
                        return
                    }
                    LepuBleLog.d(tag, "model:$model,ENCRYPT => success, decrypt: ${bytesToHex(decrypt)}")
                    val data = VentilatorBleResponse.EncryptInfo(decrypt)
                    aesEncryptKey = data.key
                    isEncryptMode = true
                    LepuBleLog.d(tag, "model:$model,ENCRYPT => success, data: $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEncrypt).post(InterfaceEvent(model, response.pkgType))
                }
                VentilatorBleCmd.DEVICE_BOUND -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBoundError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        // 0x00成功, 0x01失败, 0x02超时
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.isEmpty()) {
                                LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => decrypt.isEmpty()")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBoundError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => success, decrypt: ${bytesToHex(decrypt)}")
                            toUInt(decrypt)
                        } else {
                            if (response.content.isEmpty()) {
                                LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => response.content.isEmpty()")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBoundError).post(InterfaceEvent(model, 238))
                                return
                            }
                            toUInt(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,DEVICE_BOUND => success, data: $data")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceBound).post(InterfaceEvent(model, data))
                    }
                }
                VentilatorBleCmd.DEVICE_UNBOUND -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,DEVICE_UNBOUND => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,DEVICE_UNBOUND => success, response.pkgType: ${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDeviceUnBound).post(InterfaceEvent(model, response.pkgType))
                }
                VentilatorBleCmd.SET_USER_INFO -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_USER_INFO => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_USER_INFO => success")
//                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetUserInfo).post(InterfaceEvent(model, true))
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
//                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetUserInfo).post(InterfaceEvent(model, data))
                }
                VentilatorBleCmd.DOCTOR_MODE_IN -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,DOCTOR_MODE_IN => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorModeError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 2) {
                                LepuBleLog.d(tag, "model:$model,DOCTOR_MODE_IN => decrypt.size < 2, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorModeError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,DOCTOR_MODE_IN => success, decrypt: ${bytesToHex(decrypt)}")
                            val data = VentilatorBleResponse.DoctorModeResult(false, decrypt)
                            doctorModeResult.isOut = data.isOut
                            doctorModeResult.isSuccess = data.success
                            doctorModeResult.errCode = data.errCode
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorMode).post(InterfaceEvent(model, doctorModeResult))
                        }
                    }
                }
                VentilatorBleCmd.DOCTOR_MODE_OUT -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,DOCTOR_MODE_OUT => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorModeError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 2) {
                                LepuBleLog.d(tag, "model:$model,DOCTOR_MODE_OUT => decrypt.size < 2, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorModeError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,DOCTOR_MODE_OUT => success, decrypt: ${bytesToHex(decrypt)}")
                            val data = VentilatorBleResponse.DoctorModeResult(true, decrypt)
                            doctorModeResult.isOut = data.isOut
                            doctorModeResult.isSuccess = data.success
                            doctorModeResult.errCode = data.errCode
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorDoctorMode).post(InterfaceEvent(model, doctorModeResult))
                        }
                    }
                }
                VentilatorBleCmd.GET_WIFI_LIST -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiListError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.isEmpty()) {
                                LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => decrypt.isEmpty()")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiListError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => success, decrypt: ${bytesToHex(decrypt)}")
                            Bp2WifiDevice(decrypt)
                        } else {
                            if (response.content.isEmpty()) {
                                LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => response.content.isEmpty()")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiListError).post(InterfaceEvent(model, 238))
                                return
                            }
                            Bp2WifiDevice(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_LIST => success, data : $data")
                        for (w in data.wifiList) {
                            val wifi = Wifi()
                            wifi.state = w.state
                            wifi.ssidLen = w.ssidLen
                            wifi.ssid = w.ssid
                            wifi.type = w.type
                            wifi.rssi = w.rssi
                            wifi.pwdLen = w.pwdLen
                            wifi.pwd = w.pwd
                            wifi.macAddr = w.macAddr
                            wifi.ipType = w.ipType
                            wifi.ipLen = w.ipLen
                            wifi.ipAddr = w.ipAddr
                            wifi.netmaskLen = w.netmaskLen
                            wifi.netmaskAddr = w.netmaskAddr
                            wifi.gatewayLen = w.gatewayLen
                            wifi.gatewayAddr = w.gatewayAddr
                            wifiList.add(wifi)
                        }
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiList).post(InterfaceEvent(model, wifiList))
                    }
                }
                VentilatorBleCmd.SET_WIFI_CONFIG -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success, response.pkgType: ${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWifiConfig).post(InterfaceEvent(model, response.pkgType))
                }
                VentilatorBleCmd.GET_WIFI_CONFIG -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfigError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.isEmpty()) {
                                LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => decrypt.isEmpty()")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfigError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success, decrypt: ${bytesToHex(decrypt)}")
                            Bp2WifiConfig(decrypt)
                        } else {
                            if (response.content.isEmpty()) {
                                LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => response.content.isEmpty()")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfigError).post(InterfaceEvent(model, 238))
                                return
                            }
                            Bp2WifiConfig(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success, data: $data")
                        val wifi = Wifi()
                        wifi.state = data.wifi.state
                        wifi.ssidLen = data.wifi.ssidLen
                        wifi.ssid = data.wifi.ssid
                        wifi.type = data.wifi.type
                        wifi.rssi = data.wifi.rssi
                        wifi.pwdLen = data.wifi.pwdLen
                        wifi.pwd = data.wifi.pwd
                        wifi.macAddr = data.wifi.macAddr
                        wifi.ipType = data.wifi.ipType
                        wifi.ipLen = data.wifi.ipLen
                        wifi.ipAddr = data.wifi.ipAddr
                        wifi.netmaskLen = data.wifi.netmaskLen
                        wifi.netmaskAddr = data.wifi.netmaskAddr
                        wifi.gatewayLen = data.wifi.gatewayLen
                        wifi.gatewayAddr = data.wifi.gatewayAddr
                        wifiConfig.wifi = wifi
                        val server = Server()
                        server.state = data.server.state
                        server.addrType = data.server.addrType
                        server.addrLen = data.server.addrLen
                        server.addr = data.server.addr
                        server.port = data.server.port
                        wifiConfig.server = server
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWifiConfig).post(InterfaceEvent(model, wifiConfig))
                    }
                }
                VentilatorBleCmd.GET_VERSION_INFO -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfoError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 17) {
                                LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => decrypt.size < 17, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfoError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => success, decrypt: ${bytesToHex(decrypt)}")
                            VentilatorBleResponse.VersionInfo(decrypt)
                        } else {
                            if (response.content.size < 17) {
                                LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => response.content.size < 17")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfoError).post(InterfaceEvent(model, 238))
                                return
                            }
                            VentilatorBleResponse.VersionInfo(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,GET_VERSION_INFO => success, data: $data")
                        versionInfo.hwV = data.hwV
                        versionInfo.fwV = data.fwV
                        versionInfo.blV = data.blV
                        versionInfo.bleV = data.bleV
                        versionInfo.algV = data.algV
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVersionInfo).post(InterfaceEvent(model, versionInfo))
                    }
                }
                VentilatorBleCmd.GET_SYSTEM_SETTING -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSettingError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 9) {
                                LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => decrypt.size < 9, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSettingError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => success, decrypt: ${bytesToHex(decrypt)}")
                            SystemSetting(decrypt)
                        } else {
                            if (response.len < 9) {
                                LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => response.len < 9")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSettingError).post(InterfaceEvent(model, 238))
                                return
                            }
                            SystemSetting(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,GET_SYSTEM_SETTING => success, data: $data")
                        val unitSetting = com.lepu.blepro.ext.ventilator.SystemSetting().UnitSetting()
                        unitSetting.pressureUnit = data.unitSetting.pressureUnit
                        systemSetting.unitSetting = unitSetting
                        val languageSetting = com.lepu.blepro.ext.ventilator.SystemSetting().LanguageSetting()
                        languageSetting.language = data.languageSetting.language
                        systemSetting.languageSetting = languageSetting
                        val screenSetting = com.lepu.blepro.ext.ventilator.SystemSetting().ScreenSetting()
                        screenSetting.brightness = data.screenSetting.brightness
                        screenSetting.autoOff = data.screenSetting.autoOff
                        systemSetting.screenSetting = screenSetting
                        val replacements = com.lepu.blepro.ext.ventilator.SystemSetting().Replacements()
                        replacements.filter = data.replacements.filter
                        replacements.mask = data.replacements.mask
                        replacements.tube = data.replacements.tube
                        replacements.tank = data.replacements.tank
                        systemSetting.replacements = replacements
                        val volumeSetting = com.lepu.blepro.ext.ventilator.SystemSetting().VolumeSetting()
                        volumeSetting.volume = data.volumeSetting.volume
                        systemSetting.volumeSetting = volumeSetting
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetSystemSetting).post(InterfaceEvent(model, systemSetting))
                    }
                }
                VentilatorBleCmd.SET_SYSTEM_SETTING -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SETTING => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_SYSTEM_SETTING => success, response.pkgType: ${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetSystemSetting).post(InterfaceEvent(model, response.pkgType))
                }
                VentilatorBleCmd.GET_MEASURE_SETTING -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSettingError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 10) {
                                LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => decrypt.size < 10, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSettingError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => success, decrypt: ${bytesToHex(decrypt)}")
                            MeasureSetting(decrypt)
                        } else {
                            if (response.len < 10) {
                                LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => response.len < 10")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSettingError).post(InterfaceEvent(model, 238))
                                return
                            }
                            MeasureSetting(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,GET_MEASURE_SETTING => success, data: $data")
                        val humidification = com.lepu.blepro.ext.ventilator.MeasureSetting().Humidification()
                        humidification.humidification = data.humidification.humidification
                        measureSetting.humidification = humidification
                        val pressureReduce = com.lepu.blepro.ext.ventilator.MeasureSetting().PressureReduce()
                        pressureReduce.epr = data.pressureReduce.epr
                        measureSetting.pressureReduce = pressureReduce
                        val autoSwitch = com.lepu.blepro.ext.ventilator.MeasureSetting().AutoSwitch()
                        autoSwitch.isAutoStart = data.autoSwitch.autoStart
                        autoSwitch.isAutoEnd = data.autoSwitch.autoEnd
                        measureSetting.autoSwitch = autoSwitch
                        val preHeat = com.lepu.blepro.ext.ventilator.MeasureSetting().PreHeat()
                        preHeat.isOn = data.preHeat.on
                        measureSetting.preHeat = preHeat
                        val ramp = com.lepu.blepro.ext.ventilator.MeasureSetting().Ramp()
                        ramp.pressure = data.ramp.pressure
                        ramp.time = data.ramp.time
                        measureSetting.ramp = ramp
                        val tubeType = com.lepu.blepro.ext.ventilator.MeasureSetting().TubeType()
                        tubeType.type = data.tubeType.type
                        measureSetting.tubeType = tubeType
                        val mask = com.lepu.blepro.ext.ventilator.MeasureSetting().Mask()
                        mask.type = data.mask.type
                        mask.pressure = data.mask.pressure
                        measureSetting.mask = mask
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetMeasureSetting).post(InterfaceEvent(model, measureSetting))
                    }
                }
                VentilatorBleCmd.SET_MEASURE_SETTING -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_MEASURE_SETTING => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_MEASURE_SETTING => success, response.pkgType: ${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetMeasureSetting).post(InterfaceEvent(model, response.pkgType))
                }
                VentilatorBleCmd.MASK_TEST -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,MASK_TEST => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTestError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 3) {
                                LepuBleLog.d(tag, "model:$model,MASK_TEST => decrypt.size < 3, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTestError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,MASK_TEST => success, decrypt: ${bytesToHex(decrypt)}")
                            VentilatorBleResponse.MaskTestResult(decrypt)
                        } else {
                            if (response.len < 3) {
                                LepuBleLog.d(tag, "model:$model,MASK_TEST => response.len < 3")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTestError).post(InterfaceEvent(model, 238))
                                return
                            }
                            VentilatorBleResponse.MaskTestResult(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,MASK_TEST => success, data: $data")
                        maskTestResult.status = data.status
                        maskTestResult.leak = data.leak
                        maskTestResult.result = data.result
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorMaskTest).post(InterfaceEvent(model, maskTestResult))
                    }
                }
                VentilatorBleCmd.GET_VENTILATION_SETTING -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_VENTILATION_SETTING => error, response.pkgType: $response.pkgType")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSettingError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 10) {
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSettingError).post(InterfaceEvent(model, 238))
                                LepuBleLog.d(tag, "model:$model,GET_VENTILATION_SETTING => decrypt.size < 10, decrypt: ${bytesToHex(decrypt)}")
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_VENTILATION_SETTING => success, decrypt: ${bytesToHex(decrypt)}")
                            val data = VentilationSetting(decrypt)
                            LepuBleLog.d(tag, "model:$model, => success, data: $data")
                            val ventilationMode = com.lepu.blepro.ext.ventilator.VentilationSetting().VentilationMode()
                            ventilationMode.mode = data.ventilationMode.mode
                            ventilationSetting.ventilationMode = ventilationMode
                            val cpapPressure = com.lepu.blepro.ext.ventilator.VentilationSetting().CpapPressure()
                            cpapPressure.pressure = data.cpapPressure.pressure
                            ventilationSetting.cpapPressure = cpapPressure
                            val apapPressureMax = com.lepu.blepro.ext.ventilator.VentilationSetting().ApapPressureMax()
                            apapPressureMax.max = data.apapPressureMax.max
                            ventilationSetting.apapPressureMax = apapPressureMax
                            val apapPressureMin = com.lepu.blepro.ext.ventilator.VentilationSetting().ApapPressureMin()
                            apapPressureMin.min = data.apapPressureMin.min
                            ventilationSetting.apapPressureMin = apapPressureMin
                            val pressureInhale = com.lepu.blepro.ext.ventilator.VentilationSetting().PressureInhale()
                            pressureInhale.inhale = data.pressureInhale.inhale
                            ventilationSetting.pressureInhale = pressureInhale
                            val pressureExhale = com.lepu.blepro.ext.ventilator.VentilationSetting().PressureExhale()
                            pressureExhale.exhale = data.pressureExhale.exhale
                            ventilationSetting.pressureExhale = pressureExhale
                            val inhaleDuration = com.lepu.blepro.ext.ventilator.VentilationSetting().InhaleDuration()
                            inhaleDuration.duration = data.inhaleDuration.duration
                            ventilationSetting.inhaleDuration = inhaleDuration
                            val respiratoryRate = com.lepu.blepro.ext.ventilator.VentilationSetting().RespiratoryRate()
                            respiratoryRate.rate = data.respiratoryRate.rate
                            ventilationSetting.respiratoryRate = respiratoryRate
                            val pressureRaiseDuration = com.lepu.blepro.ext.ventilator.VentilationSetting().PressureRaiseDuration()
                            pressureRaiseDuration.duration = data.pressureRaiseDuration.duration
                            ventilationSetting.pressureRaiseDuration = pressureRaiseDuration
                            val inhaleSensitive = com.lepu.blepro.ext.ventilator.VentilationSetting().InhaleSensitive()
                            inhaleSensitive.sentive = data.inhaleSensitive.sentive
                            ventilationSetting.inhaleSensitive = inhaleSensitive
                            val exhaleSensitive = com.lepu.blepro.ext.ventilator.VentilationSetting().ExhaleSensitive()
                            exhaleSensitive.sentive = data.exhaleSensitive.sentive
                            ventilationSetting.exhaleSensitive = exhaleSensitive
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetVentilationSetting).post(InterfaceEvent(model, ventilationSetting))
                        }
                    }
                }
                VentilatorBleCmd.SET_VENTILATION_SETTING -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_VENTILATION_SETTING => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_VENTILATION_SETTING => success, response.pkgType: ${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetVentilationSetting).post(InterfaceEvent(model, response.pkgType))
                }
                VentilatorBleCmd.GET_WARNING_SETTING -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_WARNING_SETTING => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSettingError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 10) {
                                LepuBleLog.d(tag, "model:$model,GET_WARNING_SETTING => decrypt.size < 10, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSettingError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_WARNING_SETTING => success, decrypt: ${bytesToHex(decrypt)}")
                            val data = WarningSetting(decrypt)
                            val warningApnea = com.lepu.blepro.ext.ventilator.WarningSetting().WarningApnea()
                            warningApnea.apnea = data.warningApnea.apnea
                            warningSetting.warningApnea = warningApnea
                            val warningLeak = com.lepu.blepro.ext.ventilator.WarningSetting().WarningLeak()
                            warningLeak.high = data.warningLeak.high
                            warningSetting.warningLeak = warningLeak
                            val warningVt = com.lepu.blepro.ext.ventilator.WarningSetting().WarningVt()
                            warningVt.low = data.warningVt.low
                            warningSetting.warningVt = warningVt
                            val warningVentilation = com.lepu.blepro.ext.ventilator.WarningSetting().WarningVentilation()
                            warningVentilation.low = data.warningVentilation.low
                            warningSetting.warningVentilation = warningVentilation
                            val warningRrHigh = com.lepu.blepro.ext.ventilator.WarningSetting().WarningRrHigh()
                            warningRrHigh.high = data.warningRrHigh.high
                            warningSetting.warningRrHigh = warningRrHigh
                            val warningRrLow = com.lepu.blepro.ext.ventilator.WarningSetting().WarningRrLow()
                            warningRrLow.low = data.warningRrLow.low
                            warningSetting.warningRrLow = warningRrLow
                            val warningSpo2Low = com.lepu.blepro.ext.ventilator.WarningSetting().WarningSpo2Low()
                            warningSpo2Low.low = data.warningSpo2Low.low
                            warningSetting.warningSpo2Low = warningSpo2Low
                            val warningHrHigh = com.lepu.blepro.ext.ventilator.WarningSetting().WarningHrHigh()
                            warningHrHigh.high = data.warningHrHigh.high
                            warningSetting.warningHrHigh = warningHrHigh
                            val warningHrLow = com.lepu.blepro.ext.ventilator.WarningSetting().WarningHrLow()
                            warningHrLow.low = data.warningHrLow.low
                            warningSetting.warningHrLow = warningHrLow
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetWarningSetting).post(InterfaceEvent(model, warningSetting))
                        }
                    }
                }
                VentilatorBleCmd.SET_WARNING_SETTING -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,SET_WARNING_SETTING => success, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LepuBleLog.d(tag, "model:$model,SET_WARNING_SETTING => success, response.pkgType: ${response.pkgType}")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorSetWarningSetting).post(InterfaceEvent(model, response.pkgType))
                }
                VentilatorBleCmd.VENTILATION_SWITCH -> {
                    if (isEncryptMode) {
                        LepuBleLog.d(tag, "model:$model,VENTILATION_SWITCH => success, response.pkgType: ${response.pkgType}, data : ${bytesToHex(EncryptUtil.AesDecrypt(response.content, aesEncryptKey))}")
                    }
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorVentilationSwitch).post(InterfaceEvent(model, response.pkgType))
                }
                VentilatorBleCmd.GET_FILE_LIST -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileListError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 12) {
                                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => decrypt.size < 12, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileListError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success, decrypt: ${bytesToHex(decrypt)}")
                            VentilatorBleResponse.RecordList(decrypt)
                        } else {
                            if (response.len < 12) {
                                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => response.len < 12")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileListError).post(InterfaceEvent(model, 238))
                                return
                            }
                            VentilatorBleResponse.RecordList(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success, data: $data")
                        recordList.startTime = data.startTime
                        recordList.type = data.type
                        recordList.size = data.size
                        for (record in data.list) {
                            val r = RecordList().Record()
                            r.measureTime = record.measureTime
                            r.updateTime = record.updateTime
                            recordList.list.add(r)
                        }
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorGetFileList).post(InterfaceEvent(model, recordList))
                    }
                }
                VentilatorBleCmd.READ_FILE_START -> {
                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(VentilatorBleCmd.readFileEnd(aesEncryptKey))
                        LepuBleLog.d(tag, "READ_FILE_START isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                        return
                    }
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,READ_FILE_START => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        fileSize = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 4) {
                                LepuBleLog.d(tag, "model:$model,READ_FILE_START => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,READ_FILE_START => success, decrypt: ${bytesToHex(decrypt)}")
                            toUInt(decrypt)
                        } else {
                            if (response.len < 4) {
                                LepuBleLog.d(tag, "model:$model,READ_FILE_START => response.len < 4")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError).post(InterfaceEvent(model, 238))
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
                }
                VentilatorBleCmd.READ_FILE_DATA -> {
                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(VentilatorBleCmd.readFileEnd(aesEncryptKey))
                        LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF: $isCancelRF, isPausedRF: $isPausedRF")
                        return
                    }
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,READ_FILE_DATA => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError).post(InterfaceEvent(model, response.pkgType))
                    } else {
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
                }
                VentilatorBleCmd.READ_FILE_END -> {
                    if (isCancelRF || isPausedRF) {
                        LepuBleLog.d(tag, "READ_FILE_END isCancelRF: $isCancelRF, isPausedRF: $isPausedRF, offset: $offset, fileSize: $fileSize")
                        return
                    }
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,READ_FILE_END => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        if (fileContent.size < fileSize) {
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileError).post(InterfaceEvent(model, 238))
                        } else {
                            val data = StatisticsFile(fileName, fileContent)
                            statisticsFile.fileName = data.fileName
                            statisticsFile.fileVersion = data.fileVersion
                            statisticsFile.fileType = data.fileType
                            statisticsFile.duration = data.duration
                            statisticsFile.usageDays = data.usageDays
                            statisticsFile.moreThan4hDays = data.moreThan4hDays
                            statisticsFile.meanSecond = data.meanSecond
                            statisticsFile.spont = data.spont
                            statisticsFile.ahiCount = data.ahiCount
                            statisticsFile.aiCount = data.aiCount
                            statisticsFile.hiCount = data.hiCount
                            statisticsFile.oaiCount = data.oaiCount
                            statisticsFile.caiCount = data.caiCount
                            statisticsFile.rearCount = data.rearCount
                            statisticsFile.sniCount = data.sniCount
                            statisticsFile.pbCount = data.pbCount
                            statisticsFile.takeOffCount = data.takeOffCount
                            statisticsFile.llTime = data.llTime
                            statisticsFile.pressure = data.pressure
                            statisticsFile.ipap = data.ipap
                            statisticsFile.epap = data.epap
                            statisticsFile.vt = data.vt
                            statisticsFile.mv = data.mv
                            statisticsFile.leak = data.leak
                            statisticsFile.rr = data.rr
                            statisticsFile.ti = data.ti
                            statisticsFile.ie = data.ie
                            statisticsFile.spo2 = data.spo2
                            statisticsFile.pr = data.pr
                            statisticsFile.hr = data.hr
                            LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorReadFileComplete).post(InterfaceEvent(model, statisticsFile))
                        }
                    }
                }
                VentilatorBleCmd.RT_STATE -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,RT_STATE => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtStateError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 4) {
                                LepuBleLog.d(tag, "model:$model,RT_STATE => decrypt.size < 4, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtStateError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,RT_STATE => success, decrypt: ${bytesToHex(decrypt)}")
                            VentilatorBleResponse.RtState(decrypt)
                        } else {
                            if (response.len < 4) {
                                LepuBleLog.d(tag, "model:$model,RT_STATE => response.len < 4")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtStateError).post(InterfaceEvent(model, 238))
                                return
                            }
                            VentilatorBleResponse.RtState(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,RT_STATE => success, data: $data")
                        rtState.ventilationMode = data.ventilationMode
                        rtState.isVentilated = data.isVentilated
                        rtState.deviceMode = data.deviceMode
                        rtState.standard = data.standard
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtState).post(InterfaceEvent(model, rtState))
                    }
                }
                VentilatorBleCmd.RT_PARAM -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,RT_PARAM => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParamError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 24) {
                                LepuBleLog.d(tag, "model:$model,RT_PARAM => decrypt.size < 24, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParamError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,RT_PARAM => success, decrypt: ${bytesToHex(decrypt)}")
                            VentilatorBleResponse.RtParam(decrypt)
                        } else {
                            if (response.len < 24) {
                                LepuBleLog.d(tag, "model:$model,RT_PARAM => response.len < 24")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParamError).post(InterfaceEvent(model, 238))
                                return
                            }
                            VentilatorBleResponse.RtParam(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,RT_PARAM => success, data: $data")
                        rtParam.pressure = data.pressure
                        rtParam.ipap = data.ipap
                        rtParam.epap = data.epap
                        rtParam.vt = data.vt
                        rtParam.mv = data.mv
                        rtParam.leak = data.leak
                        rtParam.rr = data.rr
                        rtParam.ti = data.ti
                        rtParam.ie = data.ie
                        rtParam.spo2 = data.spo2
                        rtParam.pr = data.pr
                        rtParam.hr = data.hr
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorRtParam).post(InterfaceEvent(model, rtParam))
                    }
                }
                VentilatorBleCmd.EVENT -> {
                    if (response.pkgType != LpBleCmd.TYPE_NORMAL_RESPONSE) {
                        LepuBleLog.d(tag, "model:$model,EVENT => error, response.pkgType: ${response.pkgType}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEventError).post(InterfaceEvent(model, response.pkgType))
                    } else {
                        val data = if (isEncryptMode) {
                            val decrypt = EncryptUtil.AesDecrypt(response.content, aesEncryptKey)
                            if (decrypt.size < 8) {
                                LepuBleLog.d(tag, "model:$model,EVENT => decrypt.size < 8, decrypt: ${bytesToHex(decrypt)}")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEventError).post(InterfaceEvent(model, 238))
                                return
                            }
                            LepuBleLog.d(tag, "model:$model,EVENT => success, decrypt: ${bytesToHex(decrypt)}")
                            VentilatorBleResponse.Event(decrypt)
                        } else {
                            if (response.len < 8) {
                                LepuBleLog.d(tag, "model:$model,EVENT => response.len < 8")
                                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEventError).post(InterfaceEvent(model, 238))
                                return
                            }
                            VentilatorBleResponse.Event(response.content)
                        }
                        LepuBleLog.d(tag, "model:$model,EVENT => success, data: $data")
                        event.timestamp = data.timestamp
                        event.isAlarm = data.alarm
                        event.alarmLevel = data.alarmLevel
                        event.eventId = data.eventId
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Ventilator.EventVentilatorEvent).post(InterfaceEvent(model, event))
                    }
                }
            }
//        }
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
//        sendCmd(VentilatorBleCmd.getRtParam(aesEncryptKey))
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
    fun doctorModeIn(pin: String, timestamp: Long) {
        if (isEncryptMode) {
            if (pin.length > 6) {
                sendCmd(VentilatorBleCmd.doctorModeIn(pin.toByteArray().copyOfRange(0, 6), timestamp, aesEncryptKey))
            } else {
                sendCmd(VentilatorBleCmd.doctorModeIn(pin.toByteArray(), timestamp, aesEncryptKey))
            }
        }
    }
    // 退出医生模式
    fun doctorModeOut() {
        if (isEncryptMode) {
            sendCmd(VentilatorBleCmd.doctorModeOut(aesEncryptKey))
        }
    }
    // 搜索WiFi列表
    fun getWifiList(deviceNum: Int) {
        sendCmd(VentilatorBleCmd.getWifiList(deviceNum, aesEncryptKey))
    }
    // 配置WiFi信息
    fun setWifiConfig(c: WifiConfig) {
        val config = Bp2WifiConfig()
        config.option = 3
        val wifi = Bp2Wifi()
        wifi.state = c.wifi.state
        wifi.ssidLen = c.wifi.ssidLen
        wifi.ssid = c.wifi.ssid
        wifi.type = c.wifi.type
        wifi.rssi = c.wifi.rssi
        wifi.pwdLen = c.wifi.pwdLen
        wifi.pwd = c.wifi.pwd
        wifi.macAddr = c.wifi.macAddr
        wifi.ipType = c.wifi.ipType
        wifi.ipLen = c.wifi.ipLen
        wifi.ipAddr = c.wifi.ipAddr
        wifi.netmaskLen = c.wifi.netmaskLen
        wifi.netmaskAddr = c.wifi.netmaskAddr
        wifi.gatewayLen = c.wifi.gatewayLen
        wifi.gatewayAddr = c.wifi.gatewayAddr
        config.wifi = wifi
        val server = Bp2Server()
        server.state = c.server.state
        server.addrType = c.server.addrType
        server.addrLen = c.server.addrLen
        server.addr = c.server.addr
        server.port = c.server.port
        config.server = server
        sendCmd(VentilatorBleCmd.setWifiConfig(config.getDataBytes(), aesEncryptKey))
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
    fun setSystemSetting(data: com.lepu.blepro.ext.ventilator.SystemSetting) {
        val systemSetting = SystemSetting()
        systemSetting.type = data.type
        val unitSetting = SystemSetting.UnitSetting()
        unitSetting.pressureUnit = data.unitSetting.pressureUnit
        systemSetting.unitSetting = unitSetting
        val languageSetting = SystemSetting.LanguageSetting()
        languageSetting.language = data.languageSetting.language
        systemSetting.languageSetting = languageSetting
        val screenSetting = SystemSetting.ScreenSetting()
        screenSetting.brightness = data.screenSetting.brightness
        screenSetting.autoOff = data.screenSetting.autoOff
        systemSetting.screenSetting = screenSetting
        val replacements = SystemSetting.Replacements()
        replacements.filter = data.replacements.filter
        replacements.mask = data.replacements.mask
        replacements.tube = data.replacements.tube
        replacements.tank = data.replacements.tank
        systemSetting.replacements = replacements
        val volumeSetting = SystemSetting.VolumeSetting()
        volumeSetting.volume = data.volumeSetting.volume
        systemSetting.volumeSetting = volumeSetting
        sendCmd(VentilatorBleCmd.setSystemSetting(systemSetting.getDataBytes(), aesEncryptKey))
    }
    // 获取测量设置
    fun getMeasureSetting() {
        sendCmd(VentilatorBleCmd.getMeasureSetting(aesEncryptKey))
    }
    // 配置测量设置
    fun setMeasureSetting(data: com.lepu.blepro.ext.ventilator.MeasureSetting) {
        val measureSetting = MeasureSetting()
        measureSetting.type = data.type
        val humidification = MeasureSetting.Humidification()
        humidification.humidification = data.humidification.humidification
        measureSetting.humidification = humidification
        val pressureReduce = MeasureSetting.PressureReduce()
        pressureReduce.epr = data.pressureReduce.epr
        measureSetting.pressureReduce = pressureReduce
        val autoSwitch = MeasureSetting.AutoSwitch()
        autoSwitch.autoStart = data.autoSwitch.isAutoStart
        autoSwitch.autoEnd = data.autoSwitch.isAutoEnd
        measureSetting.autoSwitch = autoSwitch
        val preHeat = MeasureSetting.PreHeat()
        preHeat.on = data.preHeat.isOn
        measureSetting.preHeat = preHeat
        val ramp = MeasureSetting.Ramp()
        ramp.pressure = data.ramp.pressure
        ramp.time = data.ramp.time
        measureSetting.ramp = ramp
        val tubeType = MeasureSetting.TubeType()
        tubeType.type = data.tubeType.type
        measureSetting.tubeType = tubeType
        val mask = MeasureSetting.Mask()
        mask.type = data.mask.type
        mask.pressure = data.mask.pressure
        measureSetting.mask = mask
        sendCmd(VentilatorBleCmd.setMeasureSetting(measureSetting.getDataBytes(), aesEncryptKey))
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
    fun setVentilationSetting(data: com.lepu.blepro.ext.ventilator.VentilationSetting) {
        if (isEncryptMode) {
            val ventilationSetting = VentilationSetting()
            ventilationSetting.type = data.type
            val ventilationMode = VentilationSetting.VentilationMode()
            ventilationMode.mode = data.ventilationMode.mode
            ventilationSetting.ventilationMode = ventilationMode
            val cpapPressure = VentilationSetting.CpapPressure()
            cpapPressure.pressure = data.cpapPressure.pressure
            ventilationSetting.cpapPressure = cpapPressure
            val apapPressureMax = VentilationSetting.ApapPressureMax()
            apapPressureMax.max = data.apapPressureMax.max
            ventilationSetting.apapPressureMax = apapPressureMax
            val apapPressureMin = VentilationSetting.ApapPressureMin()
            apapPressureMin.min = data.apapPressureMin.min
            ventilationSetting.apapPressureMin = apapPressureMin
            val pressureInhale = VentilationSetting.PressureInhale()
            pressureInhale.inhale = data.pressureInhale.inhale
            ventilationSetting.pressureInhale = pressureInhale
            val pressureExhale = VentilationSetting.PressureExhale()
            pressureExhale.exhale = data.pressureExhale.exhale
            ventilationSetting.pressureExhale = pressureExhale
            val inhaleDuration = VentilationSetting.InhaleDuration()
            inhaleDuration.duration = data.inhaleDuration.duration
            ventilationSetting.inhaleDuration = inhaleDuration
            val respiratoryRate = VentilationSetting.RespiratoryRate()
            respiratoryRate.rate = data.respiratoryRate.rate
            ventilationSetting.respiratoryRate = respiratoryRate
            val pressureRaiseDuration = VentilationSetting.PressureRaiseDuration()
            pressureRaiseDuration.duration = data.pressureRaiseDuration.duration
            ventilationSetting.pressureRaiseDuration = pressureRaiseDuration
            val inhaleSensitive = VentilationSetting.InhaleSensitive()
            inhaleSensitive.sentive = data.inhaleSensitive.sentive
            ventilationSetting.inhaleSensitive = inhaleSensitive
            val exhaleSensitive = VentilationSetting.ExhaleSensitive()
            exhaleSensitive.sentive = data.exhaleSensitive.sentive
            ventilationSetting.exhaleSensitive = exhaleSensitive
            sendCmd(VentilatorBleCmd.setVentilationSetting(ventilationSetting.getDataBytes(), aesEncryptKey))
        }
    }
    // 获取报警提示参数
    fun getWarningSetting() {
        if (isEncryptMode) {
            sendCmd(VentilatorBleCmd.getWarningSetting(aesEncryptKey))
        }
    }
    // 配置报警提示参数
    fun setWarningSetting(data: com.lepu.blepro.ext.ventilator.WarningSetting) {
        if (isEncryptMode) {
            val warningSetting = WarningSetting()
            warningSetting.type = data.type
            val warningApnea = WarningSetting.WarningApnea()
            warningApnea.apnea = data.warningApnea.apnea
            warningSetting.warningApnea = warningApnea
            val warningLeak = WarningSetting.WarningLeak()
            warningLeak.high = data.warningLeak.high
            warningSetting.warningLeak = warningLeak
            val warningVt = WarningSetting.WarningVt()
            warningVt.low = data.warningVt.low
            warningSetting.warningVt = warningVt
            val warningVentilation = WarningSetting.WarningVentilation()
            warningVentilation.low = data.warningVentilation.low
            warningSetting.warningVentilation = warningVentilation
            val warningRrHigh = WarningSetting.WarningRrHigh()
            warningRrHigh.high = data.warningRrHigh.high
            warningSetting.warningRrHigh = warningRrHigh
            val warningRrLow = WarningSetting.WarningRrLow()
            warningRrLow.low = data.warningRrLow.low
            warningSetting.warningRrLow = warningRrLow
            val warningSpo2Low = WarningSetting.WarningSpo2Low()
            warningSpo2Low.low = data.warningSpo2Low.low
            warningSetting.warningSpo2Low = warningSpo2Low
            val warningHrHigh = WarningSetting.WarningHrHigh()
            warningHrHigh.high = data.warningHrHigh.high
            warningSetting.warningHrHigh = warningHrHigh
            val warningHrLow = WarningSetting.WarningHrLow()
            warningHrLow.low = data.warningHrLow.low
            warningSetting.warningHrLow = warningHrLow
            sendCmd(VentilatorBleCmd.setWarningSetting(warningSetting.getDataBytes(), aesEncryptKey))
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
    // 获取实时参数
    fun getRtParam() {
        sendCmd(VentilatorBleCmd.getRtParam(aesEncryptKey))
    }
}