package com.example.spacesavertreeview.export;

import com.example.spacesavertreeview.R;
import com.facebook.*;
import com.facebook.model.*;

import android.app.DownloadManager.Request;
import android.content.Context;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class clsExportToFacebook extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        					Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.activity_facebook_export, container, false);

	    return view;
	}
	
	public clsExportToFacebook()
	{
	}

	public void export()
	{
		//Constant.FACEBOO
		// Login to facebook
		//RequestAsyncTask mAsyncRunner = 
	}
}
