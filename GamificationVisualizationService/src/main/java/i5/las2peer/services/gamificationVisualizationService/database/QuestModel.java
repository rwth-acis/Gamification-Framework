package i5.las2peer.services.gamificationVisualizationService.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
* Quest Model
* 
* This is the class to store the Quest model
* 
*/

@ApiModel( value = "QuestModel", description = "Quest resource representation" )
public class QuestModel{

	public static enum QuestStatus {
		COMPLETED,
		REVEALED,
		HIDDEN
	}
	
	// Constraint for the quest to be revealed
	public static enum QuestConstraint{
		QUEST,
		POINT,
		BOTH,
		NONE
	}

	@ApiModelProperty( value = "Quest ID", required = true ) 
	private String quest_id;
	@ApiModelProperty( value = "Quest name", required = true ) 
	private String name;
	@ApiModelProperty( value = "Quest description") 
	private String description;
	@ApiModelProperty( value = "Quest status", required = true ) 
	private QuestStatus status;
	@ApiModelProperty( value = "Achievement ID", required = true ) 
	private String achievement_id;
	@ApiModelProperty( value = "Quest flag", required = true ) 
	private boolean quest_flag = false;
	@ApiModelProperty( value = "Quest ID Completed") 
	private String quest_id_completed;
	@ApiModelProperty( value = "Point flag", required = true ) 
	private boolean point_flag = false;
	@ApiModelProperty( value = "Point value") 
	private int point_value = 0;
	@ApiModelProperty( value = "Action IDs", required = true ) 
	private List<Pair<String, Integer>> action_ids;
	@ApiModelProperty( value = "Use notification status", required = true ) 
	private boolean use_notification;
	@ApiModelProperty( value = "Notification Message") 
	private String notif_message;

	
	public QuestModel(String quest_id, String name, String description, QuestStatus status, String achievement_id, boolean quest_flag, String quest_id_completed, boolean point_flag, int point_value, boolean use_notification, String notif_message){
		this.quest_id = quest_id;
		this.name = name;
		this.description = description;
		this.status = status;
		this.achievement_id = achievement_id;
		this.quest_flag = quest_flag;
		this.quest_id_completed = quest_id_completed;
		this.point_flag = point_flag;
		this.point_value = point_value;
		this.use_notification = use_notification;
		this.notif_message = notif_message;
	}
	
	/**
	 * Getter for quest flag
	 * 
	 * @return quest flag
	 */
	public boolean getQuestFlag(){
		return this.quest_flag;
	}
	
	/**
	 * Setter for quest flag
	 * 
	 * @param quest_flag quest flag
	 */
	public void setQuestFlag(boolean quest_flag){
		this.quest_flag = quest_flag;
	}

	
	/**
	 * Getter for completed quest ID
	 * 
	 * @return completed quest ID
	 */
	public String getQuestIdCompleted(){
		return this.quest_id_completed;
	}
	
	/**
	 * Setter for completed quest ID
	 * 
	 * @param quest_id_completed completed quest ID
	 */
	public void setQuestIdCompleted(String quest_id_completed){
		this.quest_id_completed =  quest_id_completed;
	}
	
	/**
	 * Getter for point flag
	 * 
	 * @return point flag
	 */
	public boolean getPointFlag(){
		return this.point_flag;
	}
	
	/**
	 * Setter for point flag
	 * 
	 * @param point_flag point flag
	 */
	public void setPointFlag(boolean point_flag){
		this.point_flag = point_flag;
	}
	
	/**
	 * Getter for point value
	 * 
	 * @return point value
	 */
	public int getPointValue(){
		return this.point_value;
	}
	
	/**
	 * Setter for point value
	 * 
	 * @param point_value point value
	 */
	public void setPointValue(int point_value){
		this.point_value =  point_value;
	}
	
	/**
	 * Getter for action ids used by quest
	 * 
	 * @return action ids
	 */
	public List<Pair<String, Integer>> getActionIds(){
		return this.action_ids;
	}
	
	/**
	 * Setter for action ids used by quest
	 * 
	 * @param action_ids list of action id with times
	 * @throws IOException io exception
	 */
	public void setActionIds(List<Pair<String, Integer>> action_ids) throws IOException{
		this.action_ids = new ArrayList<Pair<String,Integer>>();
		
		if(action_ids.isEmpty()){
			throw new IOException("List cannot be empty.");
		}
		for(Pair<String,Integer> p: action_ids){
			this.action_ids.add(Pair.of(p.getLeft(), p.getRight()));
		}
	}
	
	/**
	 * Getter for variable quest id
	 * 
	 * @return id of a quest
	 */
	public String getId(){
		return this.quest_id;
	}
	
	/**
	 * Setter for variable quest id
	 * 
	 * @param quest_id id of a quest
	 */
	public void setId(String quest_id){
		this.quest_id = quest_id;
	}
	
	/**
	 * Getter for variable name
	 * 
	 * @return name of a quest
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Setter for variable name
	 * 
	 * @param name name of a quest
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Getter for description
	 * 
	 * @return description of a quest
	 */
	public String getDescription(){
		return this.description;
	}
	
	/**
	 * Setter for description
	 * 
	 * @param description description of a quest
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**
	 * Getter for quest status
	 * 
	 * @return quest status
	 */
	public QuestStatus getStatus(){
		return this.status;
	}
	
	/**
	 * Setter for quest status
	 * 
	 * @param status quest status
	 */
	public void setStatus(QuestStatus status){
		this.status = status;
	}
	
	/**
	 * Getter for achievement id
	 * 
	 * @return id of achievement
	 */
	public String getAchievementId(){
		return this.achievement_id;
	}
	
	/**
	 * Setter for variable id
	 * 
	 * @param achievement_id id of achievement
	 */
	public void setAchievementId(String achievement_id){
		this.achievement_id = achievement_id;
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
