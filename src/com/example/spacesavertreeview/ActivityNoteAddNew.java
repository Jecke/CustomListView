package com.example.spacesavertreeview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import com.example.spacesavertreeview.imageannotation.ActivityEditAnnotationImage;
import com.example.spacesavertreeview.imageannotation.ActivityEditAnnotationText;
import com.example.spacesavertreeview.imageannotation.clsAnnotationData;
import com.example.spacesavertreeview.imageannotation.clsShapeFactory.Shape;
import com.example.spacesavertreeview.sharing.clsAllMembersArrayAdapter;
import com.example.spacesavertreeview.sharing.clsGroupMembers;
import com.example.spacesavertreeview.sharing.ActivityAllMembers.clsAllMembersListViewState;
import com.google.gson.reflect.TypeToken;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ImageView;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.view.View.OnClickListener;

public class ActivityNoteAddNew extends Activity implements clsResourceLoader.TaskCompletedInterface {
	
	public static final int GET_IMAGE_RESOURCE = 1;
	public static final int GET_VIDEO_RESOURCE = 2;
	public static final int EDIT_ANNOTATION_IMAGE = 3;
	public static final int EDIT_ANNOTATION_TEXT = 4;


	
	 public class clsArrowsListViewState 
	    {
	    	int intArrowNum;
	    	String strArrowDescription;
	    	boolean boolIsChecked = false;
	    }
	
	 
	clsActivityNoteAddNewArrayAdapter objArrayAdapter;
	ArrayList<clsArrowsListViewState> objListViewStates =  new ArrayList<clsArrowsListViewState>();
	boolean boolUseAnnotatedImage = false;
	    
	private Intent objIntent;
	private Activity objContext;

	
	// Annotation data
	public clsAnnotationData objAnnotationData;
	private boolean boolIsDirty = false;
	
	// Selected radio button
	int    resourceId;
	String resourcePath;
	String strDescription = "";
	boolean boolIsReadOnly = false;

	// Remember UUID of last item that was edited
	String strTreeNodeUuid = "";
	
	// Displays thumbnail of image or video
	ImageView objEditThumbnail;
	// bitmap containing thumbnail of image or video 
	Bitmap imageBitmap;

	Intent cameraIntent;
	
	public static ListView objListView;
	private ProgressDialog pd;
	private clsResourceLoader objResourceLoader;
	
	private String imageDirectory;
	private File strImageFilename, strImageFilenameBackup;
	private File strThumbnailFilename, strThumbnailFilenameBackup;
	private File strFullFilename, strFullFilenameBackup;
	
	// interface from  implements clsResourceLoader.TaskCompletedInterface 
	public void loadTaskComplete(Bitmap bm, Bitmap bmFull, Uri uri)
	{
		pd.dismiss();
		
		if(bm == null)
		{
			clsUtils.showErrorDialog(this, R.string.dlg_error_loading_resource, true);
		}
		else
		{
			ImageView preview = (ImageView)findViewById(R.id.imagePreview);
			preview.setImageBitmap(bm);			

			// Create a thumbnail for the tree and annotation
			SaveBitmapToFile(bm, strImageFilename.getAbsolutePath());
			SaveBitmapToFile(bm, strThumbnailFilename.getAbsolutePath());

			resourcePath = uri.toString();

			if(bmFull != null)// && resourcePath.contains("http"))
			{
				SaveBitmapToFile(bmFull, strFullFilename.getAbsolutePath(), 80);
			}
		}
		RadioGroup rg = (RadioGroup)findViewById(R.id.radioItemType);
		rg.setOnCheckedChangeListener(new RadioGroupOnCheckedChangeListener());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new);
		
		objIntent = getIntent();
		objContext = this;
		
		imageDirectory = new File(clsUtils.GetTreeNotesDirectoryName(this)).getAbsolutePath() + "/";
		
		Bundle objBundle = objIntent.getExtras();    		
		strDescription       = objBundle.getString(ActivityNoteStartup.DESCRIPTION);
		resourceId      = objBundle.getInt(ActivityNoteStartup.RESOURCE_ID);
		resourcePath    = objBundle.getString(ActivityNoteStartup.RESOURCE_PATH);
		strTreeNodeUuid = objBundle.getString(ActivityNoteStartup.TREENODE_UID);
		boolIsReadOnly = objBundle.getBoolean(ActivityNoteStartup.READONLY);
		String strAnnotationData = objBundle.getString(ActivityNoteStartup.ANNOTATION_DATA_GSON);
		objAnnotationData = clsUtils.DeSerializeFromString(strAnnotationData, clsAnnotationData.class);
		String strTreeNodeOwnername = objBundle.getString(ActivityNoteStartup.TREENODE_OWNERNAME);
		boolUseAnnotatedImage = objBundle.getBoolean(ActivityNoteStartup.USE_ANNOTATED_IMAGE);
		boolIsDirty = objBundle.getBoolean(ActivityNoteStartup.ISDIRTY);
		
