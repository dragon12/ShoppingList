package com.android.gers.shopping.list;

import java.util.List;

import model.BaseItemArrayAdapter;
import model.ShopModeItemArrayAdapter;
import model.ShoppingListItem;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class ShoppingListShoppingMode 
	extends BaseListItemsActivity 
	implements OnClickListener {

	public ShoppingListShoppingMode() {
		super(R.layout.activity_shopping_list_shopping_mode, R.menu.activity_shopping_list_shopping_mode, false);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Button undoButton = (Button) findViewById(R.id.button_undo);
        undoButton.setOnClickListener(this);
    }
    
    private void undoButtonClicked() {
    	Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected BaseItemArrayAdapter createAdapter(List<ShoppingListItem> items) {
    	return new ShopModeItemArrayAdapter(this, this, R.layout.shop_mode_item_row, items);
    };
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

	public void statusChanged(int listPosition, View triggeredView, boolean isOn) {
		ShoppingListItem itemChosen = (ShoppingListItem)getListView().getItemAtPosition(listPosition);
		
		//TODO: implement undo
		itemChosen.setComplete(true);
		updateItemInDb(itemChosen);
		
		final ShoppingListItem itemToAnimate = itemChosen;
		
		Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
		anim.setDuration(500);
		getListView().getChildAt(listPosition).startAnimation(anim);

		new Handler().postDelayed(new Runnable() {
			public void run() {
				//and remove this item from our list
				@SuppressWarnings("unchecked")
				ArrayAdapter<ShoppingListItem> adapter = (ArrayAdapter<ShoppingListItem>)getListAdapter();
				adapter.remove(itemToAnimate);
			}
		}, anim.getDuration());
	}

	@Override
	protected void listItemClicked(ShoppingListItem itemClicked, int position) {
		Toast.makeText(this, "list item clicked", Toast.LENGTH_SHORT).show();
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_undo:
			undoButtonClicked();
			break;
			
		default:
			//nothing
			break;
		}
	}
	
}
