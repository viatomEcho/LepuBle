# BleSdk

## 集成新设备

- `Bluetooth`新增`BT_NAME`和`BT_MODEL`

- 新增`BleInterface` `BleManager`同系列设备可公用

- `EventMsgConst` `InterfaceEvent`中配置对应interface的事件的通知

- 在`BleService`中配置新增的model

  ```kotlin
  fun initInterfaces(m: Int, runRtImmediately: Boolean): BleInterface {
       Bluetooth.MODEL_ER2 -> {
                  Er1BleInterface(m).apply {
                      this.runRtImmediately = runRtImmediately
                      vailFace.put(m, this)
                      return this
                  }
  
              }
      else -> {
                  return throw Exception("BleService initInterfaces() 未配置此model:$m")
              }
  }
  ```

## 主要类说明

```kotlin
/**
 * author: wujuan
 * created on: 2021/1/20 17:41
 * description: 蓝牙指令、状态基类
 * 一个model对应一个Interface实例互不干扰。App中通过BleChangeObserver、BleInterfaceLifecycle向指定model(可多个)的Interface发起订阅，观察者无需管理生命周期，自身销毁时自动注销订阅
 * 订阅成功后interface将通过BleChangeObserver#onBleStateChanged()发布蓝牙更新状态
 *
 *  1.每次发起连接将默认将isAutoReconnect赋值为true，即在断开连接回调中会重新开启扫描，重连设备
 *
 *  2.如果进入到多设备重连{BleServiceHelper #isReconnectingMulti = true}则在其中一个设备连接之后再次开启扫描
 *
 *  3.通过runRtTask(),stopRtTask()控制实时任务的开关，并将发送相应的EventMsgConst.RealTime...通知
 *
 *  4.通过自定义InterfaceEvent，发送携带model的业务通知
 *
 */
abstract class BleInterface(val model: Int): ConnectionObserver, NotifyListener {
      /**
     * 蓝牙连接状态
     */
    internal var state = false


    /**
     * 连接中
     */
    private var connecting = false


    /**
     *  断开连接后是否重新开启扫描操作重连
     *  默认false
     *  当切换设备、解绑时应该置为false
     *  调用connect() 可重新赋值
     *
     */
    var isAutoReconnect: Boolean = false

    lateinit var manager: BaseBleManager
    lateinit var device: BluetoothDevice

    private var pool: ByteArray? = null

    /**
     * 是否在第一次获取设备信息后立即执行实时任务
     * 默认：false
     */
    var runRtImmediately: Boolean = false

    /**
     * 获取实时波形
     */
    private var count: Int = 0
    private val rtHandler = Handler(Looper.getMainLooper())
    private  var rTask: RtTask = RtTask()

    /**
     * 获取实时的间隔
     * 默认： 1000 ms
     */
    var  delayMillis: Long = 1000

    /**
     * 实时任务状态flag
     */
    var isRtStop: Boolean = true
}

```

```kotlin

/**
 * 一个为蓝牙通讯的服务的Service, {@link BleServiceHelper}是它的帮助类
 *
 * 1.在Application onCreate中, 通过BleServiceHelper初始化
 *
 * 2.继承LifecycleService使生命周期可被观察, 通过实现{@link BleServiceObserver}可自定义订阅者，订阅者通过观察此服务的生命周期变化，在不同阶段进行相应的工作
 *    如： 在Service OnCreate/ onDestroy时进行 数据库初始化/关闭
 *
 * 3.vailFace是保存BleInterface的SparseArray集合, 以设备的Model值为key, value为BleInterface
 *
 * 4.开启扫描时，可指定多个需要过滤的model，可指定是否发送pair配对通知
 *   - 来自绑定时发起的扫描：APP应该只在绑定页注册foundDevice和pair通知，接收到通知后由APP进行配对及连接
 *   - 来自重连时发起的扫描：不会发送pair和found通知。判断设备是否属于reconnectDeviceName， 是则由该model的Interface发起连接。
 *                         如果指定扫描多个model，将进入到多设备扫描状态{BleServiceHelper #isReconnectingMulti = true}则在某一个设备连接成功后，检查是否还有未连接的设备，是则继续重新开启扫描
 *
 * 5. 每次发起连接前必须关闭扫描
 *
 */

class BleService: LifecycleService() {
    
    val tag: String = "BleService"

    /**
     * 保存可用的BleInterface集合
     */
    var vailFace: SparseArray<BleInterface> = SparseArray()

    /**
     *  指定扫描的Model，扫描结果根据它的值过滤
     *  每次开启扫描时传入指定model，被重新赋值
     *
     */
    var scanModel: IntArray? = null

    /**
     * 本次扫描是否需要发送配对信息
     * 默认： false
     * 开启扫描被重新赋值
     */
    var needPair: Boolean = false


    /**
     * 本次扫描是否来自重连(已知蓝牙名)，默认false，通过reconnect()开启扫描被赋值true
     */
    var isReconnectScan: Boolean = false

    /**
     * 发起重连扫描时应匹配的蓝牙名集合
     */
    var reconnectDeviceName: Array<String>? = null
}

```

