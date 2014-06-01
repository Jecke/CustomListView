package com.treeapps.treenotes;

import java.util.ArrayList;
import java.util.List;


import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsMessaging.clsMsgUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class clsGetUsersArrayAdapter  extends ArrayAdapter<clsMsgUser> {
	
		 public clsGetUsersArrayAdapter(Context context, int resource,	ArrayList<clsMsgUser> objects) {
				super(context, resource, objects);
				// TODO Auto-generated constructor stub
				this.context = context;
				this.objects = objects;
		}

		private final Context context;
		private final List<clsMsgUser> objects;
		  
		  

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		    LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowView = inflater.inflate(R.layout.get_users_list_item, parent, false);
		    TextView textView = (TextView) rowView.findViewById(R.id.get_users_item);
		    clsMessaging.clsMsgUser objMsgUser = objects.get(position);
		    textView.setText(objMsgUser.getStrUserName());
		    textView.setTag(objMsgUser);
		    return rowView;
		  }
		} 
