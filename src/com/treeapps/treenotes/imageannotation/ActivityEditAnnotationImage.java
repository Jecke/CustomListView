package com.treeapps.treenotes.imageannotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;



import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.imageannotation.clsAnnotationData.clsAnnotationItem;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.Style;
import android.graphics.Path;

// This activity handles annotation of an image. It lets the user 
// - select linestyle and color which are used for all future shapes until a new style gets selected
// - add shapes to the image
// - mark a shape 
//	- to delete it
//  - to move it
//	- to resize it
//	- to change linestyle and color
// 	- to add/edit text to a numbered arrow
// 
public class ActivityEditAnnotationImage extends Activity {
	private final int MIN_LINE_WIDTH     = 1;
	private final int MAX_LINE_WIDTH     = 20;
	
	// View containing the image to be annotated
	//private clsInteractiveImageView image;
	// View containing the shapes used to annotate the image
	private clsInteractiveImageViewOverlay overlayImage; 
	
	private AlertDialog dlg;
	private ProgressDialog pd;
	
	// data which activity is currently processing
	private clsAnnotationData data;
	
	private boolean imageLoaded;

	private Menu mOptionsMenu;
	
	private boolean changeAttributeOfSelected = false;
	
	public class ShapeObserver
	{
		public void notify(clsShapeFactory.Shape type)
		{
			boolean updateAttributes = true;
			
			// update menu items depending on selected shape
			switch(type)
			{
				case NONE:
					mOptionsMenu.findItem(R.id.actionDeleteSelected).setEnabled(false);
					mOptionsMenu.findItem(R.id.actionAttributesSelected).setEnabled(false);
					mOptionsMenu.findItem(R.id.actionAnnotateSelected).setEnabled(false);
					
					updateAttributes = false;
					break;
					
				case RECTANGLE:
					mOptionsMenu.findItem(R.id.actionDeleteSelected).setEnabled(true);
					mOptionsMenu.findItem(R.id.actionAttributesSelected).setEnabled(true);
					mOptionsMenu.findItem(R.id.actionAnnotateSelected).setEnabled(false);
					break;
					
				case ARROW:
					mOptionsMenu.findItem(R.id.actionDeleteSelected).setEnabled(true);
					mOptionsMenu.findItem(R.id.actionAttributesSelected).setEnabled(true);
					mOptionsMenu.findItem(R.id.actionAnnotateSelected).setEnabled(false);
					break;

				case NUMBERED_ARROW:
					mOptionsMenu.findItem(R.id.actionDeleteSelected).setEnabled(true);
					mOptionsMenu.findItem(R.id.actionAttributesSelected).setEnabled(true);
					mOptionsMenu.findItem(R.id.actionAnnotateSelected).setEnabled(true);
					break;
			}
			
			if(updateAttributes)
			{
				// Update the attribute dialog from the selected object
				overlayImage.getAttributesOfSelectedShape(lineAttributes);
			}
		}
	}
	private ShapeObserver shapeObserver;

	private clsAnnotationData.AttributeContainer lineAttributes;
	
