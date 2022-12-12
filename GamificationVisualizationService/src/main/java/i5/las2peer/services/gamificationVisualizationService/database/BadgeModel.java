package i5.las2peer.services.gamificationVisualizationService.database;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
* Badge Model
* 
* This is the class to store the Badge model
* 
*/

@ApiModel( value = "BadgeModel", description = "Badge resource representation" )
public class BadgeModel{
	@ApiModelProperty( value = "Badge ID", required = true ) 
	private String id;
	@ApiModelProperty( value = "Badge name", required = true ) 
	private String name;
	@ApiModelProperty( value = "Badge description") 
	private String description;
	@ApiModelProperty( value = "Use notification status", required = true ) 
	private boolean use_notification;
	@ApiModelProperty( value = "Notification Message") 
	private String notif_message;

	@ApiModelProperty( value = "Base64") 
	private String base64;
	
	public BadgeModel(String id, String name, String description, boolean use_notification, String notif_message){
		this.id = id;
		this.name = name;
		this.description = description;
		this.use_notification = use_notification;
		this.notif_message = notif_message;
	}
	
	/**
	 * Getter for variable id
	 * 
	 * @return id of a badge
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * Setter for variable id
	 * 
	 * @param id id of a badge
	 */
	public void setId(String id){
		this.id = id;
	}
	
	/**
	 * Setter for variable id
	 * 
	 */
	public String getBase64(){
		return this.base64;
	}

	/**
	 * Setter for variable id
	 * 
	 * @param base64 base64 of a badge
	 */
	public void setBase64(String base64){
		this.base64 = base64;
	}

	/**
	 * Getter for variable name
	 * 
	 * @return name of a badge
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Setter for variable name
	 * 
	 * @param name name of a badge
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Getter for description
	 * 
	 * @return description of a badge
	 */
	public String getDescription(){
		return this.description;
	}
	
	/**
	 * Setter for description
	 * 
	 * @param description description of a badge
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	
	/**
	 * Getter for use notification status
	 * 
	 * @return use notification status
	 */
	public boolean isUseNotification(){
		return this.use_notification;
	}
	
	/**
	 * Setter for use notification status
	 * 
	 * @param use_notification use notification status
	 */
	public void useNotification(boolean use_notification){
		this.use_notification = use_notification;
	}
	
	/**
	 * Getter for notification message
	 * 
	 * @return notification message
	 */
	public String getNotificationMessage(){
		return this.notif_message;
	}
	
	/**
	 * Setter for notification message
	 * 
	 * @param notif_message notification message of a badge
	 */
	public void setNotificationMessage(String notif_message){
		this.notif_message = notif_message;
	}
	
	
}
