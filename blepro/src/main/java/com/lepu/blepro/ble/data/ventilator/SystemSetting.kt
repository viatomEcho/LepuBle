package com.lepu.blepro.ble.data.ventilator

import com.lepu.blepro.utils.ByteUtils.byte2UInt

class SystemSetting() {

    var type = 0
    // reserved 3
    var unitSetting = UnitSetting()
    var languageSetting = LanguageSetting()
    var screenSetting = ScreenSetting()
    var replacements = Replacements()
    var volumeSetting = VolumeSetting()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        unitSetting = UnitSetting(bytes.copyOfRange(index, index+1))
        index++
        languageSetting = LanguageSetting(bytes.copyOfRange(index, index+1))
        index++
        screenSetting = ScreenSetting(bytes.copyOfRange(index, index+2))
        index += 2
        replacements = Replacements(bytes.copyOfRange(index, index+4))
        index += 4
        volumeSetting = VolumeSetting(bytes.copyOfRange(index, index+1))
        // reserved 7
    }

    fun getDataBytes(): ByteArray {
        var data = byteArrayOf(type.toByte())
            .plus(ByteArray(3))
        when (type) {
            0 -> {
                data = data.plus(unitSetting.getDataBytes())
                    .plus(languageSetting.getDataBytes())
                    .plus(screenSetting.getDataBytes())
                    .plus(replacements.getDataBytes())
                    .plus(volumeSetting.getDataBytes())
            }
            1 -> {
                data = data.plus(unitSetting.getDataBytes())
            }
            2 -> {
                data = data.plus(languageSetting.getDataBytes())
            }
            3 -> {
                data = data.plus(screenSetting.getDataBytes())
            }
            4 -> {
                data = data.plus(replacements.getDataBytes())
            }
            5 -> {
                data = data.plus(volumeSetting.getDataBytes())
            }
        }
        return data
    }

    class UnitSetting() {
        var pressureUnit = 0  // 0: cmH2O; 1: hPa
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            pressureUnit = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(pressureUnit.toByte())
                .plus(ByteArray(3))
        }
    }
    class LanguageSetting() {
        var language = 0  // 0: 英语; 1: 中文
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            language = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(language.toByte())
                .plus(ByteArray(3))
        }
    }
    class ScreenSetting() {
        var brightness = 60  // 屏幕亮度。5-100%，步进1%。默认60%
        var autoOff = 30     // 自动息屏。0:常亮。其他有效值：30，60，90，120. 单位秒。默认30秒
        // reserved 2
        constructor(bytes: ByteArray) : this() {
            var index = 0
            brightness = byte2UInt(bytes[index])
            index++
            autoOff = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(brightness.toByte())
                .plus(autoOff.toByte())
                .plus(ByteArray(2))
        }
    }
    class Replacements() {
        var filter = 1  // 过滤棉。0：关闭；其他有效值1-12，单位月。默认1.
        var mask = 3    // 面罩。0：关闭；其他有效值1-12，单位月。默认3.
        var tube = 3    // 管路。0：关闭；其他有效值1-12，单位月。默认3.
        var tank = 6    // 水箱。0：关闭；其他有效值1-12，单位月。默认6.
        constructor(bytes: ByteArray) : this() {
            var index = 0
            filter = byte2UInt(bytes[index])
            index++
            mask = byte2UInt(bytes[index])
            index++
            tube = byte2UInt(bytes[index])
            index++
            tank = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(filter.toByte())
                .plus(mask.toByte())
                .plus(tube.toByte())
                .plus(tank.toByte())
        }
    }
    class VolumeSetting() {
        var volume = 30  // 音量, 0-100%，步进1%；默认30%
        // reserved 3
        constructor(bytes: ByteArray) : this() {
            var index = 0
            volume = byte2UInt(bytes[index])
        }
        fun getDataBytes(): ByteArray {
            return byteArrayOf(volume.toByte())
                .plus(ByteArray(3))
        }
    }
}