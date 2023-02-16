package i5.las2peer.services.gamificationGameService.database;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
* Game Model
* 
* This is the class to store the Game model
* 
*/

@ApiModel( value = "GameModel", description = "Game data resource representation" )
public class GameModel{
	@ApiModelProperty( value = "Game ID", required = true ) 
	private String game_id;
	@ApiModelProperty( value = "Game description", required = true ) 
	private String game_desc;
	@ApiModelProperty( value = "Community type of game", required = true ) 
	private String community_type;

	public GameModel(String game_id, String game_desc, String community_type){
		this.game_id = game_id;
		this.game_desc = game_desc;
		this.community_type = community_type;
	}

	/**
	 * Getter for variable game id
	 * 
	 * @return game_id id of an game
	 */
	public String getId(){
		return this.game_id;
	}
	
	/**
	 * Setter for variable game id
	 * 
	 * @param game_id id of an game
	 */
	public void setId(String game_id){
		this.game_id = game_id;
	}
	
	/**
	 * Getter for game's description
	 * 
	 * @return game_desc game description
	 */
	public String getDescription(){
		return this.game_desc;
	}
	
	/**
	 * Setter for game's description
	 * 
	 * @param game_desc game description
	 */
	public void setDescription(String game_desc){
		this.game_desc = game_desc;
	}
	
	/**
	 * Getter for community type
	 * 
	 * @return community_type community type
	 */
	public String getCommType(){
		return this.community_type;
	}
	
	/**
	 * Setter for community type
	 * 
	 * @param community_type community type
	 */
	public void setCommType(String community_type){
		this.community_type = community_type;
	}
	
}
