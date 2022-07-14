package com.lepu.blepro.ble.data

class Bp2BleFile {
    var name: String
    var type: Int
    var size: Int
    var content: ByteArray
    var deviceName: String
    constructor(name: String, content: ByteArray, deviceName: String) {
        this.deviceName = deviceName
        this.name = name
        this.type = content[1].toInt()
        this.size = content.size
        this.content = content
    }

    override fun toString(): String {
        val string = """
            Bp2BleFile:
            file name: $name
            file type: $type
            file size: $size
            device name: $deviceName
        """.trimIndent()
        return string
    }
}