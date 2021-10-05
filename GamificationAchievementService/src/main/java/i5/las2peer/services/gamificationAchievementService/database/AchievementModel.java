package i5.las2peer.services.gamificationAchievementService.database;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
* Achievement Model
* 
* This is the class to store the Achievement model
* 
*/


@ApiModel( value = "AchievementModel", description = "Achievement resource representation" )
public class AchievementModel{

	@ApiModelProperty( value = "Achievement ID", required = true ) 
	private String achievement_id;
	@ApiModelProperty( value = "Achievement name", required = true ) 
	private String name;
	@ApiModelProperty( value = "Achievement description") 
	private String description;
	@ApiModelProperty( value = "Achievement point value") 
	private int point_value;
	@ApiModelProperty( value = "Achievement badge") 
	private String badge_id;
	@ApiModelProperty( value = "Use notification status", required = true ) 
	private boolean use_notification;
	@ApiModelProperty( value = "Notification Message") 
	private String notif_message;
	
	
	public AchievementModel(String achievement_id, String name, String description, int point_value, String badge_id, boolean use_notification, String notif_message){
		this.achievement_id = achievement_id;
		this.name = name;
		this.description = description;
		this.point_value = point_value;
		this.badge_id = badge_id;
		this.use_notification = use_notification;
		this.notif_message = notif_message;
	}
	
	/**
	 * Getter for variable id
	 * 
	 * @return id of an achievement
	 */
	public String getId(){
		return achievement_id;
	}
	
	/**
	 * Setter for variable id
	 * 
	 * @param achievement_id id of an achievement
	 */
	public void setId(String achievement_id){
		this.achievement_id = achievement_id;
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
	 * Getter for badge id of an achievement
	 * 
	 * @return badge id of an achievement
	 */
	public String getBadgeId(){
		return this.badge_id;
	}
	
	/**
	 * Setter for badge id of an achievement
	 * 
	 * @param badge_id badge id of an achievement
	 */
	public void setBadgeId(String badge_id){
		this.badge_id = badge_id;
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
