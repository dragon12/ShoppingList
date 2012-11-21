package model;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.android.gers.shopping.list.ShoppingList;

public class ShoppingListList {
	private long id;
	private String name;
	private Date creationDate;
	private Boolean complete;
	private Boolean deleted;

	private static SimpleDateFormat dbStringToDateParser;
	private static SimpleDateFormat dateToStringFormatter;
	
	static {
		dbStringToDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateToStringFormatter = new SimpleDateFormat("yyyy-MM-dd");
	}
	
	public ShoppingListList(ShoppingListList other) {
		this.id = other.id;
		this.name = other.name;
		this.creationDate = other.creationDate;
		this.complete = other.complete;
		this.deleted = other.deleted;
	}
	public ShoppingListList(String name) {
		this(-1, name, null, false, false);
	}
	
	public ShoppingListList(long id, String name, String creationDate, Boolean complete, Boolean deleted) {
		this.id = id;
		this.name = name;
		setDate(creationDate);
		this.complete = complete;
		this.deleted = deleted;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDate(String creationDate) {
		try {
			this.creationDate = 
					(creationDate == null 
						? null
						: dbStringToDateParser.parse(creationDate));
			
		} catch (Exception e){
			Log.e(ShoppingList.LOG_NAME, "Failed to parse date from " + creationDate);
		}
	}
	
	public String getCreationDate() { 
		return dbStringToDateParser.format(creationDate);
	}
	
	public String getDisplayDate() {
		return dateToStringFormatter.format(creationDate);
	}
	
	public void setComplete(Boolean complete) {
		this.complete = complete;
	}
	
	public Boolean getComplete() {
		return complete;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	public Boolean getDeleted() {
		return deleted;
	}
	
	@Override
	public String toString() {
		return String.format("id %d, name %s, creationDate %s, isComplete %s, isDeleted %s", id, name, creationDate, complete.toString(), deleted.toString());
	}
	
}
