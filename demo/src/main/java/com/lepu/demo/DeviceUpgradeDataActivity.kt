package com.lepu.demo

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lepu.demo.ble.DeviceUpgradeDataAdapter
import com.lepu.demo.data.DeviceUpgradeData
import com.lepu.demo.util.DateUtil
import com.lepu.demo.util.ExcelUtil
import com.lepu.demo.util.FileUtil
import org.json.JSONObject
import java.util.*

class DeviceUpgradeDataActivity : AppCompatActivity() {

    private lateinit var totalTextView: TextView
    private lateinit var exportButton: Button
    private lateinit var deleteButton: Button
    private lateinit var recordList: RecyclerView
    private lateinit var recordAdapter: DeviceUpgradeDataAdapter
    private var records = mutableListOf<DeviceUpgradeData>()

    var mAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_data)
        initView()
        initData()
        mAlertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage("正在处理，请稍等...")
            .create()
    }

    private fun initView() {
        recordList = findViewById(R.id.device_factory_data)
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            recordList.layoutManager = this
        }
        recordAdapter = DeviceUpgradeDataAdapter(R.layout.upgrade_data, null).apply {
            recordList.adapter = this
        }
        recordAdapter.setOnItemDeleteClickListener(object : DeviceUpgradeDataAdapter.onItemDeleteListener{
            override fun onDeleteClick(position: Int) {
                val mDialog = AlertDialog.Builder(this@DeviceUpgradeDataActivity)
                    .setCancelable(false)
                    .setMessage("是否删除记录?")
                    .setPositiveButton("确定") { _, _ ->
                        records.remove(records.asReversed()[position])
                        recordAdapter.setNewInstance(records.asReversed())
                        recordAdapter.notifyDataSetChanged()
                        totalTextView.text = "共${records.size}条"
                        var temp = ""
                        for (r in records) {
                            temp += r.toString()
                        }
                        FileUtil.saveTextFile("${getExternalFilesDir(null)?.absolutePath}/device_upgrade_data.txt", temp, false)
                        Toast.makeText(this@DeviceUpgradeDataActivity, "删除成功", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("取消") { _, _ ->
                    }
                    .create()
                mDialog.show()
            }

        })
        totalTextView = findViewById(R.id.total_count)
        exportButton = findViewById(R.id.export)
        exportButton.setOnClickListener {
            if (records.size == 0) {
                Toast.makeText(this, "暂无记录需要导出！", Toast.LENGTH_SHORT).show()
            } else {
                exportData()
            }
        }
        deleteButton = findViewById(R.id.delete)
        deleteButton.setOnClickListener {
            val r = FileUtil.deleteFile(this, "device_upgrade_data.txt")
            if (r) {
                records.clear()
                recordAdapter.setNewInstance(records.asReversed())
                recordAdapter.notifyDataSetChanged()
                totalTextView.text = "共${records.size}条"
                Toast.makeText(this, "清空成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "清空失败(无数据/存储未授权)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportData() {
        val filePath = "/sdcard/Documents/"
        val fileName = "升级信息" + DateUtil.stringFromDate(Date(System.currentTimeMillis()), "yyyyMMdd") + ".xls"
        val title = arrayOf("序号", "升级时间", "蓝牙名", "蓝牙地址", "sn")
        val sheetName = "升级信息"
        val initResult = ExcelUtil.initExcel(filePath+fileName, sheetName, title)
        if (initResult) {
            val writeResult = ExcelUtil.writeObjListToExcelUpgrade(records.asReversed(), filePath+fileName)
            if (writeResult) {
                mAlertDialog?.show()
                Handler().postDelayed({
                    mAlertDialog?.dismiss()
                    Toast.makeText(this, "导出成功，请在手机存储文档查看！(Documents文件夹下)", Toast.LENGTH_LONG).show()
                }, 200)
            }
        } else {
            Toast.makeText(this, "导出失败，请检查是否授予读写权限！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initData() {
        val data = FileUtil.readFileToString(this, "device_upgrade_data.txt")
        val strs = data.split("DeviceUpgradeData")
        if (strs.isEmpty()) return
        for (str in strs) {
            if (str.isEmpty()) continue
            val temp = JSONObject(str)
            val da = DeviceUpgradeData()
            da.time = infoStrGetString(temp, "time")
            da.name = infoStrGetString(temp, "name")
            da.address = infoStrGetString(temp, "address")
            da.sn = infoStrGetString(temp, "sn")
            records.add(da)
        }
        recordAdapter.setNewInstance(records.asReversed())
        recordAdapter.notifyDataSetChanged()
        totalTextView.text = "共${records.size}条"
    }

    private fun infoStrGetString(infoStr: JSONObject, key: String): String {
        return if (infoStr.has(key)) {
            infoStr.getString(key)
        } else {
            ""
        }
    }

}
