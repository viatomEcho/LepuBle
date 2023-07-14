package com.lepu.blepro.event

import com.jeremyliao.liveeventbus.core.LiveEvent

/**
 * author: wujuan
 * created on: 2021/2/3 18:35
 * description: 从interface发送的业务通知，都要携带model。App使用时通过model区分同功能系列的不同设备
 */
class InterfaceEvent(val model: Int, val data: Any): LiveEvent {

    /**
     * OxyBleInterface 发出的通知
     * 包含model: MODEL_O2RING(O2Ring xxxx), MODEL_BABYO2(BabyO2 xxxx), MODEL_BABYO2N(BabyO2N xxxx),
     *           MODEL_CHECKO2(O2 xxxx), MODEL_O2M(O2M xxxx), MODEL_SLEEPO2(SleepO2 xxxx),
     *           MODEL_SNOREO2(O2BAND xxxx), MODEL_WEARO2(WearO2 xxxx), MODEL_SLEEPU(SleepU xxxx),
     *           MODEL_OXYLINK(Oxylink xxxx), MODEL_KIDSO2(KidsO2 xxxx), MODEL_OXYFIT(Oxyfit xxxx),
     *           MODEL_OXYRING(OxyRing xxxx), MODEL_BBSM_S1(BBSM S1 xxxx), MODEL_BBSM_S2(BBSM S2 xxxx),
     *           MODEL_OXYU(OxyU xxxx), MODEL_AI_S100(AI S100 xxxx), MODEL_O2M_WPS(O2M-WPS xxxx),
     *           MODEL_CMRING(CMRingS xxxx), MODEL_OXYFIT_WPS(Oxyfit-WPS xxxx), MODEL_KIDSO2_WPS(KidsO2-WPS xxxx)
     * 功能：
     * 1.同步设置参数：BleServiceHelper.oxyUpdateSetting()
     * 2.获取设备信息：BleServiceHelper.oxyGetInfo()
     * 3.读取设备文件：BleServiceHelper.oxyReadFile()
     * 4.获取实时血氧数据：BleServiceHelper.oxyGetRtParam()/oxyGetRtWave()
     * 5.恢复出厂设置：BleServiceHelper.oxyFactoryReset()
     */
    interface Oxy {
        companion object {
            const val EventOxyReadFileError = "com.lepu.ble.oxy.read.file.error"              // 传输文件失败 boolean
            const val EventOxyReadFileComplete = "com.lepu.ble.oxy.read.file.complete"        // 传输文件成功 com.lepu.blepro.ext.oxy.OxyFile
            const val EventOxyReadingFileProgress = "com.lepu.ble.oxy.reading.file.progress"  // 当前文件进度 int(0-100)
            const val EventOxyFactoryReset = "com.lepu.ble.oxy.factory.reset"                 // 恢复出厂设置 boolean
            const val EventOxySyncDeviceInfo = "com.lepu.ble.oxy.sync"                        // 同步参数 String[]
            const val EventOxyInfo = "com.lepu.ble.oxy.info"                                  // 设备信息 com.lepu.blepro.ext.oxy.DeviceInfo
            const val EventOxyRtData = "com.lepu.ble.oxy.rtData"                              // 实时波形 com.lepu.blepro.ext.oxy.RtWave
            const val EventOxyRtWaveRes = "com.lepu.ble.oxy.rt.wave.res"                      // 实时波形失败 boolean
            const val EventOxyRtParamData = "com.lepu.ble.oxy.rt.param.Data"                  // 实时参数 com.lepu.blepro.ext.oxy.RtParam
            const val EventOxyRtParamRes = "com.lepu.ble.oxy.rt.param.res"                    // 实时参数失败 boolean
        }
    }

    /**
     * Er1BleInterface 发出的通知
     * 包含model: MODEL_ER1(ER1 xxxx), MODEL_ER1_N(VBeat xxxx), MODEL_HHM1(HHM1 xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.er1GetInfo()
     * 2.获取实时心电数据：BleServiceHelper.startRtTask()/BleServiceHelper.stopRtTask()
     * 3.获取文件列表：BleServiceHelper.er1GetFileList()
     * 4.读取设备文件：BleServiceHelper.er1ReadFile()
     * 5.设置/获取配置信息：BleServiceHelper.er1SetConfig()/BleServiceHelper.er1GetConfig()
     * 6.复位：BleServiceHelper.er1Reset()
     * 7.恢复出厂设置：BleServiceHelper.er1FactoryReset()
     * 8.恢复生产出厂状态：BleServiceHelper.er1FactoryResetAll()
     */
    interface ER1 {
        companion object {
            const val EventEr1Info = "com.lepu.ble.er1.info"                                  // 设备信息 com.lepu.blepro.ext.er1.DeviceInfo
            const val EventEr1RtData = "com.lepu.ble.er1.rtData"                              // 实时数据 com.lepu.blepro.ext.er1.RtData
            const val EventEr1FileList = "com.lepu.ble.er1.fileList"                          // 文件列表 ArrayList<String>
            const val EventEr1ReadFileError = "com.lepu.ble.er1.read.file.error"              // 传输文件出错 boolean
            const val EventEr1ReadingFileProgress = "com.lepu.ble.er1.reading.file.progress"  // 传输文件进度 int(0-100)
            const val EventEr1ReadFileComplete = "com.lepu.ble.er1.read.file.complete"        // 传输文件完成 com.lepu.blepro.ext.er1.Er1File
            const val EventEr1Reset = "com.lepu.ble.er1.reset"                                // 复位 boolean
            const val EventEr1ResetFactory = "com.lepu.ble.er1.reset.factory"                 // 恢复出厂设置 boolean
            const val EventEr1ResetFactoryAll = "com.lepu.ble.er1.reset.factory.all"          // 恢复生产出厂状态 boolean
            const val EventEr1GetConfig = "com.lepu.ble.er1.get.config"                       // 获取配置参数 com.lepu.blepro.ext.er1.Er1Config
            const val EventEr1GetConfigError = "com.lepu.ble.er1.get.config.error"            // 获取配置参数失败 boolean
            const val EventEr1SetConfig = "com.lepu.ble.er1.set.config"                       // 设置配置 boolean
            const val EventEr1SetTime = "com.lepu.ble.er1.set.time"                           // 同步时间 boolean
        }
    }

    /**
     * BpmBleInterface 发出的通知
     * 包含model: MODEL_BPM(BPM-188)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.bpmGetInfo()
     * 2.实时血压数据：设备测量时自动上发
     * 3.获取设备状态：BleServiceHelper.bpmGetRtState()
     * 4.获取历史数据：BleServiceHelper.bpmGetFileList()
     */
    interface BPM {
        companion object {
            const val EventBpmInfo = "com.lepu.ble.bpm.info"                     // 设备信息 com.lepu.blepro.ext.bpm.DeviceInfo
            const val EventBpmRtData = "com.lepu.ble.bpm.rtData"                 // 实时压力值 int
            const val EventBpmState = "com.lepu.ble.bpm.state"                   // 实时状态 int
            const val EventBpmSyncTime = "com.lepu.ble.bpm.sync.time"            // 同步时间 true
            const val EventBpmRecordData = "com.lepu.ble.bpm.record.data"        // 记录数据 com.lepu.blepro.ext.bpm.RecordData
            const val EventBpmRecordEnd = "com.lepu.ble.bpm.record.end"          // 传输完成 true
            const val EventBpmMeasureResult = "com.lepu.ble.bpm.measure.result"  // 测量结果 com.lepu.blepro.ext.bpm.RecordData
            const val EventBpmMeasureErrorResult = "com.lepu.ble.bpm.measure.error.result"  // 测量错误结果 int
        }
    }

    /**
     * Bp2BleInterface 发出的通知
     * 包含model: MODEL_BP2(BP2 xxxx), MODEL_BP2A(BP2A xxxx), MODEL_BP2T(BP2T xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.bp2GetInfo()
     * 2.获取实时心电、血压数据：BleServiceHelper.startRtTask()/BleServiceHelper.stopRtTask()
     * 3.获取文件列表：BleServiceHelper.bp2GetFileList()
     * 4.读取设备文件：BleServiceHelper.bp2ReadFile()
     * 5.设置/获取配置信息：BleServiceHelper.bp2SetConfig()/BleServiceHelper.bp2GetConfig()
     * 6.复位：BleServiceHelper.bp2Reset()
     * 7.恢复出厂设置：BleServiceHelper.bp2FactoryReset()
     * 8.恢复生产出厂状态：BleServiceHelper.bp2FactoryResetAll()
     */
    interface BP2 {
        companion object {
            const val EventBp2Info = "com.lepu.ble.bp2.info"                                  // 设备信息 com.lepu.blepro.ext.bp2.DeviceInfo
            const val EventBp2RtData = "com.lepu.ble.bp2.rtData"                              // 实时数据 com.lepu.blepro.ext.bp2.RtData
            const val EventBp2FileList = "com.lepu.ble.bp2.fileList"                          // 文件列表 ArrayList<String>
            const val EventBp2ReadFileError = "com.lepu.ble.bp2.read.file.error"              // 读文件出错 String(fileName)
            const val EventBp2ReadingFileProgress = "com.lepu.ble.bp2.reading.file.progress"  // 传输文件进度 int(0-100)
            const val EventBp2ReadFileComplete = "com.lepu.ble.bp2.read.file.complete"        // 传输文件完成 com.lepu.blepro.ext.bp2.Bp2File
            const val EventBp2Reset = "com.lepu.ble.bp2.reset"                                // 复位 boolean(false：失败 true：成功)
            const val EventBp2FactoryReset = "com.lepu.ble.bp2.factory.reset"                 // 恢复出厂设置 boolean
            const val EventBp2FactoryResetAll = "com.lepu.ble.bp2.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventBp2SetConfig = "com.lepu.ble.bp2.set.config"                       // 设置心跳音开关 boolean
            const val EventBp2GetConfig = "com.lepu.ble.bp2.get.config"                       // 获取配置信息 com.lepu.blepro.ext.bp2.Bp2Config
            const val EventBp2GetConfigError = "com.lepu.ble.bp2.get.config.error"            // 获取配置信息失败 true
            const val EventBp2SyncTime = "com.lepu.ble.bp2.sync.time"                         // 同步时间 boolean
        }
    }

