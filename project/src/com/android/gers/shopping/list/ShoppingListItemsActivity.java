package com.android.gers.shopping.list;

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
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ShoppingListItemsActivity
	extends ListActivity
	implements 
			DialogInterface.OnClickListener,
			OnClickListener,
			ListViewCheckBoxListener {
	
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
	int indexBeingEdited = -1;
	
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
        
        Button addButton = (Button) findViewById(R.id.button_add);
        addButton.setOnClickListener(this);
        
        Button startShopButton = (Button) findViewById(R.id.button_start_shop);
        startShopButton.setOnClickListener(this);
        
        Button resetButton = (Button) findViewById(R.id.button_reset);
        resetButton.setOnClickListener(this);
        
        /*CheckBox selectDeselectCheckBox = (CheckBox)findViewById(R.id.select_deselect_all);
        selectDeselectCheckBox.setOnClickListener(this);*/
        
        reloadListDisplay();
    }
    
    private class DialogValidator implements TextWatcher {

    	private Button button;

		public DialogValidator(Button button) {
			this.button = button;
			this.button.setEnabled(false);
		}
		
		public void afterTextChanged(Editable s) {
			//nothing
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			//nothing
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() == 0) {
				button.setEnabled(false);
			} else {
				button.setEnabled(true);
			}
		}
    	
    }

    private AlertDialog showEditDialog(String title) {
		AlertDialog.Builder alertBuilder = 
				new AlertDialog.Builder(this)
					.setTitle(title);
					
		LayoutInflater inflater = getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_edit_item, null);
		
		ArrayAdapter<QuantityType> adapter = new ArrayAdapter<QuantityType>(this, android.R.layout.simple_spinner_item, QuantityType.values());
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
	    Spinner spinner = (Spinner) dialogLayout.findViewById(R.id.edit_item_edit_quantity_type);
	    spinner.setAdapter(adapter);
	    
		alertBuilder.setView(dialogLayout);
		
		alertBuilder.setPositiveButton("Confirm", this);
		alertBuilder.setNegativeButton("Cancel", this);
		
		AlertDialog dialog = alertBuilder.create();
		dialog.show();

		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		DialogValidator validator = new DialogValidator(dialog.getButton(Dialog.BUTTON_POSITIVE));
		((EditText)dialogLayout.findViewById(R.id.edit_item_edit_name)).addTextChangedListener(validator);
		((EditText)dialogLayout.findViewById(R.id.edit_item_edit_quantity)).addTextChangedListener(validator);

		return dialog;
    }
    
    private void itemAddButtonClicked() {
		Log.i(ShoppingList.LOG_NAME, "itemAddButtonClicked");
		
		AlertDialog dialog = showEditDialog("Add New Item");
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		
		currentState = DialogState.ADD;
	}
    
    private void startShopButtonClicked() {
    	Log.i(ShoppingList.LOG_NAME, "startShopButtonClicked");
    	Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	Log.i(ShoppingList.LOG_NAME, "onListItemClick position " + position + ", id " + id);
    	itemToEdit = (ShoppingListItem) l.getItemAtPosition(position);
    	Log.i(ShoppingList.LOG_NAME, "corresponds to item: " + itemToEdit.toString() + "(which has id " + itemToEdit.getId() + ")");
    	
    	AlertDialog dialog = showEditDialog("Edit Item");
    	
    	EditText nameBox = (EditText)dialog.findViewById(R.id.edit_item_edit_name);
    	EditText quantityBox = (EditText)dialog.findViewById(R.id.edit_item_edit_quantity);
    	Spinner quantityTypeSpinner = (Spinner)dialog.findViewById(R.id.edit_item_edit_quantity_type);
    	
    	nameBox.setText(itemToEdit.getName());
    	quantityBox.setText(itemToEdit.getQuantity().toString());
    	
    	@SuppressWarnings("unchecked")
		int typePosition = ((ArrayAdapter<QuantityType>)quantityTypeSpinner.getAdapter()).getPosition(itemToEdit.getQuantityType());
    	quantityTypeSpinner.setSelection(typePosition);
    	
    	currentState = DialogState.EDIT;
    	indexBeingEdited = position;
    }
    
    
    private void resetButtonClicked() {
    	//CheckBox box = (CheckBox)findViewById(R.id.select_deselect_all);
    	
    	dataSource.setAllListItemsCompleteState(originatingListId, false);
    	
    	List<ShoppingListItem> items = dataSource.getShoppingListItems(originatingListId, false);
        
    	((ItemArrayAdapter)getListAdapter()).clear();
    	((ItemArrayAdapter)getListAdapter()).addAll(items);
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
    
    private void reloadListDisplay() {
    	Log.i(ShoppingList.LOG_NAME, "reloadListDisplay");
        List<ShoppingListItem> items = dataSource.getShoppingListItems(originatingListId, false);
        
        ItemArrayAdapter adapter = new ItemArrayAdapter(this,this, R.layout.item_row, items);
        setListAdapter(adapter);
    }
    
    //update the list display with a new item or edited item
    //if positionToModify == -1 we need to add it to the end of the items
    private void updateListDisplay(ShoppingListItem itemOfInterest, int indexToModify) {
		ItemArrayAdapter adapter = (ItemArrayAdapter)getListAdapter();
		if (indexToModify == -1) {
			adapter.add(itemOfInterest);
		} else {
			ShoppingListItem itemToDelete = adapter.getItem(indexToModify);
			adapter.remove(itemToDelete);
			adapter.insert(itemOfInterest, indexToModify);
		}
		adapter.notifyDataSetChanged();
    }


	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_add:
			itemAddButtonClicked();
			break;
		
		case R.id.button_start_shop:
			startShopButtonClicked();
			break;
		
		/*case R.id.select_deselect_all:
			resetButtonClicked();
			break;*/
		case R.id.button_reset:
			resetButtonClicked();
			break;
			
		default:
			//nothing
			break;
		}
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
        	int qty = Integer.parseInt(quantityBox.getText().toString());
        	QuantityType qtyType = (QuantityType)quantityTypeSpinner.getSelectedItem();

        	Log.i(ShoppingList.LOG_NAME, "User entered " + name + "!");

        	switch(currentState)
        	{
        	case ADD:
        		ShoppingListItem itemToAdd = new ShoppingListItem(originatingListId, name, qty, qtyType, false);

        		//add the value to our db
        		try {
        			ShoppingListItem itemAdded = dataSource.createItem(itemToAdd);
        			updateListDisplay(itemAdded, -1);
        			
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
        			if (updateItemInDb(editedItem)) {
        				updateListDisplay(editedItem, indexBeingEdited);
        			}
        		}
        		break;
        		
        	case NONE:
        		break;
        	}
        	break;
        }
		default:
			Log.i(ShoppingList.LOG_NAME, "non-positive button pressed");
			break;
		}
    	currentState = DialogState.NONE;
	}

	Boolean updateItemInDb(ShoppingListItem itemToUpdate) {
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
