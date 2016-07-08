
var QuestStatus = {
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
}


// QuestModel.prototype = {
// 	getId: function(){
// 		return this.quest_id;
// 	},
// 	setId: function(quest_id){
// 		this.quest_id = quest_id;
// 	},
// 	getName: function(){
// 		return this.name;
// 	},
// 	setName: function(name){
// 		this.name = name;
// 	},
// 	getDescription: function(){
// 		return this.description;
// 	},
// 	setDescription: function(description){
// 		this.description = description;
// 	},
// 	getStatus: function(){
// 		return this.status;
// 	},
// 	setStatus: function(status){
// 		this.status = status;
// 	},
// 	getAchievementId: function(){
// 		return achievement_id;
// 	},
// 	setAchievementId: function(achievement_id){
// 		this.achievement_id = achievement_id;
// 	},
// 	getQuestFlag: function(){
// 		return this.quest_flag;
// 	},
// 	setQuestFlag: function(quest_flag){
// 		this.quest_flag = quest_flag;
// 	},
// 	getQuestIdCompleted: function(){
// 		return this.quest_id_completed;
// 	},
// 	setQuestIdCompleted: function(quest_id_completed){
// 		this.quest_id_completed = quest_id_completed;
// 	},
// 	getPointFlag: function(){
// 		return this.point_flag;
// 	},
// 	setPointFlag: function(point_flag){
// 		this.point_flag = point_flag;
// 	},
// 	getPointValue: function(){
// 		return this.point_value;
// 	},
// 	setPointValue: function(point_value){
// 		this.point_value = point_value;
// 	},
// 	getActionIds: function(){
// 		return this.action_ids;
// 	},
// 	setActionIds: function(action_ids){
// 		this.action_ids = this.action_ids;
// 	}
// }

var QuestDAO = function(){

}

QuestDAO.prototype.createNewQuest = function(content, useNotification,successCallback, errorCallback, questid){

	this.currentAppId = Cookies.get("appid");
	var objsent = JSON.stringify(content);
	client.sendRequest(
			"POST",
			"games/quests/"+this.currentAppId,
			objsent,
			"application/json",
			{},
			function(data, type){
				

				if(useNotification){
					$.notify({
						// options
						message: "<b>" + questid + "</b> is added !"
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
						message: "Failed to add <b>" + questid + "</b> !. " + error
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

QuestDAO.prototype.updateQuest = function(content, useNotification,successCallback, errorCallback, questid){

	this.currentAppId = Cookies.get("appid");
	var objsent = JSON.stringify(content);
	client.sendRequest(
			"PUT",
			"games/quests/"+this.currentAppId+"/"+questid,
			objsent,
			"application/json",
			{},
			function(data, type){
				if(useNotification){
					$.notify({
					// options
					message: "<b>" + questid + "</b> is updated !"
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
						message: "Failed to update <b>" + questid + "</b> !" + error
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

QuestDAO.prototype.deleteQuest = function(useNotification,successCallback, errorCallback,questid){
	this.currentAppId = Cookies.get("appid");
	client.sendRequest("DELETE",
		"games/quests/"+currentAppId+"/" + questid,
		"",
		false,
		{},
		function(data, type){
		    $.notify({
				// options
				message: "<b>" + questid + "</b> is deleted !"
			},{
				// settings
				type: 'success'
			});
			successCallback(data,type);
			return false;
		},
		function(status,error) {
	         $.notify({
				// options
				message: "Failed to delete <b>" + questid + "</b> !"
			},{
				// settings
				type: 'danger'
			});
	         errorCallback(status,error);
	         return false;
	    }
	);
}

QuestDAO.prototype.storeQuestCollection = function(data){
	var questCollection = [];
	for(var i = 0; i < data.length; i++){
		var quest = new QuestModel(
			data[i].id,
			data[i].name,
			data[i].description,
			QuestStatus.valueOf(data[i].status),
			data[i].achievementId,
			data[i].questFlag,
			data[i].questIdCompleted,
			data[i].pointFlag,
			data[i].pointValue,
			data[i].actionIds
		);
		questCollection.push(quest);
	}
	return questCollection;
}
