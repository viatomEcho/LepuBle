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
  
- 2.0.0.12

  >pc80b降本指令应答问题
  >
  >er1添加烧录出厂设置和加密flash接口
  
- 2.0.0.13

  >添加ap20设备，LeW3手表部分功能
  >
  >bp2设置mtu为247
  >
  >优化一些规范
  
- 2.0.0.15

  >集成vetcorder，babyO2设备
  >
  >Scanner判空初始化问题
  >
  >BleService服务销毁不重新启动
  
- 2.0.0.16

  >集成bp2w设备
  
- 2.0.0.18

  >startScan开启扫描scanModel参数非必传
  >
  >添加Bluetooth.getDeviceName接口
  >
  >开启扫描3秒未返回结果，sdk自动重新扫描
  >
  >er1，er2添加设置参数异常消息
  
- 2.0.0.22

  >集成sp20，LeBp2w设备
  >
  >添加具体蓝牙名或蓝牙地址扫描接口
  
- 2.0.1.2

  >集成Aoj20a和Vtm20f
  
- 2.0.1.5

  >修复Vtm20f写特征校验导致无法连接问题

- 2.0.1.6

  >pc80b添加连续模式监听，兼容新版本PC80B（添加实时心率）
  >
  >修改F4体脂秤蓝牙名
  >
  >血氧使能开关由app控制，涉及设备ap20（默认打开）、sp20（默认关闭）、pc60fw（默认打开）

- 2.0.1.7

  >EventBleDeviceReady消息返回值类型修改为int(model)

- 2.0.1.8

  >集成BP2T，PC60NW，POD-2W，POD-1W
  >
  >集成Oxyfit，OxySmart等血氧系列设备

- 2.0.1.9

  >处理device.name为null情况
  >
  >修复pc80b连续测量应答问题
  >
  >集成胎心仪、checkme pod
  
- 2.0.1.10

  >集成PC-300
  >
  >集成PF-10A, PF-10B, PF-20A, PF-20B
  
- 2.0.1.11

  >集成护颈仪
  >
  >修改PF-20A，PF-20B接收数据速率问题
  
- 2.0.1.12

  >集成降本60NW
  >
  >集成S1体脂秤
  
- 2.0.1.20

  >兼容F5蓝牙名体脂秤
  >
  >集成CheckADV
  >
  >修复sdk问题
  
- 2.0.2.3

  >集成OxyRing、BBMS S1、BBMS S2
  >
  >bp2和bpm消息数据类型修改
  >
  >修改重连机制问题，优化扫描

- 2.0.2.4

  >兼容Android12
  >
  >er2分析文件解析
  >
  >bp2wifi血压文件测量模式字段解析
  >
  >发送扫描异常消息EventMsgConst.Discovery.EventDeviceFoundError

- 2.0.2.7

  >集成4G手表、OxyU、S5W设备
  >
  >合并线上切换设备扫描条件重置问题
  >
  >ex分析结果添加原始bytes的获取
  >
  >刷新扫描蓝牙名和空格问题，日志完善

- 2.0.2.9

  >添加HMM1、HHM2、HHM3、HHM4、AI S100、LP ER2设备
  >
  >修改4G手表问题
  >
  >bp2心电文件解析问题
  >
  >代码测试调整，规范输出工程软件

- 2.0.2.10

  >添加S6W、S7W、S7BW设备
  >
  >集成Bioland-BGM血糖仪、LPM311血脂仪、三合一(血糖、血酮、尿酸)设备
  >
  >OxySmart传输速率问题
  >
  >指甲血氧添加code





## `主要类说明` 

- `BleServiceHelper` ：初始化SDK，扫描，连接，断连，重连等
- `InterfaceEvent` ，`EventMsgConst` ：监听LiveEventBus消息接收数据
- `BIOL` ：订阅蓝牙状态监听
- `BleChangeObserver` ：监听蓝牙状态，必须订阅蓝牙状态监听，否则无法接收蓝牙状态
- `BleServiceObserver` ：监听蓝牙服务状态
- 每个model对应一个interface，由app管理interface（**使用sdk连接设备前必须确保已经设置对应model的interface**）



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

