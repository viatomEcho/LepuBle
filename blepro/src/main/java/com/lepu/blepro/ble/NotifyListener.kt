package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.data.Data

/**
 * author: wujuan
 * created on: 2021/1/21 14:42
 * description:
 */
interface NotifyListener {

    fun onNotify(device: BluetoothDevice?, data: Data?)

}