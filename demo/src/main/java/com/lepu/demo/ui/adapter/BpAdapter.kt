package com.lepu.demo.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.BpData

class BpAdapter(layoutResId: Int, data: MutableList<BpData>?) : BaseQuickAdapter<BpData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: BpData) {
        holder.setText(R.id.name, "${context.getString(R.string.bp_files)}${item.fileName}\n" +
                "${context.getString(R.string.sys)}${item.sys} ${context.getString(R.string.dia)}${item.dia}\n" +
                "${context.getString(R.string.mean)}${item.mean} ${context.getString(R.string.pr)}：${item.pr}")
    }
}