package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Pf10Aw1BleResponse
import com.lepu.blepro.event.InterfaceEvent

class Pf10Aw1ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1GetFileList)
            .observe(owner) { event ->
                (event.data as Pf10Aw1BleResponse.FileList).let {
                    val names = arrayListOf<String>()
                    for (fileName in it.fileNames) {
                        if (fileName.isNotEmpty()) {
                            names.add(fileName)
                        }
                    }
                    _fileNames.value = names
                    _info.value = names.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1ReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1ReadFileComplete)
            .observe(owner) { event ->
                (event.data as Pf10Aw1BleResponse.BleFile).let {
                    _readNextFile.value = true
                    _info.value = _info.value + "\n$it"
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1Reset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1FactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1FactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
    }

}