	// needs the resource id of the image in order to create the local copy
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_edit_annotate_image);
		
		Intent objIntent = getIntent();
		
		imageLoaded = false;
		
		Bundle objBundle = objIntent.getExtras();
		
		data = clsUtils.DeSerializeFromString(objBundle.getString(clsAnnotationData.DATA), 
				     						   clsAnnotationData.class);
		
		// The shapes which are associated with data get drawn in onWindowFocusChanged because
		// only then is the size of the bitmap known and valid
		
		// create Attribute container with default values
		/*lineAttributes = data.new AttributeContainer(DEFAULT_LINE_WIDTH, 
												clsUtils.Linestyle.LS_SOLID,
												Color.RED, Color.WHITE);*/		
		
		TextView textViewCompressRate = (TextView)findViewById(R.id.textViewCompression);
		// set initial compression rate in label and SeekBar
		textViewCompressRate.setText(String.valueOf(data.compressionRate) + "%");
		
		SeekBar sb = (SeekBar)findViewById(R.id.seekBarCompressionRate); 
		sb.setOnSeekBarChangeListener(new CompressBarChangeListener());
		sb.setProgress(data.compressionRate / 10);
		
		shapeObserver = new ShapeObserver();
		
		overlayImage = 
				(clsInteractiveImageViewOverlay)findViewById(R.id.imageViewAnnotateOverlay);
		overlayImage.post(new Runnable() {
			
			@Override
			public void run() {
				
				// Layout is known. Load image on view.
				loadImage(data.strLocalImage, data.compressionRate);
			}
		});
	}

	// Open the attribute dialog (set in layout XML)
	public void attributeOnClick(View v)
	{
		// Create and show the attribute dialog
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Attributes");

		LayoutInflater infl = LayoutInflater.from(this);
		View view = infl.inflate(R.layout.attribute_dialog, null);

		b.setView(view);
		
		EditText te = (EditText)view.findViewById(R.id.editText1);
		te.setText(String.valueOf(lineAttributes.lineWidth));
				
		LineStyleImageView btnLineStyle = 
			(LineStyleImageView)view.findViewById(R.id.imageViewLineStyle);
		btnLineStyle.setOnClickListener(new LineStyleOnClickListener());
		btnLineStyle.setCurrentLinestyle(lineAttributes.lineStyle);
		
		LineColorImageView btnLineColor = 
			(LineColorImageView)view.findViewById(R.id.imageViewLineColor);
		btnLineColor.setOnClickListener(new LineColorOnClickListener());
		btnLineColor.setCurrentLinecolor(lineAttributes.lineColor);
		
		// Enable OK and Cancel button but override onClickListeners later
		// if required to prevent dialog from closing.
		b.setPositiveButton("OK", null);
		b.setNegativeButton("Cancel", null);
		
		dlg = b.create();

		// It is important to use the below command instead of Builder.show
		// otherwise dlg.getButton would return null. Also the onClickListener
		// only works if registered AFTER dlg.show.
		dlg.show();

		// Set a special listener to the OK button to check the values
		// of the dialog before dismissing it. If something is wrong
		// (i.e. invalid line width) the dialog stays open.
		dlg.getButton(AlertDialog.BUTTON_POSITIVE)
		 	.setOnClickListener(new OkOnClickListener());
	}
	
	// The below method gets called when the OK button of the attribute dialog
	// gets pressed. It checks the attributes and closes the dialog if 
	// all values are in range. Otherwise the dialog stays open and a short
	// error message is displayed to the user via Toast.
	private final class OkOnClickListener implements View.OnClickListener 
	{
		public void onClick(View view) 
		{
			// Current line style
			LineStyleImageView btnLineStyle = 
					(LineStyleImageView)dlg.findViewById(R.id.imageViewLineStyle);
			lineAttributes.lineStyle = btnLineStyle.getCurrentLineStyle();
			
			EditText lineWidthText = (EditText)dlg.findViewById(R.id.editText1);
			int lineWidth = Integer.parseInt(lineWidthText.getText().toString());

			// take advantage of lazy evaluation
			if(lineWidth < MIN_LINE_WIDTH || lineWidth > MAX_LINE_WIDTH)
			{
				Toast toast = 		
						Toast.makeText(getApplicationContext(), 
										"Line width must be between " + 
										String.valueOf(MIN_LINE_WIDTH) + 
										" and " +
										String.valueOf(MAX_LINE_WIDTH), 
										Toast.LENGTH_LONG);
				
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();
				
				return;
			}
			else
			{
				lineAttributes.lineWidth = lineWidth;
			}

			// Current line color
			LineColorImageView btnLineColor = 
					(LineColorImageView)dlg.findViewById(R.id.imageViewLineColor);

			lineAttributes.lineColor = btnLineColor.getCurrentLineColor();
			lineAttributes.textColor = btnLineColor.getCurrentTextColor(); // text for numbers in numbered arrows
			
			dlg.dismiss();
			
			// If the attribute dialog got called via interaction with an
			// overlay shape 
			if(changeAttributeOfSelected)
			{
		    	overlayImage.changeAttributesOfSelectedShape(lineAttributes);
				
				changeAttributeOfSelected = false;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mOptionsMenu = menu;
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.annotate_edit_image_option, menu);
		
		// enable all menu entries by default
		menu.findItem(R.id.actionAddShape).setEnabled(true);
		menu.findItem(R.id.actionDeleteSelected).setEnabled(false);
		menu.findItem(R.id.actionAttributesSelected).setEnabled(false);
		menu.findItem(R.id.actionAnnotateSelected).setEnabled(false);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	// Accept or Cancel selected from  menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean success = false;
		
    	switch (item.getItemId()) 
    	{
        	case R.id.actionAccept:
        		// The user accepted all annotations. Now create a clsAnnotationData object and send it back
        		// to ActivityAnnotateImage.
		    			    	
		    	// clear all annotation elements
	        	data.clear(); 
		    	
	        	// fill elements
	        	overlayImage.extractAnnotations(data);

	        	// update Intent
	        	Intent objIntent = getIntent();
	        	objIntent.putExtra(clsAnnotationData.DATA, clsUtils.SerializeToString(data));
	        	
	        	// return to caller
            	setResult(RESULT_OK, objIntent);
            	finish();

        		break;
        		
        	case R.id.actionCancel:
        		setResult(RESULT_CANCELED);
        		finish();
        		break;
        		
		    case R.id.actionDeleteSelected:
		    	overlayImage.deleteSelectedShape();
		    	
		        success = true;
        		break;
		         
		    case R.id.actionAttributesSelected:
		    	// Call the onClick method of the Overlay Attribute button
		    	// because that will create and show the dialog.
		    	Button btn = (Button)findViewById(R.id.buttonAttribute);
		    	btn.callOnClick();

		    	changeAttributeOfSelected = true;

		    	success = true;
        		break;
		         
		    case R.id.actionAnnotateSelected:
		    	// Retrieve current annotation of selected object, create Intent and start
		    	// Activity which let user change the annotation
		    	String description = overlayImage.getDescriptionOfSelectedObject();
		    	if(!description.isEmpty())
		    	{
					Intent editTextIntent = new Intent(this, ActivityEditAnnotationText.class);
					
					// create temporary container for the description
					clsAnnotationData.clsAnnotationItem temp = data.new clsAnnotationItem();
					temp.setAnnotationText(description);
					
					editTextIntent.putExtra(clsAnnotationData.DATA, clsUtils.SerializeToString(temp));

					startActivityForResult(editTextIntent, 0);
		    	}
		    	
		        success = true;
        		break;

		    // shape Arrow
		    case R.id.actionAddArrow:
		    	overlayImage.addArrowOverlay(data.new AttributeContainer(lineAttributes));

		    	success = true;
        		break;
		         
			    // shape Numbered Arrow
		    case R.id.actionAddNumberedArrow:
		    	overlayImage.addNumberedArrowOverlay(data.new AttributeContainer(lineAttributes));

		        success = true;
        		break;
		         
			    // shape Rectangle
		    case R.id.actionAddRectangle:
		    	overlayImage.addRectangleOverlay(data.new AttributeContainer(lineAttributes));

		    	success = true;
        		break;
	            
		    default:
		    	return super.onOptionsItemSelected(item);
    	}
    	
		return success;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == Activity.RESULT_OK)
		{	
			Bundle objBundle = data.getExtras();
			
			clsAnnotationData.clsAnnotationItem temp =
				clsUtils.DeSerializeFromString(objBundle.getString(clsAnnotationData.DATA), 
							   					clsAnnotationItem.class);
			
			overlayImage.changeDescriptionOfSelectedShape(temp.getAnnotationText());
		}
	}
	
	private void loadImage(String origFile, int compression)
	{
		String[] input = {origFile};
		
		pd = ProgressDialog.show(this, "Processing...", "Please wait", 
									true, true, null);
		
		new LoadImage(this, compression).execute(input);
	}
	
	// The below class handles events on the LineStyle
	public static class LineStyleImageView extends ImageView
	{		
		private boolean notified = false;
		private clsUtils.Linestyle currentLS = clsUtils.Linestyle.LS_SOLID;
		
		private Paint paint;
		private Path  path;
		
		public LineStyleImageView(Context context) {
			super(context);
			
			paint = new Paint();
			path  = new Path();
		}
		
		public LineStyleImageView(Context context, AttributeSet attrs) {
			super(context, attrs);
			
			paint = new Paint();
			path  = new Path();
		}
		
		public LineStyleImageView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			
			paint = new Paint();
			path  = new Path();
		}
		
		// Inform the object that the next redraw originates from an internal
		// onClick. The object will then cycle through the line styles instead
		// of redrawing the current.
		public void setClick()
		{
			notified = true;
		}
		
		// necessary because the dialog gets destroyed when it closes
		public void setCurrentLinestyle(clsUtils.Linestyle ls)
		{
			currentLS = ls;
		}
		
		public clsUtils.Linestyle getCurrentLineStyle()
		{
			return currentLS;
		}
		
		@Override
		public void onDraw(Canvas canvas)
		{
			boolean found = false;
		
			super.onDraw(canvas);
			
			if(notified)
			{
				for(clsUtils.Linestyle ls : clsUtils.Linestyle.values())
				{
					if(found)
					{
						if(ls == clsUtils.Linestyle.LS_RUBBERBAND)
						{
							currentLS = clsUtils.Linestyle.LS_SOLID;
						}
						else
						{
							currentLS = ls;
						}

						break;
					}

					if(ls == currentLS)
					{
						found = true;
					}
				}
				
				notified = false;
			}
			
			// draw current line style to button
			paint.setStyle(Style.STROKE);
			paint.setColor(Color.RED);
			paint.setStrokeWidth(5);
			paint.setPathEffect(clsUtils.getPathEffect(currentLS));

			// Empty canvas by drawing background color
			canvas.drawColor(Color.LTGRAY);

			// Note: canvas.drawLine seems to ignore paint.setPathEffect.
			// That is why we need to create a Path for a simple line
			path.moveTo(0,          getHeight()/2);
			path.lineTo(getWidth(), getHeight()/2);

			canvas.drawPath(path, paint);
		}
	}
	
	// The below class handles events on the LineColor
	public static class LineColorImageView extends ImageView
	{		
		private Context context;
		
		private boolean notified = false;
		private int currentColor;
		private int currentTextColor;
		
		private int[][] cfgColors;
		
		public LineColorImageView(Context context) {
			super(context);
			
			this.context = context;
			
			initialiseColors();
		}
		
		public LineColorImageView(Context context, AttributeSet attrs) {
			super(context, attrs);
			
			this.context = context;
			
			initialiseColors();
		}
		
		public LineColorImageView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			
			this.context = context;
			
			initialiseColors();
		}

		private void initialiseColors()
		{
			String[] configuredColors = context.getResources().getStringArray(R.array.lineColor);
			
			if(configuredColors.length == 0)
			{
				cfgColors = new int[1][2];
				cfgColors[0][0] = Color.RED;
				cfgColors[0][1] = Color.BLACK;
			}
			else
			{
				int numColors = configuredColors.length;
				
				cfgColors = new int[numColors][2];
				
 				for(int i = 0; i < numColors; i++)
				{
					// Parse string to get the line color and text color
					String[] col = configuredColors[i].split(",");
					cfgColors[i][0] = Color.parseColor(col[0]);
					cfgColors[i][1] = Color.parseColor(col[1]);
				}
				
			}
		}
		
		// Inform the object that the next redraw originates from an internal
		// onClick. The object will then cycle through the line colors instead
		// of redrawing the current.
		public void setClick()
		{
			notified = true;
		}
		
		// necessary because the dialog gets destroyed when it closes
		public void setCurrentLinecolor(int color)
		{
			currentColor = color;
			
			int i;
			for(i = 0; i < cfgColors.length; i++)
			{
				if(cfgColors[i][0] == currentColor)
				{
					currentTextColor = cfgColors[i][1];
					break;
				}
			}
		}
		
		public int getCurrentLineColor()
		{
			return currentColor;
		}
		
		public int getCurrentTextColor()
		{
			return currentTextColor;
		}
		
		@Override
		public void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
