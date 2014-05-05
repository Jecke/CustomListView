package com.example.spacesavertreeview;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.spacesavertreeview.sharing.clsMessaging;
import com.example.spacesavertreeview.sharing.clsMessaging.clsLoginUserCmd;
import com.example.spacesavertreeview.sharing.clsMessaging.clsLoginUserResponse;
import com.google.gson.Gson;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ActivityRegister extends Activity {

    public static final String USERNAME = "com.example.spacesavertreeview.USERNAME";
    public static final String USERUUID = "com.example.spacesavertreeview.USERUUID";
    public static final String USER_EXISTS = "com.example.spacesavertreeview.USER_EXISTS";
	public static final String WEBSERVER_URL = "com.example.spacesavertreeview.WEBSERVER_URL";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mUsername;
    private String mPassword;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;

   
    // Messaging
    clsMessaging objMessaging = new clsMessaging();
    static String strWebserverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        
        Bundle objBundle = getIntent().getExtras();    		
        strWebserverUrl  = objBundle.getString(ActivityRegister.WEBSERVER_URL);

        // Set up the login form.
        mUsername = getIntent().getStringExtra(USERNAME);
        mUsernameView = (EditText) findViewById(R.id.email);
        if (!(mUsername == null || mUsername.isEmpty())) {
        	mUsernameView.setText(mUsername);
        } else {
        	mUsernameView.setText(getString(R.string.default_username));
        }
       

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText("password");
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString().trim();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!clsUtils.IsUserNameValid(mUsername)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(this);
            mAuthTask.execute((Void) null);
        }
    }

  

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class clsUserLoginResult {
    	static final int ERROR_NONE = 0;
    	static final int ERROR_ADD_USER = 1;
    	static final int ERROR_DBASE = 2;
    	static final int ERROR_REMOVE_USER = 3;
    	static final int ERROR_LOGIN_USER = 4;
    	static final int ERROR_GET_USERS = 5;
    	static final int ERROR_NONE_USER_EXISTS = 6;
    	public int intErrorCode = ERROR_NONE;
    	public String strErrorMessage = "";
    	public String strUserUuid = "";
    }
    
    public class UserLoginTask extends AsyncTask<Void, Void, clsUserLoginResult> {
    	
        ProgressDialog objProgressDialog;
			
        public UserLoginTask (Activity objActivity) {
        	objProgressDialog = new ProgressDialog(objActivity);
        }
        @Override
        protected void onPreExecute() {
        // TODO Auto-generated method stub
        	super.onPreExecute();
        	objProgressDialog.setMessage("Processing..., please wait.");
        	objProgressDialog.show();
        }

		@Override
        protected clsUserLoginResult doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
        	clsUserLoginResult objResult  = new clsUserLoginResult();
        	
        	// Action and get data from server
        	clsLoginUserCmd objLoginUserCmd = objMessaging.new clsLoginUserCmd();
        	objLoginUserCmd.setStrUsername(mUsernameView.getText().toString());
        	objLoginUserCmd.setStrPassword(mPasswordView.getText().toString());
        	objLoginUserCmd.setIntSeqNum(0);
    		Gson gson = new Gson();
    		String strJsonCommand = gson.toJson(objLoginUserCmd);
        	InputStream stream = null;
        	JSONObject objJsonResult;
            try {
            	URL urlFeed = new URL(strWebserverUrl + getResources().getString(R.string.url_register_user));
                stream = clsMessaging.downloadUrl(urlFeed, strJsonCommand);
                objJsonResult = clsMessaging.updateLocalFeedData(stream);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.wtf("myCustom", "JSON exception", e);
				objResult.intErrorCode=clsUserLoginResult.ERROR_LOGIN_USER;
				objResult.strErrorMessage = "JSON exception. " + e.getMessage();
	            return objResult;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.wtf("myCustom", "IO exception", e);
				objResult.intErrorCode=clsUserLoginResult.ERROR_LOGIN_USER;
				objResult.strErrorMessage = "IO exception. " + e.getMessage();
	            return objResult;
			} finally {
                if (stream != null) {
                    try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.wtf("myCustom", "IO exception", e);
						objResult.intErrorCode=clsUserLoginResult.ERROR_LOGIN_USER;
						objResult.strErrorMessage = "IO exception. " + e.getMessage();
			            return objResult;
					}
                }
            }
        	
        	// Analize data from server	
			clsLoginUserResponse objLoginUserResponse = gson.fromJson(objJsonResult.toString(),clsLoginUserResponse.class);
			if (objLoginUserResponse.getIntErrorNum() == clsUserLoginResult.ERROR_NONE) {
				objResult.intErrorCode=clsUserLoginResult.ERROR_NONE;
				objResult.strUserUuid = objLoginUserResponse.getStrUserUuid();
			} else if (objLoginUserResponse.getIntErrorNum() == clsUserLoginResult.ERROR_NONE_USER_EXISTS) {
				objResult.intErrorCode=clsUserLoginResult.ERROR_NONE_USER_EXISTS;
				objResult.strUserUuid = objLoginUserResponse.getStrUserUuid();
			} else {
				objResult.intErrorCode=clsUserLoginResult.ERROR_LOGIN_USER;
				objResult.strErrorMessage = objLoginUserResponse.getStrErrMessage();
			}
            return objResult;
        }

        @Override
        protected void onPostExecute(final clsUserLoginResult success) {
            mAuthTask = null;
            if (objProgressDialog.isShowing()) {
            	objProgressDialog.dismiss();
            }

            if ((success.intErrorCode == clsUserLoginResult.ERROR_NONE) || 
            		(success.intErrorCode == clsUserLoginResult.ERROR_NONE_USER_EXISTS)) {
            	Intent objIntent = getIntent();
				objIntent.putExtra(USERNAME, mUsernameView.getText().toString());
				objIntent.putExtra(USERUUID, success.strUserUuid);;
				if (success.intErrorCode == clsUserLoginResult.ERROR_NONE_USER_EXISTS ) {
					objIntent.putExtra(USER_EXISTS, true);
				} else {
					objIntent.putExtra(USER_EXISTS, false);
				}
				setResult(RESULT_OK, objIntent);    	
                finish();
                return;
            } else {
                mPasswordView.setError(success.strErrorMessage);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            if (objProgressDialog.isShowing()) {
            	objProgressDialog.dismiss();
            }
        }
    }
}
