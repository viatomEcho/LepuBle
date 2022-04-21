package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.Pc300BleCmd.*
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig
import com.lepu.blepro.utils.HexString.trimStr

/**
 *
 * 蓝牙操作
 */

class Pc300BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Pc300BleInterface"

    private lateinit var context: Context

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
                LepuBleLog.d(tag, "Device Init")
            }
            .enqueue()
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Pc300BleResponse.BleResponse) {

        when (response.cmd) {
            TOKEN_0XFF -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XFF => success ${bytesToHex(response.content)}")
                when (response.type) {
                    SET_DEVICE_ID -> {
                        val data = toUInt(response.content)
                        LepuBleLog.d(tag, "model:$model,SET_DEVICE_ID 设置产品 ID => success $data")
                    }
                    GET_DEVICE_ID -> {
                        val data = toUInt(response.content)
                        LepuBleLog.d(tag, "model:$model,GET_DEVICE_ID 查询产品 ID => success $data")
                    }
                    GET_DEVICE_NAME -> {
                        val data = trimStr(toString(response.content))
                        LepuBleLog.d(tag, "model:$model,GET_DEVICE_NAME 查询产品名称 => success $data")
                    }
                    DEVICE_INFO_2 -> {
                        val data = Pc300BleResponse.DeviceInfo(response.content)
                        LepuBleLog.d(tag, "model:$model,DEVICE_INFO_2 查询版本及电量等级 => success $data")
                    }
                    DEVICE_INFO_4 -> {
                        val data = Pc300BleResponse.DeviceInfo(response.content)
                        LepuBleLog.d(tag, "model:$model,DEVICE_INFO_4 查询版本及电量等级 => success $data")
                    }
                    SET_TIME -> {
                        LepuBleLog.d(tag, "model:$model,SET_TIME 设置时间 => success ${bytesToHex(response.content)}")
                    }
                }
            }
            TOKEN_0XD0 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XD0 上传PC_300SNT 关机命令信息 => success ${bytesToHex(response.content)}")
            }
            TOKEN_0X40 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X40 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    BP_START -> {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpStart).post(InterfaceEvent(model, true))
                        LepuBleLog.d(tag, "model:$model,BP_START 血压开始测量命令 => success ${bytesToHex(response.content)}")
                    }
                    BP_STOP -> {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpStop).post(InterfaceEvent(model, true))
                        LepuBleLog.d(tag, "model:$model,BP_STOP 血压停止测量命令 => success ${bytesToHex(response.content)}")
                    }
                    BP_MODE -> {
                        val data = toUInt(response.content)
                        LepuBleLog.d(tag, "model:$model,BP_MODE 血压模式命令 => success $data")
                    }
                }
            }
            TOKEN_0X42 -> {
                val data = Pc300BleResponse.RtBpData(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtBpData).post(InterfaceEvent(model, data))
                LepuBleLog.d(tag, "model:$model,TOKEN_0X42 血压当前值和心跳信息 => success $data")
            }
            TOKEN_0X43 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X43 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    BP_RESULT -> {
                        val data = Pc300BleResponse.BpResult(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpResult).post(InterfaceEvent(model, data))
                        LepuBleLog.d(tag, "model:$model,BP_RESULT 血压测量结果 => success $data")
                    }
                    BP_ERROR_RESULT -> {
                        val data = Pc300BleResponse.BpResultError(response.content)
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300BpErrorResult).post(InterfaceEvent(model, data))
                        LepuBleLog.d(tag, "model:$model,BP_ERROR_RESULT 血压测量出现的错误结果 => success $data")
                    }
                }
            }
            TOKEN_0X51 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X51 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    OXY_RT_STATE -> {
                        val data = Pc300BleResponse.RtOxyState(response.content)
                        LepuBleLog.d(tag, "model:$model,OXY_RT_STATE 血氧上传状态数据包 => success $data")
                    }
                    DEVICE_INFO -> {
                        val data = Pc300BleResponse.DeviceInfo(response.content)
                        LepuBleLog.d(tag, "model:$model,DEVICE_INFO 查询产品版本及电量等级 => success $data")
                    }
                }
            }
            TOKEN_0X52 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X52 血氧上传波形数据包 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    DISABLE_WAVE -> {
                        LepuBleLog.d(tag, "model:$model,DISABLE_WAVE 禁止主动发送数据 => success ${bytesToHex(response.content)}")
                    }
                    ENABLE_WAVE -> {
                        val data = Pc300BleResponse.RtOxyWave(response.content)
                        LepuBleLog.d(tag, "model:$model,ENABLE_WAVE 允许主动发送数据 => success ${bytesToHex(data.bytes)}")
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyWave).post(InterfaceEvent(model, data))
                    }
                }
            }
            TOKEN_0X53 -> {
                val data = Pc300BleResponse.RtOxyParam(response.content)
                LepuBleLog.d(tag, "model:$model,TOKEN_0X53 血氧上传参数数据包 => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtOxyParam).post(
                    InterfaceEvent(model, data)
                )
            }
            TOKEN_0X70 -> {
                val data = (byte2UInt(response.content[0]) and 0x40) shr 5
                LepuBleLog.d(tag, "model:$model,TOKEN_0X70 体温开始测量命令 => success $data")
            }
            TOKEN_0X72 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X72 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    TEMP_RESULT -> {
                        val data = Pc300BleResponse.TempResult(response.content)
                        LepuBleLog.d(tag, "model:$model,TEMP_RESULT 体温测量结果 => success $data")
                    }
                    SET_TEMP_CONFIG -> {
                        LepuBleLog.d(tag, "model:$model,SET_TEMP_CONFIG 配置体温计参数 => success ${bytesToHex(response.content)}")
                    }
                    GET_TEMP_CONFIG -> {
                        LepuBleLog.d(tag, "model:$model,GET_TEMP_CONFIG 查询体温计参数 => success ${bytesToHex(response.content)}")
                    }
                }
            }
            TOKEN_0X73 -> {
                val data = Pc300BleResponse.BsResult(response.content)
                LepuBleLog.d(tag, "model:$model,TOKEN_0X73 血糖结果 => success $data")
            }
            TOKEN_0XE0 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE0 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    BS_UNIT -> {
                        LepuBleLog.d(tag, "model:$model,BS_UNIT 控制血糖显示单位(仅适用百捷) => success ${bytesToHex(response.content)}")
                    }
                }
            }
            TOKEN_0XE2 -> {
                val data = bytes2UIntBig(response.content[0], response.content[1])
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE2 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    GLU_RESULT -> {
                        LepuBleLog.d(tag, "model:$model,GLU_RESULT 血糖 => success $data")
                    }
                    UA_RESULT -> {
                        LepuBleLog.d(tag, "model:$model,UA_RESULT 尿酸 => success $data")
                    }
                    CHOL_RESULT -> {
                        LepuBleLog.d(tag, "model:$model,CHOL_RESULT 总胆固醇 => success $data")
                    }
                }
            }
            TOKEN_0XE3 -> {
                val data = toUInt(response.content)
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE3 设置下位机血糖仪类型 => success $data")
            }
            TOKEN_0XE4 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE4 查询下位机当前配置的血糖仪类型 => success ${bytesToHex(response.content)}")
            }
            TOKEN_0XE5 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE5 清除血糖历史数据 => success ${bytesToHex(response.content)}")
            }
            TOKEN_0X30 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X30 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    ECG_START -> {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgStart).post(InterfaceEvent(model, true))
                        LepuBleLog.d(tag, "model:$model,ECG_START 心电开始测量命令 => success ${bytesToHex(response.content)}")
                    }
                    ECG_STOP -> {
                        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300EcgStop).post(InterfaceEvent(model, true))
                        LepuBleLog.d(tag, "model:$model,ECG_STOP 心电停止测量命令 => success ${bytesToHex(response.content)}")
                    }
                    ECG_DATA_DIGIT -> {
                        LepuBleLog.d(tag, "model:$model,ECG_DATA_DIGIT 设置心电数据位数 => success ${bytesToHex(response.content)}")
                    }
                }
            }
            TOKEN_0X31 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X31 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    GET_VERSION -> {
                        val data = Pc300BleResponse.DeviceInfo(response.content)
                        LepuBleLog.d(tag, "model:$model,GET_VERSION 心电查询版本 => success $data")
                    }
                    ECG_RT_STATE -> {
                        val data = Pc300BleResponse.RtEcgState(response.content)
                        LepuBleLog.d(tag, "model:$model,ECG_RT_STATE 心电查询工作状态 => success $data")
                    }
                }
            }
            TOKEN_0X32 -> {
                val data = Pc300BleResponse.RtEcgWave(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtEcgWave).post(
                    InterfaceEvent(model, data)
                )
                LepuBleLog.d(tag, "model:$model,TOKEN_0X32 心电波形上传数据 => success $data")
            }
            TOKEN_0X33 -> {
                val data = Pc300BleResponse.RtEcgResult(response.content)
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300RtEcgResult).post(InterfaceEvent(model, data))
                LepuBleLog.d(tag, "model:$model,TOKEN_0X33 心电结果上传参数 => success $data")
            }
            TOKEN_0X34 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X34 设备硬件增益 => success ${bytesToHex(response.content)}")
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
                val bleResponse = Pc300BleResponse.BleResponse(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)
                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }
        return bytesLeft
    }

    override fun dealReadFile(userId: String, fileName: String) {
    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

    override fun getInfo() {
        sendCmd(getVersion())
        sendCmd(getDeviceName())
        sendCmd(getDeviceId())
        sendCmd(getDeviceInfoFf2())
        sendCmd(getDeviceInfoFf4())
        sendCmd(getDeviceInfo51())
        sendCmd(getGlucometerType())
        sendCmd(getBpMode())
        sendCmd(getTempConfig())
    }

    override fun syncTime() {
        sendCmd(setTime())
        sendCmd(ecgDataDigit(2))
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }

    override fun getRtData() {
//        sendCmd(ecgRtState())
//        sendCmd(Pc300BleCmd.ecgDataDigit(1))
    }

    override fun getFileList() {
        setDeviceId(18)
        setBpMode(BpMode.ADULT_MODE)
        setBsUnit(BsUnit.MMOL_L)
        setGlucometerType(GlucometerType.ON_CALL_SURE_SYNC)
    }

    fun startEcg() {
        sendCmd(Pc300BleCmd.startEcg())
    }
    fun stopEcg() {
        sendCmd(Pc300BleCmd.stopEcg())
    }

}