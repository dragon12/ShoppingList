package com.android.gers.shopping.list.DB;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.android.gers.shopping.list.R;
import com.android.gers.shopping.list.ShoppingList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DebugDbHelper {
 
    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.android.gers.shopping.list/databases/";

    private final Context myContext;

    private SQLiteOpenHelper helper;
    
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DebugDbHelper(Context context, SQLiteOpenHelper helper) {
    	Log.i(ShoppingList.LOG_NAME, "DebugDbHelper starting up");
    	 
        this.myContext = context;
        this.helper = helper;
    }	
 
    public void deleteDb() {
    	myContext.deleteDatabase(ShoppingList.DB.NAME);
    }
    
    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDb() throws IOException{

    	boolean dbExist = checkDb();
 
    	if (dbExist) {
    		//do nothing - database already exist
    		Log.i(ShoppingList.LOG_NAME, "Db already existed");
    	} else {
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	helper.getReadableDatabase();
 
        	try {
        		Log.i(ShoppingList.LOG_NAME, "About to copy the db");
    			copyDb();
    			Log.i(ShoppingList.LOG_NAME, "Copied db");
    		} catch (IOException e) {
        		throw new Error("Error copying database");
        	}
        	helper.close();
    	}
 
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDb() {
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + ShoppingList.DB.NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	} catch(SQLiteException e) {
    		//database does't exist yet.
    	}
 
    	// we were able to open the DB
    	if (checkDB != null) {
    		checkDB.close();
    	}
 
    	return checkDB != null;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring bytestream.
     * */
    private void copyDb() throws IOException {
 
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(myContext.getString(R.string.test_db_name));
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + ShoppingList.DB.NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer)) > 0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
  
}
