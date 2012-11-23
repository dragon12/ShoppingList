package model;

import java.util.List;

import com.android.gers.shopping.list.ListViewStatusChangeListener;
import com.android.gers.shopping.list.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ItemArrayAdapter 
		extends BaseItemArrayAdapter {
	
	public ItemArrayAdapter(Context context, ListViewStatusChangeListener listenerCb, int textViewResourceId, List<ShoppingListItem> results) {
		super(context, listenerCb, textViewResourceId, results);
		
	}
	
	@Override
	protected void populateRow(View v, ShoppingListItem item, int position) {
		
		if (item != null) {
			TextView itemQuantity = (TextView) v.findViewById(R.id.item_row_item_quantity);
			TextView itemName = (TextView) v.findViewById(R.id.item_row_item_name);
			CheckBox itemComplete = (CheckBox) v.findViewById(R.id.item_row_item_complete);

			if (itemQuantity != null) {
				String displayName = item.getQuantityType().getDisplayName();
				itemQuantity.setText(item.getQuantity() + (displayName.length() > 0 ? " " + displayName : ""));
			}
			
			if (itemName != null){
				itemName.setText(item.getDisplayName());
			}
			
			if (itemComplete != null) {
				itemComplete.setChecked(item.getComplete());
		
				final int thisPosition = position;
				itemComplete.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						CheckBox box = (CheckBox)v.findViewById(R.id.item_row_item_complete);
						listenerCb.statusChanged(thisPosition, v, box.isChecked()); 
					}
				});
			}
			
		}
	}

}
