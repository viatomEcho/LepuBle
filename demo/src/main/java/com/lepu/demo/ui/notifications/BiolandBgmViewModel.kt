package com.lepu.demo.ui.notifications

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.BiolandBgmBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.getTimeString
import com.lepu.demo.R

class BiolandBgmViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner, context: Context) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmCountDown)
            .observe(owner) {
                val data = it.data as Int
                _info.value = "${context.getString(R.string.countdown)}$data"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmGluData)
            .observe(owner) {
                val data = it.data as BiolandBgmBleResponse.GluData
                _info.value = "${context.getString(R.string.glu_result)}${data.resultMg} mg/dL ${data.resultMmol} mmol/L\n" +
                        "${context.getString(R.string.start_time)}${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmNoGluData)
            .observe(owner) {
                _info.value = context.getString(R.string.no_file)
            }
    }

}