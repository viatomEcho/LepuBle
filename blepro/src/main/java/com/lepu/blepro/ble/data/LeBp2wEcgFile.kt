package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.toLong

class LeBp2wEcgFile {
    var content: ByteArray
    var fileName: String
    var fileVersion: Int
    var fileType: Int
    var timestamp: Long
    var waveData: ByteArray
    var waveShortData: ShortArray
    var deviceName: String
    var duration: Int
    constructor(fileName: String, content: ByteArray, deviceName: String) {
        this.content = content
        this.deviceName = deviceName
        this.fileName = fileName
        var index = 0
        fileVersion = byte2UInt(content[index])
        index++
        fileType = byte2UInt(content[index])
        index++
        timestamp = toLong(content.copyOfRange(index, index+4))
        index += 4
        // reserved 4
        index += 4
        waveData = content.copyOfRange(index, content.size)
        waveShortData = ShortArray(waveData.size.div(2))
        for (i in waveShortData.indices) {
            waveShortData[i] = toSignedShort(waveData[2 * i], waveData[2 * i + 1])
        }
        duration = waveData.size.div(2*125)
    }

    override fun toString(): String {
        return """
            LeBp2wEcgFile:
            file name: $fileName
            file version: $fileVersion
            file type: $fileType
            timestamp: $timestamp
            device name: $deviceName
            duration: $duration
        """.trimIndent()
    }
}