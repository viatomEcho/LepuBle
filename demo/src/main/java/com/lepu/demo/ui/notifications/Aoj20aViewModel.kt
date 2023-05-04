package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Aoj20aBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.getTimeString

class Aoj20aViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aTempList)
            .observe(owner) {
                val data = it.data as ArrayList<Aoj20aBleResponse.TempRecord>
                var temp = ""
                for (record in data) {
                    temp += "时间 : ${getTimeString(record.year, record.month, record.day, record.hour, record.minute, 0)} ${record.temp} ℃\n\n"
                }
                _info.value = temp
            }
    }

}