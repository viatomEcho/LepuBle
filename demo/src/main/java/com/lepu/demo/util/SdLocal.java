package com.lepu.demo.util;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;

public class SdLocal {

    public static final String LEPU_FOLDER_ROOT = "LepuBleDemo";
    public static String LEPU_PATH_SD = "";

    /**
     * 外置存储
     * @param context
     * @return
     */
    public static String getLepuRootPathSD(Context context) {
        if (TextUtils.isEmpty(LEPU_PATH_SD)) {
            String path = SdUtil.getStorgePath(context);
            if (!TextUtils.isEmpty(path)) {
                LEPU_PATH_SD = String.format("%s/%s",path,LEPU_FOLDER_ROOT);
            }
        }

        File file = new File(LEPU_PATH_SD);
        if(!file.exists()){
            file.mkdirs();
        }
        return LEPU_PATH_SD;
    }

    // ==================================================================================
    /**
     * 内置app存储
     * @param context
     * @return
     */
//    private static String getLepuRootPath(Context context) {
//        if (StringUtil.stringIsEmpty(LEPU_PATH)) {
//            String path = context.getFilesDir().getAbsolutePath();
//            // String path = SdUtil.getStorgePath(context);
//            if (!StringUtil.stringIsEmpty(path)) {
//                LEPU_PATH = path + LEPU_FOLDER;
//            }
//
//            //空间不足时，
////			try {
////				if(!SdUtil.checkIfFreeSpace(LEPU_PATH)) {
////					LEPU_PATH = getLepuRootPathSD(context);
////				}
////			}catch (Exception e){
////			}
//        }
//        return LEPU_PATH;
//    }

    public static String getTempFolder(Context context) {
        String folder = StringUtil.combinePath(
                getLepuRootPathSD(context), "/Temp");

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folder;
    }

    // 获取download folder
    public static String getDownloadFolder(Context context) {
        String folder = StringUtil.combinePath(
                getLepuRootPathSD(context), "/Download");

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folder;
    }

    public static String getUpgradeFolder(Context context) {
        String folder = StringUtil.combinePath(
                getLepuRootPathSD(context), "/Upgrade");
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folder;
    }

    public static String getcjbUpgradeFolder(Context context) {
        String folder = StringUtil.combinePath(
                getLepuRootPathSD(context), "/CjbUpgrade");

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folder;
    }

    public static String getImportEcgDataFolder(Context context) {
        String folder = StringUtil.combinePath(
                getLepuRootPathSD(context), "/ImportEcgData");

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folder;
    }

    public static String getCacheImgFoler(){
        return StringUtil.combinePath("/cache","update.zip");
    }
    public static String getDownloadRomPath(Context context, String name) {
        return StringUtil.combinePath(getDownloadFolder(context), String.format("%s.zip", name));
    }
    public static String getLogFolder(Context context) {
        String folder = StringUtil.combinePath(
                getLepuRootPathSD(context), "/Log");

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folder;
    }

    public static String getExportDataFolder(Context context) {
        String folder = StringUtil.combinePath(
                getLepuRootPathSD(context), "/ExportData");

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folder;
    }

    public static String getDataFolder(Context context) {

        String fileFolder = StringUtil.combinePath(
                getLepuRootPathSD(context), "Data");

        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        return fileFolder;
    }

    public static String getTestDataFolder(Context context) {

        String fileFolder = StringUtil.combinePath(
                getLepuRootPathSD(context), "Test/Data");

        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        return fileFolder;
    }

    public static String getTestResultFolder(Context context) {

        String fileFolder = StringUtil.combinePath(
                getLepuRootPathSD(context), "Test/Result");

        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdirs();
        }

        return fileFolder;
    }

    //================内部context目录================
    public static String getDatabasePath(Context context, String dbName) {
        return context.getDatabasePath(dbName).getPath();
    }

    public static String getContextFileDatPath(Context context, String fileName) {
        return String.format("%s/%s.dat",context.getFilesDir().getPath(),fileName);
    }

    //=================temp=========================
    public static String getTempImagePath(Context context, String fileName) {
        return StringUtil.combinePath(getTempFolder(context), String.format("%s.jpg",fileName));
    }
    public static String getTempDatPath(Context context, String fileName) {
        return StringUtil.combinePath(getTempFolder(context), String.format("%s.dat",fileName));
    }
    public static String getTempBmpPath(Context context, String fileName) {
        return StringUtil.combinePath(getTempFolder(context), String.format("%s.bmp",fileName));
    }

    //======================download =================
    public static String getDownloadDbPath(Context context, String fileName) {
        return StringUtil.combinePath(getDownloadFolder(context), String.format("%s",fileName));
    }
    public static String getDownloadApkPath(Context context, String fileName) {
        return StringUtil.combinePath(getDownloadFolder(context), String.format("%s.apk",fileName));
    }

    //============================upgrade=================
    public static String getUpgradeApkPath(Context context, String fileName) {
        return StringUtil.combinePath(getUpgradeFolder(context), String.format("%s.apk",fileName));
    }

    //=============================log========================================
    public static String getLogDatPath(Context context, String fileName) {
        return StringUtil.combinePath(getLogFolder(context), String.format("%s.dat",fileName));
    }

    // ============================data=========================================================
    public static String getDataXmlHl7Path(Context context, String fileName) {
        return StringUtil.combinePath(getDataFolder(context), String.format("%s.xml",fileName));
    }

    public static String getDataDatPath(Context context, String fileName) {
        return StringUtil.combinePath(getDataFolder(context), String.format("%s.dat",fileName));
    }

    //==================================export data============================================
    public static String getExportDataHl7XmlPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s.xml",fileName));
    }

    public static String getDataXmlHl7resultPath(Context context, String fileName) {
        return StringUtil.combinePath(getDataFolder(context), String.format("%s_result.xml",fileName));
    }

    public static String getExportDataCarewellXmlPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s_carewell.xml",fileName));
    }

    public static String getExportDataBmpPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s.bmp",fileName));
    }

    public static String getExportDataJpgPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s.jpg",fileName));
    }

    public static String getExportDataPdfPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s.pdf",fileName));
    }

    public static String getExportDataScpPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s.scp",fileName));
    }

    public static String getExportDataDicomPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s.dcm",fileName));
    }

    public static String getExportDataTxtPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s.txt",fileName));
    }

    public static String getExportDataDatPath(Context context, String fileName) {
        return StringUtil.combinePath(getExportDataFolder(context), String.format("%s.dat",fileName));
    }

    public static String getTestResultXmlHl7resultPath(Context context, String fileName) {
        return StringUtil.combinePath(getTestResultFolder(context), String.format("%s_result.xml",fileName));
    }

    public static String getSignatureFolder(Context context){
        return getLepuRootPathSD(context)+"/Signature";
    }

}
