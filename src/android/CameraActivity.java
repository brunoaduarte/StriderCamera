/*
	    Copyright 2013 Havop - Bruno A. Duarte.

		Licensed under the Apache License, Version 2.0 (the "License");
		you may not use this file except in compliance with the License.
		You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

		Unless required by applicable law or agreed to in writing, software
		distributed under the License is distributed on an "AS IS" BASIS,
		WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
		See the License for the specific language governing permissions and
   		limitations under the License.   			
 */

package io.strider.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.example.hello.R;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Camera Activity Class. Configures Android camera to take picture and show it.
 */
public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private Camera mCamera;
	private CameraPreview mPreview;
	private FrameLayout mFrameLayout;
	private boolean pressed = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_sv);

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create a Preview and set it as the content of activity.
		mPreview = new CameraPreview(this, mCamera);
		mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mFrameLayout.addView(mPreview);
		
		// get Camera parameters
		Camera.Parameters params = mCamera.getParameters();
		List<String> focusModes = params.getSupportedFocusModes();

		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); 

		// set Camera parameters
		mCamera.setParameters(params);

		// Add a listener to the Capture button
		Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (pressed) return;
				// Set pressed = true to prevent freezing. Issue 1 at http://code.google.com/p/foreground-camera-plugin/issues/detail?id=1
				pressed = true;

				// get an image from the camera
				mCamera.takePicture(null, null, mPicture);
			}
		});

		Button cancelButton = (Button) findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pressed = false;
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	@Override
	protected void onPause() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mFrameLayout.removeView(mPreview); //Remove the preview from layout.
			mCamera = null;
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mCamera == null) {
			mCamera = getCameraInstance(); //get the camera on resume.
			mPreview = new CameraPreview(this, mCamera); // Create the preview.
			mFrameLayout.addView(mPreview); //Add the preview to layout.
		}
		super.onResume();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {

//	        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//	        if (pictureFile == null){
////	            Log.d(TAG, "Error creating media file, check storage permissions: " + e.getMessage());
//	            return;
//	        }
	    	
			Uri fileUri = (Uri) getIntent().getExtras().get(MediaStore.EXTRA_OUTPUT);
			File pictureFile = new File(fileUri.getPath());

	        try {
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            fos.write(data);
	            fos.close();
	        } catch (FileNotFoundException e) {
	            Log.d(TAG, "File not found: " + e.getMessage());
	        } catch (IOException e) {
	            Log.d(TAG, "Error accessing file: " + e.getMessage());
	        }
			setResult(RESULT_OK);
			pressed = false;
			finish();
	    }
	};
	
//	/** Create a file Uri for saving an image or video */
//	private static Uri getOutputMediaFileUri(int type){
//	      return Uri.fromFile(getOutputMediaFile(type));
//	}
//
//	/** Create a File for saving an image or video */
//	private static File getOutputMediaFile(int type){
//	    // To be safe, you should check that the SDCard is mounted
//	    // using Environment.getExternalStorageState() before doing this.
//
//	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
//	    // This location works best if you want the created images to be shared
//	    // between applications and persist after your app has been uninstalled.
//
//	    // Create the storage directory if it does not exist
//	    if (! mediaStorageDir.exists()){
//	        if (! mediaStorageDir.mkdirs()){
//	            Log.d("MyCameraApp", "failed to create directory");
//	            return null;
//	        }
//	    }
//
//	    // Create a media file name
//	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//	    File mediaFile;
//	    if (type == MEDIA_TYPE_IMAGE){
//	        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
//	    } else if(type == MEDIA_TYPE_VIDEO) {
//	        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
//	    } else {
//	        return null;
//	    }
//
//	    return mediaFile;
//	}
}