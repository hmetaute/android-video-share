package com.audio.record;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mail.send.EmailValidator;
import com.mail.send.Mail;
import com.mail.send.SendEmailAsyncTask;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class MailActivity extends Activity {
	
	/**
	 * The default email to populate the email field with.
	 */
	public static final String FILENAME = "com.example.android.authenticatordemo.extra.FILENAME";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;

	// UI references.
	private EditText mEmailView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	private String mCameraFileName;
	private String mErrorMessage;
	Mail m;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_mail);

		// Set up the login form.
		mEmail = getIntent().getStringExtra(FILENAME);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);
		
		
		mCameraFileName = getIntent().getStringExtra("filename");
		

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						attemptSubmit();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_mail, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptSubmit() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		} else {
			EmailValidator validator = new EmailValidator();
			if(validator.validate(mEmail)){
				mEmailView.setError(getString(R.string.error_invalid_email));
				focusView = mEmailView;
				cancel = true;
			}
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			if(initMail())
				mAuthTask.execute((Void) null);
		}
	}
	
	private boolean initMail(){
		m = new Mail("debocaenbocaen@gmail.com", "Tvcamaras1986++");
		
		if (BuildConfig.DEBUG) Log.v(SendEmailAsyncTask.class.getName(), "SendEmailAsyncTask()");		
//		String[] toArr = {"debocaenbocaen@gmail.com"};
		String[] toArr = {"elieldavid.metaute@gmail.com"}; 
		m.setFrom("debocaenboca@gmail.com"); 
		m.setSubject("Video subido"); 
		m.setBody("Hola, enviar el video al siguiente correo:" + mEmail);

		try{
			m.addAttachment(mCameraFileName);
		}catch(Exception e){			
			showToast("An errror occur while trying to attach the video");
			return false;
		}
		//new SendEmailAsyncTask(m).execute();
		return true;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			Log.i(SendEmailAsyncTask.class.getName(), "doInBackground()");
			try {
				if(m.send()) { 
					Log.i(SendEmailAsyncTask.class.getName(), "Email was sent successfully.");
				} else { 
					Log.i(SendEmailAsyncTask.class.getName(), "Email was not sent.");
					mErrorMessage = m.getTo() + "Email was not sent.";
					return false;
				}				
			} catch (AuthenticationFailedException e) {
				Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
				mErrorMessage = "Bad account details";
				e.printStackTrace();
				return false;
			} catch (MessagingException e) {
				Log.e(SendEmailAsyncTask.class.getName(), m.getTo() + "failed");
				mErrorMessage = m.getTo() + "failed";
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			m = null;
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
			} else {
				mEmailView.setError(mErrorMessage);
				mEmailView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
	private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
