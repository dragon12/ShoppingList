package com.android.gers.shopping.list;

import java.io.IOException;
import java.util.List;

import model.ListArrayAdapter;
import model.ListDataSource;
import model.ShoppingListList;

import com.android.gers.shopping.list.DB.DbTableItems;
import com.android.gers.shopping.list.DB.ShoppingListDb;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ShoppingListActivity 
		extends ListActivity {
	
	private static final int CONTEXT_MENU_DELETE_ID = Menu.FIRST + 1;
	private static final int CONTEXT_MENU_RENAME_ID = CONTEXT_MENU_DELETE_ID + 1;
	private static final int CONTEXT_MENU_CLONE_ID = CONTEXT_MENU_RENAME_ID + 1;
	
	private ListDataSource dataSource;
	private ShoppingListDb dbHelper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list_activity);

        this.getListView().setDividerHeight(2);
        
        registerForContextMenu(getListView());
        
        dbHelper = new ShoppingListDb(this);
        
        Log.i(ShoppingList.LOG_NAME, "about to create data source");
        dataSource = new ListDataSource(this, dbHelper);
        Log.i(ShoppingList.LOG_NAME, "Created data source");
        
        dataSource.open();
        
        updateListDisplay();
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	Log.i(ShoppingList.LOG_NAME, "onListItemClick position " + position + ", id " + id);
    	ShoppingListList listChosen = (ShoppingListList) l.getItemAtPosition(position);
    	Log.i(ShoppingList.LOG_NAME, "corresponds to list: " + listChosen.toString() + "(which has id " + listChosen.getId() + ")");
    	Intent i = new Intent(this, ShoppingListItemsActivity.class);
    	i.putExtra(DbTableItems.COL_LIST_ID, listChosen.getId());
    	
    	startActivityForResult(i, 0);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.add(0, CONTEXT_MENU_RENAME_ID, 0, R.string.rename_list);
    	menu.add(0, CONTEXT_MENU_CLONE_ID, 1, R.string.clone_list);
    	menu.add(0, CONTEXT_MENU_DELETE_ID, 2, R.string.delete_list);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case CONTEXT_MENU_DELETE_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    		ShoppingListList listChosen = (ShoppingListList)getListView().getItemAtPosition(info.position);
    		
    		dataSource.deleteList(listChosen);
    		ListArrayAdapter adapter = (ListArrayAdapter)getListAdapter();
    		adapter.remove(listChosen);
    		
    		return true;
    	case CONTEXT_MENU_RENAME_ID:
    		//display alertdialog to change name
    		return true;
    	case CONTEXT_MENU_CLONE_ID:
    		//display alertdialog to get name for cloned list
    		return true;
    	}
    	return super.onContextItemSelected(item);
    }
    
    private void updateListDisplay() {
        List<ShoppingListList> lists = dataSource.getShoppingLists(true);
        
        ListArrayAdapter adapter = new ListArrayAdapter(this, R.layout.list_row, lists);
        setListAdapter(adapter);    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shopping_list_activity, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
			case R.id.menu_reset_to_test_db:
				resetToTestDb();
				return true;
			
			case R.id.menu_add_list:
				listAddButtonClicked();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
				
		}
		
	}

	private void listAddButtonClicked() {
		Log.i(ShoppingList.LOG_NAME, "listAddButtonClicked");

		AlertDialog.Builder alert = 
				new AlertDialog.Builder(this)
					.setTitle("Add New List")
					.setMessage("List Name");
		
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		alert.setView(input);

		final ShoppingListActivity thisActivity = this;
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				Log.i(ShoppingList.LOG_NAME, "User entered " + value + "!");
				
				ShoppingListList listToAdd = new ShoppingListList(value);

				//add the value to our db
				try {
					ListArrayAdapter adapter = (ListArrayAdapter)thisActivity.getListAdapter();
					
					adapter.add(dataSource.createList(listToAdd));
					Toast.makeText(thisActivity, "New list created", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Log.e(ShoppingList.LOG_NAME, "Failed to create list with exception: " + e.toString());
					Toast.makeText(thisActivity, "Failed to create new list", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		alert.show();
	}
	
	private void resetToTestDb() {
		Log.w(ShoppingList.LOG_NAME, "Resetting to test db!");
		dataSource.close();
		dbHelper.deleteDb();
		
		try {
			dbHelper.createTestDb();
			Toast.makeText(this, "Reset to test db", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e(ShoppingList.LOG_NAME, "Failed to reset to test db!");
			Toast.makeText(this, "Failed to reset to test db!", Toast.LENGTH_SHORT).show();
		}
		dbHelper.close();
		dataSource.open();
		
		updateListDisplay();
	}

}
