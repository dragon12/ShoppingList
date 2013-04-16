package Utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.android.gers.shopping.list.ShoppingList;

import android.content.ContentValues;
import android.util.Log;

public class DbFromXml {
	private InputStream inputStream;
	public DbFromXml(InputStream is) {
		inputStream = is;
	}
	
	public HashMap<String, List<ContentValues>> Execute() {
		log("Starting import");
		HashMap<String, List<ContentValues>> results = new HashMap<String, List<ContentValues>>(); 
		
		try {
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder parser = dbFactory.newDocumentBuilder();
	
			final Document document = parser.parse(inputStream);
	
			Element root = document.getDocumentElement();
			NodeList tableList = root.getChildNodes();
			for (int i = 0; i < tableList.getLength(); i++) {
				Node table = tableList.item(i);
				NamedNodeMap tableAttribs = table.getAttributes();
				String tableName = tableAttribs.getNamedItem("name").getNodeValue();
				
				log("saw table name: " + tableName);
				if (!results.containsKey(tableName)) {
					results.put(tableName, new ArrayList<ContentValues>());
				}
				
				NodeList rowList = table.getChildNodes();
				for (int j = 0; j < rowList.getLength(); j++) {
					Node row = rowList.item(j);
					NodeList columns = row.getChildNodes();
					log("  row:");
					ContentValues newRow = new ContentValues();
					for (int k = 0; k < columns.getLength(); k++) {
						Node column = columns.item(k);
						String colName = column.getAttributes().getNamedItem("name").getNodeValue();
						String colValue = column.getChildNodes().item(0).getNodeValue();
						log("    col " + colName + ": " + colValue);
						newRow.put(colName, colValue);
					}
					results.get(tableName).add(newRow);
				}
			}		
		} catch (Exception e) {
			Log.e(ShoppingList.LOG_NAME + ".DbToXml", "Exception thrown while attempting to import db values: " + e);
			return null;
		}
		return results;
	}
	
	private void log( String msg )
	{
		Log.d(ShoppingList.LOG_NAME + ".DbToXml", msg );
	}

}
