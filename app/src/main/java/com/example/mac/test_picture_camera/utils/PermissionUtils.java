package com.example.mac.test_picture_camera.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;

/**
 * Created by mac on 17-12-9.
 */

public class PermissionUtils {

    private static final String TAG = "PermissionUtils";

    public static boolean checkCameraPermission(Context context){
        return  checkPermission(context, Manifest.permission.CAMERA);
    }

    public static boolean checkSDCardPermission(Context context){
        return  checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) && checkPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestCameraPermission(Activity activity, int requestCode){
        requestPermission(activity,new String[]{Manifest.permission.CAMERA},requestCode);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestSDCardPermission(Activity activity, int requestCode){
        requestPermission(activity,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);
    }

    public static boolean checkRequestPermissionsResult(int[] grantResults){
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    public static boolean checkPermission(Context context, String permissionStr){
        boolean result = true;

        DebugUtils.d(TAG,"PermissionUtils::SDK_INT = " + Build.VERSION.SDK_INT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            result = context.checkSelfPermission(permissionStr) == PackageManager.PERMISSION_GRANTED;
        }else{
            result = PermissionChecker.checkSelfPermission(context,permissionStr) == PackageManager.PERMISSION_GRANTED;
        }

        DebugUtils.d(TAG,"PermissionUtils::result = " + result);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermission(Activity activity, String[] permissionStrs, int requestCode){
        activity.requestPermissions(permissionStrs,requestCode);
    }
}
