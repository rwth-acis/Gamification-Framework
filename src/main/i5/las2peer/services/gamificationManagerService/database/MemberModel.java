package i5.las2peer.services.gamificationManagerService.database;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
* User Model
* 
* This is the class to store the User model
* 
*/

public class MemberModel{
	
	private String user_id;
	private String first_name;
	private String last_name;
	private String email;

	public MemberModel(String user_id, String first_name, String last_name, String email){
		this.user_id = user_id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
	}
	
	/**
	 * Getter for variable id
	 * 
	 * @return user_id of a user
	 */
	public String getId(){
		return this.user_id;
	}
	
	/**
	 * Setter for variable id
	 * 
	 * @param user_id id of a user
	 */
	public void setId(String user_id){
		this.user_id = user_id;
	}
	
	/**
	 * Getter for user's first name
	 * 
	 * @return first name of a user
	 */
	public String getFirstName(){
		return this.first_name;
	}
	
	/**
	 * Setter for user's first name
	 * 
	 * @param first_name first name of a user
	 */
	public void setFirstName(String first_name){
		this.first_name = first_name;
	}

	/**
	 * Getter for user's last name
	 * 
	 * @return last name of a user
	 */
	public String getLastName(){
		return this.last_name;
	}
	
	/**
	 * Setter for user's last name
	 * 
	 * @param last_name last name of a user
	 */
	public void setLastName(String last_name){
		this.last_name = last_name;
	}

	/**
	 * Getter for user's full name
	 * 
	 * @return full name of a user
	 */
	public String getFullName(){
		return (this.first_name+" "+this.last_name);
	}
	
	/**
	 * Getter for email
	 * 
	 * @return email of a badge
	 */
	public String getEmail(){
		return this.email;
	}
	
	/**
	 * Setter for email
	 * 
	 * @param email email of a badge
	 */
	public void setEmail(String email){
		this.email = email;
	}
	
}