- `startScan(scanModel: Int? = null, needPair: Boolean = false)` 

  > 开始扫描
  >
  > scanModel：非空，对应model发送通知；为空，sdk添加的model都发送通知
  >
  > needPair：本次扫描是否需要发送配对信息，默认是false
  >
  
- `startScan(scanModel: IntArray, needPair: Boolean = false)` ：开始扫描，多个model设备

- `stopScan()` ：停止扫描

- `startScanByName(deviceName: String, scanModel: Int? = null, needPair: Boolean = false)` ：具体蓝牙名扫描

- `startScanByAddress(address: String, scanModel: Int? = null, needPair: Boolean = false)`：具体蓝牙地址扫描

- `setInterfaces(model: Int, runRtImmediately: Boolean = false)` 

  > 在initService()之后调用，**连接设备前必须已有对应interface设置** 
  >
  > 设置model的interface
  >
  > runRtImmediately：是否立即开启实时监测任务，默认是false

- `getInterface(model: Int)` ：获取model的interface

- `getInterfaces()` ：获取服务中所有的interface

- `connect(context: Context, model: Int, b: BluetoothDevice, isAutoReconnect: Boolean = true, toConnectUpdater: Boolean = false)`  

  > 连接成功后自动同步时间，但Pc80b，Pc60fw，胎心仪Fhr，Pc100，Pc66b设备没有同步时间设置，连接成功后直接发送EventMsgConst.Ble.EventBleDeviceReady
  >
  > 每次发起**连接前必须关闭扫描** ，**连接设备前必须已有对应interface设置** 
  >
  > isAutoReconnect：是否自动重连，默认是true
  >
  > toConnectUpdater：检查是否是连接升级失败的设备，默认是false

- `reconnect(scanModel: Int, name: String, needPair: Boolean = false, toConnectUpdater: Boolean = false)` ：通过**蓝牙设备名称** 重新连接，单个model设备

- `reconnect(scanModel: IntArray, name: Array<String>, needPair: Boolean = false, toConnectUpdater: Boolean = false)` ：重新连接多个model设备

- `reconnectByAddress(scanModel: Int, macAddress: String, needPair: Boolean = false, toConnectUpdater: Boolean = false)` ：通过**蓝牙地址** 重新连接，单个model设备

- `reconnectByAddress(scanModel: IntArray, macAddress: Array<String>, needPair: Boolean, toConnectUpdater: Boolean = false)` ：重新连接多个model设备

  >**蓝牙名称一致的设备重连使用蓝牙地址重连方法** ，可通过canReconnectByName()方法判断

- `disconnect(autoReconnect: Boolean)` ：所有设备断开连接

- `disconnect(model: Int, autoReconnect: Boolean)` ：单设备断开连接

- `canReconnectByName(model: Int)` ：判断蓝牙名是否唯一

- `getConnectState(model: Int)` ：获取model连接状态

- `syncTime(model: Int)` ：同步时间

- `getInfo(model: Int)` ：获取主机信息

- `getFileList(model: Int, fileType: Int? = null)` 

  > 获取设备文件列表
  >
  > fileType：LeBp2w (LeBp2wBleCmd.FileType)，CheckmeLE (CheckmeLeBleCmd.ListType)

- `readFile(userId: String, fileName: String, model: Int, offset: Int = 0)` ：读取主机文件，进入读文件流程前APP要手动停止实时任务状态

- `cancelReadFile(model: Int)` ：取消读取主机文件

- `pauseReadFile(model: Int)` ：暂停读取主机文件

- `continueReadFile(model: Int, userId: String, fileName: String, offset: Int)` ：继续读取主机文件

- `factoryResetAll(model: Int)` ：恢复生产出厂状态

- `factoryReset(model: Int)` ：恢复出厂设置

- `reset(model: Int)` ：设备复位

- `setRTDelayTime(model: Int, delayMillis: Long)` ：设置实时监测任务的间隔时间

- `startRtTask(model: Int)` ：开启实时监测任务

- `stopRtTask(model: Int, sendCmd: () -> Unit = {})` 

  > 停止实时监测任务
  >
  > sendCmd：停止实时后指定执行方法

- `isRtStop(model: Int)` ：是否已停止实时监测任务

