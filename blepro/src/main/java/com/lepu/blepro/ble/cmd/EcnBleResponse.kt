package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt
import org.json.JSONObject

object EcnBleResponse {

    class BleResponse(val bytes: ByteArray) {
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            pkgType = bytes[3]
            pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7 + len)
        }
    }

    class FileList(val bytes: ByteArray) {
        var leftSize: Int
        var listSize: Int
        var list = mutableListOf<BleFile>()
        init {
            var index = 0
            leftSize = byte2UInt(bytes[index])
            index++
            listSize = byte2UInt(bytes[index])
            index++
            for (i in 0 until listSize) {
                val len = 4+byte2UInt(bytes[index+4])+1
                list.add(BleFile(bytes.copyOfRange(index, index+len)))
                index += len
            }
        }

        override fun toString(): String {
            return """
                FileList : 
                leftSize : $leftSize
                listSize : $listSize
                list : $list
            """.trimIndent()
        }
    }

    class BleFile(val bytes: ByteArray) {
        var time: Int
        var fileNameSize: Int
        var fileName: String
        init {
            var index = 0
            time = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            fileNameSize = byte2UInt(bytes[index])
            index++
            fileName = trimStr(String(bytes.copyOfRange(index, index+fileNameSize)))
        }

        override fun toString(): String {
            return """
                BleFile : 
                time : $time
                fileNameSize : $fileNameSize
                fileName : $fileName
            """.trimIndent()
        }
    }

    class File(val content: ByteArray, val fileName: String)

    class RtState(val bytes: ByteArray) {
        var state: Int
        var duration: Int
        // reserved 7
        init {
            var index = 0
            state = byte2UInt(bytes[index])
            index++
            duration = toUInt(bytes.copyOfRange(index, index+2))
        }
        override fun toString(): String {
            return """
                RtState : 
                state : $state
                duration : $duration
            """.trimIndent()
        }
    }

    class RtData(val bytes: ByteArray) {
        var status: RtState
        var wave: ByteArray
        init {
            var index = 0
            status = RtState(bytes.copyOfRange(index, index+10))
            index += 10
            wave = bytes.copyOfRange(index, bytes.size)
        }
        override fun toString(): String {
            return """
                RtData : 
                status : $status
                wave : ${bytesToHex(wave)}
            """.trimIndent()
        }
    }

    class DiagnosisResult(val bytes: ByteArray) {
        var infoStr: JSONObject
        var result = mutableListOf<String>()
        init {
            val data = String(bytes)
            infoStr = if (data.contains("{") && data.contains("}")) {
                JSONObject(data)
            } else {
                JSONObject()
            }
            val array = infoStr.getJSONArray("DiagnosisResult")
            for (i in 0 until array.length()) {
                result.add(array[i].toString())
            }
        }
        override fun toString(): String {
            return """
                DiagnosisResult : 
                result : $result
            """.trimIndent()
        }
    }
}