package Utils;

import java.io.BufferedOutputStream;
import java.io.IOException;

import com.android.gers.shopping.list.ShoppingList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbToXml {
	private SQLiteDatabase db;
	private int version;
	
	private Exporter exporter;

	public DbToXml(SQLiteDatabase db, BufferedOutputStream bos, int version) {
		this.db = db;
		this.version = version;
		
		exporter = new Exporter( bos );
	}
	
	public void Execute() {
		log("Exporting Data");

		try
		{
			exporter.startDbExport(db.getPath(), version);
			
			// get the tables out of the given sqlite database
			String sql = "SELECT * FROM sqlite_master";

			Cursor cur = db.rawQuery(sql, new String[0]);
			Log.d("db", "show tables, cur size " + cur.getCount() );
			cur.moveToFirst();

			String tableName;
			while ( cur.getPosition() < cur.getCount() )
			{
				tableName = cur.getString( cur.getColumnIndex( "name" ) );
				log( "table name " + tableName );

				// don't process these two tables since they are used
				// for metadata
				if ( ! tableName.equals( "android_metadata" ) &&
						! tableName.equals( "sqlite_sequence" ) )
				{
					exportTable( tableName );
				}

				cur.moveToNext();
			}
			exporter.endDbExport();
			exporter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void exportTable( String tableName ) throws IOException
	{
		exporter.startTable(tableName);

		// get everything from the table
		String sql = "select * from " + tableName;
		Cursor cur = db.rawQuery( sql, new String[0] );
		int numcols = cur.getColumnCount();

		log( "Start exporting table " + tableName );

//		// logging
//		for( int idx = 0; idx < numcols; idx++ )
//		{
//			log( "column " + cur.getColumnName(idx) );
//		}

		cur.moveToFirst();

		// move through the table, creating rows
		// and adding each column with name and value
		// to the row
		while( cur.getPosition() < cur.getCount() )
		{
			exporter.startRow();
			String name;
			String val;
			for( int idx = 0; idx < numcols; idx++ )
			{
				name = cur.getColumnName(idx);
				val = cur.getString( idx );
				log( "col '" + name + "' -- val '" + val + "'" );

				exporter.addColumn( name, val );
			}

			exporter.endRow();
			cur.moveToNext();
		}

		cur.close();

		exporter.endTable();
	}

	private void log( String msg )
	{
		Log.d(ShoppingList.LOG_NAME + ".DbToXml", msg );
	}

	class Exporter
	{
		private static final String CLOSING = ">";
		private static final String CLOSING_WITH_TICK = "'>";
		private static final String START_DB_EXPORT = "<export-database ";
		private static final String END_DB_EXPORT = "</export-database>";
		private static final String DB_EXPORT_ATTRIB_NAME = "name='";
		private static final String DB_EXPORT_ATTRIB_VERSION = "version='";
		private static final String CLOSE_ATTRIBUTE = "' ";
		private static final String START_TABLE = "<table name='";
		private static final String END_TABLE = "</table>";
		private static final String START_ROW = "<row>";
		private static final String END_ROW = "</row>";
		private static final String START_COL = "<col name='";
		private static final String END_COL = "</col>";

		private BufferedOutputStream bos;

		public Exporter( BufferedOutputStream bos )
		{
			this.bos = bos;
		}

		public void close() throws IOException
		{
			if ( bos != null )
			{
				bos.close();
			}
		}

		public void startDbExport( String dbName, int version ) throws IOException
		{
			String stg = START_DB_EXPORT
							+ DB_EXPORT_ATTRIB_NAME + dbName + CLOSE_ATTRIBUTE
							+ DB_EXPORT_ATTRIB_VERSION + version + CLOSE_ATTRIBUTE
							+ CLOSING;
			bos.write( stg.getBytes() );
		}

		public void endDbExport() throws IOException
		{
			bos.write( END_DB_EXPORT.getBytes() );
		}

		public void startTable( String tableName ) throws IOException
		{
			String stg = START_TABLE + tableName + CLOSING_WITH_TICK;
			bos.write( stg.getBytes() );
		}

		public void endTable() throws IOException
		{
			bos.write( END_TABLE.getBytes() );
		}

		public void startRow() throws IOException
		{
			bos.write( START_ROW.getBytes() );
		}

		public void endRow() throws IOException
		{
			bos.write( END_ROW.getBytes() );
		}

		public void addColumn( String name, String val ) throws IOException
		{
			String stg = START_COL + name + CLOSING_WITH_TICK + val + END_COL;
			bos.write( stg.getBytes() );
		}
	}
}
