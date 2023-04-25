package com.lepu.demo.ui.adapter

import android.widget.Button
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.WirelessData
import com.lepu.demo.util.DataConvert

class WirelessDataAdapter(layoutResId: Int, data: MutableList<WirelessData>?) : BaseQuickAdapter<WirelessData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: WirelessData) {
        holder.setText(R.id.text_1, "${context.getString(R.string.duration)}${DataConvert.getEcgTimeStr(item.recordTime)}")
        holder.setText(R.id.text_2, "${context.getString(R.string.total_bytes)}${String.format("%.3f", item.totalBytes.div(1024.0))} kb")
        holder.setText(R.id.text_3, "${context.getString(R.string.total_size)}${item.totalSize}")
        holder.setText(R.id.text_4, "${context.getString(R.string.miss_size)}${item.missSize}")
        holder.setText(R.id.text_5, "${context.getString(R.string.error_bytes)}${item.errorBytes}")
        holder.setText(R.id.text_6, "${context.getString(R.string.miss_percent)}${String.format("%.3f", item.missPercent)} %")
        holder.setText(R.id.text_7, "${context.getString(R.string.error_percent)}${String.format("%.3f", item.errorPercent)} %")
        holder.setText(R.id.text_8, "${context.getString(R.string.one_delay)}${item.oneDelay} ms")
        holder.setText(R.id.text_9, "${context.getString(R.string.total_delay)}${item.totalDelay.div(item.totalSize)} ms")
        holder.setText(R.id.text_10, "${context.getString(R.string.throughput)}${String.format("%.3f", item.throughput)} kb/h")
        holder.setText(R.id.text_11, "${context.getString(R.string.speed)}${String.format("%.3f", item.speed)} b/s")
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