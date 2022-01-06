## `版本` 

第三位是0 表示测试版， 非0为发布版

- 2.0.0.1 添加Pc80b，Pc60fw，Th12，胎心仪Fhr设备

- 2.0.0.2 合并vihealth sdk分支，添加血压手表Bpw1设备

- 2.0.0.3 

  > 修改minsdk=21
  >
  > th12获取导联数据
  >
  > er1 updater设备扫描通知

- 2.0.0.5 添加返回不注册model的scanResult扫描结果

- 2.0.0.8 

  > 发送蓝牙状态和发送指令添加device ready判断
  >
  > 停止实时添加要执行函数，默认空函数
  >
  > 蓝牙名一致不进行蓝牙名重连
  >
  > demo添加api测试部分
  
- 2.0.0.9

  >Android8.0启动服务问题
  >
  >添加停止蓝牙服务BleService接口
  >
  >扫描识别vihealth设备
  
- 2.0.0.10

  >Android8.0启动服务后移除服务通知
  
- 2.0.0.11

  >添加Pc100，Pc66b设备
  >
  >er1，er2设置mtu为247
  >
  >添加bp2音量设置
  >
  >附加文档和源码



## `主要类说明` 

- `BleServiceHelper` ：初始化SDK，扫描，连接，断连，重连等
- `InterfaceEvent` ，`EventMsgConst` ：监听LiveEventBus消息接收数据
- `BIOL` ：订阅蓝牙状态监听
- `BleChangeObserver` ：监听蓝牙状态，必须订阅蓝牙状态监听，否则无法接收蓝牙状态
- `BleServiceObserver` ：监听蓝牙服务状态
- 每个model对应一个interface，由app管理interface



## `BleServiceHelper` 

初始化sdk：

- `initLog(log: Boolean)` ：配置sdk log打印开关

- `initRawFolder(folders: SparseArray<String>)` ：配置下载数据的保存路径，key为model

- `initModelConfig(modelConfig: SparseArray<Int>)` ：配置model，key为model

- `initRtConfig(runRtConfig: SparseArray<Boolean>)` ：配置接收主机信息后，是否立即开启实时监测任务，key为model

- `initService(application: Application, observer: BleServiceObserver?)` 
  
  > 在initLog()，initRawFolder()，initModelConfig()，initRtConfig()之后调用
  >
  > 启动BleService蓝牙通讯服务

其他接口：

- `stopService(application: Application)` ：停止蓝牙服务BleService

- `reInitBle()` ：重新初始化蓝牙

- `startScan(scanModel: Int, needPair: Boolean = false, isStrict: Boolean = false， isScanUnRegister: Boolean = false)` 

  > 开始扫描，单个model设备
  >
  > needPair：本次扫描是否需要发送配对信息，默认是false
  >
  > isStrict：本次扫描是否是严格模式（严格模式下只会发送scanModel的扫描结果信息），默认是false
  >
  > isScanUnRegister：本次扫描是否返回SDK没有的model蓝牙名的设备

- `startScan(scanModel: IntArray, needPair: Boolean = false, isStrict: Boolean = false)` ：开始扫描，多个model设备

- `stopScan()` ：停止扫描

- `setInterfaces(model: Int, runRtImmediately: Boolean = false)` 

  > 在initService()之后调用，**连接设备前需要设置** 
  >
  > 设置model的interface
  >
  > runRtImmediately：是否立即开启实时监测任务，默认是false

- `getInterface(model: Int)` ：获取model的interface

- `getInterfaces()` ：获取服务中所有的interface

- `connect(context: Context, model: Int, b: BluetoothDevice, isAutoReconnect: Boolean = true, toConnectUpdater: Boolean = false)`  

  > 连接成功后自动同步时间，但Pc80b，Pc60fw，胎心仪Fhr，Pc100，Pc66b设备没有同步时间设置，连接成功后直接发送EventMsgConst.Ble.EventBleDeviceReady
  >
  > 每次发起**连接前必须关闭扫描** 
  >
  > isAutoReconnect：是否自动重连，默认是true
  >
  > toConnectUpdater：检查是否是连接升级失败的设备，默认是false

