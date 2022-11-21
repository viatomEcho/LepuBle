package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.Er3Decompress
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt

object LepodBleResponse {

    class BleResponse(val bytes: ByteArray) {
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = byte2UInt(bytes[1])
            pkgType = bytes[3]
            pkgNo = byte2UInt(bytes[4])
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7 + len)
        }
    }

    class RtData(val bytes: ByteArray) {
        var param: RtParam
        var wave: RtWave

        init {
            param = RtParam(bytes.copyOfRange(0, 40))
            wave = RtWave(param.leadType, bytes.copyOfRange(40, bytes.size))
        }
    }

    class RtParam(val bytes: ByteArray) {
        var measureStatus: Int            // 测量状态（0：空闲，1：准备状态，2：正式测量状态）
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var recordTime: Int               // 已记录时长（单位:second）
        var batteryStatus: Int            // 电池状态（0：正常使用，1：充电中，2：充满，3：低电量）
        var battery: Int                  // 电池电量（e.g.100:100%）
        // reserved 6 byte
        var isInsertEcgLeadWire: Boolean  // 心电导联线状态（false：未插入导联线，true：插入导联线）
        var leadType: Int                 // 导联类型（0：LEAD_12，12导，1：LEAD_6，6导，2：LEAD_5，5导，3：LEAD_3，3导，4：LEAD_3_TEMP，3导带体温，
                                          // 5：LEAD_3_LEG，3导胸贴，6：LEAD_5_LEG，5导胸贴，7：LEAD_6_LEG，6导胸贴，0XFF：LEAD_NONSUP，不支持的导联）
        var isLeadOffRA: Boolean
        var isLeadOffLA: Boolean
        var isLeadOffLL: Boolean
        var isLeadOffRL: Boolean
        var isLeadOffV1: Boolean
        var isLeadOffV2: Boolean
        var isLeadOffV3: Boolean
        var isLeadOffV4: Boolean
        var isLeadOffV5: Boolean
        var isLeadOffV6: Boolean
        var hr: Int                       // 当前主机实时心率（bpm）
        var ecgRFlag: Boolean             // 实时运行标记 bit0:R波标记
        var respRate: Int                 // 呼吸率
        var oxyStatus: Int                // 血氧状态（0：未接入血氧，1：血氧状态正常，2：血氧手指脱落，3：探头故障）
        var spo2: Int                     // 血氧（无效值0xFF）
        var pr: Int                       // 脉率（30~250bpm）
        var pi: Float                     // 0- 200，e.g.25 : PI = 2.5
        var oxyRFlag: Boolean             // 实时运行标记 bit0:R波标记
        // reserved 2 byte
        var isInsertTemp: Boolean         // 体温状态（0：未接入体温，1：体温状态正常）
        var temp: Float                   // 体温（无效值0xFFFF，e.g.2500，temp = 25.0℃）
        // reserved 1 byte

        init {
            var index = 0
            measureStatus = byte2UInt(bytes[index])
            index++
            year = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            month = byte2UInt(bytes[index])
            index++
            day = byte2UInt(bytes[index])
            index++
            hour = byte2UInt(bytes[index])
            index++
            minute = byte2UInt(bytes[index])
            index++
            second = byte2UInt(bytes[index])
            index++
            recordTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            batteryStatus = byte2UInt(bytes[index])
            index++
            battery = byte2UInt(bytes[index])
            index++
            index += 6
            isInsertEcgLeadWire = byte2UInt(bytes[index]) == 1
            index++
            leadType = byte2UInt(bytes[index])
            index++
            isLeadOffRA = (byte2UInt(bytes[index]) and 0x01) == 1
            isLeadOffLA = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
            isLeadOffLL = ((byte2UInt(bytes[index]) and 0x04) shr 2) == 1
            isLeadOffRL = ((byte2UInt(bytes[index]) and 0x08) shr 3) == 1
            isLeadOffV1 = ((byte2UInt(bytes[index]) and 0x10) shr 4) == 1
            isLeadOffV2 = ((byte2UInt(bytes[index]) and 0x20) shr 5) == 1
            isLeadOffV3 = ((byte2UInt(bytes[index]) and 0x40) shr 6) == 1
            isLeadOffV4 = ((byte2UInt(bytes[index]) and 0x80) shr 7) == 1
            index++
            isLeadOffV5 = (byte2UInt(bytes[index]) and 0x01) == 1
            isLeadOffV6 = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
            index++
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            ecgRFlag = (byte2UInt(bytes[index]) and 0x01) == 1
            index++
            respRate = byte2UInt(bytes[index])
            index++
            oxyStatus = byte2UInt(bytes[index])
            index++
            spo2 = byte2UInt(bytes[index])
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            oxyRFlag = (byte2UInt(bytes[index]) and 0x01) == 1
            index++
            index += 2
            isInsertTemp = byte2UInt(bytes[index]) == 1
            index++
            temp = toUInt(bytes.copyOfRange(index, index+2)) / 100.toFloat()
        }

        override fun toString(): String {
            return """
                RtParam : 
                measureStatus : $measureStatus
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                recordTime : $recordTime
                batteryStatus : $batteryStatus
                battery : $battery
                isInsertEcgLeadWire : $isInsertEcgLeadWire
                leadType : $leadType
                isLeadOffRA : $isLeadOffRA
                isLeadOffLA : $isLeadOffLA
                isLeadOffLL : $isLeadOffLL
                isLeadOffRL : $isLeadOffRL
                isLeadOffV1 : $isLeadOffV1
                isLeadOffV2 : $isLeadOffV2
                isLeadOffV3 : $isLeadOffV3
                isLeadOffV4 : $isLeadOffV4
                isLeadOffV5 : $isLeadOffV5
                isLeadOffV6 : $isLeadOffV6
                hr : $hr
                ecgRFlag : $ecgRFlag
                respRate : $respRate
                oxyStatus : $oxyStatus
                spo2 : $spo2
                pr : $pr
                pi : $pi
                oxyRFlag : $oxyRFlag
                isInsertTemp : $isInsertTemp
                temp : $temp
            """.trimIndent()
        }
    }

    class RtWave(val leadType: Int, val bytes: ByteArray) {
        var samplingRate: Int             // bit0~bit3:采样率0::250Hz 1:125Hz 2:62.5Hz
        var compressType: Int             // bit4~bit7: 压缩类型 0:未压缩 1:Viatom差分压缩
        // reserved 3 byte
        var firstIndex: Int               // 数据的第一个点
        var len: Int                      // 采样点数
        var wave = ByteArray(0)      // 压缩原始数据
        var waveMvs = FloatArray(0)  // 解压后毫伏值数据，n * 0.00244140625

        init {
            var index = 0
            samplingRate = byte2UInt(bytes[index]) and 0x0f
            compressType = (byte2UInt(bytes[index]) and 0xf0) shr 4
            index++
            index += 3
            firstIndex = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            len = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            if (len > 0) {
                wave = bytes.copyOfRange(index, bytes.size)
                waveMvs = getMvsFromWaveBytes(wave, leadType)
            }
        }

        override fun toString(): String {
            return """
                RtWave : 
                compressType : $compressType
                firstIndex : $firstIndex
                len : $len
            """.trimIndent()
        }
    }

    /**
     * 实时原始波形数据和导联类型，获取8导解压后的float型采样点毫伏值顺序数组
     * V6 I II V1 V2 V3 V4 V5
     */
    fun getMvsFromWaveBytes(wave: ByteArray, leadType: Int) : FloatArray {
        val waveInts = getShortsFromWaveBytes(wave, leadType)
        val waveMvs = mutableListOf<Float>()
        for (i in waveInts) {
            waveMvs.add(i * 0.00244140625f)
        }
        return waveMvs.toFloatArray()
    }
    /**
     * 实时原始波形数据和导联类型，获取8导解压后的short型采样点毫伏值顺序数组
     * V6 I II V1 V2 V3 V4 V5
     */
    fun getShortsFromWaveBytes(wave: ByteArray, leadType: Int) : ShortArray {
        // decompress
        val num = if (leadType == 0) {
            8
        } else {
            4
        }
        val decompress = Er3Decompress(num)
        val decompressData = mutableListOf<Short>()
        for (b in wave) {
            val tmp = decompress.Decompress(b)
            if (tmp != null) {
                for (i in tmp) {
                    if (i == 32767) {  // 导联脱落，基线处理
                        decompressData.add(0)
                    } else {
                        decompressData.add(i.toShort())
                    }
                }
            }
        }

        val oriShorts = decompressData.toShortArray()
        var waveShorts = ShortArray(0)
        val tmpFs = mutableListOf<Short>()

        when(leadType) {
            0 -> {  // LEAD_12
                val lead_size = 8
                waveShorts = oriShorts
            }
            1, 7 -> {  // LEAD_6, LEAD_6_LEG
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriShorts[i+1])
                    tmpFs.add(oriShorts[i+2])
                    tmpFs.add(oriShorts[i+3])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriShorts[i])
                }
                waveShorts = tmpFs.toShortArray()
            }
            2 -> {  // LEAD_5
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriShorts[i+1])
                    tmpFs.add(oriShorts[i+2])
                    tmpFs.add(oriShorts[i+3])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveShorts = tmpFs.toShortArray()
            }
            3 -> {  // LEAD_3
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriShorts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveShorts = tmpFs.toShortArray()
            }
            4 -> {  // LEAD_3_TEMP
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriShorts[i+1])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveShorts = tmpFs.toShortArray()
            }
            5 -> {  // LEAD_3_LEG
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriShorts[i+1])
                    tmpFs.add(oriShorts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveShorts = tmpFs.toShortArray()
            }
            6 -> {  // LEAD_5_LEG
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriShorts[i+1])
                    tmpFs.add(oriShorts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriShorts[i])
                }
                waveShorts = tmpFs.toShortArray()
            }
        }
        return waveShorts
    }

    /**
     * 采集数据处理：实时原始波形数据和导联类型，获取每导解压后的short型采样点数组
     * V6 / I / II / V1 / V2 / V3 / V4 / V5 / III / aVR / aVL / aVF
     */
    fun getEachLeadShortsFromWaveBytes(wave: ByteArray, leadType: Int, leadName: String) : ShortArray {
        val waveShorts = getShortsFromWaveBytes(wave, leadType)
        val tempShorts = mutableListOf<Short>()
        when (leadName) {
            "V6" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add(waveShorts[i])
                }
            }
            "I" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add(waveShorts[i+1])
                }
            }
            "II" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add(waveShorts[i+2])
                }
            }
            "V1" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add(waveShorts[i+3])
                }
            }
            "V2" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add(waveShorts[i+4])
                }
            }
            "V3" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add(waveShorts[i+5])
                }
            }
            "V4" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add(waveShorts[i+6])
                }
            }
            "V5" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add(waveShorts[i+7])
                }
            }
            "III" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add((waveShorts[i+2] - waveShorts[i+1]).toShort())
                }
            }
            "aVR" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add((-(waveShorts[i+1] + waveShorts[i+2]).div(2)).toShort())
                }
            }
            "aVL" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add((waveShorts[i+1] - waveShorts[i+2].div(2)).toShort())
                }
            }
            "aVF" -> {
                for (i in waveShorts.indices step 8) {
                    tempShorts.add((waveShorts[i+2] - waveShorts[i+1].div(2)).toShort())
                }
            }
        }
        return tempShorts.toShortArray()
    }
    /**
     * 采集数据处理：实时原始波形数据和导联类型，获取12导解压后的short型采样点数组
     * V6 I II V1 V2 V3 V4 V5 III aVR aVL aVF
     */
    fun getAllLeadShortsFromWaveBytes(wave: ByteArray, leadType: Int) : MutableList<ShortArray> {
        // decompress
        val num = if (leadType == 0) {
            8
        } else {
            4
        }
        val decompress = Er3Decompress(num)
        val decompressData = mutableListOf<Short>()
        for (b in wave) {
            val tmp = decompress.Decompress(b)
            if (tmp != null) {
                for (i in tmp) {
                    if (i == 32767) {  // 导联脱落，基线处理
                        decompressData.add(0)
                    } else {
                        decompressData.add(i.toShort())
                    }
                }
            }
        }

        val oriShorts = decompressData.toShortArray()
        val waveShorts = mutableListOf<ShortArray>()
        val tmpV6 = mutableListOf<Short>()
        val tmpI = mutableListOf<Short>()
        val tmpII = mutableListOf<Short>()
        val tmpV1 = mutableListOf<Short>()
        val tmpV2 = mutableListOf<Short>()
        val tmpV3 = mutableListOf<Short>()
        val tmpV4 = mutableListOf<Short>()
        val tmpV5 = mutableListOf<Short>()
        val tmpIII = mutableListOf<Short>()
        val tmpaVR = mutableListOf<Short>()
        val tmpaVL = mutableListOf<Short>()
        val tmpaVF = mutableListOf<Short>()
        // III = II-I
        // aVR = - (I+II)/2
        // aVL = I - II/2
        // aVF = II - I/2
        when(leadType) {
            0 -> {  // LEAD_12
                val lead_size = 8
                for (i in oriShorts.indices step lead_size) {
                    tmpV6.add(oriShorts[i])
                    tmpI.add(oriShorts[i+1])
                    tmpII.add(oriShorts[i+2])
                    tmpV1.add(oriShorts[i+3])
                    tmpV2.add(oriShorts[i+4])
                    tmpV3.add(oriShorts[i+5])
                    tmpV4.add(oriShorts[i+6])
                    tmpV5.add(oriShorts[i+7])
                    tmpIII.add((oriShorts[i+2]-oriShorts[i+1]).toShort())
                    tmpaVR.add((-(oriShorts[i+1]+oriShorts[i+2]).div(2)).toShort())
                    tmpaVL.add((oriShorts[i+1]-oriShorts[i+2].div(2)).toShort())
                    tmpaVF.add((oriShorts[i+2]-oriShorts[i+1].div(2)).toShort())
                }
            }
            1, 7 -> {  // LEAD_6, LEAD_6_LEG
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpV6.add(0)
                    tmpI.add(oriShorts[i+1])
                    tmpII.add(oriShorts[i+2])
                    tmpV1.add(oriShorts[i+3])
                    tmpV2.add(0)
                    tmpV3.add(0)
                    tmpV4.add(0)
                    tmpV5.add(oriShorts[i])
                    tmpIII.add((oriShorts[i+2]-oriShorts[i+1]).toShort())
                    tmpaVR.add((-(oriShorts[i+1]+oriShorts[i+2]).div(2)).toShort())
                    tmpaVL.add((oriShorts[i+1]-oriShorts[i+2].div(2)).toShort())
                    tmpaVF.add((oriShorts[i+2]-oriShorts[i+1].div(2)).toShort())
                }
            }
            2 -> {  // LEAD_5
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpV6.add(0)
                    tmpI.add(oriShorts[i+1])
                    tmpII.add(oriShorts[i+2])
                    tmpV1.add(oriShorts[i+3])
                    tmpV2.add(0)
                    tmpV3.add(0)
                    tmpV4.add(0)
                    tmpV5.add(0)
                    tmpIII.add((oriShorts[i+2]-oriShorts[i+1]).toShort())
                    tmpaVR.add((-(oriShorts[i+1]+oriShorts[i+2]).div(2)).toShort())
                    tmpaVL.add((oriShorts[i+1]-oriShorts[i+2].div(2)).toShort())
                    tmpaVF.add((oriShorts[i+2]-oriShorts[i+1].div(2)).toShort())
                }
            }
            3 -> {  // LEAD_3
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpV6.add(0)
                    tmpI.add(0)
                    tmpII.add(oriShorts[i+2])
                    tmpV1.add(0)
                    tmpV2.add(0)
                    tmpV3.add(0)
                    tmpV4.add(0)
                    tmpV5.add(0)
                    tmpIII.add((oriShorts[i+2]-oriShorts[i+1]).toShort())
                    tmpaVR.add((-(oriShorts[i+1]+oriShorts[i+2]).div(2)).toShort())
                    tmpaVL.add((oriShorts[i+1]-oriShorts[i+2].div(2)).toShort())
                    tmpaVF.add((oriShorts[i+2]-oriShorts[i+1].div(2)).toShort())
                }
            }
            4 -> {  // LEAD_3_TEMP
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpV6.add(0)
                    tmpI.add(oriShorts[i+1])
                    tmpII.add(0)
                    tmpV1.add(0)
                    tmpV2.add(0)
                    tmpV3.add(0)
                    tmpV4.add(0)
                    tmpV5.add(0)
                    tmpIII.add((oriShorts[i+2]-oriShorts[i+1]).toShort())
                    tmpaVR.add((-(oriShorts[i+1]+oriShorts[i+2]).div(2)).toShort())
                    tmpaVL.add((oriShorts[i+1]-oriShorts[i+2].div(2)).toShort())
                    tmpaVF.add((oriShorts[i+2]-oriShorts[i+1].div(2)).toShort())
                }
            }
            5 -> {  // LEAD_3_LEG
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpV6.add(0)
                    tmpI.add(oriShorts[i+1])
                    tmpII.add(oriShorts[i+2])
                    tmpV1.add(0)
                    tmpV2.add(0)
                    tmpV3.add(0)
                    tmpV4.add(0)
                    tmpV5.add(0)
                    tmpIII.add((oriShorts[i+2]-oriShorts[i+1]).toShort())
                    tmpaVR.add((-(oriShorts[i+1]+oriShorts[i+2]).div(2)).toShort())
                    tmpaVL.add((oriShorts[i+1]-oriShorts[i+2].div(2)).toShort())
                    tmpaVF.add((oriShorts[i+2]-oriShorts[i+1].div(2)).toShort())
                }
            }
            6 -> {  // LEAD_5_LEG
                val lead_size = 4
                for (i in oriShorts.indices step lead_size) {
                    tmpV6.add(0)
                    tmpI.add(oriShorts[i+1])
                    tmpII.add(oriShorts[i+2])
                    tmpV1.add(0)
                    tmpV2.add(0)
                    tmpV3.add(0)
                    tmpV4.add(0)
                    tmpV5.add(oriShorts[i])
                    tmpIII.add((oriShorts[i+2]-oriShorts[i+1]).toShort())
                    tmpaVR.add((-(oriShorts[i+1]+oriShorts[i+2]).div(2)).toShort())
                    tmpaVL.add((oriShorts[i+1]-oriShorts[i+2].div(2)).toShort())
                    tmpaVF.add((oriShorts[i+2]-oriShorts[i+1].div(2)).toShort())
                }
            }
        }

        waveShorts.add(tmpV6.toShortArray())
        waveShorts.add(tmpI.toShortArray())
        waveShorts.add(tmpII.toShortArray())
        waveShorts.add(tmpV1.toShortArray())
        waveShorts.add(tmpV2.toShortArray())
        waveShorts.add(tmpV3.toShortArray())
        waveShorts.add(tmpV4.toShortArray())
        waveShorts.add(tmpV5.toShortArray())
        waveShorts.add(tmpIII.toShortArray())
        waveShorts.add(tmpaVR.toShortArray())
        waveShorts.add(tmpaVL.toShortArray())
        waveShorts.add(tmpaVF.toShortArray())

        return waveShorts
    }

    /**
     * 文件处理：通过导联名称和解压后的采样点顺序数组，获取每一导联解压后的short采样点
     * V6 / I / II / V1 / V2 / V3 / V4 / V5 / III / aVR / aVL / aVF
     */
    fun getEachLeadDataShorts(leadName: String, waveInts: Array<String>) : MutableList<Short> {
        // V6 I II V1 V2 V3 V4 V5
        // III = II-I
        // aVR = - (I+II)/2
        // aVL = I - II/2
        // aVF = II - I/2
        val tempInts = mutableListOf<Short>()
        when (leadName) {
            "V6" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i].toInt().toShort())
                }
            }
            "I" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+1].toInt().toShort())
                }
            }
            "II" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+2].toInt().toShort())
                }
            }
            "V1" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+3].toInt().toShort())
                }
            }
            "V2" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+4].toInt().toShort())
                }
            }
            "V3" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+5].toInt().toShort())
                }
            }
            "V4" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+6].toInt().toShort())
                }
            }
            "V5" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+7].toInt().toShort())
                }
            }
            "III" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add((waveInts[i+2].toInt() - waveInts[i+1].toInt()).toShort())
                }
            }
            "aVR" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add((-(waveInts[i+1].toInt() + waveInts[i+2].toInt()).div(2)).toShort())
                }
            }
            "aVL" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add((waveInts[i+1].toInt() - waveInts[i+2].toInt().div(2)).toShort())
                }
            }
            "aVF" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add((waveInts[i+2].toInt() - waveInts[i+1].toInt().div(2)).toShort())
                }
            }
        }
        return tempInts
    }

    class BleFile(val model: Int, val fileName: String, val fileSize: Int, private val userId: String, var index: Int) {
        var content: ByteArray

        init {
            content = ByteArray(fileSize)
        }

        fun addContent(bytes: ByteArray) {
            if (index >= fileSize) {
                LepuBleLog.d("index >= fileSize index:$index, fileSize:$fileSize 已下载完成")
                return // 已下载完成
            } else {
                System.arraycopy(bytes, 0, content, index, bytes.size)
                DownloadHelper.writeFile(model, userId, fileName, "dat", bytes)

                index += bytes.size
            }
            LepuBleLog.d("er3File, bytes size = ${bytes.size}, index = $index")
        }
    }

    class FileList(val bytes: ByteArray) {
        var size: Int
        // Txxxxxxxxxxxxxx：心率、体温、血氧等数据的存储文件
        // Wxxxxxxxxxxxxxx：心电波形存储文件
        var fileList = mutableListOf<String>()

        init {
            var index = 0
            size = byte2UInt(bytes[index])
            index++
            for (i in  0 until size) {
                fileList.add(trimStr(String(bytes.copyOfRange(index + i * 16, index + (i+1) * 16))))
            }
        }

        override fun toString(): String {
            return """
                FileList : 
                size : $size
                fileList : $fileList
            """.trimIndent()
        }
    }
}