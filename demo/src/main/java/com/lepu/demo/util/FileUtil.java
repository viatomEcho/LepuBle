package com.lepu.demo.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.lepu.blepro.ble.data.Th12BleFileSmall;
import com.lepu.blepro.ble.data.Th12BleFile;
import com.lepu.blepro.utils.LepuBleLog;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

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

    public static void readFileAll(Context context) {
        File file = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/th12/");
        file = new File(file, "HolterECGData_1.dat");
        File file2 = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/th12/");
        file2 = new File(file2, "HolterECGData2_ecg.dat");

        try {
            if (!file2.exists())
                file2.createNewFile();
            Th12BleFileSmall th12BleFile2 = new Th12BleFileSmall("HolterECGData2", FileUtils.readFileToByteArray(file));
            FileOutputStream fos = new FileOutputStream(file2);
            fos.write(th12BleFile2.getOriginalEcgData());
            fos.close();

            String[] strings = th12BleFile2.getMitHeadData();
            for (int i=0; i<strings.length; i++) {
                Log.d("test12345", "===========strings[i] == " + strings[i]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void readFile(Context context) {
        Th12BleFile th12BleFile = new Th12BleFile("HolterECGData");

        File file = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/th12/");
        file = new File(file, "HolterECGData_1.dat");

        File file2 = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/th12/");
        file2 = new File(file2, "HolterECGData_data.txt");

        try {
            if (!file2.exists())
                file2.createNewFile();

            InputStream inputStream = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(file2);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file2));
            byte[] headBytes = new byte[2901];
            byte[] tempBytes = new byte[9008];
            int count;

            inputStream.read(headBytes);
            th12BleFile.parseHeadData(headBytes);
            int total = th12BleFile.getValidLength()/9008;
            int index = 0;

            while ((count = inputStream.read(tempBytes)) != -1) {
                fos.write(th12BleFile.getTwoSecondEcgData(tempBytes));

                /*short[] shorts = th12BleFile.getTwoSecondLeadData("I", tempBytes);
                for (int i=0; i<shorts.length; i++) {
                    bufferedWriter.write(String.valueOf(shorts[i]));
                    bufferedWriter.write(",");
                }*/

                index++;
                if (index == total)
                    break;
            }
            Log.d("test12345", "th12BleFile.getFileName() == " + th12BleFile.getFileName());
            Log.d("test12345", "th12BleFile.getEcgTime() == " + th12BleFile.getEcgTime());

            String[] strings = th12BleFile.getMitHeadData();
            for (int i=0; i<strings.length; i++) {
                Log.d("test12345", "------------strings[i] == " + strings[i]);
            }
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
