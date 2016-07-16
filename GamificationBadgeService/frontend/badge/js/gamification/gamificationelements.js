
var ClientHelper = {
	getDataToTable : function(endPointURL, tableElement, objectFormatters, loadedHandler,callbackResponse){
		var objectgrid = $(tableElement).bootgrid({
			ajax: true,
			ajaxSettings: {
		        method: "GET",
		        cache: false
		    },
		    searchSettings: {
		        delay: 100,
		        characters: 3
		    },
		    url: useAuthentication(epURL+endPointURL),
		    formatters: objectFormatters,
		    responseHandler: function(response){
		    	callbackResponse(response);
		    	return response;
		    }
		}).on("loaded.rs.jquery.bootgrid", function()
		{
		    /* Executes after data is loaded and rendered */
		    loadedHandler(objectgrid);
		    
		});
	},
	postWithJSON : function(endPointURL, content, useNotification,successCallback, errorCallback, elementid){
		var objsent = JSON.stringify(content);
		client.sendRequest(
			"POST",
			endPointURL,
			objsent,
			"application/json",
			{},
			function(data, type){
				

				if(useNotification){
					$.notify({
						// options
						message: "<b>" + elementid + "</b> is added !"
					},{
						// settings
						type: 'success'
					});
				}
				successCallback(data,type);
				
				return false;
			},
			function(status,error) {
				if(useNotification){
					$.notify({
						// options
						message: "Failed to add <b>" + elementid + "</b> !. " + error
					},{
						// settings
						type: 'danger'
					});
				}
				errorCallback(status,error);
		        return false;
			}
		);
	},
	putWithJSON: function(endPointURL, content, useNotification,successCallback, errorCallback, elementid){
		var objsent = JSON.stringify(content);
		client.sendRequest(
			"PUT",
			endPointURL,
			objsent,
			"application/json",
			{},
			function(data, type){
				if(useNotification){
					$.notify({
					// options
					message: "<b>" + elementid + "</b> is updated !"
					},{
						// settings
						type: 'success'
					});
				}
				successCallback(data,type);
				
				return false;
			},
			function(status,error) {
				if(useNotification){
					$.notify({
						// options
						message: "Failed to update <b>" + elementid + "</b> !" + error
					},{
						// settings
						type: 'danger'
					});
				}
				errorCallback(status,error);
		        return false;
			}
		);
	},
	postWithForm:function(endPointURL, content, useNotification,successCallback, errorCallback, elementid){
		client.sendRequest(
			"POST",
			endPointURL,
			content,
			false,
			{},
			function(data, type){
				if(useNotification){
					$.notify({
						// options
						message: "new badge "+ elementid +" is added !"
					},{
						// settings
						type: 'success'
					});
				}
				successCallback(data,type);
				return false;
			},
			function(status,error) {
				if(useNotification){
			         $.notify({
						// options
						message: "Failed to add "+elementid+" !"
					},{
						// settings
						type: 'danger'
					});
		     	}
		         errorCallback(status,error);
		         return false;
			}
		);
	},
	putWithForm : function(endPointURL, content, useNotification,successCallback, errorCallback,elementid){
		client.sendRequest(
			"PUT",
			endPointURL,
			content,
			false,
			{},
			function(data, type){
				if(useNotification){
					$.notify({
					// options
						message: "<b>" + elementid + "</b> is updated !"
					},{
						// settings
						type: 'success'
					});
				}
				successCallback(data,type);
				return false;
			},
			function(error) {
				if(useNotification){
			         $.notify({
						// options
						message: "Failed to update <b>" + elementid + "</b> !"
					},{
						// settings
						type: 'danger'
					});
		     	}
		        errorCallback(status,error);
				return false;
			}
		);
	},
	deleteData : function(endPointURL, useNotification,successCallback, errorCallback,elementid){
		client.sendRequest(
			"DELETE",
			endPointURL,
			"",
			false,
			{},
			function(data, type){
			    if(useNotification){
				    $.notify({
						// options
						message: "<b>" + elementid + "</b> is deleted !"
					},{
						// settings
						type: 'success'
					});
				}
				successCallback(data,type);
				return false;
			},
			function(status,error) {
		       	if(useNotification){
			        $.notify({
						// options
						message: "Failed to delete <b>" + elementid + "</b> !"
					},{
						// settings
						type: 'danger'
					});
		    	}
		         errorCallback(status,error);
		         return false;
		    }
		);
	}

};

