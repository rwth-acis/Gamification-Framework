package i5.las2peer.services.gamificationBadgeService.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalFileManager
{
	private static String baseDir = "../GamificationBadgeService/files/";
	private static LocalFileManager manager;

	protected LocalFileManager()
	{

	}


	public static String getBasedir()
	{
		return baseDir;
	}

	
	public static void setBasedir(String dir)
	{
		baseDir = dir;
	}

	protected static LocalFileManager getManager()
	{
		if (manager == null)
			manager = new LocalFileManager();

		return manager;
	}

	public static byte[] getFile(String file)
	{
		getManager();
		/* if(file.contains(".."))//ignore
		     return null;*/
		return getFile(new File(baseDir + file));
	}

	public static List<String> getDir(String dir)
	{
		try
		{
			File directory = new File(baseDir + dir);

			List<String> dirs = new ArrayList<String>();
			List<String> files = new ArrayList<String>();
			File[] dirContents = directory.listFiles();
			for (final File fileEntry : dirContents) {
				if (fileEntry.isDirectory()) {
					dirs.add(fileEntry.getName());
				} else {
					files.add(fileEntry.getName());
				}
			}
			Collections.sort(dirs);
			Collections.sort(files);
			dirs.addAll(files);
			return dirs;
		} catch (Exception e)
		{
			return null;
		}

	}

	/**
	 * Reads a given file
	 * @param file file to read
	 * @return content of file
	 */
	public static byte[] getFile(File file)
	{

		byte[] result = new byte[] {};

		try
		{
			result = Files.readAllBytes(file.toPath());
		} catch (IOException e)
		{

		}

		return result;
	}

	/**
	 * Writes a string to a file
	 * @param file file path
	 * @param content what to write into the file
	 * @throws IOException IO exception
	 */
	public static void writeFile(String file, String content) throws IOException
	{
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.write(content);

		} finally
		{
			if (writer != null)
				writer.close();

		}
	}
	

	public static void writeByteArrayToFile(String filepath, byte[] content) throws IOException
	{
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filepath);
			fos.write(content);
			fos.close();

		} finally
		{
			if (fos != null)
				fos.close();

		}
	}
	
	public static boolean deleteFile(String filepath)
	{
		try{
			File file = new File(filepath);
	    	
			if(file.delete()){
				System.out.println(file.getName() + " is deleted!");
				return true;
			}else{
				System.out.println("Delete operation is failed.");
				return false;
			}
		}catch(Exception e){
    		
    		e.printStackTrace();
    		return false;
		}
		
		
	   
	}

}
