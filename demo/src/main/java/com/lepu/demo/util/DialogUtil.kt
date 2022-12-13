package com.lepu.demo.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.lepu.demo.R

/**
 * @ClassName DialogUtil
 * @Description TODO
 * @Author wujuan
 * @Date 2021/10/27 18:19
 */
object DialogUtil {

    fun showDurationDialog(activity: Activity, text: String, listener: (String) -> Unit) {
        val dialog = Dialog(activity, R.style.dialog_style)
        dialog.setContentView(R.layout.duration_dialog)
        dialog.setCanceledOnTouchOutside(false)//点击外部不关闭
        dialog.setCancelable(false)//点击返回键不关闭
        //获取对话框的窗口，并设置窗口参数
        dialog.show()

        val duration = dialog.findViewById<EditText>(R.id.duration)
        duration.setText(text)
        duration.setSelection(text.length)
        duration.setSingleLine()

        dialog.setOnDismissListener { setBackgroundAlpha(activity, false) }
        dialog.findViewById<TextView>(R.id.noteOk).setOnClickListener {
            dialog.dismiss()
            setBackgroundAlpha(activity, false)
            listener.invoke(duration.text.toString().trim())
        }
        dialog.findViewById<TextView>(R.id.noteCancel).setOnClickListener {
            dialog.dismiss()
            setBackgroundAlpha(activity, false)
        }
    }

    fun getScreen(context: Context, boolean: Boolean): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.defaultDisplay.getRealSize(point)
        } else {
            wm.defaultDisplay.getSize(point)
        }
        return if (boolean) point.x else point.y
    }

    fun setBackgroundAlpha(activity: Activity, boolean: Boolean) {
        val lp = activity.window.attributes
        lp.alpha = if (boolean) 0.7f else 1f
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        activity.window.attributes = lp
    }
}