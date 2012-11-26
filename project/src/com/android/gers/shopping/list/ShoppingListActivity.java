package com.android.gers.shopping.list;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import model.ListArrayAdapter;
import model.ListDataSource;
import model.ShoppingListItemStats;
import model.ShoppingListList;

import com.android.gers.shopping.list.SimpleInputDialog.DialogClickListener;
import com.android.gers.shopping.list.DB.DbTableItems;
import com.android.gers.shopping.list.DB.ShoppingListDb;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ShoppingListActivity 
		extends ListActivity 
		implements DialogClickListener {
	
	private static final int CONTEXT_MENU_DELETE_ID = Menu.FIRST + 1;
	private static final int CONTEXT_MENU_RENAME_ID = CONTEXT_MENU_DELETE_ID + 1;
	private static final int CONTEXT_MENU_CLONE_ID = CONTEXT_MENU_RENAME_ID + 1;
	
	private static final int DIALOG_ID_LIST_EDIT_NAME = 1;
	private static final int DIALOG_ID_LIST_CLONE = 2;
	
	private enum EditDialogState {
		NONE,
		ADD,
		RENAME;
	}
	private EditDialogState editDialogState = EditDialogState.NONE;
	
	private ShoppingListList listBeingModified = null;
	private int indexBeingModified = -1;
	
	private ListDataSource dataSource;
	private ShoppingListDb dbHelper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list_activity);

        this.getListView().setDividerHeight(2);
        
        registerForContextMenu(getListView());
        
        dbHelper = new ShoppingListDb(this);
        
        Log.d(ShoppingList.LOG_NAME, "about to create data source");
        dataSource = new ListDataSource(this, dbHelper);
        Log.d(ShoppingList.LOG_NAME, "Created data source");
        
        dataSource.open();
        
        reloadListDisplay();
    }
    
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {     
    	super.onActivityResult(requestCode, resultCode, data); 
    	reloadListDisplay();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	Log.d(ShoppingList.LOG_NAME, "onListItemClick position " + position + ", id " + id);
    	ShoppingListList listChosen = (ShoppingListList) l.getItemAtPosition(position);
    	Log.d(ShoppingList.LOG_NAME, "corresponds to list: " + listChosen.toString() + "(which has id " + listChosen.getId() + ")");
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
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		ShoppingListList listChosen = (ShoppingListList)getListView().getItemAtPosition(info.position);
		indexBeingModified = info.position;
		
    	switch(item.getItemId()) {
    	case CONTEXT_MENU_DELETE_ID:
    		dataSource.deleteList(listChosen);
    		ListArrayAdapter adapter = (ListArrayAdapter)getListAdapter();
    		adapter.remove(listChosen);
    		
    		return true;
    	case CONTEXT_MENU_RENAME_ID:
    		displayRenameDialog(listChosen);
    		return true;
    		
    	case CONTEXT_MENU_CLONE_ID:
    		displayCloneDialog(listChosen);
    		return true;
    	}
    	return super.onContextItemSelected(item);
    }
    
    private void updateStats(ListArrayAdapter adapter) {
    	HashMap<Long, ShoppingListItemStats> stats = dataSource.getShoppingListItemStats();
        adapter.setStats(stats);
    }
    
    private void reloadListDisplay() {
        List<ShoppingListList> lists = dataSource.getShoppingLists(true);
        ListArrayAdapter adapter = new ListArrayAdapter(this, R.layout.list_row, lists);
        updateStats(adapter);
        setListAdapter(adapter);    	
    }
    

    //update the list display with a new list or edited list
    //if positionToModify == -1 we need to add it to the end of the items
    private void updateListDisplay(ShoppingListList listOfInterest, int indexToModify) {
		ListArrayAdapter adapter = (ListArrayAdapter)getListAdapter();
		if (indexToModify == -1) {
			adapter.add(listOfInterest);
		} else {
			ShoppingListList listToDelete = adapter.getItem(indexToModify);
			adapter.remove(listToDelete);
			adapter.insert(listOfInterest, indexToModify);
		}
		adapter.notifyDataSetChanged();
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
				displayAddListDialog();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
		
	}
	
	private AlertDialog showListNameEditDialog(String title) {
    	LayoutInflater inflater = getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_list_name, null);

		AlertDialog dialog = SimpleInputDialog.SimpleInputDialogBuilder(this, this, DIALOG_ID_LIST_EDIT_NAME, title, null, dialogLayout);
		
		DialogValidator validator = new DialogValidator(dialog.getButton(Dialog.BUTTON_POSITIVE));
		((EditText)dialogLayout.findViewById(R.id.rename_list_edit_name)).addTextChangedListener(validator);
		
		return dialog;
    }

	private AlertDialog showCloneListDialog(String title) {
    	LayoutInflater inflater = getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_list_clone, null);

		AlertDialog dialog = SimpleInputDialog.SimpleInputDialogBuilder(this, this, DIALOG_ID_LIST_CLONE, title, null, dialogLayout);
		
		DialogValidator validator = new DialogValidator(dialog.getButton(Dialog.BUTTON_POSITIVE));
		((EditText)dialogLayout.findViewById(R.id.clone_list_edit_name)).addTextChangedListener(validator);
		
		return dialog;
    }

	private void displayAddListDialog() {
		Log.d(ShoppingList.LOG_NAME, "displayAddListDialog");
		editDialogState = EditDialogState.ADD;
		showListNameEditDialog("Add New List");
	}
	
	private void displayRenameDialog(ShoppingListList listChosen) {
		editDialogState = EditDialogState.RENAME;
		listBeingModified = listChosen;
		
		AlertDialog dialog = showListNameEditDialog("Rename List '" + listBeingModified.getName() + "'");
		
		//populate the text box with the current name of the list
		EditText editText = (EditText)dialog.findViewById(R.id.rename_list_edit_name);
		editText.setText(listChosen.getName());
	}

	private void displayCloneDialog(ShoppingListList listChosen) {
		editDialogState = EditDialogState.NONE;
		listBeingModified = listChosen;
		
		AlertDialog dialog = showCloneListDialog("Clone List '" + listBeingModified.getName() + "'");
		
		//populate the text box with the current name of the list
		EditText editText = (EditText)dialog.findViewById(R.id.clone_list_edit_name);
		editText.setText("Cloned " + listChosen.getName());
		
		//checkbox should be set by default
		CheckBox box = (CheckBox)dialog.findViewById(R.id.clone_list_keep_complete_check);
		box.setChecked(true);
	}

	public void buttonClicked(int id, DialogInterface dialog, int whichButton) {
		switch(whichButton)
    	{
    		case DialogInterface.BUTTON_POSITIVE:
    			AlertDialog alertView = (AlertDialog)dialog;
    			switch(id)
    			{
    				case DIALOG_ID_LIST_EDIT_NAME:
    					EditText inputText = (EditText)alertView.findViewById(R.id.rename_list_edit_name);
    					String valueEntered = inputText.getText().toString();
    					Log.d(ShoppingList.LOG_NAME, "User entered " + valueEntered + "!");
    					
    					if (editDialogState == EditDialogState.ADD) {
    						ShoppingListList listToAdd = new ShoppingListList(valueEntered);

    						//add the value to our db
    						try {
    							ListArrayAdapter adapter = (ListArrayAdapter)getListAdapter();
    							
    							adapter.add(dataSource.createList(listToAdd));
    							Toast.makeText(this, "New list created", Toast.LENGTH_SHORT).show();
    						} catch (Exception e) {
    							Log.e(ShoppingList.LOG_NAME, "Failed to create list with exception: " + e.toString());
    							Toast.makeText(this, "Failed to create new list", Toast.LENGTH_SHORT).show();
    						}
    					} 
    					else if (editDialogState == EditDialogState.RENAME) {
    		        		//update the item that was selected for editing
    		        		if (listBeingModified.getName() != valueEntered) {
	    		        		ShoppingListList renamedList = new ShoppingListList(listBeingModified);
	    		        		renamedList.setName(valueEntered);
	    		        		
	    		        		try {
	    		        			if (dataSource.updateList(renamedList)) {
	    		        				updateListDisplay(renamedList, indexBeingModified);
	    		        			} else {
	    		        				Log.e(ShoppingList.LOG_NAME, "Failed to do rename!");
	    		        				Toast.makeText(this,  "Rename failed", Toast.LENGTH_SHORT).show();
	    		        			}
	    		        		} catch(Exception e) {
	    		        			Log.e(ShoppingList.LOG_NAME, "Threw exception from updateList: " + e.toString());
	    		        			Toast.makeText(this, "Exception thrown", Toast.LENGTH_SHORT).show();
	    		        		}
    		        				
    		        		}
    		        		break;
    					} 
    					else {
    						Log.e(ShoppingList.LOG_NAME, "Invalid state: " + editDialogState.toString());
    					}
    					
    					break;
    					
    				case DIALOG_ID_LIST_CLONE:
    					EditText cloneText = (EditText)alertView.findViewById(R.id.clone_list_edit_name);
    					String clonedName = cloneText.getText().toString();
    					
    					CheckBox cloneBox = (CheckBox)alertView.findViewById(R.id.clone_list_keep_complete_check);
    					Boolean keepComplete = !cloneBox.isChecked();
    					
    					Log.d(ShoppingList.LOG_NAME, "User entered " + clonedName + ", keep_complete = " + keepComplete.toString());
    					ShoppingListList listToAdd = new ShoppingListList(clonedName);

    					//clone the list in our db
    					try {
    						ListArrayAdapter adapter = (ListArrayAdapter)getListAdapter();
    						
    						//clone the list
    						ShoppingListList clonedList = dataSource.cloneList(listBeingModified.getId(), listToAdd, keepComplete);
    								
    						//update the adapter stats with the cloned stuff
    						updateStats(adapter);
    						
    						//and finally add the cloned list to our listView
    						adapter.add(clonedList);
    						Toast.makeText(this, "List cloned", Toast.LENGTH_SHORT).show();
    					} catch (Exception e) {
    						Log.e(ShoppingList.LOG_NAME, "Failed to clone list with exception: " + e.toString());
    						Toast.makeText(this, "Failed to clone list", Toast.LENGTH_SHORT).show();
    					}
    					break;

    				default:
    					Log.e(ShoppingList.LOG_NAME, "Saw bad id in ButtonClicked: " + id);
    					break;
    			}
    			break;

    		default:
    			break;
    	}	
		
		//reset state
		editDialogState = EditDialogState.NONE;
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
		
		reloadListDisplay();
	}
}
