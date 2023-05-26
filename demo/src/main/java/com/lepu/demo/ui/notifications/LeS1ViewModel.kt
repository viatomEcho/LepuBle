package com.lepu.demo.ui.notifications

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.LeS1BleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.DateUtil
import com.lepu.demo.R

class LeS1ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner, context: Context) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1NoFile)
            .observe(owner) { event ->
                (event.data as Boolean).let {
                    _info.value = "${context.getString(R.string.no_file)}$it"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it.div(10)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1ReadFileComplete)
            .observe(owner) { event ->
                (event.data as LeS1BleResponse.BleFile).let {
                    val temp = getEcgData(DateUtil.getSecondTimestamp("00000000000000"), "00000000000000", it.ecgData, it.ecgIntData, it.ecgResult?.recordingTime!!)
                    _ecgData.value = temp
                    _readNextFile.value = true
                }
            }
    }

}