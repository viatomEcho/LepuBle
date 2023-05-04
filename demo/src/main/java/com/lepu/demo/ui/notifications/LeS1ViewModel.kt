package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.LeS1BleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.DateUtil

class LeS1ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1NoFile)
            .observe(owner) { event ->
                (event.data as Boolean).let {
                    _info.value = "没有文件 $it"
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