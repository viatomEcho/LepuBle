package com.lepu.demo.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.BpData

class BpAdapter(layoutResId: Int, data: MutableList<BpData>?) : BaseQuickAdapter<BpData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: BpData) {
        holder.setText(R.id.name, "血压文件：${item.fileName}\n收缩压：${item.sys} 舒张压：${item.dia}\n平均压：${item.mean} 脉率：${item.pr}")
    }
}