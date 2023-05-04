package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Er3BleResponse
import com.lepu.blepro.ble.data.Er3DataFile
import com.lepu.blepro.event.InterfaceEvent

class Er3ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        /*LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3FileList)
            .observe(owner) { event ->
                (event.data as Er3BleResponse.FileList).let {
                    val names = arrayListOf<String>()
                    for (fileName in it.fileList) {
                        if (fileName.isNotEmpty()) {
                            names.add(fileName)
                        }
                    }
                    _fileNames.value = names
                    _info.value = names.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3ReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3ReadFileComplete)
            .observe(owner) { event ->
                (event.data as Er3BleResponse.Er3File).let {
                    if (it.fileName.contains("T")) {
                        val data = Er3DataFile(it.content)
//                        _info.value = "$data"
                    } else if (it.fileName.contains("W")) {

                    }
                    _readNextFile.value = true
                }
            }*/
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3Reset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3FactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3FactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
    }

}