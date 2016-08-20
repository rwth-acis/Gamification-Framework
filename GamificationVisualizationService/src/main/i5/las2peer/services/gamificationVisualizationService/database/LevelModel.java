package i5.las2peer.services.gamificationVisualizationService.database;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
* Level Model
* 
* This is the class to store the Level model
* 
*/

@ApiModel( value = "LevelModel", description = "Level resource representation" )
public class LevelModel{

	@ApiModelProperty( value = "Level number", required = true ) 
	private int level_num;
	@ApiModelProperty( value = "Level name", required = true ) 
	private String name;
	@ApiModelProperty( value = "Level point value threshold") 
	private int point_value = 0;
	@ApiModelProperty( value = "Use notification status", required = true ) 
	private boolean use_notification;
	@ApiModelProperty( value = "Notification Message") 
	private String notif_message;
	
	public LevelModel(int level_num, String name, int point_value, boolean use_notification, String notif_message){
		this.level_num = level_num;
		this.name = name;
		this.point_value = point_value;
		this.use_notification = use_notification;
		this.notif_message = notif_message;
	}
	
	/**
	 * Getter for variable level number
	 * 
	 * @return level_num level number
	 */
	public int getNumber(){
		return level_num;
	}
	
	/**
	 * Setter for variable level number
	 * 
	 * @param level_num level number
	 */
	public void setNumber(int level_num){
		this.level_num = level_num;
	}
	
	/**
	 * Getter for variable name
	 * 
	 * @return name of a level
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Setter for variable name
	 * 
	 * @param name name of a level
	 */
	public void setName(String name){
		this.name = name;
	}
	
	
	/**
	 * Getter for point value
	 * 
	 * @return point value of a level
	 */
	public int getPointValue(){
		return this.point_value;
	}
	
	/**
	 * Setter for point value
	 * 
	 * @param point_value point value of a level
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