- `startBp(model: Int)` ：开始测量血压，支持设备有Bpm，Bpw1，Pc100

- `stopBp(model: Int)` ：停止测量血压

- `burnFactoryInfo(model: Int, config: FactoryConfig)` ：烧录出厂信息，支持设备有Er1，Er2，O2系列

- `burnLockFlash(model: Int)` ：加密Flash，支持设备有Er1，Er2

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

- `sendHeartbeat(model: Int)` ：发送心跳包查询电量，支持设备有Pc80b

- `bpw1SetMeasureTime(model: Int, measureTime: Array<String?>)` 
  
  > 设置定时测量血压时间
  >
  > String格式："startHH,startMM,stopHH,stopMM,interval,serialNum,totalNum"
  
- `bpw1GetMeasureTime(model: Int)` ：获取定时测量血压时间

- `bpw1SetTimingSwitch(model: Int, timingSwitch: Boolean)` ：设置定时测量血压开关，支持设备有Bpw1

------------------------------------------------------------O2系列-----------------------------------------------------

- `oxyGetPpgRt(model: Int)` ：获取实时原始数据，支持设备O2系列
- `oxyGetWave(model: Int)` ：获取实时波形数据，支持设备O2系列
- `oxyGetRtParam(model: Int)` ：获取实时参数值，支持设备O2系列
- `updateSetting(model: Int, type: String, value: Any)` ：更新设备设置，支持设备O2系列

----------------------------------------------------------------ap20------------------------------------------------------

- `ap20SetConfig(model: Int, type: Int, config: Int)` 

  > ap20设置配置信息
  >
  > type：Ap20BleCmd.ConfigType
  >
  > type 0：设置背光等级（config：0-5）
  >
  > type 1：警报功能开关（config：0 off，1 on）
  >
  > type 2：血氧过低阈值（config：85-99）
  >
  > type 3：脉率过低阈值（config：30-99）
  >
  > type 4：脉率过高阈值（config：100-250）

- `ap20GetConfig(model: Int, type: Int)` ：ap20获取配置信息

- `ap20EnableRtData(model: Int, type: Int, enable: Boolean)` 

  >使能实时数据发送
  >
  >type：Ap20BleCmd.EnableType

- `ap20GetBattery(model: Int)` ：获取电量

---------------------------------------------------------lew手表-----------------------------------------------------

- `lewBoundDevice(model: Int, bound: Boolean)` 

  >绑定/解绑设备
  >
  >bound：true绑定，false解绑

- `lewGetBattery(model: Int)` ：获取电量

- `lewFindDevice(model: Int)` ：寻找设备

- `lewGetSystemSetting(model: Int)` ：获取系统配置信息

- `lewSetSystemSetting(model: Int, setting: SystemSetting)` 

  >设置系统信息，包括语言、单位、翻腕亮屏、佩戴方式（左右手）
  >
  >SystemSetting：LewBleCmd.Language、UnitSetting、HandRaiseSetting、LewBleCmd.Hand

- `lewGetLanguage(model: Int)` ：查询语言

- `lewSetLanguage(model: Int, language: Int)` 

  >设置语言
  >
  >language：LewBleCmd.Language

- `lewGetUnit(model: Int)` ：查询单位

- `lewSetUnit(model: Int, unit: UnitSetting)` 

  >设置单位
  >
  >UnitSetting：
  >
  >lengthUnit：长度单位，LewBleCmd.Unit
  >
  >weightUnit：体重单位，LewBleCmd.Unit
  >
  >tempUnit：温度单位，LewBleCmd.Unit

- `lewGetHandRaise(model: Int)` ：查询翻腕亮屏配置信息

- `lewSetHandRaise(model: Int, handRaise: HandRaiseSetting)` 

  > 设置翻腕亮屏信息
  >
  > HandRaiseSetting：
  >
  > switch：开关
  >
  > startHour：开始小时
  >
  > startMin：开始分钟
  >
  > stopHour：结束小时
  >
  > stopMin：结束分钟

- `lewGetLrHand(model: Int)` ：查询佩戴方式

- `lewSetLrHand(model: Int, hand: Int)` 

  >设置佩戴方式
  >
  >hand：左右手，LewBleCmd.Hand

