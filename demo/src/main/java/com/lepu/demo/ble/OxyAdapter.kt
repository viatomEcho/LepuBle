package com.lepu.demo.ble

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.OxyData
import com.lepu.demo.util.DataConvert

class OxyAdapter(layoutResId: Int, data: MutableList<OxyData>?) : BaseQuickAdapter<OxyData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: OxyData) {
        holder.setText(R.id.name, "血氧文件\n${item.fileName}\n记录时长：${DataConvert.getEcgTimeStr(item.oxyBleFile.recordingTime)}")
    }
}