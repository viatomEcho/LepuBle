package com.lepu.demo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.demo.ble.BleSO
import com.lepu.demo.ble.LpBleUtil
import com.permissionx.guolindev.PermissionX
const val CHECK_BLE_REQUEST_CODE = 6001
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        needPermission()


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
                       if (it)initBLE()
                    }
            }

    }

    private fun initBLE(){
        LpBleUtil.getServiceHelper()
            .initLog(BuildConfig.DEBUG)
            .initModelConfig(SparseArray<Int>().apply {
                this.put(Bluetooth.MODEL_O2RING, Bluetooth.MODEL_O2RING)
                this.put(Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2)
                this.put(Bluetooth.MODEL_ER1, Bluetooth.MODEL_ER1)
                this.put(Bluetooth.MODEL_PC60FW, Bluetooth.MODEL_PC60FW)
            }) // 配置要支持的设备
            .initService(
                application,
                BleSO.getInstance(application)
            ) //必须在initModelConfig initRawFolder之后调用

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