    /**
     * Bp2wBleInterface 发出的通知
     * 包含model: MODEL_BP2W(BP2W xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.bp2wGetInfo()
     * 2.获取实时心电、血压数据：BleServiceHelper.startRtTask()/BleServiceHelper.stopRtTask()
     * 3.获取文件列表：BleServiceHelper.bp2wGetFileList()
     * 4.读取设备文件：BleServiceHelper.bp2wReadFile()
     * 5.设置/获取配置信息：BleServiceHelper.bp2wSetConfig()/BleServiceHelper.bp2wGetConfig()
     * 6.复位：BleServiceHelper.bp2wReset()
     * 7.恢复出厂设置：BleServiceHelper.bp2wFactoryReset()
     * 8.恢复生产出厂状态：BleServiceHelper.bp2wFactoryResetAll()
     * 9.删除文件：BleServiceHelper.bp2wDeleteFile()
     */
    interface BP2W {
        companion object {
            const val EventBp2wInfo = "com.lepu.ble.bp2w.info"                                  // 设备信息 com.lepu.blepro.ext.bp2w.DeviceInfo
            const val EventBp2wRtData = "com.lepu.ble.bp2w.rtData"                              // 实时数据 com.lepu.blepro.ext.bp2w.RtData
            const val EventBp2wFileList = "com.lepu.ble.bp2w.fileList"                          // 文件列表 ArrayList<String>
            const val EventBp2wReadFileError = "com.lepu.ble.bp2w.read.file.error"              // 读文件出错 String(fileName)
            const val EventBp2wReadingFileProgress = "com.lepu.ble.bp2w.reading.file.progress"  // 传输文件进度 int(0-100)
            const val EventBp2wReadFileComplete = "com.lepu.ble.bp2w.read.file.complete"        // 传输文件完成 com.lepu.blepro.ext.bp2w.Bp2wFile
            const val EventBp2wReset = "com.lepu.ble.bp2w.reset"                                // 复位 boolean
            const val EventBp2wFactoryReset = "com.lepu.ble.bp2w.factory.reset"                 // 恢复出厂设置 boolean
            const val EventBp2wFactoryResetAll = "com.lepu.ble.bp2w.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventBp2wSetConfig = "com.lepu.ble.bp2w.set.config"                       // 设置参数 boolean
            const val EventBp2wGetConfig = "com.lepu.ble.bp2w.get.config"                       // 获取参数 com.lepu.blepro.ext.bp2w.Bp2wConfig
            const val EventBp2wSyncTime = "com.lepu.ble.bp2w.sync.time"                         // 同步时间 boolean
//            const val EventBp2WifiList = "com.lepu.ble.bp2w.wifi.list"                          // 获取路由
//            const val EventBp2WifiScanning = "com.lepu.ble.bp2w.wifi.scanning"                  // 正在扫描路由 boolean
//            const val EventBp2wGetWifiConfig = "com.lepu.ble.bp2w.get.wifi.config"              // 获取WiFi配置
//            const val EventBp2wSetWifiConfig = "com.lepu.ble.bp2w.set.wifi.config"              // 设置WiFi boolean
            const val EventBp2wDeleteFile = "com.lepu.ble.bp2w.delete.file"                     // 删除文件 boolean
        }
    }

    /**
     * LpBp2wBleInterface 发出的通知
     * 包含model: MODEL_LP_BP2W(LP-BP2W xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.lpBp2wGetInfo()
     * 2.获取实时心电、血压数据：BleServiceHelper.startRtTask()/BleServiceHelper.stopRtTask()
     * 3.获取用户列表：BleServiceHelper.lpBp2wGetFileList(Constant.LpBp2wListType.USER_TYPE)
     * 4.获取血压列表：BleServiceHelper.lpBp2wGetFileList(Constant.LpBp2wListType.BP_TYPE)
     * 5.获取心电列表：BleServiceHelper.lpBp2wGetFileList(Constant.LpBp2wListType.ECG_TYPE)
     * 6.读取心电文件：BleServiceHelper.lpBp2wReadFile()
     * 7.写用户数据：BleServiceHelper.lpBp2WriteUserList()
     * 8.设置/获取配置信息：BleServiceHelper.lpBp2wSetConfig()/BleServiceHelper.lpBp2wGetConfig()
     * 9.复位：BleServiceHelper.lpBp2wReset()
     * 10.恢复出厂设置：BleServiceHelper.lpBp2wFactoryReset()
     * 11.恢复生产出厂状态：BleServiceHelper.lpBp2wFactoryResetAll()
     * 12.删除文件：BleServiceHelper.lpBp2wDeleteFile()
     */
    interface LpBp2w {
        companion object {
            const val EventLpBp2wInfo = "com.lepu.ble.lp.bp2w.info"                                  // 设备信息 com.lepu.blepro.ext.lpbp2w.LpBp2wConfig
            const val EventLpBp2wRtData = "com.lepu.ble.lp.bp2w.rtData"                              // 实时数据 com.lepu.blepro.ext.lpbp2w.RtData
            const val EventLpBp2wUserFileList = "com.lepu.ble.lp.bp2w.user.fileList"                 // 用户文件列表 ArrayList<UserInfo>
            const val EventLpBp2wBpFileList = "com.lepu.ble.lp.bp2w.bp.fileList"                     // 血压文件列表 ArrayList<BpRecord>
            const val EventLpBp2wEcgFileList = "com.lepu.ble.lp.bp2w.ecg.fileList"                   // 心电文件列表 ArrayList<EcgRecord>
            const val EventLpBp2wReadFileError = "com.lepu.ble.lp.bp2w.read.file.error"              // 读文件出错 String(fileName)
            const val EventLpBp2wReadingFileProgress = "com.lepu.ble.lp.bp2w.reading.file.progress"  // 传输文件进度 int(0-100)
            const val EventLpBp2wReadFileComplete = "com.lepu.ble.lp.bp2w.read.file.complete"        // 传输文件完成 com.lepu.blepro.ext.lpbp2w.EcgFile
            const val EventLpBp2WriteFileError = "com.lepu.ble.lp.bp2w.write.file.error"             // 写文件出错 String(fileName)
            const val EventLpBp2WriteFileComplete = "com.lepu.ble.lp.bp2w.write.file.complete"       // 写文件完成 com.lepu.blepro.ext.lpbp2w.FileListCrc
            const val EventLpBp2WritingFileProgress = "com.lepu.ble.lp.bp2w.writing.file.progress"   // 写文件进度 int(0-100)
            const val EventLpBp2wReset = "com.lepu.ble.lp.bp2w.reset"                                // 复位 boolean
            const val EventLpBp2wFactoryReset = "com.lepu.ble.lp.bp2w.factory.reset"                 // 恢复出厂设置 boolean
            const val EventLpBp2wFactoryResetAll = "com.lepu.ble.lp.bp2w.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventLpBp2wSetConfig = "com.lepu.ble.lp.bp2w.set.config"                       // 设置参数 boolean
            const val EventLpBp2wGetConfig = "com.lepu.ble.lp.bp2w.get.config"                       // 获取参数 com.lepu.blepro.ext.lpbp2w.LpBp2wConfig
            const val EventLpBp2wSyncUtcTime = "com.lepu.ble.lp.bp2w.sync.utc.time"                  // 同步UTC时间（带时区） boolean
            const val EventLpBp2WifiList = "com.lepu.ble.lp.bp2w.wifi.list"                          // 获取路由
            const val EventLpBp2WifiScanning = "com.lepu.ble.lp.bp2w.wifi.scanning"                  // 正在扫描路由 boolean
            const val EventLpBp2wGetWifiConfig = "com.lepu.ble.lp.bp2w.get.wifi.config"              // 获取WiFi配置
            const val EventLpBp2wSetWifiConfig = "com.lepu.ble.lp.bp2w.set.wifi.config"              // 设置WiFi boolean
            const val EventLpBp2wGetFileListCrc = "com.lepu.ble.lp.bp2w.get.file.list.crc"           // 获取文件列表CRC com.lepu.blepro.ext.lpbp2w.FileListCrc
            const val EventLpBp2wDeleteFile = "com.lepu.ble.lp.bp2w.delete.file"                     // 删除文件 boolean
            const val EventLeBp2wGetWifiVersion = "com.lepu.ble.le.bp2w.get.wifi.version"            // 获取WiFi固件版本 String
        }
    }

    /**
     * Er2BleInterface 发出的通知
     * 包含model: MODEL_ER2(ER2 xxxx), MODEL_LP_ER2(LP ER2 xxxx)
     *           MODEL_DUOEK(DuoEK xxxx), MODEL_HHM2(HHM2 xxxx), MODEL_HHM3(HHM3 xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.er2GetInfo()
     * 2.获取实时心电数据：BleServiceHelper.startRtTask()/BleServiceHelper.stopRtTask()
     * 3.获取文件列表：BleServiceHelper.er2GetFileList()
     * 4.读取设备文件：BleServiceHelper.er2ReadFile()
     * 5.设置/获取配置信息：BleServiceHelper.er2SetConfig()
     * 6.复位：BleServiceHelper.er2Reset()
     * 7.恢复出厂设置：BleServiceHelper.er2FactoryReset()
     * 8.恢复生产出厂状态：BleServiceHelper.er2FactoryResetAll()
     */
    interface ER2 {
        companion object {
            const val EventEr2Info = "com.lepu.ble.er2.info"                                  // 设备信息 com.lepu.blepro.ext.er2.DeviceInfo
            const val EventEr2SetTime = "com.lepu.ble.er2.set.time"                           // 同步时间 boolean
            const val EventEr2SetConfig = "com.lepu.ble.er2.set.config"                       // 设置心跳音 boolean
            const val EventEr2GetConfig = "com.lepu.ble.er2.get.config"                       // 获取配置参数 com.lepu.blepro.ext.er2.Er2Config
            const val EventEr2GetConfigError = "com.lepu.ble.er2.get.config.error"            // 获取配置参数失败 boolean
            const val EventEr2Reset = "com.lepu.ble.er2.reset"                                // 复位 boolean
            const val EventEr2FactoryReset = "com.lepu.ble.er2.factory.reset"                 // 恢复出厂设置 boolean
            const val EventEr2FactoryResetAll = "com.lepu.ble.er2.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventEr2RtData = "com.lepu.ble.er2.realtime.data"                       // 实时数据 com.lepu.blepro.ext.er2.RtData
            const val EventEr2FileList = "com.lepu.ble.er2.file.list"                         // 文件列表 ArrayList<String>
            const val EventEr2ReadFileError = "com.lepu.ble.er2.file.read.error"              // 传输文件出错 boolean
            const val EventEr2ReadingFileProgress = "com.lepu.ble.er2.file.reading.progress"  // 传输文件进度 int(0-100)
            const val EventEr2ReadFileComplete = "com.lepu.ble.er2.file.read.complete"        // 传输文件完成 com.lepu.blepro.ext.er2.Er2File
        }
    }

