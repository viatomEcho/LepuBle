package com.lepu.demo

import android.app.Application
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/1/19 17:09
 * description:
 */


class MyApplication: Application(){
    override fun onCreate() {
        super.onCreate()




        LepuBleLog.setDebug(true)






    }



}