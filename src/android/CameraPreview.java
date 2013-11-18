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

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private static final String TAG = "CameraPreview";

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {

			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();	
			
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

		if (mHolder.getSurface() == null) {
			return;
		}
		
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
		}

		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}
	
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	
    	int idealH, idealW, difH, difW;
	    float zoom;
    	
	    // Adjust surface view size to match camera preview and stretch it to fit screen
    	if(changed && l == 0 && t == 0) {
    		
    		Camera.Size actualPreviewSize = mCamera.getParameters().getPreviewSize();
    	    
    	    difW = (r - actualPreviewSize.width);
    	    difH = (b - actualPreviewSize.height);
    	    
    	    if(difW > difH) {
    	    	zoom = (float) b / actualPreviewSize.height;
    	    } else {
    	    	zoom = (float) r / actualPreviewSize.width;
    	    }
    	    
    	    idealH = Math.round(actualPreviewSize.height * zoom);
    	    idealW = Math.round(actualPreviewSize.width * zoom);
    	    
    	    difW = (r - idealW) / 2;
    	    difH = (b - idealH) / 2;
    		
    		this.layout(difW, difH, (idealW + difW), (idealH + difH));
    		
    	}
    	
    }
}