    /**
     * PC60FwBleInterface 发出的通知
     * 包含model: MODEL_PC60FW(PC-60F_SNxxxxxx), MODEL_PC66B(PC-66B:xxxx), MODEL_OXYSMART(OxySmart xxxx),
     *           MODEL_POD_1W(POD-1_SNxxxx、POD-1_SNxxxxxx), MODEL_POD2B(POD-2B_SNxxxx),
     *           MODEL_PF_10(PF-10_xxxx), MODEL_PF_10AW(PF-10AW_xxxx), MODEL_PF_10AW1(PF-10AW1_xxxx),
     *           MODEL_PF_10BW(PF-10BW_xxxx), MODEL_PF_10BW1(PF-10BW1_xxxx),
     *           MODEL_PF_20(PF-20_xxxx), MODEL_PF_20AW(PF-20AW_xxxx), MODEL_PF_20B(PF-20B_xxxx),
     *           MODEL_PC_60NW(PC-60NW_SNxxxxxx), MODEL_PC_60NW_1(PC-60NW-1_SNxxxx、PC-60NW-1_SNxxxxxx),
     *           MODEL_S5W(S5W_SNxxxxxx), MODEL_S6W(S6W_xxxx), MODEL_S7W(S7W_xxxx), MODEL_S7BW(S7BW_xxxx),
     *           MODEL_S6W1(S6W1_xxxx), MODEL_PC60NW_BLE(PC-60NW_BLE), MODEL_PC60NW_WPS(PC-60NW-WPS),
     *           MODEL_PC_60NW_NO_SN(PC-60NW)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.pc60fwGetInfo()
     * 2.实时血氧数据：设备测量时自动上发
     * 3.工作状态数据（部分设备只有点测模式发送，部分设备不发送）
     * 4.电量信息（部分设备不发送）
     */
    interface PC60Fw {
        companion object {
            const val EventPC60FwRtParam = "com.lepu.ble.pc60fw.rt.param"              // 血氧参数 com.lepu.blepro.ext.pc60fw.RtParam
            const val EventPC60FwRtWave = "com.lepu.ble.pc60fw.rt.wave"                // 血氧波形 com.lepu.blepro.ext.pc60fw.RtWave
            const val EventPC60FwBatLevel = "com.lepu.ble.pc60fw.bat.level"            // 电池电量 int(0-3)
            const val EventPC60FwDeviceInfo = "com.lepu.ble.pc60fw.device.info"        // 设备信息 com.lepu.blepro.ext.pc60fw.DeviceInfo
            const val EventPC60FwWorkingStatus = "com.lepu.ble.pc60fw.working.status"  // 工作状态 com.lepu.blepro.ext.pc60fw.WorkingStatus (Spot check mode)
        }
    }

    /**
     * PC80BleInterface 发出的通知
     * 包含model: MODEL_PC80B(PC80B), MODEL_PC80B_BLE(PC80B-BLE), MODEL_PC80B_BLE2(PC80B_BLE:)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.pc80bGetInfo()
     * 2.实时心电数据：设备测量时自动上发
     * 3.设备文件：设备上发
     * 4.电量信息：BleServiceHelper.pc80bGetBattery()
     */
    interface PC80B {
        companion object {
            const val EventPc80bBatLevel = "com.lepu.ble.pc80b.bat.level"                         // 电池电量 int(0-3)
            const val EventPc80bDeviceInfo = "com.lepu.ble.pc80b.device.info"                     // 设备信息 com.lepu.blepro.ext.pc80b.DeviceInfo
            const val EventPc80bFastData = "com.lepu.ble.pc80b.fast.data"                         // 实时数据 com.lepu.blepro.ext.pc80b.RtFastData
            const val EventPc80bContinuousData = "com.lepu.ble.pc80b.continuous.data"             // 连续实时数据 com.lepu.blepro.ext.pc80b.RtContinuousData
            const val EventPc80bContinuousDataEnd = "com.lepu.ble.pc80b.continuous.data.end"      // 连续实时结束 boolean
            const val EventPc80bReadFileError = "com.lepu.ble.pc80b.file.read.error"              // 传输文件出错 boolean
            const val EventPc80bReadingFileProgress = "com.lepu.ble.pc80b.file.reading.progress"  // 传输文件进度 int(0-100)
            const val EventPc80bReadFileComplete = "com.lepu.ble.pc80b.file.read.complete"        // 传输文件完成 com.lepu.blepro.ext.pc80b.EcgFile
        }
    }

    /**
     * PC100BleInterface 发出的通知
     * 包含model: MODEL_PC100(PC-100:xxxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.pc100GetInfo()
     * 2.开始/结束测量血压：BleServiceHelper.pc100StartBp()/BleServiceHelper.pc100StopBp()
     * 3.实时血压/血氧数据：设备测量时自动上发
     */
    interface PC100 {
        companion object {
            const val EventPc100DeviceInfo = "com.lepu.ble.pc100.device.info"         // 设备信息 com.lepu.blepro.ext.pc102.DeviceInfo
            const val EventPc100BpResult = "com.lepu.ble.pc100.bp.result"             // 血压测量结果 com.lepu.blepro.ext.pc102.BpResult
            const val EventPc100BpErrorResult = "com.lepu.ble.pc100.bp.error.result"  // 血压测量错误结果 com.lepu.blepro.ext.pc102.BpResultError
            const val EventPc100BpStart = "com.lepu.ble.pc100.bp.start"               // 血压开始测量 boolean
            const val EventPc100BpStop = "com.lepu.ble.pc100.bp.stop"                 // 血压停止测量 boolean
            const val EventPc100RtBpData = "com.lepu.ble.pc100.rt.bp.data"            // 血压实时测量值 com.lepu.blepro.ext.pc102.RtBpData
            const val EventPc100RtOxyWave = "com.lepu.ble.pc100.rt.oxy.wave"          // 血氧实时波形包 com.lepu.blepro.ext.pc102.RtOxyWave
            const val EventPc100RtOxyParam = "com.lepu.ble.pc100.rt.oxy.param"        // 血氧实时测量值 com.lepu.blepro.ext.pc102.RtOxyParam
            const val EventPc100OxyFingerOut = "com.lepu.ble.pc100.oxy.finger.out"    // 血氧脱落未接入 boolean
        }
    }

    /**
     * Ap20BleInterface 发出的通知
     * 包含model: MODEL_AP20(AP-20:xxxxx), MODEL_AP20_WPS(AP-20-WPS)
     * 功能：
     * 1获取设备信息：BleServiceHelper.ap20GetInfo()
     * 2.获取电量信息：BleServiceHelper.ap20GetBattery()
     * 3.实时血氧/鼻息数据：设备测量时自动上发
     * 4.设置/获取配置信息：BleServiceHelper.ap20SetConfig()/BleServiceHelper.ap20GetConfig()
     */
    interface AP20 {
        companion object {
            const val EventAp20SetTime = "com.lepu.ble.ap20.set.time"               // 设置时间 boolean
            const val EventAp20DeviceInfo = "com.lepu.ble.ap20.device.info"         // 设备信息 com.lepu.blepro.ext.ap20.DeviceInfo
            const val EventAp20BatLevel = "com.lepu.ble.ap20.bat.level"             // 电池电量 int（0-3）
            const val EventAp20RtOxyWave = "com.lepu.ble.ap20.rt.oxy.wave"          // 血氧波形包数据 com.lepu.blepro.ext.ap20.RtOxyWave
            const val EventAp20RtOxyParam = "com.lepu.ble.ap20.rt.oxy.param"        // 血氧参数包数据 com.lepu.blepro.ext.ap20.RtOxyParam
            const val EventAp20RtBreathWave = "com.lepu.ble.ap20.rt.breath.wave"    // 鼻息流波形包数据 com.lepu.blepro.ext.ap20.RtBreathWave
            const val EventAp20RtBreathParam = "com.lepu.ble.ap20.rt.breath.param"  // 鼻息流参数包数据 com.lepu.blepro.ext.ap20.RtBreathParam
            /**
             * type : 0 背光等级（0-5）
             *        1 警报功能开关（0 off，1 on）
             *        2 血氧过低阈值（85-99）
             *        3 脉率过低阈值（30-99）
             *        4 脉率过高阈值（100-250）
             */
            const val EventAp20GetConfigResult = "com.lepu.ble.ap20.get.config.result"   // 获取参数 com.lepu.blepro.ext.ap20.GetConfigResult
            const val EventAp20SetConfigResult = "com.lepu.ble.ap20.set.config.result"   // 配置参数 com.lepu.blepro.ext.ap20.SetConfigResult
        }
    }

    /**
     * VetcorderBleInterface 发出的通知
     * 包含model: MODEL_VETCORDER(Vetcorder), MODEL_CHECK_ADV(CheckADV xxxx)
     * 功能：
     * 1.实时心电/血氧数据：设备测量时自动上发
     */
    interface CheckmeMonitor {
        companion object {
            const val EventCheckmeMonitorRtData = "com.lepu.ble.checkme.monitor.rtdata"  // com.lepu.blepro.ext.checkmemonitor.RtData
        }
    }

