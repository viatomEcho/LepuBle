package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.Er3Decompress
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt

object Er3BleResponse {

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
            param = RtParam(bytes.copyOfRange(0, 48))
            wave = RtWave(param.leadType, bytes.copyOfRange(48, bytes.size))
        }
    }

    class RtParam(val bytes: ByteArray) {

        var hr: Int                       // 当前主机实时心率（bpm）
        var temp: Float                   // 体温（无效值0xFFFF，e.g.2500，temp = 25.0℃）
        var spo2: Int                     // 血氧（无效值0xFF）
        var pi: Float                     // 0- 200，e.g.25 : PI = 2.5
        var pr: Int                       // 脉率（30~250bpm）
        var respRate: Int                 // 呼吸率
        var batteryStatus: Int            // 电池状态（0：正常使用，1：充电中，2：充满，3：低电量）
        var isInsertEcgLeadWire: Boolean  // 心电导联线状态（false：未插入导联线，true：插入导联线）
        var oxyStatus: Int                // 血氧状态（0：未接入血氧，1：血氧状态正常，2：血氧手指脱落，3：探头故障）
        var isInsertTemp: Boolean         // 体温状态（0：未接入体温，1：体温状态正常）
        var measureStatus: Int            // 测量状态（0：空闲，1：准备状态，2：正式测量状态）
        var isHasDevice: Boolean          // 正式测量状态中配置的设备类型是否有设备，暂无效
        var isHasTemp: Boolean            // 正式测量状态中配置的设备类型是否有体温，暂无效
        var isHasOxy: Boolean             // 正式测量状态中配置的设备类型是否有血氧，暂无效
        var isHasRespRate: Boolean        // 正式测量状态中配置的设备类型是否有呼吸率，暂无效
        var battery: Int                  // 电池电量（e.g.100:100%）
        var recordTime: Int               // 已记录时长（单位:second）
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var second: Int
        var leadType: Int                 // 导联类型（0：LEAD_12，12导，1：LEAD_6，6导，2：LEAD_5，5导，3：LEAD_3，3导，4：LEAD_3_TEMP，3导带体温，
                                          // 5：LEAD_3_LEG，3导胸贴，6：LEAD_5_LEG，5导胸贴，7：LEAD_6_LEG，6导胸贴，0XFF：LEAD_NONSUP，不支持的导联）
        var leadSn: String                // 一次性导联的sn
        var isLeadOffI: Boolean
        var isLeadOffII: Boolean
        var isLeadOffIII: Boolean         // 暂无效
        var isLeadOffaVR: Boolean         // 暂无效
        var isLeadOffaVL: Boolean         // 暂无效
        var isLeadOffaVF: Boolean         // 暂无效
        var isLeadOffV1: Boolean
        var isLeadOffV2: Boolean
        var isLeadOffV3: Boolean
        var isLeadOffV4: Boolean
        var isLeadOffV5: Boolean
        var isLeadOffV6: Boolean
        // reserved 6 byte

        init {
            var index = 0
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            temp = toUInt(bytes.copyOfRange(index, index+2)) / 100.toFloat()
            index += 2
            spo2 = byte2UInt(bytes[index])
            index++
            pi = byte2UInt(bytes[index]).div(10f)
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            respRate = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            batteryStatus = byte2UInt(bytes[index]) and 0x03
            isInsertEcgLeadWire = ((byte2UInt(bytes[index]) and 0x04) shr 2) == 1
            oxyStatus = (byte2UInt(bytes[index]) and 0x18) shr 3
            isInsertTemp = ((byte2UInt(bytes[index]) and 0x60) shr 5) == 1
            measureStatus = (byte2UInt(bytes[index]) and 0x80) shr 7 + (byte2UInt(bytes[index+1]) and 0x01) shl 1
            isHasDevice = ((byte2UInt(bytes[index+1]) and 0x0E) shr 1) != 0
            isHasTemp = ((byte2UInt(bytes[index+1]) and 0x02) shr 1) == 1
            isHasOxy = ((byte2UInt(bytes[index+1]) and 0x04) shr 2) == 1
            isHasRespRate = ((byte2UInt(bytes[index+1]) and 0x08) shr 3) == 1
            index += 2
            battery = byte2UInt(bytes[index])
            index++
            recordTime = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
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
            leadType = byte2UInt(bytes[index])
            index++
            leadSn = String(bytes.copyOfRange(index, index+15))
            index += 15
            isLeadOffI = (byte2UInt(bytes[index]) and 0x01) == 1
            isLeadOffII = ((byte2UInt(bytes[index]) and 0x02) shr 1) == 1
            isLeadOffIII = ((byte2UInt(bytes[index]) and 0x04) shr 2) == 1
            isLeadOffaVR = ((byte2UInt(bytes[index]) and 0x08) shr 3) == 1
            isLeadOffaVL = ((byte2UInt(bytes[index]) and 0x10) shr 4) == 1
            isLeadOffaVF = ((byte2UInt(bytes[index]) and 0x20) shr 5) == 1
            isLeadOffV1 = ((byte2UInt(bytes[index]) and 0x40) shr 6) == 1
            isLeadOffV2 = ((byte2UInt(bytes[index]) and 0x80) shr 7) == 1
            isLeadOffV3 = (byte2UInt(bytes[index+1]) and 0x01) == 1
            isLeadOffV4 = ((byte2UInt(bytes[index+1]) and 0x02) shr 1) == 1
            isLeadOffV5 = ((byte2UInt(bytes[index+1]) and 0x04) shr 2) == 1
            isLeadOffV6 = ((byte2UInt(bytes[index+1]) and 0x08) shr 3) == 1
            index += 2
            index += 6
        }

        override fun toString(): String {
            return """
                RtParam : 
                hr : $hr
                temp : $temp
                spo2 : $spo2
                pi : $pi
                pr : $pr
                respRate : $respRate
                batteryStatus : $batteryStatus
                isInsertEcgLeadWire : $isInsertEcgLeadWire
                oxyStatus : $oxyStatus
                isInsertTemp : $isInsertTemp
                measureStatus : $measureStatus
                isHasDevice : $isHasDevice
                isHasTemp : $isHasTemp
                isHasOxy : $isHasOxy
                isHasRespRate : $isHasRespRate
                battery : $battery
                recordTime : $recordTime
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                second : $second
                leadType : $leadType
                leadSn : $leadSn
                isLeadOffI : $isLeadOffI
                isLeadOffII : $isLeadOffII
                isLeadOffIII : $isLeadOffIII
                isLeadOffaVR : $isLeadOffaVR
                isLeadOffaVL : $isLeadOffaVL
                isLeadOffaVF : $isLeadOffaVF
                isLeadOffV1 : $isLeadOffV1
                isLeadOffV2 : $isLeadOffV2
                isLeadOffV3 : $isLeadOffV3
                isLeadOffV4 : $isLeadOffV4
                isLeadOffV5 : $isLeadOffV5
                isLeadOffV6 : $isLeadOffV6
            """.trimIndent()
        }
    }

    class RtWave(leadType: Int, val bytes: ByteArray) {

        var firstIndex: Int               // 数据的第一个点
        var len: Int                      // 采样点数
        var wave = ByteArray(0)      // 压缩原始数据
        var waveInts = IntArray(0)   // 解压后采样点数据
        var waveMvs = FloatArray(0)  // 解压后毫伏值数据，n * 0.00244140625

        init {
            var index = 0
            firstIndex = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            len = toUInt(bytes.copyOfRange(index, index+2))
            index += 2

            if (len > 0) {
                wave = bytes.copyOfRange(index, bytes.size)
                waveInts = getIntsFromWaveBytes(wave, leadType)
                waveMvs = getMvsFromWaveBytes(wave, leadType)
            }
        }

        override fun toString(): String {
            return """
                RtWave : 
                firstIndex : $firstIndex
                len : $len
            """.trimIndent()
        }
    }

    /**
     * 通过设备.dat文件压缩的原始波形数据和导联类型，获取8导解压后的浮点型采样点毫伏值顺序数组
     * V6 I II V1 V2 V3 V4 V5
     */
    fun getMvsFromWaveBytes(wave: ByteArray, leadType: Int) : FloatArray {
        val waveInts = getIntsFromWaveBytes(wave, leadType)
        val waveMvs = mutableListOf<Float>()
        for (i in waveInts) {
            waveMvs.add(i * 0.00244140625f)
        }
        return waveMvs.toFloatArray()
    }

    /**
     * 通过设备.dat文件压缩的原始波形数据和导联类型，获取8导解压后的整型采样点顺序数组
     * V6 I II V1 V2 V3 V4 V5
     */
    fun getIntsFromWaveBytes(wave: ByteArray, leadType: Int) : IntArray {
        // decompress
        val num = if (leadType == 0) {
            8
        } else {
            4
        }
        val decompress = Er3Decompress(num)
        val decompressData = mutableListOf<Int>()
        for (b in wave) {
            val tmp = decompress.Decompress(b)
            if (tmp != null) {
                for (i in tmp) {
                    if (i == 32767) {  // 导联脱落，基线处理
                        decompressData.add(0)
                    } else {
                        decompressData.add(i)
                    }
                }
            }
        }

        val oriInts = decompressData.toIntArray()
        var waveInts = IntArray(0)
        val tmpFs = mutableListOf<Int>()

        when(leadType) {
            0 -> {  // LEAD_12
                val lead_size = 8
                waveInts = oriInts
            }
            1, 7 -> {  // LEAD_6
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(oriInts[i+3])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i])
                }
                waveInts = tmpFs.toIntArray()
            }
            2 -> {  // LEAD_5
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(oriInts[i+3])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveInts = tmpFs.toIntArray()
            }
            3 -> {  // LEAD_3
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveInts = tmpFs.toIntArray()
            }
            4 -> {  // LEAD_3_TEMP
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveInts = tmpFs.toIntArray()
            }
            5 -> {  // LEAD_3_LEG
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                }
                waveInts = tmpFs.toIntArray()
            }
            6 -> {  // LEAD_5_LEG
                val lead_size = 4
                for (i in oriInts.indices step lead_size) {
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i+1])
                    tmpFs.add(oriInts[i+2])
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(0)
                    tmpFs.add(oriInts[i])
                }
                waveInts = tmpFs.toIntArray()
            }
        }
        return waveInts
    }

    /**
     * 通过导联名称和解压后的整型采样点顺序数组，获取每一导联解压后的整型采样点
     * V6 / I / II / V1 / V2 / V3 / V4 / V5 / III / aVR / aVL / aVF
     */
    fun getEachLeadDataInts(leadName: String, waveInts: Array<String>) : MutableList<Int> {
        // V6 I II V1 V2 V3 V4 V5
        // III = II-I
        // aVR = - (I+II)/2
        // aVL = I - II/2
        // aVF = II - I/2
        val tempInts = mutableListOf<Int>()
        when (leadName) {
            "V6" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i].toInt())
                }
            }
            "I" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+1].toInt())
                }
            }
            "II" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+2].toInt())
                }
            }
            "V1" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+3].toInt())
                }
            }
            "V2" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+4].toInt())
                }
            }
            "V3" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+5].toInt())
                }
            }
            "V4" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+6].toInt())
                }
            }
            "V5" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+7].toInt())
                }
            }
            "III" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+2].toInt() - waveInts[i+1].toInt())
                }
            }
            "aVR" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(-(waveInts[i+1].toInt() + waveInts[i+2].toInt()).div(2))
                }
            }
            "aVL" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+1].toInt() - waveInts[i+2].toInt().div(2))
                }
            }
            "aVF" -> {
                for (i in waveInts.indices step 8) {
                    tempInts.add(waveInts[i+2].toInt() - waveInts[i+1].toInt().div(2))
                }
            }
        }
        return tempInts
    }

    class Er3File(val model: Int, val fileName: String, val fileSize: Int, private val userId: String, var index: Int) {

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