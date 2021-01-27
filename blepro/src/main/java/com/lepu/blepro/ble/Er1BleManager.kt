package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.BaseBleManager
import com.lepu.blepro.ble.cmd.UniversalBleCmd
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/27 10:22
 * description:
 */
class Er1BleManager(context: Context): BaseBleManager(context) {
    override fun init() {
        syncTime()
        getInfo()
        LepuBleLog.d("Er1BleManager inited")
    }
    private fun getInfo() {
        sendCmd(UniversalBleCmd.getInfo());
    }

    private fun syncTime() {

    }
}