package com.example.mac.test_picture_camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mac.test_picture_camera.utils.CommonUtils;
import com.example.mac.test_picture_camera.utils.DebugUtils;
import com.example.mac.test_picture_camera.utils.PermissionUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int SDCARD_PERMISSION_REQUEST_CODE = 2;
    private static final int CAMERA_OPEN_REQUEST_CODE = 3;
    private static final int GALLERY_OPEN_REQUEST_CODE = 4;
    private static final int CROP_IMAGE_REQUEST_CODE = 5;

    private Activity mActivity;
    private Context mContext;

    private ImageView mImg;

    private Button mCarmeraBtn;
    private Button mGalleryBtn;

    private String mCameraFilePath = "";
    private String mCropImgFilePath = "";

    private boolean isClickRequestCameraPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;
        mContext = this;

        mImg = findViewById(R.id.img);

        mCarmeraBtn = findViewById(R.id.carmera_btn);
        mGalleryBtn = findViewById(R.id.gallery_btn);


        mCarmeraBtn.setOnClickListener(mOnClickListener);
        mGalleryBtn.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.carmera_btn:
                    if(!PermissionUtils.checkCameraPermission(mContext)){
                        isClickRequestCameraPermission = true;
                        PermissionUtils.requestCameraPermission(mActivity,CAMERA_PERMISSION_REQUEST_CODE);
                    }else {
                        if(!PermissionUtils.checkSDCardPermission(mContext)){
                            isClickRequestCameraPermission = true;
                            PermissionUtils.requestSDCardPermission(mActivity,SDCARD_PERMISSION_REQUEST_CODE);
                        }else{
                            CommonUtils.startCamera(mActivity,CAMERA_OPEN_REQUEST_CODE,generateCameraFilePath());
                        }
                    }
                    break;
                case R.id.gallery_btn:
                    if(!PermissionUtils.checkSDCardPermission(mContext)){
                        PermissionUtils.requestSDCardPermission(mActivity,SDCARD_PERMISSION_REQUEST_CODE);
                    }else{
                        CommonUtils.startGallery(mActivity,GALLERY_OPEN_REQUEST_CODE);
                    }
                    break;
            }
        }
    };

    private String generateCameraFilePath(){
        String mCameraFileDirPath = Environment.getExternalStorageDirectory() + File.separator + "camera";
        File mCameraFileDir = new File(mCameraFileDirPath);
        if(!mCameraFileDir.exists()){
            mCameraFileDir.mkdirs();
        }
        mCameraFilePath = mCameraFileDirPath + File.separator + System.currentTimeMillis() + ".jgp";
        return mCameraFilePath;
    }

    private String generateCropImgFilePath(){
        String mCameraFileDirPath = Environment.getExternalStorageDirectory() + File.separator + "camera";
        File mCameraFileDir = new File(mCameraFileDirPath);
        if(!mCameraFileDir.exists()){
            mCameraFileDir.mkdirs();
        }
        mCropImgFilePath = mCameraFileDirPath + File.separator + System.currentTimeMillis() + ".jgp";
        return mCropImgFilePath;
    }

    private BitmapFactory.Options getBitampOptions(String path){
        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,mOptions);
        return mOptions;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_PERMISSION_REQUEST_CODE:
                if(PermissionUtils.checkRequestPermissionsResult(grantResults)){
                    if(!PermissionUtils.checkSDCardPermission(mContext)){
                        PermissionUtils.requestSDCardPermission(mActivity,SDCARD_PERMISSION_REQUEST_CODE);
                    }else{
                        isClickRequestCameraPermission = false;
                        CommonUtils.startCamera(mActivity,CAMERA_OPEN_REQUEST_CODE,generateCameraFilePath());
                    }
                }else{
                    CommonUtils.showMsg(mContext,"打开照相机请求被拒绝!");
                }
                break;
            case SDCARD_PERMISSION_REQUEST_CODE:
                if(PermissionUtils.checkRequestPermissionsResult(grantResults)){
                    if(isClickRequestCameraPermission){
                        isClickRequestCameraPermission = false;
                        CommonUtils.startCamera(mActivity,CAMERA_OPEN_REQUEST_CODE,generateCameraFilePath());
                    }else{
                        CommonUtils.startGallery(mActivity,GALLERY_OPEN_REQUEST_CODE);
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        DebugUtils.d(TAG,"onActivityResult::requestCode = " + requestCode);
        DebugUtils.d(TAG,"onActivityResult::resultCode = " + resultCode);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case CAMERA_OPEN_REQUEST_CODE:
                    if(data == null || data.getExtras() == null){
                        DebugUtils.d(TAG,"onActivityResult::CAMERA_OPEN_REQUEST_CODE::data null");
                        //mImg.setImageBitmap(BitmapFactory.decodeFile(mCameraFilePath));

                        BitmapFactory.Options mOptions = getBitampOptions(mCameraFilePath);
                        generateCropImgFilePath();
                        CommonUtils.startCropImage(
                                mActivity,
                                mCameraFilePath,
                                mCropImgFilePath,
                                mOptions.outWidth,
                                mOptions.outHeight,
                                mImg.getWidth(),
                                mImg.getHeight(),
                                CROP_IMAGE_REQUEST_CODE);
                    }else{
                        Bundle mBundle = data.getExtras();
                        DebugUtils.d(TAG,"onActivityResult::CAMERA_OPEN_REQUEST_CODE::data = " + mBundle.get("data"));
                    }
                    break;
                case GALLERY_OPEN_REQUEST_CODE:
                    if(data == null){
                        DebugUtils.d(TAG,"onActivityResult::GALLERY_OPEN_REQUEST_CODE::data null");
                    }else{
                        DebugUtils.d(TAG,"onActivityResult::GALLERY_OPEN_REQUEST_CODE::data = " + data.getData());
                        String mGalleryPath = CommonUtils.parseGalleryPath(mContext,data.getData());
                        DebugUtils.d(TAG,"onActivityResult::GALLERY_OPEN_REQUEST_CODE::mGalleryPath = " + mGalleryPath);
                        /*
                        mImg.setImageBitmap(BitmapFactory.decodeFile(mGalleryPath));
                        */


                        BitmapFactory.Options mOptions = getBitampOptions(mGalleryPath);
                        generateCropImgFilePath();
                        CommonUtils.startCropImage(
                                mActivity,
                                mGalleryPath,
                                mCropImgFilePath,
                                mOptions.outWidth,
                                mOptions.outHeight,
                                mImg.getWidth(),
                                mImg.getHeight(),
                                CROP_IMAGE_REQUEST_CODE);
                    }
                    break;
                case CROP_IMAGE_REQUEST_CODE:
                    DebugUtils.d(TAG,"onActivityResult::CROP_IMAGE_REQUEST_CODE::mCropImgFilePath = " + mCropImgFilePath);
                    mImg.setImageBitmap(BitmapFactory.decodeFile(mCropImgFilePath));
                    break;
            }
        }
    }
}
