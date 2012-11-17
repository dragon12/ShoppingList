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

	private static SimpleDateFormat dbStringToDateParser;
	private static SimpleDateFormat dateToStringFormatter;
	
	static {
		dbStringToDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateToStringFormatter = new SimpleDateFormat("yyyy-MM-dd");
	}
	
	public ShoppingListList(String name) {
		this(-1, name, null, false);
	}
	
	public ShoppingListList(long id, String name, String creationDate, Boolean complete) {
		this.id = id;
		this.name = name;
		setDate(creationDate);
		this.complete = complete;
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
			this.creationDate = dbStringToDateParser.parse(creationDate);
		} catch (Exception e){
			Log.e(ShoppingList.LOG_NAME, "Failed to parse date from " + creationDate);
		}
	}
	
	public String getDate() {
		return dateToStringFormatter.format(creationDate);
	}
	
	public void setComplete(Boolean complete) {
		this.complete = complete;
	}
	
	public Boolean getComplete() {
		return complete;
	}
	
	@Override
	public String toString() {
		return String.format("id %d, name %s, creationDate %s, isComplete %s", id, name, creationDate, complete.toString());
	}
	
}
