//---------------------------------------------------------//


var questTabHandler = (function(){
	var questAccess = new QuestDAO();
	var questCollection = [];
	var modalInputQuestId = $("#modalquestdiv").find("#quest_id");
    var modalInputQuestName = $("#modalquestdiv").find("#quest_name");
    var modalInputQuestDescription = $("#modalquestdiv").find("#quest_desc");
    var modalInputQuestAchievementId = $("#modalquestdiv").find("#quest_achievement_id");
    var modalInputQuestStatus = $("#modalquestdiv").find("#quest_status_text");
    var modalCheckQuestPoint = $("#modalquestdiv").find("#quest_point_check");
    var modalInputQuestPoint = $("#modalquestdiv").find("#quest_point_value");
    var modalCheckQuestQuestCompleted = $("#modalquestdiv").find("#quest_quest_check");
    var modalInputQuestQuestCompleted = $("#modalquestdiv").find("#quest_id_completed");
    var modalTitle = $("#modalquestdiv").find(".modal-title");
    var modalQuestActionGroup = $("#modalquestdiv").find('#quest_action_list_group');


    function renderAppendActionListInModal(selectedaction,selectedtimes){
		// put in the list group
    	var htmlelement = "<li class=\"list-group-item\" id=\""+selectedaction+"\"><div class=\"input-group\"><a>"+selectedaction+" - <p> "+selectedtimes+" times</p></a>"+
						 "<span class=\"input-group-btn\"><button type=\"button\" id=\""+selectedaction+"\" class=\"close\" >&times;</button></span></div></li>";
		$('#quest_action_list_group').append(htmlelement);

		// Button delete listener
		$('#quest_action_list_group').find('button').on("click", function(e){
			var buttoniddeleted = $(this).attr("id");
			$('#quest_action_list_group').find('li#'+buttoniddeleted).remove();
		});
	}


	currentAppId = Cookies.get("appid");
	var endPointURL = epURL+"games/quests/"+currentAppId;

	$("table#list_quests").bootgrid("destroy");
	
	var questgrid = $("table#list_quests").bootgrid({
		ajax: true,
		columnSelection: false,
		ajaxSettings: {
	        method: "GET",
	        cache: false
	    },
	    searchSettings: {
	        delay: 100,
	        characters: 3
	    },
	    url: useAuthentication(endPointURL),
	    formatters: {
	    	"pointFlag":function(column, row)
	    	{
	    		if(row.pointFlag){
	    			return "yes";
	    		}
	    		else if(!row.pointFlag){
	    			return "no";
	    		}
	    		else{
	    			return "";
	    		}
	    		
	    	},
	    	"questFlag":function(column, row)
	    	{
	    		if(row.questFlag){
	    			return "yes";
	    		}
	    		else if(!row.questFlag){
	    			return "no";
	    		}
	    		else{
	    			return "";
	    		}
	    		
	    	},
	    	"actionIds":function(column, row)
	    	{
	    		var htmlelement = "<ul class='list-group'>";
	    		for (i = 0; i < row.actionIds.length; i++) { 
				     htmlelement += "<li class='list-group-item'><span class='badge'>"+row.actionIds[i].times+"</span>"+row.actionIds[i].actionId +"</li>"
				}
				htmlelement += "</ul>";
				// var popaction ="<a data-html=\"true\" data-toggle=\"popover\" data-trigger=\"focus\" data-content=\""+htmlelement+"\">Dismissible popover</a>";
	    		var poptitle = "<li class='list-group-item'>Action ID - times</li>";
				var popaction = "<a href=\"#\" data-placement=\"top\" data-html=\"true\" data-trigger=\"hover\" data-toggle=\"popover\" data-content=\""+htmlelement+" \" title=\""+poptitle+"\">Actions used</a>";

				return popaction;
	    	},
	        "commands": function(column, row)
	        {

	    			return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\"></span></button> " + 
	                "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-trash-o\"></span></button>";
	        	
	        }
	    },
	    responseHandler: function (response) { 

	    	
	    	questCollection = questAccess.storeQuestCollection(response.rows);
	    	console.log(questCollection);
	    	return response; 
	    }
	}).on("loaded.rs.jquery.bootgrid", function()
	{
		$("[data-toggle=popover]").popover();
	    questgrid.find(".command-edit").on("click", function(e)
	    {	
	    	var tdcollection = $(this).parent().parent().find("td");
	    	var questid_ = $(this).data("row-id");

	    	var index = 0;
	    	for(var i = 0; i < questCollection.length; i++){
	    		if(questCollection[i].quest_id == questid_)
	    		{
	    			index = i;
	    			break;
	    		}
	    		if(i == questCollection.length -1){
	    			throw "Quest ID not found in collection";
	    			return false;
	    		}
	    	}
	    	// Adapt Modal with update form
	    	console.log(questCollection[index]);
	    	var selectedQuestModel = questCollection[index];
			$("#modalquestsubmit").html('Update');
			$(modalTitle).html('Update an Level');
	    	$(modalInputQuestId).val(selectedQuestModel.quest_id);
			$(modalInputQuestId).prop('readonly', true);
		    $(modalInputQuestName).val(selectedQuestModel.name);
	    	$(modalInputQuestDescription).text(selectedQuestModel.description);
	    	$(modalInputQuestAchievementId).val(selectedQuestModel.achievement_id);
		    $(modalInputQuestStatus).val(QuestStatus.toString(selectedQuestModel.status));

		    $(modalCheckQuestPoint).prop('checked',selectedQuestModel.point_flag);
		    $(modalInputQuestPoint).val(selectedQuestModel.point_value);
		    $(modalCheckQuestQuestCompleted).prop('checked',selectedQuestModel.quest_flag);
		    $(modalInputQuestQuestCompleted).val(selectedQuestModel.quest_id_completed);
   			$(modalQuestActionGroup).empty();

   			// action list group
   			for(var i = 0; i < questCollection[index].action_ids.length; i++){
   				renderAppendActionListInModal(selectedQuestModel.action_ids[i].actionId,selectedQuestModel.action_ids[i].times);
   			}

		    $("#modalquestdiv").modal('toggle');
	    }).end().find(".command-delete").on("click", function(e)
	    {
	    	var questid = $(this).data("row-id");
	    	questAccess.deleteQuest(
	    		true,
	    		function(data,type){
					reloadActiveTab();
	    		}, 
	    		{},
	    		questid
	    	);
	    });
	});

	
	$("#addnewquest").on('click', function(event) {
		// count number of rows so the number can be incremented
	    // Adapt Modal with add form
	    $(modalQuestActionGroup).empty();
		$("#modalquestsubmit").html('Submit');
		$(modalInputQuestId).prop('readonly', false);
		$(modalInputQuestId).val('');
		$(modalInputQuestDescription).val('');
		$(modalTitle).html('Add a New Quest');
	    $(modalInputQuestName).val('');
	   	$(modalInputQuestDescription).text('');
	    $(modalInputQuestAchievementId).val('');
	    $(modalInputQuestAchievementId).prop('readonly',true);
	    $(modalInputQuestStatus).val('REVEALED');
	    $(modalCheckQuestPoint).prop('checked', false);
	   	$(modalInputQuestPoint).val('');
	    $(modalCheckQuestQuestCompleted).prop('checked', false);
	    $(modalInputQuestQuestCompleted).val('');
	    $("#modalquestdiv").modal('toggle');
		
	});

	// ----------------------------------------------------------------
	// Point in level ------------------------------------


	// Check boxes in modal
	// check box for point flag
	$('input[type="checkbox"]#quest_point_check').click(function(){
        if($(this).prop("checked") == true){
            $(modalInputQuestPoint).prop('disabled', false);
        }
        else if($(this).prop("checked") == false){
            $(modalInputQuestPoint).prop('disabled', true);
        }
    });

	// check box for quest flag
	$('input[type="checkbox"]#quest_quest_check').click(function(){
		// Disable input text
		// Hide panel completed
        if($(this).prop("checked") == false){
            $(modalInputQuestQuestCompleted).prop('disabled', true);
        	$('#panel_quest_completed').collapse("hide");
        }
        else if($(this).prop("checked") == true){
		// Enable input text
		// Show panel completed
            $(modalInputQuestQuestCompleted).prop('disabled', false);
            
        	$('#panel_quest_completed').collapse("show");
        }

        // Process the data, get data from quest table
        $('#quest_completed_list').empty();
        var questdata = $('table#list_quests tbody tr');
		
		var htmlelement="";
		for (i = 0; i < questdata.length; i++) {
			console.log($(questdata[i]).data("row-id"));
			var questidcompleted = $(questdata[i]).data("row-id");
			htmlelement += "<a href=\"#\" id=\""+questidcompleted+"\" class=\"list-group-item\">"+questidcompleted+"</a>"
		}
		$('#quest_completed_list').append(htmlelement);
		
		// Enable event listener
	 	$('#quest_completed_list').find('a').on('click', function (e) {

		  	var target = $(e.target).attr("id") // activated tab
		  	console.log(target);
		  	$(modalInputQuestQuestCompleted).val(target);
        	$('#panel_quest_completed').collapse("hide");
		});
    });

    // dropdown
    $('#quest_dropdown_status').find('a').on('click', function (e) {

	  	var target = $(e.target).attr("href") // activated tab
	  	console.log(target);
	  	switch(target) {
		    case "#revealed":
		    	$(modalInputQuestStatus).val('REVEALED');
		    break;
		    case "#hidden":
		    	$(modalInputQuestStatus).val('HIDDEN');
		    break;
		    case "#completed":
		    	$(modalInputQuestStatus).val('COMPLETED');
		    break;
		}
	});

	$("button#modalquestsubmit").on("click", function(e){
		//disable the default form submission
		e.preventDefault();

		// gather the data to be sent
		var questid = $(modalInputQuestId).val();
		var questname = $(modalInputQuestName).val();

		var questdescription = $(modalInputQuestDescription).val();
		
		var queststatus = $(modalInputQuestStatus).val();
		var questachievementid =  $(modalInputQuestAchievementId).val();
		if(questachievementid == ''){
			$.notify({
				// options
				message: "Achievement should be selected!"
			},{
				// settings
				type: 'danger'
			});
			return false;
		}
		var questpointflag = $(modalCheckQuestPoint).prop('checked');
		var questpointvalue = $(modalInputQuestPoint).val();
		if(questpointflag){
			if(questpointvalue == ''){
				$.notify({
				// options
					message: "Point value should be set!"
				},{
					// settings
					type: 'danger'
				});
				return false;
			}
		}
		var questquestflag = $(modalCheckQuestQuestCompleted).prop('checked');
		var questidcompleted = $(modalInputQuestQuestCompleted).val();
		if(questquestflag){
			if(questidcompleted == ''){
				$.notify({
				// options
					message: "Quest completed should be set!"
				},{
					// settings
					type: 'danger'
				});
				return false;
			}
		}

		var actionids = {
		    questactionids: []
		};

		var list = $(modalQuestActionGroup).find('li');
		if(list.length == 0){
			$.notify({
				// options
				message: "Actions should be selected!"
			},{
				// settings
				type: 'danger'
			});
			return false;
		}
		for(var i = 0;i < list.length; i++) {

		    
		    var action = $(list[i]).prop('id');
		    var times = $(list[i]).find('p').text();

		    actionids.questactionids.push({ 
		        action : action,
		        times  : parseInt(times)
		    });
		}

	   var content = {
	   	questid:questid,
	   	questname :questname,
	   	questdescription :questdescription,
	   	queststatus: queststatus,
	   	questpointflag :questpointflag,
	   	questpointvalue : parseInt(questpointvalue),
	   	questquestflag :questquestflag,
	   	questidcompleted :questidcompleted,
	   	questactionids: actionids.questactionids,
	   	questachievementid: questachievementid
	   }
	   
		var questid = $("#modalquestdiv").find("#quest_id").val();
			
		var method;
		var endPointPath;

		var submitButton = $("button#modalquestsubmit").html();


		if(submitButton=='Submit'){
			questAccess.createNewQuest(
				content, 
				true, 
				function(data,type){
					$("#modalquestdiv").modal('toggle');
					reloadActiveTab();
				},
				function(status,error){},
				questid
			);
		}
		else{
			questAccess.updateQuest(
				content, 
				true, 
				function(data,type){
					$("#modalquestdiv").modal('toggle');
					reloadActiveTab();
				},
				function(status,error){},
				questid
			);
		}
		return false;
	});

	$("#modalquestdiv").find("#select_achievement").on("click", function(e){
		$("table#list_achievements_a").bootgrid("destroy");

		var endPointURL = epURL+"games/achievements/"+currentAppId;
		var achievementgrid = $("table#list_achievements_a").bootgrid({
			ajax: true,
			ajaxSettings: {
		        method: "GET",
		        cache: false
		    },
		    searchSettings: {
		        delay: 100,
		        characters: 3
		    },
		    url: useAuthentication(endPointURL),
		    formatters: {
		    	"badgeId": function(column, row)
		    	{
		    		if(row.badgeId!=null){
		    			return "<p class=\"show-badge\" id=\""+ row.badgeId +"\" data-row-badgeid=\"" + row.badgeId + "\" >"+row.badgeId+"</p>";
					}
		    		else{
		    			return "";
		    		}
		    	},
		        "commands": function(column, row)
		        {
		            return "<button type=\"button\" class=\"btn btn-xs btn-default command-select\" data-row-id=\"" + row.id + "\">select</button> ";
		        }
		    },
		    responseHandler:function(response){
		    	return response;
		    }
		}).on("loaded.rs.jquery.bootgrid", function()
		{
			    // Show badge data as popover
		    achievementgrid.find(".show-badge").popover({
			        html : true, 
			        container: "body",
			        content: function() {
			          return $("#badge-popover-content").html();
			        },
			        title: function() {
			          return $("#badge-popover-title").html();
			        },
			        trigger: 'manual',
			        placement:'top'
			    });
		    
		    achievementgrid.find(".command-select").on("click", function(e)
		    {	
		    	$("#modalquestdiv").find("#panel_achievement").collapse('toggle')
		    	$("#modalquestdiv").find("#quest_achievement_id").val($(this).data("row-id"));		    	
		    });
		});
	});


	// Show existing quest
	$("#modalquestdiv").find("#select_action").on("click", function(e){
			// Retrieve badge data. The only difference is this is read only
		var endPointURL = epURL+"games/actions/"+currentAppId;
		$("#modalquestdiv").find("table#list_actions_a").bootgrid("destroy");
		var actiongrid = $("#modalquestdiv").find("table#list_actions_a").bootgrid({
			ajax: true,
			ajaxSettings: {
		        method: "GET",
		        cache: false
		    },
		    searchSettings: {
		        delay: 100,
		        characters: 3
		    },
		    url: useAuthentication(endPointURL),
		    formatters: {
		    	"times": function(column, row)
		    	{
		    		var htmlelement = "<input type=\"number\" class=\"form-control\" value=\"1\" id=\""+row.id+"\" >";
		    		return htmlelement;
		    	},
		    	"commands": function(column, row)
		        {
		        	// The only difference is this is read only
		            return "<button type=\"button\" class=\"btn btn-xs btn-default command-select\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\">select</span></button> ";
		        }
		    }
		}).on("loaded.rs.jquery.bootgrid", function()
		{
		    /* Executes after data is loaded and rendered */
		    actiongrid.find(".command-select").on("click", function(e)
		    {	
		    	// get selected data
		    	var selectedaction = $(this).data("row-id");
		    	var selectedtimes = $('#list_actions_a tbody tr input#'+selectedaction).val();

		    	// add new if the action is not exist yet
		    	if($('#quest_action_list_group').find('li#' + selectedaction).length == 0){
		    		renderAppendActionListInModal(selectedaction,selectedtimes);
		    		
				}
				else{
					$.notify({
						// options
						message: "Action is already selected!"
					},{
						// settings
						type: 'danger'
					});
				}
		    	
		    });
		});
	});



});