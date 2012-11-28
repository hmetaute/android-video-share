package com.audio.record;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.client2.session.Session.AccessType;

import com.audio.record.UploadFile;

public class MainActivity extends Activity {

	private static final String TAG = "Record";

	private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    
    /* upload to dropbox variables */
    
    final static private String APP_KEY = "vel5sdq4n7pj1pm";
    final static private String APP_SECRET = "mq75q25xezhtdr3";
    
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    
    DropboxAPI<AndroidAuthSession> mApi;

    private boolean mLoggedIn;
    private String mCameraFileName = "/sdcard/someexamplevideo.mp4";
    
    private final String VIDEO_DIR = "/Videos/";
    
    private Button mSubmit;
    private Button mUpload;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videorecorderlayout);
        
        final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        final Button captureButton = (Button) findViewById(R.id.button_capture);
        final Button playButton = (Button) findViewById(R.id.button_play);
        final Button mailButton =  (Button) findViewById(R.id.button_mail);
        
        mailButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				sendEmail();				
			}
		});
        
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
				video.setVideoURI(Uri.fromFile(new File(mCameraFileName)));
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

        /** Authentication logic */

        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        checkAppKeySetup();

        mSubmit = (Button)findViewById(R.id.auth_button);

        mSubmit.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		// This logs you out if you're logged in, or vice versa
        		if (mLoggedIn) {
        			logOut();
        		} else {
        			// Start the remote authentication
        			mApi.getSession().startAuthentication(MainActivity.this);
        		}
        	}
        });
        
        /**Upload file logic**/
        
        mUpload = (Button)findViewById(R.id.upload_button);
        
        mUpload.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				uploadFile();
			}
		});
        // Display the proper UI state if logged in or not
        setLoggedIn(mApi.getSession().isLinked());
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("mCameraFileName", mCameraFileName);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }
    }
	
    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
    	releaseCamera();
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance (the front cammera)
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
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
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//        mMediaRecorder.setMaxDuration((int) MAX_TIME); 
//        mMediaRecorder.setVideoSize(320, 240); 
//        mMediaRecorder.setVideoFrameRate(15); 
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
//        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(mCameraFileName);

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
    
    /** Upload File and Authentication aditional methods**/
    
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    
    private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }
    
    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    
    private void setLoggedIn(boolean loggedIn) {
    	mLoggedIn = loggedIn;
    	if (loggedIn) {
    		mSubmit.setText("Unlink from Dropbox");            
    	} else {
    		mSubmit.setText("Link with Dropbox");            
    	}
    }
    
    private void uploadFile(){
    	File file = new File(mCameraFileName);
	    
		if (file != null && file.exists()) {
			UploadFile upload = new UploadFile(this, mApi, VIDEO_DIR, file);
			upload.execute();    				
		} else {
			showToast("Video not foud");
		}
    }
    
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
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
        showToast("Sending email");
    }
    
    
}