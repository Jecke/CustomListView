package com.example.spacesavertreeview.sharing.subscriptions;

import java.util.List;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.sharing.ActivityGetUserForGroups;
import com.example.spacesavertreeview.sharing.ActivityGetUserForGroups.clsGetUsersForGroupsListViewState;
import com.example.spacesavertreeview.sharing.subscriptions.ActivitySubscriptionSearch.clsListViewStateSearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class clsActivitySubscriptionSearchArrayAdapter extends ArrayAdapter<clsListViewStateSearch> {
	
	private final Context context;
	private final List<clsListViewStateSearch> objects;
	int resource;

	public clsActivitySubscriptionSearchArrayAdapter(Context context,int resource, List<clsListViewStateSearch> objects) {
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
		
		clsListViewStateSearch objListViewState = objects.get(position);
	    TextView textView = (TextView) todoView.findViewById(R.id.get_publication_item);
	    textView.setText(objListViewState.strUserName + "/" + objListViewState.strNoteName);
	    
	    CheckBox objCheckBox = (CheckBox) todoView.findViewById(R.id.get_publication_checkbox);
	    objCheckBox.setTag(objListViewState);
	    objCheckBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 CheckBox cb = (CheckBox) v;
				 ActivitySubscriptionSearch.clsListViewStateSearch objListViewState = (ActivitySubscriptionSearch.clsListViewStateSearch)v.getTag();
				 objListViewState.boolIsChecked = cb.isChecked();
			}
		});
	    
	    
	    return todoView;
	}

}
