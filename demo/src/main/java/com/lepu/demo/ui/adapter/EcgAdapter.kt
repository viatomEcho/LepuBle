package com.lepu.demo.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.EcgData
import com.lepu.demo.util.DataConvert

class EcgAdapter(layoutResId: Int, data: MutableList<EcgData>?) : BaseQuickAdapter<EcgData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: EcgData) {
        holder.setText(R.id.name, "${context.getString(R.string.ecg_files)}\n" +
                "${item.fileName}\n" +
                "${context.getString(R.string.duration)}${DataConvert.getEcgTimeStr(item.duration)}")
    }
}