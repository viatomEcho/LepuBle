package com.lepu.blepro.event

import com.jeremyliao.liveeventbus.core.LiveEvent

/**
 * author: wujuan
 * created on: 2021/2/3 18:35
 * description: 从interface发送的业务通知，都要携带model。App使用时通过model区分同功能系列的不同设备
 */
class InterfaceEvent(val model: Int, val data: Any): LiveEvent {

    /**
     * Oxy LiveDataBus Event
     * OxyBleInterface发出的通知
     * 包含model: MODEL_O2RING, MODEL_BABYO2, MODEL_BABYO2N,
     *           MODEL_CHECKO2, MODEL_O2M, MODEL_SLEEPO2,
     *           MODEL_SNOREO2, MODEL_WEARO2, MODEL_SLEEPU,
     *           MODEL_OXYLINK, MODEL_KIDSO2, MODEL_OXYFIT
     */
    interface Oxy {
        companion object {
            const val EventOxyReadFileError = "com.lepu.ble.oxy.read.file.error"              // 传输文件失败 true
            const val EventOxyReadFileComplete = "com.lepu.ble.oxy.read.file.complete"        // 传输文件成功 OxyBleResponse.OxyFile
            const val EventOxyReadingFileProgress = "com.lepu.ble.oxy.reading.file.progress"  // 当前文件进度 展示时：(dialogProgress / 10.0) + "%")
            const val EventOxyFactoryReset = "com.lepu.ble.oxy.factory.reset"                 // 恢复出厂设置 true
            const val EventOxySyncDeviceInfo = "com.lepu.ble.oxy.sync"                        // 同步参数 true
            const val EventOxyInfo = "com.lepu.ble.oxy.info"                                  // 设备信息 OxyBleResponse.OxyInfo
            const val EventOxyBoxInfo = "com.lepu.ble.oxy.box.info"                           // 盒子信息 LepuDevice
            const val EventOxyRtData = "com.lepu.ble.oxy.rtData"                              // 实时波形 OxyBleResponse.RtWave
            const val EventOxyRtWaveRes = "com.lepu.ble.oxy.rt.wave.res"                      // 实时波形失败 true
            const val EventOxyRtParamData = "com.lepu.ble.oxy.rt.param.Data"                  // 实时参数 OxyBleResponse.RtParam
            const val EventOxyRtParamRes = "com.lepu.ble.oxy.rt.param.res"                    // 实时参数失败 true
            const val EventOxyPpgData = "com.lepu.ble.oxy.ppg.data"                           // PPG数据成功 OxyBleResponse.PPGData
            const val EventOxyPpgRes = "com.lepu.ble.oxy.ppg.res"                             // PPG数据失败 true
        }
    }

    /**
     * Er1BleInterface发出的通知
     * 包含model: MODEL_ER1, MODEL_ER1_N, MODEL_DUOEK
     */
    interface ER1 {
        companion object {
            const val EventEr1Info = "com.lepu.ble.er1.info"                                  // 设备信息 LepuDevice
            const val EventEr1RtData = "com.lepu.ble.er1.rtData"                              // 实时数据 Er1BleResponse.RtData
            const val EventEr1FileList = "com.lepu.ble.er1.fileList"                          // 文件列表 String
            const val EventEr1ReadFileError = "com.lepu.ble.er1.read.file.error"              // 传输文件出错 true
            const val EventEr1ReadingFileProgress = "com.lepu.ble.er1.reading.file.progress"  // 传输文件进度 int(0-100)
            const val EventEr1ReadFileComplete = "com.lepu.ble.er1.read.file.complete"        // 传输文件完成 Er1BleResponse.Er1File
            const val EventEr1Reset = "com.lepu.ble.er1.reset"                                // 复位 boolean
            const val EventEr1ResetFactory = "com.lepu.ble.er1.reset.factory"                 // 恢复出厂设置 boolean
            const val EventEr1ResetFactoryAll = "com.lepu.ble.er1.reset.factory.all"          // 恢复生产出厂状态 boolean
            const val EventEr1VibrateConfig = "com.lepu.ble.er1.vibrate.config"               // 获取配置参数 byte数组
            const val EventEr1GetConfigError = "com.lepu.ble.er1.get.config.error"            // 获取配置参数失败 boolean
            const val EventEr1SetSwitcherState = "com.lepu.ble.er1.set.switcher.state"        // 设置心跳音开关 boolean
            const val EventEr1SetTime = "com.lepu.ble.er1.set.time"                           // 同步时间 boolean
            const val EventEr1BurnFactoryInfo = "com.lepu.ble.er1.burn.factory.info"          // 烧录出厂信息 boolean
            const val EventEr1BurnLockFlash = "com.lepu.ble.er1.burn.lock.flash"              // 加密Flash boolean
        }
    }

