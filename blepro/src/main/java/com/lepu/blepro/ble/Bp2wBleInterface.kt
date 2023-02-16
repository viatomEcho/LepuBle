package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.Bp2wBleCmd
import com.lepu.blepro.ble.cmd.Bp2wBleCmd.*
import com.lepu.blepro.ble.cmd.LeBp2wBleCmd
import com.lepu.blepro.ble.cmd.LepuBleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.Bp2WifiConfig
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.bp2w.*
import com.lepu.blepro.utils.CrcUtil.calCRC8
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.add
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import kotlin.experimental.inv

/**
 * bp2w心电血压计：
 * send:
 * 1.同步时间
 * 2.获取设备信息
 * 3.获取实时心电、血压
 * 4.获取/配置参数
 * 5.获取/设置设备状态
 * 6.获取文件列表
 * 7.下载文件内容
 * 8.恢复出厂设置
 * 9.获取路由
 * 10.获取/配置WiFi
 * 心电采样率：实时250HZ，存储125HZ
 * 血压采样率：实时50HZ，存储50HZ
 * 心电增益：n * 0.003098-----322.7888960619755倍
 */
class Bp2wBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Bp2wBleInterface"
    private var deviceInfo = DeviceInfo()
    private var fileNames = arrayListOf<String>()
    private var bp2wFile = Bp2wFile()
    private var rtData = RtData()
    private var rtStatus = RtStatus()
    private var rtParam = RtParam()
    private var config = Bp2wConfig()
    /*private var wifiList = arrayListOf<Bp2Wifi>()
    private var wifi = Bp2Wifi()
    private var server = Bp2wServer()
    private var wifiConfig = com.lepu.blepro.ext.bp2w.Bp2WifiConfig()*/

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
    var fileName:String=""
    var curSize: Int = 0
    var fileContent : ByteArray? = null
    var userList: LeBp2wUserList? = null

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
        LepuBleLog.d(tag, "onResponseReceived bytes : ${bytesToHex(bleResponse.bytes)}")

        when (bleResponse.cmd) {
            GET_INFO -> {
                if (bleResponse.len <= 0 || bleResponse.content.size < 38) {
                    LepuBleLog.d(tag, "GET_INFO bleResponse.len <= 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                val info = LepuDevice(bleResponse.content)
                deviceInfo.hwVersion = info.hwV
                deviceInfo.swVersion = info.fwV
                deviceInfo.btlVersion = info.btlV
                deviceInfo.branchCode = info.branchCode
                deviceInfo.snLen = info.snLen
                deviceInfo.sn = info.sn
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wInfo).post(InterfaceEvent(model, deviceInfo))
            }

            SET_TIME -> {
                //同步时间
                LepuBleLog.d(tag, "model:$model,SET_TIME => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSyncTime).post(InterfaceEvent(model, true))
            }

            RT_STATE -> {
                if (bleResponse.len <= 0 || bleResponse.content.size < 7) {
                    LepuBleLog.d(tag, "RT_STATE bleResponse.len <= 0")
                    return
                }
                // 主机状态
                LepuBleLog.d(tag, "model:$model,RT_STATE => success")
                val rtState = Bp2BleRtState(bleResponse.content)
                rtStatus.deviceStatus = rtState.status
                rtStatus.deviceStatusMsg = rtState.statusMsg
                rtStatus.batteryStatus = rtState.battery.state
                rtStatus.batteryStatusMsg = rtState.battery.stateMsg
                rtStatus.percent = rtState.battery.percent
                rtStatus.vol = rtState.battery.vol
                rtStatus.avgCnt = rtState.avgCnt
                rtStatus.avgWaitTick = rtState.avgWaitTick
//                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtState).post(InterfaceEvent(model, rtStatus))
            }

            GET_FILE_LIST -> {
                LepuBleLog.d(tag, "model:$model,GET_FILE_LIST => success")
                if (bleResponse.len <= 0 || bleResponse.content.isEmpty()) {
                    LepuBleLog.d(tag, "GET_FILE_LIST bleResponse.len <= 0")
                    return
                }

                val list = if (device.name == null) {
                    KtBleFileList(bleResponse.content, "")
                } else {
                    KtBleFileList(bleResponse.content, device.name)
                }
                for (name in list.fileNameList) {
                    fileNames.add(name)
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFileList).post(InterfaceEvent(model, fileNames))
            }

            //----------------------------读文件--------------------------
            READ_FILE_START -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_START => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(readFileEnd())
                    LepuBleLog.d(tag, "READ_FILE_START isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_START => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }

                fileContent = null
                fileSize = toUInt(bleResponse.content)
                LepuBleLog.d(tag, "download file $fileName CMD_FILE_READ_START fileSize == $fileSize")
                if (fileSize <= 0) {
                    sendCmd(readFileEnd())
                } else {
                    sendCmd(readFileData(0))
                }
            }
            READ_FILE_DATA -> {
                LepuBleLog.d(tag, "model:$model,READ_FILE_CONTENT => success")

                //检查当前的下载状态
                if (isCancelRF || isPausedRF) {
                    sendCmd(readFileEnd())
                    LepuBleLog.d(tag, "READ_FILE_DATA isCancelRF:$isCancelRF, isPausedRF:$isPausedRF")
                    return
                }

                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_CONTENT => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }

                curSize += bleResponse.len
                val part = Bp2FilePart(fileName, fileSize, curSize)
                fileContent = add(fileContent, bleResponse.content)
                LepuBleLog.d(tag, "download file $fileName READ_FILE_CONTENT curSize == $curSize | fileSize == $fileSize")

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadingFileProgress).post(InterfaceEvent(model, (part.percent*100).toInt()))
                if (curSize < fileSize) {
                    sendCmd(readFileData(curSize))
                } else {
                    sendCmd(readFileEnd())
                }
            }
            READ_FILE_END -> {
                //检查返回是否异常
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LepuBleLog.d(tag, "model:$model, fileName = ${fileName}, READ_FILE_END => error")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileError).post(InterfaceEvent(model, fileName))
                    return
                }
                LepuBleLog.d(tag, "model:$model,READ_FILE_END => success")

                curSize = 0
                if (fileContent == null) fileContent = ByteArray(0)

                if (isCancelRF || isPausedRF){
                    LepuBleLog.d(tag, "已经取消/暂停下载 isCancelRF = $isCancelRF, isPausedRF = $isPausedRF" )
                    return
                }

                fileContent?.let {
                    if (it.isNotEmpty()) {
                        val data = if (device.name == null) {
                            Bp2BleFile(fileName, it, "")
                        } else {
                            Bp2BleFile(fileName, it, device.name)
                        }
                        bp2wFile.fileName = data.name
                        bp2wFile.type = data.type
                        bp2wFile.content = data.content
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileComplete).post(InterfaceEvent(model, bp2wFile))
                    }
                }
            }

            //实时波形
            RT_DATA -> {
                if (bleResponse.len <= 0 || bleResponse.content.size < 32) {
                    LepuBleLog.d(tag, "RT_DATA bleResponse.len <= 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,RT_DATA => success")

                val data = Bp2BleRtData(bleResponse.content)
                rtStatus.deviceStatus = data.rtState.status
                rtStatus.deviceStatusMsg = data.rtState.statusMsg
                rtStatus.batteryStatus = data.rtState.battery.state
                rtStatus.batteryStatusMsg = data.rtState.battery.stateMsg
                rtStatus.percent = data.rtState.battery.percent
                rtStatus.vol = data.rtState.battery.vol
                rtStatus.avgCnt = data.rtState.avgCnt
                rtStatus.avgWaitTick = data.rtState.avgWaitTick
                rtData.status = rtStatus
                rtParam.paramDataType = data.rtWave.waveDataType
                rtParam.paramData = data.rtWave.waveData
                rtParam.ecgBytes = data.rtWave.waveform
                rtParam.ecgShorts = data.rtWave.waveShorts
                rtParam.ecgFloats = data.rtWave.waveFloats
                rtData.param = rtParam
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtData).post(InterfaceEvent(model, rtData))
            }

            SET_CONFIG -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetConfig).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SET_CONFIG => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,SET_CONFIG => success")
                //心跳音开关
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetConfig).post(InterfaceEvent(model, true))
            }

            GET_CONFIG -> {
                if (bleResponse.len <= 0 || bleResponse.content.size < 27) {
                    LepuBleLog.d(tag, "GET_CONFIG bleResponse.len <= 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,GET_CONFIG => success")
                val data = Bp2Config(bleResponse.content)
                config.isSoundOn = data.beepSwitch
                config.avgMeasureMode = data.avgMeasureMode
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetConfig).post(InterfaceEvent(model, config))
            }

            RESET -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReset).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,RESET => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReset).post(InterfaceEvent(model, true))
            }

            FACTORY_RESET -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryReset).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryReset).post(InterfaceEvent(model, true))
            }

            FACTORY_RESET_ALL -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryResetAll).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,FACTORY_RESET_ALL => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryResetAll).post(InterfaceEvent(model, true))
            }

            SWITCH_STATE ->{
                if (bleResponse.pkgType != 0x01.toByte()) {
//                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSwitchState).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SWITCH_STATE => error")
                    return
                }

                LepuBleLog.d(tag, "model:$model,SWITCH_STATE => success")
                //切换状态
//                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSwitchState).post(InterfaceEvent(model, true))
            }

            GET_WIFI_ROUTE -> {
                LepuBleLog.d(tag, "model:$model,GET_WIFI_ROUTE => success")
                LepuBleLog.d(tag, "model:$model,bytesToHex == " + bytesToHex(bleResponse.content))
                if (bleResponse.pkgType == 0xFF.toByte()) {
//                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2WifiScanning).post(InterfaceEvent(model, true))
                } else {
                    val data = Bp2WifiDevice(bleResponse.content)
                    LepuBleLog.d(tag, "model:$model, data.toString == $data")
                    /*for (w in data.wifiList) {
                        val wifi = Bp2Wifi()
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
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2WifiList).post(InterfaceEvent(model, wifiList))*/
                }
            }

            GET_WIFI_CONFIG -> {
                if (bleResponse.len <= 0) {
                    LepuBleLog.d(tag, "GET_WIFI_CONFIG bleResponse.len <= 0")
                    return
                }
                LepuBleLog.d(tag, "model:$model,GET_WIFI_CONFIG => success")
                LepuBleLog.d(tag, "model:$model,bytesToHex == " + bytesToHex(bleResponse.content))
                val data = Bp2WifiConfig(bleResponse.content)
                LepuBleLog.d(tag, "model:$model, data.toString == $data")
                /*wifi.state = data.wifi.state
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
                server.state = data.server.state
                server.addrType = data.server.addrType
                server.addrLen = data.server.addrLen
                server.addr = data.server.addr
                server.port = data.server.port
                wifiConfig.server = server
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetWifiConfig).post(InterfaceEvent(model, wifiConfig))*/
            }

            SET_WIFI_CONFIG -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
