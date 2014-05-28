package com.treeapps.treenotes.export;

import java.util.Arrays;

import com.facebook.*;
import com.facebook.model.*;
import com.treeapps.treenotes.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

// The Activity hosting the Facebook functionality. Note that only
// one Fragment is used here which represents the unauthorised as well
// authorised user interface. 
// The purpose of that Activity is to export the information of a TreeNotes
// note to Facebook without any other user intervention than login/logout.
public class ActivityFacebookExport extends FragmentActivity {

	private Context objContext;
	private Activity objActivity;
	
	private clsExportToFacebook mainFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  
	  if(savedInstanceState == null)
	  {
		  mainFragment = new clsExportToFacebook();
		  getSupportFragmentManager()
		  .beginTransaction()
		  .add(android.R.id.content, mainFragment)
		  .commit();
	  }
	  else
	  {
		  mainFragment = (clsExportToFacebook)getSupportFragmentManager()
				  			.findFragmentById(android.R.id.content);
	  }
	  
	  objContext  = this;
	  objActivity = this;
	  
	  
	  // start Facebook Login
	  Session.openActiveSession(this, true, new Session.StatusCallback() {

		  // callback when session changes state
		  @Override
		  public void call(Session session, SessionState state, Exception exception) {
			  if (session.isOpened()) {

				  // make request to the /me API
				  Request.newMeRequest(session, new Request.GraphUserCallback() {

					  // callback after Graph API response with user object
					  @Override
					  public void onCompleted(GraphUser user, Response response) {
						  if (user != null) {
							  // Display user name in title of Activity
							  setTitle(getResources().getString(R.string.title_activity_facebook_export) + 
									  " (" + user.getName() + ")");
						  }
					  }
				  }).executeAsync();

				  // Start export if the necessary permissions are granted, otherwise request permissions first.
				  // publish_actions is needed to upload content to FB
				  // user_photos is used to request information about the photo albums
				  if(session.isPermissionGranted("publish_actions") &&
						  session.isPermissionGranted("user_photos"))
				  {
					  mainFragment.export(objActivity);//, exportData);
				  }
				  else
				  {
					  Session.NewPermissionsRequest newPermissionsRequest = new Session
							  .NewPermissionsRequest((Activity) objContext, Arrays.asList("publish_actions", "user_photos"));
					  session.requestNewPublishPermissions(newPermissionsRequest);
				  }
			  }
			  else
			  {
				  setTitle(getResources().getString(R.string.title_activity_facebook_export));
			  }
		  }
	  });
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);

	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
}
