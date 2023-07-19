package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.OxyBleFile
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.getTimeString
import com.lepu.demo.data.OxyData
import java.io.File

class OxyViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo)
            .observe(owner) { event ->
                (event.data as OxyBleResponse.OxyInfo).let {
                    val names = arrayListOf<String>()
                    for (fileName in it.fileList.split(",")) {
                        if (fileName.isNotEmpty()) {
                            names.add(fileName)
                        }
                    }
                    _fileNames.value = names
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it.div(10)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileComplete)
            .observe(owner) { event ->
                (event.data as OxyBleResponse.OxyFile).let {
                    val content = DownloadHelper.readFile(it.model, "", it.fileName)
                    val data = if (content.isEmpty()) {
                        OxyBleFile(it.fileContent)
                    } else {
                        OxyBleFile(content)
                    }
                    val fileName = getTimeString(data.year, data.month, data.day, data.hour, data.minute, data.second)
                    val filePath = "${BleServiceHelper.BleServiceHelper.rawFolder?.get(Bluetooth.MODEL_O2RING)}/$fileName.dat"
                    val isSave = File(filePath).exists()
                    if (!isSave) {
                        DownloadHelper.writeFile(Bluetooth.MODEL_O2RING, "", fileName, "dat", data.bytes)
                    }
                    val temp = OxyData()
                    temp.fileName = fileName
                    temp.oxyBleFile = data
                    _oxyData.value = temp
                    _readNextFile.value = true
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyFactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
    }

}