package com.treeapps.treenotes;

import java.util.List;


import com.treeapps.treenotes.ActivityNoteAddNew.clsArrowsListViewState;
import com.treeapps.treenotes.sharing.ActivityAllMembers.clsAllMembersListViewState;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class clsActivityNoteAddNewArrayAdapter extends
		ArrayAdapter<clsArrowsListViewState> {

	public clsActivityNoteAddNewArrayAdapter(Context context, int resource,
			List<clsArrowsListViewState> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.objects = objects;
	}

	private final Context context;
	private final List<clsArrowsListViewState> objects;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ActivityNoteAddNew.clsArrowsListViewState objListViewState = objects
				.get(position);
		View rowView = inflater.inflate(R.layout.arrows_list_item, parent,
				false);
		TextView textView = (TextView) rowView
				.findViewById(R.id.arrows_description);
		textView.setText(objListViewState.strArrowDescription);
		textView.setTag(objListViewState);

		CheckBox objCheckBox = (CheckBox) rowView
				.findViewById(R.id.arrows_checkbox);
		objCheckBox.setChecked(objListViewState.boolIsChecked);
		objCheckBox.setTag(objListViewState);
		objCheckBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CheckBox cb = (CheckBox) v;
				ActivityNoteAddNew.clsArrowsListViewState objChangedListViewState = (ActivityNoteAddNew.clsArrowsListViewState) v
						.getTag();
				for (clsArrowsListViewState objListViewState : objects) {
					if (objListViewState.equals(objChangedListViewState)) {
						objListViewState.boolIsChecked = cb.isChecked();
					} else {
						objListViewState.boolIsChecked = false;
					}
				}
				ActivityNoteAddNew.objListView.invalidateViews();
			}
		});

		return rowView;
	}
}
