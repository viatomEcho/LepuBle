package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.icomon.icbodyfatalgorithms.ICBodyFatAlgorithms
import com.icomon.icbodyfatalgorithms.ICBodyFatAlgorithmsParams
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.FscaleUserInfo
import com.lepu.blepro.ble.data.FscaleWeightData
import com.lepu.blepro.utils.*

/**
 *
 * 蓝牙操作
 */

class F5ScaleBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "F5ScaleBleInterface"

    private var userInfo = FscaleUserInfo()
    private var weightData = FscaleWeightData()

    private var receivedCount = 0

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = F5ScaleBleManager(context)
        manager?.let {
            it.isUpdater = isUpdater
            it.setConnectionObserver(this)
            it.notifyListener = this
            it.connect(device)
                .useAutoConnect(false)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "manager.connect done")
                }
                .enqueue()
        } ?: kotlin.run {
            LepuBleLog.d(tag, "manager == null")
        }
    }

    override fun dealReadFile(userId: String, fileName: String) {

    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: F5ScaleBleResponse.F5ScaleResponse) {
        LepuBleLog.d(tag, "received cmd : " + bytesToHex(response.bytes))

        when(response.cmd) {
            F5ScaleBleCmd.WEIGHT_DATA -> {
                LepuBleLog.d(tag, "model:$model,WEIGHT_DATA => success")
                var info = F5ScaleBleResponse.WeightData(response.content)
                LepuBleLog.d(tag, "model:$model,WEIGHT_DATA => info.toString() == $info")

                weightData.weight_g = info.weightG
                weightData.weight_kg = info.weightKG
                weightData.weight_lb = info.weightLB
                weightData.weight_st = info.weightST
                weightData.weight_st_lb = info.weightSTLB

                receivedCount = 0

            }
            F5ScaleBleCmd.IMPEDANCE_DATA -> {
                LepuBleLog.d(tag, "model:$model,IMPEDANCE_DATA => success")
                var info = F5ScaleBleResponse.ImpedanceData(response.content)
                LepuBleLog.d(tag, "model:$model,IMPEDANCE_DATA => info.toString() == $info")
                weightData.time = System.currentTimeMillis() / 1000

                // 算法库计算
                var params = ICBodyFatAlgorithmsParams()
                params.weight = weightData.weight_kg
                params.height = userInfo.height
                params.sex = userInfo.sex
                params.age = userInfo.age
                params.algType = userInfo.bfaType
                params.peopleType = userInfo.peopleType
                params.imp1 = info.imp
                val result = ICBodyFatAlgorithms.calc(params)
                weightData.bmi = result.bmi
                weightData.bodyFatPercent = result.bfr
                weightData.subcutaneousFatPercent = result.subcutfat
                weightData.visceralFat = result.vfal
                weightData.musclePercent = result.muscle
                weightData.bmr = result.bmr
                weightData.boneMass = result.bone
                weightData.moisturePercent = result.water
                weightData.physicalAge = result.age.toDouble()
                weightData.proteinPercent = result.protein
                weightData.smPercent = result.sm
                weightData.bodyScore = result.bodyScore
                weightData.bodyType = result.bodyType
                weightData.targetWeight = result.weightTarget
                receivedCount++
                LepuBleLog.d(tag, "model:$model,IMPEDANCE_DATA => receivedCount == $receivedCount")
                if (receivedCount == 1) {
                    LepuBleLog.d(tag, "model:$model,IMPEDANCE_DATA => info.toString() == $weightData")
                }
            }
            F5ScaleBleCmd.UNSTABLE_DATA -> {
                LepuBleLog.d(tag, "model:$model,UNSTABLE_DATA => success")
                var info = F5ScaleBleResponse.StableData(response.content)
                LepuBleLog.d(tag, "model:$model,UNSTABLE_DATA => info.toString() == $info")
            }
            F5ScaleBleCmd.OTHER_DATA -> {
                LepuBleLog.d(tag, "model:$model,OTHER_DATA => success")
                var info = F5ScaleBleResponse.HrData(response.content)
                LepuBleLog.d(tag, "model:$model,OTHER_DATA => info.toString() == $info")

                weightData.hr = info.hr

            }
            F5ScaleBleCmd.HISTORY_DATA -> {
                LepuBleLog.d(tag, "model:$model,HISTORY_DATA => success")
                var info = F5ScaleBleResponse.HistoryData(response.content)
                LepuBleLog.d(tag, "model:$model,HISTORY_DATA => info.toString() == $info")

                weightData.weight_g = info.weightData.weightG
                weightData.weight_kg = info.weightData.weightKG
                weightData.weight_lb = info.weightData.weightLB
                weightData.weight_st = info.weightData.weightST
                weightData.weight_st_lb = info.weightData.weightSTLB

                weightData.time = info.time
                weightData.hr = info.hr

                LepuBleLog.d(tag, "model:$model,HISTORY_DATA => info.toString() == $weightData")

            }
        }

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 4) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-3) {
            if (bytes[i] != 0xAC.toByte() || bytes[i+1] != 0x27.toByte()) {
                continue@loop
            }

            val len = 16
            if (i+4+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)

            val crc = (temp.last().toUInt() and 0x1Fu).toInt()

            if (crc == CrcUtil.calF5ScaleCHK(temp)) {
                val bleResponse = F5ScaleBleResponse.F5ScaleResponse(temp)
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
    }

    override fun syncTime() {

    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
    }

    /**
     * get file list
     */
    override fun getFileList() {
    }

    fun setUserInfo(userInfo: FscaleUserInfo) {
        this.userInfo = userInfo
        sendCmd(F5ScaleBleCmd.setUserInfo(0, userInfo.userIndex, userInfo.height, (userInfo.weight*100).toInt(), userInfo.age, userInfo.sex.value))
    }
    fun setUserList(userList: List<FscaleUserInfo>) {
        for (userInfo in userList) {
            sendCmd(F5ScaleBleCmd.setUserList(userInfo.userIndex, userInfo.height, (userInfo.weight*100).toInt(), userInfo.age, userInfo.sex.value))
        }
    }

}