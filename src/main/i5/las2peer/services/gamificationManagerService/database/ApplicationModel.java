package i5.las2peer.services.gamificationManagerService.database;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
* Application Model
* 
* This is the class to store the Application model
* 
*/

@ApiModel( value = "ApplicationModel", description = "Application data resource representation" )
public class ApplicationModel{
	@ApiModelProperty( value = "Application ID", required = true ) 
	private String app_id;
	@ApiModelProperty( value = "Application description", required = true ) 
	private String app_desc;
	@ApiModelProperty( value = "Community type of application", required = true ) 
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
	public String getDescription(){
		return this.app_desc;
	}
	
	/**
	 * Setter for application's description
	 * 
	 * @param app_desc application description
	 */
	public void setDescription(String app_desc){
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
