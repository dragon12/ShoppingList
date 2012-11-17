package model;

import android.util.Log;

import com.android.gers.shopping.list.ShoppingList;

public enum QuantityType {
	UNITS("units", ""),
	GRAMMES("g", "g"),
	MILLILITRES("ml", "ml");
	
	public static QuantityType fromString(String text) {
		if (text != null) {
			for (QuantityType b : QuantityType.values()) {
				if (text.equalsIgnoreCase(b.getName())) {
					return b;
				}
			}
		}
		return null;
	}

	private String name;
	private String displayName;
	
	QuantityType(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return getName();
	}
}