    /**
     * BpmBleInterface发出的通知
     * 包含model: MODEL_BPM
     */
    interface BPM {
        companion object {
            const val EventBpmInfo = "com.lepu.ble.bpm.info"                     // 设备信息 BpmDeviceInfo
            const val EventBpmRtData = "com.lepu.ble.bpm.rtData"                 // 实时数据 BpmCmd
            const val EventBpmState = "com.lepu.ble.bpm.state"                   // 实时状态 byte数组
            const val EventBpmSyncTime = "com.lepu.ble.bpm.sync.time"            // 同步时间 true
            const val EventBpmRecordData = "com.lepu.ble.bpm.record.data"        // 记录数据 BpmCmd
            const val EventBpmRecordEnd = "com.lepu.ble.bpm.record.end"          // 传输完成 true
            const val EventBpmMeasureResult = "com.lepu.ble.bpm.measure.result"  // 测量结果 BpmCmd
            const val EventBpmMeasureErrorResult = "com.lepu.ble.bpm.measure.error.result"  // 测量错误结果 BpmCmd
        }
    }

    /**
     * Bp2BleInterface发出的通知
     * 包含model: MODEL_BP2, MODEL_BP2A, MODEL_BP2T
     */
    interface BP2 {
        companion object {
            const val EventBp2Info = "com.lepu.ble.bp2.info"                                  // 设备信息 Bp2DeviceInfo
            const val EventBp2RtData = "com.lepu.ble.bp2.rtData"                              // 实时数据 Bp2BleRtData
            const val EventBp2State = "com.lepu.ble.bp2.state"                                // 实时状态 Bp2BleRtState
            const val EventBp2FileList = "com.lepu.ble.bp2.fileList"                          // 文件列表 KtBleFileList
            const val EventBp2ReadFileError = "com.lepu.ble.bp2.read.file.error"              // 读文件出错 String(fileName)
            const val EventBp2ReadingFileProgress = "com.lepu.ble.bp2.reading.file.progress"  // 传输文件进度 Bp2FilePart
            const val EventBp2ReadFileComplete = "com.lepu.ble.bp2.read.file.complete"        // 传输文件完成 Bp2BleFile
            const val EventBp2Reset = "com.lepu.ble.bp2.reset"                                // 复位 int(0：失败 1：成功)
            const val EventBp2FactoryReset = "com.lepu.ble.bp2.factory.reset"                 // 恢复出厂设置 int(0：失败 1：成功)
            const val EventBp2FactoryResetAll = "com.lepu.ble.bp2.factory.reset.all"          // 恢复生产出厂状态 int(0：失败 1：成功)
            const val EventBpSetConfigResult = "com.lepu.ble.bp2.set.config"                  // 设置心跳音开关 int(0：失败 1：成功)
            const val EventBpGetConfigResult = "com.lepu.ble.bp2.get.config"                  // 心跳音开关 int(0：关 1：开)
            const val EventBp2SyncTime = "com.lepu.ble.bp2.sync.time"                         // 同步时间 boolean
            const val EventBpSwitchState = "com.lepu.ble.bp2.switch.state"                    // 切换设备状态 boolean
            const val EventBp2SetPhyState = "com.lepu.ble.bp2.set.phy.state"                  // 设置理疗状态 Bp2BlePhyState
            const val EventBp2GetPhyState = "com.lepu.ble.bp2.get.phy.state"                  // 获取理疗状态 Bp2BlePhyState
            const val EventBp2GetPhyStateError = "com.lepu.ble.bp2.get.phy.state.error"       // 获取理疗状态出错 boolean
        }
    }

