package i5.las2peer.services.badgeService.database;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
* Badge Model
* 
* This is the class to store the Badge model
* 
*/

public class BadgeModel{// implements Serializable{
	
//	private static final long serialVersionUID = -300657519857636303L;
	private String id;
	private String name;
	private String image_path;
	//private BufferedImage badge_image;
	private String desc;
	
	public BadgeModel(String id, String name, String desc, String image_path){
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.image_path = image_path;
		//this.badge_image = badge_image;
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
	 * Getter for desc
	 * 
	 * @return desx of a badge
	 */
	public String getDescription(){
		return this.desc;
	}
	
	/**
	 * Setter for image
	 * 
	 * @param desc desc of a badge
	 */
	public void setDescription(String desc){
		this.desc = desc;
	}
	
	/**
	 * Getter for image path
	 * 
	 * @return image path of a badge
	 */
	public String getImagePath(){
		return image_path;
	}
	
	/**
	 * Setter for image path
	 * 
	 * @param image_path image path of a badge
	 */
	public void setImagePath(String image_path){
		this.image_path = image_path;
	}
	
//	/**
//	 * Getter for image
//	 * 
//	 * @return image of a badge
//	 */
//	public BufferedImage getImage(){
//		return badge_image;
//	}
//	
//	/**
//	 * Setter for image
//	 * 
//	 * @param bImage image of a badge
//	 */
//	public void setImage(BufferedImage bImage){
//		this.badge_image = bImage;
//	}
	
}
