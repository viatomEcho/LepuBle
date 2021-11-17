package com.lepu.demo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import com.lepu.blepro.utils.LepuBleLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;

/**
 * @author wxd
 */
public class FileUtil {
    /**
     * 创建该路径的父文件夹
     * @param path
     * @return
     */
    public static boolean createParentDirFile(String path) {
        File file = new File(path);
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return true;
    }


    public static void saveFile(String filePath, byte[] data, boolean isAppend) {
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath, isAppend);
            fos.write(data);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveTextFile(String filePath, String data, boolean isAppend) {
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LepuBleLog.e(Log.getStackTraceString(e));
            }
        }

        BufferedWriter out=null;
        try {
            out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file,isAppend), "UTF-8"));
            //out = new BufferedWriter(new FileWriter(file,isAppend));
            out.write(data);
            out.flush();
        } catch (FileNotFoundException e) {
            LepuBleLog.e(Log.getStackTraceString(e));
        } catch (IOException e) {
            LepuBleLog.e(Log.getStackTraceString(e));
        } catch (Exception e) {
            LepuBleLog.e(Log.getStackTraceString(e));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    LepuBleLog.e(Log.getStackTraceString(e));
                }
            }
        }
    }

    public static void deleteFile(Context context,File file) {
        if (file == null) {
            return;
        }

        if (file.exists()) {
            if (file.isFile()) {
                try {
                    // 先对要删除的文件进行重命名，然后再删除。这样删除过程中的文件锁就加在另一个文件上了，不会影响再次创建的过程
                    // 防止出现 open failed: EBUSY (Device or resource busy)
//                    File to = new File(file.getAbsolutePath()
//                            + System.currentTimeMillis());
//                    file.renameTo(to);
//                    to.delete();

                    file.delete();
                } catch (Exception e) {
                    LepuBleLog.e(Log.getStackTraceString(e));
                }
            } else if (file.isDirectory()) {
                try {
                    File[] files = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFile(context,files[i]);
                    }
//                    File to = new File(file.getAbsolutePath()
//                            + System.currentTimeMillis());
//                    file.renameTo(to);
//                    to.delete();

                    file.delete();
                } catch (Exception e) {
                    LepuBleLog.e(Log.getStackTraceString(e));
                }
            }

            scanFile(context, file);//通知pc
        }
    }
    /**
     * 扫描文件 多次调用就不起作用了
     * @param context
     * @param file
     */
    public static void scanFile(Context context,File file) {
        try {
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(file));
            context.sendBroadcast(scanIntent);
        }catch (Exception e){
            LepuBleLog.e(Log.getStackTraceString(e));
        }
    }
}