		strImageFilename           = new File(imageDirectory + strTreeNodeUuid + ".jpg");
		strImageFilenameBackup     = new File(strImageFilename + ".backup");
		strThumbnailFilename       = new File(imageDirectory + strTreeNodeUuid + "_annotate.jpg");
		strThumbnailFilenameBackup = new File(strThumbnailFilename + ".backup");
		strFullFilename            = new File(imageDirectory + strTreeNodeUuid + "_full.jpg");
		strFullFilenameBackup      = new File(strFullFilename + ".backup");
		
		if(resourceId == clsTreeview.IMAGE_RESOURCE || strTreeNodeUuid.equals("temp_uuid"))
		{
			
			// make copies of important files which could be altered
			if (strImageFilename.exists()) {
				clsUtils.CopyFile(strImageFilename, strImageFilenameBackup,     false);
			}
			if (strThumbnailFilename.exists()) {
				clsUtils.CopyFile(strThumbnailFilename, strThumbnailFilenameBackup, false);
			}
			
			if (strFullFilename.exists()) {
				clsUtils.CopyFile(strFullFilename,      strFullFilenameBackup,      false);
			}
		}

		RadioGroup rg = (RadioGroup)findViewById(R.id.radioItemType);
		
		// set description field
		EditText objEditText = (EditText)findViewById(R.id.editTextNoteName);
		if (boolIsReadOnly) {
			objEditText.setText(strDescription + " - Comment by " + strTreeNodeOwnername);
		} else {
			objEditText.setText(strDescription);
		}

		objEditText.setEnabled(!boolIsReadOnly);
				
		// remove thumbnail
		objEditThumbnail = (ImageView)findViewById(R.id.imagePreview);
		objEditThumbnail.setImageBitmap(null);	
		
		// Annotation display
		TextView lblImageAnotationStatus = (TextView)findViewById(R.id.lblImageAnotationStatus);
		lblImageAnotationStatus.setVisibility(View.INVISIBLE);
		TextView lblNumberedArrows = (TextView)findViewById(R.id.lblNumberedArrows);
		lblNumberedArrows.setVisibility(View.INVISIBLE);
		RelativeLayout objRelativeLayout = (RelativeLayout)findViewById(R.id.relativeLayoutAnnotation);
		if (resourceId == clsTreeview.IMAGE_RESOURCE) {	
			// Fill ListView ArrayAdapter
			objArrayAdapter = new clsActivityNoteAddNewArrayAdapter(this, R.layout.arrows_list_item, objListViewStates);
			objListView = (ListView)findViewById(R.id.listviewNumberedArrows);
			objListView.setAdapter(objArrayAdapter);
			if (objAnnotationData != null) {
				for(clsAnnotationData.clsAnnotationItem objAnnotationItem : objAnnotationData.items) {
					int intArrowNum = 1;
					if (objAnnotationItem.getType() == Shape.NUMBERED_ARROW) {
						clsArrowsListViewState objListViewState = new clsArrowsListViewState();
						objListViewState.strArrowDescription = objAnnotationItem.getAnnotationText();
						objListViewState.intArrowNum =intArrowNum; intArrowNum +=1;
						objListViewState.boolIsChecked = false;
						objListViewStates.add(objListViewState);
					}	
				}
			}		
			objRelativeLayout.setVisibility(View.VISIBLE);
			if (objAnnotationData != null) {
				lblImageAnotationStatus.setVisibility(View.VISIBLE);
				lblImageAnotationStatus.setText("Annotated");
				lblNumberedArrows.setVisibility(View.VISIBLE);
			}
		} else {
			objRelativeLayout.setVisibility(View.GONE);
		}
		
