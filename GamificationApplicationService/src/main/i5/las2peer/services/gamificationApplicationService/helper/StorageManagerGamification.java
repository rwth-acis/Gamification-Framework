package i5.las2peer.services.gamificationApplicationService.helper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import i5.las2peer.execution.L2pServiceException;
import i5.las2peer.p2p.AgentNotKnownException;
import i5.las2peer.p2p.TimeoutException;
import i5.las2peer.security.L2pSecurityException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class StorageManagerGamification {

	/**
	 * Function to store configuration
	 * @param appId appId
	 * @return true, if clean storage success
	 */
	public static boolean cleanStorage(String appId){
			// RMI call without parameters
		File appFolder = new File("../GamificationBadgeService/files/"+appId);
		File appFolder2 = new File("../GamificationPointService/files/"+appId);
		
		try {
			recursiveDelete(appFolder);
			recursiveDelete(appFolder2);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    }
	
	public static void recursiveDelete(File appFolder) throws IOException{
		if(appFolder.isDirectory()){
    		//directory is empty, then delete it
    		if(appFolder.list().length==0){
    			appFolder.delete();
    		   System.out.println("Directory is deleted : " 
                                                 + appFolder.getAbsolutePath());
    		}else{
    			
    		   //list all the directory contents
        	   String files[] = appFolder.list();
     
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(appFolder, temp);
        		 
        	      //recursive delete
        	      recursiveDelete(fileDelete);
        	   }
        		
        	   //check the directory again, if empty then delete it
        	   if(appFolder.list().length==0){
        		   appFolder.delete();
        	     System.out.println("Directory is deleted : " + appFolder.getAbsolutePath());
        	   }
    		}
    	}else{
    		//if file, then delete it
    		appFolder.delete();
    		System.out.println("File is deleted : " + appFolder.getAbsolutePath());
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
	 * @param appId application id
	 * @param badgeId badge id
	 * @param filename file name
	 * @param filecontent file data
	 * @param mimeType mime type code
	 * @param description description of the badge image
	 * @throws IOException IO Exception
	 * @throws AgentNotKnownException AgentNotKnownException
	 * @throws L2pServiceException L2pServiceException
	 * @throws L2pSecurityException L2pSecurityException
	 * @throws InterruptedException InterruptedException
	 * @throws TimeoutException TimeoutException
	 * @throws IOException IOException
	 */
	public static void storeBadgeData(String appId, String badgeId, String filename, byte[] filecontent, String mimeType, String description) throws AgentNotKnownException, L2pServiceException, L2pSecurityException, InterruptedException, TimeoutException, IOException{
			// RMI call without parameters
		File appFolder = new File("../GamificationBadgeService/files/"+appId);
		if(!appFolder.exists()){
			if(appFolder.mkdir()){
				System.out.println("New directory "+ appId +" is created!");
			}
			else{
				System.out.println("Failed to create directory");
			}
		}
		LocalFileManager.writeByteArrayToFile("../GamificationBadgeService/files/"+appId+"/"+badgeId, filecontent);

//		Object result = this.invokeServiceMethod("i5.las2peer.services.fileService.FileService@1.0", "storeFile", new Serializable[] {(String) badgeid, (String) filename, (byte[]) filecontent, (String) mimeType, (String) description});
	}
	
	public static byte[] fetchBadgeData(String appId, String badgeId){
		byte[] filecontent = LocalFileManager.getFile("../GamificationBadgeService/files/"+appId+"/"+badgeId);
		return filecontent;
	}

	public static JSONObject fetchConfigurationToSystem(String appId) throws IOException {
		String confPath = "../GamificationPointService/files/"+appId+"/conf.json";
		// RMI call without parameters
		File appFolder = new File("../GamificationPointService/files/"+appId);
		if(!appFolder.exists()){
			if(appFolder.mkdir()){
				System.out.println("New directory "+ appId +" is created!");
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
		String confJSONByte = new String(LocalFileManager.getFile(appId+"/conf.json"));
		return (JSONObject) JSONValue.parse(confJSONByte);
	}
	
	/**
	 * Function to store configuration
	 * @param appId appId
	 * @param obj JSON object
	 * @throws IOException IO Exception
	 */
	public static void storeConfigurationToSystem(String appId, JSONObject obj) throws IOException{
		String confPath = "../GamificationPointService/files/"+appId+"/conf.json";
			// RMI call without parameters
		File appFolder = new File("../GamificationPointService/files/"+appId);
		if(!appFolder.exists()){
			if(appFolder.mkdir()){
				System.out.println("New directory "+ appId +" is created!");
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
		LocalFileManager.writeFile("../GamificationPointService/files/"+appId+"/conf.json", obj.toJSONString());
	}
}
