package com.lepu.demo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.observer.BleChangeObserver
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.HexString
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.CHECK_BLE_REQUEST_CODE
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.SUPPORT_MODELS
import com.lepu.demo.util.CollectUtil
import com.permissionx.guolindev.PermissionX
import java.util.*


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
        initLiveEvent()
//        split()

    }

    /**
     * ble sdk 服务启动完成后, 现在是点去连接时候才初始化interface  所以这里注释
     */
    private fun afterLpBleInit(){
        // ble service 初始完成后添加订阅才有效
//        lifecycle.addObserver(BIOL(this, SUPPORT_MODELS))

//        viewModel._scanning.value = true

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
        viewModel.scanning.observe(this, {
            if (it){
               LpBleUtil.startScan(SUPPORT_MODELS)
            } else LpBleUtil.stopScan()

        })

    }

    private fun initLiveEvent(){
        // 当BleService onServiceConnected执行后发出通知 蓝牙sdk 初始化完成
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).observe(this, Observer {
            Constant.BluetoothConfig.bleSdkServiceEnable = true
            afterLpBleInit()
        })

        //-------------------------er1---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1SetTime)
            .observe(this, {
                Toast.makeText(this, "er1 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Info)
            .observe(this, { event ->
                (event.data as LepuDevice).let {
                    Toast.makeText(this, "er1 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._er1Info.value = it
                }
            })
        //-------------------------er2---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2SetTime)
            .observe(this, {
                Toast.makeText(this, "er2 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Info)
            .observe(this, { event ->
                (event.data as Er2DeviceInfo).let {
                    Toast.makeText(this, "er2 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._er2Info.value = it
                }
            })
        //-------------------------fhr---------------------------
        //-------------------------pc60fw---------------------------
        //-------------------------pc80b---------------------------
        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventBleDeviceReady)
            .observe(this, {
                Toast.makeText(this, "EventBleDeviceReady 连接成功", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(viewModel.curBluetooth.value!!.modelNo)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC80B.EventPc80bDeviceInfo)
            .observe(this, { event ->
                (event.data as PC80BleResponse.DeviceInfo).let {
                    Toast.makeText(this, "pc80b 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._pc80bInfo.value = it
                }
            })
        //-------------------------bp2 bp2a---------------------------
        //bp2 同步时间
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime)
            .observe(this, {
                Toast.makeText(this, "bp2 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            })
        //bp2 info
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info)
            .observe(this, { event ->
                (event.data as Bp2DeviceInfo).let {
                    Toast.makeText(this, "bp2 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._bp2Info.value = it
                }
            })
        //-------------------------bpm---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmSyncTime)
            .observe(this, {
                Toast.makeText(this, "bpm 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmInfo)
            .observe(this, { event ->
                (event.data as BpmDeviceInfo).let {
                    Toast.makeText(this, "bpm 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._bpmInfo.value = it
                }
            })

        //-------------------------o2ring---------------------------
        // o2ring 同步时间
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(this, {
                Toast.makeText(this, "o2ring 完成时间同步", Toast.LENGTH_SHORT).show()
                LpBleUtil.getInfo(it.model)

            })

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo).observe(this, { event ->
            (event.data as OxyBleResponse.OxyInfo).let {
                viewModel._oxyInfo.value = it
            }
        })

        //-------------------------pc100---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100DeviceInfo)
            .observe(this, { event ->
                (event.data as Pc100DeviceInfo).let {
                    Toast.makeText(this, "pc100 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._pc100Info.value = it
                }
            })

        //-------------------------pc6n---------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC60Fw.EventPC60FwDeviceInfo)
            .observe(this, { event ->
                (event.data as Pc6nDeviceInfo).let {
                    Toast.makeText(this, "pc6n 获取设备信息成功", Toast.LENGTH_SHORT).show()
                    viewModel._pc6nInfo.value = it
                }
            })

    }
    private fun needPermission(){
        PermissionX.init(this)
            .permissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
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


    override fun onBleStateChanged(model: Int, state: Int) {
        LepuBleLog.d("onBleStateChanged model = $model, state = $state")
        viewModel._bleState.value = state == LpBleUtil.State.CONNECTED

        when(state){
            LpBleUtil.State.CONNECTED ->{
            }
            LpBleUtil.State.DISCONNECTED ->{
                LpBleUtil.stopRtTask(model)


            }
        }
    }
    fun split(){

//        val stringList = arrayListOf<String>("A508F70193DE00060024990E1200009A020000000000000000000000000200000080EF00205F00030009000C000D000E000E000C0008000300FFFFF9FFF1FFE7FFDFFFD9FFCFFFBDFFAEFFAEFFF4FF5A00D6004501850185013D01CA004800DFFFA9FFA5FFB8FFC9FFD0FFD1FFD4FFDBFFE4FFEEFFF9FF03000E0015001C00220028002D00320036003A003C003D003E003E003D003D003D003C003900360032002C00260020001A0014000D000400FAFFF0FFE7FFDFFFD8FFD3FFD0FFD1FFD2FFD3FFD3FFD4FFD5FFD6FFD6FFD8FFDEFFE8FFF2FFFBFF0000030008000D00100010000D003A",
//            "A508F701941C01060024990E1200009A0200000000000000000000000022000000000006437E0009000400FEFFF6FFEEFFE7FFE1FFDBFFCFFFBCFFAEFFAEFFF4FF5800D2004001820182013D01C9004700DFFFA8FFA1FFB4FFC7FFCFFFD1FFD3FFDAFFE4FFF0FFFAFF01000A0012001A00210028002E0034003A003D003E003E003F003F003F003E003C003C003C003A0035002E00280023001E0017000F000700FFFFF5FFE9FFDFFFD9FFD4FFD1FFCFFFCFFFD5FFD9FFD9FFD6FFD3FFD2FFD6FFDCFFE5FFEFFFFAFF010007000B000F00110011000E00090005000000FBFFF2FFE8FFE1FFDBFFD1FFBEFFAEFFAEFFF6FF5C00D70040017D017D013D01CB004800DCFFA5FFA1FFB4FFC6FFCFFFD2FFD4FFDAFFE3FFEEFFFBFF04000C0012001900220098",
//            "A508F701951601060024990E1200009A02010000000000000000000000D7FFFFFF00006C427B0029002E003200350039003C003E003E003F003F003E003D003B003A00370033002C00260021001C0016000E000400FDFFF5FFEDFFE3FFD8FFD2FFD0FFD1FFD2FFD3FFD4FFD7FFD8FFD7FFD6FFD8FFDFFFE9FFF3FFFCFF010007000C000F00110010000D0008000100FBFFF4FFEEFFE7FFE0FFD9FFCDFFBCFFAEFFAEFFF3FF5800D50047018A018A014201CE004B00E1FFA9FFA3FFB8FFCBFFD3FFD4FFD5FFDAFFE3FFEDFFF7FF00000B0014001C00210026002C0034003B003F003F003F003E003E003E003D003C003D003D003A00360031002A0023001C0016000F000700FEFFF3FFE9FFE1FFDAFFD3FFCFFFD0FFD4FFD7FFD8FFD7FF86",
//            "A508F701961601060024990E1200009A02010000000000000000000000CD000000008098437B00D5FFD3FFD3FFD8FFDFFFE7FFF0FFF8FFFFFF05000B000E000F000E000C00090006000100FBFFF1FFE8FFE2FFDDFFD3FFBFFFAFFFAFFFF8FF5C00D3003B017A017B013B01CA004800DFFFA9FFA4FFB6FFC7FFCEFFD0FFD3FFDBFFE5FFF0FFF9FF0000080010001900220029002E00320038003C003E003F003F003E003D003C003A0039003800360031002C00260022001D0016000D000300FDFFF5FFECFFE1FFD8FFD3FFD1FFD2FFD3FFD5FFD8FFDAFFDAFFD6FFD4FFD7FFDFFFE9FFF2FFFAFF010008000E00100010000F000C0007000100FDFFF9FFF2FFEAFFE2FFDBFFD1FFC0FFB1FFB1FFF4FF5900D6004701880188014001CD00F3",
//            "A508F701972E01060024990E1200009A02020000000000000000000000D5FFFFFF0000644287004B00E0FFA7FFA2FFB7FFC9FFCFFFCFFFD2FFDAFFE5FFEFFFFAFF03000D0014001A001E0025002C00330038003A003B003D003E003E003D003B003B003B003A00380034002F00290021001A0014000E000500FCFFF3FFEBFFE2FFD9FFD1FFCDFFD0FFD4FFD7FFD7FFD6FFD6FFD7FFD7FFD8FFDEFFE7FFF1FFFAFF000005000A000E0010000F000D000A000500FFFFF7FFEEFFE7FFE2FFDCFFCFFFBCFFADFFADFFF6FF5B00D3003F017E017E013D01CB004A00E2FFAAFFA3FFB5FFC8FFD1FFD2FFD2FFD8FFE3FFEFFFF9FF0000080012001A00210025002A00300038003D003E003D003C003E003F003E003B003A003A003800350030002A0024001E0015000D0006000000F6FFEBFFE1FFDAFFD5FF23",
//            "A508F701981601060024990E1200009A0202000000000000000000FFFF40B60020405F00207B00D3FFD3FFD5FFD8FFDAFFDBFFDBFFD6FFD6FFD9FFE0FFE8FFF1FFFCFF05000C000F001000110010000E00090003000000FBFFF3FFE8FFE0FFDBFFD1FFBFFFAFFFAFFFF4FF5B00D7004401810181013B01C9004600DEFFA8FFA3FFB4FFC4FFCBFFCEFFD2FFD9FFE1FFECFFF8FF02000B00120018001F0026002C00300035003A003D003F003F003C003B003B003C003C003A00370033002E00270021001C0017000F000400FAFFF2FFEAFFE1FFD7FFD0FFCFFFD0FFD2FFD3FFD4FFD5FFD6FFD4FFD2FFD5FFDDFFE9FFF3FFFAFFFFFF040009000B000C000B000A0007000200FCFFF5FFEFFFE9FFE2FFDCFFD0FFBEFFAFFFAFFFF2FF560078",
//            "A508F701991C01060024990E1200009A020300000000000000000000003A00000000001E437E00D2004301860186014101CD004A00DFFFA5FF9FFFB3FFC7FFD0FFD0FFD1FFD8FFE4FFF0FFF8FF00000700100018001F0025002B00320038003C003C003C003C003D003D003B00390038003800360031002C00270022001D0016000D000400FCFFF3FFEAFFE1FFDAFFD3FFCFFFCEFFCEFFD4FFD7FFD8FFD8FFD4FFD4FFD7FFDEFFE8FFF3FFFDFF0200060009000C001000110011000A0005000000FAFFF2FFE8FFE1FFDBFFD0FFBCFFACFFACFFF4FF5A00D2003B01780179013B01CB004A00E1FFABFFA5FFB6FFC6FFCFFFD2FFD6FFDBFFE4FFEFFFFBFF05000C0014001C0025002C003100340038003B003D003D003C003C003E003F003F003C003A00B5",
//            "A508F7019A1A01060024990E1200009A02030000000000000000000000090000000000DA427D00370034002F00290023001C0015000B000300FCFFF5FFEBFFE1FFD8FFD2FFD0FFD0FFD2FFD3FFD6FFD7FFD7FFD4FFD2FFD6FFDEFFE8FFF2FFFAFF010009000D000F000F00100010000C000400FEFFF7FFF0FFE8FFE0FFD8FFCEFFBDFFAFFFAFFFF1FF5600D2004401870187014001CC004900DEFFA4FF9FFFB4FFC8FFD0FFD0FFD2FFD8FFE3FFEEFFF8FF01000900100016001D0024002C00330038003B003C003D003E003E003C003A0039003900380034002F002900250020001A0013000C000400FCFFF3FFEAFFE2FFD9FFD1FFCDFFCEFFD1FFD3FFD4FFD4FFD4FFD4FFD3FFD5FFDDFFE9FFF4FFFCFF0000040009000E0010000F000C000900E1",
//            "A508F7019B2A01060024990E1200009A020400000000000000000000004000000000002443850005000000F9FFF0FFE8FFE1FFDAFFCEFFBBFFACFFACFFF4FF5800CF003A017A017C013D01CD004D00E5FFACFFA4FFB5FFC6FFCDFFCFFFD0FFD7FFE3FFF0FFFBFF010008000F001800210027002B00300036003B003D003C003C003F0040003E003C003A003800360031002B00260021001B0014000D0007000000F7FFEBFFE1FFD9FFD4FFD2FFD1FFD1FFD2FFD3FFD3FFD2FFD1FFD1FFD7FFDEFFE7FFF1FFFBFF020008000A000B000D000E000E0009000300FFFFF9FFF1FFE7FFDFFFDAFFD1FFBFFFAFFFAFFFF0FF5600D4004301830183014001CF004D00E2FFAAFFA4FFB8FFC9FFCFFFD1FFD5FFDCFFE4FFEDFFF8FF03000D0014001A00200027002D00320037003B003F0040004000B8"
//        )

        val stringList = arrayListOf<String>("A508F70172660006001E840E0000000002000000000000000000000020150C0000040000002300D3FFC5FFB3FFABFFABFF0D007C00F4005601830183011A019F002000C5FF9FFF9FFFBBFFC9FFCCFFCDFFD2FFDBFFE6FFF0FFF9FF01000A00110017001F0027002E00330036000F",
            "A508F701731C0106001E840E0000000002000000000000000000000000040000000000D0427E0039003B003C003C003B003A003A0039003800360032002E002A0025001F001A0015000E000400F9FFEEFFE6FFDEFFD6FFCFFFCDFFCFFFD3FFD5FFD5FFD3FFD2FFD4FFDBFFE5FFEFFFF8FF000006000A000B000C000D000D0007000100FCFFF5FFEEFFE6FFE0FFD9FFCFFFBDFFABFFABFFE3FF4200BE0034017F0188014C01DD005900E9FFAAFF9EFFB0FFC4FFCEFFD0FFD1FFD5FFDEFFE9FFF5FF000007000F0016001E0024002A003000360039003900380038003A003B003B003A0038003800360032002C00260020001B0015000D000400FDFFF5FFEDFFE3FFD9FFD1FFCEFFCFFFD1FFD4FFD5FFD3FFD0FFCEFFCEFFDDFFE8FFF2FFF9FFFFFF040063",
"A508F70174160106001E840E0000000002010000000000000000000000E9FFFFFF00009A427B0009000C000D000D0007000100FCFFF6FFF0FFE9FFE2FFDCFFD4FFC6FFB4FFACFFC6FF0D007D00F700590186016F0119019C001E00C5FF9FFFA4FFB9FFC8FFCDFFCDFFD0FFD9FFE5FFF1FFFAFF0100090012001900200026002D00340038003A003B003B003D003D003C003B003B003B00390034002F00290025001F0017000E0006000000FAFFF1FFE6FFDDFFD5FFCFFFCDFFCDFFD3FFD6FFD6FFD3FFD0FFD0FFD9FFE4FFEDFFF4FFFBFF020009000D000D000C000B0007000100FBFFF4FFEDFFE5FFDDFFD6FFCDFFBDFFAEFFB4FFE6FF4500BF00300177017F017F01DA005800E9FFAAFF9FFF9FFFC2FFCBFFCDFFD0FFD7FFE0FFE9FF8A",
        )

        var list = ArrayList<Float>()

        for (i in 0 until stringList.size) {
            HexString.hexToBytes(stringList[i]).let { bytes ->
                LepuBleLog.d("hexToByteArray -----${bytes.size}")
                Bp2BleResponse.BleResponse(bytes).let { response ->
                    val rtData = Bp2BleRtData(response.content)
                    ByteUtils.bytes2mvs(rtData.rtWave.waveform).let { floats ->
                        if (floats != null) {
                            LepuBleLog.d("---${floats.joinToString(",")}")
                            floats.forEach {
                                list.add(it)
                            }
                        }
                    }
                }
            }
        }

        LepuBleLog.d("list---${list.joinToString(",")}")



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