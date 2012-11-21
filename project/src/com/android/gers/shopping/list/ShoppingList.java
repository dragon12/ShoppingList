package com.android.gers.shopping.list;

import android.content.Context;
import android.widget.Toast;

public final class ShoppingList {

	private ShoppingList()
	{
		
	}
	
	public static final String LOG_NAME = "SHOPPING_LIST";
	
	public static final class DB {
		public static final String NAME = "ShoppingList";
		public static final Integer DB_VERSION = 6;
	}
	
	
	public static void ToastNotImplemented(Context context) {
		Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show();
	}
}