    /**
     * Sp20BleInterface 发出的通知
     * 包含model: MODEL_SP20(SP-20:xxxxx), MODEL_SP20_BLE(SP-20-BLE:xxxxx), MODEL_SP20_WPS(SP-20-WPS:xxxxx),
     *           MODEL_SP20_NO_SN(SP-20), MODEL_SP20_WPS_NO_SN(SP-20-WPS)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.sp20GetInfo()
     * 2.获取电量信息：BleServiceHelper.sp20GetBattery()
     * 3.实时血氧数据：设备测量时自动上发
     * 4.设置/获取配置信息：BleServiceHelper.sp20SetConfig()/BleServiceHelper.sp20GetConfig()
     */
    interface SP20 {
        companion object {
            const val EventSp20SetTime = "com.lepu.ble.sp20.set.time"        // 设置时间 boolean
            const val EventSp20DeviceInfo = "com.lepu.ble.sp20.device.info"  // 设备信息 com.lepu.blepro.ext.sp20.DeviceInfo
            const val EventSp20Battery = "com.lepu.ble.sp20.battery"         // 电池电量 int(0-3)
            const val EventSp20RtWave = "com.lepu.ble.sp20.rtwave"           // 血氧波形包数据 com.lepu.blepro.ext.sp20.RtWave
            const val EventSp20RtParam = "com.lepu.ble.sp20.rtparam"         // 血氧参数包数据 com.lepu.blepro.ext.sp20.RtParam
            const val EventSp20TempData = "com.lepu.ble.sp20.temp.data"      // 体温数据 com.lepu.blepro.ext.sp20.TempResult
            /**
             * type :
             *        2 血氧过低阈值（value：85-99）
             *        3 脉率过低阈值（value：30-99）
             *        4 脉率过高阈值（value：100-250）
             */
            const val EventSp20GetConfig = "com.lepu.ble.sp20.get.config"          // 获取配置信息 com.lepu.blepro.ext.sp20.GetConfigResult
            const val EventSp20SetConfig = "com.lepu.ble.sp20.set.config.success"  // 配置信息 com.lepu.blepro.ext.sp20.SetConfigResult
        }
    }

    /**
     * Vtm20fBleInterface 发出的通知
     * 包含model: MODEL_TV221U(VTM 20F)
     * 功能：
     * 1.实时血氧数据：设备测量完成自动上发
     */
    interface VTM20f {
        companion object {
            const val EventVTM20fRtWave = "com.lepu.ble.vtm20f.rtwave"           // 血氧波形包数据 com.lepu.blepro.ext.vtm20f.RtWave
            const val EventVTM20fRtParam = "com.lepu.ble.vtm20f.rtparam"         // 血氧参数包数据 com.lepu.blepro.ext.vtm20f.RtParam
        }
    }

    /**
     * Aoj20aBleInterface 发出的通知
     * 包含model: MODEL_AOJ20A(AOJ-20A)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.aoj20aGetInfo()
     * 2.获取历史数据：BleServiceHelper.aoj20aGetFileList()
     * 3.实时体温数据：设备测量完成自动上发
     * 4.删除数据：BleServiceHelper.aoj20aDeleteData()
     */
    interface AOJ20a {
        companion object {
            const val EventAOJ20aTempRtData = "com.lepu.ble.aoj20a.temp.rtdata"         // 实时测温数据 com.lepu.blepro.ext.aoj20a.TempResult
            const val EventAOJ20aSetTime = "com.lepu.ble.aoj20a.set.time"               // 同步时间 boolean
            const val EventAOJ20aTempList = "com.lepu.ble.aoj20a.temp.list"             // 历史测量数据 ArrayList<Record>
            const val EventAOJ20aDeviceData = "com.lepu.ble.aoj20a.device.data"         // 设备数据 com.lepu.blepro.ext.aoj20a.DeviceInfo
            const val EventAOJ20aTempErrorMsg = "com.lepu.ble.aoj20a.temp.error.msg"    // 错误码数据 com.lepu.blepro.ext.aoj20a.ErrorResult
            const val EventAOJ20aDeleteData = "com.lepu.ble.aoj20a.delete.data"         // 删除历史数据 boolean
        }
    }

    /**
     * CheckmePodBleInterface 发出的通知
     * 包含model: MODEL_CHECK_POD(Checkme Pod xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.checkmePodGetInfo()
     * 2.获取文件列表：BleServiceHelper.checkmePodGetFileList()
     * 3.获取实时血氧、体温数据：BleServiceHelper.startRtTask()/BleServiceHelper.stopRtTask()
     */
    interface CheckmePod {
        companion object {
            const val EventCheckmePodSetTime = "com.lepu.ble.checkme.pod.set.time"                            // 同步时间 boolean
            const val EventCheckmePodDeviceInfo = "com.lepu.ble.checkme.pod.device.info"                      // 设备信息 DeviceInfo
            const val EventCheckmePodRtData = "com.lepu.ble.checkme.pod.realtime.data"                        // 实时数据 RtData
            const val EventCheckmePodRtDataError = "com.lepu.ble.checkme.pod.realtime.data.error"             // 实时数据出错 boolean
            const val EventCheckmePodFileList = "com.lepu.ble.checkme.pod.file.list"                          // 文件列表 ArrayList<Record>
            const val EventCheckmePodGetFileListError = "com.lepu.ble.checkme.pod.get.file.list.error"        // 获取文件列表出错 boolean
            const val EventCheckmePodGetFileListProgress = "com.lepu.ble.checkme.pod.get.file.list.progress"  // 获取文件列表进度 int(0-100)
        }
    }

    /**
     * PulsebitBleInterface 发出的通知
     * 包含model: MODEL_PULSEBITEX(Pulsebit xxxx), MODEL_HHM4(HHM4 xxxx), MODEL_CHECKME(Checkme xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.pulsebitExGetInfo()
     * 2.获取文件列表：BleServiceHelper.pulsebitExGetFileList()
     * 3.读取设备文件：BleServiceHelper.pulsebitExReadFile()
     */
    interface Pulsebit {
        companion object {
            const val EventPulsebitSetTime = "com.lepu.ble.pulsebit.set.time"                            // 同步时间 boolean
            const val EventPulsebitDeviceInfo = "com.lepu.ble.pulsebit.device.info"                      // 设备信息 com.lepu.blepro.ext.pulsebit.DeviceInfo
            const val EventPulsebitGetFileListProgress = "com.lepu.ble.pulsebit.get.file.list.progress"  // 获取文件列表进度 int(0-100)
            const val EventPulsebitGetFileList = "com.lepu.ble.pulsebit.get.file.list"                   // 文件列表 ArrayList<String>
            const val EventPulsebitGetFileListError = "com.lepu.ble.pulsebit.get.file.list.error"        // 获取文件列表出错 boolean
            const val EventPulsebitReadingFileProgress = "com.lepu.ble.pulsebit.reading.file.progress"   // 获取文件进度 int(0-100)
            const val EventPulsebitReadFileComplete = "com.lepu.ble.pulsebit.read.file.complete"         // 获取文件完成 com.lepu.blepro.ext.pulsebit.EcgFile
            const val EventPulsebitReadFileError = "com.lepu.ble.pulsebit.read.file.error"               // 获取文件出错 boolean
        }
    }

    /**
     * CheckmeBleInterface 发出的通知
     * 包含model: MODEL_CHECKME
     */
    interface Checkme {
        companion object {
            const val EventCheckmeSetTime = "com.lepu.ble.checkme.set.time"                            // 同步时间 boolean
            const val EventCheckmeDeviceInfo = "com.lepu.ble.checkme.device.info"                      // 设备信息 CheckmeBleResponse.DeviceInfo
            const val EventCheckmeRtData = "com.lepu.ble.checkme.rt.data"                              // 实时数据 CheckmeBleResponse.RtData
            const val EventCheckmeGetFileListProgress = "com.lepu.ble.checkme.get.file.list.progress"  // 获取文件列表进度 int
            const val EventCheckmeGetFileList = "com.lepu.ble.checkme.get.file.list"                   // 文件列表 CheckmeBleResponse.ListContent
            const val EventCheckmeGetFileListError = "com.lepu.ble.checkme.get.file.list.error"        // 获取文件列表出错 boolean
            const val EventCheckmeReadingFileProgress = "com.lepu.ble.checkme.reading.file.progress"   // 获取文件进度 int
            const val EventCheckmeReadFileComplete = "com.lepu.ble.checkme.read.file.complete"         // 获取文件完成 CheckmeBleResponse.FileContent
            const val EventCheckmeReadFileError = "com.lepu.ble.checkme.read.file.error"               // 获取文件出错 boolean
        }
    }

    /**
     * Pc68bBleInterface 发出的通知
     * 包含model: MODEL_PC_68B(PC-68B)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.pc68bGetInfo()
     * 2.实时血氧数据：设备测量时自动上发
     */
    interface PC68B {
        companion object {
            const val EventPc68bDeviceInfo = "com.lepu.ble.pc68b.device.info"               // 设备信息 com.lepu.blepro.ext.pc68b.DeviceInfo（定制版有sn，通用版没有sn）
            const val EventPc68bRtWave = "com.lepu.ble.pc68b.rtwave"                        // 血氧波形包数据 com.lepu.blepro.ext.pc68b.RtWave
            const val EventPc68bRtParam = "com.lepu.ble.pc68b.rtparam"                      // 血氧参数包数据 com.lepu.blepro.ext.pc68b.RtParam
        }
    }

