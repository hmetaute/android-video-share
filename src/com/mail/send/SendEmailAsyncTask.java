package com.mail.send;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.audio.record.BuildConfig;

public class SendEmailAsyncTask extends AsyncTask <Void, Void, Boolean> {
	Mail m;

	public SendEmailAsyncTask(Mail mail) {
		 m = mail;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Log.i(SendEmailAsyncTask.class.getName(), "doInBackground()");
		try {
			if(m.send()) { 
//				Toast.makeText(context, "Email was sent successfully.", Toast.LENGTH_LONG).show();
				Log.i(SendEmailAsyncTask.class.getName(), "Email was sent successfully.");
			} else { 
//				Toast.makeText(context, "Email was not sent.", Toast.LENGTH_LONG).show();
				Log.i(SendEmailAsyncTask.class.getName(), "Email was not sent.");
				return false;
			}
			return true;
		} catch (AuthenticationFailedException e) {
			Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
			e.printStackTrace();
			return false;
		} catch (MessagingException e) {
			Log.e(SendEmailAsyncTask.class.getName(), m.getTo() + "failed");
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

