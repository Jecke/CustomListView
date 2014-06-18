package com.treeapps.treenotes;

import java.io.File;
import java.util.ArrayList;

import com.google.gson.reflect.TypeToken;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.imageannotation.ActivityEditAnnotationImage;
import com.treeapps.treenotes.imageannotation.ActivityEditAnnotationText;
import com.treeapps.treenotes.imageannotation.clsAnnotationData;
import com.treeapps.treenotes.imageannotation.clsCombineAnnotateImage;
import com.treeapps.treenotes.imageannotation.clsShapeFactory.Shape;

import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RadioGroup;
import android.widget.ImageView;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View.OnClickListener;

public class ActivityNoteAddNew extends Activity implements clsResourceLoader.TaskCompletedInterface, 
															clsCombineAnnotateImage.TaskCompletedInterface {
	
	public static final int EDIT_ANNOTATION_IMAGE = 10;
	public static final int EDIT_ANNOTATION_TEXT  = 11;
	
	public static final String WEB_VIEW_URL   = "com.treeapps.treenotes.url";
	public static final String WEB_VIEW_IMAGE = "com.treeapps.treenotes.web_image";
	
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

	private String url = "";
	
	// Annotation data
	public clsAnnotationData objAnnotationData;
	private boolean boolIsDirty = false;
	
	// Selected radio button
	int    resourceId, resourceIdOrig;
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
	
	private File strImageFilename;
	private File strFullFilename; 
	private File strAnnotatedFilename;
	
	// If the user selects the camera to get a picture then the result must be handled differently
	// than a picture from the gallery.
	private Uri mPhotoUri;
	
	private String strWebPageURL;

	public void loadTaskComplete(String combinedFile) {

		// Remove possible backup files
		clsUtils.RemoveBackupImagesOfNode((Context)objContext, strTreeNodeUuid);
			
		// put Information for MainActivity together
		// depending on resourceId not all the information is needed
    	objIntent.putExtra(ActivityNoteStartup.DESCRIPTION,   strDescription);
    	objIntent.putExtra(ActivityNoteStartup.RESOURCE_ID,   resourceId);            	
    	objIntent.putExtra(ActivityNoteStartup.RESOURCE_PATH, resourcePath);
    	objIntent.putExtra(ActivityNoteStartup.TREENODE_URL, strWebPageURL);
    	objIntent.putExtra(ActivityNoteStartup.TREENODE_UID, strTreeNodeUuid);
    	objIntent.putExtra(ActivityNoteStartup.USE_ANNOTATED_IMAGE, boolUseAnnotatedImage);
    	objIntent.putExtra(ActivityNoteStartup.ISDIRTY, true);
    	
    	setResult(RESULT_OK, objIntent);
    	ActivityNoteAddNew.this.finish();
	}

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

			resourcePath = uri.toString();

			if(bmFull != null)// && resourcePath.contains("http"))
			{
				objResourceLoader.saveBitmapToFile(objContext, bmFull, 
													strFullFilename.getAbsolutePath(), 100);
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
		
		Bundle objBundle = objIntent.getExtras();    		
		strDescription  = objBundle.getString(ActivityNoteStartup.DESCRIPTION);
		
		resourceId      = objBundle.getInt(ActivityNoteStartup.RESOURCE_ID);
		resourceIdOrig = resourceId; // keep backup in case user cancels 
		
		resourcePath    = objBundle.getString(ActivityNoteStartup.RESOURCE_PATH);
		strTreeNodeUuid = objBundle.getString(ActivityNoteStartup.TREENODE_UID);
		boolIsReadOnly = objBundle.getBoolean(ActivityNoteStartup.READONLY);
		String strAnnotationData = objBundle.getString(ActivityNoteStartup.ANNOTATION_DATA_GSON);
		objAnnotationData = clsUtils.DeSerializeFromString(strAnnotationData, clsAnnotationData.class);
		String strTreeNodeOwnername = objBundle.getString(ActivityNoteStartup.TREENODE_OWNERNAME);
		boolUseAnnotatedImage = objBundle.getBoolean(ActivityNoteStartup.USE_ANNOTATED_IMAGE);
		boolIsDirty = objBundle.getBoolean(ActivityNoteStartup.ISDIRTY);
		
		// Set title of activity to prefix retrieved from config (strings.xml) plus
		// the first 50 characters of the description associated with the parent note. 
		String strParentName = objBundle.getString(ActivityNoteStartup.TREENODE_PARENTNAME);
		String strActivityTitle = getResources().getString(R.string.title_activity_add_new_or_edit) + ": ";
		strActivityTitle += (strParentName.length() > 50)?(strParentName.substring(0, 50)+"..."):(strParentName);
		setTitle(strActivityTitle);

		strImageFilename           = new File(clsUtils.GetThumbnailImageFileName(objContext, strTreeNodeUuid));
		strFullFilename            = new File(clsUtils.GetFullImageFileName(objContext, strTreeNodeUuid));
		strAnnotatedFilename       = new File(clsUtils.GetAnnotatedImageFileName(objContext, strTreeNodeUuid));
		
		if(resourceId == clsTreeview.IMAGE_RESOURCE || resourceId == clsTreeview.WEB_RESOURCE ||
		   strTreeNodeUuid.equals("temp_uuid"))
		{
			// make copies of important files which could be altered
			// thumbnail
			clsUtils.CreateBackupFromFile(strImageFilename);
			
			// original image
			clsUtils.CreateBackupFromFile(strFullFilename);

			// annotated image
			clsUtils.CreateBackupFromFile(strAnnotatedFilename);
		}

		// Hide items when in read only mode
		HideUnhideUiItems(boolIsReadOnly);
		
		
		// set description field
		EditText objEditText = (EditText)findViewById(R.id.editTextNoteName);
		if (boolIsReadOnly) {
			strDescription = strDescription.trim();
			if(clsUtils.IsLastCharacterFullstop(strDescription)) {
				objEditText.setText(strDescription + " Comment by " + strTreeNodeOwnername);
			} else {
				objEditText.setText(strDescription + ". Comment by " + strTreeNodeOwnername);
			}
			
		} else {
			objEditText.setText(strDescription);
		}

		objEditText.setEnabled(!boolIsReadOnly);
				
		// remove thumbnail
		objEditThumbnail = (ImageView)findViewById(R.id.imagePreview);
		objEditThumbnail.setImageBitmap(null);	
		
		// Annotation display
		RelativeLayout objRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutAnnotation);
		TextView lblImageAnotationStatus = (TextView)findViewById(R.id.lblImageAnotationStatus);
		lblImageAnotationStatus.setVisibility(View.INVISIBLE);
		TextView lblNumberedArrows = (TextView)findViewById(R.id.lblNumberedArrows);
		lblNumberedArrows.setVisibility(View.INVISIBLE);
		if (resourceId == clsTreeview.IMAGE_RESOURCE || resourceId == clsTreeview.WEB_RESOURCE) {	
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
				CheckBox checkBoxUseAnnotatedImage = (CheckBox)v.findViewById(R.id.checkBoxUseAnnotatedImage);
				boolUseAnnotatedImage = checkBoxUseAnnotatedImage.isChecked();
			}
		});
		
		Button objButtonAnnotate = (Button)findViewById(R.id.buttonAnnotateImage);
		objButtonAnnotate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
	        	 objAnnotationData.strLocalImage 	= clsUtils.GetFullImageFileName(objContext, strTreeNodeUuid);
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
					clsUtils.MessageBox(objContext, "Please select an arrow item first", false);
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
				   resourceId == clsTreeview.VIDEO_RESOURCE ||
				   resourceId == clsTreeview.WEB_RESOURCE)
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
							
						case clsTreeview.WEB_RESOURCE:
							resourceId = clsTreeview.WEB_RESOURCE;

							String imageFile = strFullFilename.toString();
							
							Intent web = new Intent(objContext, ActivityWebBrowser.class);
							web.putExtra(WEB_VIEW_URL, url);
							web.putExtra(WEB_VIEW_IMAGE, imageFile);

							startActivityForResult(web, clsTreeview.WEB_RESOURCE);
							break;
					}
				}
			}
		});
		
		
		RadioGroup rg = (RadioGroup) findViewById(R.id.radioItemType);
		switch(resourceId)
		{
			case clsTreeview.IMAGE_RESOURCE:
				// set radio button
				rg.check(R.id.radioImageNote);
				loadThumbnailFromImage(objEditThumbnail);
				break;

			case clsTreeview.WEB_RESOURCE:
				// set radio button
				rg.check(R.id.radioURLNote);
				loadThumbnailFromImage(objEditThumbnail);
				break;
				
			case clsTreeview.VIDEO_RESOURCE:
				// set radio button
				rg.check(R.id.radioVideoNote);
				loadThumbnailFromImage(objEditThumbnail);
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
	
	private void HideUnhideUiItems(boolean boolIsReadOnly) {		
		RadioGroup rg = (RadioGroup) findViewById(R.id.radioItemType);
		RelativeLayout objRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutAnnotation);
		Button objButtonEditArrow = (Button)findViewById(R.id.buttonEditArrowText);
		Button objButtonAnnotateImage = (Button)findViewById(R.id.buttonAnnotateImage);
		CheckBox objCheckUseAnnotatedImage = (CheckBox) findViewById(R.id.checkBoxUseAnnotatedImage);
		LinearLayout ll = (LinearLayout) findViewById(R.id.my_image_button);
		TextView tv = (TextView) findViewById(R.id.textViewURLNote);
		if (boolIsReadOnly) {
			rg.setVisibility(View.INVISIBLE);
			objRelativeLayout.setVisibility(View.INVISIBLE);
			ll.setVisibility(View.INVISIBLE);
			tv.setVisibility(View.INVISIBLE);
			objButtonEditArrow.setVisibility(View.INVISIBLE);
			objButtonAnnotateImage.setVisibility(View.INVISIBLE);
			objCheckUseAnnotatedImage.setVisibility(View.INVISIBLE);
		} else {
			rg.setVisibility(View.VISIBLE);
			objRelativeLayout.setVisibility(View.VISIBLE);
			ll.setVisibility(View.VISIBLE);
			tv.setVisibility(View.VISIBLE);
			objButtonEditArrow.setVisibility(View.VISIBLE);
			objButtonAnnotateImage.setVisibility(View.VISIBLE);
			objCheckUseAnnotatedImage.setVisibility(View.VISIBLE);
		}
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
					mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
				            								new ContentValues());
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
					
					intentArray[0] = cameraIntent;
					
					// create the app chooser and add the image intent to it
					Intent chooser;
					chooser = new Intent(Intent.ACTION_CHOOSER, null);
					chooser.putExtra(Intent.EXTRA_INTENT,  imageIntent);
					chooser.putExtra(Intent.EXTRA_TITLE, "Select Source of Image");
					
					// add the camera to the chooser
					chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,  intentArray);
					
					// start activity and provide ID of source (i.e. image)