/*var QuestStatus = {
	    REVEALED: 0,
	    HIDDEN: 1,
	    COMPLETED: 2,
	    valueOf: function(status){
    	if(status == "REVEALED"){
    		return 0;
    	}
    	else if(status == "HIDDEN"){
    		return 1;
    	}
    	else if(status == "COMPLETED"){
    		return 2;
    	}
    	else{
    		throw "Enum type is not identified!"
    	}
    	},
	    toString: function(QuestStatus){
    	switch(QuestStatus){
    		case 0: return "REVEALED";
    		case 1: return "HIDDEN";
    		case 2: return "COMPLETED";
    		default: throw "Enum type is not identified!";
    	}
    	}
	

}

var QuestModel = function(quest_id,name,description,status,achievement_id,quest_flag,quest_id_completed,point_flag,point_value, action_ids){

	this.quest_id = quest_id;
	this.name = name;
	this.description = description;
	this.status = status;
	this.achievement_id = achievement_id;
	this.quest_flag = quest_flag;
	this.quest_id_completed = quest_id_completed;
	this.point_flag = point_flag;
	this.point_value = point_value;
	this.action_ids = action_ids;
}*/


var QuestDAO = function(){

}

QuestDAO.prototype.getQuestsDataToTable = function(tableElement, objectFormatters, loadedHandler,callbackResponse){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/quests/"+currentAppId;
	ClientHelper.getDataToTable(endPointURL, tableElement, objectFormatters, loadedHandler, callbackResponse);
}

QuestDAO.prototype.createNewQuest = function(content, useNotification,successCallback, errorCallback, questid){

	this.currentAppId = Cookies.get("appid");
	var objsent = JSON.stringify(content);
	var endPointURL = "gamification/quests/"+this.currentAppId;
	ClientHelper.postWithJSON(endPointURL, content, useNotification,successCallback, errorCallback, questid);
}

QuestDAO.prototype.updateQuest = function(content, useNotification,successCallback, errorCallback, questid){

	this.currentAppId = Cookies.get("appid");
	var objsent = JSON.stringify(content);
	var endPointURL = "gamification/quests/"+this.currentAppId+"/"+questid;
	ClientHelper.putWithJSON(endPointURL, content, useNotification,successCallback, errorCallback, questid);
}

QuestDAO.prototype.deleteQuest = function(useNotification,successCallback, errorCallback,questid){
	this.currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/quests/"+this.currentAppId+"/"+questid;
	ClientHelper.deleteData(endPointURL,useNotification,successCallback, errorCallback,questid);
}

/*var BadgeModel = function(badge_id,name,description,image_path){
	this.badge_id = badge_id;
	this.name = name;
	this.description = description;
	this.image_path = image_path;
}
*/
var BadgeDAO = function(){
}

BadgeDAO.prototype.getBadgesDataToTable = function(tableElement, objectFormatters, loadedHandler, callbackResponse){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/badges/"+currentAppId;
	ClientHelper.getDataToTable(endPointURL, tableElement, objectFormatters, loadedHandler, callbackResponse);
}

BadgeDAO.prototype.getBadgeDataWithId = function(badgeid,successCallback, errorCallback){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/badges/"+currentAppId+"/"+badgeid;
	client.sendRequest(
		"GET",
		"gamification/badges/"+currentAppId+"/"+badgeid,
		{},
		false,
		{},
		function(data,type){
			successCallback(data,type);
		},
		function(error){
			errorCallback(error);
		}
	);
}

BadgeDAO.prototype.createNewBadge = function(content, useNotification,successCallback, errorCallback, badgeid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/badges/"+currentAppId;
	ClientHelper.postWithForm(endPointURL, content, useNotification,successCallback, errorCallback, badgeid);
}

BadgeDAO.prototype.updateBadge = function(content, useNotification,successCallback, errorCallback, badgeid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/badges/"+currentAppId+"/"+badgeid;
	ClientHelper.putWithForm(endPointURL,content, useNotification,successCallback, errorCallback, badgeid);
}

BadgeDAO.prototype.deleteBadge = function(useNotification,successCallback, errorCallback, badgeid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/badges/"+currentAppId+"/" + badgeid;
	ClientHelper.deleteData(endPointURL,useNotification,successCallback, errorCallback,badgeid);
}