    /**
     * PC300BleInterface 发出的通知
     * 包含model: MODEL_PC300(PC_300SNT), MODEL_PC300_BLE(PC_300SNT-BLE)
     *           MODEL_PC200_BLE(PC-200-BLE), MODEL_GM_300SNT(GM_300SNT, GM_300SNT-BLE),
     *           MODEL_CMI_PC303(CMI_PC303, CMI_PC303-BLE)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.pc300GetInfo()
     * 2.实时心电/血压/血氧/血糖/体温数据：设备测量时自动上发
     */
    interface PC300 {
        companion object {
            const val EventPc300DeviceInfo = "com.lepu.ble.pc300.device.info"                 // 设备信息 com.lepu.blepro.ext.pc303.DeviceInfo
            const val EventPc300BpStart = "com.lepu.ble.pc300.bp.start"                       // 血压开始测量 boolean
            const val EventPc300BpStop = "com.lepu.ble.pc300.bp.stop"                         // 血压停止测量 boolean
            const val EventPc300BpResult = "com.lepu.ble.pc300.bp.result"                     // 血压测量结果 com.lepu.blepro.ext.pc303.BpResult
            const val EventPc300BpErrorResult = "com.lepu.ble.pc300.bp.error.result"          // 血压测量错误结果 com.lepu.blepro.ext.pc303.BpResultError
            const val EventPc300RtBpData = "com.lepu.ble.pc300.bp.rtdata"                     // 血压实时测量值 int
            const val EventPc300RtOxyWave = "com.lepu.ble.pc300.oxy.rtwave"                   // 血氧实时波形包 com.lepu.blepro.ext.pc303.RtOxyWave
            const val EventPc300RtOxyParam = "com.lepu.ble.pc300.oxy.rtparam"                 // 血氧实时参数包 com.lepu.blepro.ext.pc303.RtOxyParam
            const val EventPc300EcgStart = "com.lepu.ble.pc300.ecg.start"                     // 心电开始测量 boolean
            const val EventPc300EcgStop = "com.lepu.ble.pc300.ecg.stop"                       // 心电停止测量 boolean
            const val EventPc300RtEcgWave = "com.lepu.ble.pc300.ecg.rtwave"                   // 心电实时波形包 com.lepu.blepro.ext.pc303.RtEcgWave
            const val EventPc300EcgResult = "com.lepu.ble.pc300.ecg.result"                   // 心电结果 com.lepu.blepro.ext.pc303.EcgResult
            const val EventPc300GluResult = "com.lepu.ble.pc300.glu.result"                   // 血糖结果 com.lepu.blepro.ext.pc303.GluResult
            const val EventPc300UaResult = "com.lepu.ble.pc300.ua.result"                     // 尿酸结果 float
            const val EventPc300CholResult = "com.lepu.ble.pc300.chol.result"                 // 总胆固醇结果 int
            const val EventPc300TempResult = "com.lepu.ble.pc300.temp.result"                 // 温度结果 float
            const val EventPc300GetGlucometerType = "com.lepu.ble.pc300.get.glucometer.type"  // 获取血糖仪类型 int
            const val EventPc300SetGlucometerType = "com.lepu.ble.pc300.set.glucometer.type"  // 设置血糖仪类型 boolean
        }
    }

    /**
     * CheckmeLeInterface 发出的通知
     * 包含model: MODEL_CHECKME_LE(CheckmeLE xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.checkmeLeGetInfo()
     * 2.获取日测列表：BleServiceHelper.checkmeLeGetFileList(Constant.CheckmeLeListType.DLC_TYPE)
     * 3.获取血氧列表：BleServiceHelper.checkmeLeGetFileList(Constant.CheckmeLeListType.OXY_TYPE)
     * 4.获取体温列表：BleServiceHelper.checkmeLeGetFileList(Constant.CheckmeLeListType.TEMP_TYPE)
     * 5.获取心电列表：BleServiceHelper.checkmeLeGetFileList(Constant.CheckmeLeListType.ECG_TYPE)
     * 6.获取心电文件：BleServiceHelper.checkmeLeReadFile()
     */
    interface CheckmeLE {
        companion object {
            const val EventCheckmeLeSetTime = "com.lepu.ble.checkmele.set.time"                            // 同步时间 boolean
            const val EventCheckmeLeDeviceInfo = "com.lepu.ble.checkmele.device.info"                      // 设备信息 com.lepu.blepro.ext.checkmele.DeviceInfo
            const val EventCheckmeLeGetFileListProgress = "com.lepu.ble.checkmele.get.file.list.progress"  // 获取文件列表进度 int(0-100)
            const val EventCheckmeLeEcgList = "com.lepu.ble.checkmele.ecg.list"                            // 心电文件列表 ArrayList<EcgRecord>
            const val EventCheckmeLeOxyList = "com.lepu.ble.checkmele.oxy.list"                            // 血氧文件列表 ArrayList<OxyRecord>
            const val EventCheckmeLeDlcList = "com.lepu.ble.checkmele.dlc.list"                            // 日测文件列表 ArrayList<DlcRecord>
            const val EventCheckmeLeTempList = "com.lepu.ble.checkmele.temp.list"                          // 体温文件列表 ArrayList<TempRecord>
            const val EventCheckmeLeGetFileListError = "com.lepu.ble.checkmele.get.file.list.error"        // 获取文件列表出错 boolean
            const val EventCheckmeLeReadingFileProgress = "com.lepu.ble.checkmele.reading.file.progress"   // 获取文件进度 int(0-100)
            const val EventCheckmeLeReadFileComplete = "com.lepu.ble.checkmele.read.file.complete"         // 获取文件完成 com.lepu.blepro.ext.checkmele..EcgFile
            const val EventCheckmeLeReadFileError = "com.lepu.ble.checkmele.read.file.error"               // 获取文件出错 boolean
        }
    }

    /**
     * LemBleInterface 发出的通知
     * 包含model: MODEL_LEM(LEM1 xxxx)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.lemGetInfo()
     * 2.获取电量信息：BleServiceHelper.lemGetBattery()
     * 3.设置加热模式：BleServiceHelper.lemHeatMode()
     * 4.设置按摩模式：BleServiceHelper.lemMassageMode()
     * 5.设置按摩力度：BleServiceHelper.lemMassageLevel()
     * 6.设置按摩时间：BleServiceHelper.lemMassageTime()
     */
    interface LEM {
        companion object {
            const val EventLemDeviceInfo = "com.lepu.ble.lem.device.info"             // com.lepu.blepro.ext.LemData
            const val EventLemBattery = "com.lepu.ble.lem.battery"                    // int 1-100%
            const val EventLemSetHeatMode = "com.lepu.ble.lem.set.heat.mode"          // boolean (true设置开成功，false设置关成功)
            const val EventLemSetMassageMode = "com.lepu.ble.lem.set.massage.mode"    // int (LemBleCmd.MassageMode) 设置成功
            const val EventLemSetMassageLevel = "com.lepu.ble.lem.set.massage.level"  // int (1-15，0关闭) 设置成功
            const val EventLemSetMassageTime = "com.lepu.ble.lem.set.massage.time"    // int (LemBleCmd.MassageTime) 设置成功
        }
    }

    /**
     * Lpm311BleInterface 发出的通知
     * 包含model: MODEL_LPM311(LPM311)
     * 功能：
     * 1.获取血脂数据：BleServiceHelper.lpm311GetData()
     */
    interface LPM311 {
        companion object {
            const val EventLpm311Data = "com.lepu.ble.lpm311.data"  // 血脂数据 com.lepu.blepro.ext.Lpm311Data
        }
    }

    /**
     * PoctorM3102BleInterface 发出的通知
     * 包含model: MODEL_POCTOR_M3102(PoctorM3102)
     * 功能：
     * 1.实时血糖/尿酸/血酮数据：：设备测量完成自动上发
     */
    interface PoctorM3102 {
        companion object {
            const val EventPoctorM3102Data = "com.lepu.ble.poctor.m3102.data"  // 测量结果 com.lepu.blepro.ext.PoctorM3102Data
        }
    }

    /**
     * BiolandBgmBleInterface 发出的通知
     * 包含model: MODEL_BIOLAND_BGM(Bioland-BGM)
     * 功能：
     * 1.获取设备信息：BleServiceHelper.biolandBgmGetInfo()
     * 2.获取历史数据（最新一条）：BleServiceHelper.biolandBgmGetGluData()
     */
    interface BiolandBgm {
        companion object {
            const val EventBiolandBgmDeviceInfo = "com.lepu.ble.bioland.bgm.device.info"       // com.lepu.blepro.ext.bioland.DeviceInfo
            const val EventBiolandBgmCountDown = "com.lepu.ble.bioland.bgm.count.down"         // int
            const val EventBiolandBgmGluData = "com.lepu.ble.bioland.bgm.glu.data"             // com.lepu.blepro.ext.bioland.GluData
            const val EventBiolandBgmNoGluData = "com.lepu.ble.bioland.bgm.no.glu.data"        // boolean
        }
    }

    /**
     * Er3BleInterface 发出的通知
     * 包含model: MODEL_ER3
     * 功能：
     * 1.获取设备信息：BleServiceHelper.er3GetInfo()
     * 2.获取实时数据：BleServiceHelper.startRtTask()/BleServiceHelper.stopRtTask()
     * 3.设置/获取配置信息：BleServiceHelper.er3SetConfig()/BleServiceHelper.er3GetConfig()
     * 4.复位：BleServiceHelper.er3Reset()
     * 5.恢复出厂设置：BleServiceHelper.er3FactoryReset()
     * 6.恢复生产出厂状态：BleServiceHelper.er3FactoryResetAll()
     */
    interface ER3 {
        companion object {
            const val EventEr3Info = "com.lepu.ble.er3.info"                                  // 设备信息 com.lepu.blepro.ext.er3.DeviceInfo
            const val EventEr3RtData = "com.lepu.ble.er3.rtData"                              // 实时数据 com.lepu.blepro.ext.er3.RtData
            const val EventEr3Reset = "com.lepu.ble.er3.reset"                                // 复位 boolean
            const val EventEr3FactoryReset = "com.lepu.ble.er3.factory.reset"                 // 恢复出厂设置 boolean
            const val EventEr3FactoryResetAll = "com.lepu.ble.er3.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventEr3GetConfig = "com.lepu.ble.er3.get.config"                       // 获取配置参数 int
            const val EventEr3GetConfigError = "com.lepu.ble.er3.get.config.error"            // 获取配置参数失败 boolean
            const val EventEr3SetConfig = "com.lepu.ble.er3.set.config"                       // 设置模式 boolean
            const val EventEr3SetTime = "com.lepu.ble.er3.set.time"                           // 同步时间 boolean
            const val EventEr3EcgStop = "com.lepu.ble.er3.ecg.stop"                           // 退出测量模式 boolean
        }
    }

