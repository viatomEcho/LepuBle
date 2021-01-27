package com.lepu.blepro.ble

import android.content.Context
import com.lepu.blepro.base.BaseBleManager
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/27 10:25
 * description:
 */
class OxyBleManager(context: Context): BaseBleManager(context) {
    override fun init() {
        LepuBleLog.d("OxyBleManager inited")
    }
}