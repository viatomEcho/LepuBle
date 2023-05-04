package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.lew.*
import com.lepu.blepro.event.InterfaceEvent

class LewViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewFileList)
            .observe(owner) {
                val data = it.data as LewBleResponse.FileList
                when (data.type) {
                    LewBleCmd.ListType.SPORT -> {
                        val list = SportList(data.listSize, data.content)
                        _info.value = "$list"
                    }
                    LewBleCmd.ListType.ECG -> {
                        val list = EcgList(data.listSize, data.content)
                        val names = arrayListOf<String>()
                        for (item in list.items) {
                            names.add(item.name)
                        }
                        _fileNames.value = names
                        _info.value = names.toString()
                    }
                    LewBleCmd.ListType.HR -> {
                        val list = HrList(data.listSize, data.content)
                        _info.value = "$list"
                    }
                    LewBleCmd.ListType.OXY -> {
                        val list = OxyList(data.listSize, data.content)
                        _info.value = "$list"
                    }
                    LewBleCmd.ListType.SLEEP -> {
                        val list = SleepList(data.listSize, data.content)
                        _info.value = "$list"
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it.div(10)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewReadFileComplete)
            .observe(owner) { event ->
                (event.data as LewBleResponse.EcgFile).let {
                    val file = EcgFile(it.content)
//                    _info.value = "$file"
                    _readNextFile.value = true
                }
            }
    }

}