    /**
     * LepodBleInterface 发出的通知
     * 包含model: MODEL_LEPOD
     * 功能：
     * 1.获取设备信息：BleServiceHelper.lepodGetInfo()
     * 2.获取实时数据：BleServiceHelper.startRtTask()/BleServiceHelper.stopRtTask()
     * 3.设置/获取配置信息：BleServiceHelper.lepodSetConfig()/BleServiceHelper.lepodGetConfig()
     * 4.复位：BleServiceHelper.lepodReset()
     * 5.恢复出厂设置：BleServiceHelper.lepodFactoryReset()
     * 6.恢复生产出厂状态：BleServiceHelper.lepodFactoryResetAll()
     * 7.开始/结束测量：BleServiceHelper.lepodStartEcg()/BleServiceHelper.lepodStopEcg()
     */
    interface Lepod {
        companion object {
            const val EventLepodInfo = "com.lepu.ble.lepod.info"                                  // 设备信息 com.lepu.blepro.ext.lepod.DeviceInfo
            const val EventLepodRtData = "com.lepu.ble.lepod.rtData"                              // 实时数据 com.lepu.blepro.ext.lepod.RtData
            const val EventLepodReset = "com.lepu.ble.lepod.reset"                                // 复位 boolean
            const val EventLepodFactoryReset = "com.lepu.ble.lepod.factory.reset"                 // 恢复出厂设置 boolean
            const val EventLepodFactoryResetAll = "com.lepu.ble.lepod.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventLepodGetConfig = "com.lepu.ble.lepod.get.config"                       // 获取配置参数 int
            const val EventLepodGetConfigError = "com.lepu.ble.lepod.get.config.error"            // 获取配置参数失败 boolean
            const val EventLepodSetConfig = "com.lepu.ble.lepod.set.config"                       // 设置模式 boolean
            const val EventLepodSetTime = "com.lepu.ble.lepod.set.time"                           // 同步时间 boolean
            const val EventLepodEcgStart = "com.lepu.ble.lepod.ecg.start"                         // 开始测量 boolean
            const val EventLepodEcgStop = "com.lepu.ble.lepod.ecg.stop"                           // 结束测量 boolean
        }
    }

    /**
     * FhrBleInterface 发出的通知
     * 包含model: MODEL_FHR
     */
    interface FHR {
        companion object {
            const val EventFhrDeviceInfo = "com.lepu.ble.fhr.device.info"  // 设备信息 FhrBleResponse.DeviceInfo
            const val EventFhrAudioData = "com.lepu.ble.fhr.audio.data"    // 音频数据 byte数组
        }
    }

    /**
     * Bpw1BleInterface 发出的通知
     * 包含model: MODEL_BPW1
     */
    interface BPW1 {
        companion object {
            const val EventBpw1SetTime = "com.lepu.ble.bpw1.set.time"                            // 同步时间 true
            const val EventBpw1SetMeasureTime = "com.lepu.ble.bpw1.set.measure.time"             // 设置定时测量时间 true
            const val EventBpw1SetTimingSwitch = "com.lepu.ble.bpw1.set.timing.switch"           // 设置定时测量开关 true
            const val EventBpw1DeviceInfo = "com.lepu.ble.bpw1.device.info"                      // 设备信息 Bpw1BleResponse.DeviceInfo
            const val EventBpw1MeasureState = "com.lepu.ble.bpw1.measure.state"                  // 测量状态 int(1 开始 2 停止)
            const val EventBpw1RtData = "com.lepu.ble.bpw1.rt.data"                              // 实时数据 Bpw1BleResponse.RtData
            const val EventBpw1ErrorResult = "com.lepu.ble.bpw1.error.result"                    // 测量出错 Bpw1BleResponse.ErrorResult
            const val EventBpw1MeasureResult = "com.lepu.ble.bpw1.measure.result"                // 测量结果 Bpw1BleResponse.BpData
            const val EventBpw1GetFileListComplete = "com.lepu.ble.bpw1.get.file.list.complete"  // 传输文件完成 Bpw1BleResponse.BpData
            const val EventBpw1GetMeasureTime = "com.lepu.ble.bpw1.get.measure.time"             // 获取定时测量血压时间 Bpw1BleResponse.MeasureTime
        }
    }
    /**
     * LewBleInterface 发出的通知
     * 包含model: MODEL_LEW, MODEL_W12C
     */
    interface Lew {
        companion object {
            const val EventLewDeviceInfo = "com.lepu.ble.lew.device.info"                       // 设备信息 DeviceInfo
            const val EventLewBatteryInfo = "com.lepu.ble.lew.battery.info"                     // 电量信息 BatteryInfo
            const val EventLewSetTime = "com.lepu.ble.lew.set.time"                             // 同步时间 true
            const val EventLewGetTime = "com.lepu.ble.lew.get.time"                             // 同步时间 TimeData
            const val EventLewBoundDevice = "com.lepu.ble.lew.bound.device"                     // 请求绑定设备 boolean
            const val EventLewUnBoundDevice = "com.lepu.ble.lew.unbound.device"                 // 解绑设备
            const val EventLewFindPhone = "com.lepu.ble.lew.find.phone"                         // 找手机 boolean（true打开查找，false关闭查找）
            const val EventLewGetDeviceNetwork = "com.lepu.ble.lew.get.device.network"          // 获取设备联网模式 DeviceNetwork
            const val EventLewGetSystemSetting = "com.lepu.ble.lew.get.system.setting"          // 获取系统配置 SystemSetting（包含语言、单位、翻腕、左右手）
            const val EventLewSetSystemSetting = "com.lepu.ble.lew.set.system.setting"          // 设置系统配置（包含语言、单位、翻腕、左右手）
            const val EventLewGetLanguageSetting = "com.lepu.ble.lew.get.language.setting"      // 获取语言配置 LewBleCmd.Language
            const val EventLewSetLanguageSetting = "com.lepu.ble.lew.set.language.setting"      // 设置语言配置
            const val EventLewGetUnitSetting = "com.lepu.ble.lew.get.unit.setting"              // 获取单位配置 UnitSetting
            const val EventLewSetUnitSetting = "com.lepu.ble.lew.set.unit.setting"              // 设置单位配置
            const val EventLewGetHandRaiseSetting = "com.lepu.ble.lew.get.hand.raise.setting"   // 获取翻腕亮屏配置 HandRaiseSetting
            const val EventLewSetHandRaiseSetting = "com.lepu.ble.lew.set.hand.raise.setting"   // 设置翻腕亮屏配置
            const val EventLewGetLrHandSetting = "com.lepu.ble.lew.get.lrhand.setting"          // 获取左右手配置 LewBleCmd.Hand
            const val EventLewSetLrHandSetting = "com.lepu.ble.lew.set.lrhand.setting"          // 设置左右手配置

            const val EventLewGetNoDisturbMode = "com.lepu.ble.lew.get.no.disturb.mode"         // 获取勿扰模式 NoDisturbMode
            const val EventLewSetNoDisturbMode = "com.lepu.ble.lew.set.no.disturb.mode"         // 设置勿扰模式
            const val EventLewGetAppSwitch = "com.lepu.ble.lew.get.app.switch"                  // 获取App消息通知开关 AppSwitch
            const val EventLewSetAppSwitch = "com.lepu.ble.lew.set.app.switch"                  // 设置App消息通知开关
            const val EventLewSendNotification = "com.lepu.ble.lew.send.notification"           // 发送消息通知
            const val EventLewGetDeviceMode = "com.lepu.ble.lew.get.device.mode"                // 获取设备模式 LewBleCmd.DeviceMode
            const val EventLewSetDeviceMode = "com.lepu.ble.lew.set.device.mode"                // 设置设备模式
            const val EventLewGetAlarmClock = "com.lepu.ble.lew.get.alarm.clock"                // 获取闹钟 AlarmClockInfo
            const val EventLewSetAlarmClock = "com.lepu.ble.lew.set.alarm.clock"                // 设置闹钟
            const val EventLewGetPhoneSwitch = "com.lepu.ble.lew.get.phone.switch"              // 获取手机通知开关 PhoneSwitch（短信、来电）
            const val EventLewSetPhoneSwitch = "com.lepu.ble.lew.set.phone.switch"              // 设置手机通知开关（短信、来电）
            const val EventLewGetMedicineRemind = "com.lepu.ble.lew.get.medicine.remind"        // 获取用药提醒 MedicineRemind
            const val EventLewSetMedicineRemind = "com.lepu.ble.lew.set.medicine.remind"        // 设置用药提醒
            const val EventLewPhoneCall = "com.lepu.ble.lew.phone.call"                         // 来电控制 true：挂断
            const val EventLewGetMeasureSetting = "com.lepu.ble.lew.get.measure.setting"        // 获取测量配置 MeasureSetting（运动目标值、达标提醒、久坐提醒、自测心率）
            const val EventLewSetMeasureSetting = "com.lepu.ble.lew.set.measure.setting"        // 设置测量配置（运动目标值、达标提醒、久坐提醒、自测心率）
            const val EventLewGetSportTarget = "com.lepu.ble.lew.get.sport.target"              // 获取运动目标值 SportTarget
            const val EventLewSetSportTarget = "com.lepu.ble.lew.set.sport.target"              // 设置运动目标值
            const val EventLewGetTargetRemind = "com.lepu.ble.lew.get.target.remind"            // 获取达标提醒 TargetRemind
            const val EventLewSetTargetRemind = "com.lepu.ble.lew.set.target.remind"            // 设置达标提醒
            const val EventLewGetSittingRemind = "com.lepu.ble.lew.get.sitting.remind"          // 获取久坐提醒 SittingRemind
            const val EventLewSetSittingRemind = "com.lepu.ble.lew.set.sitting.remind"          // 设置久坐提醒
            const val EventLewGetHrDetect = "com.lepu.ble.lew.get.hr.detect"                    // 获取自测心率 HrDetect
            const val EventLewSetHrDetect = "com.lepu.ble.lew.set.hr.detect"                    // 设置自测心率
            const val EventLewGetOxyDetect = "com.lepu.ble.lew.get.oxy.detect"                  // 获取自测血氧 OxyDetect
            const val EventLewSetOxyDetect = "com.lepu.ble.lew.set.oxy.detect"                  // 设置自测血氧

            const val EventLewGetUserInfo = "com.lepu.ble.lew.get.user.info"                    // 获取用户信息 UserInfo
            const val EventLewSetUserInfo = "com.lepu.ble.lew.set.user.info"                    // 设置用户信息
            const val EventLewGetPhoneBook = "com.lepu.ble.lew.get.phone.book"                  // 获取通讯录 PhoneBook
            const val EventLewSetPhoneBook = "com.lepu.ble.lew.set.phone.book"                  // 同步通讯录
            const val EventLewGetSosContact = "com.lepu.ble.lew.get.sos.contact"                // 获取紧急联系人 SosContact
            const val EventLewSetSosContact = "com.lepu.ble.lew.set.sos.contact"                // 同步紧急联系人
            const val EventLewGetSecondScreen = "com.lepu.ble.lew.get.second.screen"            // 获取副屏配置信息 SecondScreen
            const val EventLewSetSecondScreen = "com.lepu.ble.lew.set.second.screen"            // 设置副屏
            const val EventLewGetCards = "com.lepu.ble.lew.get.cards"                           // 获取卡片配置信息 int[], LewBleCmd.Cards
            const val EventLewSetCards = "com.lepu.ble.lew.set.cards"                           // 编辑卡片

            /**
             * type: LewBleCmd.ListType.SPORT / ECG / HR / OXY / SLEEP
             * content: SportList, EcgList, HrList, OxyList, SleepList
             */
            const val EventLewFileList = "com.lepu.ble.lew.file.list"                           // 记录数据 LewBleResponse.FileList

            const val EventLewGetHrThreshold = "com.lepu.ble.lew.get.hr.threshold"              // 获取心率阈值 HrThreshold
            const val EventLewSetHrThreshold = "com.lepu.ble.lew.set.hr.threshold"              // 设置心率阈值
            const val EventLewGetOxyThreshold = "com.lepu.ble.lew.get.oxy.threshold"            // 获取血氧阈值 OxyThreshold
            const val EventLewSetOxyThreshold = "com.lepu.ble.lew.set.oxy.threshold"            // 设置血氧阈值
            const val EventLewReset = "com.lepu.ble.lew.reset"                                  // 复位 true
            const val EventLewFactoryReset = "com.lepu.ble.lew.factory.reset"                   // 恢复出厂设置 true
            const val EventLewFactoryResetAll = "com.lepu.ble.lew.factory.reset.all"            // 恢复生产出厂状态 true
            const val EventLewRtData = "com.lepu.ble.lew.realtime.data"                         // 实时数据 LewBleResponse.RtData
            const val EventLewReadFileError = "com.lepu.ble.lew.file.read.error"                // 传输文件出错 true
            const val EventLewReadingFileProgress = "com.lepu.ble.lew.file.reading.progress"    // 传输文件进度 int(0-100)
            const val EventLewReadFileComplete = "com.lepu.ble.lew.file.read.complete"          // 传输文件完成 LewBleResponse.EcgFile
        }
    }
    /**
     * VcominFhrBleInterface 发出的通知
     * 包含model: MODEL_VCOMIN
     */
    interface VCOMIN {
        companion object {
            const val EventVcominRtHr = "com.lepu.ble.vcomin.rt.hr"  // 实时心率 VcominData
        }
    }