```kotlin
/**
 * 单例的蓝牙服务帮助类，原则上只通过此类开放API
 *
 * 1. 在Application onCreate()中初始化，完成必须配置(modelConfig、runRtConfig)后通过initService()开启服务#BleService。
 *
 *
 */
class BleServiceHelper private constructor() {

    /**
     * 下载数据的保存路径，key为model
     */
    var rawFolder: SparseArray<String>? = null

    /**
     * 服务onServiceConnected()时，应该初始化的model配置。必须在initService()之前完成
     * key为model
     */
    var modelConfig: SparseArray<Int> = SparseArray()
    /**
     * 服务onServiceConnected()时，应该初始化的model配置。必须在initService()之前完成
     * key为model
     */
    var runRtConfig: SparseArray<Boolean> = SparseArray()

    /**
     * 多设备模式手动重连中
     */
    var isReconnectingMulti: Boolean = false
```

## 特殊API说明

### BPM捷美瑞血压计

- 获取文件列表getBpmFileList(model: Int, map: HashMap<String, Any>)



# APP集成BleSdk指南

主题：除停留在`绑定页`时允许存在多个设备(DeviceType)的interface，其他时候蓝牙服务中应保持只有`已绑定的当前的`设备的interface，或者没有interface。(一个DeviceType可拥有多个model，一个model对应一个Interface实例)

## 依赖

api 'com.lepu.blepro:lepu-ble:0.0.5-alpha2'

**app中有关BleSdk api的调用全部封装在此， 不要在其他地方直接使用BleServiceHelper !!!!!**

```kotlin
class BleUtilService {}
```

##  1. 初始化(必读)

**主题：在Application onCreate时完成配置开启Service，初始化已绑定状态的当前的设备的Interface。服务完成初始化不会自动连接蓝牙，由APP必要的时刻检查蓝牙权限后，根据设备蓝牙名主动发起reconnect**

### 流程

- 获取设备列表（通过`Constants.DeviceType`区分设备, 某一deviceType可能对应多个Model）

- 初始化BleService

  - 配置log

  - 配置RawFolder--设备(model)源文件下载后保存路径

  - 配置ModelConfig--设备model(条件：当前Device && 已绑定)

  - 配置RtConfig --Interface接收getInfo响应后是否立即开启实时测量任务(条件：当前Device && 已绑定)

  - 初始化initService

    **`ModelConfig`、`RtConfig` 必须在initService之前配置，用于服务onServiceConnected时初始化**

    **interface** 

```kotlin
class AppLifecycleImpl


    private void initBlePro(Application application){
        initMyDeviceList(application);

        //配置所有设备的数据保存路径
        BLUETOOTH.BLE_RAW_FOLDERS.put(Bluetooth.MODEL_O2RING, Constants.PATH.DIR_O2RING_OXY);
        BLUETOOTH.BLE_RAW_FOLDERS.put(Bluetooth.MODEL_ER1, Constants.PATH.DIR_ER1_ECG);

        //只初始化当前设备的interface
        SparseArray<Boolean> ru = BleUtilService.Companion.initRtConfig(BindDeviceUtils.getCurrentDevice());
        SparseArray<Integer> mode = BleUtilService.Companion.initModelConfig(BindDeviceUtils.getCurrentDevice());
        LogUtils.d(ru.size(), mode.size());


        BleUtilService.Companion.getServiceHelper()
                .initLog(BuildConfig.DEBUG)
                .initRawFolder(BLUETOOTH.BLE_RAW_FOLDERS)
                .initModelConfig(mode)
                .initRtConfig(ru)
                .initService(application, new BleServiceObserverImpl());
    }

```

### 新增配置

```kotlin
class BleUtilService

fun initModelConfig()....//todo add
fun initRtConfig()....//todo add
```

```kotlin
class AppLifecycleImpl

BLUETOOTH.BLE_RAW_FOLDERS.put...//todo add
```

## 2. 绑定

主题：

