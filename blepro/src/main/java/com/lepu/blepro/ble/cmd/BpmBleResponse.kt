package com.lepu.blepro.ble.cmd

import com.lepu.blepro.ble.data.BpmCmd
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.ByteUtils.bytes2UIntBig
import com.lepu.blepro.utils.bytesToHex

object BpmBleResponse {

    class RecordData(val bytes: ByteArray)  {
        var sys: Int
        var dia: Int
        var regularHrFlag: Boolean  // true：有心率不齐，false：无心率不齐
        var pr: Int
        var deviceUserId: Int       // 用户id
        var storeId: Int            // 数据序号
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        init {
            val data = BpmCmd(bytes).data
            sys = ((data[0].toUInt() and 0xFFu).toInt() shl 8) + (data[1].toUInt() and 0xFFu).toInt()
            dia = ((data[2].toUInt() and 0xFFu).toInt() shl 8) + (data[3].toUInt() and 0xFFu).toInt()
            regularHrFlag = (data[4].toUInt() and 0xFFu).toInt() != 0
            pr = (data[5].toUInt() and 0xFFu).toInt()
            deviceUserId = (data[6].toUInt() and 0xFFu).toInt()
            storeId = ((data[7].toUInt() and 0xFFu).toInt() shl 8) + (data[8].toUInt() and 0xFFu).toInt()
            year = (data[9].toUInt() and 0xFFu).toInt() + 2000
            month = (data[10].toUInt() and 0xFFu).toInt()
            day = (data[11].toUInt() and 0xFFu).toInt()
            hour = (data[12].toUInt() and 0xFFu).toInt()
            minute = (data[13].toUInt() and 0xFFu).toInt()
        }
        override fun toString(): String {
            return """
                RecordData : 
                bytes : ${bytesToHex(bytes)}
                sys : $sys
                dia : $dia
                regularHrFlag : $regularHrFlag
                pr : $pr
                deviceUserId : $deviceUserId
                storeId : $storeId
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
            """.trimIndent()
        }
    }

    class RtData(val bytes: ByteArray)  {
        var ps: Int
        init {
            ps = bytes2UIntBig(bytes[4], bytes[5])
        }
        override fun toString(): String {
            return """
            RtData : 
            bytes : ${bytesToHex(bytes)}
            ps : $ps
        """.trimIndent()
        }
    }

    class RtState(val bytes: ByteArray)  {
        var state: Int           // 设备状态 0：时间设置状态，1：历史界面状态，2：测量状态，3：测量加压状态，
                                 //         4：泄气心率闪烁状态，5：测量结束状态，6：待机界面/时间界面
        var stateMessZh: String
        var stateMessEn: String
        init {
            val data = BpmCmd(bytes).data
            state = ByteUtils.byte2UInt(data[0])
            stateMessZh = getStateMessZh(state)
            stateMessEn = getStateMessEn(state)
        }
        private fun getStateMessZh(state: Int): String {
            return when(state) {
                0 -> "时间设置状态"
                1 -> "历史界面状态"
                2 -> "测量状态"
                3 -> "测量加压状态"
                4 -> "泄气心率闪烁状态"
                5 -> "测量结束状态"
                6 -> "待机界面/时间界面"
                else -> ""
            }
        }
        private fun getStateMessEn(state: Int): String {
            return when(state) {
                0 -> "Time setting state"
                1 -> "Historical interface status"
                2 -> "Measurement status "
                3 -> "Measuring the pressurized state"
                4 -> "The flickering indication of the heart rate in deflating mode"
                5 -> "Measurement end state"
                6 -> "Standby interface/time interface"
                else -> ""
            }
        }
        override fun toString(): String {
            return """
                RtState : 
                bytes : ${bytesToHex(bytes)}
                state : $state
                stateMessZh : $stateMessZh
                stateMessEn : $stateMessEn
            """.trimIndent()
        }
    }

    class ErrorResult(val bytes: ByteArray)  {
        var result: Int           // 测量错误结果 1：传感器震荡异常，2：检测不到足够的心跳或算不出血压，3：测量结果异常，4：袖带过松或漏气(10 秒内加压不到 30mmHg)，
                                  //            5：气管被堵住，6：测量时压力波动大，7：压力超过上限，8：标定数据异常或未标定
        var resultMessZh: String
        var resultMessEn: String
        init {
            val data = BpmCmd(bytes).data
            result = ByteUtils.byte2UInt(data[5])
            resultMessZh = getResultMessZh(result)
            resultMessEn = getResultMessEn(result)
        }
        private fun getResultMessZh(code: Int): String {
            return when(code) {
                1 -> "传感器震荡异常"
                2 -> "检测不到足够的心跳或算不出血压"
                3 -> "测量结果异常"
                4 -> "袖带过松或漏气(10 秒内加压不到 30mmHg)"
                5 -> "气管被堵住"
                6 -> "测量时压力波动大"
                7 -> "压力超过上限"
                8 -> "标定数据异常或未标定"
                else -> ""
            }
        }
        private fun getResultMessEn(code: Int): String {
            return when(code) {
                1 -> "Sensor vibration anomaly"
                2 -> "Not enough heart rate to be detected or blood pressure value to be calculated"
                3 -> "Measurement results are abnormal"
                4 -> "Cuff is too loose or air leakage(Pressure value less than 30mmHg in 10 seconds)"
                5 -> "The tube is blocked"
                6 -> "Large pressure fluctuations during measurement"
                7 -> "Pressure exceeds upper limit"
                8 -> "Calibration data is abnormal or uncalibrated"
                else -> ""
            }
        }
        override fun toString(): String {
            return """
                ErrorResult : 
                bytes : ${bytesToHex(bytes)}
                result : $result
                resultMessZh : $resultMessZh
                resultMessEn : $resultMessEn
            """.trimIndent()
        }
    }

}