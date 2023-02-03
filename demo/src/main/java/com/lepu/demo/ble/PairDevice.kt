package com.lepu.demo.ble

import android.bluetooth.le.ScanRecord
import java.util.*
import kotlin.collections.HashMap

/**
 * author: wujuan
 * created on: 2020/12/17 11:05
 * description:
 */
class PairDevice {
    companion object{

        fun pairO2(record: ScanRecord): Boolean {
            val parseRecord = record.bytes?.let { parseRecord(it) } ?: return false

            return parseRecord[-1].equals("4EF301")

        }

        private fun parseRecord(scanRecord: ByteArray): Map<Int, String>? {


            val ret: MutableMap<Int, String> = HashMap()
            var index = 0

            while (index < scanRecord.size) {
                val length = scanRecord[index++].toInt()
                //Zero value indicates that we are done with the record now
                if (length <= 0) break
                val type = scanRecord[index].toInt()
                //if the type is zero, then we are pass the significant section of the data,
                // and we are thud done
                if (type == 0) break
                val data = Arrays.copyOfRange(scanRecord, index + 1, index + length)
                if (data.isNotEmpty()) {
                    val hex = StringBuilder(data.size * 2)
                    //                // the data appears to be there backwards
//                for (int bb = data.length- 1; bb >= 0; bb--){
//                    hex.append(String.format("%02X", data[bb]));
//                }
                    for (bb in data.indices) {
                        hex.append(String.format("%02X", data[bb]))
                    }
                    ret[type] = hex.toString()
                }
                index += length
            }
            return ret
        }
    }
}