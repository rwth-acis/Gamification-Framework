package i5.las2peer.services.gamificationManagerService.quest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
* Quest Model
* 
* This is the class to store the Quest model
* 
*/

public class QuestModel{
	
//	public static enum QuestStatus {
//		COMPLETED{
//		    public String toString() {
//		        return "COMPLETED";
//		    }
//		},
//		REVEALED{
//		    public String toString() {
//		        return "REVEALED";
//		    }
//		},
//		HIDDEN{
//		    public String toString() {
//		        return "HIDDEN";
//		    }
//		}
//	}
	
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
	
	private String quest_id;
	private String name;
	private String description;
//	private String status;
	private QuestStatus status;
	private String achievement_id;
	private boolean quest_flag = false;
	private String quest_id_completed;
	private boolean point_flag = false;
	private int point_value = 0;
	private List<Pair<String, Integer>> action_ids;
	
	public QuestModel(String quest_id, String name, String description, QuestStatus status, String achievement_id){
		this.quest_id = quest_id;
		this.name = name;
		this.description = description;
//		this.setStatus(this.status);
		this.status = status;
		this.achievement_id = achievement_id;
	}
	
	public QuestModel(String quest_id, String name, String description, QuestStatus status, String achievement_id, boolean quest_flag, String quest_id_completed, boolean point_flag, int point_value){
		this.quest_id = quest_id;
		this.name = name;
		this.description = description;
		this.status = status;
		this.achievement_id = achievement_id;
		this.quest_flag = quest_flag;
		this.quest_id_completed = quest_id_completed;
		this.point_flag = point_flag;
		this.point_value = point_value;
	}
	
	public void setConstraint(int point_value){
		this.point_flag = true;
		this.quest_flag = false;
		this.point_value = point_value;
		this.quest_id_completed = null;
	}
	
	public void setConstraint(String quest_id_completed){
		this.point_flag = false;
		this.quest_flag = true;
		this.point_value = 0;
		this.quest_id_completed = quest_id_completed;
	}
	
	public void setConstraint(String quest_id_completed, int point_value){
		this.point_flag = true;
		this.quest_flag = true;
		this.point_value = point_value;
		this.quest_id_completed = quest_id_completed;
	}
	
	public QuestConstraint getConstraint(){
		if(point_flag && quest_flag){
			return QuestConstraint.BOTH;
		}
		else if(point_flag && !quest_flag){
			return QuestConstraint.POINT;
		}
		else if(!point_flag && quest_flag){
			return QuestConstraint.QUEST;
		}
		else{
			return QuestConstraint.NONE;
		}
	}
	
	public void setQuestFlag(boolean quest_flag){
		this.quest_flag = quest_flag;
	}
	public void setPointFlag(boolean point_flag){
		this.point_flag = point_flag;
	}
	public boolean getQuestFlag(){
		return this.quest_flag;
	}
	public boolean getPointFlag(){
		return this.point_flag;
	}
	
	public void setQuestIdCompleted(String quest_id_completed){
		this.quest_id_completed =  quest_id_completed;
	}
	public String getQuestIdCompleted(){
		return this.quest_id_completed;
	}
	
	public void setPointValue(int point_value){
		this.point_value =  point_value;
	}
	public int getPointValue(){
		return this.point_value;
	}
	
	public void setActionIds(List<Pair<String, Integer>> action_ids) throws IOException{
		this.action_ids = new ArrayList<Pair<String,Integer>>();
		
		if(action_ids.isEmpty()){
			throw new IOException("List cannot be empty.");
		}
		for(Pair<String,Integer> p: action_ids){
			this.action_ids.add(Pair.of(p.getLeft(), p.getRight()));
		}
	}
	
	public List<Pair<String, Integer>> getActionIds(){
		return this.action_ids;
	}
	
	/**
	 * Getter for variable id
	 * 
	 * @return id of a quest
	 */
	public String getId(){
		return this.quest_id;
	}
	
	/**
	 * Setter for variable id
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
	
//	/**
//	 * Getter for quest status
//	 * 
//	 * @return quest status
//	 */
//	public QuestStatus getStatus(){
//		return this.statusEnum;
//	}
//	
//	/**
//	 * Setter for quest status
//	 * 
//	 * @param statusEnum quest status
//	 */
//	public void setStatus(QuestStatus statusEnum){
//		this.status = statusEnum.toString();
//		this.statusEnum = statusEnum;
//	}
//	public void setStatus(String status){
//		this.status = status;
//		if(status.equals(QuestStatus.COMPLETED.toString())){
//			this.statusEnum = QuestStatus.COMPLETED;
//		}
//		else if(status.equals(QuestStatus.HIDDEN.toString())){
//			this.statusEnum = QuestStatus.HIDDEN;
//		}
//		else if(status.equals(QuestStatus.REVEALED.toString())){
//			this.statusEnum = QuestStatus.REVEALED;
//		}
//	}
	
	/**
	 * Getter for quest status
	 * 
	 * @return quest status
	 */
	public QuestStatus getStatus(){
		return this.status;
	}
	
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
}
