package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Pc68bBleResponse
import com.lepu.blepro.event.InterfaceEvent

class Pc68bViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bFileList)
            .observe(owner) {
                val data = it.data as MutableList<String>
                val names = arrayListOf<String>()
                for (i in data) {
                    names.add(i)
                }
                _fileNames.value = names
                _info.value = names.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bReadFileComplete)
            .observe(owner) {
                val data = it.data as Pc68bBleResponse.Record
                _info.value = "$data"
                _readNextFile.value = true
            }
    }

}