- 所有的绑定页集成到`scanActivity`拥有的Fragment中,Activity中负责公共部分，各自设备的核心绑定功能在各个Fragment中完成，通过`ViewModel`共享数据
- 负责绑定业务的Fragment初始化时，应向蓝牙服务添加对应model的Interface。离开绑定页面，将比对当前设备并根据设备的绑定情况整理服务中的interface。

```kotlin
class ScanActivity : AppCompatActivity() {
//配置Fragment    
private fun initUI() {
        when (currentType) {
            
            Constants.DeviceType.ER2_TYPE -> {
                addEr2Fragment(Bluetooth.MODEL_ER2)
            }
            //todo add
            else -> LogUtils.d("warning:: not find currentType-$currentType")
        }

    }
}
```

```kotlin
class BleUtilService

 		/**
         * 绑定设备
         */
        fun bind(info: Any, b: Bluetooth): Boolean {
            LogUtils.d("start binding...")
            var d: MyDevice? = null
            d = when (b.model) {
              
                Bluetooth.MODEL_ER1 -> {
                    bindEr1(b, info as LepuDevice)
                    return true
                }
                else -> {
                    LogUtils.d("Error: 未配置此model!!")
                    null
                }

            }
            LogUtils.d("绑定失败!!")
            return false
        }
		
		/**
         *
         * 整理interface
         *
         * 保持在bleService中只有当前的并已绑定状态的设备的interface（进入绑定操作时除外）
         * 所以操作切换设备,都要切换完当前设备后调用此方法
         * 1.清除其他设备的interface
         * 2.如果当前设备已绑定则，则去setInterface, 如果是存在的不会新建interface
         * 
         * * @param device MyDevice 当前设备
         */
        fun switchCurrentInterfaces(device: MyDevice){
            LogUtils.d("switchCurrentInterfaces", device.toString())

            // 如果当前已绑定
            if (device.isBonded){

                when(device.deviceType.toInt()){
                    Constants.DeviceType.O2RING_TYPE -> {
                        //清除非本设备的model
                        clearOtherModel(intArrayOf(Bluetooth.MODEL_O2RING))
                        //添加model 如果已经存在 不会重复创建interface
                        setInterface(Bluetooth.MODEL_O2RING, runRtImmediately = true)
                        //如果未连接 去重连
                        if (getDeviceState(Bluetooth.MODEL_O2RING) == State.DISCONNECTED) {
                            BleServiceHelper.BleServiceHelper.reconnect(Bluetooth.MODEL_O2RING, device.deviceName)
                        }

                    }
                    // todo add
                    else -> clearOtherModel(intArrayOf())
                }


            } else clearOtherModel(intArrayOf())


        }
```

## 3. 获取设备状态

```kotlin
class BleUtilService{
		/**
         * 先判断蓝牙状态  再判断绑定状态
         * 返回State中的状态(0至3)
         */
        fun getDeviceState(model: Int): Int {
             Bluetooth.MODEL_ER2 -> {
                            SPUtils.getInstance(Const.CONFIG_USER).getString(Constants.ACTION_ER2_DEVICE_NAME)
                            }
                            else ->  throw Exception("Error: 无法识别此model：$model")
            //todo add
        }
		fun getDeviceState(device: MyDevice): Int {
            //todo add
        }    
} 		

```

## 4. 订阅蓝牙状态

### 单设备订阅/同时多设备订阅

1. LifecycleOwner实现BleChangeObserver接口
2. LifecycleOwner订阅Interface

```java
public class Fragment implements BleChangeObserver {
     @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //订阅interface， 传入需要订阅的model的数组，同时订阅多个设备
        getLifecycle().addObserver(new BIOL(this, new int[]{Bluetooth.MODEL_ER2}));

    }
     /**
     * 
     * @param model  如果同时订阅了多个interface, 用来区别信号来源
     * @param state 蓝牙连接状态Ble.State.CONNECTED 、 Ble.State.CONNECTING、 Ble.State.DISCONNECTED
     */
     @Override
    public void onBleStateChanged(int i, int i1) {

    }
}
```

### 不同时多设备订阅

订阅方式同上，注意要在合适的时机更新订阅需要订阅的数组，参考MyDeviceActivity

## 5. 切换设备

### 流程

1. 断开所有连接
2. 更新全局参数
3. BleSdk切换Interface
4. 更新订阅
5. 销毁MainActivity
6. 重连当前设备
7. 刷新UI

```java
class MyDeviceActivity{
	/**
     * 切换设备
     */
    private void switchToDevice(MyDevice device) {
        
        disconnectAll();
        BindDeviceUtils.switchToDevice(device);
        
        BleUtilService.Companion.switchCurrentInterfaces(device);
        //更新订阅
        BleUtilService.Companion.updateSubBIOL(biol);
        //-------------------sdk
        ActivityUtils.finishActivity(MainActivity.class);
        
        // connect
        if (device.isBonded) {
            connect(device);
        }

        refreshUI();
    }
}
```



