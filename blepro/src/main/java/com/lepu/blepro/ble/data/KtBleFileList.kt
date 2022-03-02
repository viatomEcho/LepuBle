package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.HexString.trimStr
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
                    val fileName:String = trimStr(toString(bytes.copyOfRange(1+i*16, 16*(i+1)+1)))
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