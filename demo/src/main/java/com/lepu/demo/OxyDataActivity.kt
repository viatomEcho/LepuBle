package com.lepu.demo

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.formatter.XAxisValueFormatter
import com.github.mikephil.charting.formatter.YAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ViewPortHandler
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.oxyData
import com.lepu.demo.views.*
import java.util.*

class OxyDataActivity : AppCompatActivity() {

    private lateinit var mSpO2Chart: SpO2Chart
    private lateinit var mHrChart: HrChart
    private lateinit var mMovementChart: MovementChart
    private var minHr = 35
    private var height = 0
    private var bound = 0f

    //时分
    private val xVals = ArrayList<String>()
    //时分秒
    private val timeList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oxy_data)
        initView()
    }

    private fun initView() {
        findViewById<TextView>(R.id.avg_spo2_val).text = "${oxyData.oxyBleFile.avgSpo2}"
        findViewById<TextView>(R.id.min_spo2_val).text = "${oxyData.oxyBleFile.minSpo2}"
        findViewById<TextView>(R.id.drops_3_val).text = "${oxyData.oxyBleFile.dropsTimes3Percent}"
        findViewById<TextView>(R.id.drops_4_val).text = "${oxyData.oxyBleFile.dropsTimes4Percent}"
        findViewById<TextView>(R.id.below_90_duration_val).text = "${oxyData.oxyBleFile.durationTime90Percent}"
        findViewById<TextView>(R.id.below_90_time_val).text = "${oxyData.oxyBleFile.dropsTimes90Percent}"
        findViewById<TextView>(R.id.asleep_percent_val).text = "${oxyData.oxyBleFile.asleepTimePercent}"
        findViewById<TextView>(R.id.asleep_time_val).text = "${oxyData.oxyBleFile.asleepTime}"
        findViewById<TextView>(R.id.o2_score_val).text = "${oxyData.oxyBleFile.o2Score.div(10f)}"
        findViewById<TextView>(R.id.recording_time_val).text = "${oxyData.oxyBleFile.recordingTime}"
        mSpO2Chart = findViewById(R.id.chart_spo2)
        mHrChart = findViewById(R.id.chart_hr)
        mMovementChart = findViewById(R.id.chart_movement)
        mSpO2Chart.post {
            mSpO2Chart.height
            mSpO2Chart.width
            height = mSpO2Chart.measuredHeight
            val diff = 32
            val top = 75
            val rate: Float = (150 - minHr) / 120f //0.5f;
            bound = ((height - 2 * top + diff) * rate + top) / height //震动标记上边界y值轴占图的比例
            PhoneGlobal.VibratiobMarkHeight = (height - 2 * top + diff) * (1 - rate) //震动标记图形高度
            makeSpo2Wave()
            makeHrWave()
            makeMovementWave()
        }
    }

    private fun makeSpo2Wave() {
        mSpO2Chart.setDescription("")
        mSpO2Chart.setDescriptionColor(resources.getColor(R.color.colorBlueDark))
        mSpO2Chart.setDescriptionTextSize(12f)
        mSpO2Chart.setNoDataTextDescription("No Data")
        mSpO2Chart.isDoubleTapToZoomEnabled = false
        mSpO2Chart.isHighlightPerDragEnabled = true
        mSpO2Chart.setTouchEnabled(true)
        mSpO2Chart.isDragEnabled = true
        mSpO2Chart.setScaleEnabled(true)
        mSpO2Chart.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        mSpO2Chart.setDrawBorders(true)
        mSpO2Chart.setBorderColor(resources.getColor(R.color.colorDeviceGrey)) //上面的边框颜色

        mSpO2Chart.setBorderWidth(1.0f)

        val xAxis = mSpO2Chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineWidth = 1.0f
        xAxis.axisLineColor = resources.getColor(R.color.colorDeviceGrey)
        xAxis.valueFormatter =
            XAxisValueFormatter { original: String?, index: Int, viewPortHandler: ViewPortHandler? -> " " }

        val leftAxis = mSpO2Chart.axisLeft
        leftAxis.textColor = resources.getColor(R.color.colorBlueDark)
        leftAxis.isGranularityEnabled = true
        leftAxis.setAxisMaxValue(100f)
        leftAxis.setAxisMinValue(70f)
        leftAxis.setLabelCount(3, true)
        leftAxis.maxWidth = 30f //使三张图标签宽度保持一致

        leftAxis.minWidth = 30f
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.textSize = 12f
        leftAxis.gridColor = resources.getColor(R.color.colorDeviceGrey)
        leftAxis.axisLineColor = resources.getColor(R.color.colorDeviceGrey)
        leftAxis.valueFormatter =
            YAxisValueFormatter { value: Float, yAxis: YAxis? -> " " + value.toInt() }

        val rightAxis = mSpO2Chart.axisRight
        rightAxis.textColor = resources.getColor(R.color.colorBlueDark)

        rightAxis.isGranularityEnabled = false
        rightAxis.setAxisMaxValue(100f)
        rightAxis.setAxisMinValue(70f)
        rightAxis.setLabelCount(3, true)

        rightAxis.setDrawLabels(false)
        rightAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)
        rightAxis.gridColor = resources.getColor(R.color.colorDeviceGrey)
        rightAxis.axisLineColor = resources.getColor(R.color.colorDeviceGrey)
        rightAxis.valueFormatter = YAxisValueFormatter { value: Float, yAxis: YAxis? -> "" }

        val paint = mSpO2Chart.renderer.paintRender
        val linGrad = LinearGradient(
            0f, 0f, 0f, height*1f, intArrayOf(
                resources.getColor(R.color.text_green),
                resources.getColor(R.color.colorOrange),
                resources.getColor(R.color.red_m),
                resources.getColor(R.color.red_m),
                resources.getColor(R.color.white)
            ), floatArrayOf(0.25f, 0.32f, 0.50f, 0.861f, 0.862f), Shader.TileMode.CLAMP
        )
        paint.shader = linGrad
        mSpO2Chart.data = makeSpo2Data()
        val markerView = SpO2MarkerView(this, R.layout.spo2_marker_view, mSpO2Chart.lineData)
        mSpO2Chart.markerView = markerView
        mSpO2Chart.animateX(300)
        val l = mSpO2Chart.legend
        l.form = Legend.LegendForm.LINE
        l.formSize = 0f //不显示图例

        l.textSize = 12f
        l.textColor = resources.getColor(R.color.colorBlueDark)
        l.position = Legend.LegendPosition.ABOVE_CHART_LEFT //hide unused legend

        // dont forget to refresh the drawing
        mSpO2Chart.invalidate()
    }
    private fun makeSpo2Data(): LineData {
        val o2List = getAllDataList(0)

        setTimeData(o2List)

        val yVals = arrayListOf<Entry>() //可见
        val yValsInvalid = arrayListOf<Entry>() //不可见
        for (i in o2List.indices) {
            if (o2List[i] == -1) {
                yValsInvalid.add(Entry(90f, i, PhoneGlobal.MARK_INVALID))
            } else {
                yVals.add(Entry(o2List[i].toFloat(), i, PhoneGlobal.MARK_VALID))
            }
        }
        val vibrationList = getAllSign(0)
        val vibrate = arrayListOf<Entry>()
        for (i in o2List.indices) {
            if (vibrationList[i]) {
                vibrate.add(Entry(70f, i, PhoneGlobal.MARK_VIBRATE))
            }
        }
        val lineDataSet1 = LineDataSet(yVals, "氧含量（%）")
        lineDataSet1.axisDependency = YAxis.AxisDependency.LEFT
        lineDataSet1.color = resources.getColor(R.color.color_ecg_bkg)
        lineDataSet1.setCircleColor(resources.getColor(R.color.color_ecg_bkg))
        lineDataSet1.cubicIntensity = 1f
        lineDataSet1.lineWidth = 1f
        lineDataSet1.circleSize = 0f
        lineDataSet1.fillAlpha = 65
        lineDataSet1.fillColor = resources.getColor(R.color.color_ecg_bkg)
        lineDataSet1.isHighlightEnabled = false
        lineDataSet1.enableDashedHighlightLine(10f, 5f, 0f)
        lineDataSet1.highLightColor = resources.getColor(R.color.color_ecg_bkg)
        lineDataSet1.highlightLineWidth = 1f
        lineDataSet1.setDrawValues(false)
        lineDataSet1.valueTextColor = resources.getColor(R.color.red_b)
        lineDataSet1.valueFormatter =
            ValueFormatter { value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler? ->
                value.toInt().toString() + ""
            }
        val lineDataSetInvalid = LineDataSet(yValsInvalid, "invalid_spo2")
        lineDataSetInvalid.isVisible = false
        if (yVals.isNotEmpty()) {
            lineDataSetInvalid.label = " "
        }
        lineDataSetInvalid.axisDependency = YAxis.AxisDependency.LEFT
        lineDataSetInvalid.color = resources.getColor(R.color.color_ecg_bkg)
        lineDataSetInvalid.setCircleColor(resources.getColor(R.color.color_ecg_bkg))
        lineDataSetInvalid.lineWidth = 1f
        lineDataSetInvalid.circleSize = 0f
        lineDataSetInvalid.fillAlpha = 65
        lineDataSetInvalid.fillColor = resources.getColor(R.color.color_ecg_bkg)
        lineDataSetInvalid.isHighlightEnabled = false
        lineDataSetInvalid.enableDashedHighlightLine(10f, 5f, 0f)
        lineDataSetInvalid.highLightColor = resources.getColor(R.color.color_ecg_bkg)
        lineDataSetInvalid.highlightLineWidth = 1f
        lineDataSetInvalid.setDrawValues(false)
        lineDataSetInvalid.valueTextColor = resources.getColor(R.color.red_b)
        lineDataSetInvalid.valueFormatter =
            ValueFormatter { value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler? ->
                " " //不显示--
            }
        val lineDataSet2 = LineDataSet(vibrate, "Vibration")
        lineDataSet2.axisDependency = YAxis.AxisDependency.LEFT
        lineDataSet2.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet2.setDrawValues(false)
        lineDataSet2.circleRadius = 0f
        lineDataSet2.label = null
        lineDataSet2.isHighlightEnabled = false
        lineDataSet2.valueFormatter =
            ValueFormatter { value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler? -> " " }
        val dataSets = arrayListOf<ILineDataSet>()
        dataSets.add(lineDataSet1)
        dataSets.add(lineDataSetInvalid)
        dataSets.add(lineDataSet2)
        return LineData(timeList, dataSets)
    }

    private fun makeHrWave() {
        mHrChart.setDescription("")
        mHrChart.setDescriptionColor(resources.getColor(R.color.teal_200))
        mHrChart.setDescriptionTextSize(12f)
        mHrChart.setNoDataTextDescription("No Data")
        mHrChart.setNoDataText("")
        mHrChart.isDoubleTapToZoomEnabled = false
        mHrChart.isHighlightPerDragEnabled = true
        mHrChart.setTouchEnabled(true)
        mHrChart.isDragEnabled = true
        mHrChart.setScaleEnabled(true)
        val paint: Paint = mHrChart.renderer.paintRender
        mHrChart.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        mHrChart.setDrawBorders(true)
        mHrChart.setBorderColor(resources.getColor(R.color.colorDeviceGrey)) //上面的边框颜色
        mHrChart.setBorderWidth(1.0f)
        val xAxis: XAxis = mHrChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineWidth = 1.0f
        xAxis.axisLineColor = resources.getColor(R.color.colorDeviceGrey)
        xAxis.valueFormatter =
            XAxisValueFormatter { original: String?, index: Int, viewPortHandler: ViewPortHandler? -> " " }
        val leftAxis: YAxis = mHrChart.axisLeft
        leftAxis.textColor = resources.getColor(R.color.teal_200)
        leftAxis.isGranularityEnabled = true
        leftAxis.setAxisMaxValue(150f)
        leftAxis.setAxisMinValue(30f)
        leftAxis.gridColor = resources.getColor(R.color.colorDeviceGrey)
        leftAxis.axisLineColor = resources.getColor(R.color.colorDeviceGrey)
        leftAxis.setLabelCount(3, true)
        leftAxis.maxWidth = 30f
        leftAxis.minWidth = 30f
        leftAxis.textSize = 12f
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.valueFormatter = YAxisValueFormatter { value: Float, yAxis: YAxis? ->
            value.toInt().toString() + ""
        }
        val rightAxis: YAxis = mHrChart.axisRight
        rightAxis.textColor = resources.getColor(R.color.teal_200)
        rightAxis.isGranularityEnabled = false
        rightAxis.setAxisMaxValue(150f)
        rightAxis.setAxisMinValue(30f)
        rightAxis.setDrawLabels(false)
        rightAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)
        rightAxis.gridColor = resources.getColor(R.color.colorDeviceGrey)
        rightAxis.axisLineColor = resources.getColor(R.color.colorDeviceGrey)
        rightAxis.setLabelCount(3, true)
        rightAxis.valueFormatter = YAxisValueFormatter { value: Float, yAxis: YAxis? -> "" }

        val linGrad = LinearGradient(
            0f, 0f, 0f, height*1f, intArrayOf(
                resources.getColor(R.color.red_m),
                resources.getColor(R.color.colorOrange),
                resources.getColor(R.color.text_green),
                resources.getColor(R.color.text_green),
                resources.getColor(R.color.white)
            ), floatArrayOf(0.22f, 0.416f, 0.5f, bound, bound), Shader.TileMode.CLAMP
        )
        paint.shader = linGrad
        mHrChart.data = makeHrData()
        val markerView = HrMarkerView(this, R.layout.hr_marker_view, mHrChart.lineData)
        mHrChart.markerView = markerView
        mHrChart.animateX(300)
        val l: Legend = mHrChart.legend
        l.form = Legend.LegendForm.LINE
        l.formSize = 0f //不显示图例
        l.textSize = 12f
        l.textColor = resources.getColor(R.color.teal_200)
        l.position = Legend.LegendPosition.ABOVE_CHART_LEFT
        // dont forget to refresh the drawing
        mHrChart.invalidate()
    }
    private fun makeHrData(): LineData {
        val o2List = getAllDataList(1)
//        setTimeData(o2List)
        val yVals1 = arrayListOf<Entry>()
        val yValsInvalid = arrayListOf<Entry>()
        for (i in o2List.indices) {
            val value = o2List[i]
            if (o2List[i] == 65535) {
                yValsInvalid.add(Entry(120f, i, PhoneGlobal.MARK_INVALID))
            } else {
                yVals1.add(Entry((if (value > 150) 150 else value).toFloat(), i, value))
            }
        }

        val virationList = getAllSign(1)
        val vibrate = arrayListOf<Entry>()
        for (i in o2List.indices) {
            if (virationList[i]) {
                vibrate.add(Entry(30f, i, PhoneGlobal.MARK_VIBRATE))
            }
        }

        val lineDataSet1 = LineDataSet(yVals1, "脉率")
        lineDataSet1.axisDependency = YAxis.AxisDependency.RIGHT
        lineDataSet1.color = resources.getColor(R.color.teal_200)
        lineDataSet1.setCircleColor(resources.getColor(R.color.teal_200))
        lineDataSet1.lineWidth = 1f
        lineDataSet1.cubicIntensity = 1f
        lineDataSet1.circleSize = 0f
        lineDataSet1.fillAlpha = 65
        lineDataSet1.isHighlightEnabled = false
        lineDataSet1.enableDashedHighlightLine(10f, 5f, 0f)
        lineDataSet1.highLightColor = resources.getColor(R.color.color_ecg_bkg)
        lineDataSet1.highlightLineWidth = 1f
        lineDataSet1.setDrawValues(false)
        lineDataSet1.valueTextColor = resources.getColor(R.color.red_b)
        lineDataSet1.fillColor = resources.getColor(R.color.color_ecg_bkg)
        lineDataSet1.valueFormatter =
            ValueFormatter { value: Float, entry: Entry, dataSetIndex: Int, viewPortHandler: ViewPortHandler? ->
                entry.data.toString()
            }
        val lineDataSetInvalid = LineDataSet(yValsInvalid, "invalid_hr")
        lineDataSetInvalid.isVisible = false
        lineDataSetInvalid.label = " "
        lineDataSetInvalid.axisDependency = YAxis.AxisDependency.LEFT
        lineDataSetInvalid.color = resources.getColor(R.color.color_ecg_bkg)
        lineDataSetInvalid.setCircleColor(resources.getColor(R.color.color_ecg_bkg))
        lineDataSetInvalid.lineWidth = 1f
        lineDataSetInvalid.circleSize = 0f
        lineDataSetInvalid.fillAlpha = 65
        lineDataSetInvalid.fillColor = resources.getColor(R.color.color_ecg_bkg)
        lineDataSetInvalid.isHighlightEnabled = false
        lineDataSetInvalid.enableDashedHighlightLine(10f, 5f, 0f)
        lineDataSetInvalid.highLightColor = resources.getColor(R.color.color_ecg_bkg)
        lineDataSetInvalid.highlightLineWidth = 1f
        lineDataSetInvalid.setDrawValues(false)
        lineDataSetInvalid.valueTextColor = resources.getColor(R.color.red_b)
        lineDataSetInvalid.valueFormatter =
            ValueFormatter { value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler? ->
                " " //不显示--
            }
        val lineDataSet2 = LineDataSet(vibrate, "Vibration")
        lineDataSet2.axisDependency = YAxis.AxisDependency.LEFT
//        lineDataSet2.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet2.setDrawValues(false)
        lineDataSet2.circleRadius = 0f
        lineDataSet2.label = null
        lineDataSet2.isHighlightEnabled = false
        lineDataSet2.valueFormatter =
            ValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
                " " //不显示--
            }
        val dataSets = arrayListOf<ILineDataSet>()
        dataSets.add(lineDataSet1)
        dataSets.add(lineDataSetInvalid)
        dataSets.add(lineDataSet2)
        return LineData(timeList, dataSets)
    }

    private fun makeMovementWave() {
        mMovementChart.setDescription("")
        mMovementChart.setDescriptionColor(resources.getColor(R.color.colorOrange))
        mMovementChart.setDescriptionTextSize(12f)
        mMovementChart.setNoDataTextDescription("No Data")
        mMovementChart.setDrawBarShadow(false)
        mMovementChart.isDoubleTapToZoomEnabled = false //双击缩放
        mMovementChart.isHighlightPerDragEnabled = true
        mMovementChart.setTouchEnabled(true)
        mMovementChart.isDragEnabled = true
        mMovementChart.setScaleEnabled(true)
        mMovementChart.isScaleYEnabled = false
        val paint: Paint = mMovementChart.renderer.paintRender
        mMovementChart.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        mMovementChart.setDrawBorders(true)
        mMovementChart.setBorderColor(resources.getColor(R.color.colorDeviceGrey)) //上面的边框颜色
        mMovementChart.setBorderWidth(1.0f)
        mMovementChart.setDrawGridBackground(false)
        val xAxis: XAxis = mMovementChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM //底部
        xAxis.textSize = 10f //时间大小
        xAxis.textColor = resources.getColor(R.color.colorDeviceGrey) //颜色值
        xAxis.setDrawGridLines(false) //是否网格线
        xAxis.gridColor = resources.getColor(R.color.colorDeviceGrey) //网格线颜色
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineColor = resources.getColor(R.color.colorDeviceGrey)
        val leftAxis: YAxis = mMovementChart.axisLeft
        leftAxis.isEnabled = true
        leftAxis.setDrawGridLines(false)
        leftAxis.isGranularityEnabled = false
        leftAxis.setDrawAxisLine(false)
        leftAxis.setAxisMaxValue(255f)
        leftAxis.setAxisMinValue(0f)
        leftAxis.maxWidth = 30f
        leftAxis.minWidth = 30f
        leftAxis.valueFormatter = YAxisValueFormatter { value: Float, yAxis: YAxis? -> " " }
        val rightAxis: YAxis = mMovementChart.axisRight
        rightAxis.isEnabled = true
        rightAxis.setDrawGridLines(false)
        rightAxis.isGranularityEnabled = false
        rightAxis.setDrawAxisLine(false)
        rightAxis.setAxisMaxValue(255f)
        rightAxis.setAxisMinValue(0f)
        rightAxis.valueFormatter = YAxisValueFormatter { value: Float, yAxis: YAxis? -> "" }
        mMovementChart.data = makeMovementData()
        mMovementChart.animateX(300)

        val l: Legend = mMovementChart.legend
        l.form = Legend.LegendForm.SQUARE
        l.formSize = 0f //不显示图例
        val color = arrayListOf<Int>()
        color.add(resources.getColor(R.color.colorOrange))
        l.setComputedColors(color)
        l.textSize = 13f
        l.textColor = resources.getColor(R.color.colorOrange)
        l.position = Legend.LegendPosition.ABOVE_CHART_LEFT
        mMovementChart.invalidate()
    }
    private fun makeMovementData(): BarData {
        val o2List = getAllDataList(2)
//        setTimeData(o2List)

        val yVals = arrayListOf<BarEntry>()
        for (i in o2List.indices) {
            yVals.add(BarEntry((if (o2List[i] < 0) 0 else o2List[i]).toFloat(), i))
        }
        val barDataSet = BarDataSet(yVals, "体动")
        barDataSet.color = resources.getColor(R.color.colorOrange)
        barDataSet.barSpacePercent = 5f
        barDataSet.setDrawValues(false)
        barDataSet.isHighlightEnabled = false
        barDataSet.barShadowColor = resources.getColor(R.color.colorOrange)
        val dataSets = arrayListOf<IBarDataSet>()
        dataSets.add(barDataSet)
        return BarData(xVals, dataSets)
    }

    private fun getAllDataList(isType: Int): ArrayList<Int> {
        val integers = arrayListOf<Int>()
        for (i in oxyData.oxyBleFile.data.indices) {
            var integer = 0
            when (isType) {
                0 -> integer = oxyData.oxyBleFile.data[i].spo2
                1 -> integer = oxyData.oxyBleFile.data[i].pr
                2 -> integer = oxyData.oxyBleFile.data[i].vector
            }
            integers.add(integer)
        }
        return integers
    }
    private fun getAllSign(isType: Int): ArrayList<Boolean> {
        if (oxyData.oxyBleFile.data.size <= 0) {
            return arrayListOf()
        }
        val integers = arrayListOf<Boolean>()
        for (i in oxyData.oxyBleFile.data.indices) {
            when (isType) {
                0 -> integers.add(oxyData.oxyBleFile.data[i].warningSignSpo2)
                1 -> integers.add(oxyData.oxyBleFile.data[i].warningSignPr)
                2 -> integers.add(oxyData.oxyBleFile.data[i].warningSignVector)
            }
        }
        return integers
    }
    private fun setTimeData(o2List: ArrayList<Int>) {
        for (i in o2List.indices) {
            val startTime: Long = oxyData.oxyBleFile.startTime*1000L + i * 2000
            //体动时分
            xVals.add(stringFromDate(Date(startTime), "HH:mm"))
            //血氧、心率时分秒
            timeList.add(stringFromDate(Date(startTime), "dd日 HH:mm:ss"))
        }
    }

}