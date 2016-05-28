package com.flintsoft.miman;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;

/**
 * Created by Xin on 2015/8/8.
 */
public class Util {
    public static final String uniqueCacheName = "imgCache";

    public static File getDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            File cacheDir = context.getExternalCacheDir() == null? context.getCacheDir() : context.getExternalCacheDir();
            cachePath = cacheDir.getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueCacheName);
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
