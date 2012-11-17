package com.android.gers.shopping.list;

import java.util.ArrayList;
import java.util.List;

import com.android.gers.shopping.list.DB.DbTableItems;
import com.android.gers.shopping.list.DB.ShoppingListDb;

import model.ItemArrayAdapter;
import model.ListDataSource;
import model.QuantityType;
import model.ShoppingListItem;
import model.ShoppingListList;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ShoppingListItemsActivity
	extends ListActivity
	implements DialogInterface.OnClickListener, ListViewCheckBoxListener {
	
	private static final int CONTEXT_MENU_DELETE_ID = Menu.FIRST + 1;

	private ListDataSource dataSource;
	private ShoppingListDb dbHelper;
	
	private long originatingListId;
	
	private enum DialogState {
		NONE,
		ADD,
		EDIT;
	}
	
	
	DialogState currentState = DialogState.NONE;
	ShoppingListItem itemToEdit = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_items);
        
        Bundle extras = getIntent().getExtras();
        
        if (extras != null) {
        	if (extras.containsKey(DbTableItems.COL_LIST_ID)) {
        		Log.i(ShoppingList.LOG_NAME, "extras does contain our key");
        	}
        	
        	originatingListId = extras.getLong(DbTableItems.COL_LIST_ID);
        	Log.i(ShoppingList.LOG_NAME, "Originating was " + originatingListId);
        	
        } else {
        	Log.e(ShoppingList.LOG_NAME, "Had no extras in activity!");
        	this.finish();
        }
        
        this.getListView().setDividerHeight(2);
        
        registerForContextMenu(getListView());
        
        dbHelper = new ShoppingListDb(this);
        
        Log.i(ShoppingList.LOG_NAME, "Creating list data source in items");
        dataSource = new ListDataSource(this, dbHelper);
        Log.i(ShoppingList.LOG_NAME, "Done");
        
        dataSource.open();
        
        //get the list details
        ShoppingListList list = dataSource.getShoppingList(originatingListId);
        EditText title = (EditText)findViewById(R.id.list_name);
        title.setText(list.getName());
        
        updateListDisplay();
    }
    

    private AlertDialog createEditDialog(String title) {
		AlertDialog.Builder alert = 
				new AlertDialog.Builder(this)
					.setTitle(title);
					
		LayoutInflater inflater = getLayoutInflater();
		View dialoglayout = inflater.inflate(R.layout.dialog_edit_item, null);
		
		ArrayAdapter<QuantityType> adapter = new ArrayAdapter<QuantityType>(this, android.R.layout.simple_spinner_item, QuantityType.values());
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
	    Spinner spinner = (Spinner) dialoglayout.findViewById(R.id.edit_item_edit_quantity_type);
	    spinner.setAdapter(adapter);
	    
		alert.setView(dialoglayout);
		
		alert.setPositiveButton("Confirm", this);
		alert.setNegativeButton("Cancel", this);
		
		return alert.create();
    }
    
    private void itemAddButtonClicked() {
		Log.i(ShoppingList.LOG_NAME, "itemAddButtonClicked");
		
		AlertDialog dialog = createEditDialog("Add New Item");
		
		currentState = DialogState.ADD;
		
		dialog.show();
	}
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	Log.i(ShoppingList.LOG_NAME, "onListItemClick position " + position + ", id " + id);
    	itemToEdit = (ShoppingListItem) l.getItemAtPosition(position);
    	Log.i(ShoppingList.LOG_NAME, "corresponds to item: " + itemToEdit.toString() + "(which has id " + itemToEdit.getId() + ")");
    	
    	AlertDialog dialog = createEditDialog("Edit Item");
    	dialog.show();
    	
    	EditText nameBox = (EditText)dialog.findViewById(R.id.edit_item_edit_name);
    	EditText quantityBox = (EditText)dialog.findViewById(R.id.edit_item_edit_quantity);
    	Spinner quantityTypeSpinner = (Spinner)dialog.findViewById(R.id.edit_item_edit_quantity_type);
    	
    	nameBox.setText(itemToEdit.getName());
    	quantityBox.setText(itemToEdit.getQuantity());
    	
    	int typePosition = ((ArrayAdapter<QuantityType>)quantityTypeSpinner.getAdapter()).getPosition(itemToEdit.getQuantityType());
    	quantityTypeSpinner.setSelection(typePosition);
    	
    	currentState = DialogState.EDIT;
    }
    
    // handles checkbox changes in the listview
	public void checkBoxChanged(int listPosition, CompoundButton buttonView, boolean isChecked) {
		Log.i(ShoppingList.LOG_NAME, "checkBoxChanged, position " + listPosition);
		
    	ShoppingListItem itemChecked = (ShoppingListItem) getListView().getItemAtPosition(listPosition);
    	Log.i(ShoppingList.LOG_NAME, "corresponds to item: " + itemChecked.toString() + "(which has id " + itemChecked.getId() + ")");

    	itemChecked.setComplete(isChecked);
    	updateItemInDb(itemChecked);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_shopping_list_items, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
			case R.id.menu_add_item:
				itemAddButtonClicked();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
				
		}
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.delete_item);  
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case CONTEXT_MENU_DELETE_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    		ShoppingListItem itemChosen = (ShoppingListItem)getListView().getItemAtPosition(info.position);
    		
    		dataSource.deleteItem(itemChosen);
    		ItemArrayAdapter adapter = (ItemArrayAdapter)getListAdapter();
    		adapter.remove(itemChosen);
    		
    		return true;
    	}
    	return super.onContextItemSelected(item);
    }
    
    private void updateListDisplay() {
        List<ShoppingListItem> items = dataSource.getShoppingListItems(originatingListId, false);
        
        ItemArrayAdapter adapter = new ItemArrayAdapter(this,this, R.layout.item_row, items);
        setListAdapter(adapter);
    }
    
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
        case DialogInterface.BUTTON_POSITIVE:
        {
        	AlertDialog editAlertView = (AlertDialog)dialog;
        	
        	EditText nameBox = (EditText)editAlertView.findViewById(R.id.edit_item_edit_name);
        	EditText quantityBox = (EditText)editAlertView.findViewById(R.id.edit_item_edit_quantity);
        	Spinner quantityTypeSpinner = (Spinner)editAlertView.findViewById(R.id.edit_item_edit_quantity_type);
        	
        	String name = nameBox.getText().toString();
        	String qty = quantityBox.getText().toString();
        	QuantityType qtyType = (QuantityType)quantityTypeSpinner.getSelectedItem();

        	Log.i(ShoppingList.LOG_NAME, "User entered " + name + "!");

        	switch(currentState)
        	{
        	case ADD:
        		ShoppingListItem itemToAdd = new ShoppingListItem(originatingListId, name, qty, qtyType, false);

        		//add the value to our db
        		try {
        			ItemArrayAdapter adapter = (ItemArrayAdapter)getListAdapter();
        			adapter.add(dataSource.createItem(itemToAdd));

        			Toast.makeText(this, "New item created", Toast.LENGTH_SHORT).show();
        		} catch (Exception e) {
        			Log.e(ShoppingList.LOG_NAME, "Failed to create item with exception: " + e.toString());
        			Toast.makeText(this, "Failed to create new item", Toast.LENGTH_SHORT).show();
        		}
        		break;
        	case EDIT:
        		//update the item that was selected for editing
        		ShoppingListItem editedItem = new ShoppingListItem(itemToEdit.getId(), itemToEdit.getListId(), name, qty, qtyType, itemToEdit.getComplete());
        		if (editedItem.equals(itemToEdit)) {
        			//no change, do nothing
        		} else {
        			updateItemInDb(editedItem);
        		}
        	}
        	break;
        }
		default:
			Log.i(ShoppingList.LOG_NAME, "non-positive button pressed");
			break;
		}
    	currentState = DialogState.NONE;
	}

	void updateItemInDb(ShoppingListItem itemToUpdate) {
		try {
			dataSource.updateItem(itemToUpdate);
		} catch (Exception e) {
			Log.e(ShoppingList.LOG_NAME, "Failed to edit item with exception: " + e.toString());
			Toast.makeText(this, "Failed to edit item", Toast.LENGTH_SHORT).show();
		}
		updateListDisplay();		
	}
}
