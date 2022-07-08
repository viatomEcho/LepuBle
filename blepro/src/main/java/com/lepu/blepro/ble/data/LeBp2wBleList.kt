package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toString

class LeBp2wBleList(val bytes: ByteArray) {
    var listNum: Int
    var listName = mutableListOf<String>()
    init {
        listNum = bytes[0].toInt()
            if (listNum != 0) {
                for (i in 0 until listNum) {
                    val name = trimStr(toString(bytes.copyOfRange(1+i*16, 16*(i+1)+1)))
//                Log.d(TAG, "$i: $fileName")
                    listName.add(name)
                }
            }

    }

    override fun toString(): String {
        return """
            LeBp2wBleList : 
            bytes : ${bytesToHex(bytes)}
            listNum : $listNum
            listName : $listName
        """.trimIndent()
    }
}