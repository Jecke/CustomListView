package com.example.spacesavertreeview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
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
				updateUrlTextView(url);
				pd.dismiss();
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
			}
		});
		// The backspace key does not get recognised by the emulator.
		// TODO JE Test backspace of web view on actual phone
		webView.setOnKeyListener(new View.OnKeyListener() 
		{
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) 
			{
				if(arg2.getAction() == KeyEvent.ACTION_DOWN)
				{
					switch(arg1)
					{
						// back key
						case KeyEvent.KEYCODE_BACK:
							if(webView.canGoBack())
							{
								webView.goBack();
								return true;
							}
					}
				}
				return false;
			}
		});
		
		Button urlSearch = (Button)findViewById(R.id.buttonWebSearch);
		urlSearch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Retrieve url from text view
				strUrl = urlTextView.getText().toString();
				
				loadWebPage(strUrl);
			}
		});

		urlTextView = (TextView)findViewById(R.id.editTextNoteName);
		urlTextView.setOnKeyListener(new View.OnKeyListener() 
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if(event.getAction() == KeyEvent.ACTION_DOWN &&
					keyCode == KeyEvent.KEYCODE_ENTER)
				{
					// Activate the search button (start to load page) if user presses
					// ENTER in the text field containing the URL.
					Button urlSearch = (Button)findViewById(R.id.buttonWebSearch);
					urlSearch.callOnClick();
					
					return true;
				}
				
				return false;
			}
		});
		
		Bundle objBundle = objIntent.getExtras();    		
		strUrl = objBundle.getString(ActivityNoteAddNew.WEB_VIEW_URL);
		strWebImage = objBundle.getString(ActivityNoteAddNew.WEB_VIEW_IMAGE);
		
		updateUrlTextView(strUrl);
		loadWebPage(strUrl);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_web_browser, menu);
		
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
        		objIntent.putExtra(ActivityNoteAddNew.WEB_VIEW_IMAGE, strWebImage);
        		
        		Bitmap bm;
    			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) 
    			{
    				// Note: The below code does not work for API's <KITKAT.
    				// getDrawingCache always returns null
            		webView.setDrawingCacheEnabled(true);
            		bm = Bitmap.createBitmap(webView.getDrawingCache());
            		webView.setDrawingCacheEnabled(false);
    			}
    			else
    			{
            		// ?? Issue ??
            		// We only capture the visible content of the web view here because big websites can cause
            		// an out-of-memory error. If there is a resolution for that then below call can use
            		// webView.getContentHeight instead of webView.getHeight to get all the content of the page.
            		bm = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Config.ARGB_8888);
            		Canvas canvas = new Canvas(bm);
            		webView.draw(canvas);
    			}
        		
        		// save bitmap to local file and return the URI to the caller
    			// image is saved in 80 percent quality to save some space
        		new clsResourceLoader().saveBitmapToFile(objContext, bm, strWebImage, 80);
            	
            	setResult(RESULT_OK, objIntent);
            	this.finish();

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
		strUrl = url;
	}
	
	// Load web page if provided url is not empty
	private void loadWebPage(String url)
	{
		if(strUrl != null && !strUrl.isEmpty())
		{
			if(!strUrl.startsWith("http"))
			{
				strUrl = "http://" + strUrl;
			}
			
			pd = ProgressDialog.show(objContext, 
					"Please wait", 
					"Loading ...", 
					true, true, null);

			webView.loadUrl(strUrl);
		}
	}
}
