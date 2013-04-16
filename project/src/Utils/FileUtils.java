package Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

	public static BufferedOutputStream OpenFileForWriting(String fileName) throws IOException {

		// create a file on the sdcard to export the
		// database contents to
		File myFile = new File( fileName );
		myFile.createNewFile();

		FileOutputStream fOut =  new FileOutputStream(myFile);
		BufferedOutputStream bos = new BufferedOutputStream( fOut );
		return bos;

	}
	
	public static InputStream GetFileStream(String fileName) throws IOException {
		InputStream in = new FileInputStream(fileName);
		return in;
	}
}
