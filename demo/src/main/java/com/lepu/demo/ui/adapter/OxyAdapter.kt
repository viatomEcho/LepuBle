package com.lepu.demo.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.OxyData
import com.lepu.demo.util.DataConvert

class OxyAdapter(layoutResId: Int, data: MutableList<OxyData>?) : BaseQuickAdapter<OxyData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: OxyData) {
        holder.setText(R.id.name, "${context.getString(R.string.oxy_files)}\n" +
                "${item.fileName}\n" +
                "${context.getString(R.string.duration)}${DataConvert.getEcgTimeStr(item.oxyBleFile.recordingTime)}")
    }
}