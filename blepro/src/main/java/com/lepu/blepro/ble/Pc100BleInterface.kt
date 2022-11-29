package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.Pc100DeviceInfo
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.HexString.trimStr
import java.util.*

/**
 * pc100血压血氧体温设备：
 * send:
 * 1.获取设备信息
 * receive:
 * 1.实时血氧、血压
 * 血氧采样率：参数1HZ，波形50HZ
 */

class Pc100BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "PC100BleInterface"

    private lateinit var context: Context
    private var pc100Device = Pc100DeviceInfo()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Pc100BleManager(context)
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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Pc100BleResponse.Pc100Response) {
        LepuBleLog.d(tag, "onResponseReceived cmd: ${response.cmd}, bytes: ${bytesToHex(response.bytes)}")
        when (response.cmd) {
            Pc100BleCmd.TOKEN_0XFF -> {
                when (response.type) {
                    Pc100BleCmd.HAND_SHAKE -> {
                        LepuBleLog.d(tag, "model:$model,HAND_SHAKE => success")
                        val str = toString(response.content).split(":")
                        pc100Device.deviceName = trimStr(toString(response.content))
                        device.name?.let {
                            pc100Device.deviceName = it
                        }
                        pc100Device.sn = str[1]
                        LepuBleLog.d(tag, "model:$model,HAND_SHAKE deviceName => " + toString(response.content))
                        LepuBleLog.d(tag, "model:$model,HAND_SHAKE sn => " + pc100Device.sn)
                    }
                    Pc100BleCmd.GET_DEVICE_ID -> {
                        LepuBleLog.d(tag, "model:$model,GET_DEVICE_ID => success")
                        pc100Device.deviceId = toUInt(response.content)
                        LepuBleLog.d(tag, "model:$model,GET_DEVICE_ID deviceId => " + toUInt(response.content))
                        LepuBleLog.d(tag, "model:$model,GET_DEVICE_ID response.content[0] => " + (response.content[0].toUInt() and 0xFFu).toInt())
                        LepuBleLog.d(tag, "model:$model,GET_DEVICE_ID response.content[1] => " + (response.content[1].toUInt() and 0xFFu).toInt())
                    }
                }
            }
            Pc100BleCmd.GET_DEVICE_INFO -> {
                LepuBleLog.d(tag, "model:$model,GET_DEVICE_INFO => success")
                if (response.len != 5) {
                    LepuBleLog.d(tag, "model:$model,bytesToHex(response.content) == " + bytesToHex(response.content))
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoFingerOut).post(InterfaceEvent(model, true))
                } else {
                    val info = Pc100BleResponse.DeviceInfo(response.content)
                    pc100Device.softwareV = info.softwareV
                    pc100Device.hardwareV = info.hardwareV
                    pc100Device.batLevel = info.batLevel
                    pc100Device.batStatus = info.batStatus
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo).post(InterfaceEvent(model, pc100Device))
                    LepuBleLog.d(tag, "model:$model,GET_DEVICE_INFO info.softwareV => " + info.softwareV)
                    LepuBleLog.d(tag, "model:$model,GET_DEVICE_INFO info.hardwareV => " + info.hardwareV)
                    LepuBleLog.d(tag, "model:$model,GET_DEVICE_INFO info.batLevel => " + info.batLevel)
                    LepuBleLog.d(tag, "model:$model,GET_DEVICE_INFO info.batStatus => " + info.batStatus)
                }
            }

            Pc100BleCmd.BP_MODULE_STATE -> {
                when (response.type) {
                    Pc100BleCmd.BP_START -> {
                        LepuBleLog.d(tag, "model:$model,BP_MODULE_STATE BP_START => success")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpStart).post(InterfaceEvent(model, true))
                    }
                    Pc100BleCmd.BP_END -> {
                        LepuBleLog.d(tag, "model:$model,BP_MODULE_STATE BP_END => success")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpStop).post(InterfaceEvent(model, true))
                    }
                }
            }
            Pc100BleCmd.BP_GET_RESULT -> {
                when (response.type) {
                    Pc100BleCmd.BP_RESULT -> {
                        LepuBleLog.d(tag, "model:$model,BP_GET_RESULT BP_RESULT => success")
                        val info = Pc100BleResponse.BpResult(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult).post(InterfaceEvent(model, info))
                        LepuBleLog.d(tag, "model:$model,BP_RESULT info.sys => " + info.sys)
                        LepuBleLog.d(tag, "model:$model,BP_RESULT info.map => " + info.map)
                        LepuBleLog.d(tag, "model:$model,BP_RESULT info.dia => " + info.dia)
                        LepuBleLog.d(tag, "model:$model,BP_RESULT info.plus => " + info.plus)
                    }
                    Pc100BleCmd.BP_RESULT_ERROR -> {
                        LepuBleLog.d(tag, "model:$model,BP_GET_RESULT BP_RESULT_ERROR => success")
                        val info = Pc100BleResponse.BpResultError(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult).post(InterfaceEvent(model, info))
                        LepuBleLog.d(tag, "model:$model,BP_RESULT_ERROR info.errorType => " + info.errorType)
                        LepuBleLog.d(tag, "model:$model,BP_RESULT_ERROR info.errorNum => " + info.errorNum)
                        LepuBleLog.d(tag, "model:$model,BP_RESULT_ERROR info.errorMess => " + info.errorMess)
                    }
                }
            }
            Pc100BleCmd.BP_GET_STATUS -> {
                LepuBleLog.d(tag, "model:$model,BP_GET_STATUS => success")
                val info = Pc100BleResponse.BpStatus(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpStatus).post(InterfaceEvent(model, info))
                LepuBleLog.d(tag, "model:$model,BP_GET_STATUS info.status => " + info.status)
                LepuBleLog.d(tag, "model:$model,BP_GET_STATUS info.statusMess => " + info.statusMess)
            }
            Pc100BleCmd.BP_RT_DATA -> {
                LepuBleLog.d(tag, "model:$model,BP_RT_DATA => success")
                val info = Pc100BleResponse.RtBpData(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpRtData).post(InterfaceEvent(model, info))
                LepuBleLog.d(tag, "model:$model,BP_RT_DATA info.sign => " + info.sign)
                LepuBleLog.d(tag, "model:$model,BP_RT_DATA info.psValue => " + info.psValue)
            }

            Pc100BleCmd.BO_MODULE_STATE -> {
                when (response.type) {
                    Pc100BleCmd.BO_START -> {
                        LepuBleLog.d(tag, "model:$model,BO_MODULE_STATE BO_START => success")
                        val status = (response.content[0].toUInt() and 0xFFu).toInt()
                        LepuBleLog.d(tag, "model:$model,BO_START info.status => $status")
                    }
                    Pc100BleCmd.BO_END -> {
                        LepuBleLog.d(tag, "model:$model,BO_MODULE_STATE BO_END => success")
                    }
                }
            }
            Pc100BleCmd.BO_GET_STATUS -> {
                LepuBleLog.d(tag, "model:$model,BO_GET_STATUS => success")
                val info = Pc100BleResponse.BoStatus(response.content)
                LepuBleLog.d(tag, "model:$model,BO_GET_STATUS info.status => " + info.status)
                LepuBleLog.d(tag, "model:$model,BO_GET_STATUS info.statusMess => " + info.statusMess)
                LepuBleLog.d(tag, "model:$model,BO_GET_STATUS info.sw_ver => " + info.sw_ver)
                LepuBleLog.d(tag, "model:$model,BO_GET_STATUS info.hw_ver => " + info.hw_ver)
            }
            Pc100BleCmd.BO_RT_WAVE -> {
                LepuBleLog.d(tag, "model:$model,BO_RT_WAVE => success")
                val info = Pc100BleResponse.RtBoWave(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoRtWave).post(InterfaceEvent(model, info))
            }
            Pc100BleCmd.BO_RT_PARAM -> {
                LepuBleLog.d(tag, "model:$model,BO_RT_PARAM => success")
                val info = Pc100BleResponse.RtBoParam(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BoRtParam).post(InterfaceEvent(model, info))
                LepuBleLog.d(tag, "model:$model,BO_RT_PARAM info.spo2 => " + info.spo2)
                LepuBleLog.d(tag, "model:$model,BO_RT_PARAM info.pr => " + info.pr)
                LepuBleLog.d(tag, "model:$model,BO_RT_PARAM info.pi => " + info.pi)
            }

            Pc100BleCmd.BS_MODULE_STATE -> {
                when (response.type) {
                    Pc100BleCmd.BS_START -> {
                        LepuBleLog.d(tag, "model:$model,BS_MODULE_STATE BS_START => success")
                        val status = (response.content[0].toUInt() and 0xFFu).toInt()
                        LepuBleLog.d(tag, "model:$model,BS_START info.status => $status")
                    }
                    Pc100BleCmd.BS_END -> {
                        LepuBleLog.d(tag, "model:$model,BS_MODULE_STATE BS_END => success")
                    }
                }
            }
            Pc100BleCmd.BS_GET_RESULT -> {
                LepuBleLog.d(tag, "model:$model,BS_GET_RESULT => success")
                val info = Pc100BleResponse.BsResult(response.content)
                LepuBleLog.d(tag, "model:$model,BS_GET_RESULT info.type => " + info.type)
                LepuBleLog.d(tag, "model:$model,BS_GET_RESULT info.unit => " + info.unit)
                LepuBleLog.d(tag, "model:$model,BS_GET_RESULT info.data => " + info.data)
            }
            Pc100BleCmd.BS_GET_STATUS -> {
                LepuBleLog.d(tag, "model:$model,BS_GET_STATUS => success")
                val info = Pc100BleResponse.BsStatus(response.content)
                LepuBleLog.d(tag, "model:$model,BS_GET_STATUS info.status => " + info.status)
                LepuBleLog.d(tag, "model:$model,BS_GET_STATUS info.sw_ver => " + info.sw_ver)
                LepuBleLog.d(tag, "model:$model,BS_GET_STATUS info.hw_ver => " + info.hw_ver)
            }

            Pc100BleCmd.BT_MODULE_STATE -> {
                when (response.type) {
                    Pc100BleCmd.BT_START -> {
                        LepuBleLog.d(tag, "model:$model,BT_MODULE_STATE BT_START => success")
                        val status = (response.content[0].toUInt() and 0xFFu).toInt()
                        LepuBleLog.d(tag, "model:$model,BT_START info.status => $status")
                    }
                    Pc100BleCmd.BT_END -> {
                        LepuBleLog.d(tag, "model:$model,BT_MODULE_STATE BT_END => success")
                    }
                }
            }
            Pc100BleCmd.BT_GET_RESULT -> {
                LepuBleLog.d(tag, "model:$model,BT_GET_RESULT => success")
                val info = Pc100BleResponse.BtResult(response.content)
                LepuBleLog.d(tag, "model:$model,BT_GET_RESULT info.status => " + info.status)
                LepuBleLog.d(tag, "model:$model,BT_GET_RESULT info.data => " + info.data)
            }
            Pc100BleCmd.BT_GET_STATUS -> {
                LepuBleLog.d(tag, "model:$model,BT_GET_STATUS => success")
                val info = Pc100BleResponse.BtStatus(response.content)
                LepuBleLog.d(tag, "model:$model,BT_GET_STATUS info.status => " + info.status)
                LepuBleLog.d(tag, "model:$model,BT_GET_STATUS info.sw_ver => " + info.sw_ver)
                LepuBleLog.d(tag, "model:$model,BT_GET_STATUS info.hw_ver => " + info.hw_ver)
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes
        if (bytes == null || bytes.size < 5) {
            return bytes
        }
        loop@ for (i in 0 until bytes.size-4) {
            if (bytes[i] != 0xAA.toByte()) {
                continue@loop
            }
            if (bytes[i + 1] != 0x55.toByte()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+3, i+4))
//            Log.d(TAG, "want bytes length: $len")
            if (i+4+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)
            if (temp.last() == CrcUtil.calCRC8Pc(temp)) {
                val bleResponse = Pc100BleResponse.Pc100Response(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }

    override fun getInfo() {
        sendCmd(Pc100BleCmd.handShake())
        sendCmd(Pc100BleCmd.getDeviceId())
        sendCmd(Pc100BleCmd.getDeviceInfo())
        LepuBleLog.d(tag, "getInfo")
    }

    override fun getFileList() {
        sendCmd(Pc100BleCmd.getBpResult())
        LepuBleLog.d(tag, "getFileList")
    }

    fun getBpState() {
        sendCmd(Pc100BleCmd.getBpStatus())
        LepuBleLog.d(tag, "getBpState")
    }
    fun startBp() {
        sendCmd(Pc100BleCmd.setBpModuleState(Pc100BleCmd.BP_START))
        LepuBleLog.d(tag, "startBp")
    }
    fun stopBp() {
        sendCmd(Pc100BleCmd.setBpModuleState(Pc100BleCmd.BP_END))
        LepuBleLog.d(tag, "stopBp")
    }

    fun startBo() {
        sendCmd(Pc100BleCmd.setBoModuleState(Pc100BleCmd.BO_START))
        LepuBleLog.d(tag, "startBo")
    }
    fun stopBo() {
        sendCmd(Pc100BleCmd.setBoModuleState(Pc100BleCmd.BO_END))
        LepuBleLog.d(tag, "stopBo")
    }
    fun getBoState() {
        sendCmd(Pc100BleCmd.getBoStatus())
        LepuBleLog.d(tag, "getBoState")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealReadFile not yet implemented")
    }

    override fun syncTime() {
        LepuBleLog.e(tag, "syncTime not yet implemented")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset not yet implemented")
    }

    override fun factoryReset() {
        LepuBleLog.e(tag, "factoryReset not yet implemented")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryResetAll not yet implemented")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

    override fun getRtData() {
        LepuBleLog.e(tag, "getRtData not yet implemented")
    }

}