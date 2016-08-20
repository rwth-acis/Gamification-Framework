package i5.las2peer.services.gamificationVisualizationService.database;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
* User Model
* 
* This is the class to store the User model
* 
*/

@ApiModel( value = "MemberModel", description = "Member data resource representation" )
public class MemberModel{
	@ApiModelProperty( value = "Member ID", required = true ) 
	private String member_id;
	@ApiModelProperty( value = "Member first name", required = true )
	private String first_name;
	@ApiModelProperty( value = "Member last name", required = true )
	private String last_name;
	@ApiModelProperty( value = "Member e-mail", required = true )
	private String email;

	public MemberModel(String member_id, String first_name, String last_name, String email){
		this.member_id = member_id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
	}
	
	/**
	 * Getter for variable id
	 * 
	 * @return member_id of a user
	 */
	public String getId(){
		return this.member_id;
	}
	
	/**
	 * Setter for variable id
	 * 
	 * @param member_id id of a member
	 */
	public void setId(String member_id){
		this.member_id = member_id;
	}
	
	/**
	 * Getter for member's first name
	 * 
	 * @return first name of a member
	 */
	public String getFirstName(){
		return this.first_name;
	}
	
	/**
	 * Setter for member's first name
	 * 
	 * @param first_name first name of a member
	 */
	public void setFirstName(String first_name){
		this.first_name = first_name;
	}

	/**
	 * Getter for member's last name
	 * 
	 * @return last name of a member
	 */
	public String getLastName(){
		return this.last_name;
	}
	
	/**
	 * Setter for member's last name
	 * 
	 * @param last_name last name of a member
	 */
	public void setLastName(String last_name){
		this.last_name = last_name;
	}

	/**
	 * Getter for member's full name
	 * 
	 * @return full name of a member
	 */
	public String getFullName(){
		return (this.first_name+" "+this.last_name);
	}
	
	/**
	 * Getter for email
	 * 
	 * @return email of a member
	 */
	public String getEmail(){
		return this.email;
	}
	
	/**
	 * Setter for email
	 * 
	 * @param email email of a member
	 */
	public void setEmail(String email){
		this.email = email;
	}
	
}
