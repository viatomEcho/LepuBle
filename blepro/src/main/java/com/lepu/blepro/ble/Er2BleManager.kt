package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.BaseBleManager
import com.lepu.blepro.ble.cmd.er1.Er1BleCmd
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/27 10:22
 * description:
 */
class Er2BleManager(context: Context): BaseBleManager(context) {
    override fun init() {
        getInfo()
        LepuBleLog.d("Er2BleManager inited")
    }
    private fun getInfo() {
        sendCmd(Er1BleCmd.getInfo());
    }

}