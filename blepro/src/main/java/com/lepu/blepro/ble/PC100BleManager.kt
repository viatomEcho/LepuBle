import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.BaseBleManager
import com.lepu.blepro.ble.cmd.Er2BleCmd
import com.lepu.blepro.utils.LepuBleLog
import java.util.*

/**
 * author: wujuan
 * created on: 2021/3/31 10:09
 * description:
 */
class PC100BleManager(context: Context): BaseBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339")
        write_uuid = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
    }



    override fun initReqQueue() {
        beginAtomicRequestQueue()
                .add(enableNotifications(notify_char))
                .done { device: BluetoothDevice? ->
                    log(
                            Log.INFO,
                            "Target initialized"
                    )
                }
                .enqueue()
    }

    override fun init() {
        if (!isUpdater)
            syncTime()
        LepuBleLog.d("PC100BleManager inited")
    }

    private fun syncTime() {
        sendCmd(Er2BleCmd.setTime());
    }

}