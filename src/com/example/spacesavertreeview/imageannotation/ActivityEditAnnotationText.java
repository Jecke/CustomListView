package com.example.spacesavertreeview.imageannotation;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsUtils;
import com.example.spacesavertreeview.imageannotation.clsAnnotationData.clsAnnotationItem;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;

// This activity handles annotation texts (at the moment these are tests associated with numbered arrows)
public class ActivityEditAnnotationText extends Activity {

	// static Id annotation text (i.e. Arrow x)
	private String annotationId;
	
	// container used to receive and send text 
	private clsAnnotationItem cnt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		annotationId = "";
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_edit_annotation_text);
		
		Intent objIntent = getIntent();
		Bundle objBundle = objIntent.getExtras();   
		
		cnt = 
			clsUtils.DeSerializeFromString(objBundle.getString(clsAnnotationData.DATA), 
										   clsAnnotationItem.class);
		
		String strResult = cnt.getAnnotationText();
		
		// parse the annotation text and use the text from the start to the first colon
		// as description above the actual text field
		// Note: That text should be 'Arrow x' at the moment but we accept everything up to the
		// first colon for easier future changes.
		TextView id = (TextView)findViewById(R.id.annotationId);
		
		int idx = strResult.indexOf(": ");
		if(idx != -1)
		{
			annotationId = strResult.substring(0, idx);

			// display the remaining text in the editable text field
			String annotationText = strResult.substring(idx + 2);
			
			EditText editText = (EditText)findViewById(R.id.editTextNoteName);
			editText.setText(annotationText);
		}
		
		id.setText(annotationId + ":");
		
		// pop up soft keyboard
		clsUtils.showKeyboard(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_new_or_edit, menu);
		return true;
	}
	
	// Accept or Cancel selected from  menu
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Intent objIntent = getIntent();
		
    	switch (item.getItemId()) 
    	{
        	case R.id.actionAccept:
        		EditText objEditView    = (EditText)findViewById(R.id.editTextNoteName);
        		String   strDescription = objEditView.getText().toString();

        		// put Information for MainActivity together
        		cnt.setAnnotationText(annotationId + ": " + strDescription);

        		objIntent.putExtra(clsAnnotationData.DATA, clsUtils.SerializeToString(cnt));

        		setResult(RESULT_OK, objIntent);

        		ActivityEditAnnotationText.this.finish();

        		return true;
        	 
        	case R.id.actionCancel:
        		objIntent.putExtra(clsAnnotationData.DESCRIPTION, "");
        		setResult(RESULT_CANCELED, objIntent);
        		ActivityEditAnnotationText.this.finish();
        		return true;
             
        	default:
        		return super.onOptionsItemSelected(item);
    	}
	}
	
	public static void showErrorDialog(Context context, int textId, boolean cancelable)
	{
	 	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	 	
	    builder.setMessage(textId);
	    builder.setCancelable(cancelable);
	    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    {
            public void onClick(DialogInterface dialog, int id) {}
        });
	    
	    AlertDialog dialog = builder.create();
	    dialog.show();
	}
}