    /**
     * Ad5FhrBleInterface 发出的通知
     * 包含model: MODEL_VTM_AD5, MODEL_FETAL
     */
    interface AD5 {
        companion object {
            const val EventAd5RtHr = "com.lepu.ble.ad5.rt.hr"  // 实时心率 Ad5Data
        }
    }
    /**
     * LeS1BleInterface 发出的通知
     * 包含model: MODEL_LES1
     */
    interface LES1 {
        companion object {
            const val EventLeS1Info = "com.lepu.ble.les1.info"                                  // 设备信息 LepuDevice
            const val EventLeS1RtData = "com.lepu.ble.les1.rtData"                              // 实时数据 LeS1BleResponse.RtData
            const val EventLeS1NoFile = "com.lepu.ble.les1.no.file"                             // 没有文件
            const val EventLeS1ReadFileError = "com.lepu.ble.les1.read.file.error"              // 传输文件出错 true
            const val EventLeS1ReadingFileProgress = "com.lepu.ble.les1.reading.file.progress"  // 传输文件进度 int(0-100)
            const val EventLeS1ReadFileComplete = "com.lepu.ble.les1.read.file.complete"        // 传输文件完成 LeS1BleResponse.BleFile
            const val EventLeS1Reset = "com.lepu.ble.les1.reset"                                // 复位 boolean
            const val EventLeS1ResetFactory = "com.lepu.ble.les1.reset.factory"                 // 恢复出厂设置 boolean
            const val EventLeS1SetTime = "com.lepu.ble.les1.set.time"                           // 同步时间 boolean
        }
    }

    /**
     * Vtm01BleInterface 发出的通知
     * 包含model: MODEL_VTM01
     */
    interface VTM01 {
        companion object {
            const val EventVtm01Info = "com.lepu.ble.vtm01.info"                          // 获取设备信息 LepuDevice
            const val EventVtm01RtData = "com.lepu.ble.vtm01.rtData"                      // 获取实时数据 Vtm01BleResponse.RtData
            const val EventVtm01RtParam = "com.lepu.ble.vtm01.rtParam"                    // 获取实时参数 Vtm01BleResponse.RtParam
            const val EventVtm01OriginalData = "com.lepu.ble.vtm01.original.data"         // 获取原始数据 Vtm01BleResponse.OriginalData
            const val EventVtm01Reset = "com.lepu.ble.vtm01.reset"                        // 复位 boolean
            const val EventVtm01FactoryReset = "com.lepu.ble.vtm01.factory.reset"         // 恢复出厂设置 boolean
            const val EventVtm01SleepMode = "com.lepu.ble.vtm01.sleep.mode"               // 睡眠模式开关 boolean
            const val EventVtm01BurnFactoryInfo = "com.lepu.ble.vtm01.burn.factory.info"  // 烧录出厂信息 boolean
        }
    }

    /**
     * BtpBleInterface 发出的通知
     * 包含model: MODEL_BTP
     */
    interface BTP {
        companion object {
            const val EventBtpGetInfo = "com.lepu.ble.btp.get.info"                           // 设备信息 LepuDevice
            const val EventBtpRtData = "com.lepu.ble.btp.rtData"                              // 实时数据 BtpBleResponse.RtData
            const val EventBtpGetFileList = "com.lepu.ble.btp.get.file.list"                  // 获取文件列表 BtpBleResponse.FileList
            const val EventBtpReadFileError = "com.lepu.ble.btp.read.file.error"              // 读文件出错 String(fileName)
            const val EventBtpReadingFileProgress = "com.lepu.ble.btp.reading.file.progress"  // 传输文件进度 int
            const val EventBtpReadFileComplete = "com.lepu.ble.btp.read.file.complete"        // 传输文件完成 byte[]
            const val EventBtpReset = "com.lepu.ble.btp.reset"                                // 复位 boolean
            const val EventBtpFactoryReset = "com.lepu.ble.btp.factory.reset"                 // 恢复出厂设置 boolean
            const val EventBtpFactoryResetAll = "com.lepu.ble.btp.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventBtpGetBattery = "com.lepu.ble.btp.get.battery"                     // 获取电量 KtBleBattery
            const val EventBtpGetConfig = "com.lepu.ble.btp.get.config"                       // 获取配置参数 BtpBleResponse.ConfigInfo
            const val EventBtpSetLowHr = "com.lepu.ble.btp.set.low.hr"                        // 设置心率低阈值 boolean
            const val EventBtpSetHighHr = "com.lepu.ble.btp.set.high.hr"                      // 设置心率高阈值 boolean
            const val EventBtpSetTempUnit = "com.lepu.ble.btp.set.temp.unit"                  // 设置温度单位 boolean
            const val EventBtpSetLowTemp = "com.lepu.ble.btp.set.low.temp"                    // 设置温度低阈值 boolean
            const val EventBtpSetHighTemp = "com.lepu.ble.btp.set.high.temp"                  // 设置温度高阈值 boolean
            const val EventBtpSetSystemSwitch = "com.lepu.ble.btp.set.system.switch"          // 设置系统开关 boolean
            const val EventBtpSetTime = "com.lepu.ble.btp.set.time"                           // 同步时间 boolean
            const val EventBtpBurnFactoryInfo = "com.lepu.ble.btp.burn.factory.info"          // 烧录出厂信息 boolean
        }
    }