Log.d("<<onDraw>>", String.valueOf(currentColor));			
			if(notified)
			{
				int i;
				for(i = 0; i < cfgColors.length; i++)
				{
					if(cfgColors[i][0] == currentColor)
					{
						Log.d("<<break>>", " ");			
						break;
					}
				}
				// Select next color considering wrap
				i = (i >= (cfgColors.length - 1))?0:++i;
				currentColor     = cfgColors[i][0];
				currentTextColor = cfgColors[i][1];

				notified = false;
			}

			// change background color of button
			canvas.drawColor(currentColor);
		}
	}

	private final class LineStyleOnClickListener implements View.OnClickListener 
	{
		public void onClick(View view) 
		{
			// inform the view that the next redraw originates from a button click
			((LineStyleImageView)view).setClick();
			
			// invalidate view to force redraw
			view.invalidate();
		}
	}

	private final class LineColorOnClickListener implements View.OnClickListener 
	{
		public void onClick(View view) 
		{
			// inform the view that the next redraw originates from a button click
			((LineColorImageView)view).setClick();
			
			// invalidate view to force redraw
			view.invalidate();
		}
	}
	
	// Class handling events on the scale to select the compression of the image
	private final class CompressBarChangeListener implements SeekBar.OnSeekBarChangeListener
	{
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			TextView textViewCompressRate = (TextView)findViewById(R.id.textViewCompression);
			textViewCompressRate.setText(String.valueOf(progress * 10) + " %");
		}

		public void onStartTrackingTouch(SeekBar seekBar)
		{
			
		}

		public void onStopTrackingTouch(SeekBar seekBar)
		{
			int progress = seekBar.getProgress() * 10;
			
			data.compressionRate = progress;
			
			// Load the image in a specific quality.
			loadImage(data.strLocalImage, data.compressionRate);
		}
	}

	// Async task to load images
	public class LoadImage extends AsyncTask<String, Integer, Bitmap>
	{
		int quality = 100;
		String outFile;
		Context context;
		
		public LoadImage(Context context, int compression) 
		{
			this.context = context;
			
			quality = 100 - compression;
		}
		
		protected Bitmap doInBackground(String...inFile)
		{
			Bitmap imageBitmap = null;

			if(quality == 100)
			{
				// use original file
				outFile = inFile[0];
				
				// Create image which fits the screen from file 
				imageBitmap = clsUtils.downsampleImageToView(outFile, overlayImage.getWidth(), overlayImage.getHeight(), false);
			}
			else
			{
				OutputStream fOutputStream = null;

				// create bitmap from original file
				imageBitmap = BitmapFactory.decodeFile(inFile[0]);

				// Create of temporary file. Note: outFile is only necessary because
				// no means is known to compress a bitmap without writing it to a 
				// file. 
				outFile = inFile[0].substring(0, inFile[0].lastIndexOf('/'));
				outFile = outFile + "/downsampled.jpg";
				
	            try {
					fOutputStream = new FileOutputStream(new File(outFile));

					imageBitmap.compress(CompressFormat.JPEG, quality, fOutputStream);
					
					fOutputStream.flush();
					fOutputStream.close();
					
					imageBitmap.recycle();
					imageBitmap = clsUtils.downsampleImageToView(outFile, overlayImage.getWidth(), overlayImage.getHeight(), false);

	            } catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return imageBitmap;
		}
		
		@Override
		protected void onPreExecute()
		{

		}

		@Override 
		protected void onPostExecute(Bitmap p)
		{
			overlayImage.setImageBitmap(p);
			
			// The overlay image only gets initialised once
			if(!imageLoaded)
			{
				overlayImage.setShapeObserver(shapeObserver);
				overlayImage.startMatrixOperation();
				overlayImage.createShapes(data);
				
				imageLoaded = true;
			}

			// calculate initial size of image
			long size = new File(outFile).length();
			
			float cvrt = ((float)size)/1024f; String unit = "Kb";
			if(cvrt > 1024)
			{
				cvrt = ((float)size)/(1024f * 1024f); unit = "Mb";
			}
			TextView textViewFileSize = (TextView)findViewById(R.id.textViewFileSize);
			textViewFileSize.setText(String.format("%.2f %s", Float.valueOf(cvrt), unit));

			// Create default line width of shapes
			float w = overlayImage.getBitmapWidth();
			float defaultWidthDivisor;
			try {
				defaultWidthDivisor = (float)getResources().getInteger(R.integer.line_width_divisor);
			} catch (NotFoundException e) {
				defaultWidthDivisor = 100f;
			}
			// safety check
			if(defaultWidthDivisor <= 0)
				defaultWidthDivisor = 100f;
			
			int defaultLineWidth = (int)(w/defaultWidthDivisor);
			
			// create Attribute container with default values
			lineAttributes = data.new AttributeContainer(defaultLineWidth, 
													clsUtils.Linestyle.LS_SOLID,
													Color.RED, Color.WHITE);		

			pd.dismiss();
		}
	}

// in LoadFile call invalidateOptionsMenu to set mOptionsMenu
}
