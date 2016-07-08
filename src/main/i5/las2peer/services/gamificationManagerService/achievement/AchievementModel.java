package i5.las2peer.services.gamificationManagerService.achievement;


/**
* Achievement Model
* 
* This is the class to store the Achievement model
* 
*/

public class AchievementModel{
	
	private String achievement_id;
	private String name;
	private String description;
	private int point_value;
	private String badge_id;
	
	public AchievementModel(String achievement_id, String name, String description, int point_value, String badge_id){
		this.achievement_id = achievement_id;
		this.name = name;
		this.description = description;
		this.point_value = point_value;
		this.badge_id = badge_id;
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
}