- `lewGetNoDisturbMode(model: Int)` ：查询勿扰模式配置信息

- `lewSetNoDisturbMode(model: Int, mode: NoDisturbMode)` 

  > 设置勿扰模式信息
  >
  > NoDisturbMode：
  >
  > switch：开关
  >
  > itemSize：勿扰时段数量，最多5个
  >
  > items：勿扰时段开始结束时间

- `lewGetAppSwitch(model: Int)` ：查询app消息提醒开关

- `lewSetAppSwitch(model: Int, app: AppSwitch)` 

  > 设置app消息提醒开关
  >
  > AppSwitch：
  >
  > all：总开关

- `lewSendNotification(model: Int, info: NotificationInfo)` 

  > 发送消息
  >
  > NotificationInfo：
  >
  > appId：LewBleCmd.AppId
  >
  > time：消息时间戳，单位秒
  >
  > info：NotificationInfo.NotiPhone，NotificationInfo.NotiMessage，NotificationInfo.NotiOther

- `lewGetDeviceMode(model: Int)` ：查询设备模式

- `lewSetDeviceMode(model: Int, mode: Int)` 

  >设置设备模式
  >
  >mode：LewBleCmd.DeviceMode

- `lewGetAlarmClock(model: Int)` ：查询闹钟信息

- `lewSetAlarmClock(model: Int, info: AlarmClockInfo)` 

  > 设置闹钟
  >
  > AlarmClockInfo：
  >
  > itemSize：闹钟数量
  >
  > items：闹钟时间

- `lewGetPhoneSwitch(model: Int)` ：查询手机来信、短信消息提醒开关

- `lewSetPhoneSwitch(model: Int, phone: PhoneSwitch)` 

  > 设置手机消息来信、短信消息提醒开关
  >
  > PhoneSwitch：
  >
  > call：来电
  >
  > message：短信

- `lewGetMeasureSetting(model: Int)` ：获取测量配置信息

- `lewSetMeasureSetting(model: Int, setting: MeasureSetting)` 

  >设置测量信息，包括运动目标值、达标提醒、久坐提醒、自测心率
  >
  >MeasureSetting：SportTarget、targetRemind、SittingRemind、HrDetect

- `lewGetSportTarget(model: Int)` ：查询运动目标值

- `lewSetSportTarget(model: Int, target: SportTarget)` 

  > 设置运动目标值
  >
  > SportTarget：
  >
  > step：单位:步数
  >
  > distance：单位:⽶
  >
  > calories：单位:卡路⾥
  >
  > sleep：单位:分钟
  >
  > sportTime：单位:分钟

- `lewGetTargetRemind(model: Int)` ：查询达标提醒开关

- `lewSetTargetRemind(model: Int, remind: Boolean)` 

  > 设置达标提醒
  >
  > remind：true提醒，false不提醒

- `lewGetSittingRemind(model: Int)` ：查询久坐提醒配置信息

- `lewSetSittingRemind(model: Int, remind: SittingRemind)` 

  >设置久坐提醒信息
  >
  >SittingRemind：
  >
  >switch：开关
  >
  >noonSwitch：午休免打扰中午12:00⾄下午02:00不要提醒
  >
  >weekRepeat：bit0->Sunday,bit1->monday,bit6->saturday
  >
  >everySunday：是否提醒
  >
  >startHour：起始小时
  >
  >startMin：起始分钟
  >
  >stopHour：结束小时
  >
  >stopMin：结束分钟

- `lewGetHrDetect(model: Int)` ：查询自测心率配置信息

- `lewSetHrDetect(model: Int, detect: HrDetect)` 

  >设置自测心率信息
  >
  >HrDetect：
  >
  >switch：自测心率开关
  >
  >interval：自测间隔，单位分钟

- `lewGetUserInfo(model: Int)` ：查询用户信息

- `lewSetUserInfo(model: Int, info: UserInfo)` 

  >设置用户信息
  >
  >UserInfo：
  >
  >aid：主账号id
  >
  >uid：用户id
  >
  >fName：姓
  >
  >name：名
  >
  >birthday：生日yyyy-mm-dd
  >
  >height：身高cm
  >
  >weight：体重kg
  >
  >gender：性别，LewBleCmd.Gender

