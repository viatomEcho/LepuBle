package com.lepu.demo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class DemoWidgetProvider extends AppWidgetProvider {

    public static void updateWidgetView(Context context, String str) {
        //初始化RemoteViews
        ComponentName componentName = new ComponentName(context, DemoWidgetProvider.class);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_appwidget);

        //点击事件，点击跳转到MainActivity页面
        /*Intent startActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent processInfoIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            processInfoIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            processInfoIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        remoteViews.setOnClickPendingIntent(R.id.lly_bg, processInfoIntent);*/

        //更新文本数据
        remoteViews.setTextViewText(R.id.lly_text, str);

        //开始更新视图
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        awm.updateAppWidget(componentName, remoteViews);
    }

}
