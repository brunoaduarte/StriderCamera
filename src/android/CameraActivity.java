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
import java.util.List;

import com.strider.app.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
	private ImageView mImageView;
	private boolean pressed = false;

	private Button captureButton;
	private Button cancelButton;
	private Button saveButton;
	private Button backButton;

	private byte[] photoData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_sv);

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create a Preview and set it as the content of activity.
		mPreview = new CameraPreview(this, mCamera);
		mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mFrameLayout.addView(mPreview);

		mImageView = (ImageView) findViewById(R.id.imageView1);

		// get Camera parameters
		Camera.Parameters params = mCamera.getParameters();
		List<String> focusModes = params.getSupportedFocusModes();

		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		// set Camera parameters
		mCamera.setParameters(params);

		// Add listener to buttons
		captureButton = (Button) findViewById(R.id.button_capture);
		cancelButton = (Button) findViewById(R.id.button_cancel);
		saveButton =  (Button) findViewById(R.id.button_save);
		backButton =  (Button) findViewById(R.id.button_back);

		captureButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (pressed) return;
				pressed = true; // Set pressed = true to prevent freezing. Issue 1 at http://code.google.com/p/foreground-camera-plugin/issues/detail?id=1
				mCamera.takePicture(null, null, mPicture); // get an image from the camera
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pressed = false;
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				writePhotoData();
				
				setResult(RESULT_OK);
				pressed = false;
				finish();
			}
		});

		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				HideConfirmation();
				pressed = false;

				onResume();
				mCamera.startPreview();
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

			mCamera.stopPreview();
			onPause();

			photoData = data;

			Bitmap bm = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
			ShowConfirmation(bm);
		}
	};

	private void ShowConfirmation(Bitmap bm) {

//		mFrameLayout.setVisibility(View.GONE);

		mImageView.setImageBitmap(bm);
		mImageView.setVisibility(View.VISIBLE);

		captureButton.setVisibility(View.GONE);
		cancelButton.setVisibility(View.GONE);
		saveButton.setVisibility(View.VISIBLE);
		backButton.setVisibility(View.VISIBLE);

	}

	private void HideConfirmation() {

		mImageView.setVisibility(View.GONE);

//		mFrameLayout.setVisibility(View.VISIBLE);

		captureButton.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);
		saveButton.setVisibility(View.GONE);
		backButton.setVisibility(View.GONE);		

	}

	private void writePhotoData() {

		if(photoData != null) {
			Uri fileUri = (Uri) getIntent().getExtras().get(MediaStore.EXTRA_OUTPUT);
			File pictureFile = new File(fileUri.getPath());
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(photoData);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
		}
	}

}