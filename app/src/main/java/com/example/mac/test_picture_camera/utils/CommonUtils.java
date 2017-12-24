package com.example.mac.test_picture_camera.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mac on 17-12-9.
 */

public class CommonUtils {
    private static final String TAG = "CommonUtils";

    public static void startCamera(Activity activity,int requestCode,String filePath){
        if(hasSdcard()){
            Intent mOpenCameraIntent = new Intent();
            mOpenCameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            Uri desUri = getFileUri(activity,filePath);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                //已申请camera权限
                //mOpenCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            mOpenCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,desUri);
            activity.startActivityForResult(mOpenCameraIntent,requestCode);
        }else{
            showMsg(activity,"设备没有SD卡!");
        }
    }

    public static void startGallery(Activity activity,int requestCode){
        if(hasSdcard()){
            Intent mOpenGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            mOpenGalleryIntent.setType("image/*");
            activity.startActivityForResult(mOpenGalleryIntent,requestCode);
        }else{
            showMsg(activity,"设备没有SD卡!");
        }
    }

    public static void startCropImage(Activity activity, String originPath, String desPath, int aspectX, int aspectY, int outputX, int outputY, int requestCode){
        startCropImage(activity,getFileUri(activity,originPath),getFileUri(activity,desPath),aspectX,aspectY,outputX,outputY,requestCode);
    }

    public static void startCropImage(Activity activity, Uri originUri, Uri desUri, int aspectX, int aspectY, int outputX, int outputY, int requestCode){
        Intent mIntent = new Intent();
        mIntent.setAction("com.android.camera.action.CROP");
        mIntent.setDataAndType(originUri,"image/*");

        List resInfoList = queryActivityByIntent(activity,mIntent);
        if (resInfoList.size() == 0) {
            showMsg(activity, "没有合适的应用程序");
            return;
        }
        Iterator resInfoIterator = resInfoList.iterator();
        while (resInfoIterator.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo) resInfoIterator.next();
            String packageName = resolveInfo.activityInfo.packageName;
            activity.grantUriPermission(packageName, desUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }


        mIntent.putExtra("crop","true");
        mIntent.putExtra("aspectX",aspectX);
        mIntent.putExtra("aspectY",aspectY);
        mIntent.putExtra("outputX",outputX);
        mIntent.putExtra("outputY",outputY);
        mIntent.putExtra("scale",true);

        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, desUri);
        mIntent.putExtra("return-data",false);
        mIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        mIntent.putExtra("noFaceDetection",true);

