package com.lepu.demo

import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lepu.demo.ble.WirelessDataAdapter
import com.lepu.demo.data.WirelessData
import com.lepu.demo.util.FileUtil
import org.json.JSONObject

class WirelessDataActivity : AppCompatActivity() {

    private lateinit var recordList: RecyclerView
    private lateinit var recordAdapter: WirelessDataAdapter
    private lateinit var textView: TextView
    private var records = mutableListOf<WirelessData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wireless_data)
        initView()
        initData()
    }

    private fun initView() {
        textView = findViewById(R.id.total_count)
        recordList = findViewById(R.id.wireless_data)
        LinearLayoutManager(this).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            recordList.layoutManager = this
        }
        recordAdapter = WirelessDataAdapter(R.layout.wireless_data_item, null).apply {
            recordList.adapter = this
        }
        recordAdapter.setOnItemDeleteClickListener(object : WirelessDataAdapter.onItemDeleteListener {
            override fun onDeleteClick(position: Int) {
                val mDialog = AlertDialog.Builder(this@WirelessDataActivity)
                    .setCancelable(false)
                    .setMessage("是否删除记录?")
                    .setPositiveButton("确定") { _, _ ->
                        records.remove(records[position])
                        recordAdapter.setNewInstance(records)
                        recordAdapter.notifyDataSetChanged()
                        textView.text = "共"+ records.size + "条"
                        var temp = ""
                        for (r in records) {
                            temp += r.toString()
                        }
                        FileUtil.saveTextFile("${getExternalFilesDir(null)?.absolutePath}/wireless_test.txt", temp, false)
                    }
                    .setNegativeButton("取消") { _, _ ->
                    }
                    .create()
                mDialog.show()
            }
        })
    }

    private fun initData() {
        val data = FileUtil.readFileToString(this, "wireless_test.txt")
        val strs = data.split("WirelessData")
        if (strs.isEmpty()) return
        for (str in strs) {
            if (str.isEmpty()) continue
            val temp = JSONObject(str)
            val da = WirelessData()
            da.startTime = temp.getLong("startTime")
            da.receiveBytes = temp.getInt("receiveBytes")
            da.missSize = temp.getInt("missSize")
            da.errorBytes = temp.getInt("errorBytes")
            da.totalBytes = temp.getInt("totalBytes")
            da.totalSize = temp.getInt("totalSize")
            da.recordTime = temp.getInt("recordTime")
            da.errorPercent = temp.getDouble("errorPercent")
            da.missPercent = temp.getDouble("missPercent")
            da.speed = temp.getDouble("speed")
            da.throughput = temp.getDouble("throughput")
            da.oneDelay = temp.getLong("oneDelay")
            da.totalDelay = temp.getLong("totalDelay")
            records.add(da)
        }
        recordAdapter.setNewInstance(records)
        recordAdapter.notifyDataSetChanged()
        textView.text = "共"+ records.size + "条"
    }

}