package com.lepu.demo.ui.notifications

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hi.dhl.jdatabinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.MainViewModel
import com.lepu.demo.R
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant
import com.lepu.demo.databinding.FragmentInfoBinding


class InfoFragment : Fragment(R.layout.fragment_info){

    private val mainViewModel: MainViewModel by activityViewModels()

    private val binding: FragmentInfoBinding by binding()

    private var fileNames: ArrayList<String> = arrayListOf()

    private var curFileName = ""
    private var readFileProcess = ""
    private var fileCount = 0

    private var fileType = Ble.File.ECG_TYPE

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
        mainViewModel.pc100Info.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        mainViewModel.boInfo.observe(viewLifecycleOwner, {
            binding.info.text = it.toString()
        })
        // 公共方法测试
        // 获取设备信息
        binding.getInfo.setOnClickListener {
            LpBleUtil.getInfo(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        // 获取文件列表
        binding.getFileList.setOnClickListener {
            fileCount = 0
            fileNames.clear()

            fileType++
            if (fileType > 2) {
                fileType = 0
            }
            if (Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_O2RING || Constant.BluetoothConfig.currentModel[0] == Bluetooth.MODEL_BABYO2) {
                LpBleUtil.getInfo(Constant.BluetoothConfig.currentModel[0])
            } else {
                LpBleUtil.getFileList(Constant.BluetoothConfig.currentModel[0], fileType)
            }
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        // 读文件
        binding.readFile.setOnClickListener {
            readFileProcess = ""
            readFile()
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
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        // 恢复出厂设置
        binding.factory.setOnClickListener {
            LpBleUtil.factoryReset(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }
        // 恢复出厂状态
        binding.factoryAll.setOnClickListener {
            LpBleUtil.factoryResetAll(Constant.BluetoothConfig.currentModel[0])
            binding.sendCmd.text = "send : " + LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
        }

        binding.getMtu.setOnClickListener {
            binding.sendCmd.text = "mtu : " + LpBleUtil.getBleMtu(Constant.BluetoothConfig.currentModel[0])
        }
        binding.setMtu.setOnClickListener {
            var mtu = 0
            if (binding.mtuText.text.toString() != "")
                mtu = binding.mtuText.text.toString().toInt()
            LpBleUtil.setBleMtu(Constant.BluetoothConfig.currentModel[0], mtu)
        }

    }

    private fun initEvent(){
        //--------------------------------er1 duoek-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList)
            .observe(this, { event ->
                (event.data as String).let {
                    binding.info.text = it
                    for (fileName in it.split(",")) {
                        if (fileName.contains("R")) {
                            fileNames.add(fileName)
                        }
                    }
                    Toast.makeText(context, "er1/duoek 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadingFileProgress)
            .observe(this, { event ->
                (event.data as Int).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it/10).toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileComplete)
            .observe(this, { event ->
                (event.data as Er1BleResponse.Er1File).let {
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n"
                    fileNames.removeAt(0)
                    readFile()
                }
            })
        //--------------------------------er2-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList)
            .observe(this, { event ->
                (event.data as Er2FileList).let {
                    binding.info.text = it.toString()
                    for (fileName in it.fileNames) {
                        if (fileName.contains("R")) {
                            fileNames.add(fileName)
                        }
                    }
                    Toast.makeText(context, "er2 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadingFileProgress)
            .observe(this, { event ->
                (event.data as Int).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it/10).toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileComplete)
            .observe(this, { event ->
                (event.data as Er2File).let {
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n"
                    fileNames.removeAt(0)
                    readFile()
                }
            })
        //--------------------------------lew3-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3FileList)
            .observe(this, { event ->
                (event.data as LeW3FileList).let {
                    binding.info.text = it.toString()
                    for (fileName in it.fileNames) {
                        fileNames.add(fileName)
                    }
                    Toast.makeText(context, "lew3 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3ReadingFileProgress)
            .observe(this, { event ->
                (event.data as Int).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it/10).toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LeW3.EventLeW3ReadFileComplete)
            .observe(this, { event ->
                (event.data as LeW3File).let {
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n"
                    fileNames.removeAt(0)
                    readFile()
                }
            })
        //--------------------------------bp2-----------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList)
            .observe(this, { event ->
                if (event.model == Bluetooth.MODEL_BP2W) {
                    (event.data as Bp2BleFile).let {
                        when (it.type) {
                            Ble.File.ECG_TYPE -> {
                                val data = Bp2wEcgList(it.content)
                                binding.info.text = data.toString()
                            }
                            Ble.File.BP_TYPE -> {
                                val data = Bp2wBpList(it.content)
                                binding.info.text = data.toString()
                            }
                            Ble.File.USER_TYPE -> {
                                val data = Bp2wUserList(it.content)
                                binding.info.text = data.toString()
                            }
                            else -> {
                                binding.info.text = it.toString()
                            }
                        }
                    }
                } else {
                    (event.data as KtBleFileList).let {
                        binding.info.text = it.toString()
                        for (fileName in it.fileNameList) {
                            if (fileName != null)
                                fileNames.add(fileName)
                        }
                        Toast.makeText(
                            context,
                            "bp2 获取文件列表成功 共有${fileNames.size}个文件",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress)
            .observe(this, { event ->
                (event.data as Bp2FilePart).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it.percent*100).toInt().toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete)
            .observe(this, { event ->
                (event.data as Bp2BleFile).let {
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n"
                    fileNames.removeAt(0)
                    readFile()
                }
            })
        //------------------------------bpm--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BPM.EventBpmRecordData)
            .observe(this, { event ->
                (event.data as BpmCmd).let {
                    fileCount++
                    readFileProcess += BpmData(it.data).toString() + " fileCount : $fileCount \n\n"
                    binding.info.text = readFileProcess
                }
            })
        //------------------------------pc100--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpResult)
            .observe(this, { event ->
                (event.data as Pc100BleResponse.BpResult).let {
                    binding.info.text = it.toString()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.PC100.EventPc100BpErrorResult)
            .observe(this, { event ->
                (event.data as Pc100BleResponse.BpResultError).let {
                    binding.info.text = it.toString()
                }
            })
        //------------------------------o2--------------------------------------
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo)
            .observe(this, { event ->
                (event.data as OxyBleResponse.OxyInfo).let {
                    for (fileName in it.fileList.split(",")) {
                        fileNames.add(fileName)
                    }
                    Toast.makeText(context, "o2 获取文件列表成功 共有${fileNames.size}个文件", Toast.LENGTH_SHORT).show()
                    binding.info.text = it.toString()
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadingFileProgress)
            .observe(this, { event ->
                (event.data as Int).let {
                    binding.process.text = readFileProcess + curFileName + " 读取进度:" + (it/10).toString() + "%"
                }
            })
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileComplete)
            .observe(this, { event ->
                (event.data as OxyBleResponse.OxyFile).let {
                    readFileProcess = "$readFileProcess$curFileName 读取进度:100% \n"
                    fileNames.removeAt(0)
                    readFile()
                }
            })
    }


    private fun readFile() {
        if (fileNames.size == 0) return
        curFileName = fileNames[0]
        LpBleUtil.readFile("", fileNames[0], Constant.BluetoothConfig.currentModel[0])
        binding.sendCmd.text = LpBleUtil.getSendCmd(Constant.BluetoothConfig.currentModel[0])
    }

}