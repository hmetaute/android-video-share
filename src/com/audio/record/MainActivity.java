package com.audio.record;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.mail.send.Mail;
import com.mail.send.SendEmailAsyncTask;

public class MainActivity extends Activity {

	private static final String TAG = "Record";

	private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    
    FrameLayout preview;
    
    private String mCameraFileName = "/sdcard/someexamplevideo.mp4";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videorecorderlayout);
        
        //final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        final Button captureButton = (Button) findViewById(R.id.button_capture);
        final Button playButton = (Button) findViewById(R.id.button_play);
        final Button mailButton =  (Button) findViewById(R.id.button_mail);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        
        mailButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,MailActivity.class);
				intent.putExtra(MailActivity.FILENAME, mCameraFileName);
				startActivity(intent);
			}
        	
        });
        /*
        mailButton.setOnClickListener(new OnClickListener() {
			
        	public void onClick(View v) {
        		AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        		alert.setTitle("Ingresa tu correo");
        		alert.setMessage("Ingresa tu correo");

        		// Set an EditText view to get user input
        		final EditText input = new EditText(MainActivity.this);
        		alert.setView(input);
        		android.content.DialogInterface.OnClickListener okListener = new android.content.DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Editable value = input.getText();
		        		Mail m = new Mail("debocaenbocaen@gmail.com", "Tvcamaras1986++");
						
						        		if (BuildConfig.DEBUG) Log.v(SendEmailAsyncTask.class.getName(), "SendEmailAsyncTask()");
						        		String[] toArr = {"debocaenbocaen@gmail.com"};
//						        		String[] toArr = {"hernan.metaute@gmail.com", "debocaenbocaen@gmail.com"};
						        		m.setTo(toArr); 
						        		m.setFrom("debocaenboca@gmail.com"); 
						        		m.setSubject("Video subido"); 
						        		m.setBody("Hola, enviar el video al siguiente correo:" + value.toString());
						
						        		try{
						        			m.addAttachment(mCameraFileName);
						        		}catch(Exception e){
						        			showToast("An errror occur while trying to attach the video");
						        		}
						
						        		new SendEmailAsyncTask(m).execute();
						showToast(value.toString());
						
					}
				};
        		alert.setPositiveButton("Ok", okListener);
        		

        		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        		  public void onClick(DialogInterface dialog, int whichButton) {
        		    // Canceled.
        		  }
        		});

        		alert.show();
        		
        	}
		});*/
        
        // Add a listener to the Capture button
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    if (isRecording) {
                        stopRecording();
                    } else {
                        // initialize video camera
                        if (prepareVideoRecorder()) {
                            // Camera is available and unlocked, MediaRecorder is prepared,
                            // now you can start recording
                            mMediaRecorder.start();

                            // inform the user that recording has started
                            captureButton.setText("Listo");
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
				video.setVideoURI(Uri.fromFile(new File(mCameraFileName)));
				video.setMediaController(new MediaController(MainActivity.this));
				video.requestFocus();
				video.start();
				playButton.setEnabled(false);
				captureButton.setEnabled(false);
				
				video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					
					public void onCompletion(MediaPlayer mp) {
						System.out.println("Video view complte");
						preview.removeAllViews();
						preview.addView(mPreview);
						playButton.setEnabled(true);
						captureButton.setEnabled(true);
					}
				});
				preview.addView(video);
//				MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, Uri.fromFile(new File("/sdcard/someexamplevideo.mp4")));
//				mediaPlayer.start();
			}
		});
    }
    
    private void stopRecording() {
		// stop recording and release camera
        final Button captureButton = (Button) findViewById(R.id.button_capture);
		mMediaRecorder.stop();  // stop the recording
		releaseMediaRecorder(); // release the MediaRecorder object
		mCamera.lock();         // take camera access back from MediaRecorder

		// inform the user that recording has stopped
		captureButton.setText("Grabar");
		isRecording = false;
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Log.v(getClass().getSimpleName(), "on save instance here!!!");
        outState.putString("mCameraFileName", mCameraFileName);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(getClass().getSimpleName(), "on resumen here!!!");
        
        try
        {
            //mCamera.setPreviewCallback(null);
            mCamera = getCameraInstance();
            //mCamera.setPreviewCallback(null);
            mPreview = new CameraPreview(this, mCamera);//set preview
            preview.addView(mPreview);
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
	
    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
    	releaseCamera();
    	int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
                try {
                    cam = Camera.open( camIdx );
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
         
        return cam;
    }
    
    private boolean prepareVideoRecorder(){
    	int MAX_TIME = 60;
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        mMediaRecorder.setOutputFile(mCameraFileName);
//        mMediaRecorder.setMaxDuration((int) MAX_TIME); 
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        
        
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//        mMediaRecorder.setVideoSize(480, 320); 
//        mMediaRecorder.setVideoFrameRate(15);
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file

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
        mCamera.stopPreview();
//        mCamera.setPreviewCallback(null);
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
        Log.v(getClass().getSimpleName(), "on pause calidoso here!!!");
        preview.removeAllViews();
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
        	mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    
    
    @Override
	protected void onRestart() {
		super.onRestart();
		Log.v(getClass().getSimpleName(), "on restart here!!!");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.v(getClass().getSimpleName(), "on start here!!!");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
		Log.v(getClass().getSimpleName(), "on stop here!!!");
	}

	private void sendEmail(){
    	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
        emailIntent.setType("video/mp4");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] 
        {"hernan.metaute@gmail.com"}); 
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
        "Test Subject"); 
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, 
        "go on read the emails"); 
        Log.v(getClass().getSimpleName(), "sPhotoUri=" + Uri.parse("file://"+ mCameraFileName));
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ mCameraFileName));
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
//        showToast("Sending email");
    }
	
	private void createStopTimer(){
		Timer timer = new Timer ();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopRecording();
			}
		}, 10*1000);
	}

	
	   
}

