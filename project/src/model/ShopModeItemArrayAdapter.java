package model;

import java.util.List;

import com.android.gers.shopping.list.ListViewStatusChangeListener;
import com.android.gers.shopping.list.R;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ShopModeItemArrayAdapter 
		extends BaseItemArrayAdapter {
	
	public ShopModeItemArrayAdapter(Context context, ListViewStatusChangeListener listenerCb, int textViewResourceId, List<ShoppingListItem> results) {
		super(context, listenerCb, textViewResourceId, results);
		
	}
	
	@Override
	protected void populateRow(View v, ShoppingListItem item, int position) {
		
		if (item != null) {
			TextView itemQuantity = (TextView) v.findViewById(R.id.item_row_item_quantity);
			TextView itemName = (TextView) v.findViewById(R.id.item_row_item_name);
			Button itemDone = (Button)v.findViewById(R.id.item_row_item_done);
			
			if (itemQuantity != null) {
				String displayName = item.getQuantityType().getDisplayName();
				StringBuilder displayQty = new StringBuilder(item.getQuantity().toString());
				if (displayQty.substring(displayQty.length() - 2).equals(".0")) {
					displayQty = displayQty.delete(displayQty.length() - 2, displayQty.length());
				}
				if (displayName.length() > 0) {
					displayQty = displayQty.append(" ").append(displayName); 
				}
				
				itemQuantity.setText(displayQty.toString());
			}
			
			if (itemName != null){
				itemName.setText(item.getDisplayName());
			}
			
			if (itemDone != null) {
				final int thisPosition = position;
				itemDone.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						//Button box = (Button)v.findViewById(R.id.item_row_item_done);
						listenerCb.statusChanged(thisPosition, v, true); 
					}
				});
			}
		}
	}

}