BadgeDAO.prototype.getBadgeImage = function(badgeid){
	currentAppId = Cookies.get("appid");

	if(!client.isAnonymous()){
		console.log("Authenticated request");
		var rurl = epURL + "gamification/badges/"+currentAppId+"/" + badgeid + "/img";
		
		return useAuthentication(rurl);
	} else {
		console.log("Anonymous request... ");
		return null;
	}
}

/*var AchievementModel = function(achievement_id,name,description,point_value,badge_id){
	this.achievement_id = achievement_id;
	this.name = name;
	this.description = description;
	this.point_value = point_value;
	this.badge_id = badge_id;
}*/

var AchievementDAO = function(){
}

AchievementDAO.prototype.getAchievementsDataToTable = function(tableElement, objectFormatters, loadedHandler, callbackResponse){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/achievements/"+currentAppId;
	ClientHelper.getDataToTable(endPointURL, tableElement, objectFormatters, loadedHandler, callbackResponse);
}

AchievementDAO.prototype.createNewAchievement = function(content, useNotification,successCallback, errorCallback, achievementid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/achievements/"+currentAppId;
	ClientHelper.postWithForm(endPointURL, content, useNotification,successCallback, errorCallback, achievementid);
}

AchievementDAO.prototype.updateAchievement = function(content, useNotification,successCallback, errorCallback, achievementid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/achievements/"+currentAppId+"/"+achievementid;
	ClientHelper.putWithForm(endPointURL,content, useNotification,successCallback, errorCallback, achievementid);
}

AchievementDAO.prototype.deleteAchievement = function(useNotification,successCallback, errorCallback, achievementid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/achievements/"+currentAppId+"/" + achievementid;
	ClientHelper.deleteData(endPointURL,useNotification,successCallback, errorCallback,achievementid);
}

/*var ActionModel = function(action_id,name,description,point_value){
	this.action_id = action_id;
	this.name = name;
	this.description = description;
	this.point_value = point_value;
}*/

var ActionDAO = function(){
}

ActionDAO.prototype.getActionsDataToTable = function(tableElement, objectFormatters, loadedHandler, callbackResponse){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/actions/"+currentAppId;
	ClientHelper.getDataToTable(endPointURL, tableElement, objectFormatters, loadedHandler, callbackResponse);
}

ActionDAO.prototype.createNewAction = function(content, useNotification,successCallback, errorCallback, actionid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/actions/"+currentAppId;
	ClientHelper.postWithForm(endPointURL, content, useNotification,successCallback, errorCallback, actionid);
}

ActionDAO.prototype.updateAction = function(content, useNotification,successCallback, errorCallback, actionid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/actions/"+currentAppId+"/"+actionid;
	ClientHelper.putWithForm(endPointURL,content, useNotification,successCallback, errorCallback, actionid);
}

ActionDAO.prototype.deleteAction = function(useNotification,successCallback, errorCallback, actionid){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/actions/"+currentAppId+"/" + actionid;
	ClientHelper.deleteData(endPointURL,useNotification,successCallback, errorCallback,actionid);
}


/*var LevelModel = function(level_num,name,point_value){
	this.level_num = level_num;
	this.name = name;
	this.point_value = point_value;
}
*/
var LevelDAO = function(){
}

LevelDAO.prototype.getLevelsDataToTable = function(tableElement, objectFormatters, loadedHandler, callbackResponse){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/levels/"+currentAppId;
	ClientHelper.getDataToTable(endPointURL, tableElement, objectFormatters, loadedHandler, callbackResponse);
}

LevelDAO.prototype.createNewLevel = function(content, useNotification,successCallback, errorCallback, levelnum){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/levels/"+currentAppId;
	ClientHelper.postWithForm(endPointURL, content, useNotification,successCallback, errorCallback, levelnum);
}

LevelDAO.prototype.updateLevel = function(content, useNotification,successCallback, errorCallback, levelnum){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/levels/"+currentAppId+"/"+levelnum;
	ClientHelper.putWithForm(endPointURL,content, useNotification,successCallback, errorCallback, levelnum);
}

LevelDAO.prototype.deleteLevel = function( useNotification,successCallback, errorCallback, levelnum){
	currentAppId = Cookies.get("appid");
	var endPointURL = "gamification/levels/"+currentAppId+"/" + levelnum;
	ClientHelper.deleteData(endPointURL,useNotification,successCallback, errorCallback,levelnum);
}

