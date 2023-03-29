package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.PC80BleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.demo.util.DataConvert

class Pc80bViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadingFileProgress)
            .observe(owner) {
                _process.value = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bReadFileComplete)
            .observe(owner) {
                val data = it.data as PC80BleResponse.ScpEcgFile
                val fileName = getTimeString(data.section1.year, data.section1.month, data.section1.day, data.section1.hour, data.section1.minute, data.section1.second)
                val temp = getEcgData(DateUtil.getSecondTimestamp(fileName), fileName, data.section6.ecgData.ecg, DataConvert.getPc80bShortArray(data.section6.ecgData.ecg), 30)
                _ecgData.value = temp
            }
    }

}