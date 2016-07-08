package i5.las2peer.services.gamificationManagerService.badge;

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
	@ApiModelProperty( value = "Badge description", required = true ) 
	private String description;
	@ApiModelProperty( value = "Badge image path", required = true ) 
	private String imagepath;
	
	public BadgeModel(String id, String name, String description, String imagepath){
		this.id = id;
		this.name = name;
		this.description = description;
		this.imagepath = imagepath;
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
	 * Getter for image path
	 * 
	 * @return image path of a badge
	 */
	public String getImagePath(){
		return imagepath;
	}
	
	/**
	 * Setter for image path
	 * 
	 * @param imagepath image path of a badge
	 */
	public void setImagePath(String imagepath){
		this.imagepath = imagepath;
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