- `reconnect(scanModel: Int, name: String, needPair: Boolean = false, toConnectUpdater: Boolean = false)` ：通过**蓝牙设备名称** 重新连接，单个model设备

- `reconnect(scanModel: IntArray, name: Array<String>, needPair: Boolean = false, toConnectUpdater: Boolean = false)` ：重新连接多个model设备

- `reconnectByAddress(scanModel: Int, macAddress: String, needPair: Boolean = false, toConnectUpdater: Boolean = false)` ：通过**蓝牙地址** 重新连接，单个model设备

- `reconnectByAddress(scanModel: IntArray, macAddress: Array<String>, needPair: Boolean, toConnectUpdater: Boolean = false)` ：重新连接多个model设备

  >**蓝牙名称一致的设备重连必须使用蓝牙地址重连方法** ，蓝牙名一样的设备目前有Pc80b，胎心仪Fhr，血压手表Bpw1

- `disconnect(autoReconnect: Boolean)` ：所有设备断开连接

- `disconnect(model: Int, autoReconnect: Boolean)` ：单设备断开连接

- `getConnectState(model: Int)` ：获取model连接状态

- `syncTime(model: Int)` ：同步时间

- `getInfo(model: Int)` ：获取主机信息

- `getFileList(model: Int)` ：获取设备文件列表

- `readFile(userId: String, fileName: String, model: Int, offset: Int = 0)` ：读取主机文件，进入读文件流程前APP要手动停止实时任务状态

- `cancelReadFile(model: Int)` ：取消读取主机文件

- `pauseReadFile(model: Int)` ：暂停读取主机文件

- `continueReadFile(model: Int, userId: String, fileName: String, offset: Int)` ：继续读取主机文件

- `factoryResetAll(model: Int)` ：恢复生产出厂状态

- `factoryReset(model: Int)` ：恢复出厂设置

- `reset(model: Int)` ：设备复位

- `updateSetting(model: Int, type: String, value: Any)` ：更新设备设置

- `setRTDelayTime(model: Int, delayMillis: Long)` ：设置实时监测任务的间隔时间

- `startRtTask(model: Int)` ：开启实时监测任务

- `stopRtTask(model: Int, sendCmd: () -> Unit = {})` 

  > 停止实时监测任务
  >
  > sendCmd：停止实时后指定执行方法

- `isRtStop(model: Int)` ：是否已停止实时监测任务

- `startBp(model: Int)` ：开始测量血压，支持设备有Bpm，Bpw1，Pc100

- `stopBp(model: Int)` ：停止测量血压

- `bp2SetConfig(model: Int, switch: Boolean, volume: Int = 2)` 

  > 设置Bp2配置信息
  >
  > switch：声音开关
  >
  > volume：声音大小（0-3）

- `bp2GetConfig(model: Int)` ：获取Bp2心跳音开关信息

- `getBpmFileList(model: Int, map: HashMap<String, Any>)` ：获取Bpm设备文件列表

- `setEr2SwitcherState(model: Int, hrFlag: Boolean)` ：设置Er2心跳音开关

- `getEr2SwitcherState(model: Int)` ：获取Er2心跳音开关信息

- `getEr1VibrateConfig(model: Int)` ：获取Er1，Duoek配置信息

- `setEr1Vibrate(model: Int, switcher: Boolean, threshold1: Int, threshold2: Int)` 
  
  > 设置Er1配置信息
  >
  > switcher：心率震动开关
  >
  > threshold1：心率阈值1
  >
  > threshold2：心率阈值2
  
- `setEr1Vibrate(model: Int, switcher: Boolean, vector: Int, motionCount: Int, motionWindows: Int)` 

  > 设置Duoek配置信息
  >
  > switcher：心跳音开关
  >
  > vector：加速度阈值
  >
  > motionCount：加速度检测次数
  >
  > motionWindows：加速度检测窗口

- `bp2SwitchState(model: Int, state: Int)` 
  
  > 切换Bp2设备状态
  >
  > state：0 进入血压测量，1 进入心电测量，2 进入历史回顾，3 进入开机预备状态，4 关机，5 进入理疗模式
  
- `oxyGetPpgRt(model: Int)` ：获取血氧戒指O2Ring原始数据

