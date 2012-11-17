package model;

public class ShoppingListItem {
	private long id;
	private long listId;
	private String name;
	private String quantity;
	private QuantityType quantityType;
	private Boolean complete;
	
	public boolean equals(Object obj)
	{
		// if the two objects are equal in reference, they are equal
		if (this == obj) {
			return true;
		} else if (obj instanceof ShoppingListItem) {
			ShoppingListItem item = (ShoppingListItem) obj;
			if (	id == item.id
				 && listId == item.listId
				 && name.equals(item.name)
				 && quantity.equals(item.quantity)
				 && quantityType.equals(item.quantityType)
				 && complete.equals(item.complete)
			    )
			{
				return true;
			}
		}

		return false;
	}

	public ShoppingListItem(long listId, String name, String quantity, QuantityType quantityType, Boolean complete) {
		this(-1, listId, name, quantity, quantityType, complete);
	}

	public ShoppingListItem(long id, long listId, String name, String quantity, QuantityType quantityType, Boolean complete) {
		this.id = id;
		this.listId = listId;
		this.name = name;
		this.quantity = quantity;
		this.quantityType = quantityType;
		this.complete = complete;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getListId() {
		return listId;
	}

	public void setListId(long listId) {
		this.listId = listId;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	public QuantityType getQuantityType() {
		return quantityType;
	}

	public void setQuantityType(QuantityType quantityType) {
		this.quantityType = quantityType;
	}


	@Override
	public String toString() {
		return String.format("id %d,  listId %d, quantity %s, quantityType %s, name %s, complete %s",
							id, listId, quantity, quantityType.toString(), name, complete.toString());
	}	
}
