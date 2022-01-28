package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex

class FileListCrc {
    var fileType: Int  // 文件类型 Ble.File.ECG_TYPE, Ble.File.BP_TYPE, Ble.File.USER_TYPE,
    var crc: String    // 0表示文件不存在

    constructor(fileType: Int, crc: ByteArray) {
        this.fileType = fileType
        this.crc = bytesToHex(crc)
    }

    override fun toString(): String {
        return """
            fileType : $fileType
            crc : $crc
        """.trimIndent()
    }

}