    /**
     * Bp2wBleInterface发出的通知
     * 包含model: MODEL_BP2W
     */
    interface BP2W {
        companion object {
            const val EventBp2wInfo = "com.lepu.ble.bp2w.info"                                  // 设备信息 LepuDevice
            const val EventBp2wRtState = "com.lepu.ble.bp2w.rtState"                            // 主机状态 Bp2BleRtState
            const val EventBp2wRtData = "com.lepu.ble.bp2w.rtData"                              // 实时数据 Bp2BleRtData
            const val EventBp2wFileList = "com.lepu.ble.bp2w.fileList"                          // 文件列表 KtBleFileList
            const val EventBp2wReadFileError = "com.lepu.ble.bp2w.read.file.error"              // 读文件出错 String(fileName)
            const val EventBp2wReadingFileProgress = "com.lepu.ble.bp2w.reading.file.progress"  // 传输文件进度 Bp2FilePart
            const val EventBp2wReadFileComplete = "com.lepu.ble.bp2w.read.file.complete"        // 传输文件完成
            const val EventBp2wReset = "com.lepu.ble.bp2w.reset"                                // 复位 boolean
            const val EventBp2wFactoryReset = "com.lepu.ble.bp2w.factory.reset"                 // 恢复出厂设置 boolean
            const val EventBp2wFactoryResetAll = "com.lepu.ble.bp2w.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventBp2wSetConfig = "com.lepu.ble.bp2w.set.config"                       // 设置心跳音开关 boolean
            const val EventBp2wGetConfig = "com.lepu.ble.bp2w.get.config"                       // 获取参数 Bp2Config
            const val EventBp2wSyncTime = "com.lepu.ble.bp2w.sync.time"                         // 同步时间 boolean
            const val EventBp2wSwitchState = "com.lepu.ble.bp2w.switch.state"                   // 切换设备状态 boolean
            const val EventBp2WifiDevice = "com.lepu.ble.bp2w.wifi.device"                      // 获取路由 Bp2WifiDevice
            const val EventBp2WifiScanning = "com.lepu.ble.bp2w.wifi.scanning"                  // 正在扫描路由 boolean
            const val EventBp2wGetWifiConfig = "com.lepu.ble.bp2w.get.wifi.config"              // 获取WiFi配置 Bp2WifiConfig
            const val EventBp2wSetWifiConfig = "com.lepu.ble.bp2w.set.wifi.config"              // 设置WiFi boolean
            const val EventBp2wDeleteFile = "com.lepu.ble.bp2w.delete.file"                     // 删除文件 boolean
        }
    }

    /**
     * LeBp2wBleInterface发出的通知
     * 包含model: MODEL_LE_BP2W
     */
    interface LeBP2W {
        companion object {
            const val EventLeBp2wInfo = "com.lepu.ble.le.bp2w.info"                                  // 设备信息 LepuDevice
            const val EventLeBp2wRtState = "com.lepu.ble.le.bp2w.rtState"                            // 主机状态 Bp2BleRtState
            const val EventLeBp2wRtData = "com.lepu.ble.le.bp2w.rtData"                              // 实时数据 Bp2BleRtData
            const val EventLeBp2wList = "com.lepu.ble.le.bp2w.List"                                  // 列表名 LeBp2wBleList
            const val EventLeBp2wFileList = "com.lepu.ble.le.bp2w.fileList"                          // 文件列表内容 Bp2BleFile(type 0：LeBp2wUserList 1：LeBp2wBpList 2：LeBp2wEcgList)
            const val EventLeBp2wReadFileError = "com.lepu.ble.le.bp2w.read.file.error"              // 读文件出错 String(fileName)
            const val EventLeBp2wReadingFileProgress = "com.lepu.ble.le.bp2w.reading.file.progress"  // 传输文件进度 Bp2FilePart
            const val EventLeBp2wReadFileComplete = "com.lepu.ble.le.bp2w.read.file.complete"        // 传输文件完成 LeBp2wEcgFile
            const val EventLeBp2WriteFileError = "com.lepu.ble.le.bp2w.write.file.error"             // 写文件出错 String(fileName)
            const val EventLeBp2WriteFileComplete = "com.lepu.ble.le.bp2w.write.file.complete"       // 写文件完成 FileListCrc
            const val EventLeBp2WritingFileProgress = "com.lepu.ble.le.bp2w.writing.file.progress"   // 写文件进度 Bp2FilePart
            const val EventLeBp2wReset = "com.lepu.ble.le.bp2w.reset"                                // 复位 boolean
            const val EventLeBp2wFactoryReset = "com.lepu.ble.le.bp2w.factory.reset"                 // 恢复出厂设置 boolean
            const val EventLeBp2wFactoryResetAll = "com.lepu.ble.le.bp2w.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventLeBp2wSetConfig = "com.lepu.ble.le.bp2w.set.config"                       // 设置心跳音开关 boolean
            const val EventLeBp2wGetConfig = "com.lepu.ble.le.bp2w.get.config"                       // 获取参数 Bp2Config
            const val EventLeBp2wSyncTime = "com.lepu.ble.le.bp2w.sync.time"                         // 同步时间 boolean
            const val EventLeBp2wSwitchState = "com.lepu.ble.le.bp2w.switch.state"                   // 切换设备状态 boolean
            const val EventLeBp2WifiDevice = "com.lepu.ble.le.bp2w.wifi.device"                      // 获取路由 Bp2WifiDevice
            const val EventLeBp2WifiScanning = "com.lepu.ble.le.bp2w.wifi.scanning"                  // 正在扫描路由 boolean
            const val EventLeBp2wGetWifiConfig = "com.lepu.ble.le.bp2w.get.wifi.config"              // 获取WiFi配置 Bp2WifiConfig
            const val EventLeBp2wSetWifiConfig = "com.lepu.ble.le.bp2w.set.wifi.config"              // 设置WiFi boolean
            const val EventLeBp2wGetFileListCrc = "com.lepu.ble.le.bp2w.get.fileList.crc"            // 获取列表校验值 FileListCrc
            const val EventLeBp2wDeleteFile = "com.lepu.ble.le.bp2w.delete.file"                     // 删除文件 boolean
        }
    }

