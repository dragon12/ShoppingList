package model;

import java.util.List;

import com.android.gers.shopping.list.ListViewStatusChangeListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class BaseItemArrayAdapter 
		extends ArrayAdapter<ShoppingListItem> {
	private List<ShoppingListItem> shoppingListItems;
	private int textViewResourceId;
	
	protected ListViewStatusChangeListener listenerCb;
	
	public BaseItemArrayAdapter(Context context, ListViewStatusChangeListener listenerCb, int textViewResourceId, List<ShoppingListItem> results) {
		super(context, textViewResourceId, results);
		this.textViewResourceId = textViewResourceId;

		this.shoppingListItems = results;
		this.listenerCb = listenerCb;
	}

	protected abstract void populateRow(View v, ShoppingListItem item, int position);
	
	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(textViewResourceId, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		ShoppingListItem i = shoppingListItems.get(position);
		if (i != null) {
			populateRow(v, i, position);
		}
		
		// the view must be returned to our activity
		return v;
	}

}
