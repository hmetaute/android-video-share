package com.audio.record;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends Activity {

	private static final String TAG = "Record";

	private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videorecorderlayout);
        
        final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        final Button captureButton = (Button) findViewById(R.id.button_capture);
        final Button playButton = (Button) findViewById(R.id.button_play);
        
        
        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        
        preview.addView(mPreview);
        // Add a listener to the Capture button
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    if (isRecording) {
                        // stop recording and release camera
                        mMediaRecorder.stop();  // stop the recording
                        releaseMediaRecorder(); // release the MediaRecorder object
                        mCamera.lock();         // take camera access back from MediaRecorder

                        // inform the user that recording has stopped
                        captureButton.setText("Capture");
                        isRecording = false;
                    } else {
                        // initialize video camera
                        if (prepareVideoRecorder()) {
                            // Camera is available and unlocked, MediaRecorder is prepared,
                            // now you can start recording
                            mMediaRecorder.start();

                            // inform the user that recording has started
                            captureButton.setText("Stop");
                            isRecording = true;
                        } else {
                            // prepare didn't work, release the camera
                            releaseMediaRecorder();
                            // inform user
                        }
                    }
                }
            }
        );
        
        playButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mCamera.stopPreview();
				preview.removeAllViews();
				final VideoView video = new VideoView(MainActivity.this);
				video.setVideoURI(Uri.fromFile(new File("/sdcard/someexamplevideo.mp4")));
				video.setMediaController(new MediaController(MainActivity.this));
				video.requestFocus();
				video.start();
				video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					
					public void onCompletion(MediaPlayer mp) {
						preview.removeAllViews();
						preview.addView(mPreview);
					}
				});
				preview.addView(video);
//				MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, Uri.fromFile(new File("/sdcard/someexamplevideo.mp4")));
//				mediaPlayer.start();
			}
		});
    }
	
    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
    	releaseCamera();
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    
    private boolean prepareVideoRecorder(){
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile("/sdcard/someexamplevideo.mp4");

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}