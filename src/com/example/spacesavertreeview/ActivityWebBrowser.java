package com.example.spacesavertreeview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class ActivityWebBrowser extends Activity {

	private Intent objIntent;
	private Context objContext;
	
	private String strUrl;
	
	private WebView webView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_browser);
		
		objIntent = getIntent();
		objContext = this;
		
		webView = (WebView)findViewById(R.id.webView);
		
		Bundle objBundle = objIntent.getExtras();    		
		strUrl = objBundle.getString(ActivityNoteAddNew.WEB_VIEW_URL);

		
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_browser, menu);
		
        return super.onCreateOptionsMenu(menu);
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean success = false;
		
    	switch (item.getItemId()) 
    	{
        	case R.id.actionAccept:
        		return true;
        		
        	case R.id.actionCancel:
        		return true;
        		
            default:
                return super.onOptionsItemSelected(item);
    	}
	}
}
