
var memberId;
var epURL = "http://localhost:8081/";
var client;


            console.log(memberId);
$(document).ready(function() {

    // Add drag and resize option to panel
    $("#toolbox-tools").draggable({
        handle: ".panel-heading",
        stop: function(evt, el) {
            // Save size and position in cookie
            /*
            $.cookie($(evt.target).attr("id"), JSON.stringify({
                "el": $(evt.target).attr("id"),
                "left": el.position.left,
                "top": el.position.top,
                "width": $(evt.target).width(),
                "height": $(evt.target).height()
            }));
            */
        }
    }).resizable({
        handles: "e, w, s, se",
        stop: function(evt, el) {
            // Save size and position in cookie
            
            $.cookie($(evt.target).attr("id"), JSON.stringify({
                "el": $(evt.target).attr("id"),
                "left": el.position.left,
                "top": el.position.top,
                "width": el.size.width,
                "height": el.size.height
            }));
            
        }
    });
	
    
    // Expand and collaps the toolbar
    $("#toggle-toolbox-tools").on("click", function() {
        var panel = $("#toolbox-tools");
    	
    	if ($(panel).data("org-height") == undefined) {
    		$(panel).data("org-height", $(panel).css("height"));
            $(panel).data("org-width", $(panel).css("width"));
    		$(panel).css("height","41px");
            $(panel).css("width","150px")
    	} else {
    		$(panel).css("height", $(panel).data("org-height"));
            $(panel).css("width", $(panel).data("org-width"));
    		$(panel).removeData("org-height");
            $(panel).removeData("org-width");
    	}
    	    	
    	$(this).toggleClass('fa-chevron-down').toggleClass('fa-chevron-right');
    });


    // Make toolbar groups sortable
/*    $( "#sortable" ).sortable({
		stop: function (event, ui) {
            var ids = [];
			$.each($(".draggable-group"), function(idx, grp) {
				ids.push($(grp).attr("id"));
			});
			
            // Save order of groups in cookie
			//$.cookie("group_order", ids.join());
		}
	});
	$( "#sortable" ).disableSelection();*/


  /*  // Make Tools panel group minimizable
	$.each($(".draggable-group"), function(idx, grp) {
		var tb = $(grp).find(".toggle-button-group");

		$(tb).on("click", function() {
			$(grp).toggleClass("minimized");
			$(this).toggleClass("fa-caret-down").toggleClass("fa-caret-up");

			// Save draggable groups to cookie (frue = Minimized, false = Not Minimized)
			var ids = [];
			$.each($(".draggable-group"), function(iidx, igrp) {
				var itb = $(igrp).find(".toggle-button-group");
				var min = $(igrp).hasClass("minimized");

				ids.push($(igrp).attr("id") + "=" + min);
			});

			$.cookie("group_order", ids.join());
		});
	});*/


    // Close thr panel
    $(".close-panel").on("click", function() {
		$(this).parent().parent().hide();
	});

    
    // Add Tooltips
    $('button').tooltip();
    $('.toggle-button-group').tooltip();


    var singInFunction = $('.oidc-signin').data('callback');
    console.log(singInFunction);


    
});


    function signinCallback(result) {
        if(result === "success"){
            
            memberId = oidc_userinfo.preferred_username;
            // after successful sign in, display a welcome string for the user
            $("#status").html("Hello, " + memberId + "!");
            client = new TemplateServiceClient(epURL);

            getLevel();
        } else {
            // if sign in was not successful, log the cause of the error on the console
             
            console.log(result);
        }
        if(result === "success"){
            $('#container-text').prop('hidden',true);
            $('#vis_container').prop('hidden',false)
        }else{
            $('#container-text').prop('hidden',false);
            $('#vis_container').prop('hidden',true);
        }

    }


var MemberDAO = function(){

}

window.localStorage['app_id'] = 'test';

currentAppId = window.localStorage["app_id"];

function getLevel(){
var endPointURL = "games/quests/"+currentAppId+"/"+memberId+"/status/REVEALED";
ClientHelper.getData(
    endPointURL,
    function(data,type){
        console.log(data);
    },
    function(status,error){
    }
    );
}

MemberDAO.prototype.getBadges = function(tableElement, objectFormatters, loadedHandler,callbackResponse){
    currentAppId = window.localStorage["app_id"];
    var endPointURL = "games/badges/"+currentAppId;
    ClientHelper.getDataToTable(endPointURL, tableElement, objectFormatters, loadedHandler, callbackResponse);
}

MemberDAO.prototype.getLevel = function(successCallback, errorCallback){
    currentAppId = window.localStorage["app_id"];

    var endPointURL = "games/levels/"+currentAppId+"/"+memberId;
    ClientHelper.getData(
        endPointURL,
        function(data,type){
            console.log(data);
            successCallback(data,type);
        },
        function(status,error){
            errorCallback(status,error);
        }
        );
}

var ClientHelper = {
     getData : function(endPointURL, successCallback, errorCallback){
        client.sendRequest(
            "GET",
            endPointURL,
            "",
            "application/json",
            {},
            function(data, type){
                
                successCallback(data,type);
                
                return false;
            },
            function(status,error) {
                errorCallback(status,error);
                return false;
            }
        );
    }

};

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

var BadgeModel = function(badge_id,name,description,image_path){
    this.badge_id = badge_id;
    this.name = name;
    this.description = description;
    this.image_path = image_path;
}


var AchievementModel = function(achievement_id,name,description,point_value,badge_id){
    this.achievement_id = achievement_id;
    this.name = name;
    this.description = description;
    this.point_value = point_value;
    this.badge_id = badge_id;
}


var ActionModel = function(action_id,name,description,point_value){
    this.action_id = action_id;
    this.name = name;
    this.description = description;
    this.point_value = point_value;
}



var LevelModel = function(level_num,name,point_value){
    this.level_num = level_num;
    this.name = name;
    this.point_value = point_value;
}
