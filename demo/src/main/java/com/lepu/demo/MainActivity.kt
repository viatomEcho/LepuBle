package com.lepu.demo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.ble.data.lew.DeviceInfo
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.CHECK_BLE_REQUEST_CODE
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.SUPPORT_MODELS
import com.lepu.demo.util.CollectUtil
import com.permissionx.guolindev.PermissionX


class MainActivity : AppCompatActivity() , BleChangeObserver {
    private val TAG: String = "MainActivity"

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        applicationContext?.let { context ->
            CollectUtil.getInstance(context).let {
                it.initCurrentCollectDuration()
            }
        }

        subscribeUi()
        needPermission()
        checkServer()
        initLiveEvent()
//        split()

    }

    //创建菜单
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.right_menu, menu)
        return true
    }

    //菜单点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_factory_data -> {
                startActivity(Intent(this, DeviceFactoryDataActivity::class.java))
            }
        }
        return true
    }

    /**
     * ble sdk 服务启动完成后, 现在是点去连接时候才初始化interface  所以这里注释
     */
    private fun afterLpBleInit(){
        // ble service 初始完成后添加订阅才有效
//        lifecycle.addObserver(BIOL(this, SUPPORT_MODELS))

//        viewModel._scanning.value = true
        LpBleUtil.startScan(SUPPORT_MODELS)
    }

    private fun subscribeUi() {

        //手机ble状态,
        viewModel.bleEnable.observe(this, Observer{

            if (it) {
                //ble service
                if (Constant.BluetoothConfig.bleSdkServiceEnable) afterLpBleInit()
                else LpBleUtil.initBle(application)
            }

        })

        // 开启/关闭扫描
        viewModel.scanning.observe(this) {
            LpBleUtil.startScan(SUPPORT_MODELS)
        }

    }

    private fun initLiveEvent(){
        // 当BleService onServiceConnected执行后发出通知 蓝牙sdk 初始化完成
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observe(this) {
                Constant.BluetoothConfig.bleSdkServiceEnable = true
                afterLpBleInit()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyResponseBytes)
            .observe(this) {
                val data = it.data as ResponseBytes
                Log.d(TAG, "${data.dataType} --- ${bytesToHex(data.data)}")
            }
        //-------------------------er1---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetTime)
            .observe(this) {
                when (it.model) {
                    Bluetooth.MODEL_ER1 -> {
                        Toast.makeText(this, "ER1 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_ER1_N -> {
                        Toast.makeText(this, "VBeat 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_HHM1 -> {
                        Toast.makeText(this, "HHM1 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_DUOEK -> {
                        Toast.makeText(this, "DuoEK 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_HHM2 -> {
                        Toast.makeText(this, "HHM2 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_HHM3 -> {
                        Toast.makeText(this, "HHM3 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(this, "ER1 完成时间同步", Toast.LENGTH_SHORT).show()
                }
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Info)
            .observe(this) { event ->
                (event.data as LepuDevice).let {
                    when (event.model) {
                        Bluetooth.MODEL_ER1 -> {
                            Toast.makeText(this, "ER1 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_ER1_N -> {
                            Toast.makeText(this, "VBeat 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_HHM1 -> {
                            Toast.makeText(this, "HHM1 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_DUOEK -> {
                            Toast.makeText(this, "DuoEK 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_HHM2 -> {
                            Toast.makeText(this, "HHM2 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_HHM3 -> {
                            Toast.makeText(this, "HHM3 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        else -> Toast.makeText(this, "ER1 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    }
                    viewModel._er1Info.value = it
                }
            }
        //-------------------------er2---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetTime)
            .observe(this) {
                when (it.model) {
                    Bluetooth.MODEL_ER2 -> {
                        Toast.makeText(this, "ER2 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_LP_ER2 -> {
                        Toast.makeText(this, "LP ER2 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(this, "ER2 完成时间同步", Toast.LENGTH_SHORT).show()
                }
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Info)
            .observe(this) { event ->
                (event.data as Er2DeviceInfo).let {
                    when (event.model) {
                        Bluetooth.MODEL_ER2 -> {
                            Toast.makeText(this, "ER2 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_LP_ER2 -> {
                            Toast.makeText(this, "LP ER2 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        else -> Toast.makeText(this, "ER2 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    }
                    viewModel._er2Info.value = it
                }
            }
        //-------------------------lew---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewSetTime)
            .observe(this) {
                Toast.makeText(this, "4G手表 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lew.EventLewDeviceInfo)
            .observe(this) { event ->
                (event.data as DeviceInfo).let {
                    Toast.makeText(this, "4G手表 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._lewInfo.value = it
                }
            }
        //-------------------------fhr---------------------------
        //-------------------------pc60fw---------------------------
        //-------------------------pc80b---------------------------
        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady)
            .observe(this) {
                Toast.makeText(this, "EventBleDeviceReady 连接成功", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo)
            .observe(this) { event ->
                (event.data as PC80BleResponse.DeviceInfo).let {
                    Toast.makeText(this, "PC80B 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._pc80bInfo.value = it
                }
            }
        //-------------------------bp2 bp2a---------------------------
        //bp2 同步时间
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime)
            .observe(this) {
                when (it.model) {
                    Bluetooth.MODEL_BP2 -> {
                        Toast.makeText(this, "BP2 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_BP2A -> {
                        Toast.makeText(this, "BP2A 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_BP2T -> {
                        Toast.makeText(this, "BP2T 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    else -> Toast.makeText(this, "BP2 完成时间同步", Toast.LENGTH_SHORT).show()
                }
                LpBleUtil.getInfo(it.model)
                LpBleUtil.bp2GetRtState(it.model)
            }
        //bp2 info
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info)
            .observe(this) { event ->
                (event.data as Bp2DeviceInfo).let {
                    when (event.model) {
                        Bluetooth.MODEL_BP2 -> {
                            Toast.makeText(this, "BP2 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_BP2A -> {
                            Toast.makeText(this, "BP2A 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_BP2T -> {
                            Toast.makeText(this, "BP2T 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        else -> Toast.makeText(this, "BP2 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    }
                    viewModel._bp2Info.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2State)
            .observe(this) { event ->
                (event.data as Bp2BleRtState).let {
                    viewModel._battery.value = "${it.battery.percent} %"
                }
            }
        //-------------------------bp2w---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSyncTime)
            .observe(this) {
                Toast.makeText(this, "BP2W 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
                LpBleUtil.bp2GetRtState(it.model)
                LpBleUtil.bp2GetWifiConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wInfo)
            .observe(this) { event ->
                (event.data as LepuDevice).let {
                    Toast.makeText(this, "BP2W 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._er1Info.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtState)
            .observe(this) { event ->
                (event.data as Bp2BleRtState).let {
                    viewModel._battery.value = "${it.battery.percent} %"
                }
            }
        //-------------------------LeBp2w---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSyncTime)
            .observe(this) {
                Toast.makeText(this, "LP-BP2W 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
                LpBleUtil.bp2GetRtState(it.model)
                LpBleUtil.bp2GetWifiConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wSyncUtcTime)
            .observe(this) {
                Toast.makeText(this, "LP-BP2W 完成UTC时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
                LpBleUtil.bp2GetRtState(it.model)
                LpBleUtil.bp2GetWifiConfig(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wInfo)
            .observe(this) { event ->
                (event.data as LepuDevice).let {
                    Toast.makeText(this, "LP-BP2W 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._er1Info.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeBP2W.EventLeBp2wRtState)
            .observe(this) { event ->
                (event.data as Bp2BleRtState).let {
                    viewModel._battery.value = "${it.battery.percent} %"
                }
            }
        //-------------------------bpm---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmSyncTime)
            .observe(this) {
                Toast.makeText(this, "BPM 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmInfo)
            .observe(this) { event ->
                (event.data as BpmDeviceInfo).let {
                    Toast.makeText(this, "BPM 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._bpmInfo.value = it
                }
            }
        //-------------------------o2ring---------------------------
        // o2ring 同步时间
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(this) {
                Toast.makeText(this, "O2系列 完成时间同步 ${it.data}", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)

            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo).observe(this) { event ->
            (event.data as OxyBleResponse.OxyInfo).let {
                viewModel._oxyInfo.value = it
                viewModel._battery.value = it.batteryValue
                Toast.makeText(this, "O2系列 获取设备信息成功", Toast.LENGTH_SHORT).show()
                if (event.model == Bluetooth.MODEL_BABYO2N) {
                    LpBleUtil.oxyGetBoxInfo(event.model)
                }
            }
        }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyBoxInfo).observe(this) { event ->
            (event.data as LepuDevice).let {
                Toast.makeText(this, "BABYO2N 获取盒子信息成功 $it", Toast.LENGTH_SHORT).show()
            }
        }
        //-------------------------pc100---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo)
            .observe(this) { event ->
                (event.data as Pc100DeviceInfo).let {
                    Toast.makeText(this, "PC-100 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._pc100Info.value = it
                    viewModel._battery.value = "${it.batLevel.times(25)} - ${(it.batLevel+1).times(25)} %"
                }
            }
        //-------------------------pc60fw pc6n---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo)
            .observe(this) { event ->
                (event.data as BoDeviceInfo).let {
                    Toast.makeText(this, "指甲血氧 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._boInfo.value = it
                }
            }
        //-------------------------ap20---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20SetTime)
            .observe(this) {
                Toast.makeText(this, "AP-20 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AP20.EventAp20DeviceInfo)
            .observe(this) { event ->
                (event.data as BoDeviceInfo).let {
                    Toast.makeText(this, "AP-20 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._boInfo.value = it
                }
            }
        //-------------------------sp20---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20SetTime)
            .observe(this) {
                Toast.makeText(this, "SP-20 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.SP20.EventSp20DeviceInfo)
            .observe(this) { event ->
                (event.data as BoDeviceInfo).let {
                    Toast.makeText(this, "SP-20 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._boInfo.value = it
                }
            }
        //-------------------------aoj20a---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aSetTime)
            .observe(this) {
                Toast.makeText(this, "AOJ-20A 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AOJ20a.EventAOJ20aDeviceData)
            .observe(this) { event ->
                (event.data as Aoj20aBleResponse.DeviceData).let {
                    Toast.makeText(this, "AOJ-20A 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._aoj20aInfo.value = it
                }
            }
        //-------------------------checkme pod-------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodSetTime)
            .observe(this) {
                Toast.makeText(this, "Checkme Pod 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmePod.EventCheckmePodDeviceInfo)
            .observe(this) { event ->
                (event.data as CheckmePodBleResponse.DeviceInfo).let {
                    Toast.makeText(this, "Checkme Pod 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._checkmePodInfo.value = it
                }
            }
        //-------------------------pc68b---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bSetTime)
            .observe(this) {
                Toast.makeText(this, "PC-68B 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC68B.EventPc68bDeviceInfo)
            .observe(this) { event ->
                (event.data as BoDeviceInfo).let {
                    Toast.makeText(this, "PC-68B 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._boInfo.value = it
                    LpBleUtil.pc68bGetTime(event.model)
                }
            }
        //-------------------------pulsebit-------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitSetTime)
            .observe(this) {
                when (it.model) {
                    Bluetooth.MODEL_PULSEBITEX -> {
                        Toast.makeText(this, "Pulsebit 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_HHM4 -> {
                        Toast.makeText(this, "HHM4 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    Bluetooth.MODEL_CHECKME -> {
                        Toast.makeText(this, "Checkme 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "Pulsebit 完成时间同步", Toast.LENGTH_SHORT).show()
                    }
                }
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitDeviceInfo)
            .observe(this) { event ->
                (event.data as PulsebitBleResponse.DeviceInfo).let {
                    when (event.model) {
                        Bluetooth.MODEL_PULSEBITEX -> {
                            Toast.makeText(this, "Pulsebit 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_HHM4 -> {
                            Toast.makeText(this, "HHM4 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        Bluetooth.MODEL_CHECKME -> {
                            Toast.makeText(this, "Checkme 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Pulsebit 获取设备信息成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                    viewModel._pulsebitInfo.value = it
                }
            }
        //-------------------------CheckmeLE-------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeSetTime)
            .observe(this) {
                Toast.makeText(this, "CheckmeLE 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.CheckmeLE.EventCheckmeLeDeviceInfo)
            .observe(this) { event ->
                (event.data as CheckmeLeBleResponse.DeviceInfo).let {
                    Toast.makeText(this, "CheckmeLE 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._checkmeLeInfo.value = it
                }
            }
        //-------------------------pc300---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC300.EventPc300DeviceInfo)
            .observe(this) { event ->
                (event.data as Pc300DeviceInfo).let {
                    Toast.makeText(this, "PC_300SNT 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._pc300Info.value = it
                    viewModel._battery.value = "${it.batLevel.times(25)} - ${(it.batLevel+1).times(25)} %"
                }
            }
        //-------------------------lem---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LEM.EventLemDeviceInfo)
            .observe(this) { event ->
                (event.data as LemBleResponse.DeviceInfo).let {
                    Toast.makeText(this, "LEM1 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._lemInfo.value = it
                }
            }
        //-------------------------le S1---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1SetTime)
            .observe(this) {
                Toast.makeText(this, "le S1 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LES1.EventLeS1Info)
            .observe(this) { event ->
                (event.data as LepuDevice).let {
                    Toast.makeText(this, "le S1 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._er1Info.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmDeviceInfo)
            .observe(this) {
                Toast.makeText(this, "Bioland-BGM 获取设备信息成功", Toast.LENGTH_SHORT).show()
                val data = it.data as BiolandBgmBleResponse.DeviceInfo
                viewModel._biolandInfo.value = data
                viewModel._battery.value = "${data.battery} %"
            }
        //-------------------------er3---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3SetTime)
            .observe(this) {
                Toast.makeText(this, "ER3 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER3.EventEr3Info)
            .observe(this) { event ->
                (event.data as LepuDevice).let {
                    Toast.makeText(this, "ER3 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._er1Info.value = it
                }
            }
        //-------------------------lepod---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodSetTime)
            .observe(this) {
                Toast.makeText(this, "Lepod 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Lepod.EventLepodInfo)
            .observe(this) { event ->
                (event.data as LepuDevice).let {
                    Toast.makeText(this, "Lepod 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._er1Info.value = it
                }
            }
        //--------------------------vtm01--------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM01.EventVtm01Info)
            .observe(this) {
                Toast.makeText(this, "VTM01 获取设备信息成功", Toast.LENGTH_SHORT).show()
                viewModel._er1Info.value = it.data as LepuDevice
            }
    }
    private fun needPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
                .onExplainRequestReason { scope, deniedList ->
                    // 当请求被拒绝后，说明权限原因
                    scope.showRequestReasonDialog(
                        deniedList, getString(R.string.permission_location_reason), getString(
                            R.string.open
                        ), getString(R.string.ignore)
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    //选择了拒绝且不再询问的权限，去设置
                    scope.showForwardToSettingsDialog(
                        deniedList, getString(R.string.permission_location_setting), getString(
                            R.string.confirm
                        ), getString(R.string.ignore)
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    Log.e("权限授权情况", "$allGranted, $grantedList, $deniedList")

                    //权限OK, 检查蓝牙状态
                    if (allGranted)
                        checkBluetooth(CHECK_BLE_REQUEST_CODE).let {
                            LepuBleLog.d(TAG, "蓝牙状态 $it")
                            viewModel._bleEnable.value = true
                        }
                }
        } else {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .onExplainRequestReason { scope, deniedList ->
                    // 当请求被拒绝后，说明权限原因
                    scope.showRequestReasonDialog(
                        deniedList, getString(R.string.permission_location_reason), getString(
                            R.string.open
                        ), getString(R.string.ignore)
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    //选择了拒绝且不再询问的权限，去设置
                    scope.showForwardToSettingsDialog(
                        deniedList, getString(R.string.permission_location_setting), getString(
                            R.string.confirm
                        ), getString(R.string.ignore)
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    Log.e("权限授权情况", "$allGranted, $grantedList, $deniedList")

                    //权限OK, 检查蓝牙状态
                    if (allGranted)
                        checkBluetooth(CHECK_BLE_REQUEST_CODE).let {
                            LepuBleLog.d(TAG, "蓝牙状态 $it")
                            viewModel._bleEnable.value = true
                        }
                }
        }

    }

    private fun checkServer() {
        var gpsEnabled = false
        var networkEnabled = false
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (!gpsEnabled && !networkEnabled) {
            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            dialog.setMessage("开启位置服务")
            dialog.setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            dialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->
                finish()
            }
            dialog.setCancelable(false)
            dialog.show()
        }
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        LepuBleLog.d("onBleStateChanged model = $model, state = $state")
        viewModel._bleState.value = state == LpBleUtil.State.CONNECTED

        when(state){
            LpBleUtil.State.CONNECTED ->{
                //
            }
            LpBleUtil.State.DISCONNECTED ->{
                LpBleUtil.stopRtTask(model)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_BLE_REQUEST_CODE) {
            //重启蓝牙权限后
            LpBleUtil.reInitBle()
            viewModel._bleEnable.value = true
        }
    }

}

fun Activity.checkBluetooth(requestCode: Int, finishOnCancel: Boolean = false): Boolean =
    BluetoothAdapter.getDefaultAdapter()?.let {
        return if(it.isEnabled) true else {
            MaterialDialog.Builder(this)
                .title(R.string.prompt)
                .content(R.string.permission_bluetooth)
                .negativeText(R.string.cancel)
                .onNegative { dialog: MaterialDialog, which: DialogAction? ->
                    dialog.dismiss()
                    if (finishOnCancel) finish()
                }
                .positiveText(R.string.open)
                .onPositive { dialog: MaterialDialog, which: DialogAction? ->
                    dialog.dismiss()
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(intent, requestCode)
                }
                .show()
            false
        }
    }?: false