package com.lepu.demo.ui.adapter

import android.widget.Button
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.WirelessData
import com.lepu.demo.util.DataConvert

class WirelessDataAdapter(layoutResId: Int, data: MutableList<WirelessData>?) : BaseQuickAdapter<WirelessData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: WirelessData) {
        holder.setText(R.id.text_1, "测量时长:${DataConvert.getEcgTimeStr(item.recordTime)}")
        holder.setText(R.id.text_2, "数据总量:${String.format("%.3f", item.totalBytes.div(1024.0))} kb")
        holder.setText(R.id.text_3, "总包数:${item.totalSize}")
        holder.setText(R.id.text_4, "丢包数:${item.missSize}")
        holder.setText(R.id.text_5, "错误字节:${item.errorBytes}")
        holder.setText(R.id.text_6, "丢包率:${String.format("%.3f", item.missPercent)} %")
        holder.setText(R.id.text_7, "误码率:${String.format("%.3f", item.errorPercent)} %")
        holder.setText(R.id.text_8, "单次延迟:${item.oneDelay} ms")
        holder.setText(R.id.text_9, "总延迟:${item.totalDelay.div(item.totalSize)} ms")
        holder.setText(R.id.text_10, "吞吐量:${String.format("%.3f", item.throughput)} kb/h")
        holder.setText(R.id.text_11, "数据传输速度:${String.format("%.3f", item.speed)} b/s")
        holder.getView<Button>(R.id.record_delete).setOnClickListener {
            mOnItemDeleteListener?.onDeleteClick(getItemPosition(item))
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