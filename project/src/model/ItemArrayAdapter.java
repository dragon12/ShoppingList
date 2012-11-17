package model;

import java.util.List;

import com.android.gers.shopping.list.ListViewCheckBoxListener;
import com.android.gers.shopping.list.R;
import com.android.gers.shopping.list.ShoppingList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ItemArrayAdapter extends ArrayAdapter<ShoppingListItem> {
	private List<ShoppingListItem> shoppingListItems;
	private ListViewCheckBoxListener listenerCb;

	public ItemArrayAdapter(Context context, ListViewCheckBoxListener listenerCb, int textViewResourceId, List<ShoppingListItem> results) {
		super(context, textViewResourceId, results);

		this.shoppingListItems = results;
		this.listenerCb = listenerCb;
	}

	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.item_row, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		ShoppingListItem i = shoppingListItems.get(position);
		Log.i(ShoppingList.LOG_NAME, String.format("dealing with position %d, item %s", position, i.toString()));
		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			TextView itemQuantity = (TextView) v.findViewById(R.id.item_row_item_quantity);
			TextView itemName = (TextView) v.findViewById(R.id.item_row_item_name);
			CheckBox itemComplete = (CheckBox) v.findViewById(R.id.item_row_item_complete);

			if (itemQuantity != null) {
				String displayName = i.getQuantityType().getDisplayName();
				itemQuantity.setText(i.getQuantity() + (displayName.length() > 0 ? " " + displayName : ""));
			}
			
			if (itemName != null){
				Boolean pluralRequired = (i.getQuantityType() == QuantityType.UNITS && !i.getName().endsWith("s"));
				itemName.setText(i.getName() + (pluralRequired ? "s" : ""));
			}
			
			if (itemComplete != null) {
				Log.i(ShoppingList.LOG_NAME, "before itemComplete.setChecked(" + i.getComplete() + ")");
				itemComplete.setChecked(i.getComplete());
				Log.i(ShoppingList.LOG_NAME, "after itemComplete.setChecked(" + i.getComplete() + ")");

				final int thisPosition = position;
				itemComplete.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						CheckBox box = (CheckBox)v.findViewById(R.id.item_row_item_complete);
						listenerCb.checkBoxChanged(thisPosition, null, box.isChecked()); 
					}
				});
			}
			
		}

		// the view must be returned to our activity
		return v;
	}

}
