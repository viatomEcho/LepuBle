package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.toSignedShort
import com.lepu.blepro.utils.toUInt
import java.util.*

class VetcorderInfo(var data: ByteArray) {
    var ecgWave: ByteArray
    var ecgwIs: ShortArray
    var ecgwFs: FloatArray
    var hr: Int
    var qrs: Int
    var st: Int
    var pvcs: Int
    var mark: Int
    var ecgNote: Int
    var spo2Wave: ByteArray
    var spo2wIs: IntArray
    var pr: Int
    var spo2: Int
    var pi: Int
    var pulseSound: Int
    var spo2Note: Int
    var battery: Int

    init {
        var index = 4
        ecgWave = data.copyOfRange(index, index+10)
        val len = ecgWave.size.div(2)
        ecgwIs = ShortArray(len)
        ecgwFs = FloatArray(len)
        for (i in ecgwFs.indices) {
            ecgwIs[i] = toSignedShort(ecgWave[2 * i], ecgWave[2 * i + 1])
//            ecgwFs[i] = (ecgwIs[i]*4033)/(32767*12*8f)
            ecgwFs[i] = (ecgwIs[i]*4033)/(32767*12*1.05f)
        }

        index += 10
        hr = toUInt(data.copyOfRange(index, index+2))
        index += 2
        qrs = toUInt(data.copyOfRange(index, index+2))
        index += 2
        st = toUInt(data.copyOfRange(index, index+2))
        index += 2
        pvcs = toUInt(data.copyOfRange(index, index+2))
        index += 2
        mark = toUInt(data.copyOfRange(index, index+1))
        index++
        ecgNote = toUInt(data.copyOfRange(index, index+1))
        index++

        index++
        spo2Wave = data.copyOfRange(index, index+10)
        spo2wIs = IntArray(spo2Wave.size/2)
        for (i in spo2wIs.indices) {
            spo2wIs[i] = toUInt(spo2Wave.copyOfRange(2 * i, 2 * i + 2))
        }

        index += 10
        pr = toUInt(data.copyOfRange(index, index+2))
        index += 2
        spo2 = toUInt(data.copyOfRange(index, index+1))
        index++
        pi = toUInt(data.copyOfRange(index, index+1))
        index++
        pulseSound = toUInt(data.copyOfRange(index, index+1))
        index++
        spo2Note = toUInt(data.copyOfRange(index, index+1))
        index++
        index++
        battery = toUInt(data.copyOfRange(index, index+1))

    }

    override fun toString(): String {
        return """
            VetcorderInfo
            ecgWave: ${Arrays.toString(ecgWave)}
            ecgwIs: ${Arrays.toString(ecgwIs)}
            ecgwFs: ${Arrays.toString(ecgwFs)}
            hr: $hr
            qrs: $qrs
            st: $st
            pvcs: $pvcs
            mark: $mark
            ecgNote: $ecgNote
            spo2Wave: ${Arrays.toString(spo2Wave)}
            spo2wIs: ${Arrays.toString(spo2wIs)}
            pr: $pr
            spo2: $spo2
            pi: $pi
            pulseSound: $pulseSound
            spo2Note: $spo2Note
            battery: $battery
        """.trimIndent()
    }
}