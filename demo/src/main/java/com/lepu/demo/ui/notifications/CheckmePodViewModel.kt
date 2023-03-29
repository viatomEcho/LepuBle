package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.CheckmePodBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.getTimeString

class CheckmePodViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodGetFileListProgress)
            .observe(owner) {
                _process.value = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodFileList)
            .observe(owner) {
                val data = it.data as CheckmePodBleResponse.FileList
                var temp = ""
                for (record in data.list) {
                    temp += "时间 : ${getTimeString(record.year, record.month, record.day, record.hour, record.minute, record.second)}\nSpO2 : ${record.spo2} %\nPR : ${record.pr}\nPI : ${record.pi}\nTemp : ${record.temp} ℃\n\n"
                }
                _info.value = temp
            }
    }

}