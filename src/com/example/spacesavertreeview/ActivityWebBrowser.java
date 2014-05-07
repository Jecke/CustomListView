package com.example.spacesavertreeview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityWebBrowser extends Activity {

	private Intent objIntent;
	private Context objContext;
	
	private String strUrl;
	private String strWebImage;
	private ProgressDialog pd;
	
	private WebView webView;
	private TextView urlTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_browser);
		
		objIntent = getIntent();
		objContext = this;
		
		webView = (WebView)findViewById(R.id.webView);
		webView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onPageFinished(WebView view, String url)
			{
				pd.dismiss();
			}
		});
		
		urlTextView = (TextView)findViewById(R.id.editTextNoteName);
		
		Button urlSearch = (Button)findViewById(R.id.buttonWebSearch);
		urlSearch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Retrieve url from text view
				strUrl = urlTextView.getText().toString();
				
				loadWebPage(strUrl);
			}
		});
		
		Bundle objBundle = objIntent.getExtras();    		
		strUrl = objBundle.getString(ActivityNoteAddNew.WEB_VIEW_URL);
		strWebImage = objBundle.getString(ActivityNoteAddNew.WEB_VIEW_IMAGE);
		
		loadWebPage(strUrl);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_browser, menu);
		
        return super.onCreateOptionsMenu(menu);
    }
    
    // Action bar menu
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) 
    	{
    		// User is happy with the Web Page. Create a temporary snapshot of the page
    		// and return the URI of the picture and the URL of the page to the caller.
        	case R.id.actionAccept:
        		objIntent.putExtra(ActivityNoteAddNew.WEB_VIEW_URL, strUrl);
        		
        		// TODO remove
        		//Log.d("webheight", String.valueOf(webView.getHeight())+"/"+String.valueOf(webView.getContentHeight()));
        		//Log.d("webwidth", String.valueOf(webView.getWidth()));
        		
        		Bitmap bm = Bitmap.createBitmap(webView.getWidth(), webView.getContentHeight(), Config.ARGB_8888);
        		Canvas canvas = new Canvas(bm);
        		webView.draw(canvas);
        		
        		return true;
        		
        	case R.id.actionCancel:
            	setResult(RESULT_CANCELED, objIntent);
            	this.finish();
                return true;
        		
            default:
                return super.onOptionsItemSelected(item);
    	}
	}
	
	// Update text view holding the Url
	private void updateUrlTextView(String url)
	{
		urlTextView.setText(url);
	}
	
	// Load web page if provided url is not empty
	private void loadWebPage(String url)
	{
		if(!strUrl.isEmpty())
		{
			pd = ProgressDialog.show(this, 
					"Please wait", 
					"Loading ...", 
					true, true, null);
			
			if(!strUrl.startsWith("http"))
			{
				strUrl = "http://" + strUrl;
			}
			
			updateUrlTextView(strUrl);
			
			webView.loadUrl(strUrl);
		}
	}
}
