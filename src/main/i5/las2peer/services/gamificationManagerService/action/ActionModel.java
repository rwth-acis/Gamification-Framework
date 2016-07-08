package i5.las2peer.services.gamificationManagerService.action;


/**
* Action Model
* 
* This is the class to store the Badge model
* 
*/

public class ActionModel{
	
	private String action_id;
	private String name;
	private String description;
	private int point_value = 0;
	
	public ActionModel(String action_id, String name, String description, int point_value){
		this.action_id = action_id;
		this.name = name;
		this.description = description;
		this.point_value = point_value;
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
}
