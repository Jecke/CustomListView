package com.treeapps.treenotes.sharing;

import java.util.List;



import com.treeapps.treenotes.R;
import com.treeapps.treenotes.sharing.ActivityGetUserForGroups.clsGetUsersForGroupsListViewState;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class clsGetUserForGroupsArrayAdapter  extends ArrayAdapter<clsGetUsersForGroupsListViewState> {
	
		 public clsGetUserForGroupsArrayAdapter(Context context, int resource,	List<clsGetUsersForGroupsListViewState> objects) {
				super(context, resource, objects);
				// TODO Auto-generated constructor stub
				this.context = context;
				this.objects = objects;
		}

		private final Context context;
		private final List<clsGetUsersForGroupsListViewState> objects;
		  
		  

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    
		    ActivityGetUserForGroups.clsGetUsersForGroupsListViewState objListViewState = objects.get(position);
		    View rowView = inflater.inflate(R.layout.get_users_list_item, parent, false);
		    TextView textView = (TextView) rowView.findViewById(R.id.get_users_item);
		    clsMessaging.clsMsgUser objMsgUser = objListViewState.objMsgUser;
		    textView.setText(objMsgUser.getStrUserName());
		    textView.setTag(objListViewState);
		    
		    CheckBox objCheckBox = (CheckBox) rowView.findViewById(R.id.get_users_checkbox);
		    objCheckBox.setTag(objListViewState);
		    objCheckBox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 CheckBox cb = (CheckBox) v;
					 ActivityGetUserForGroups.clsGetUsersForGroupsListViewState objListViewState = ( ActivityGetUserForGroups.clsGetUsersForGroupsListViewState) v.getTag();
					 objListViewState.boolIsChecked = cb.isChecked();
				}
			});

		    return rowView;
		  }
		} 
