package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.bytesToSignedShort
import com.lepu.blepro.utils.toUInt

class Th12BleFile(val fileName: String) {

    private val leadNameTable = arrayOf("NULL", "I", "II", "NULL", "NULL", "NULL", "NULL",
        "V1", "V2", "V3", "V4", "V5", "V6", "Pacer")

    private var headDataLen: Int = 2901              // 头文件长度
    private var samplingRate: Int = 250              // 采样率
    private var range: Int = 50000                   // 电压范围
    private var precision: Int = 32768               // 电压数值
    private var adcGain: Float = 655.36f             // adc增益
    private var leadNum: Int = 9                     // 导联数量
    private var samplingNum: Int = 0                 // 每导联采样点数
    private var validLength: Int = 0                 // 文件有效长度

    private lateinit var leadNames: Array<String?>   // 导联名称

    private lateinit var year: String
    private lateinit var month: String
    private lateinit var day: String

    private lateinit var hour: String
    private lateinit var minute: String
    private lateinit var second: String

    private lateinit var sumCrcs: Array<Short?>
    private lateinit var initDatas: Array<Short?>

    private var index = 0

    fun parseHeadData(byteArray: ByteArray) {
        if (byteArray.size < headDataLen) return
        samplingRate = toUInt(byteArray.copyOfRange(17, 19))
        range = toUInt(byteArray.copyOfRange(19, 23))
        precision = toUInt(byteArray.copyOfRange(23, 27))
        adcGain = precision.div(range.toFloat()).times(1000)
        leadNum = byteArray[29].toInt()

        var leadNameByteArray = byteArray.copyOfRange(30, 30+leadNum)
        leadNames = arrayOfNulls(leadNum)
        for (i in leadNames.indices) {
            leadNames[i] = leadNameTable[leadNameByteArray[i].toInt()]
        }

        sumCrcs = arrayOfNulls(leadNum)
        initDatas = arrayOfNulls(leadNum)

        validLength = toUInt(byteArray.copyOfRange(49, 53))

        year = bytesToHex(byteArray.copyOfRange(53, 55))
        month = bytesToHex(byteArrayOf(byteArray[55]))
        day = bytesToHex(byteArrayOf(byteArray[56]))

        hour = bytesToHex(byteArrayOf(byteArray[57]))
        minute = bytesToHex(byteArrayOf(byteArray[58]))
        second = bytesToHex(byteArrayOf(byteArray[59]))
    }

    fun getValidLength(): Int {
        return validLength - headDataLen
    }

    fun getFileCreateTime(): String {
        return "$year-$month-$day $hour:$minute:$second"
    }

    fun getEcgTime(): Int {
        return samplingNum/samplingRate
    }

    fun getTwoSecondEcgData(byteArray: ByteArray): ByteArray {
        var ecgData = byteArray.copyOfRange(8, byteArray.size)
        if (index == 0) {
            for (i in 0 until leadNum) {
                initDatas[i] = bytesToSignedShort(ecgData[i*2], ecgData[i*2+1])
                sumCrcs[i] = 0
            }
        }

        for (i in sumCrcs.indices) {
            for (j in 0 until (ecgData.size/2/leadNum))
                sumCrcs[i] =
                    sumCrcs[i]?.plus(bytesToSignedShort(ecgData[j*leadNum*2+i*2], ecgData[j*leadNum*2+i*2+1]))?.toShort()
        }

        index++
        samplingNum = (validLength-headDataLen-index*8)/2/leadNum
        return ecgData
    }

    fun getTwoSecondLeadData(leadName: String, ecgData: ByteArray): ShortArray {
        var index = 0
        when(leadName) {
            "I" -> index = 0
            "II" -> index = 1
            "V1" -> index = 2
            "V2" -> index = 3
            "V3" -> index = 4
            "V4" -> index = 5
            "V5" -> index = 6
            "V6" -> index = 7
            "Pacer" -> index = 8
            "III" -> index = 9
            "aVR" -> index = 10
            "aVL" -> index = 11
            "aVF" -> index = 12
        }
//        var ecgData = byteArray.copyOfRange(8, byteArray.size)
        var leadData = ShortArray(ecgData.size/2/leadNum)
        var leadDataI = ShortArray(ecgData.size/2/leadNum)
        var leadDataII = ShortArray(ecgData.size/2/leadNum)

        if (index < 9) {
            for (i in leadData.indices) {
                leadData[i] = bytesToSignedShort(ecgData[index*2+leadNum*2*i], ecgData[index*2+leadNum*2*i+1])
            }
        } else {
            for (i in leadData.indices) {
                leadDataI[i] = bytesToSignedShort(ecgData[leadNum*2*i], ecgData[leadNum*2*i+1])
                leadDataII[i] = bytesToSignedShort(ecgData[2+leadNum*2*i], ecgData[2+leadNum*2*i+1])
            }
            when(leadName) {
                "III" -> { // III = II - I
                    for (i in leadData.indices) {
                        leadData[i] = (leadDataII[i] - leadDataI[i]).toShort()
                    }
                }
                "aVR" -> { // aVR = -(II + I)/2
                    for (i in leadData.indices) {
                        leadData[i] = (-(leadDataII[i] + leadDataI[i])/2).toShort()
                    }
                }
                "aVL" -> { // aVL = I - II/2
                    for (i in leadData.indices) {
                        leadData[i] = (leadDataI[i] - leadDataII[i]/2).toShort()
                    }
                }
                "aVF" -> { // aVF = II - I/2
                    for (i in leadData.indices) {
                        leadData[i] = (leadDataII[i] - leadDataI[i]/2).toShort()
                    }
                }
            }
        }
        return leadData
    }

    fun getMitHeadData(): Array<String?> {
        val date = "$day/$month/$year"
        val time = "$hour:$minute:$second"
        var headData = arrayOfNulls<String>(leadNum+1)
        headData[0] = "$fileName $leadNum $samplingRate $samplingNum $time $date"
        for (i in 1 until headData.size) {
            headData[i] = fileName + ".dat 16 " + adcGain + " 16 0 " + initDatas[i-1] + " " + sumCrcs[i-1] + " 0 " + leadNames[i-1]
        }
        return headData
    }
}