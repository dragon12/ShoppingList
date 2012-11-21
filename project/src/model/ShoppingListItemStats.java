package model;

public class ShoppingListItemStats {
	 public long listId;
	 public int numComplete;
	 public int numIncomplete;
	 
	 public ShoppingListItemStats(long listId) {
		 this.listId = listId;
		 this.numComplete = 0;
		 this.numIncomplete = 0;
	 }
	 
}
