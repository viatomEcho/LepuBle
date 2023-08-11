package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.TmbBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.data.*

class TmbViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.TMB.EventTmbRecordData)
            .observe(owner) {
                val data = it.data as TmbBleResponse.Record
                val temp = BpData()
                temp.fileName = data.fileName
                temp.sys = data.sys
                temp.dia = data.dia
                temp.pr = data.pr
                temp.mean = data.mean
                _bpData.value = temp
            }
    }

}