## 6. 断开连接

```kotlin
class BleUtilService
 		/**
         * 应用场景：切换设备时调用此方法，并且应该指定autoReconnect = false
         * @param model Int
         * @param autoReconnect Boolean 值 =true时，当蓝牙断开后会马上开启扫描尝试连接该model
         */
        fun disconnect(model: Int, autoReconnect: Boolean) {
            BleServiceHelper.BleServiceHelper.disconnect(model, autoReconnect)
        }
```

## 7.重连

```kotlin
class BleUtilService 		
		/**
         * 发起重连，通过蓝牙名发起请求连接
         * 应用场景：1.进入设备首页，如果已经绑定立即发起重连 2.某个model被切换至当前时立即发起重连
         * @param model Int
         * @param name String
         */
        fun reconnect(model: Int, name: String) {
            LogUtils.d("reconnect...$model")
            //检查必须要有interface
            BleServiceHelper.BleServiceHelper.getInterface(model)?.let {
                // 有未连接蓝牙的设备
                BleServiceHelper.BleServiceHelper.hasUnConnected(intArrayOf(model)).let {
                    BleServiceHelper.BleServiceHelper.getConnectState(model).let {
                        LogUtils.d("device state", it)
                        //必须已绑定
                        if (it == State.DISCONNECTED)BleServiceHelper.BleServiceHelper.reconnect(model, name)
                    }
                }

            }?: LogUtils.e("current model $model interface is null !!!")
        }
```

## 8.解绑

```kotlin
 fun unbound(deviceType: Int) {
            when (deviceType) {
             
                Constants.DeviceType.ER1_TYPE -> {
                    disconnect(Bluetooth.MODEL_ER1, false)
                    SPUtils.getInstance(Constants.CONFIG_USER).remove(Constants.ACTION_ER1_DEVICE_NAME, true)
                    SPUtils.getInstance(Constants.CONFIG_USER).remove(Constants.ACTION_ER1_DEVICE_ADDRESS, true)

                    BleServiceHelper.BleServiceHelper.getInterfaces()?.remove(Bluetooth.MODEL_ER1)
                    LogUtils.d("er1 unbind  success")
                }
             

                //todo
            }

        }
```

## 9.BleSO

主题：订阅BleService的生命周期

应用：保存APP运行时的实时测量数据

## 10.下载文件

```kotlin
class BleUtilService{
		 @JvmOverloads
        fun readFile(userId: String, fileName: String, model: Int, offset: Int = 0) {
            BleServiceHelper.BleServiceHelper.readFile(userId, fileName, model, offset)
        }

        @JvmOverloads
        fun readFile(userId: String, fileName: String, device: MyDevice, offset: Int = 0) {
            when(device.deviceType.toInt()){
                Constants.DeviceType.ER1_TYPE -> {
                    readFile(userId, fileName, Bluetooth.MODEL_ER1, offset)
                }
                Constants.DeviceType.DUOEK_TYPE -> {
                    readFile(userId, fileName, Bluetooth.MODEL_DUOEK, offset)
                }
                Constants.DeviceType.O2RING_TYPE -> {
                    readFile(userId, fileName, Bluetooth.MODEL_O2RING, 0) //不支持断点下载
                }
                else -> throw Exception("无法识别：deviceType=${device.deviceType}")
            }
        }
}
```

## 11.断点下载

```java
//获取本地文件长度，即这次读文件的偏移量
byte[] offset = BleUtilService.Companion.getOffset(curModel, LoginUtil.getUserId(), fileList.get(0).trim());
        BleUtilService.Companion.readFile(LoginUtil.getUserId(), fileList.get(0), myDevice, offset.length);
```

### 暂停下载

```kotlin
 /**
 * 暂停读取文件
 * @param model Int
 */
fun pauseReadFile(model: Int){
    BleServiceHelper.BleServiceHelper.pauseReadFile(model)
}        
```

## 继续下载

```kotlin
fun continueReadFile(model: Int, userId: String, fileName: String){
    getOffset(model, userId, fileName)?.let {
        LogUtils.d("continueReadFile...")
        BleServiceHelper.BleServiceHelper.continueReadFile(model, userId, fileName, it.size)
    }
}
```

### 结束下载

```kotlin
/**
* 结束读取文件
* @param model Int
*/
fun cancelReadFile(model: Int){
    BleServiceHelper.BleServiceHelper.cancelReadFile(model)
}
```



