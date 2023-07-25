package com.lepu.demo.ui.notifications

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.OxyIIBleCmd
import com.lepu.blepro.ble.cmd.OxyIIBleResponse
import com.lepu.blepro.ble.data.OxyIIBleFile
import com.lepu.blepro.ble.data.PpgFile
import com.lepu.blepro.event.InterfaceEvent

class OxyIIViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetFileList)
            .observe(owner) { event ->
                (event.data as OxyIIBleResponse.FileList).let {
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadFileComplete)
            .observe(owner) { event ->
                (event.data as OxyIIBleResponse.BleFile).let {
                    _readNextFile.value = true
                    val data = if (it.fileType == OxyIIBleCmd.FileType.OXY) {
                        OxyIIBleFile(it.content)
                    } else {
                        PpgFile(it.content)
                    }
                    _info.value = _info.value + "\n$data"
                    Log.d("111111111111", "_info.value : ${_info.value}")
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
    }

}