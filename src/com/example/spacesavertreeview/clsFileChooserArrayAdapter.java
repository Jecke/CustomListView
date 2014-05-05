package com.example.spacesavertreeview;

import java.util.List; 
 
import android.content.Context; 
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup; 
import android.widget.ArrayAdapter;
import android.widget.ImageView; 
import android.widget.TextView;


public class clsFileChooserArrayAdapter extends ArrayAdapter<clsItem>{

	private Context c;
	private int id;
	private List<clsItem>items;
	
	public clsFileChooserArrayAdapter(Context context, int textViewResourceId,
			List<clsItem> objects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
		
		
		
		
	}
	public clsItem getItem(int i)
	 {
		 return items.get(i);
	 }
	 @Override
       public View getView(int position, View convertView, ViewGroup parent) {
               View v = convertView;
               if (v == null) {
                   LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                   v = vi.inflate(id, null);
               }
                             
               
               /* create a new view of my layout and inflate it in the row */
       		//convertView = ( RelativeLayout ) inflater.inflate( resource, null );
       		
               final clsItem o = items.get(position);
               if (o != null) {
                       TextView t1 = (TextView) v.findViewById(R.id.label_subscriptions);
                       t1.setOnLongClickListener(new OnLongClickListener() {
           				
		   						@Override
		   						public boolean onLongClick(View arg0) {
		   							// TODO Auto-generated method stub
		   							return false;
		   						}
		   			    });
                       TextView t2 = (TextView) v.findViewById(R.id.TextView02);
                       TextView t3 = (TextView) v.findViewById(R.id.TextViewDate);
                       /* Take the ImageView from layout and set the city's image */
	               		ImageView imageCity = (ImageView) v.findViewById(R.id.fd_Icon1);
	               		String uri = "drawable/" + o.getImage();
	               	    int imageResource = c.getResources().getIdentifier(uri, null, c.getPackageName());
	               	    Drawable image = c.getResources().getDrawable(imageResource);
	               	    imageCity.setImageDrawable(image);
                       
                       if(t1!=null)
                       	t1.setText(o.getName());
                       if(t2!=null)
                          	t2.setText(o.getData());
                       if(t3!=null)
                          	t3.setText(o.getDate());
                       
               }
               return v;
       }

}
