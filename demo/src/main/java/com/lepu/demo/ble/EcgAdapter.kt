package com.lepu.demo.ble

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.EcgData
import com.lepu.demo.util.DataConvert

class EcgAdapter(layoutResId: Int, data: MutableList<EcgData>?) : BaseQuickAdapter<EcgData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: EcgData) {
        holder.setText(R.id.name, "心电文件\n${item.fileName}\n记录时长：${DataConvert.getEcgTimeStr(item.duration)}")
    }
}