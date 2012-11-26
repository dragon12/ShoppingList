package com.android.gers.shopping.list;

import java.util.List;

import com.android.gers.shopping.list.DB.DbTableItems;
import com.android.gers.shopping.list.DB.ShoppingListDb;

import model.BaseItemArrayAdapter;
import model.ListDataSource;
import model.ShoppingListItem;
import model.ShoppingListList;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public abstract class BaseListItemsActivity 
		extends ListActivity 
		implements ListViewStatusChangeListener {

	protected ListDataSource dataSource;
	protected ShoppingListDb dbHelper;

	protected long originatingListId;
	
	private int viewResourceId;
	private int menuResourceId;
	private boolean includeComplete;

	public BaseListItemsActivity(int activityViewResourceId, int menuResourceId, boolean includeComplete) {
		this.viewResourceId = activityViewResourceId;
		this.menuResourceId = menuResourceId;
		this.includeComplete = includeComplete;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(ShoppingList.LOG_NAME, "onCreate called");
		super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(viewResourceId);

		if (savedInstanceState != null) {
			Log.d(ShoppingList.LOG_NAME, "saved state non-null");
		}

		this.getListView().setDividerHeight(1);

		registerForContextMenu(getListView());

		dbHelper = new ShoppingListDb(this);

		dataSource = new ListDataSource(this, dbHelper);
		dataSource.open();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(ShoppingList.LOG_NAME, "onPause called");
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putLong("originatingListId", originatingListId);
		
		editor.commit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(ShoppingList.LOG_NAME, "onResume called");
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(DbTableItems.COL_LIST_ID)) {
				Log.d(ShoppingList.LOG_NAME, "extras does contain our key");
			}

			originatingListId = extras.getLong(DbTableItems.COL_LIST_ID);
			Log.d(ShoppingList.LOG_NAME, "Originating was " + originatingListId);

		} else {
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			long retVal = prefs.getLong("originatingListId", -1);
			if (retVal != -1) {
				originatingListId = retVal;
			}
			else {
				Log.e(ShoppingList.LOG_NAME, "Had no extras in activity!");
				this.finish();
			}
		}
		
		//get the list details
		ShoppingListList list = dataSource.getShoppingList(originatingListId);
		EditText title = (EditText)findViewById(R.id.list_name);
		title.setText(list.getName());

		reloadListDisplay();
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(ShoppingList.LOG_NAME, "onSaveInstanceState called");
		outState.putLong("originatingListId", originatingListId);
		super.onSaveInstanceState(outState);
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Always call the superclass so it can restore the view hierarchy
	    super.onRestoreInstanceState(savedInstanceState);
	   
	    Log.d(ShoppingList.LOG_NAME, "onRestoreInstanceState called");
	}
	
	public void statusChanged(int listPosition, View triggeredView, boolean isOn) {
		// TODO Auto-generated method stub
		
	}


	protected abstract void listItemClicked(ShoppingListItem itemClicked, int position);
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	Log.d(ShoppingList.LOG_NAME, "onListItemClick position " + position + ", id " + id);
    	ShoppingListItem itemClicked = (ShoppingListItem) l.getItemAtPosition(position);
    	
    	listItemClicked(itemClicked, position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(menuResourceId, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case android.R.id.home:
    		NavUtils.navigateUpFromSameTask(this);
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

    protected abstract BaseItemArrayAdapter createAdapter(List<ShoppingListItem> items);
    
    protected void reloadListDisplay() {
    	Log.d(ShoppingList.LOG_NAME, "reloadListDisplay");
        List<ShoppingListItem> items = dataSource.getShoppingListItems(originatingListId, includeComplete);
        
        BaseItemArrayAdapter adapter = createAdapter(items);
        setListAdapter(adapter);
    }
    
	protected Boolean updateItemInDb(ShoppingListItem itemToUpdate) {
		Boolean retVal = true;
		try {
			dataSource.updateItem(itemToUpdate);
		} catch (Exception e) {
			Log.e(ShoppingList.LOG_NAME, "Failed to edit item with exception: " + e.toString());
			Toast.makeText(this, "Failed to edit item", Toast.LENGTH_SHORT).show();
			retVal = false;
		}
		return retVal;
	}
}
