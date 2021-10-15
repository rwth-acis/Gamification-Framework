package i5.las2peer.services.gamification.listener;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
* Config Model
* 
* This is the class to store the Config model
* 
*/
@ApiModel( value = "ConfigModel", description = "Config resource representation" )
public class ConfigModel {
	
	@ApiModelProperty( value = "Config ID", required = true ) 
	private String config_id;
	@ApiModelProperty( value = "Config name", required = true ) 
	private String name;
	@ApiModelProperty( value = "Config description") 
	private String description;
	
	@ApiModelProperty( value = "Listen games", required = true ) 
	private GameModel game;
	@ApiModelProperty( value = "Listen quests", required = true ) 
	private QuestModel quest;
	@ApiModelProperty( value = "Listen actions", required = true ) 
	private ActionModel action;
	@ApiModelProperty( value = "Listen points", required = true ) 
	private PointModel point;
	@ApiModelProperty( value = "Listen achievements", required = true ) 
	private AchievementModel achievement;
	@ApiModelProperty( value = "Listen badges", required = true ) 
	private BadgeModel badge;
	@ApiModelProperty( value = "Listen levels", required = true ) 
	private LevelModel level;
	@ApiModelProperty( value = "Listens gamifiers")
	private Map<String, String> gamifier;
	@ApiModelProperty( value = "Listen visualizers") 
	private Map<String, String> visualization;
	
	
	
	public ConfigModel(Object game){
	}
}
