package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toUInt

class LeBp2wEcgFile {
    var content: ByteArray
    var fileName: String
    var fileVersion: Int
    var fileType: Int
    var timestamp: Long
    var waveData: ByteArray
    var deviceName: String
    constructor(fileName: String, content: ByteArray, deviceName: String) {
        this.content = content
        this.deviceName = deviceName
        this.fileName = fileName
        var index = 0
        fileVersion = byte2UInt(content[index])
        index++
        fileType = byte2UInt(content[index])
        index++
        timestamp = toUInt(content.copyOfRange(index, index+4)).toLong()
        index += 4
        // reserved 4
        index += 4
        waveData = content.copyOfRange(index, content.size)
    }

    override fun toString(): String {
        return """
            LeBp2wEcgFile:
            file name: $fileName
            file version: $fileVersion
            file type: $fileType
            timestamp: $timestamp
            device name: $deviceName
        """.trimIndent()
    }
}