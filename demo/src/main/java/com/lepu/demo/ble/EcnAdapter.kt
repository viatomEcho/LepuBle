package com.lepu.demo.ble

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lepu.demo.R
import com.lepu.demo.data.EcnData

class EcnAdapter(layoutResId: Int, data: MutableList<EcnData>?) : BaseQuickAdapter<EcnData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: EcnData) {
        holder.setText(R.id.name, "${item.fileName}")
    }
}