    /**
     * VentilatorBleInterface 发出的通知
     * 包含model: MODEL_R20, MODEL_R21,
     *           MODEL_R10, MODEL_R11,
     *           MODEL_LERES
     */
    interface Ventilator {
        companion object {
            const val EventVentilatorSetUtcTime = "com.lepu.ble.ventilator.set.utc.time"                        // 同步UTC时间 int
            const val EventVentilatorGetInfo = "com.lepu.ble.ventilator.get.info"                               // 获取设备信息 LepuDevice
            const val EventVentilatorGetInfoError = "com.lepu.ble.ventilator.get.info.error"                    // 获取设备信息错误
            const val EventVentilatorFactoryReset = "com.lepu.ble.ventilator.factory.reset"                     // 恢复出厂设置 int
            const val EventVentilatorEncrypt = "com.lepu.ble.ventilator.encrypt"                                // 交换密钥 int
            const val EventVentilatorDeviceBound = "com.lepu.ble.ventilator.device.bound"                       // 绑定设备 int (0成功, 1失败, 2超时)
            const val EventVentilatorDeviceBoundError = "com.lepu.ble.ventilator.device.bound.error"            // 绑定设备失败
            const val EventVentilatorDeviceUnBound = "com.lepu.ble.ventilator.device.un.bound"                  // 解绑设备 int
            const val EventVentilatorDoctorMode = "com.lepu.ble.ventilator.doctor.mode"                         // 医生模式 VentilatorBleResponse.DoctorModeResult
            const val EventVentilatorDoctorModeError = "com.lepu.ble.ventilator.doctor.mode.error"              // 医生模式失败
            const val EventVentilatorGetWifiList = "com.lepu.ble.ventilator.get.wifi.list"                      // 获取WiFi列表 WifiList
            const val EventVentilatorGetWifiListError = "com.lepu.ble.ventilator.get.wifi.list.error"           // 获取WiFi列表错误
            const val EventVentilatorSetWifiConfig = "com.lepu.ble.ventilator.set.wifi.config"                  // 配置WiFi信息 int
            const val EventVentilatorGetWifiConfig = "com.lepu.ble.ventilator.get.wifi.config"                  // 获取WiFi信息 WifiConfig
            const val EventVentilatorGetWifiConfigError = "com.lepu.ble.ventilator.get.wifi.config.error"       // 获取WiFi信息失败
            const val EventVentilatorGetVersionInfo = "com.lepu.ble.ventilator.get.version.info"                // 获取详细版本信息 VentilatorBleResponse.VersionInfo
            const val EventVentilatorGetVersionInfoError = "com.lepu.ble.ventilator.get.version.info.error"     // 获取详细版本信息失败
            const val EventVentilatorGetSystemSetting = "com.lepu.ble.ventilator.get.system.setting"            // 获取系统设置 SystemSetting
            const val EventVentilatorGetSystemSettingError = "com.lepu.ble.ventilator.get.system.setting.error" // 获取系统设置失败
            const val EventVentilatorSetSystemSetting = "com.lepu.ble.ventilator.set.system.setting"            // 配置系统设置 int
            const val EventVentilatorGetMeasureSetting = "com.lepu.ble.ventilator.get.measure.setting"          // 获取测量设置 MeasureSetting
            const val EventVentilatorGetMeasureSettingError = "com.lepu.ble.ventilator.get.measure.setting.error"   // 获取测量设置错误
            const val EventVentilatorSetMeasureSetting = "com.lepu.ble.ventilator.set.measure.setting"          // 配置测量设置 int
            const val EventVentilatorMaskTest = "com.lepu.ble.ventilator.mask.test"                             // 佩戴测试 VentilatorBleResponse.MaskTestResult
            const val EventVentilatorMaskTestError = "com.lepu.ble.ventilator.mask.test.error"                  // 佩戴测试错误
            const val EventVentilatorGetVentilationSetting = "com.lepu.ble.ventilator.get.ventilation.setting"  // 获取通气控制参数 VentilationSetting
            const val EventVentilatorGetVentilationSettingError = "com.lepu.ble.ventilator.get.ventilation.setting.error"  // 获取通气控制参数错误
            const val EventVentilatorSetVentilationSetting = "com.lepu.ble.ventilator.set.ventilation.setting"  // 配置通气控制参数 int
            const val EventVentilatorGetWarningSetting = "com.lepu.ble.ventilator.get.warning.setting"          // 获取报警提示参数 WarningSetting
            const val EventVentilatorGetWarningSettingError = "com.lepu.ble.ventilator.get.warning.setting.error"// 获取报警提示参数错误
            const val EventVentilatorSetWarningSetting = "com.lepu.ble.ventilator.set.warning.setting"          // 配置报警提示参数 int
            const val EventVentilatorVentilationSwitch = "com.lepu.ble.ventilator.ventilation.switch"           // 启动/停止通气 int
            const val EventVentilatorGetFileList = "com.lepu.ble.ventilator.get.file.list"                      // 获取记录列表 VentilatorBleResponse.RecordList
            const val EventVentilatorGetFileListError = "com.lepu.ble.ventilator.get.file.list.error"           // 获取记录列表错误
            const val EventVentilatorReadFileError = "com.lepu.ble.ventilator.read.file.error"                  // 传输文件出错 int
            const val EventVentilatorReadingFileProgress = "com.lepu.ble.ventilator.reading.file.progress"      // 传输文件进度 int
            const val EventVentilatorReadFileComplete = "com.lepu.ble.ventilator.read.file.complete"            // 传输文件完成 byte[]
            const val EventVentilatorRtState = "com.lepu.ble.ventilator.rt.state"                               // 实时状态 VentilatorBleResponse.RtState
            const val EventVentilatorRtStateError = "com.lepu.ble.ventilator.rt.state.error"                    // 实时状态错误
            const val EventVentilatorRtParam = "com.lepu.ble.ventilator.rt.param"                               // 实时参数 VentilatorBleResponse.RtParam
            const val EventVentilatorRtParamError = "com.lepu.ble.ventilator.rt.param.error"                    // 实时参数失败
            const val EventVentilatorEvent = "com.lepu.ble.ventilator.event"                                    // 事件上报 VentilatorBleResponse.Event
            const val EventVentilatorEventError = "com.lepu.ble.ventilator.event.error"                         // 事件上报失败
            const val EventVentilatorWritingFileProgress = "com.lepu.ble.ventilator.writing.file.progress"      // 传输文件进度 int
            const val EventVentilatorWriteFileEnd = "com.lepu.ble.ventilator.write.file.end"                    // 传输文件结束 boolean
        }
    }

    /**
     * EcnBleInterface 发出的通知
     * 包含model: MODEL_ECN
     */
    interface ECN {
        companion object {
            const val EventEcnGetFileList = "com.lepu.ble.ecn.get.file.list"                  // 获取文件列表 EcnBleResponse.FileList
            const val EventEcnReadFileError = "com.lepu.ble.ecn.read.file.error"              // 读文件出错 String(fileName)
            const val EventEcnReadingFileProgress = "com.lepu.ble.ecn.reading.file.progress"  // 传输文件进度 int
            const val EventEcnReadFileComplete = "com.lepu.ble.ecn.read.file.complete"        // 传输文件完成 EcnBleResponse.File
            const val EventEcnGetRtState = "com.lepu.ble.ecn.get.rt.state"                    // 获取实时状态 EcnBleResponse.RtState
            const val EventEcnRtData = "com.lepu.ble.ecn.rt.data"                             // 实时数据 EcnBleResponse.RtData
            const val EventEcnStartRtData = "com.lepu.ble.ecn.start.rt.data"                  // 开始上发实时数据 boolean
            const val EventEcnStopRtData = "com.lepu.ble.ecn.stop.rt.data"                    // 停止上发实时数据 boolean
            const val EventEcnStartCollect = "com.lepu.ble.ecn.start.collect"                 // 开始采集 boolean
            const val EventEcnStopCollect = "com.lepu.ble.ecn.stop.collect"                   // 停止采集 boolean
            const val EventEcnDiagnosisResult = "com.lepu.ble.ecn.diagnosis.result"           // 诊断结论 EcnBleResponse.DiagnosisResult
        }
    }

    /**
     * Bp3BleInterface 发出的通知
     * 包含model: MODEL_LP_BP3W, MODEL_LP_BP3C
     */
    interface BP3 {
        companion object {
            const val EventBp3Echo = "com.lepu.ble.bp3.echo"                                  // 回显 byte[]
            const val EventBp3GetInfo = "com.lepu.ble.bp3.get.info"                           // 设备信息 LepuDevice
            const val EventBp3GetBattery = "com.lepu.ble.bp3.get.battery"                     // 电池信息 KtBleBattery
            const val EventBp3RtData = "com.lepu.ble.bp3.rt.data"                             // 实时数据 Bp2BleRtData
            const val EventBp3RtPressure = "com.lepu.ble.bp3.rt.pressure"                     // 实时压 int
            const val EventBp3CurPressure = "com.lepu.ble.bp3.cur.pressure"                   // 当前压力 int
            const val EventBp3RtWave = "com.lepu.ble.bp3.rt.wave"                             // 实时波形 Bp2BleRtWave
            const val EventBp3Reset = "com.lepu.ble.bp3.reset"                                // 复位 boolean
            const val EventBp3FactoryReset = "com.lepu.ble.bp3.factory.reset"                 // 恢复出厂设置 boolean
            const val EventBp3FactoryResetAll = "com.lepu.ble.bp3.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventBp3SetConfig = "com.lepu.ble.bp3.set.config"                       // 设置心跳音开关 boolean
            const val EventBp3GetConfig = "com.lepu.ble.bp3.get.config"                       // 获取参数 Bp2Config
            const val EventBp3SetUtcTime = "com.lepu.ble.bp3.set.utc.time"                    // 同步UTC时间 boolean
            const val EventBp3GetWifiList = "com.lepu.ble.bp3.get.wifi.list"                  // 获取路由 Bp2WifiDevice
            const val EventBp3GetWifiConfig = "com.lepu.ble.bp3.get.wifi.config"              // 获取WiFi配置 Bp2WifiConfig
            const val EventBp3SetWifiConfig = "com.lepu.ble.bp3.set.wifi.config"              // 设置WiFi boolean
            const val EventBp3BurnFactoryInfo = "com.lepu.ble.bp3.burn.factory.info"          // 烧录设备信息 boolean
            const val EventBp3CalibrationZero = "com.lepu.ble.bp3.calibration.zero"           // 校零 int
            const val EventBp3CalibrationSlope = "com.lepu.ble.bp3.calibration.slope"         // 校准 int
            const val EventBp3PressureTest = "com.lepu.ble.bp3.pressure.test"                 // 血压测试 boolean
            const val EventBp3SwitchTestMode = "com.lepu.ble.bp3.switch.test.mode"            // 切换测试模式 boolean
            const val EventBp3SwitchBpUnit = "com.lepu.ble.bp3.switch.bp.unit"                // 切换血压单位 boolean
            const val EventBp3SwitchValve = "com.lepu.ble.bp3.switch.valve"                   // 气阀开关 boolean
            const val EventBp3SwitchWifi4g = "com.lepu.ble.bp3.switch.wifi.4g"                // WiFi/4g开关 boolean
            const val EventBp3WritingFileProgress = "com.lepu.ble.bp3.writing.file.progress"  // 写文件进度 int
            const val EventBp3WriteFileComplete = "com.lepu.ble.bp3.write.file.complete"      // 写文件完成 int
            const val EventBp3GetFileList = "com.lepu.ble.bp3.get.file.list"                  // 获取文件列表 KtBleFileList
            const val EventBp3ReadFileError = "com.lepu.ble.bp3.read.file.error"              // 读文件错误 boolean
            const val EventBp3ReadingFileProgress = "com.lepu.ble.bp3.reading.file.process"   // 读文件进度 int
            const val EventBp3ReadFileComplete = "com.lepu.ble.bp3.read.file.complete"        // 读文件完成 byte[]
        }
    }

}