    /**
     * Er2BleInterface发出的通知
     * 包含model: MODEL_ER2
     */
    interface ER2 {
        companion object {
            const val EventEr2Info = "com.lepu.ble.er2.info"                                  // 设备信息 Er2DeviceInfo
            const val EventEr2SetTime = "com.lepu.ble.er2.set.time"                           // 同步时间 true
            const val EventEr2SetSwitcherState = "com.lepu.ble.er2.set.switcher.state"        // 设置心跳音 boolean
            const val EventEr2SwitcherState = "com.lepu.ble.er2.switcher.state"               // 获取配置参数 byte数组
            const val EventEr2GetConfigError = "com.lepu.ble.er2.get.config.error"            // 获取配置参数失败 boolean
            const val EventEr2Reset = "com.lepu.ble.er2.reset"                                // 复位 boolean
            const val EventEr2FactoryReset = "com.lepu.ble.er2.factory.reset"                 // 恢复出厂设置 boolean
            const val EventEr2FactoryResetAll = "com.lepu.ble.er2.factory.reset.all"          // 恢复生产出厂状态 boolean
            const val EventEr2RtData = "com.lepu.ble.er2.realtime.data"                       // 实时数据 Er2RtData
            const val EventEr2FileList = "com.lepu.ble.er2.file.list"                         // 文件列表 Er2FileList
            const val EventEr2ReadFileError = "com.lepu.ble.er2.file.read.error"              // 传输文件出错 true
            const val EventEr2ReadingFileProgress = "com.lepu.ble.er2.file.reading.progress"  // 传输文件进度 int(0-100)
            const val EventEr2ReadFileComplete = "com.lepu.ble.er2.file.read.complete"        // 传输文件完成 Er2File
        }
    }

    /**
     * PC60FwBleInterface发出的通知
     * 包含model: MODEL_PC60FW, MODEL_PC66B, MODEL_OXYSMART,
     *           MODEL_POD_1W, MODEL_POD2B, MODEL_PC_60NW,
     *           MODEL_PC_60B
     */
    interface PC60Fw {
        companion object {
            const val EventPC60FwRtDataParam = "com.lepu.ble.pc60fw.rt.data.param"     // 血氧参数 PC60FwBleResponse.RtDataParam
            const val EventPC60FwRtDataWave = "com.lepu.ble.pc60fw.rt.data.wave"       // 血氧波形 PC60FwBleResponse.RtDataWave
            const val EventPC60FwBattery = "com.lepu.ble.pc60fw.battery"               // 电池电量 PC60FwBleResponse.Battery
            const val EventPC60FwDeviceInfo = "com.lepu.ble.pc60fw.device.info"        // 设备信息 BoDeviceInfo
            const val EventPC60FwWorkingStatus = "com.lepu.ble.pc60fw.working.status"  // 工作状态 PC60FwBleResponse.WorkingStatus
            const val EventPC60FwOriginalData = "com.lepu.ble.pc60fw.original.data"    // 红外数据 PC60FwBleResponse.OriginalData
        }
    }

