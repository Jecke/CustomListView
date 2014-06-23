package com.treeapps.treenotes;

import java.util.ArrayList;

import com.google.gson.reflect.TypeToken;
import com.treeapps.treenotes.imageannotation.clsAnnotationData;
import com.treeapps.treenotes.imageannotation.clsZoomableImageView;
import com.treeapps.treenotes.imageannotation.clsShapeFactory.Shape;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;

public class ActivityViewImage extends Activity {

	public class clsSession {
		public String strDescription;
		ArrayList<clsListViewState> objListViewStates = new ArrayList<clsListViewState>();
		public String strWebPageURL;
		public String strTreeNodeUuid;
		public clsAnnotationData objAnnotationData;
	}

	public static class clsListViewState {
		String strArrowDescription;
	}

	public static final String FILE_FULLFILENAME = "com.treeapps.treenotes.strFileFullFilename";
	public static final String URL               = "com.treeapps.treenotes.strWebPageURL";

	public static final String LISTVIEWSTATES_GSON = "com.treeapps.treenotes.listviewstates_gson";

	public static final String DESCRIPTION = "com.treeapps.treenotes.strDescription";

	clsSession objSession = new clsSession();
	Context context;
	
	ActivityViewImage thisObject;

	clsArrayAdapter objArrayAdapter;
	