//                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetWifiConfig).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => error")
                    return
                }
//                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetWifiConfig).post(InterfaceEvent(model, true))
                LepuBleLog.d(tag, "model:$model,SET_WIFI_CONFIG => success")
            }

            DELETE_FILE -> {
                if (bleResponse.pkgType != 0x01.toByte()) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wDeleteFile).post(InterfaceEvent(model, false))
                    LepuBleLog.d(tag, "model:$model,DELETE_FILE => error")
                    return
                }
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wDeleteFile).post(InterfaceEvent(model, true))
                LepuBleLog.d(tag, "model:$model,DELETE_FILE => success")
            }

        }
    }

    override fun getInfo() {
        sendCmd(Bp2wBleCmd.getInfo())
        LepuBleLog.d(tag, "getInfo...")
    }

    override fun syncTime() {
        sendCmd(setTime())
        LepuBleLog.d(tag, "syncTime...")
    }

    fun deleteFile() {
        sendCmd(Bp2wBleCmd.deleteFile())
        LepuBleLog.d(tag, "deleteFile...")
    }

    //实时波形命令
    override fun getRtData() {
        sendCmd(Bp2wBleCmd.getRtData())
        LepuBleLog.d(tag, "getRtData ...")
    }

    fun setConfig(c: Bp2wConfig){
        val config = Bp2Config()
        config.beepSwitch = c.isSoundOn
        config.avgMeasureMode = c.avgMeasureMode
        sendCmd(setConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setConfig...$config")
    }
     fun getConfig(){
         sendCmd(Bp2wBleCmd.getConfig())
         LepuBleLog.d(tag, "getConfig...")
    }

    override fun getFileList() {
        fileNames.clear()
        sendCmd(Bp2wBleCmd.getFileList())
        LepuBleLog.d(tag, "getFileList...")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        this.fileName = fileName
        sendCmd(readFileStart(fileName.toByteArray(), 0))
        LepuBleLog.d(tag, "dealReadFile... userId:$userId, fileName == $fileName")
    }

    override fun reset() {
        sendCmd(Bp2wBleCmd.reset())
        LepuBleLog.d(tag, "reset...")
    }
    override fun factoryReset() {
        sendCmd(Bp2wBleCmd.factoryReset())
        LepuBleLog.d(tag, "factoryReset...")
    }
    override fun factoryResetAll() {
        sendCmd(Bp2wBleCmd.factoryResetAll())
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
        sendCmd(Bp2wBleCmd.switchState(state))
        LepuBleLog.e("enter  switchState： SWITCH_STATE===$state")
    }

    /**
     * 获取wifi路由
     */
    fun getWifiDevice() {
//        wifiList.clear()
        sendCmd(getWifiRoute(0))
        LepuBleLog.d(tag, "getWifiDevice...")
    }

    /**
     * 配置要连接的wifi
     */
    fun setWifiConfig(config: Bp2WifiConfig) {
        sendCmd(setWifiConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setWifiConfig...config:$config")
    }

    /*fun setWifiConfig(c: com.lepu.blepro.ext.bp2w.Bp2WifiConfig) {
        val config = Bp2WifiConfig()
        config.option = 3
        val wifi = com.lepu.blepro.ble.data.Bp2Wifi()
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
        sendCmd(LeBp2wBleCmd.setWifiConfig(config.getDataBytes()))
        LepuBleLog.d(tag, "setWifiConfig...config:$config")
    }*/

    /**
     * 获取当前配置的wifi
     */
    fun getWifiConfig() {
        sendCmd(Bp2wBleCmd.getWifiConfig())
        LepuBleLog.d(tag, "getWifiConfig...")
    }

    fun getRtState() {
        sendCmd(Bp2wBleCmd.getRtState())
        LepuBleLog.d(tag, "getRtState...")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

}