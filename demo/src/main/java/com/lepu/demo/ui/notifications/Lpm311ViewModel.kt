package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.data.Lpm311Data
import com.lepu.blepro.event.InterfaceEvent

class Lpm311ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LPM311.EventLpm311Data)
            .observe(owner) {
                val data = it.data as Lpm311Data
                _info.value = "user : ${data.user}\nCHOL : ${data.chol} （${data.cholStr}）\nTRIG : ${data.trig} （${data.trigStr}）\n" +
                        "HDL : ${data.hdl} （${data.hdlStr}）\nLDL : ${data.ldl} （${data.ldlStr}）\n" +
                        "CHOL/HDL : ${data.cholDivHdl} （${data.cholDivHdlStr}）\nUNIT : ${if (data.unit == 0) {"mmol/L"} else {"mg/dL"}}"
            }
    }

}