- `lewGetPhoneBook(model: Int)` ：查询通讯录

- `lewSetPhoneBook(model: Int, book: PhoneBook)` 

  >同步通讯录
  >
  >PhoneBook：
  >
  >leftSize：剩余待传输数量
  >
  >currentSize：当前传输数量
  >
  >items：联系人信息

- `lewGetSosContact(model: Int)` ：查询紧急联系人

- `lewSetSosContact(model: Int, sos: SosContact)` 

  >设置紧急联系人
  >
  >SosContact：
  >
  >switch：开关
  >
  >nameLen：姓名长度
  >
  >name：姓名
  >
  >phoneLen：电话号码长度
  >
  >phone：电话号码

- `lewGetFileList(model: Int, type: Int, startTime: Int)` 

  >获取记录数据
  >
  >type：数据类型，LewBleCmd.ListType
  >
  >startTime：起始时间戳，单位秒

- `lewGetHrThreshold(model: Int)` ：查询心率阈值配置信息

- `lewSetHrThreshold(model: Int, threshold: HrThreshold)` 

  >设置心率阈值信息，大于等于阈值提醒
  >
  >HrThreshold：
  >
  >switch：开关
  >
  >threshold：心率阈值（范围0-255）

- `lewGetOxyThreshold(model: Int)` ：查询血氧阈值配置信息

- `lewSetOxyThreshold(model: Int, threshold: OxyThreshold)` 

  >设置血氧阈值信息，小于等于阈值提醒
  >
  >OxyThreshold：
  >
  >switch：开关
  >
  >threshold：血氧阈值（范围0-255）

------------------------------------------------------------bp2--------------------------------------------------------

- `bp2SetConfig(model: Int, switch: Boolean, volume: Int = 2)` 

  > 设置配置信息，支持设备Bp2，Bp2A
  >
  > switch：声音开关
  >
  > volume：声音大小（0-3）

- `bp2SetConfig(model: Int, config: Bp2Config)` ：配置参数，支持设备Bp2W，LeBp2W

- `bp2GetConfig(model: Int)` ：获取配置信息，支持设备Bp2，Bp2A，Bp2W，LeBp2W

- `bp2SwitchState(model: Int, state: Int)` 

  > 切换设备状态，支持设备Bp2，Bp2A，Bp2W，LeBp2W
  >
  > state：Bp2BleCmd.SwitchState
  >
  > 0 进入血压测量，1 进入心电测量，2 进入历史回顾，3 进入开机预备状态，4 关机，5 进入理疗模式

- `bp2DeleteFile(model: Int)` ：删除文件，支持设备Bp2W，LeBp2W

- `bp2GetRtState(model: Int)` ：获取主机实时状态，支持设备Bp2W，LeBp2W

- `bp2GetWifiDevice(model: Int)` ：获取路由，支持设备Bp2W，LeBp2W

- `bp2SetWifiConfig(model: Int, config: Bp2WifiConfig)` ：配置WiFi信息，支持设备Bp2W，LeBp2W

- `bp2GetWifiConfig(model: Int)` ：获取WiFi配置信息，支持设备Bp2W，LeBp2W

- `bp2GetFileListCrc(model: Int, fileType: Int)` 

  > 获取文件列表校验码，支持设备LeBp2W
  >
  > fileType：LeBp2wBleCmd.FileType

- `bp2WriteUserList(model: Int, userList: Bp2wUserList)` ：写用户信息，支持设备LeBp2W

---------------------------------------------------------------sp20-----------------------------------------------------

- `sp20SetConfig(model: Int, config: Sp20Config)` ：配置参数

- `sp20GetConfig(model: Int, type: Int)` ：获取参数

- `sp20EnableRtData(model: Int, type: Int, enable: Boolean)` 

  >使能实时数据发送
  >
  >type：Sp20BleCmd.EnableType

- `sp20GetBattery(model: Int)` ：获取电量

--------------------------------------------------------aoj20a-----------------------------------------------------

- `aoj20aDeleteData(model: Int)` ：删除历史数据

-----------------------pc60fw，pc66b，oxysmart，pod1w，pod2w，pc60nw---------------------

