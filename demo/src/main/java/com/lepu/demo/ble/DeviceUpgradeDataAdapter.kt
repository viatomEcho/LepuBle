package com.lepu.demo.ble

import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.DeviceUpgradeData

class DeviceUpgradeDataAdapter(layoutResId: Int, data: MutableList<DeviceUpgradeData>?) : BaseQuickAdapter<DeviceUpgradeData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: DeviceUpgradeData) {
        holder.setText(R.id.text_1, "升级时间：${item.time}")
        holder.setText(R.id.text_2, "蓝牙名：${item.name}")
        holder.setText(R.id.text_3, "蓝牙地址：${item.address}")
        holder.setText(R.id.text_4, "sn：${item.sn}")
        holder.getView<CardView>(R.id.item).setOnLongClickListener {
            mOnItemDeleteListener?.onDeleteClick(getItemPosition(item))
            return@setOnLongClickListener true
        }
    }

    interface onItemDeleteListener {
        fun onDeleteClick(position: Int)
    }

    private var mOnItemDeleteListener: onItemDeleteListener? = null

    fun setOnItemDeleteClickListener(mOnItemDeleteListener: onItemDeleteListener?) {
        this.mOnItemDeleteListener = mOnItemDeleteListener
    }
}