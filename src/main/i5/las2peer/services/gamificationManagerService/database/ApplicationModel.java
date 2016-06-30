package i5.las2peer.services.gamificationManagerService.database;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
* Application Model
* 
* This is the class to store the Application model
* 
*/

public class ApplicationModel{
	
	private String app_id;
	private String app_desc;
	private String community_type;

	public ApplicationModel(String app_id, String app_desc, String community_type){
		this.app_id = app_id;
		this.app_desc = app_desc;
		this.community_type = community_type;
	}

	/**
	 * Getter for variable app id
	 * 
	 * @return app_id id of an app
	 */
	public String getId(){
		return this.app_id;
	}
	
	/**
	 * Setter for variable app id
	 * 
	 * @param app_id id of an app
	 */
	public void setId(String app_id){
		this.app_id = app_id;
	}
	
	/**
	 * Getter for application's description
	 * 
	 * @return app_desc application description
	 */
	public String getAppDescription(){
		return this.app_desc;
	}
	
	/**
	 * Setter for application's description
	 * 
	 * @param app_desc application description
	 */
	public void setAppDescription(String app_desc){
		this.app_desc = app_desc;
	}
	
	/**
	 * Getter for community type
	 * 
	 * @return community_type community type
	 */
	public String getCommType(){
		return this.community_type;
	}
	
	/**
	 * Setter for community type
	 * 
	 * @param community_type community type
	 */
	public void setCommType(String community_type){
		this.community_type = community_type;
	}
	
}