    /**
     * PC80BleInterface发出的通知
     * 包含model: MODEL_PC80B
     */
    interface PC80B {
        companion object {
            const val EventPc80bBatLevel = "com.lepu.ble.pc80b.bat.level"                         // 电池电量 int(0-3)
            const val EventPc80bDeviceInfo = "com.lepu.ble.pc80b.device.info"                     // 设备信息 PC80BleResponse.DeviceInfo
            const val EventPc80bTrackData = "com.lepu.ble.pc80b.track.data"                       // 快速实时数据 PC80BleResponse.RtTrackData
            const val EventPc80bContinuousData = "com.lepu.ble.pc80b.continuous.data"             // 连续实时数据 PC80BleResponse.RtContinuousData
            const val EventPc80bContinuousDataEnd = "com.lepu.ble.pc80b.continuous.data.end"       // 连续实时结束 true
            const val EventPc80bReadFileError = "com.lepu.ble.pc80b.file.read.error"              // 传输文件出错 true
            const val EventPc80bReadingFileProgress = "com.lepu.ble.pc80b.file.reading.progress"  // 传输文件进度 int(0-100)
            const val EventPc80bReadFileComplete = "com.lepu.ble.pc80b.file.read.complete"        // 传输文件完成 PC80BleResponse.ScpEcgFile
        }
    }

    /**
     * FhrBleInterface发出的通知
     * 包含model: MODEL_FHR
     */
    interface FHR {
        companion object {
            const val EventFhrDeviceInfo = "com.lepu.ble.fhr.device.info"  // 设备信息 FhrBleResponse.DeviceInfo
            const val EventFhrAudioData = "com.lepu.ble.fhr.audio.data"    // 音频数据 byte数组
        }
    }

    /**
     * Bpw1BleInterface发出的通知
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
     * PC100BleInterface发出的通知
     * 包含model: MODEL_PC100
     */
    interface PC100 {
        companion object {
            const val EventPc100DeviceInfo = "com.lepu.ble.pc100.device.info"         // 设备信息 Pc100DeviceInfo
            const val EventPc100BpResult = "com.lepu.ble.pc100.bp.result"             // 血压测量结果 Pc100BleResponse.BpResult
            const val EventPc100BpErrorResult = "com.lepu.ble.pc100.bp.error.result"  // 血压测量错误结果 Pc100BleResponse.BpResultError
            const val EventPc100BpStart = "com.lepu.ble.pc100.bp.start"               // 血压开始测量 true
            const val EventPc100BpStop = "com.lepu.ble.pc100.bp.stop"                 // 血压停止测量 true
            const val EventPc100BpStatus = "com.lepu.ble.pc100.bp.status"             // 血压测量状态 Pc100BleResponse.BpStatus
            const val EventPc100BpRtData = "com.lepu.ble.pc100.bp.rtdata"             // 血压实时测量值 Pc100BleResponse.RtBpData
            const val EventPc100BoRtWave = "com.lepu.ble.pc100.bo.rtwave"             // 血氧实时波形包 byte数组
            const val EventPc100BoRtParam = "com.lepu.ble.pc100.bo.rtparam"           // 血氧实时测量值 Pc100BleResponse.RtBoParam
            const val EventPc100BoFingerOut = "com.lepu.ble.pc100.bo.finger.out"      // 血氧脱落未接入
        }
    }

    /**
     * Ap10BleInterface
     * 包含model: MODEL_AP20
     */
    interface AP20 {
        companion object {
            const val EventAp20SetTime = "com.lepu.ble.ap20.set.time"              // 设置时间 true
            const val EventAp20DeviceInfo = "com.lepu.ble.ap20.device.info"        // 设备信息 BoDeviceInfo
            const val EventAp20Battery = "com.lepu.ble.ap20.battery"               // 电池电量 int（0-3）
            const val EventAp20RtBoWave = "com.lepu.ble.ap20.bo.rtwave"            // 血氧波形包数据 Ap20BleResponse.RtBoWave
            const val EventAp20RtBoParam = "com.lepu.ble.ap20.bo.rtparam"          // 血氧参数包数据 Ap20BleResponse.RtBoParam
            const val EventAp20RtBreathWave = "com.lepu.ble.ap20.breath.rtwave"    // 鼻息流波形包数据 Ap20BleResponse.RtBreathWave
            const val EventAp20RtBreathParam = "com.lepu.ble.ap20.breath.rtparam"  // 鼻息流参数包数据 Ap20BleResponse.RtBreathParam

            /**
             * type : 0 背光等级（0-5）
             *        1 警报功能开关（0 off，1 on）
             *        2 血氧过低阈值（85-99）
             *        3 脉率过低阈值（30-99）
             *        4 脉率过高阈值（100-250）
             */
            const val EventAp20ConfigInfo = "com.lepu.ble.ap20.config.info"   // 获取配置信息 Ap20BleResponse.ConfigInfo
        }
    }