		CheckBox checkBoxUseAnnotatedImage = (CheckBox)findViewById(R.id.checkBoxUseAnnotatedImage);
		checkBoxUseAnnotatedImage.setChecked(boolUseAnnotatedImage);
		checkBoxUseAnnotatedImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CheckBox checkBoxUseAnnotatedImage = (CheckBox)v.findViewById(R.id.checkBoxUseAnnotatedImage);
				boolUseAnnotatedImage = checkBoxUseAnnotatedImage.isChecked();
			}
		});
		
		Button objButtonAnnotate = (Button)findViewById(R.id.buttonAnnotateImage);
		objButtonAnnotate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
	        	 if(objAnnotationData == null)
	        	 {
	        		 objAnnotationData = new clsAnnotationData(strTreeNodeUuid);
	        		 
	        		 objAnnotationData.compressionRate = 0;
	        		 objAnnotationData.sampleSize[0] = 1f;
	        		 objAnnotationData.sampleSize[1] = 1f;
	        	 }

	        	 // The outer wrapping of the annotation data contains the data necessary to handle the image.
	        	 // We do not know if the elements of the node have changed since the last annotation due to edits. 
	        	 // To avoid processing at different positions in MainActivity we update the annotation here.
	        	 objAnnotationData.strNodeUuid  	= strTreeNodeUuid;
	        	 objAnnotationData.strResourcePath  = resourcePath;
	        	 objAnnotationData.strDescription   = strDescription;
	        	 objAnnotationData.strLocalImage 	= imageDirectory + strTreeNodeUuid + "_full.jpg";
	        	 objAnnotationData.resourceId 		= clsTreeview.translateResourceId(resourceId);
				
				Intent editImageIntent = new Intent(objContext, ActivityEditAnnotationImage.class);			
				editImageIntent.putExtra(clsAnnotationData.DATA, 
										 clsUtils.SerializeToString(objAnnotationData));			
				startActivityForResult(editImageIntent, EDIT_ANNOTATION_IMAGE);
			}
		});
		
		Button objButtonEditArrow = (Button)findViewById(R.id.buttonEditArrowText);
		objButtonEditArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int intArrowNum = -1;
				int inArrowCount = 0;
				// Find clicked item first
				for (clsArrowsListViewState objListViewState : objListViewStates) {
					if (objListViewState.boolIsChecked) {
						intArrowNum = objListViewState.intArrowNum;
						break;
					}
				}
				if (intArrowNum == -1) {
					clsUtils.MessageBox(objContext, "Please select an arrow item first");
					return;
				}
				
				// Then find data and pass on to new activity
				for(clsAnnotationData.clsAnnotationItem objAnnotationItem : objAnnotationData.items) {					
					if (objAnnotationItem.getType() == Shape.NUMBERED_ARROW) {
						inArrowCount += 1;
						if (inArrowCount == intArrowNum) {
							Intent editTextIntent = new Intent(objContext, ActivityEditAnnotationText.class);							
							editTextIntent.putExtra(clsAnnotationData.DATA, clsUtils.SerializeToString(objAnnotationItem));							
							startActivityForResult(editTextIntent, EDIT_ANNOTATION_TEXT);
							break;
						}
					}	
				}
			}
		});
		
		// attach onClick listener to preview thumbnail to let the
		// user change the image or video
		objEditThumbnail.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				Intent chooser;
				if(resourceId == clsTreeview.IMAGE_RESOURCE || 
				   resourceId == clsTreeview.VIDEO_RESOURCE)
				{
					Intent[] intentArray = new Intent[1];
					Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
					
					mediaIntent.addCategory(Intent.CATEGORY_OPENABLE);
					// create the app chooser and add the image intent to it
					
					chooser = new Intent(Intent.ACTION_CHOOSER, null);
					chooser.putExtra(Intent.EXTRA_INTENT,  mediaIntent);
					// add the camera to the chooser
					chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,  intentArray);

					switch(resourceId)
					{
						case clsTreeview.IMAGE_RESOURCE:
							mediaIntent.setType("image/*");
							
							cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
							intentArray[0] = cameraIntent;
						
							chooser.putExtra(Intent.EXTRA_TITLE, "Select Source of Image");
												
							// start activity and provide ID of source (i.e. image)
							startActivityForResult(chooser, clsTreeview.IMAGE_RESOURCE);
							break;
						
						case clsTreeview.VIDEO_RESOURCE:
							mediaIntent.setType("video/*");
							
							cameraIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
							intentArray[0] = cameraIntent;
						
							chooser.putExtra(Intent.EXTRA_TITLE, "Select Source of Video");
					
							// start activity and provide ID of source (i.e. image)
							startActivityForResult(chooser, clsTreeview.VIDEO_RESOURCE);					
							break;
					}
				}
			}
		});
		
		switch(resourceId)
		{
			case clsTreeview.IMAGE_RESOURCE:
				loadThumbnailFromImage(objEditThumbnail);
				
				// set radio button
				rg.check(R.id.radioImageNote);
				break;
				
			case clsTreeview.VIDEO_RESOURCE:
				loadThumbnailFromImage(objEditThumbnail);
				
				// set radio button
				rg.check(R.id.radioVideoNote);
				break;
		}
		
		// object to create thumbnails from images and videos
		objResourceLoader = new clsResourceLoader();

	    if (savedInstanceState == null) {
	    	clsUtils.CustomLog( "onCreate SaveFile");
	    	SaveFile();
	    	rg.setOnCheckedChangeListener(new RadioGroupOnCheckedChangeListener());
	    } else {
	    	rg.setOnCheckedChangeListener(null);
	    	clsUtils.CustomLog( "onCreate LoadFile");
	    	LoadFile();
	    }
	    
	    rg.setFocusable(true);
	    rg.setFocusableInTouchMode(true);     
	    rg.requestFocus();
	    
	}
	
	public class RadioGroupOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			Intent[] intentArray = new Intent[1];
			final ImageView objEditThumbnail = new ImageView(objContext);
			
			// Get InputManager to hide/show soft keyboard depending on 
			// selected note type
			EditText           edit_text = (EditText)findViewById(R.id.editTextNoteName);
			InputMethodManager imm       = (InputMethodManager)getSystemService(
												Context.INPUT_METHOD_SERVICE);		
			
			switch(checkedId)
			{
				// text as note
				case R.id.radioTextNote:
					resourceId = clsTreeview.TEXT_RESOURCE;
					
					// show soft keyboard if type of note is text
					imm.showSoftInput(edit_text, 0);
					
					objEditThumbnail.setImageBitmap(null);
					resourcePath = "";
					
					break;
					
				// image as note
				case R.id.radioImageNote:
				{
					resourceId = clsTreeview.IMAGE_RESOURCE;
					
					// hide soft keyboard if type of note is photo or video
					imm.hideSoftInputFromWindow(edit_text.getWindowToken(), 0);
																	
					objEditThumbnail.setImageBitmap(null);
					resourcePath = "";

					// In the following an image chooser gets created. It lets the user
					// select an image from the supporting apps including camera which gets
					// manually added to the chooser.
					//
					// Restrict data to images
					Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
					imageIntent.setType("image/*");
					// make sure the source can be opened via openFileDescriptor
					imageIntent.addCategory(Intent.CATEGORY_OPENABLE);
					
					// create an intent to capture image by camera and add it to the array which
					// gets later added to the chooser
					cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					intentArray[0] = cameraIntent;
					
					// create the app chooser and add the image intent to it
					Intent chooser;
					chooser = new Intent(Intent.ACTION_CHOOSER, null);
					chooser.putExtra(Intent.EXTRA_INTENT,  imageIntent);
					chooser.putExtra(Intent.EXTRA_TITLE, "Select Source of Image");
					
					// add the camera to the chooser
					chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,  intentArray);
					
					// start activity and provide ID of source (i.e. image)
					startActivityForResult(chooser, GET_IMAGE_RESOURCE);
				}	
				break;
					
				// video as note
				case R.id.radioVideoNote:
				{
					resourceId = clsTreeview.VIDEO_RESOURCE;
					
					// hide soft keyboard if type of note is photo or video
					imm.hideSoftInputFromWindow(edit_text.getWindowToken(), 0);
					
					objEditThumbnail.setImageBitmap(null);
					resourcePath = "";

					// In the following an image chooser gets created. It lets the user
					// select a video from the supporting apps including camera which gets
					// manually added to the chooser.
					//
					// Restrict data to video
					Intent videoIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
					videoIntent.setType("video/*");
					// make sure the source can be opened via openFileDescriptor
					videoIntent.addCategory(Intent.CATEGORY_OPENABLE);
						
					// create an intent to capture image by camera and add it to the array which
					// gets later added to the chooser
					cameraIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
					intentArray[0] = cameraIntent;
					
					// create the app chooser and add the image intent to it
					Intent chooser;
					chooser = new Intent(Intent.ACTION_CHOOSER, null);
					chooser.putExtra(Intent.EXTRA_INTENT,  videoIntent);
					chooser.putExtra(Intent.EXTRA_TITLE, "Select Source of Video");
				
					// add the camera to the chooser
					chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,  intentArray);
					
					// start activity and provide ID of source (i.e. image)
					startActivityForResult(chooser, GET_VIDEO_RESOURCE);					
				}
				break;
					
				// image as note
				case R.id.radioURLNote:
				{
				}
				break;
				
				default:
					assert(false);
					break;
			}
			
		}
	}
	
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
		clsUtils.CustomLog("onRestart LoadFile");
		LoadFile();
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
    	clsUtils.CustomLog( "onStop SaveFile");
    	SaveFile();
    	super.onStop();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	clsUtils.CustomLog("onPause SaveFile");
    	SaveFile();
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	clsUtils.CustomLog("onDestroy SaveFile");
    	SaveFile();
    	super.onDestroy();
    }
    
    @Override
    protected void onRestart() {
    	clsUtils.CustomLog("onRestart LoadFile");
    	LoadFile();
    	super.onRestart();
    }
    
    @Override
    protected void onResume() {
    	clsUtils.CustomLog("onResume LoadFile");
    	LoadFile();
    	super.onResume();
    }
    
 
    public void SaveFile() {
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityNoteAddNew",Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPref.edit();
    	editor.putInt("resourceId",resourceId);
    	editor.putString("resourcePath",resourcePath);
    	editor.putString("strResult",strDescription);
    	editor.putBoolean("boolIsReadOnly",boolIsReadOnly);
    	editor.putString("strTreeNodeUuid",strTreeNodeUuid);
    	String strAnnotationData = clsUtils.SerializeToString(objAnnotationData);
    	editor.putString("objAnnotationData",strAnnotationData);   
    	String strListViewStates = clsUtils.SerializeToString(objListViewStates);
    	editor.putString("objListViewStates",strListViewStates);  
    	editor.putBoolean("boolUseAnnotatedImage",boolUseAnnotatedImage);
    	editor.putBoolean("boolIsDirty",boolIsDirty);
    	if (strImageFilename != null) {
        	editor.putString("strImageFilename",strImageFilename.toString());
    	} else {
    		editor.putString("strImageFilename","");
    	}
    	if (strThumbnailFilename != null) {
        	editor.putString("strThumbnailFilename",strThumbnailFilename.toString());
    	} else {
    		editor.putString("strThumbnailFilename","");
    	}
    	if (strFullFilename != null) {
        	editor.putString("strFullFilename",strFullFilename.toString());
    	} else {
    		editor.putString("strFullFilename","");
    	}
    	

	
    	editor.commit();
    	sharedPref = null;
	}
    
	private void LoadFile() {
		objIntent = getIntent();
		objContext = this;
		
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityNoteAddNew",Context.MODE_PRIVATE);
		resourceId = sharedPref.getInt("resourceId",0);
		resourcePath = sharedPref.getString("resourcePath","");
		strDescription = sharedPref.getString("strResult","");
		boolIsReadOnly = sharedPref.getBoolean("boolIsReadOnly",false);
		strTreeNodeUuid = sharedPref.getString("strTreeNodeUuid","");	
		String strAnnotationData = sharedPref.getString("objAnnotationData","");
		objAnnotationData = clsUtils.DeSerializeFromString(strAnnotationData, clsAnnotationData.class);
		String strListViewStates = sharedPref.getString("objListViewStates","");
		java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsArrowsListViewState>>(){}.getType();
		objListViewStates = clsUtils.DeSerializeFromString(strListViewStates, collectionType);
		if (objArrayAdapter != null) {
			objArrayAdapter.clear();objArrayAdapter.addAll(objListViewStates);
		} else {
			objArrayAdapter = new clsActivityNoteAddNewArrayAdapter(this, R.layout.arrows_list_item, objListViewStates);
			objListView = (ListView)findViewById(R.id.listviewNumberedArrows);
			objListView.setAdapter(objArrayAdapter);		
		}
		boolUseAnnotatedImage = sharedPref.getBoolean("boolUseAnnotatedImage",false);	
		boolIsDirty = sharedPref.getBoolean("boolIsDirty",true);
		
		String strLocalImageFilename = sharedPref.getString("strImageFilename","");	
		if (!strLocalImageFilename.isEmpty()) {
			strImageFilename = new File(strLocalImageFilename);
		}
		strLocalImageFilename = sharedPref.getString("strThumbnailFilename","");	
		if (!strLocalImageFilename.isEmpty()) {
			strThumbnailFilename = new File(strLocalImageFilename);
		}
		strLocalImageFilename = sharedPref.getString("strFullFilename","");	
		if (!strLocalImageFilename.isEmpty()) {
			strFullFilename = new File(strLocalImageFilename);
		}
					
		RelativeLayout objRelativeLayout = (RelativeLayout)findViewById(R.id.relativeLayoutAnnotation);
		if (resourceId == clsTreeview.IMAGE_RESOURCE) {		
			objEditThumbnail = (ImageView)findViewById(R.id.imagePreview);
			if (strImageFilename.exists()) {
				loadThumbnailFromImage(objEditThumbnail);
			}
			objRelativeLayout.setVisibility(View.VISIBLE);
			if (objAnnotationData != null) {
				TextView lblImageAnotationStatus = (TextView)findViewById(R.id.lblImageAnotationStatus);
				lblImageAnotationStatus.setVisibility(View.VISIBLE);
				lblImageAnotationStatus.setText("Annotated");
				TextView lblNumberedArrows = (TextView)findViewById(R.id.lblNumberedArrows);
				lblNumberedArrows.setVisibility(View.VISIBLE);
			}
		} else {
			objRelativeLayout.setVisibility(View.GONE);
		}
	}
	
	private void loadThumbnailFromImage(ImageView preview)
	{	
		Bitmap bitmap = BitmapFactory.decodeFile(strImageFilename.getAbsolutePath());
		preview.setImageBitmap(bitmap);	
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// Note that this method gets called when the chooser gets closed (by either 
		// selecting an image or video or aborting the chooser or the app which
		// provides the media)
		
		ImageView objEditThumbnail;
		objEditThumbnail = (ImageView)findViewById(R.id.imagePreview);
		objEditThumbnail.setImageBitmap(null);

		if(resultCode == RESULT_OK)
		{	
			switch (requestCode) {
			case GET_IMAGE_RESOURCE:
			case GET_VIDEO_RESOURCE:
				Uri origUri = data.getData();
				if(origUri != null)
				{
					resourcePath = origUri.toString();

					pd = ProgressDialog.show(this,  
							"Please wait", 
							"Loading Resource", 
							true, true, null);

					// This Activity is the only one which has access rights to remote images because it gets them from
					// the external media App (e.g. Gallery or Google Photo). That is why it retrieves the media contents
					// and creates a thumbnail bitmap and an image bitmap (not for videos).
					// Due to limits of hardware memory large images can cause an out-of-memory exception while being loaded.
					// To avoid this the app down-samples the images to fit the screen. The screen size is close enough to the
					// size of the Annotation view, which actually uses the images. It should be stressed that for future
					// high-resolution hardware that issue can arise again.
					DisplayMetrics dm = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(dm);

					objResourceLoader.createThumbnailFromImageResource(this, this, requestCode, origUri, 
							dm.widthPixels, dm.heightPixels);
					RelativeLayout objRelativeLayout = (RelativeLayout)findViewById(R.id.relativeLayoutAnnotation);
					objRelativeLayout.setVisibility(View.VISIBLE);
					
					objAnnotationData = null;
					objListViewStates.clear();
					objArrayAdapter.clear();objArrayAdapter.addAll(objListViewStates);
					objListView.invalidateViews();
					TextView lblImageAnotationStatus = (TextView)findViewById(R.id.lblImageAnotationStatus);
					lblImageAnotationStatus.setVisibility(View.INVISIBLE);
					TextView lblNumberedArrows = (TextView)findViewById(R.id.lblNumberedArrows);
					lblNumberedArrows.setVisibility(View.INVISIBLE);
					CheckBox checkBoxUseAnnotatedImage = (CheckBox)findViewById(R.id.checkBoxUseAnnotatedImage);
					boolUseAnnotatedImage = false;
					checkBoxUseAnnotatedImage.setChecked(boolUseAnnotatedImage);
					SaveFile();
				
				}
				else {
					resourcePath = "";
				}

				break;
			case EDIT_ANNOTATION_IMAGE:
				Bundle objBundle = data.getExtras();
				objAnnotationData = clsUtils.DeSerializeFromString(objBundle.getString(clsAnnotationData.DATA), 
										   clsAnnotationData.class);


				
				if (objAnnotationData != null) {
					if (objAnnotationData.items.size()  == 0 ) {
						objAnnotationData = null;
					}		
				}
				

				// Update the display
				objListViewStates.clear();
				Bitmap bitmap = BitmapFactory.decodeFile(strImageFilename.getAbsolutePath());
				objEditThumbnail.setImageBitmap(bitmap);
				int intArrowNum = 1;
				if (objAnnotationData != null) {
					
					for(clsAnnotationData.clsAnnotationItem objAnnotationItem : objAnnotationData.items)
					{
						if (objAnnotationItem.getType() == Shape.NUMBERED_ARROW) {
							clsArrowsListViewState objListViewState = new clsArrowsListViewState();
							objListViewState.strArrowDescription = objAnnotationItem.getAnnotationText();
							objListViewState.intArrowNum =intArrowNum; intArrowNum +=1;
							objListViewState.boolIsChecked = false;
							objListViewStates.add(objListViewState);
						}
					}
					
					TextView lblImageAnotationStatus = (TextView)findViewById(R.id.lblImageAnotationStatus);
					lblImageAnotationStatus.setVisibility(View.VISIBLE);
					lblImageAnotationStatus.setText("Annotated");
					TextView lblNumberedArrows = (TextView)findViewById(R.id.lblNumberedArrows);
					lblNumberedArrows.setVisibility(View.VISIBLE);
					CheckBox checkBoxUseAnnotatedImage = (CheckBox)findViewById(R.id.checkBoxUseAnnotatedImage);
					boolUseAnnotatedImage = true;
					checkBoxUseAnnotatedImage.setChecked(boolUseAnnotatedImage);

				} else {
					TextView lblImageAnotationStatus = (TextView)findViewById(R.id.lblImageAnotationStatus);
					lblImageAnotationStatus.setVisibility(View.INVISIBLE);
					TextView lblNumberedArrows = (TextView)findViewById(R.id.lblNumberedArrows);
					lblNumberedArrows.setVisibility(View.INVISIBLE);
					CheckBox checkBoxUseAnnotatedImage = (CheckBox)findViewById(R.id.checkBoxUseAnnotatedImage);
					boolUseAnnotatedImage = false;
					checkBoxUseAnnotatedImage.setChecked(boolUseAnnotatedImage);
				}
				
				objArrayAdapter.clear();objArrayAdapter.addAll(objListViewStates);
				objListView.invalidateViews();
				
				SaveFile();
				break;
			case EDIT_ANNOTATION_TEXT:	
				objBundle = data.getExtras();
				clsAnnotationData.clsAnnotationItem objUpdatedAnnotationItem = clsUtils.DeSerializeFromString(objBundle.getString(clsAnnotationData.DATA), 
						clsAnnotationData.clsAnnotationItem.class);
				// Then replace the item
				intArrowNum = -1;
				int inArrowCount = 0;
				// Find clicked item first
				for (clsArrowsListViewState objListViewState : objListViewStates) {
					if (objListViewState.boolIsChecked) {
						intArrowNum = objListViewState.intArrowNum;
						break;
					}
				}
				// Replace item in annotation object
				for(clsAnnotationData.clsAnnotationItem objOrigAnnotationItem : objAnnotationData.items) {					
					if (objOrigAnnotationItem.getType() == Shape.NUMBERED_ARROW) {
						inArrowCount += 1;
						if (inArrowCount == intArrowNum) {
							int intIndex = objAnnotationData.items.indexOf(objOrigAnnotationItem);
							objAnnotationData.items.set(intIndex, objUpdatedAnnotationItem);
							break;
						}
					}	
				}

				// Update listview
				objListViewStates.clear();
				intArrowNum = 1;
				for(clsAnnotationData.clsAnnotationItem objAnnotationItem : objAnnotationData.items) {
					if (objAnnotationItem.getType() == Shape.NUMBERED_ARROW) {
						clsArrowsListViewState objListViewState = new clsArrowsListViewState();
						objListViewState.strArrowDescription = objAnnotationItem.getAnnotationText();
						objListViewState.intArrowNum =intArrowNum; intArrowNum +=1;
						objListViewState.boolIsChecked = false;
						objListViewStates.add(objListViewState);
					}	
				}
				objArrayAdapter.clear();objArrayAdapter.addAll(objListViewStates);
				objListView.invalidateViews();
				
				bitmap = BitmapFactory.decodeFile(strImageFilename.getAbsolutePath());
				objEditThumbnail.setImageBitmap(bitmap);

				SaveFile();
				break;
			}		
		} 
		RadioGroup rg = (RadioGroup)findViewById(R.id.radioItemType);
		rg.setOnCheckedChangeListener(new RadioGroupOnCheckedChangeListener());
		
	}
	
	private void SaveBitmapToFile (Bitmap objBitmap, String strFilename) {
		
		SaveBitmapToFile(objBitmap, strFilename, 100);
	}

	private void SaveBitmapToFile (Bitmap objBitmap, String strFilename, int compressRate) {
		OutputStream fOutputStream = null;
		
		File file = new File(strFilename);
        try {
            fOutputStream = new FileOutputStream(file);

            objBitmap.compress(Bitmap.CompressFormat.JPEG, compressRate, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save Failed (Not Found)", Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save Failed IO", Toast.LENGTH_SHORT).show();
            return;
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.note_add_new_or_edit, menu);
		if (boolIsReadOnly) {
			menu.findItem(R.id.actionAccept).setVisible(false);
			menu.findItem(R.id.actionCancel).setVisible(false);
		} else {
			menu.findItem(R.id.actionAccept).setVisible(true);
			menu.findItem(R.id.actionCancel).setVisible(true);
		}

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		boolean success = false;
		
    	switch (item.getItemId()) 
    	{
        	case R.id.actionAccept:
        		EditText objEditView    = (EditText)findViewById(R.id.editTextNoteName);
         		String   strDescription = objEditView.getText().toString();
         		clsUtils.CustomLog("on Accept, resourceId = " + resourceId);
         		switch(resourceId)
         		{
         			case clsTreeview.TEXT_RESOURCE:
         				// for a TEXT_RESOURCE the description MUST be set
         				if(strDescription.isEmpty())
         				{
         					// pop up information dialog
         					// showErrorDialog(this, R.string.dlg_description_empty, true);
         					strDescription = "Empty note";
         					success = true;
         				}
         				else
         				{
         					success = true;
         				}
         				
         				break;
         				
         			case clsTreeview.IMAGE_RESOURCE: // Todo
         				// check if resource_path is set
         				if(resourcePath == null || resourcePath.isEmpty())
         				{
         					// pop up information dialog
         					clsUtils.showErrorDialog(this, R.string.dlg_image_empty, true);
                		    
                		    success = false;
         				}
         				else
         				{
         					String strAnnotationData = clsUtils.SerializeToString(objAnnotationData);
         					objIntent.putExtra(ActivityNoteStartup.ANNOTATION_DATA_GSON, strAnnotationData);
         					success = true;
         				}
         				break;
         				
         			case clsTreeview.VIDEO_RESOURCE: // Todo
         				if(resourcePath == null || resourcePath.isEmpty())
         				{
         					// pop up information dialog
         					clsUtils.showErrorDialog(this, R.string.dlg_video_empty, true);
                		    
                		    success = false;
         				}
         				else
         				{
         					success = true;
         				}
         				break;
         				
         			default:
         				assert(false);
         		}
         		
         		if(success)
         		{
         			// put Information for MainActivity together
         			// depending on resourceId not all the information is needed
	            	objIntent.putExtra(ActivityNoteStartup.DESCRIPTION,   strDescription);
	            	objIntent.putExtra(ActivityNoteStartup.RESOURCE_ID,   resourceId);            	
	            	objIntent.putExtra(ActivityNoteStartup.RESOURCE_PATH, resourcePath);
	            	objIntent.putExtra(ActivityNoteStartup.TREENODE_UID, strTreeNodeUuid);
	            	objIntent.putExtra(ActivityNoteStartup.USE_ANNOTATED_IMAGE, boolUseAnnotatedImage);
	            	objIntent.putExtra(ActivityNoteStartup.ISDIRTY, true);
	            	
	            	setResult(RESULT_OK, objIntent);
	            	ActivityNoteAddNew.this.finish();
	            	
	            	return true;
         		}
         		else
	            	return false;
        	 
         case R.id.actionCancel:
     		// If initial resource was an Image then restore relevant files because they 
     		// might have been overwritten in that Activity.
     		if(resourceId == clsTreeview.IMAGE_RESOURCE)
     		{
     			strImageFilenameBackup.renameTo(strImageFilename);
     			strThumbnailFilenameBackup.renameTo(strThumbnailFilename);
     			strFullFilenameBackup.renameTo(strFullFilename);
     		}
     		      	 
        	objIntent.putExtra(ActivityNoteStartup.DESCRIPTION, "");
        	objIntent.putExtra(ActivityNoteStartup.ISDIRTY, false);
        	setResult(RESULT_CANCELED, objIntent);
        	this.finish();
            return true;
             
         default:
             return super.onOptionsItemSelected(item);
      }
	}
}
