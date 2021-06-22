package com.viatom.ktble.ble.objs

import android.util.Log
import com.lepu.blepro.utils.toString

class KtBleFileList {
    var fileNum: Int
    var fileNameList : Array<String?>
    var deviceName: String
    constructor(bytes: ByteArray, deviceName: String) {
        this.deviceName = deviceName
            this.fileNum = bytes[0].toInt()

            fileNameList = arrayOfNulls<String>(fileNum)

            if (fileNum != 0) {
                for (i in 0 until fileNum) {
                    val fileName:String = toString(bytes.copyOfRange(1+i*16, 16*(i+1)+1)).trimIndent()
//                Log.d(TAG, "$i: $fileName")
                    fileNameList[i] = fileName
                }
            }

    }

    override fun toString(): String {
        var str : String = "["

        for (s : String? in fileNameList) {
            str += s
            str += ", "
        }

        str += "]"

        return str
    }
}