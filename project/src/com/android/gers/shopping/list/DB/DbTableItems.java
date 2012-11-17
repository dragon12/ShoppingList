package com.android.gers.shopping.list.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

public class DbTableItems extends DbTableBase {
	
	private static final String TABLE_NAME = "items";

	public static final String COL_ID = BaseColumns._ID;
	public static final int COL_IDX_ID = 0;
	
	public static final String COL_LIST_ID = "list_id";
	public static final int COL_IDX_LIST_ID = 1;

	public static final String COL_NAME = "name";
	public static final int COL_IDX_NAME = 2;

	public static final String COL_QUANTITY = "quantity";
	public static final int COL_IDX_QUANTITY = 3;

	public static final String COL_QUANTITY_TYPE = "quantity_type";
	public static final int COL_IDX_QUANTITY_TYPE = 4;

	public static final String COL_IS_COMPLETE = "is_complete";
	public static final int COL_IDX_IS_COMPLETE = 5;

	public static final String[] Columns = {
		COL_ID,
		COL_LIST_ID,
		COL_NAME,
		COL_QUANTITY,
		COL_QUANTITY_TYPE,
		COL_IS_COMPLETE
	};
	
	private String foreignTable;

	private String foreignColumn;

	public DbTableItems(String foreignTable, String foreignColumn) {
		super(TABLE_NAME);
		this.foreignTable = foreignTable;
		this.foreignColumn = foreignColumn;
	}
	
	public void drop(SQLiteDatabase db)
	{
		db.execSQL("drop table if exists " + TABLE_NAME);
	}
	
	@Override
	public void create(SQLiteDatabase db)
	{
		String sql = 
				"CREATE TABLE " + TABLE_NAME + "(" + 
						COL_ID + " integer primary key autoincrement, " +
						COL_LIST_ID + " integer not null REFERENCES " + foreignTable + "(" + foreignColumn + ")," +
						COL_NAME + " text not null, " +
						COL_QUANTITY + " text not null, " +
						COL_QUANTITY_TYPE + " text not null, " +
						COL_IS_COMPLETE + " boolean DEFAULT false)";
						
		db.execSQL(sql);		
	}
	
	public long insert(SQLiteDatabase db, ContentValues kvps) {
		return db.insert(TABLE_NAME, null, kvps);
	}
	
	public Boolean update(SQLiteDatabase db, long id, ContentValues kvps) {
		return db.update(TABLE_NAME, kvps, COL_ID + " = " + id, null) == 1;
	}

	public Cursor getItemById(SQLiteDatabase db, long id) {
		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		qb.appendWhere(COL_ID + " = " + id);
		
		return qb.query(db, Columns, null, null, null, null, COL_ID);		
	}
	
	public Cursor getItemsByListId(SQLiteDatabase db, long listId, Boolean noComplete) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);
		
		qb.appendWhere(COL_LIST_ID + " = " + listId);
		if (noComplete)
		{
			qb.appendWhere(COL_IS_COMPLETE + " = 'false'");
		}
		
		return qb.query(db, Columns, null, null, null, null, COL_ID);		
	}
	
	// we don't return a status for this one because there's no guarantee that the list
	//  has any items
	public Boolean deleteItemsByListId(SQLiteDatabase db, long listId) {
		db.delete(TABLE_NAME, COL_LIST_ID + " = " + listId, null);
		return true;
	}
	
	public Boolean deleteItemsById(SQLiteDatabase db, long id) {
		return db.delete(TABLE_NAME, COL_ID + " = " + id, null) == 1;
	}
}