        activity.startActivityForResult(mIntent,requestCode);
    }

    public static boolean hasSdcard(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static void showMsg(Context context, String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    public static int getScreenWidth(Context context){
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Point mPoint = new Point();
        mWindowManager.getDefaultDisplay().getSize(mPoint);

        return mPoint.x;
    }

    public static int getScreenHeight(Context context){
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Point mPoint = new Point();
        mWindowManager.getDefaultDisplay().getSize(mPoint);

        return mPoint.y;
    }

    public static String parseGalleryPath(Context context,Uri uri){
        String pathHead = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context,uri)){

            String authority = uri.getAuthority();
            DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::authority = " + authority);

            String id = DocumentsContract.getDocumentId(uri);
            DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::id = " + id);

            String[] idArray = id.split(":");
            String type = idArray.length > 0 ? idArray[0] : "";

            Uri contentUri = null;


            if(isExternalStorageDocument(uri)){
                DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::isExternalStorageDocument");
            }else if(isDownloadsDocument(uri)){
                DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::isDownloadsDocument");

                contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(id));
                return pathHead + getDataColumn(context,contentUri,null,null);

            }else if(isMediaDocument(uri)){
                DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::isMediaDocument");

                if("image".equals(type)){
                    DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::isMediaDocument::image");

                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }else if("video".equals(type)){
                    DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::isMediaDocument::video");

                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }else if("audio".equals(type)){
                    DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::isMediaDocument::audio");

                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                DebugUtils.d(TAG,"parseGalleryPath::KITKAT DocumentsContract::isMediaDocument::idArray.length = " + idArray.length);

                if(idArray.length >= 2){
                    String selection = "_id = ? ";
                    String[] selectionArgs = new String[]{idArray[1]};
                    return pathHead + getDataColumn(context,contentUri,selection,selectionArgs);
                }
            }

        }else if("content".equalsIgnoreCase(uri.getScheme())){
            String data = getDataColumn(context,uri,null,null);
            DebugUtils.d(TAG,"parseGalleryPath::content::data = " + data);
            return pathHead + data;
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            String filePath = uri.getPath();
            DebugUtils.d(TAG,"parseGalleryPath::file::filePath = " + filePath);
            return pathHead + filePath;
        }
        return "";
    }

    private static String getDataColumn(Context context,Uri uri,String selection,String[] selectionArgs){
        DebugUtils.d(TAG,"getDataColumn::uri = " + uri);
        DebugUtils.d(TAG,"getDataColumn::selection = " + selection);
        DebugUtils.d(TAG,"getDataColumn::selectionArgs = " + selectionArgs);
        String column = "_data";
        String[] projections = new String[]{column};

        ContentResolver cr = context.getContentResolver();
        Cursor mCursor = cr.query(uri,projections,selection,selectionArgs,null);
        if(mCursor != null){
            if(mCursor.moveToFirst()){
                return mCursor.getString(mCursor.getColumnIndex(column));
            }
            mCursor.close();
        }
        return "";
    }

    private static boolean isExternalStorageDocument(Uri uri){
        String authority = uri.getAuthority();
        return "com.android.externalstorage.documents".equals(authority);
    }

    private static boolean isDownloadsDocument(Uri uri){
        String authority = uri.getAuthority();
        return "com.android.providers.downloads.documents".equals(authority);
    }

    private static boolean isMediaDocument(Uri uri){
        String authority = uri.getAuthority();
        return "com.android.providers.media.documents".equals(authority);
    }

    private static Uri getFileUri(Context context,String filePath){
        Uri mUri = null;
        File mFile = new File(filePath);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mUri = FileProvider.getUriForFile(context,"com.zxl.test_picture_camera",mFile);
        }else{
            mUri = Uri.fromFile(mFile);
        }
        return mUri;
    }

    private static List<ResolveInfo> queryActivityByIntent(Activity activity, Intent intent){
        List resInfoList = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resInfoList;
    }
    
/*
    if (mSaveUri != null) {
        OutputStream outputStream = null;
        try {
            outputStream = mContentResolver.openOutputStream(mSaveUri);
            outputStream.write(data);
            outputStream.close();

            setResult(RESULT_OK);
            finish();
        } catch (IOException ex) {
            // ignore exception
        } finally {
            Util.closeSilently(outputStream);
        }
    } else {
        Bitmap bitmap = createCaptureBitmap(data);
        setResult(RESULT_OK,
                new Intent("inline-data").putExtra("data", bitmap));
        finish();
    }
*/

/*
FileProvider.getUriForFile
android.os.FileUriExposedException: file:///storage/emulated/0/camera/1513393885728.jgp exposed beyond app through ClipData.Item.getUri()
*/


/*
java.lang.SecurityException: Permission Denial: reading android.support.v4.content.FileProvider
uri content://com.zxl.test_picture_camera/camera_gallery/camera/1514101205911.jgp
from pid=5847, uid=10048 requires the provider be exported, or grantUriPermission()
*/

/*
java.lang.SecurityException: Permission Denial: writing android.support.v4.content.FileProvider
uri content://com.zxl.test_picture_camera/camera_gallery/camera/1514101317846.jgp
from pid=8336, uid=10048 requires the provider be exported, or grantUriPermission()
*/


}
