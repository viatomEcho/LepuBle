package com.lepu.demo.ble

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.blepro.ble.data.Bp2Wifi
import com.lepu.demo.R

class WifiAdapter(layoutResId: Int, data: MutableList<Bp2Wifi>?) : BaseQuickAdapter<Bp2Wifi, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: Bp2Wifi) {
        holder.setText(R.id.name, "name：${item.ssid}\naddress：${item.macAddr}\nrssi：${item.rssi}dBm")
    }
}