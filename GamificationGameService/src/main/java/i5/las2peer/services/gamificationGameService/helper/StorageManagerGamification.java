package i5.las2peer.services.gamificationGameService.helper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.p2p.TimeoutException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class StorageManagerGamification {

	/**
	 * Function to store configuration
	 * @param gameId gameId
	 * @return true, if clean storage success
	 */
	public static boolean cleanStorage(String gameId){
			// RMI call without parameters
		File gameFolder = new File("../GamificationBadgeService/files/"+gameId);
		File gameFolder2 = new File("../GamificationPointService/files/"+gameId);
		
		try {
			recursiveDelete(gameFolder);
			recursiveDelete(gameFolder2);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    }
	
	public static void recursiveDelete(File gameFolder) throws IOException{
		if(gameFolder.isDirectory()){
    		//directory is empty, then delete it
    		if(gameFolder.list().length==0){
    			gameFolder.delete();
    		   System.out.println("Directory is deleted : " 
                                                 + gameFolder.getAbsolutePath());
    		}else{
    			
    		   //list all the directory contents
        	   String files[] = gameFolder.list();
     
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(gameFolder, temp);
        		 
        	      //recursive delete
        	      recursiveDelete(fileDelete);
        	   }
        		
        	   //check the directory again, if empty then delete it
        	   if(gameFolder.list().length==0){
        		   gameFolder.delete();
        	     System.out.println("Directory is deleted : " + gameFolder.getAbsolutePath());
        	   }
    		}
    	}else{
    		//if file, then delete it
    		gameFolder.delete();
    		System.out.println("File is deleted : " + gameFolder.getAbsolutePath());
    	}
	}
	
	/**
	 * Function to resize image
	 * @param inputImageRaw input image in byte array
	 * @return return resized image in byte array
	 * @throws IOException IO exception
	 * @throws IllegalArgumentException Illegal Argument Exception
	 */
	public static byte[] resizeImage(byte[] inputImageRaw) throws IllegalArgumentException, IOException{

		BufferedImage img = ImageIO.read(new ByteArrayInputStream(inputImageRaw));
		BufferedImage newImg = Scalr.resize(img,Mode.AUTOMATIC,300,300);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(newImg, "png", baos);
		baos.flush();
		byte[] output = baos.toByteArray();
		baos.close();
		return output;
		
	}
	
	/**
	 * Function to store badge image in storage
	 * @param gameId game id
	 * @param badgeId badge id
	 * @param filename file name
	 * @param filecontent file data
	 * @param mimeType mime type code
	 * @param description description of the badge image
	 * @throws IOException IO Exception
	 * @throws AgentNotFoundException AgentNotFoundException
	 * @throws InternalServiceException InternalServiceException
	 * @throws InterruptedException InterruptedException
	 * @throws TimeoutException TimeoutException
	 * @throws IOException IOException
	 */
	
	public static void storeBadgeData(String gameId, String badgeId, String filename, byte[] filecontent, String mimeType, String description) throws AgentNotFoundException, InternalServiceException, InterruptedException, TimeoutException, IOException{
			// RMI call without parameters
		File gameFolder = new File("../GamificationBadgeService/files/"+gameId);
		if(!gameFolder.exists()){
			if(gameFolder.mkdir()){
				System.out.println("New directory "+ gameId +" is created!");
			}
			else{
				System.out.println("Failed to create directory");
			}
		}
		LocalFileManager.writeByteArrayToFile("../GamificationBadgeService/files/"+gameId+"/"+badgeId, filecontent);

//		Object result = this.invokeServiceMethod("i5.las2peer.services.fileService.FileService@1.0", "storeFile", new Serializable[] {(String) badgeid, (String) filename, (byte[]) filecontent, (String) mimeType, (String) description});
	}
	
	public static byte[] fetchBadgeData(String gameId, String badgeId){
		byte[] filecontent = LocalFileManager.getFile("../GamificationBadgeService/files/"+gameId+"/"+badgeId);
		return filecontent;
	}

	public static JSONObject fetchConfigurationToSystem(String gameId) throws IOException {
		String confPath = "../GamificationPointService/files/"+gameId+"/conf.json";
		// RMI call without parameters
		File gameFolder = new File("../GamificationPointService/files/"+gameId);
		if(!gameFolder.exists()){
			if(gameFolder.mkdir()){
				System.out.println("New directory "+ gameId +" is created!");
			}
			else{
				System.out.println("Failed to create directory");
			}
		}
		File fileconf = new File(confPath);
		if(!fileconf.exists()) {
			if(fileconf.createNewFile()){
				// Initialize
				LocalFileManager.writeFile(confPath, "{}");
				System.out.println("New file is created!");
			}
			else{
				System.out.println("Failed to create file");
			}
		} 
		String confJSONByte = new String(LocalFileManager.getFile(gameId+"/conf.json"));
		return (JSONObject) JSONValue.parse(confJSONByte);
	}
	
	/**
	 * Function to store configuration
	 * @param gameId gameId
	 * @param obj JSON object
	 * @throws IOException IO Exception
	 */
	public static void storeConfigurationToSystem(String gameId, JSONObject obj) throws IOException{
		String confPath = "../GamificationPointService/files/"+gameId+"/conf.json";
			// RMI call without parameters
		File gameFolder = new File("../GamificationPointService/files/"+gameId);
		if(!gameFolder.exists()){
			if(gameFolder.mkdir()){
				System.out.println("New directory "+ gameId +" is created!");
			}
			else{
				System.out.println("Failed to create directory");
			}
		}
		File fileconf = new File(confPath);
		if(!fileconf.exists()) {
			if(fileconf.createNewFile()){
				// Initialize
				LocalFileManager.writeFile(confPath, "{}");
				System.out.println("New file is created!");
			}
			else{
				System.out.println("Failed to create file");
			}
		} 
		LocalFileManager.writeFile("../GamificationPointService/files/"+gameId+"/conf.json", obj.toJSONString());
	}
}