    /**
     * LeW3BleInterface发出的通知
     * 包含model: MODEL_LEW3
     */
    interface Lew3 {
        companion object {
            const val EventLew3Info = "com.lepu.ble.lew3.info"                                  // 设备信息 LepuDevice
            const val EventLew3BatteryInfo = "com.lepu.ble.lew3.battery.info"                   // 电量信息 KtBleBattery
            const val EventLew3SetTime = "com.lepu.ble.lew3.set.time"                           // 同步时间 true
            const val EventLew3BoundDevice = "com.lepu.ble.lew3.bound.device"                   // 请求绑定设备
            const val EventLew3GetConfig = "com.lepu.ble.lew3.get.config"                       // 获取配置信息
            const val EventLew3SetServer = "com.lepu.ble.lew3.set.server"                       // 配置服务器信息
            const val EventLew3SystemSettings = "com.lepu.ble.lew3.system.settings"             // 配置系统设置
            const val EventLew3Reset = "com.lepu.ble.lew3.reset"                                // 复位 true
            const val EventLew3FactoryReset = "com.lepu.ble.lew3.factory.reset"                 // 恢复出厂设置 true
            const val EventLew3FactoryResetAll = "com.lepu.ble.lew3.factory.reset.all"          // 恢复生产出厂状态 true
            const val EventLew3RtData = "com.lepu.ble.lew3.realtime.data"                       // 实时数据 Lew3BleResponse.RtData
            const val EventLew3FileList = "com.lepu.ble.lew3.file.list"                         // 文件列表 Lew3BleResponse.FileList
            const val EventLew3ReadFileError = "com.lepu.ble.lew3.file.read.error"              // 传输文件出错 true
            const val EventLew3ReadingFileProgress = "com.lepu.ble.lew3.file.reading.progress"  // 传输文件进度 int(0-100)
            const val EventLew3ReadFileComplete = "com.lepu.ble.lew3.file.read.complete"        // 传输文件完成 Lew3BleResponse.EcgFile
        }
    }

    /**
     * VetcorderBleInterface发出的通知
     * 包含model: MODEL_VETCORDER
     */
    interface Vetcorder {
        companion object {
            const val EventVetcorderInfo = "com.lepu.ble.vetcorder.info"  // VetcorderInfo
        }
    }

    /**
     * Sp20BleInterface
     * 包含model: MODEL_SP20
     */
    interface SP20 {
        companion object {
            const val EventSp20SetTime = "com.lepu.ble.sp20.set.time"        // 设置时间 true
            const val EventSp20DeviceInfo = "com.lepu.ble.sp20.device.info"  // 设备信息 BoDeviceInfo
            const val EventSp20Battery = "com.lepu.ble.sp20.battery"         // 电池电量 int（0-3）
            const val EventSp20RtWave = "com.lepu.ble.sp20.rtwave"           // 血氧波形包数据 Sp20BleResponse.RtWave
            const val EventSp20RtParam = "com.lepu.ble.sp20.rtparam"         // 血氧参数包数据 Sp20BleResponse.RtParam
            const val EventSp20TempData = "com.lepu.ble.sp20.temp.data"      // 体温数据 Sp20BleResponse.TempData

            /**
             * type :
             *        2 血氧过低阈值（value：85-99）
             *        3 脉率过低阈值（value：30-99）
             *        4 脉率过高阈值（value：100-250）
             *        5 搏动音开关（value：0 off，1 on）
             */
            const val EventSp20GetConfig = "com.lepu.ble.sp20.get.config"          // 获取配置信息 Sp20Config
            /**
             * value : 0 失败
             *         1 成功
             */
            const val EventSp20SetConfig = "com.lepu.ble.sp20.set.config.success"  // 配置信息 Sp20Config
        }
    }

    /**
     * Vtm20fBleInterface
     * 包含model: MODEL_TV221U
     */
    interface VTM20f {
        companion object {
            const val EventVTM20fRtWave = "com.lepu.ble.vtm20f.rtwave"           // 血氧波形包数据 Vtm20fBleResponse.RtWave
            const val EventVTM20fRtParam = "com.lepu.ble.vtm20f.rtparam"         // 血氧参数包数据 Vtm20fBleResponse.RtParam
        }
    }

