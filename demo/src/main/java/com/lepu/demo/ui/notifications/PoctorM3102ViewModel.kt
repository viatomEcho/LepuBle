package com.lepu.demo.ui.notifications

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.data.PoctorM3102Data
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.getTimeString
import com.lepu.demo.R

class PoctorM3102ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner, context: Context) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PoctorM3102.EventPoctorM3102Data)
            .observe(owner) {
                val data = it.data as PoctorM3102Data
                _info.value = when (data.type) {
                    0 -> "${context.getString(R.string.glu_result)}${if (data.normal) {"${data.result} mmol/L\n" +
                            "${context.getString(R.string.start_time)}${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    1 -> "${context.getString(R.string.ua_result)}${if (data.normal) {"${data.result} umol/L\n" +
                            "${context.getString(R.string.start_time)}${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    3 -> "${context.getString(R.string.ketone_result)}${if (data.normal) {"${data.result} mmol/L\n" +
                            "${context.getString(R.string.start_time)}${getTimeString(data.year, data.month, data.day, data.hour, data.minute, 0)}"} else {if (data.result == 1) {"Hi"} else {"Lo"} }}"
                    else -> "${context.getString(R.string.error_result)}\n$data"
                }
            }
    }

}