package com.lepu.demo.views;

import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.lepu.demo.R;
import java.text.DecimalFormat;

/**
 * @author chenyongfeng
 */
public class HrMarkerView extends com.github.mikephil.charting.components.MarkerView {

    private TextView tvContent;
    private DecimalFormat format;
    private LineData mData;

    public HrMarkerView(Context context, int layoutResource, LineData lineData) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
        format = new DecimalFormat("###");
        mData = lineData;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e.getData().equals(PhoneGlobal.MARK_INVALID)) {
            tvContent.setText(mData.getDataSetByIndex(1).isHighlightEnabled() ? "--" + "\n" + mData.getXVals().get(e.getXIndex()) : "");
        } else if (e.getData().equals(PhoneGlobal.MARK_VIBRATE)) {
            tvContent.setText("");
        } else {
            tvContent.setText(mData.getDataSetByIndex(0).isHighlightEnabled() ? format.format(Float.parseFloat(e.getData().toString())) + "/min" + "\n" + mData.getXVals().get(e.getXIndex()) : "");
        }
    }

    @Override
    public int getXOffset(float xpos) {
        return -(getWidth() / 8);
    }

    @Override
    public int getYOffset(float ypos) {
        return -(getHeight() / 8);
    }
}