    /**
     * Aoj20aBleInterface
     * 包含model: MODEL_AOJ20A
     */
    interface AOJ20a {
        companion object {
            const val EventAOJ20aTempRtData = "com.lepu.ble.aoj20a.temp.rtdata"         // 实时测温数据 Aoj20aBleResponse.TempRtData
            const val EventAOJ20aSetTime = "com.lepu.ble.aoj20a.set.time"               // 同步时间 boolean
            const val EventAOJ20aTempRecord = "com.lepu.ble.aoj20a.temp.record"         // 历史测量数据 Aoj20aBleResponse.TempRecord
            const val EventAOJ20aNoTempRecord = "com.lepu.ble.aoj20a.no.temp.record"    // 无历史数据 boolean
            const val EventAOJ20aDeviceData = "com.lepu.ble.aoj20a.device.data"         // 设备数据 Aoj20aBleResponse.DeviceData
            const val EventAOJ20aTempErrorMsg = "com.lepu.ble.aoj20a.temp.error.msg"    // 错误码数据 Aoj20aBleResponse.ErrorMsg
            const val EventAOJ20aDeleteData = "com.lepu.ble.aoj20a.delete.data"         // 删除历史数据 boolean
        }
    }

    interface CheckmePod {
        companion object {
            const val EventCheckmePodSetTime = "com.lepu.ble.checkme.pod.set.time"                            // 同步时间 boolean
            const val EventCheckmePodDeviceInfo = "com.lepu.ble.checkme.pod.device.info"                      // 设备信息 CheckmePodBleResponse.DeviceInfo
            const val EventCheckmePodRtData = "com.lepu.ble.checkme.pod.realtime.data"                        // 实时数据 CheckmePodBleResponse.RtData
            const val EventCheckmePodRtDataError = "com.lepu.ble.checkme.pod.realtime.data.error"             // 实时数据出错 boolean
            const val EventCheckmePodFileList = "com.lepu.ble.checkme.pod.file.list"                          // 文件列表 CheckmePodBleResponse.FileList
            const val EventCheckmePodGetFileListError = "com.lepu.ble.checkme.pod.get.file.list.error"        // 获取文件列表出错 boolean
            const val EventCheckmePodGetFileListProgress = "com.lepu.ble.checkme.pod.get.file.list.progress"  // 获取文件列表进度 int
        }
    }

    interface Pulsebit {
        companion object {
            const val EventPulsebitSetTime = "com.lepu.ble.pulsebit.set.time"                            // 同步时间 boolean
            const val EventPulsebitDeviceInfo = "com.lepu.ble.pulsebit.device.info"                      // 设备信息 PulsebitBleResponse.DeviceInfo
            const val EventPulsebitGetFileListProgress = "com.lepu.ble.pulsebit.get.file.list.progress"  // 获取文件列表进度 int
            const val EventPulsebitGetFileList = "com.lepu.ble.pulsebit.get.file.list"                   // 文件列表 PulsebitBleResponse.FileList
            const val EventPulsebitGetFileListError = "com.lepu.ble.pulsebit.get.file.list.error"        // 获取文件列表出错 boolean
            const val EventPulsebitReadingFileProgress = "com.lepu.ble.pulsebit.reading.file.progress"   // 获取文件进度 int
            const val EventPulsebitReadFileComplete = "com.lepu.ble.pulsebit.read.file.complete"         // 获取文件完成 PulsebitBleResponse.EcgFile
            const val EventPulsebitReadFileError = "com.lepu.ble.pulsebit.read.file.error"               // 获取文件出错 boolean
        }
    }


    /**
     * Pc68bBleInterface
     * 包含model: MODEL_PC_68B
     */
    interface PC68B {
        companion object {
            const val EventPc68bSetTime = "com.lepu.ble.pc68b.set.time"                     // 设置时间 true
            const val EventPc68bGetTime = "com.lepu.ble.pc68b.get.time"                     // 获取时间 Pc68bBleResponse.DeviceTime
            const val EventPc68bDeleteFile = "com.lepu.ble.pc68b.delete.file"               // 删除文件 true
            const val EventPc68bDeviceInfo = "com.lepu.ble.pc68b.device.info"               // 设备信息 BoDeviceInfo
            const val EventPc68bRtWave = "com.lepu.ble.pc68b.rtwave"                        // 血氧波形包数据 Pc68bBleResponse.RtWave
            const val EventPc68bRtParam = "com.lepu.ble.pc68b.rtparam"                      // 血氧参数包数据 Pc68bBleResponse.RtParam
            const val EventPc68bStatusInfo = "com.lepu.ble.pc68b.status.info"               // 状态信息 Pc68bBleResponse.StatusInfo
            const val EventPc68bConfigInfo = "com.lepu.ble.pc68b.config.info"               // 获取配置信息 Pc68bConfig
            const val EventPc68bFileList = "com.lepu.ble.pc68b.file.list"                   // 文件列表 MutableList<String>
            const val EventPc68bReadFileComplete = "com.lepu.ble.pc68b.read.file.complete"  // 文件内容 Pc68bBleResponse.Record
        }
    }

