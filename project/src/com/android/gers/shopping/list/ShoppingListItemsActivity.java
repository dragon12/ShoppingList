package com.android.gers.shopping.list;

import java.util.List;

import com.android.gers.shopping.list.SimpleInputDialog.DialogClickListener;
import com.android.gers.shopping.list.DB.DbTableItems;
import model.BaseItemArrayAdapter;
import model.ItemArrayAdapter;
import model.QuantityType;
import model.ShoppingListItem;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ShoppingListItemsActivity
	extends BaseListItemsActivity
	implements 
			DialogClickListener,
			OnClickListener
			 {
	
	private static final int CONTEXT_MENU_DELETE_ID = Menu.FIRST + 1;
	
	private enum DialogState {
		NONE,
		ADD,
		EDIT;
	}
	
	
	DialogState currentState = DialogState.NONE;
	ShoppingListItem itemToEdit = null;
	int indexBeingEdited = -1;

	public ShoppingListItemsActivity() {
		super(R.layout.activity_shopping_list_items, R.menu.activity_shopping_list_items, true);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Button addButton = (Button) findViewById(R.id.button_add);
        addButton.setOnClickListener(this);
        
        Button startShopButton = (Button) findViewById(R.id.button_start_shop);
        startShopButton.setOnClickListener(this);
    }
    
    @Override
    protected BaseItemArrayAdapter createAdapter(List<ShoppingListItem> items) {
    	return new ItemArrayAdapter(this, this, R.layout.item_row, items);
    }
    

    private AlertDialog showEditDialog(String title) {
    	
		LayoutInflater inflater = getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_edit_item, null);

		ArrayAdapter<QuantityType> adapter = new ArrayAdapter<QuantityType>(this, android.R.layout.simple_spinner_item, QuantityType.values());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		Spinner spinner = (Spinner) dialogLayout.findViewById(R.id.edit_item_edit_quantity_type);
		spinner.setAdapter(adapter);
		
		AlertDialog dialog = SimpleInputDialog.SimpleInputDialogBuilder(this, this, 0, title, null, dialogLayout);
				
		DialogValidator validator = new DialogValidator(dialog.getButton(Dialog.BUTTON_POSITIVE));
		((EditText)dialogLayout.findViewById(R.id.edit_item_edit_name)).addTextChangedListener(validator);
		((EditText)dialogLayout.findViewById(R.id.edit_item_edit_quantity)).addTextChangedListener(validator);

		return dialog;
    }
    
    private void itemAddButtonClicked() {
		Log.d(ShoppingList.LOG_NAME, "itemAddButtonClicked");
		
		AlertDialog dialog = showEditDialog("Add New Item");
		dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
		
		currentState = DialogState.ADD;
	}
    
    private void startShopButtonClicked() {
    	Log.d(ShoppingList.LOG_NAME, "startShopButtonClicked");
    	Intent i = new Intent(this, ShoppingListShoppingMode.class);
    	i.putExtra(DbTableItems.COL_LIST_ID, originatingListId);
    	
    	startActivityForResult(i, 0);
    }
    
    @Override
    protected void listItemClicked(ShoppingListItem itemClicked, int position) {
    	itemToEdit = itemClicked;
    	
    	AlertDialog dialog = showEditDialog("Edit Item");
    	
    	EditText nameBox = (EditText)dialog.findViewById(R.id.edit_item_edit_name);
    	EditText quantityBox = (EditText)dialog.findViewById(R.id.edit_item_edit_quantity);
    	Spinner quantityTypeSpinner = (Spinner)dialog.findViewById(R.id.edit_item_edit_quantity_type);
    	
    	nameBox.setText(itemToEdit.getName());
    	quantityBox.setText(itemToEdit.getQuantity().toString());
    	
    	@SuppressWarnings("unchecked")
    	ArrayAdapter<QuantityType> adapter = (ArrayAdapter<QuantityType>)quantityTypeSpinner.getAdapter();
		int typePosition = adapter.getPosition(itemToEdit.getQuantityType());
    	quantityTypeSpinner.setSelection(typePosition);
    	
    	currentState = DialogState.EDIT;
    	indexBeingEdited = position;
    }
    
    
    private void resetButtonClicked() {
    	//CheckBox box = (CheckBox)findViewById(R.id.select_deselect_all);
    	
    	dataSource.setAllListItemsCompleteState(originatingListId, false);
    	
    	List<ShoppingListItem> items = dataSource.getShoppingListItems(originatingListId, true);
        
    	((ItemArrayAdapter)getListAdapter()).clear();
    	((ItemArrayAdapter)getListAdapter()).addAll(items);
    }
    
    // handles checkbox changes in the listview
	public void statusChanged(int listPosition, View triggeredView, boolean isOn) {
		Log.d(ShoppingList.LOG_NAME, "checkBoxChanged, position " + listPosition);
		
    	ShoppingListItem itemChecked = (ShoppingListItem) getListView().getItemAtPosition(listPosition);
    	Log.d(ShoppingList.LOG_NAME, "corresponds to item: " + itemChecked.toString() + "(which has id " + itemChecked.getId() + ")");

    	itemChecked.setComplete(isOn);
    	updateItemInDb(itemChecked);
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
			case R.id.menu_reset_done:
				resetButtonClicked();
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
			
		default:
			//nothing
			break;
		}
	}
	
	public void buttonClicked(int id, DialogInterface dialog, int which) {
		switch(which) {
        case DialogInterface.BUTTON_POSITIVE:
        {
        	AlertDialog editAlertView = (AlertDialog)dialog;
        	
        	EditText nameBox = (EditText)editAlertView.findViewById(R.id.edit_item_edit_name);
        	EditText quantityBox = (EditText)editAlertView.findViewById(R.id.edit_item_edit_quantity);
        	Spinner quantityTypeSpinner = (Spinner)editAlertView.findViewById(R.id.edit_item_edit_quantity_type);
        	
        	String name = nameBox.getText().toString();
        	double qty = Double.parseDouble(quantityBox.getText().toString());
        	QuantityType qtyType = (QuantityType)quantityTypeSpinner.getSelectedItem();

        	Log.d(ShoppingList.LOG_NAME, "User entered " + name + "!");

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
			Log.d(ShoppingList.LOG_NAME, "non-positive button pressed");
			break;
		}
    	currentState = DialogState.NONE;
	}
}
