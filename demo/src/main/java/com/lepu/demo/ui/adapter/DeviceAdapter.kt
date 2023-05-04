package com.lepu.demo.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.R

/**
 * author: wujuan
 * description:
 */
class DeviceAdapter(layoutResId: Int, data: MutableList<Bluetooth>?) : BaseQuickAdapter<Bluetooth, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: Bluetooth) {
        holder.setText(R.id.name, "name：${item.name}\naddress：${item.macAddr}\nrssi：${item.rssi}dBm")
    }
}