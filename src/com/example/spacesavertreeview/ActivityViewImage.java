package com.example.spacesavertreeview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.imageannotation.clsAnnotationData;
import com.example.spacesavertreeview.imageannotation.clsCombineAnnotateImage;
import com.example.spacesavertreeview.imageannotation.clsInteractiveImageView;
import com.example.spacesavertreeview.imageannotation.clsShapeFactory.Shape;
import com.example.spacesavertreeview.imageannotation.clsZoomableImageView;
import com.example.spacesavertreeview.sharing.ActivityGetUserForGroups;
import com.example.spacesavertreeview.sharing.clsGetUserForGroupsArrayAdapter;
import com.google.gson.reflect.TypeToken;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.os.Build;

public class ActivityViewImage extends Activity implements clsCombineAnnotateImage.TaskCompletedInterface {

	public class clsSession {
		public File fileTreeNodesDir;
		public String strDescription;
		ArrayList<clsListViewState> objListViewStates = new ArrayList<clsListViewState>();
		public String strCombinedFileFullFilename;
	}

	public static class clsListViewState {
		String strArrowDescription;
	}

	public static final String FILE_FULLFILENAME = "com.example.spacesavertreeview.strFileFullFilename";

	public static final String LISTVIEWSTATES_GSON = "com.example.spacesavertreeview.listviewstates_gson";

	public static final String DESCRIPTION = "com.example.spacesavertreeview.strDescription";

	clsSession objSession = new clsSession();
	Context context;
	
	ActivityViewImage thisObject;

	clsArrayAdapter objArrayAdapter;
	
	private clsAnnotationData objAnnotationData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);
		this.context = this;
		thisObject = this;
		
		ListView objListView = (ListView) findViewById(R.id.activity_view_image_arrows);
		
		
		clsZoomableImageView objImageView = (clsZoomableImageView) findViewById(R.id.activity_view_image_image);
		//File fileImageFilename  = new File(objSession.strCombinedFileFullFilename);
		
//		Log.d("specViewer", String.valueOf(objImageView.getWidth())+"/"+String.valueOf(objImageView.getHeight()));
		//bitmap = BitmapFactory.decodeFile(fileImageFilename.getAbsolutePath());

		// The below code gets called when the layout is known including the size of the ImageView
		objImageView.post(new Runnable() {
			
			@Override
			public void run() {

				clsZoomableImageView objImageView = (clsZoomableImageView)findViewById(R.id.activity_view_image_image);
								
				clsCombineAnnotateImage objCombineAnnotateImage = new clsCombineAnnotateImage(context, thisObject);
				objCombineAnnotateImage.createAnnotatedImage(objSession.strCombinedFileFullFilename, objAnnotationData,
																objImageView.getWidth(), objImageView.getHeight());
			}
		});

		
		if (savedInstanceState == null) {
			Bundle objBundle = getIntent().getExtras();
			
			objAnnotationData = clsUtils.DeSerializeFromString(objBundle.getString(ActivityNoteStartup.ANNOTATION_DATA_GSON), 
																clsAnnotationData.class);
			
			objSession.strDescription = objBundle.getString(ActivityViewImage.DESCRIPTION);
			objSession.strCombinedFileFullFilename = objBundle.getString(ActivityViewImage.FILE_FULLFILENAME);
			
			String strListViewStatesGson = objBundle.getString(ActivityViewImage.LISTVIEWSTATES_GSON);
			if (!strListViewStatesGson.isEmpty()) {
				java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsListViewState>>(){}.getType();
				objSession.objListViewStates = clsUtils.DeSerializeFromString(strListViewStatesGson, collectionType);
			}
			objSession.fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(this));
			if (!objSession.fileTreeNodesDir.exists()) {
				objSession.fileTreeNodesDir.mkdirs();
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
			clsUtils.CustomLog("ActivityViewImage onCreate SaveFile");
		}

		// Hide text area displaying information about numbered arrows if there are no such shapes 
		// in the annotation to gain space for the actual image.
		boolean numberedArrowAvailable = false;
		for(clsAnnotationData.clsAnnotationItem item : objAnnotationData.items)
		{
			if(item.getType() == Shape.NUMBERED_ARROW)
			{
				numberedArrowAvailable = true;
				break;
			}
		}
		if(!numberedArrowAvailable)
		{
			
			// hide 
//			TextView objLabel = (TextView)findViewById(R.id.activity_view_arrow_label);
//			objLabel.setVisibility(View.GONE);
//			//ListView list = (ListView)findViewById(R.id.activity_view_image_arrows);
//			objListView.setVisibility(View.GONE);
//			
//			
			// hide layout which contains the texts for the numbered arrows
			LinearLayout objLayout = (LinearLayout)findViewById(R.id.linear_layout_arrow_info);
			objLayout.setVisibility(View.GONE);
			
			// adjust layout so that the TextView for the description is at the bottom of the screen
			//@+id/RelativeLayout1
			
			TextView objLabel = (TextView)findViewById(R.id.activity_view_description);
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) objLabel.getLayoutParams();
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, objLabel.getId());
			
			Log.d("JE", "-- GONE --");
		}
		
		UpdateScreen();

	}
	
	@Override
	public void loadTaskComplete(String combinedFile) {
		
		Bitmap bitmap = BitmapFactory.decodeFile(combinedFile);

		clsZoomableImageView objImageView = (clsZoomableImageView)findViewById(R.id.activity_view_image_image);
		objImageView.setImageBitmap(bitmap);

		TextView objTextView = (TextView)findViewById(R.id.activity_view_description);
		objTextView.setText(objSession.strDescription);
		
		if (objSession.objListViewStates.size() == 0) {
			TextView objTextViewLabel = (TextView)findViewById(R.id.activity_view_arrow_label);
			objTextViewLabel.setVisibility(View.INVISIBLE);
		}
	}
	
	private void UpdateScreen() {

		
		//Log.d("--Abs--", fileImageFilename.getAbsolutePath());
//		objImageView.setImageBitmap(bitmap);	
		
//		TextView objTextView = (TextView)findViewById(R.id.activity_view_description);
//		objTextView.setText(objSession.strDescription);
//		
//		if (objSession.objListViewStates.size() == 0) {
//			TextView objTextViewLabel = (TextView)findViewById(R.id.activity_view_arrow_label);
//			objTextViewLabel.setVisibility(View.INVISIBLE);
//		}
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
		objSession.fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(this));
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		SaveFile();
		super.onPause();
		clsUtils.CustomLog("ActivityViewImage onPause");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		SaveFile();
		super.onDestroy();
		clsUtils.CustomLog("ActivityViewImage onDestroy");
	}

	
	public class clsArrayAdapter extends ArrayAdapter<clsListViewState> {

		public clsArrayAdapter(Context context, int resource, ArrayList<clsListViewState> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			
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
