package com.example.spacesavertreeview.sharing;

import java.util.ArrayList;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsUtils;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;

public class ActivityAllMembers extends ListActivity {

	public static final String ACTION = "com.example.spacesavertreeview.sharing.action";
	public static final String ACTION_CHOOSE_MEMBER = "com.example.spacesavertreeview.sharing.action_choose_member";
	public static final String ACTION_CHOOSE_MEMBER_RESP_GSON = "com.example.spacesavertreeview.sharing.action_choose_member";

	
    public class clsAllMembersListViewState 
    {
    	clsGroupMembers.clsUser objUser;
    	boolean boolIsChecked = false;
    }
    
    clsAllMembersArrayAdapter objAllMembersArrayAdapter;
    ArrayList<clsAllMembersListViewState> objListViewStates =  new ArrayList<clsAllMembersListViewState>();
    clsGroupMembers objGroupMembers = new clsGroupMembers(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_members);
		
		objGroupMembers.LoadFile();
		for (clsGroupMembers.clsUser objUser: objGroupMembers.objMembersRepository.objUsers) {
			clsAllMembersListViewState objListViewState = new clsAllMembersListViewState();
			objListViewState.objUser = objUser;
			objListViewStates.add(objListViewState);
		}
		objAllMembersArrayAdapter = new clsAllMembersArrayAdapter(this, R.layout.all_members_list_item, objListViewStates);
		setListAdapter(objAllMembersArrayAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_all_members, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
		
		Intent objIntent = getIntent();
		objIntent.putExtra(ActivityAllMembers.ACTION_CHOOSE_MEMBER_RESP_GSON, clsUtils.SerializeToString(objListViewStates));
		setResult(RESULT_OK, objIntent);    	
    	super.onBackPressed();
	}

}