- `sendHeartbeat(model: Int)` ：发送心跳包查询电量，支持设备有Pc80b

- `setMeasureTime(model: Int, measureTime: Array<String?>)` 
  
  > 设置定时测量血压时间，支持设备有Bpw1
  >
  > String格式："startHH,startMM,stopHH,stopMM,interval,serialNum,totalNum"
  
- `getMeasureTime(model: Int)` ：获取定时测量血压时间，支持设备有Bpw1

- `setTimingSwitch(model: Int, timingSwitch: Boolean)` ：设置定时测量血压开关，支持设备有Bpw1



## `InterfaceEvent` 

```
class InterfaceEvent(val model: Int, val data: Any): LiveEvent {

    /**
     * Oxy LiveDataBus Event
     * OxyBleInterface发出的通知
     * 包含model: model_o2ring
     */
    interface Oxy{
        companion object{
            const val EventOxyReadFileError = "com.lepu.ble.oxy.read.file.error"              // 传输文件失败 true
            const val EventOxyReadFileComplete = "com.lepu.ble.oxy.read.file.complete"        // 传输文件成功 OxyBleResponse.OxyFile
            const val EventOxyReadingFileProgress = "com.lepu.ble.oxy.reading.file.progress"  // 当前文件进度 展示时：(dialogProgress / 10.0) + "%")
            const val EventOxyFactoryReset = "com.lepu.ble.oxy.factory.reset"                 // 恢复出厂设置 true
            const val EventOxySyncDeviceInfo = "com.lepu.ble.oxy.sync"                        // 同步参数 true
            const val EventOxyInfo = "com.lepu.ble.oxy.info"                                  // 设备信息 OxyBleResponse.OxyInfo
            const val EventOxyRtData = "com.lepu.ble.oxy.rtData"                              // 实时波形 OxyBleResponse.RtWave
            const val EventOxyRtParamData = "com.lepu.ble.oxy.rt.param.Data"                  // 实时参数 OxyBleResponse.RtParam
            const val EventOxyPpgData = "com.lepu.ble.oxy.ppg.data"                           // PPG数据成功 OxyBleResponse.PPGData
            const val EventOxyPpgRes = "com.lepu.ble.oxy.ppg.res"                             // PPG数据失败 true
        }
    }

    /**
     * Er1BleInterface发出的通知
     * 包含model: model_er1\model_duoek
     */
    interface ER1{
        companion object{
            const val EventEr1Info = "com.lepu.ble.er1.info"                                  // 设备信息 LepuDevice
            const val EventEr1RtData = "com.lepu.ble.er1.rtData"                              // 实时数据 Er1BleResponse.RtData
            const val EventEr1FileList = "com.lepu.ble.er1.fileList"                          // 文件列表 String
            const val EventEr1ReadFileError = "com.lepu.ble.er1.read.file.error"              // 传输文件出错 true
            const val EventEr1ReadingFileProgress = "com.lepu.ble.er1.reading.file.progress"  // 传输文件进度 int(0-100)
            const val EventEr1ReadFileComplete = "com.lepu.ble.er1.read.file.complete"        // 传输文件完成 Er1BleResponse.Er1File
            const val EventEr1Reset = "com.lepu.ble.er1.reset"                                // 复位 true
            const val EventEr1ResetFactory = "com.lepu.ble.er1.reset.factory"                 // 恢复出厂设置 true
            const val EventEr1ResetFactoryAll = "com.lepu.ble.er1.reset.factory.all"          // 恢复生产出厂状态 true
            const val EventEr1VibrateConfig = "com.lepu.ble.er1.vibrate.config"               // 配置参数 byte数组
            const val EventEr1SetSwitcherState = "com.lepu.ble.er1.set.switcher.state"        // 设置心跳音开关 true
            const val EventEr1SetTime = "com.lepu.ble.er1.set.time"                           // 同步时间 true
        }
    }

    /**
     * BpmBleInterface发出的通知
     * 包含model: model_bpm
     */
    interface BPM{
        companion object{
            const val EventBpmInfo = "com.lepu.ble.bpm.info"                     // 设备信息 BpmDeviceInfo
            const val EventBpmRtData = "com.lepu.ble.bpm.rtData"                 // 实时数据 BpmCmd
            const val EventBpmState = "com.lepu.ble.bpm.state"                   // 实时状态 byte数组
            const val EventBpmSyncTime = "com.lepu.ble.bpm.sync.time"            // 同步时间 true
            const val EventBpmRecordData = "com.lepu.ble.bpm.record.data"        // 记录数据 BpmCmd
            const val EventBpmRecordEnd = "com.lepu.ble.bpm.record.end"          // 传输完成 true
            const val EventBpmMeasureResult = "com.lepu.ble.bpm.measure.result"  // 测量结果 BpmCmd
        }
    }

    /**
     * Bp2BleInterface发出的通知
     * 包含model: model_bp2
     */
    interface BP2{
        companion object{
            const val EventBp2Info = "com.lepu.ble.bp2.info"                                  // 设备信息 Bp2DeviceInfo
            const val EventBp2RtData = "com.lepu.ble.bp2.rtData"                              // 实时数据 Bp2BleRtData
            const val EventBp2State = "com.lepu.ble.bp2.state"                                // 实时状态 Bp2BleRtState
            const val EventBp2FileList = "com.lepu.ble.bp2.fileList"                          // 文件列表 KtBleFileList
            const val EventBp2ReadFileError = "com.lepu.ble.bp2.read.file.error"              // 传输文件出错
            const val EventBp2ReadingFileProgress = "com.lepu.ble.bp2.reading.file.progress"  // 传输文件进度 Bp2FilePart
            const val EventBp2ReadFileComplete = "com.lepu.ble.bp2.read.file.complete"        // 传输文件完成 Bp2BleFile
            const val EventBp2Reset = "com.lepu.ble.bp2.reset"                                // 复位 int(0 失败 1 成功)
            const val EventBp2FactoryReset = "com.lepu.ble.bp2.factory.reset"                 // 恢复出厂设置 int(0 失败 1 成功)
            const val EventBp2FactoryResetAll = "com.lepu.ble.bp2.factory.reset.all"          // 恢复生产出厂状态 int(0 失败 1 成功)
            const val EventBp2SyncTime = "com.lepu.ble.bp2.sync.time"                         // 同步时间 true
            const val EventBpSetConfigResult = "com.lepu.ble.bp2.measure.config"              // 设置心跳音开关 int(0 失败 1 成功)
            const val EventBpGetConfigResult = "com.lepu.ble.bp2.measure.getConfig"           // 心跳音开关 int(0 关 1 开)
            const val EventBpSwitchState = "com.lepu.ble.bp2.switch.state"                    // 切换设备状态 true
        }
    }

    /**
     * Er2BleInterface发出的通知
     * 包含model: model_er2
     */
    interface ER2{
        companion object{
            const val EventEr2Info = "com.lepu.ble.er2.info"                                  // 设备信息 Er2DeviceInfo
            const val EventEr2SetTime = "com.lepu.ble.er2.set.time"                           // 同步时间 true
            const val EventEr2SetSwitcherState = "com.lepu.ble.er2.set.switcher.state"        // 设置心跳音 true
            const val EventEr2SwitcherState = "com.lepu.ble.er2.switcher.state"               // 配置参数 byte数组
            const val EventEr2Reset = "com.lepu.ble.er2.reset"                                // 复位 true
            const val EventEr2FactoryReset = "com.lepu.ble.er2.factory.reset"                 // 恢复出厂设置 true
            const val EventEr2FactoryResetAll = "com.lepu.ble.er2.factory.reset.all"          // 恢复生产出厂状态 true
            const val EventEr2RtData = "com.lepu.ble.er2.realtime.data"                       // 实时数据 Er2RtData
            const val EventEr2FileList = "com.lepu.ble.er2.file.list"                         // 文件列表 Er2FileList
            const val EventEr2ReadFileError = "com.lepu.ble.er2.file.read.error"              // 传输文件出错 true
            const val EventEr2ReadingFileProgress = "com.lepu.ble.er2.file.reading.progress"  // 传输文件进度 int(0-100)
            const val EventEr2ReadFileComplete = "com.lepu.ble.er2.file.read.complete"        // 传输文件完成 Er2File
        }
    }

    /**
     * PC60FwBleInterface，Pc6nBleInterface发出的通知
     * 包含model: model_pc60fw，model_pc_6n
     */
    interface PC60Fw{
        companion object{
            const val EventPC60FwRtDataParam = "com.lepu.ble.pc60fw.rt.data.param"     // 血氧参数 PC60FwBleResponse.RtDataParam
            const val EventPC60FwRtDataWave = "com.lepu.ble.pc60fw.rt.data.wave"       // 血氧波形 PC60FwBleResponse.RtDataWave
            const val EventPC60FwBattery = "com.lepu.ble.pc60fw.battery"               // 电池电量 PC60FwBleResponse.Battery
            const val EventPC60FwWorkingStatus = "com.lepu.ble.pc60fw.working.status"  // 工作状态 PC60FwBleResponse.WorkingStatus
            const val EventPC60FwDeviceInfo = "com.lepu.ble.pc60fw.device.info"        // 设备信息 Pc6nDeviceInfo
        }
    }

    /**
     * PC80BleInterface发出的通知
     * 包含model: model_pc80b
     */
    interface PC80B{
        companion object{
            const val EventPc80bBatLevel = "com.lepu.ble.pc80b.bat.level"                         // 电池电量 int(0-3)
            const val EventPc80bDeviceInfo = "com.lepu.ble.pc80b.device.info"                     // 设备信息 PC80BleResponse.DeviceInfo
            const val EventPc80bTrackData = "com.lepu.ble.pc80b.track.data"                       // 实时数据 PC80BleResponse.RtTrackData
            const val EventPc80bReadFileError = "com.lepu.ble.pc80b.file.read.error"              // 传输文件出错 true
            const val EventPc80bReadingFileProgress = "com.lepu.ble.pc80b.file.reading.progress"  // 传输文件进度 int(0-100)
            const val EventPc80bReadFileComplete = "com.lepu.ble.pc80b.file.read.complete"        // 传输文件完成 PC80BleResponse.ScpEcgFile
        }
    }

    /**
     * FhrBleInterface发出的通知
     * 包含model: model_fhr
     */
    interface FHR{
        companion object{
            const val EventFhrDeviceInfo = "com.lepu.ble.fhr.device.info"  // 设备信息 FhrBleResponse.DeviceInfo
            const val EventFhrAudioData = "com.lepu.ble.fhr.audio.data"    // 音频数据 byte数组
        }
    }

    /**
     * Bpw1BleInterface发出的通知
     * 包含model: model_bpw1
     */
    interface BPW1{
        companion object{
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
     * 包含model: model_pc100
     */
    interface PC100{
        companion object{
            const val EventPc100DeviceInfo = "com.lepu.ble.pc100.device.info"         // 设备信息 Pc100DeviceInfo
            const val EventPc100BpResult = "com.lepu.ble.pc100.bp.result"             // 血压测量结果 Pc100BleResponse.BpResult
            const val EventPc100BpErrorResult = "com.lepu.ble.pc100.bp.error.result"  // 血压测量错误结果 Pc100BleResponse.BpResultError
            const val EventPc100BpStart = "com.lepu.ble.pc100.bp.start"               // 血压开始测量 true
            const val EventPc100BpStop = "com.lepu.ble.pc100.bp.stop"                 // 血压停止测量 true
            const val EventPc100BpStatus = "com.lepu.ble.pc100.bp.status"             // 血压测量状态 Pc100BleResponse.BpStatus
            const val EventPc100BpRtData = "com.lepu.ble.pc100.bp.rtdata"             // 血压实时测量值 Pc100BleResponse.RtBpData
            const val EventPc100BoRtWave = "com.lepu.ble.pc100.bo.rtwave"             // 血氧实时波形数据 byte数组
            const val EventPc100BoRtParam = "com.lepu.ble.pc100.bo.rtparam"           // 血氧实时测量值 Pc100BleResponse.RtBoParam
        }
    }

}
```



