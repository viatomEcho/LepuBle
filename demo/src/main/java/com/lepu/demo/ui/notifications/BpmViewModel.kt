package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.BpmBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.getTimeString
import com.lepu.demo.data.BpData

class BpmViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordData)
            .observe(owner) { event ->
                (event.data as BpmBleResponse.RecordData).let {
                    val temp = BpData()
                    temp.fileName = getTimeString(it.year, it.month, it.day, it.hour, it.minute, 0)
                    temp.sys = it.sys
                    temp.dia = it.dia
                    temp.pr = it.pr
                    _bpData.value = temp
                }
            }
    }
}