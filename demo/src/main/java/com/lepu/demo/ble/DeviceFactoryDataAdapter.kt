package com.lepu.demo.ble

import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.DeviceFactoryData

class DeviceFactoryDataAdapter(layoutResId: Int, data: MutableList<DeviceFactoryData>?) : BaseQuickAdapter<DeviceFactoryData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: DeviceFactoryData) {
        holder.setText(R.id.text_1, "烧录时间：${item.time}")
        holder.setText(R.id.text_2, "蓝牙名：${item.name}")
        holder.setText(R.id.text_3, "蓝牙地址：${item.address}")
        holder.setText(R.id.text_4, "sn：${item.sn}")
        holder.setText(R.id.text_5, "code：${item.code}")
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