    /**
     * VcominFhrBleInterface
     * 包含model: MODEL_VCOMIN
     */
    interface VCOMIN {
        companion object {
            const val EventVcominRtHr = "com.lepu.ble.vcomin.rt.hr"  // 实时心率
        }
    }

    /**
     * Ad5FhrBleInterface
     * 包含model: MODEL_VTM_AD5, MODEL_FETAL
     */
    interface AD5 {
        companion object {
            const val EventAd5RtHr = "com.lepu.ble.ad5.rt.hr"  // 实时心率
        }
    }

    /**
     * PC300BleInterface发出的通知
     * 包含model: MODEL_PC300
     */
    interface PC300 {
        companion object {
            const val EventPc300DeviceInfo = "com.lepu.ble.pc300.device.info"         // 设备信息 Pc300DeviceInfo
            const val EventPc300BpStart = "com.lepu.ble.pc300.bp.start"               // 血压开始测量 true
            const val EventPc300BpStop = "com.lepu.ble.pc300.bp.stop"                 // 血压停止测量 true
            const val EventPc300BpResult = "com.lepu.ble.pc300.bp.result"             // 血压测量结果 Pc300BleResponse.BpResult
            const val EventPc300BpErrorResult = "com.lepu.ble.pc300.bp.error.result"  // 血压测量错误结果 Pc300BleResponse.BpResultError
            const val EventPc300RtBpData = "com.lepu.ble.pc300.bp.rtdata"             // 血压实时测量值 int
            const val EventPc300RtOxyWave = "com.lepu.ble.pc300.oxy.rtwave"           // 血氧实时波形包 Pc300BleResponse.RtOxyWave
            const val EventPc300RtOxyParam = "com.lepu.ble.pc300.oxy.rtparam"         // 血氧实时参数包 Pc300BleResponse.RtOxyParam
            const val EventPc300EcgStart = "com.lepu.ble.pc300.ecg.start"             // 心电开始测量 true
            const val EventPc300EcgStop = "com.lepu.ble.pc300.ecg.stop"               // 心电停止测量 true
            const val EventPc300RtEcgWave = "com.lepu.ble.pc300.ecg.rtwave"           // 心电实时波形包 Pc300BleResponse.RtEcgWave
            const val EventPc300EcgResult = "com.lepu.ble.pc300.ecg.result"           // 心电实时结果 Pc300BleResponse.EcgResult
            const val EventPc300GluResult = "com.lepu.ble.pc300.glu.result"           // 血糖结果 Pc300BleResponse.GluResult
            const val EventPc300TempResult = "com.lepu.ble.pc300.temp.result"         // 血糖结果 Pc300BleResponse.TempResult
        }
    }

    /**
     * CheckmeLeInterface发出的通知
     * 包含model: MODEL_CHECKME_LE
     */
    interface CheckmeLE {
        companion object {
            const val EventCheckmeLeSetTime = "com.lepu.ble.checkmele.set.time"                            // 同步时间 boolean
            const val EventCheckmeLeDeviceInfo = "com.lepu.ble.checkmele.device.info"                      // 设备信息 CheckmeLeBleResponse.DeviceInfo
            const val EventCheckmeLeGetFileListProgress = "com.lepu.ble.checkmele.get.file.list.progress"  // 获取文件列表进度 int
            const val EventCheckmeLeGetFileList = "com.lepu.ble.checkmele.get.file.list"                   // 文件列表 CheckmeLeBleResponse.ListContent
            const val EventCheckmeLeGetFileListError = "com.lepu.ble.checkmele.get.file.list.error"        // 获取文件列表出错 boolean
            const val EventCheckmeLeReadingFileProgress = "com.lepu.ble.checkmele.reading.file.progress"   // 获取文件进度 int
            const val EventCheckmeLeReadFileComplete = "com.lepu.ble.checkmele.read.file.complete"         // 获取文件完成 CheckmeLeBleResponse.EcgFile
            const val EventCheckmeLeReadFileError = "com.lepu.ble.checkmele.read.file.error"               // 获取文件出错 boolean
        }
    }

}