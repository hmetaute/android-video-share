package com.audio.record;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

public class AudioRecorder extends Activity {

	MediaRecorder mediarecorder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiorecorderlayout);		
		Button recordbutton = (Button) findViewById(R.id.record);
		Button stopbutton = (Button) findViewById(R.id.stop);
		stopbutton.setVisibility(View.GONE);
		//Just show record button
		recordbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				record("/sdcard/someexamplevideo.mp4");
			}
		});
		
		stopbutton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				stopRecord();				
			}
		});

	}

	private void record(String filePath) {
		try {
						
			File mediafile = new File(filePath);
			if (mediafile.exists()) {
				mediafile.delete();
			}
			mediafile = null;
			// record button goes away
			Button recordButton = (Button) findViewById(R.id.record);
			recordButton.setVisibility(View.GONE);
			// stop button shows up
			Button stopbutton = (Button) findViewById(R.id.stop);
			stopbutton.setVisibility(View.VISIBLE);

			
			mediarecorder = new MediaRecorder();
			
//			mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
//			mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
			mediarecorder.setOutputFile("/sdcard/someexamplevideo.mp4");
			
			// provide a surface to show the preview in. in this case a VideoView is used
			VideoView videoview = (VideoView) findViewById(R.id.videosurface);
			SurfaceHolder holder = videoview.getHolder();
			mediarecorder.setPreviewDisplay(holder.getSurface());
			mediarecorder.setVideoSize(480,800);
			mediarecorder.setVideoFrameRate(5);
			// prepare
			mediarecorder.prepare();
			final Context context = getApplicationContext();
			int duration = Toast.LENGTH_LONG;
			CharSequence text2 = "Recording...";
			Toast toast = Toast.makeText(context, text2, duration);
			toast.show();
			// start recording
			mediarecorder.start();			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopRecord() {
		try{
		final Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		CharSequence text2 = "Stopping..";
		Toast toast = Toast.makeText(context, text2, duration);
		toast.show();
		// stop media recorder
		mediarecorder.stop();
		// reset media recorder
		mediarecorder.reset();
		// record button shows up
		ImageButton button = (ImageButton) findViewById(R.id.record);
		button.setVisibility(View.VISIBLE);
		// stop button goes away
		ImageButton stopbutton = (ImageButton) findViewById(R.id.stop);
		stopbutton.setVisibility(View.GONE);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}