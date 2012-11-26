package com.android.gers.shopping.list;

import java.util.LinkedList;
import java.util.List;

import model.BaseItemArrayAdapter;
import model.QuantityType;
import model.ShopModeItemArrayAdapter;
import model.ShoppingListItem;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ShoppingListShoppingMode 
	extends BaseListItemsActivity 
	implements OnClickListener {

	
	private class DoneCommand {
		public DoneCommand(int listPosition, ShoppingListItem item) {
			this.listPosition = listPosition;
			this.item = item;
		}
		public int getListPosition() { return listPosition; }
		public ShoppingListItem getListItem() { return item; }
		
		private int listPosition;
		private ShoppingListItem item;
	}
	
	LinkedList<DoneCommand> undoStack = new LinkedList<DoneCommand>();
	
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
    	//restore the last item in the undoStack
    	if (undoStack.size() != 0) {
    		DoneCommand toUndo = undoStack.removeLast();
    		
    		final ShoppingListItem itemToRestore = toUndo.getListItem();
    		final int listPosition = toUndo.getListPosition();
    		
    		itemToRestore.setComplete(false);
    		updateItemInDb(itemToRestore);
    		Log.d(ShoppingList.LOG_NAME, "listView.getCount here = " + getListView().getCount());
			@SuppressWarnings("unchecked")
			ArrayAdapter<ShoppingListItem> adapter = (ArrayAdapter<ShoppingListItem>)getListAdapter();
			adapter.insert(itemToRestore, toUndo.getListPosition());
			
    		Log.d(ShoppingList.LOG_NAME, "listView.getCount2 here = " + getListView().getCount());
    		
			if (listPosition > getListView().getLastVisiblePosition() || listPosition < getListView().getFirstVisiblePosition()) {
			    getListView().setSelection(listPosition);
			    
			    final Context ctxt = this;
							    
				getListView().post(new Runnable(){
					  public void run() {
						Log.d(ShoppingList.LOG_NAME, "last position in run is " + getListView().getLastVisiblePosition());
						if (listPosition <= getListView().getLastVisiblePosition()) {
							slideIn(ctxt, listPosition);
						}
						else {
						    Log.d(ShoppingList.LOG_NAME, "unexpected");
						}
					  }});			    
			}
			else { 
				slideIn(this, toUndo.getListPosition());
			}
			Log.d(ShoppingList.LOG_NAME, "last position before is " + getListView().getLastVisiblePosition());

			


			Toast.makeText(this, "Restored \"" + itemToRestore.getName() + "\"", Toast.LENGTH_SHORT).show();
    	}
    	else {
    		Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private void slideIn(Context ctxt, int listPosition) {
    	Animation anim = AnimationUtils.loadAnimation(ctxt, android.R.anim.slide_in_left);
		anim.setDuration(500);
		
		int visiblePosition = listPosition - getListView().getFirstVisiblePosition() - getListView().getHeaderViewsCount();
		getListView().getChildAt(visiblePosition).startAnimation(anim);
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
		
		itemChosen.setComplete(true);
		updateItemInDb(itemChosen);
		undoStack.add(new DoneCommand(listPosition, itemChosen));
		
		final ShoppingListItem itemToAnimate = itemChosen;
		
		Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
		anim.setDuration(500);
		
		int visiblePosition = listPosition - getListView().getFirstVisiblePosition() - getListView().getHeaderViewsCount();
		getListView().getChildAt(visiblePosition).startAnimation(anim);

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