- `pc60fwEnableRtData(model: Int, type: Int, enable: Boolean)` ：使能实时数据发送

  >type : Pc60FwBleCmd.EnableType
  
- `pc60fwGetBranchCode(model: Int)` ：获取code

- `pc60fwSetBranchCode(model: Int, code: String)` ：设置code

------------------------------------------------VTM AD5, MD1000AF4----------------------------------

- `ad5EnableRtData(model: Int, enable: Boolean)` ：使能实时数据发送

-------------------------------------------------------LEM----------------------------------------

- `lemGetBattery(model: Int)` ：获取电量

- `lemHeatMode(model: Int, on: Boolean)` : 恒温加热模式开关

- `lemMassageMode(model: Int, mode: Int)` 

  >设置按摩模式
  >
  >mode：LemBleCmd.MassageMode

- `lemMassageLevel(model: Int, level: Int)` 

  >设置按摩力度等级
  >
  >level：1-15挡，0关闭

- `lemMassageTime(model: Int, time: Int)` 

  >设置按摩时间
  >
  >time：LemBleCmd.MassageTime



## `InterfaceEvent` 

```kotlin
class InterfaceEvent(val model: Int, val data: Any): LiveEvent {

    /**
     * Oxy LiveDataBus Event
     * OxyBleInterface发出的通知
     * 包含model: MODEL_O2RING, MODEL_BABYO2, MODEL_BABYO2N,
     *           MODEL_CHECKO2, MODEL_O2M, MODEL_SLEEPO2,
     *           MODEL_SNOREO2, MODEL_WEARO2, MODEL_SLEEPU,
     *           MODEL_OXYLINK, MODEL_KIDSO2, MODEL_OXYFIT,
     *           MODEL_OXYRING, MODEL_BBSM_S1, MODEL_BBSM_S2,
     *           MODEL_OXYU, MODEL_AI_S100
     *
     * MODEL_BABYO2N 接收 EventBleDeviceReady 消息为连接成功，因为需要app先同步设备信息再同步时间处理或者在3s后再发指令给设备
     */
    interface Oxy {
        companion object {
            const val EventOxyReadFileError = "com.lepu.ble.oxy.read.file.error"              // 传输文件失败 true
            const val EventOxyReadFileComplete = "com.lepu.ble.oxy.read.file.complete"        // 传输文件成功 OxyBleResponse.OxyFile
            const val EventOxyReadingFileProgress = "com.lepu.ble.oxy.reading.file.progress"  // 当前文件进度 展示时：(dialogProgress / 10.0) + "%")
            const val EventOxyFactoryReset = "com.lepu.ble.oxy.factory.reset"                 // 恢复出厂设置 true
            const val EventOxyBurnFactoryInfo = "com.lepu.ble.oxy.burn.factory.info"          // 烧录设备信息 true
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
     *           MODEL_HHM1, MODEL_HHM2, MODEL_HHM3
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
            const val EventBpmRtData = "com.lepu.ble.bpm.rtData"                 // 实时压力值 BpmBleResponse.RtData
            const val EventBpmState = "com.lepu.ble.bpm.state"                   // 实时状态 BpmBleResponse.RtState
            const val EventBpmSyncTime = "com.lepu.ble.bpm.sync.time"            // 同步时间 true
            const val EventBpmRecordData = "com.lepu.ble.bpm.record.data"        // 记录数据 BpmBleResponse.RecordData
            const val EventBpmRecordEnd = "com.lepu.ble.bpm.record.end"          // 传输完成 true
            const val EventBpmMeasureResult = "com.lepu.ble.bpm.measure.result"  // 测量结果 BpmBleResponse.RecordData
            const val EventBpmMeasureErrorResult = "com.lepu.ble.bpm.measure.error.result"  // 测量错误结果 BpmBleResponse.ErrorResult
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
            const val EventBp2SetConfigResult = "com.lepu.ble.bp2.set.config.result"          // 设置心跳音开关 int(0：失败 1：成功)
            const val EventBp2GetConfigResult = "com.lepu.ble.bp2.get.config.result"          // 获取配置信息 Bp2Config
            const val EventBp2GetConfigError = "com.lepu.ble.bp2.get.config.error"            // 获取配置信息失败 true
            const val EventBp2SyncTime = "com.lepu.ble.bp2.sync.time"                         // 同步时间 boolean
            const val EventBp2SwitchState = "com.lepu.ble.bp2.switch.state"                   // 切换设备状态 boolean
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
            const val EventLeBp2wSyncUtcTime = "com.lepu.ble.le.bp2w.sync.utc.time"                  // 同步UTC时间 boolean
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
     * 包含model: MODEL_ER2, MODEL_LP_ER2
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
            const val EventEr2BurnFactoryInfo = "com.lepu.ble.er2.burn.factory.info"          // 烧录出厂信息 boolean
            const val EventEr2BurnLockFlash = "com.lepu.ble.er2.burn.lock.flash"              // 加密Flash boolean
        }
    }

    /**
     * PC60FwBleInterface发出的通知
     * 包含model: MODEL_PC60FW, MODEL_PC66B, MODEL_OXYSMART,
     *           MODEL_POD_1W, MODEL_POD2B, MODEL_PC_60NW_1,
     *           MODEL_PC_60B, MODEL_PF_10A, MODEL_PF_10B,
     *           MODEL_PF_20A, MODEL_PF_20B, MODEL_PC_60NW,
     *           MODEL_S5W, MODEL_S6W, MODEL_S7W, MODEL_S7BW
     */
    interface PC60Fw {
        companion object {
            const val EventPC60FwRtDataParam = "com.lepu.ble.pc60fw.rt.data.param"     // 血氧参数 PC60FwBleResponse.RtDataParam
            const val EventPC60FwRtDataWave = "com.lepu.ble.pc60fw.rt.data.wave"       // 血氧波形 PC60FwBleResponse.RtDataWave
            const val EventPC60FwBattery = "com.lepu.ble.pc60fw.battery"               // 电池电量 PC60FwBleResponse.Battery
            /**
             * MODEL_POD2B设备名解出PC-60B不正确，协议问题，设备端暂不处理
             * app可识别model处理为POD-2
             */
            const val EventPC60FwDeviceInfo = "com.lepu.ble.pc60fw.device.info"        // 设备信息 BoDeviceInfo
            const val EventPC60FwWorkingStatus = "com.lepu.ble.pc60fw.working.status"  // 工作状态 PC60FwBleResponse.WorkingStatus
            const val EventPC60FwOriginalData = "com.lepu.ble.pc60fw.original.data"    // 红外数据 PC60FwBleResponse.OriginalData
            const val EventPC60FwSetCode = "com.lepu.ble.pc60fw.set.code"              // 设置code boolean
            const val EventPC60FwGetCode = "com.lepu.ble.pc60fw.get.code"              // 获取code String
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
     * LewBleInterface发出的通知
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
            const val EventAOJ20aTempList = "com.lepu.ble.aoj20a.temp.list"             // 历史测量数据 ArrayList<Aoj20aBleResponse.TempRecord>
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
            const val EventVcominRtHr = "com.lepu.ble.vcomin.rt.hr"  // 实时心率 FhrData
        }
    }

    /**
     * Ad5FhrBleInterface
     * 包含model: MODEL_VTM_AD5, MODEL_FETAL
     */
    interface AD5 {
        companion object {
            const val EventAd5RtHr = "com.lepu.ble.ad5.rt.hr"  // 实时心率 FhrData
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
            const val EventPc300TempResult = "com.lepu.ble.pc300.temp.result"         // 温度结果 Pc300BleResponse.TempResult
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

    /**
     * LemBleInterface
     * 包含model: MODEL_LEM
     */
    interface LEM {
        companion object {
            const val EventLemDeviceInfo = "com.lepu.ble.lem.device.info"             // LemBleResponse.DeviceInfo
            const val EventLemBattery = "com.lepu.ble.lem.battery"                    // int 1-100%
            const val EventLemSetHeatMode = "com.lepu.ble.lem.set.heat.mode"          // boolean (true设置开成功，false设置关成功)
            const val EventLemSetMassageMode = "com.lepu.ble.lem.set.massage.mode"    // int (LemBleCmd.MassageMode) 设置成功
            const val EventLemSetMassageLevel = "com.lepu.ble.lem.set.massage.level"  // int (1-15，0关闭) 设置成功
            const val EventLemSetMassageTime = "com.lepu.ble.lem.set.massage.time"    // int (LemBleCmd.MassageTime) 设置成功
        }
    }

    /**
     * LeS1BleInterface发出的通知
     * 包含model: MODEL_LES1
     */
    interface LES1 {
        companion object {
            const val EventLeS1Info = "com.lepu.ble.les1.info"                                  // 设备信息 LepuDevice
            const val EventLeS1RtData = "com.lepu.ble.les1.rtData"                              // 实时数据 LeS1BleResponse.RtData
            const val EventLeS1NoFile = "com.lepu.ble.les1.no.file"                             // 没有文件
            const val EventLeS1ReadFileError = "com.lepu.ble.les1.read.file.error"              // 传输文件出错 true
            const val EventLeS1ReadingFileProgress = "com.lepu.ble.les1.reading.file.progress"  // 传输文件进度 int(0-100)
            const val EventLeS1ReadFileComplete = "com.lepu.ble.les1.read.file.complete"        // 传输文件完成 LeS1BleResponse.Er1File
            const val EventLeS1Reset = "com.lepu.ble.les1.reset"                                // 复位 boolean
            const val EventLeS1ResetFactory = "com.lepu.ble.les1.reset.factory"                 // 恢复出厂设置 boolean
            const val EventLeS1SetTime = "com.lepu.ble.les1.set.time"                           // 同步时间 boolean
        }
    }

    /**
     * Lpm311BleInterface
     * 包含model: MODEL_LPM311
     */
    interface LPM311 {
        companion object {
            const val EventLpm311Data = "com.lepu.ble.lpm311.data"  // 血脂数据 Lpm311Data
        }
    }

    /**
     * PoctorM3102BleInterface
     * 包含model: MODEL_POCTOR_M3102
     */
    interface PoctorM3102 {
        companion object {
            const val EventPoctorM3102Data = "com.lepu.ble.poctor.m3102.data"  // 测量结果 PoctorM3102Data
        }
    }

    /**
     * BiolandBgmBleInterface
     * 包含model: MODEL_BIOLAND_BGM
     */
    interface BiolandBgm {
        companion object {
            const val EventBiolandBgmDeviceInfo = "com.lepu.ble.bioland.bgm.device.info"       // BiolandBgmBleResponse.DeviceInfo
            const val EventBiolandBgmCountDown = "com.lepu.ble.bioland.bgm.count.down"         // int
            const val EventBiolandBgmGluData = "com.lepu.ble.bioland.bgm.glu.data"             // BiolandBgmBleResponse.GluData
            const val EventBiolandBgmNoGluData = "com.lepu.ble.bioland.bgm.no.glu.data"        // true
        }
    }

}
```



