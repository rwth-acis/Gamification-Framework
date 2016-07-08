package i5.las2peer.services.gamificationManagerService.level;


/**
* Level Model
* 
* This is the class to store the Level model
* 
*/

public class LevelModel{
	
	private int level_num;
	private String name;
	private int point_value = 0;
	
	public LevelModel(int level_num, String name, int point_value){
		this.level_num = level_num;
		this.name = name;
		this.point_value = point_value;
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

}
