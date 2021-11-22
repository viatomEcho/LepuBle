package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.bytesToSignedShort
import com.lepu.blepro.utils.toUInt

class Th12BleFile(val fileName: String, val byteArray: ByteArray) {

    private val leadNameTable = arrayOf("NULL", "I", "II", "NULL", "NULL", "NULL", "NULL",
        "V1", "V2", "V3", "V4", "V5", "V6", "Pacer")

    var samplingRate: Int                // 采样率
    var range: Int                       // 电压范围
    var precision: Int                   // 电压数值
    var adcGain: Float                   // adc增益
    var leadNum: Int                     // 导联数量
    var samplingNum: Int                 // 每导联采样点数

    var leadNames: Array<String?>        // 导联名称

    var year: String
    var month: String
    var day: String

    var hour: String
    var minute: String
    var second: String

    var originalEcgData: ByteArray        // 原始心电数据
    var leadEcgData: Array<ByteArray?>    // 分组导联数据

    init {
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
        var validLength = toUInt(byteArray.copyOfRange(49, 53))

        year = bytesToHex(byteArray.copyOfRange(53, 55))
        month = bytesToHex(byteArrayOf(byteArray[55]))
        day = bytesToHex(byteArrayOf(byteArray[56]))

        hour = bytesToHex(byteArrayOf(byteArray[57]))
        minute = bytesToHex(byteArrayOf(byteArray[58]))
        second = bytesToHex(byteArrayOf(byteArray[59]))

        originalEcgData = getOriginalEcgData(byteArray.copyOfRange(2901, validLength))
        samplingNum = originalEcgData.size/2/leadNum
        leadEcgData = arrayOfNulls(leadNum)
        for (i in 0 until leadNum) {
            leadEcgData[i] = ByteArray(originalEcgData.size/leadNum)
            for (j in 0 until samplingNum) {
                leadEcgData[i]?.set(j*2, originalEcgData[j*2*leadNum + i*2])
                leadEcgData[i]?.set(j*2+1, originalEcgData[j*2*leadNum + i*2+1])
            }
        }
    }

    private fun getOriginalEcgData(byteArray: ByteArray): ByteArray {
        val interval = 8 + 500 * 2 * leadNum // 每2秒数据大小(数据头+导联数据)
        val headCount = byteArray.size/interval
        val ecgLength = byteArray.size - headCount*8 // 导联数据 = 数据内容 - 数据头
        var ecgByteArray = ByteArray(ecgLength)
        var headByteArray = ByteArray(headCount*8)
        for (i in 0 until headCount) {
            System.arraycopy(
                byteArray,
                i * interval,
                headByteArray,
                i * 8,
                8)
            if (i == (headCount - 1)) {
                System.arraycopy(
                    byteArray,
                    8 + i * interval,
                    ecgByteArray,
                    i * (interval - 8),
                    ecgLength - i * (interval - 8))
            } else {
                System.arraycopy(
                    byteArray,
                    8 + i * interval,
                    ecgByteArray,
                    i * (interval - 8),
                    interval - 8)
            }
        }
        return ecgByteArray
    }

    fun getEachLeadData(leadName: String): ShortArray {
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
        var leadData = ShortArray(originalEcgData.size/2/leadNum)
        if (index < 9) {
            for (i in leadData.indices) {
                leadData[i] = bytesToSignedShort(leadEcgData[index]!![i*2], leadEcgData[index]!![i*2+1])
            }
        } else {
            // 其余四导计算获得
            leadData = getOtherLeadData(leadName)
        }
        return leadData
    }

    private fun getOtherLeadData(leadName: String): ShortArray {
        var leadData = ShortArray(originalEcgData.size/2/leadNum)
        var leadDataI = ShortArray(originalEcgData.size/2/leadNum)
        var leadDataII = ShortArray(originalEcgData.size/2/leadNum)
        for (i in leadData.indices) {
            leadDataI[i] = bytesToSignedShort(leadEcgData[0]!![i*2], leadEcgData[0]!![i*2+1])
            leadDataII[i] = bytesToSignedShort(leadEcgData[1]!![i*2], leadEcgData[1]!![i*2+1])
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
        return leadData
    }

    fun getMitHeadData(): Array<String?> {
        val date = "$day/$month/$year"
        val time = "$hour:$minute:$second"
        var sumCrcs = arrayOfNulls<Short>(leadNames.size)
        for (i in sumCrcs.indices) {
            sumCrcs[i] = 0
            for (j in 0 until samplingNum) {
                sumCrcs[i] =
                    sumCrcs[i]?.plus(bytesToSignedShort(leadEcgData[i]!![j*2], leadEcgData[i]!![j*2+1]))?.toShort()
            }
        }
        var headData = arrayOfNulls<String>(leadNum+1)
        headData[0] = "$fileName $leadNum $samplingRate $samplingNum $time $date"
        for (i in 1 until headData.size) {
            headData[i] = fileName + " 16 " + adcGain + " 16 0 " + bytesToSignedShort(leadEcgData[i-1]!![0], leadEcgData[i-1]!![1]) + " " + sumCrcs[i-1] + " 0 " + leadNames[i-1]
        }
        return headData
    }

    override fun toString(): String {
        val string = """
            download file:
            samplingRate: $samplingRate
            range: $range
            precision: $precision
            leadNum: $leadNum
            leadNames: $leadNames
            year: $year
            month: $month
            day: $day
            hour: $hour
            minute: $minute
            second: $second
        """
        return string
    }
}