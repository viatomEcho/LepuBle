package com.lepu.blepro.ble.data

class Bp2Config {
    var switchState: Boolean
    var volume: Int

    constructor(content: ByteArray) {
        switchState = content[24].toInt() == 1
        volume = content[26].toInt()
    }

    override fun toString(): String {
        return """
            Bp2Config:
            switchState: $switchState
            volume: $volume
        """.trimIndent()
    }
}