## `EventMsgConst` 

```kotlin
object EventMsgConst {

    interface Ble{
        companion object{
            const val EventServiceConnectedAndInterfaceInit = "com.lepu.ble.service.interface.init"  // 服务连接后初始化interface成功会发送 true
            const val EventBleDeviceReady = "com.lepu.ble.device.ready"  // 没有同步时间的设备连接成功后会发送 model
        }
    }


    /**
     * ble discovery
     */
    interface Discovery{
        companion object{
            const val EventDeviceFound = "com.lepu.ble.device.found"  // 扫描到sdk已有model设备会发送 Bluetooth
            const val EventDeviceFound_Device = "com.lepu.ble.device.found.device"  // 开始扫描设置需要配对的信息 Bluetooth/BluetoothDevice
            const val EventDeviceFound_ScanRecord = "com.lepu.ble.device.found.scanResult"  // 开始扫描设置需要配对的信息 ScanRecord
            const val EventDeviceFoundForUnRegister = "com.lepu.ble.device.found.unregister"  // 扫描到sdk没有model设备会发送 ScanResult
            const val EventDeviceFound_ScanRecordUnRegister = "com.lepu.ble.device.found.scanResult.unregister"
            const val EventDeviceFound_ER1_UPDATE = "com.lepu.ble.device.found.er1Update"  // 扫描到er1 updater设备会发送
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
            const val EventCmdResponseContent = "com.lepu.ble.cmd.response.content"  // 指令响应
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