## `EventMsgConst` 

```
object EventMsgConst {

    interface Ble{
        companion object{
            const val EventServiceConnectedAndInterfaceInit = "com.lepu.ble.service.interface.init"  // 服务连接后初始化interface成功会发送 true
            const val EventBleDeviceReady = "com.lepu.ble.device.ready"  // 没有同步时间的设备连接成功后会发送 true
        }
    }


    /**
     * ble discovery
     */
    interface Discovery{
        companion object{
            const val EventDeviceFound = "com.lepu.ble.device.found"  // 扫描到设备会发送 Bluetooth
            const val EventDeviceFound_Device = "com.lepu.ble.device.found.device"  // 开始扫描设置需要配对的信息 BluetoothDevice
            const val EventDeviceFound_ScanRecord = "com.lepu.ble.device.found.scanResult"  // 开始扫描设置需要配对的信息 ScanRecord
            const val EventDeviceFound_ER1_UPDATE = "com.lepu.ble.device.found.er1Update"  // 扫描到er1 updater设备会发送
            const val EventDeviceFoundForUnRegister = "com.lepu.ble.device.found.unregister"  // 不需要添加model BluetoothDevice
            const val EventDeviceFound_ScanRecordUnRegister = "com.lepu.ble.device.found.scanResult.unregister"
        }

    }

    /**
     * ble realtime task
     * 发送通知携带model
     */
    interface RealTime{
        companion object{
            //实时波形
            const val EventRealTimeStart = "com.lepu.ble.realtime.start"  // 开启实时监测任务后会发送 model
            const val EventRealTimeStop = "com.lepu.ble.realtime.stop"  // 停止实时监测任务后会发送 model


            //bp2 获取实时状态
            const val EventRealTimeStateStart = "com.lepu.ble.realtime.state.start"
            const val EventRealTimeStateStop = "com.lepu.ble.realtime.state.stop"
        }
    }

    interface Download{
        companion object{
            const val EventIsPaused = "com.lepu.ble.download.paused"  // 暂停下载设备文件会发送
            const val EventIsContinue = "com.lepu.ble.download.continue"  // 停止下载设备文件会发送
            const val EventIsCancel = "com.lepu.ble.download.cancel"  // 取消下载设备文件会发送
        }
    }

    interface Updater{
        companion object{
            const val EventBleConnected = "com.lepu.ble.updater.ble.connected"  // 升级设备连接成功会发送
        }
    }

    interface Cmd{
        companion object{
            const val EventCmdResponseTimeOut = "com.lepu.ble.cmd.response.timeout"  // 指令响应超时会发送
        }
    }
}
```



