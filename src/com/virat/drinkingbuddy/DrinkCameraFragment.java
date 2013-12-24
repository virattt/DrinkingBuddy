package com.virat.drinkingbuddy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class DrinkCameraFragment extends Fragment {
	private static final String TAG = "drinkCameraFragment";
	
	public static final String EXTRA_PHOTO_FILENAME = 
			"com.virat.drinkingbuddy.photo_filename";

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;
    private ImageButton mTakePictureButton;
    private ImageButton mFlashButton;
    private boolean mFlashButtonOn = false;

    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drink_camera, parent, false);
        
        mTakePictureButton = (ImageButton)v
            .findViewById(R.id.drink_camera_takePictureButton);
        
        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mCamera != null) {
                	mCamera.takePicture(mShutterCallback, null, mJpegCallback);
                }
            }
        });
        
        mFlashButton = (ImageButton)v.findViewById(R.id.drink_camera_flashButton);
        mFlashButton.setBackgroundResource(R.drawable.ic_flash_off_holo_light_large);

        mSurfaceView = (SurfaceView)v.findViewById(R.id.drink_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        // setType() and SURFACE_TYPE_PUSH_BUFFERS are both deprecated,
        // but are required for Camera preview to work on pre-3.0 devices.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        holder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceCreated(SurfaceHolder holder) {
                // Tell the camera to use this surface as its preview area
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                        
	                    // set SurfaceView to Portrait mode
	                    if (Build.VERSION.SDK_INT >= 8) 
	                    	mCamera.setDisplayOrientation(90);
	                    
                    }
                } catch (IOException exception) {
                    Log.e(TAG, "Error setting up preview display", exception);
                }   
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // We can no longer display on this surface, so stop the preview.
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
            
            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            	if (mCamera == null) return;

                // The surface has changed size; update the camera preview size
                Camera.Parameters parameters = mCamera.getParameters();
                Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), w, h);
                parameters.setPreviewSize(s.width, s.height);
                s = getBestSupportedSize(parameters.getSupportedPictureSizes(), w, h);
                parameters.setPictureSize(s.width, s.height);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                mCamera.setParameters(parameters);
                
                // toggle flash button
                mFlashButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!mFlashButtonOn) {
					        mFlashButton.setBackgroundResource(0);
			                Camera.Parameters parameters = mCamera.getParameters();
			                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON); // turn on flash 
			                mCamera.setParameters(parameters);
			                mFlashButton.setBackgroundResource(R.drawable.ic_flash_on_holo_light_large);
			                mFlashButtonOn = true; // flash button is now on
						} else {
					        mFlashButton.setBackgroundResource(0);
							Camera.Parameters parameters = mCamera.getParameters();
			                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			                mCamera.setParameters(parameters);
			                mFlashButton.setBackgroundResource(R.drawable.ic_flash_off_holo_light_large);
			                mFlashButtonOn = false; // flash button is now off 
						}
					}
				});
                
                // handle auto focus touching
                
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }
        });
        
        mProgressContainer = v.findViewById(R.id.drink_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);
        
        return v;
    }
    
    /** A simple algorithm to get the largest size available. For a more
     * robust version, see CameraPreview.java in the ApiDemos
     * sample app from Android. */
    private Size getBestSupportedSize(List<Size> sizes, int width, int height) {
        Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }
    
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		@Override
		public void onShutter() {
			// Display the progress indicator
			mProgressContainer.setVisibility(View.VISIBLE);
		}
	};
	
	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// Create a filename
			String filename = UUID.randomUUID().toString() + ".jpg";
			// Save the jpeg data to disk
			FileOutputStream os = null;
			boolean success = true;
			try {
				os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
				os.write(data);
			} catch (Exception e) {
				Log.e(TAG, "Error writing to file " + filename, e);
				success = false;
			} finally {
				try {
					if (os != null)
						os.close();
				} catch (Exception e) {
					Log.e(TAG, "Error closing file " + filename, e);
					success = false;
				}
			}
			// Set the photo filename and result intent
			if (success) {
				Intent i = new Intent();
				i.putExtra(EXTRA_PHOTO_FILENAME, filename);
				getActivity().setResult(Activity.RESULT_OK, i);
			} else {
				getActivity().setResult(Activity.RESULT_CANCELED);
			}
			getActivity().finish();
		}
	};
    
    @TargetApi(9)
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
