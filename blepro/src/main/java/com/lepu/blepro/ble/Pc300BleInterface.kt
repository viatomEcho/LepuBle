package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.Pc300BleCmd.*
import com.lepu.blepro.ble.cmd.*
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
                        LepuBleLog.d(tag, "model:$model,SET_DEVICE_ID => success $data")
                    }
                    GET_DEVICE_ID -> {
                        val data = toUInt(response.content)
                        LepuBleLog.d(tag, "model:$model,GET_DEVICE_ID => success $data")
                    }
                    GET_DEVICE_NAME -> {
                        val data = trimStr(toString(response.content))
                        LepuBleLog.d(tag, "model:$model,GET_DEVICE_NAME => success $data")
                    }
                    DEVICE_INFO_2 -> {
                        val data = Pc300BleResponse.DeviceInfo(response.content)
                        LepuBleLog.d(tag, "model:$model,DEVICE_INFO_2 => success $data")
                    }
                    DEVICE_INFO_4 -> {
                        val data = Pc300BleResponse.DeviceInfo(response.content)
                        LepuBleLog.d(tag, "model:$model,DEVICE_INFO_4 => success $data")
                    }
                    SET_TIME -> {
                        LepuBleLog.d(tag, "model:$model,SET_TIME => success ${bytesToHex(response.content)}")
                    }
                }
            }
            TOKEN_0XD0 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XD0 => success ${bytesToHex(response.content)}")
            }
            TOKEN_0X40 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X40 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    BP_START -> {
                        LepuBleLog.d(tag, "model:$model,BP_START => success ${bytesToHex(response.content)}")
                    }
                    BP_STOP -> {
                        LepuBleLog.d(tag, "model:$model,BP_STOP => success ${bytesToHex(response.content)}")
                    }
                    BP_MODE -> {
                        val data = toUInt(response.content)
                        LepuBleLog.d(tag, "model:$model,BP_MODE => success $data")
                    }
                }
            }
            TOKEN_0X42 -> {
                val data = Pc300BleResponse.RtBpData(response.content)
                LepuBleLog.d(tag, "model:$model,TOKEN_0X42 => success $data")
            }
            TOKEN_0X43 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X43 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    BP_RESULT -> {
                        val data = Pc300BleResponse.BpResult(response.content)
                        LepuBleLog.d(tag, "model:$model,BP_RESULT => success $data")
                    }
                    BP_ERROR_RESULT -> {
                        val data = Pc300BleResponse.BpResultError(response.content)
                        LepuBleLog.d(tag, "model:$model,BP_ERROR_RESULT => success $data")
                    }
                }
            }
            TOKEN_0X51 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X51 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    OXY_RT_STATE -> {
                        val data = Pc300BleResponse.RtOxyState(response.content)
                        LepuBleLog.d(tag, "model:$model,OXY_RT_STATE => success $data")
                    }
                    DEVICE_INFO -> {
                        val data = Pc300BleResponse.DeviceInfo(response.content)
                        LepuBleLog.d(tag, "model:$model,DEVICE_INFO => success $data")
                    }
                }
            }
            TOKEN_0X52 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X52 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    DISABLE_WAVE -> {
                        LepuBleLog.d(tag, "model:$model,DISABLE_WAVE => success ${bytesToHex(response.content)}")
                    }
                    ENABLE_WAVE -> {
                        val data = response.content.copyOfRange(0, response.content.size).toList().asSequence().map { (it.toInt() and 0x7f).toByte() }.toList().toByteArray()
                        LepuBleLog.d(tag, "model:$model,ENABLE_WAVE => success ${bytesToHex(data)}")
                    }
                }
            }
            TOKEN_0X53 -> {
                val data = Pc300BleResponse.RtOxyParam(response.content)
                LepuBleLog.d(tag, "model:$model,TOKEN_0X53 => success $data")
            }
            TOKEN_0X70 -> {
                val data = (byte2UInt(response.content[0]) and 0x40) shr 5
                LepuBleLog.d(tag, "model:$model,TOKEN_0X70 => success $data")
            }
            TOKEN_0X72 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X72 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    TEMP_RESULT -> {
                        val data = Pc300BleResponse.TempResult(response.content)
                        LepuBleLog.d(tag, "model:$model,TEMP_RESULT => success $data")
                    }
                    SET_TEMP_CONFIG -> {
                        LepuBleLog.d(tag, "model:$model,SET_TEMP_CONFIG => success ${bytesToHex(response.content)}")
                    }
                    GET_TEMP_CONFIG -> {
                        LepuBleLog.d(tag, "model:$model,GET_TEMP_CONFIG => success ${bytesToHex(response.content)}")
                    }
                }
            }
            TOKEN_0X73 -> {
                val data = Pc300BleResponse.BsResult(response.content)
                LepuBleLog.d(tag, "model:$model,TOKEN_0X73 => success $data")
            }
            TOKEN_0XE0 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE0 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    BS_UNIT -> {
                        LepuBleLog.d(tag, "model:$model,BS_UNIT => success ${bytesToHex(response.content)}")
                    }
                }
            }
            TOKEN_0XE2 -> {
                val data = bytes2UIntBig(response.content[0], response.content[1])
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE2 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    GLU_RESULT -> {
                        LepuBleLog.d(tag, "model:$model,GLU_RESULT => success $data")
                    }
                    UA_RESULT -> {
                        LepuBleLog.d(tag, "model:$model,UA_RESULT => success $data")
                    }
                    CHOL_RESULT -> {
                        LepuBleLog.d(tag, "model:$model,CHOL_RESULT => success $data")
                    }
                }
            }
            TOKEN_0XE3 -> {
                val data = toUInt(response.content)
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE3 => success $data")
            }
            TOKEN_0XE4 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE4 => success ${bytesToHex(response.content)}")
            }
            TOKEN_0XE5 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0XE5 => success ${bytesToHex(response.content)}")
            }
            TOKEN_0X30 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X30 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    ECG_START -> {
                        LepuBleLog.d(tag, "model:$model,ECG_START => success ${bytesToHex(response.content)}")
                    }
                    ECG_STOP -> {
                        LepuBleLog.d(tag, "model:$model,ECG_STOP => success ${bytesToHex(response.content)}")
                    }
                    ECG_DATA_DIGIT -> {
                        LepuBleLog.d(tag, "model:$model,ECG_DATA_DIGIT => success ${bytesToHex(response.content)}")
                    }
                }
            }
            TOKEN_0X31 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X31 => success ${bytesToHex(response.content)}")
                when (response.type) {
                    GET_VERSION -> {
                        val data = Pc300BleResponse.DeviceInfo(response.content)
                        LepuBleLog.d(tag, "model:$model,GET_VERSION => success $data")
                    }
                    ECG_RT_STATE -> {
                        val data = Pc300BleResponse.RtEcgState(response.content)
                        LepuBleLog.d(tag, "model:$model,ECG_RT_STATE => success $data")
                    }
                }
            }
            TOKEN_0X32 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X32 => success ${bytesToHex(response.content)}")
            }
            TOKEN_0X33 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X33 => success ${bytesToHex(response.content)}")
            }
            TOKEN_0X34 -> {
                LepuBleLog.d(tag, "model:$model,TOKEN_0X34 => success ${bytesToHex(response.content)}")
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
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }

    override fun getRtData() {
        sendCmd(ecgRtState())
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