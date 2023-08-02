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
                    temp.recordingTime = data.recordingTime
                    temp.avgSpo2 = data.avgSpo2
                    temp.minSpo2 = data.minSpo2
                    temp.dropsTimes3Percent = data.dropsTimes3Percent
                    temp.dropsTimes4Percent = data.dropsTimes4Percent
                    temp.asleepTimePercent = data.asleepTimePercent
                    temp.durationTime90Percent = data.durationTime90Percent
                    temp.dropsTimes90Percent = data.dropsTimes90Percent
                    temp.asleepTime = data.asleepTime
                    temp.o2Score = data.o2Score
                    temp.startTime = data.startTime
                    val len = data.data.size
                    val spo2s = IntArray(len)
                    val hrs = IntArray(len)
                    val motions = IntArray(len)
                    val warningSpo2s = BooleanArray(len)
                    val warningHrs = BooleanArray(len)
                    for (i in 0 until len) {
                        spo2s[i] = data.data[i].spo2
                        hrs[i] = data.data[i].pr
                        motions[i] = data.data[i].vector
                        warningSpo2s[i] = data.data[i].warningSignSpo2
                        warningHrs[i] = data.data[i].warningSignPr
                    }
                    temp.spo2s = spo2s
                    temp.hrs = hrs
                    temp.motions = motions
                    temp.warningSpo2s = warningSpo2s
                    temp.warningHrs = warningHrs
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