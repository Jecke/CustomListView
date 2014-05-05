package com.example.spacesavertreeview;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList; 
import java.util.Collections;
import java.util.List;
import java.text.DateFormat; 
import android.os.Bundle; 
import android.app.ListActivity;
import android.content.Intent; 
import android.view.View;
import android.widget.ListView; 

public class ActivityFileChooser extends ListActivity {

	private File currentDir;
    private clsFileChooserArrayAdapter adapter;
    private boolean _boolAddNewFilePromp;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        Bundle objBundle = getIntent().getExtras();    		
		String strStartDir    = objBundle.getString(ActivityExplorerStartup.PATH);
		_boolAddNewFilePromp = objBundle.getBoolean(ActivityExplorerStartup.NEW_FILE_PROMPT);
        currentDir = new File(strStartDir);    
        fill(currentDir); 
    }
    
    private boolean IsTreeNoteFileType(String filename) {
		if(filename.lastIndexOf('.')>0)
           {
              // get last index for '.' char
              int lastIndex = filename.lastIndexOf('.');
              
              // get extension
              String str = filename.substring(lastIndex);
              
              // match path name extension
              if(str.equals(".treenotes"))
              {
                 return true;
              }
           }
           return false;
	}
    private void fill(File f)
    {
    	File[]dirs = f.listFiles(); 
		 this.setTitle("Current Dir: "+f.getName());
		 List<clsItem>dir = new ArrayList<clsItem>();
		 List<clsItem>fls = new ArrayList<clsItem>();
		 try{
			 for(File ff: dirs)
			 { 
				Date lastModDate = new Date(ff.lastModified()); 
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if(ff.isDirectory()){
					
					
					File[] fbuf = ff.listFiles(); 
					int buf = 0;
					if(fbuf != null){ 
						buf = fbuf.length;
					} 
					else buf = 0; 
					String num_item = String.valueOf(buf);
					if(buf == 0) num_item = num_item + " item";
					else num_item = num_item + " items";
					
					//String formated = lastModDate.toString();
					String strFoldername = ff.getName();
					if (strFoldername.indexOf(getResources().getString(R.string.explorer_file_extention)) == 0) {
						dir.add(new clsItem(ff.getName(),num_item,date_modify,ff.getAbsolutePath(),"directory_icon"));
					} else {
						dir.add(new clsItem(ff.getName(),num_item,date_modify,ff.getAbsolutePath(),"file_icon"));
					}
					 
				}
				else
				{
					String strFilename = ff.getName();
					if (IsTreeNoteFileType(strFilename)) {
						// Do not display any files
						// fls.add(new Item(strFilename,ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"file_icon"));
					}		
				}
			 }
		 }catch(Exception e)
		 {    
			 
		 }
		 Collections.sort(dir);
		 if (_boolAddNewFilePromp) {
			 dir.add(0, new clsItem("<" + getResources().getString(R.string.new_file_prompt) + ">","", "", "","file_icon"));
		 }
		 Collections.sort(fls);

		 dir.addAll(fls);
		 if(!f.getName().equalsIgnoreCase("sdcard"))
			 dir.add(0,new clsItem("..","Parent Directory","",f.getParent(),"directory_up"));
		 adapter = new clsFileChooserArrayAdapter(ActivityFileChooser.this,R.layout.file_view,dir);
		 this.setListAdapter(adapter); 
    }
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		clsItem o = adapter.getItem(position);
		if(o.getImage().equalsIgnoreCase("directory_icon")||o.getImage().equalsIgnoreCase("directory_up")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		}
		else
		{
			onFileClick(o);
		}
	}
    
    
    private void onFileClick(clsItem o)
    {
    	//Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
    	Intent intent = new Intent();
        intent.putExtra("GetPath",currentDir.toString());
        intent.putExtra("GetFileName",o.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
    
    
}
