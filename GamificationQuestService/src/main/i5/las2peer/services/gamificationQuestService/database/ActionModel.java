package i5.las2peer.services.gamificationQuestService.database;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;





/**
* Action Model
* 
* This is the class to store the Badge model
* 
*/

@ApiModel( value = "ActionModel", description = "Action resource representation" )
public class ActionModel{
	

	@ApiModelProperty( value = "Action ID", required = true ) 
	private String action_id;
	@ApiModelProperty( value = "Action name", required = true ) 
	private String name;
	@ApiModelProperty( value = "Action description") 
	private String description;
	@ApiModelProperty( value = "Action point value") 
	private int point_value = 0;
	@ApiModelProperty( value = "Use notification status", required = true ) 
	private boolean use_notification;
	@ApiModelProperty( value = "Notification Message") 
	private String notif_message;
	
	public ActionModel(String action_id, String name, String description, int point_value, boolean use_notification, String notif_message){
		this.action_id = action_id;
		this.name = name;
		this.description = description;
		this.point_value = point_value;
		this.use_notification = use_notification;
		this.notif_message = notif_message;
	}
	
	/**
	 * Getter for variable id
	 * 
	 * @return id of an action
	 */
	public String getId(){
		return action_id;
	}
	
	/**
	 * Setter for variable id
	 * 
	 * @param action_id id of an action
	 */
	public void setId(String action_id){
		this.action_id = action_id;
	}
	
	/**
	 * Getter for variable name
	 * 
	 * @return name of an achievement
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Setter for variable name
	 * 
	 * @param name name of an achievement
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Getter for description
	 * 
	 * @return description of an achievement
	 */
	public String getDescription(){
		return this.description;
	}
	
	/**
	 * Setter for description
	 * 
	 * @param description description of an achievement
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**
	 * Getter for point value
	 * 
	 * @return point value of an achievement
	 */
	public int getPointValue(){
		return this.point_value;
	}
	
	/**
	 * Setter for point value
	 * 
	 * @param point_value point value of an achievement
	 */
	public void setPointValue(int point_value){
		this.point_value = point_value;
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
