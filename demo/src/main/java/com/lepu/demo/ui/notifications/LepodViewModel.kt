package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.LepodBleResponse
import com.lepu.blepro.ble.data.Er3DataFile
import com.lepu.blepro.event.InterfaceEvent

class LepodViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodFileList)
            .observe(owner) { event ->
                (event.data as LepodBleResponse.FileList).let {
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodReadFileComplete)
            .observe(owner) { event ->
                (event.data as LepodBleResponse.BleFile).let {
                    if (it.fileName.contains("T")) {
                        val data = Er3DataFile(it.content)
//                        _info.value = "$data"
                    } else if (it.fileName.contains("W")) {

                    }
                    _readNextFile.value = true
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodReset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodFactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodFactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
    }

}