	private Bitmap bitmap = null;
	private clsZoomableImageView objImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);
		this.context = this;
		thisObject = this;
		
		ListView objListView = (ListView) findViewById(R.id.activity_view_image_arrows);
		
		objImageView = (clsZoomableImageView) findViewById(R.id.activity_view_image_image);

		// The below code gets called when the layout is known including the size of the ImageView
		objImageView.post(new Runnable() {
			
			@Override
			public void run() {

				clsZoomableImageView objImageView = (clsZoomableImageView)findViewById(R.id.activity_view_image_image);
				if ((objSession.objAnnotationData == null) && (objSession.strWebPageURL.isEmpty())) {
					// Display a simple single local file
					bitmap = BitmapFactory.decodeFile(clsUtils.GetFullImageFileName(context, objSession.strTreeNodeUuid));
				} else {
					// Display annotated image or webpage
					bitmap = BitmapFactory.decodeFile(clsUtils.GetAnnotatedImageFileName(context, objSession.strTreeNodeUuid));
					objImageView.setWebPageURL(objSession.strWebPageURL);
				}

				objImageView.setImageBitmap(bitmap);

				TextView objTextView = (TextView)findViewById(R.id.activity_view_description);
				objTextView.setText(objSession.strDescription);
				
				if (objSession.objListViewStates.size() == 0) {
					TextView objTextViewLabel = (TextView)findViewById(R.id.activity_view_arrow_label);
					objTextViewLabel.setVisibility(View.INVISIBLE);
				}
				
				UpdateScreen();
			}
		});

		
		if (savedInstanceState == null) {
			Bundle objBundle = getIntent().getExtras();
			
			objSession.objAnnotationData = clsUtils.DeSerializeFromString(objBundle.getString(ActivityNoteStartup.ANNOTATION_DATA_GSON), 
																			clsAnnotationData.class);
			
			objSession.strDescription = objBundle.getString(ActivityViewImage.DESCRIPTION);
			objSession.strWebPageURL = objBundle.getString(ActivityViewImage.URL);
			objSession.strTreeNodeUuid = objBundle.getString(ActivityNoteStartup.TREENODE_UID);
			
			String strListViewStatesGson = objBundle.getString(ActivityViewImage.LISTVIEWSTATES_GSON);
			if (!strListViewStatesGson.isEmpty()) {
				java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsListViewState>>(){}.getType();
				objSession.objListViewStates = clsUtils.DeSerializeFromString(strListViewStatesGson, collectionType);
			}

			// Show the Up button in the action bar.
			setupActionBar();

			// Setup listView
			objArrayAdapter = new clsArrayAdapter(this, R.layout.arrows_list_item,
					objSession.objListViewStates);
			objListView.setAdapter(objArrayAdapter);

			SaveFile();
			clsUtils.CustomLog("ActivityViewImage onCreate SaveFile");
			
		} else {
			LoadFile();
			clsUtils.CustomLog("ActivityViewImage onCreate LoadFile");
		}

		// Hide text area displaying information about numbered arrows if there are no such shapes 
		// in the annotation to gain space for the actual image.
		boolean numberedArrowAvailable = false;
		if (objSession.objAnnotationData!= null) {
			for(clsAnnotationData.clsAnnotationItem item : objSession.objAnnotationData.items)
			{
				if(item.getType() == Shape.NUMBERED_ARROW)
				{
					numberedArrowAvailable = true;
					break;
				}
			}
		}
		
		if(!numberedArrowAvailable)
		{
			// hide layout which contains the texts for the numbered arrows
			LinearLayout objLayout = (LinearLayout)findViewById(R.id.linear_layout_arrow_info);
			objLayout.setVisibility(View.GONE);
			
			// adjust layout so that the TextView for the description is at the bottom of the screen
			//@+id/RelativeLayout1
			
			TextView objLabel = (TextView)findViewById(R.id.activity_view_description);
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) objLabel.getLayoutParams();
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, objLabel.getId());
		}
	}
	
	private void UpdateScreen() {

		if(bitmap != null)
		{
			objImageView.setImageBitmap(bitmap);	

			TextView objTextView = (TextView)findViewById(R.id.activity_view_description);
			objTextView.setText(objSession.strDescription);

			if (objSession.objListViewStates.size() == 0) {
				TextView objTextViewLabel = (TextView)findViewById(R.id.activity_view_arrow_label);
				objTextViewLabel.setVisibility(View.INVISIBLE);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_view_image, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void SaveFile() {
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityViewImage", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		String strSession = clsUtils.SerializeToString(objSession);
		editor.putString("objSession", strSession);
		editor.commit();
		sharedPref = null;
	}

	private void LoadFile() {
		this.context = this;
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityViewImage", Context.MODE_PRIVATE);
		String strSession = sharedPref.getString("objSession", "");
		objSession = clsUtils.DeSerializeFromString(strSession, objSession.getClass());

		if (objArrayAdapter == null) {
			objArrayAdapter = new clsArrayAdapter(this, R.layout.arrows_list_item, objSession.objListViewStates);
			ListView objListView = (ListView) findViewById(R.id.activity_view_image_arrows);
			objListView.setAdapter(objArrayAdapter);
		} else {
			objArrayAdapter.clear();
			objArrayAdapter.addAll(objSession.objListViewStates);
		}
	}

	@Override
	protected void onStart() {
		LoadFile();
		super.onStart();
		clsUtils.CustomLog("ActivityViewImage onStart");
	}

	@Override
	protected void onRestart() {
		LoadFile();
		super.onRestart();
		clsUtils.CustomLog("ActivityViewImage onRestart");
	}

	@Override
	protected void onResume() {
		LoadFile();
		UpdateScreen();
		super.onResume();
		clsUtils.CustomLog("ActivityViewImage onResume");
	}

	@Override
	protected void onStop() {
		SaveFile();
		super.onStop();
		clsUtils.CustomLog("ActivityViewImage onStop");
	}

	@Override
	protected void onPause() {
		SaveFile();
		super.onPause();
		clsUtils.CustomLog("ActivityViewImage onPause");
	}

	@Override
	protected void onDestroy() {
		SaveFile();
		super.onDestroy();
		clsUtils.CustomLog("ActivityViewImage onDestroy");
	}
	
	public class clsArrayAdapter extends ArrayAdapter<clsListViewState> {

		public clsArrayAdapter(Context context, int resource, ArrayList<clsListViewState> objects) {
			super(context, resource, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			 LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			 View rowView = inflater.inflate(R.layout.arrows_list_item, parent, false);
			    
			 clsListViewState objListViewState = objSession.objListViewStates.get(position);

			 TextView objTextView = (TextView) rowView.findViewById(R.id.arrows_description);
			 objTextView.setText(objListViewState.strArrowDescription);
			 CheckBox objCheckBox = (CheckBox) rowView.findViewById(R.id.arrows_checkbox);
			 objCheckBox.setVisibility(View.GONE);    
			 return rowView;
		}

	}

}