## `TH12` 

th12存储文件格式说明：文件头+数据内容

数据内容是从文件头(固定2901个字节)结束开始追加，每次追加**两秒数据**(数据头8个字节+导联数据9000个字节)

* `Th12BleFile(fileName: String)` 创建对象

* `parseHeadData(byteArray: ByteArray)` 

  > 解析文件头数据
  >
  > 传入参数：设备.dat文件的前2901字节

* `getValidLength()` ：获取文件有效心电长度

* `getTwoSecondEcgData(byteArray: ByteArray)` 

  >传入参数：9008字节原始数据
  >
  >返回值：9000字节心电数据

* `getMitHeadData()` ：转Mit格式的.hea文件内容

* `getTwoSecondLeadData(leadName: String, ecgData: ByteArray)` 

  >传入参数：导联名称，9000字节心电数据
  >
  >返回值：导联采样点数值
  >
  >导联名称： I, II, V1, V2, V3, V4, V5, V6, Pacer, III, aVR, aVL, aVF
  >
  >mv = 采样点数值 / 655.36

* `getFileCreateTime()` 

  > 获取文件创建时间
  >
  > 时间格式yyyy-mm-dd hh:mm:ss

* `getEcgTime()` ：心电时长，单位为秒



## `胎心仪FHR` 

* `FhrBleFile` 静态类 

  采集的音频数据byte[]（二进制文件内容）

  ---> `decode()` 得到解压后的音频数据，采样率为2000（返回数据可存储为后缀名`.pcm` 文件）

  ---> `resample()` 得到变采样后的音频数据（2000 --->8000，返回数据可存储为后缀名`.pcm` 文件）

  ---> `convertWav()` 得到可播放音频数据（返回数据可存储为后缀名`.wav` 文件）
