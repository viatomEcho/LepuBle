package com.lepu.demo.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hi.dhl.jdatabinding.binding
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.databinding.FragmentInfoBinding


class InfoFragment : Fragment(R.layout.fragment_info){

    private val mainViewModel: MainViewModel by activityViewModels()

    private val binding: FragmentInfoBinding by binding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }
    private fun initView(){
        mainViewModel.er1Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.er2Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.pc80bInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.bp2Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.bpmInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.oxyInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        // 公共方法测试
        // 获取设备信息
        binding.getInfo.setOnClickListener {
            LpBleUtil.getInfo(Constant.BluetoothConfig.currentModel[0])
        }
        // 获取文件列表
        binding.getFileList.setOnClickListener {
            LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0])
        }
        // 读文件
        binding.readFile.setOnClickListener {

        }
        // 暂停读取文件
        binding.pauseRf.setOnClickListener {

        }
        // 继续读取文件
        binding.continueRf.setOnClickListener {

        }
        // 更新配置
        binding.updateSet.setOnClickListener {

        }
        // 复位
        binding.reset.setOnClickListener {
            LpBleUtil.reset(Constant.BluetoothConfig.currentModel[0])
        }
        // 恢复出厂设置
        binding.factory.setOnClickListener {
            LpBleUtil.factoryReset(Constant.BluetoothConfig.currentModel[0])
        }
        // 恢复出厂状态
        binding.factoryAll.setOnClickListener {
            LpBleUtil.factoryResetAll(Constant.BluetoothConfig.currentModel[0])
        }

    }

    private fun initEvent(){




    }

}