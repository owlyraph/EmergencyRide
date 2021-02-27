package com.example.emergencyride.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.emergencyride.ClientMapActivity;
import com.example.emergencyride.DriverMapActivity;
import com.example.emergencyride.FindAmbulanceActivity;
import com.example.emergencyride.MainActivity;
import com.example.emergencyride.R;
import com.example.emergencyride.ShowCaptureActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

  public class CameraFragment extends Fragment implements SurfaceHolder.Callback {

      Camera camera;

      Camera.PictureCallback jpegCallBack;
      SurfaceView mSurfaceView;
      SurfaceHolder mSurfaceHolder;

      final int CAMERA_REQUEST_CODE=1;
      public static CameraFragment newInstance(){
          CameraFragment fragment= new CameraFragment();
          return fragment;
      }


      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
          View view =inflater.inflate(R.layout.fragment_camera ,container, false);

          mSurfaceView=view.findViewById(R.id.surfaceView);
          mSurfaceHolder=mSurfaceView.getHolder();

          if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
              ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
          }else{
              mSurfaceHolder.addCallback(this);
              mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
          }
          Button mBack=view.findViewById(R.id.back);
          Button mCapture=view.findViewById(R.id.capture);
          Button mFindAmb=view.findViewById(R.id.findAmbulance);
          mFindAmb.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  findAmbulance();

              }
          });
          mBack.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Back();
              }
          });
          mCapture.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  captureImage();
              }
          });

          jpegCallBack=new Camera.PictureCallback() {
              @Override
              public void onPictureTaken(byte[] data, Camera camera) {
                  Bitmap decodedBitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
                  Bitmap rotateBitmap = rotate(decodedBitmap);

                  String fileLocation = SaveImageToStorage(rotateBitmap);
                  if(fileLocation!= null){
                      Intent intent = new Intent(getActivity(), ShowCaptureActivity.class);
                      startActivity(intent);
                      return;
                  }
              }
          };

          return view;
      }

      public String SaveImageToStorage(Bitmap bitmap){
          String fileName="ImageToSend";
          try {
              ByteArrayOutputStream bytes= new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
              FileOutputStream fo = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
              fo.write(bytes.toByteArray());
              fo.close();
          }catch(Exception e){
              e.printStackTrace();
              fileName = null;
          }
          return fileName;
      }

      private Bitmap rotate(Bitmap decodedBitmap) {
          int w=decodedBitmap.getWidth();
          int h=decodedBitmap.getHeight();

          Matrix matrix= new Matrix();
          matrix.setRotate(90);
          return Bitmap.createBitmap(decodedBitmap, 0,0,w,h,matrix,true);
      }

      private void captureImage() {
          camera.takePicture(null,null, jpegCallBack);
      }


      @Override
      public void surfaceCreated(SurfaceHolder holder) {
          camera=Camera.open();

          Camera.Parameters parameters;
          parameters=camera.getParameters();

          camera.setDisplayOrientation(90);
          parameters.setPreviewFrameRate(30);
          parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

          Camera.Size bestSize=null;
          List<Camera.Size> sizeList=camera.getParameters().getSupportedPreviewSizes();
          bestSize=sizeList.get(0);
          for (int i=1; i<sizeList.size();i++){
              if ((sizeList.get(i).width * sizeList.get(i).height)>(bestSize.width *bestSize.height)){
                  bestSize=sizeList.get(i);
              }
          }
          parameters.setPreviewSize(bestSize.width,bestSize.height);
          camera.setParameters(parameters);

          try {
              camera.setPreviewDisplay(holder);
          } catch (IOException e) {
              e.printStackTrace();
          }
          camera.startPreview();

      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {
      }

      @Override
      public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
          super.onRequestPermissionsResult(requestCode, permissions, grantResults);
          switch (requestCode){
              case CAMERA_REQUEST_CODE:{
                  if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                      mSurfaceHolder.addCallback(this);
                      mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                  }else {
                      Toast.makeText(getContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                  }
                  break;
              }
          }
      }


      private void findAmbulance() {
          Intent intent=new Intent(getContext(), FindAmbulanceActivity.class);
          startActivity(intent);
          return;
      }

    private void Back() {
        Intent intent=new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return;
    }

}