//					startActivityForResult(chooser, GET_IMAGE_RESOURCE);
					startActivityForResult(chooser, clsTreeview.IMAGE_RESOURCE);
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
					startActivityForResult(chooser, clsTreeview.VIDEO_RESOURCE);					
				}
				break;
					
				// image as note
				case R.id.radioURLNote:
				{
					resourceId = clsTreeview.WEB_RESOURCE;

					String imageFile = strImageFilename.toString();
					
					Intent web = new Intent(objContext, ActivityWebBrowser.class);
					web.putExtra(WEB_VIEW_IMAGE, imageFile);

					startActivityForResult(web, clsTreeview.WEB_RESOURCE);
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
    	clsUtils.CustomLog("onPause SaveFile");
    	SaveFile();
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
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
    	if (strFullFilename != null) {
        	editor.putString("strFullFilename",strFullFilename.toString());
    	} else {
    		editor.putString("strFullFilename","");
    	}
    	if (strAnnotatedFilename != null) {
        	editor.putString("strAnnotatedFilename", strAnnotatedFilename.toString());
    	} else {
    		editor.putString("strAnnotatedFilename","");
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
		strLocalImageFilename = sharedPref.getString("strFullFilename","");	
		if (!strLocalImageFilename.isEmpty()) {
			strFullFilename = new File(strLocalImageFilename);
		}
		strLocalImageFilename = sharedPref.getString("strAnnotatedFilename","");	
		if (!strLocalImageFilename.isEmpty()) {
			strAnnotatedFilename = new File(strLocalImageFilename);
		}
					
		RelativeLayout objRelativeLayout = (RelativeLayout)findViewById(R.id.relativeLayoutAnnotation);
		if (resourceId == clsTreeview.IMAGE_RESOURCE || resourceId == clsTreeview.WEB_RESOURCE) {		
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
		Bundle objBundle;
		Uri origUri;
		
		ImageView objEditThumbnail;
		objEditThumbnail = (ImageView)findViewById(R.id.imagePreview);
		objEditThumbnail.setImageBitmap(null);

		if(resultCode == RESULT_OK)
		{	
			switch (requestCode) {
			case clsTreeview.IMAGE_RESOURCE:
			case clsTreeview.VIDEO_RESOURCE:
				
				if(data == null)
				{
					// Image comes from camera. mPhotoUri has been provided to the camera intent
					// earlier and points now to the image taken.
					origUri = mPhotoUri;

				}
				else
				{
					origUri = data.getData();
				}
				
				if(origUri != null)
				{
					resourcePath = origUri.toString();

					pd = ProgressDialog.show(this, 
							"Please wait", 
							"Loading Resource", 
							true, true, null);

					loadImageAndEnableAnnotation(requestCode, origUri);
				}
				else 
				{
					resourcePath = "";
				}

				break;
				
			case clsTreeview.WEB_RESOURCE:
				objBundle = data.getExtras();

				url          = objBundle.getString(WEB_VIEW_URL);
				resourcePath = objBundle.getString(WEB_VIEW_IMAGE);
				origUri 	 = Uri.parse(resourcePath);

				
				// Put URL in description if description is empty
				EditText objEditView = (EditText)findViewById(R.id.editTextNoteName);
				if(objEditView.getText().toString().isEmpty())
				{
					// set description field
					objEditView.setText(url);
				}
				
				if(origUri != null)
				{

					pd = ProgressDialog.show(this, 
							"Please wait", 
							"Loading Resource", 
							true, true, null);

					loadImageAndEnableAnnotation(requestCode, origUri);
				}
				else 
				{
					resourcePath = "";
				}
				break;
				
			case EDIT_ANNOTATION_IMAGE:
				objBundle = data.getExtras();
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
	
	// Helper
	private void loadImageAndEnableAnnotation(int requestCode, Uri origUri)
	{
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

	private void SaveBitmapToFile (Bitmap objBitmap, String strFilename) {
		
		objResourceLoader.saveBitmapToFile(objContext, objBitmap, strFilename, 100);
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
		boolean success = false;
		
    	switch (item.getItemId()) 
    	{
        	case R.id.actionAccept:
        		EditText objEditView    = (EditText)findViewById(R.id.editTextNoteName);
         		
        		strDescription = objEditView.getText().toString();
         		
         		// Default: Set annotationData to null. That covers the use cases when an annotated 
         		// image gets replaced by another type of node.
         		String strAnnotationData = clsUtils.SerializeToString(null);
				objIntent.putExtra(ActivityNoteStartup.ANNOTATION_DATA_GSON, strAnnotationData);
         		
				strWebPageURL = "";
				
         		switch(resourceId)
         		{
         			case clsTreeview.TEXT_RESOURCE:
         				// for a TEXT_RESOURCE the description MUST be set
         				if(strDescription.isEmpty())
         				{
         					strDescription = "Empty note";
         					success = true;
         				}
         				else
         				{
         					success = true;
         				}
         				
         				break;
         				
         			case clsTreeview.IMAGE_RESOURCE:
         			case clsTreeview.WEB_RESOURCE:
         				// check if resource_path is set
         				if(resourcePath == null || resourcePath.isEmpty())
         				{
         					// pop up information dialog
         					clsUtils.showErrorDialog(this, R.string.dlg_image_empty, true);
                		    
                		    success = false;
         				}
         				else
         				{
         					if(resourceId == clsTreeview.WEB_RESOURCE)
         					{
         						strWebPageURL = url;
         					}
         					
         					// Create the annotated version of the original file and store it
         					// for later use (e.g. exporting and display)
         					DisplayMetrics dm = new DisplayMetrics();
         					getWindowManager().getDefaultDisplay().getMetrics(dm);
         					
         					clsCombineAnnotateImage objCombineAnnotateImage = new clsCombineAnnotateImage(objContext, this);
         					objCombineAnnotateImage.createAnnotatedImage(strFullFilename.toString(), strAnnotatedFilename.toString(), 
         																	objAnnotationData, dm.widthPixels, dm.heightPixels);


         					strAnnotationData = clsUtils.SerializeToString(objAnnotationData);
         					objIntent.putExtra(ActivityNoteStartup.ANNOTATION_DATA_GSON, strAnnotationData);
         					success = true;
         					
         					return true;
         				}
         				break;
         				
         			case clsTreeview.VIDEO_RESOURCE:
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
         			loadTaskComplete("");
	            	return true;
         		}
         		else
	            	return false;
        	 
         case R.id.actionCancel:
        	 // Restore original condition:
        	 // If old and new id supports images then restore backup.
        	 if((resourceId     == clsTreeview.IMAGE_RESOURCE || resourceId     == clsTreeview.WEB_RESOURCE) &&
        		(resourceIdOrig == clsTreeview.IMAGE_RESOURCE || resourceIdOrig == clsTreeview.WEB_RESOURCE))
        	 {
      			clsUtils.RestoreFileFromBackup(strImageFilename);
      			clsUtils.RestoreFileFromBackup(strFullFilename);
      			clsUtils.RestoreFileFromBackup(strAnnotatedFilename);
        	 }
        	 // otherwise just blindly try to remove all images even though the actual resource may not support images
        	 else if(resourceIdOrig == clsTreeview.TEXT_RESOURCE)
        	 {
      			clsUtils.RemoveAllImagesOfNode(objContext, strTreeNodeUuid);
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
