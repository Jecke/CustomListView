package com.example.spacesavertreeview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityWebBrowser extends Activity {

	private Intent objIntent;
	private Context objContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new);
		
		objIntent = getIntent();
		objContext = this;
	}
}
