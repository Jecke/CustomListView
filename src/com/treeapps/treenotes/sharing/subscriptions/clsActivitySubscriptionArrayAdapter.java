package com.treeapps.treenotes.sharing.subscriptions;

import java.util.ArrayList;



import com.treeapps.treenotes.R;
import com.treeapps.treenotes.sharing.subscriptions.ActivitySubscriptions.clsListViewState;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class clsActivitySubscriptionArrayAdapter extends ArrayAdapter<clsListViewState> {
	
	private final ActivitySubscriptions context;
	private final ArrayList<clsListViewState> objects;
	int resource;

	public clsActivitySubscriptionArrayAdapter(ActivitySubscriptions context,int resource, ArrayList<clsListViewState> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.objects = objects;
		this.resource = resource;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		RelativeLayout todoView;
		if (convertView == null) {
		      todoView = new RelativeLayout(getContext());
		      String inflater = Context.LAYOUT_INFLATER_SERVICE;
		      LayoutInflater li;
		      li = (LayoutInflater)getContext().getSystemService(inflater);
		      li.inflate(resource, todoView, true);
	    } else {
	    	todoView = (RelativeLayout) convertView;
	    }
		
		clsListViewState objListViewState = objects.get(position);
	    TextView textView = (TextView) todoView.findViewById(R.id.get_publication_item);
	    textView.setText(objListViewState.strUserName + "/" + objListViewState.strNoteName);
	    
	    CheckBox objCheckBox = (CheckBox) todoView.findViewById(R.id.get_publication_checkbox);
	    objCheckBox.setTag(objListViewState);
	    objCheckBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 CheckBox cb = (CheckBox) v;
				 ActivitySubscriptions.clsListViewState objListViewState = (ActivitySubscriptions.clsListViewState)v.getTag();
				 objListViewState.boolIsChecked = cb.isChecked();
			}
		});
	    
	    
	    return todoView;
	}

}
