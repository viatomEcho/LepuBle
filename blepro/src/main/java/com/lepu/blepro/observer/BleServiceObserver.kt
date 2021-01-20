package com.lepu.blepro.observer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * author: wujuan
 * created on: 2020/12/10 16:21
 * description:
 *  <ul>用于蓝牙服务的生命周期管理
 *      <li>
 *         如：服务结束时关闭数据库
 *      </li>
 *  </ul>
 *
 */
interface BleServiceObserver: LifecycleObserver{

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate()

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy()


}