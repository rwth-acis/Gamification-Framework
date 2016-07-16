//---------------------------------------------------------//

var questModule = (function() {
	
	var questAccess, achievementAccess, actionAccess;
	var questCollection;
	var modalInputQuestId;
    var modalInputQuestName;
    var modalInputQuestDescription;
    var modalInputQuestAchievementId;
    var modalInputQuestStatus;
    var modalCheckQuestPoint;
    var modalInputQuestPoint;
    var modalCheckQuestQuestCompleted;
    var modalInputQuestQuestCompleted;
    var modalTitle;
    var modalQuestActionGroup;
    var modalQuestSubmitButton;

	var modalNotifCheck;
	var modalNotifMessageInput;
	var initialize = function(){

		questAccess = new QuestDAO();
		achievementAccess = new AchievementDAO();
		actionAccess = new ActionDAO();
		questCollection = [];
		modalInputQuestId = $("#modalquestdiv").find("#quest_id");
	    modalInputQuestName = $("#modalquestdiv").find("#quest_name");
	    modalInputQuestDescription = $("#modalquestdiv").find("#quest_desc");
	    modalInputQuestAchievementId = $("#modalquestdiv").find("#quest_achievement_id");
	    modalInputQuestStatus = $("#modalquestdiv").find("#quest_status_text");
	    modalCheckQuestPoint = $("#modalquestdiv").find("#quest_point_check");
	    modalInputQuestPoint = $("#modalquestdiv").find("#quest_point_value");
	    modalCheckQuestQuestCompleted = $("#modalquestdiv").find("#quest_quest_check");
	    modalInputQuestQuestCompleted = $("#modalquestdiv").find("#quest_id_completed");
	    modalTitle = $("#modalquestdiv").find(".modal-title");
	    modalQuestSubmitButton = $("#modalquestdiv").find("#modalquestsubmit");
	    modalQuestActionGroup = $("#modalquestdiv").find('#quest_action_list_group');

		modalNotifCheck = $("#modalquestdiv").find("#quest_notification_check");
		modalNotifMessageInput = $("#modalquestdiv").find("#quest_notification_message");
	};

	var loadTable = function(){
		var tableElement = $("table#list_quests");
		questAccess.getQuestsDataToTable(
			tableElement,
			{
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
		    	// Pop over for action id
		    	"actionIds":function(column, row)
		    	{
		    		var htmlelement = "<ul class='list-group'>";
		    		for (i = 0; i < row.actionIds.length; i++) { 
					     htmlelement += "<li class='list-group-item'><span class='badge'>"+row.actionIds[i].times+"</span>"+row.actionIds[i].actionId +"</li>"
					}
					htmlelement += "</ul>";
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
			function(objectgrid){
				$("[data-toggle=popover]").popover();

				// Update button listener
			    objectgrid.find(".command-edit").on("click", function(e)
			    {	

			    	var questid_ = $(this).data("row-id");
			    	
			    	var index = 0;
			    	for(var i = 0; i < questCollection.length; i++){
			    		if(questCollection[i].id == questid_)
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
					$(modalQuestSubmitButton).html('Update');
					$(modalTitle).html('Update an Level');
			    	$(modalInputQuestId).val(selectedQuestModel.id);
					$(modalInputQuestId).prop('readonly', true);
				    $(modalInputQuestName).val(selectedQuestModel.name);
			    	$(modalInputQuestDescription).text(selectedQuestModel.description);
			    	$(modalInputQuestAchievementId).val(selectedQuestModel.achievementId);
				    $(modalInputQuestStatus).val(selectedQuestModel.status);
				    console.log(modalTitle);

				    $(modalCheckQuestPoint).prop('checked',selectedQuestModel.pointFlag);
				    $(modalInputQuestPoint).val(selectedQuestModel.pointValue);
				    $(modalCheckQuestQuestCompleted).prop('checked',selectedQuestModel.questFlag);
				    $(modalInputQuestQuestCompleted).val(selectedQuestModel.questIdCompleted);

				    $(modalNotifCheck).prop('checked',selectedQuestModel.useNotification);
					$(modalNotifMessageInput).val(selectedQuestModel.notificationMessage);
		   			$(modalQuestActionGroup).empty();

		   			// enable checked input if the flag is true
		   			$(modalInputQuestPoint).prop('readonly',true);
				    if(selectedQuestModel.pointFlag){
				    	$(modalInputQuestPoint).prop('readonly',false);
				    }

		   			$(modalNotifMessageInput).prop('readonly',true);
				    if(selectedQuestModel.useNotification){
				    	$(modalNotifMessageInput).prop('readonly',false);
				    }
		   			// action list group
		   			for(var i = 0; i < questCollection[index].actionIds.length; i++){
		   				renderAppendActionListInModal(selectedQuestModel.actionIds[i].actionId,selectedQuestModel.actionIds[i].times);
		   			}

				    $("#modalquestdiv").modal('toggle');
			    }).end().find(".command-delete").on("click", function(e)
			    {
			    	var questid = $(this).data("row-id");
			    	questAccess.deleteQuest(
			    		true,
						function(data,type){
							$("#modalbadgediv").modal('toggle');
							//reloadActiveTab();
						},
						function(status,error){},
			    		questid
			    	);
			    });
			},
			function (response) { 

		    	questCollection = response.rows;
		    	console.log(questCollection);
	    	}
		);
	};

	var addNewButtonListener = function(){
		$("#addnewquest").on('click', function(event) {
		    // Adapt Modal with add form
		    $(modalQuestActionGroup).empty();
			$(modalQuestSubmitButton).html('Submit');
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
		    $(modalInputQuestQuestCompleted).val(null);
			$(modalNotifCheck).prop('checked',false);
			$(modalNotifMessageInput).val('');
		    $("#modalquestdiv").modal('toggle');
			
		});
	};

	var renderAppendActionListInModal = function(selectedaction,selectedtimes){
		// put in the list group
    	var htmlelement = "<li class=\"list-group-item\" id=\""+selectedaction+"\"><div class=\"input-group\">"+selectedaction+"  -  <span id=\"times\"> "+selectedtimes+" times</span>"+
						 "<span class=\"input-group-btn\"><button type=\"button\" id=\""+selectedaction+"\" class=\"close\" >&times;</button></span></div></li>";
		$('#quest_action_list_group').append(htmlelement);

		// Button delete listener
		$('#quest_action_list_group').find('button').on("click", function(e){
			var buttoniddeleted = $(this).attr("id");
			$('#quest_action_list_group').find('li#'+buttoniddeleted).remove();
		});
	}

	var dropDownListener = function(){
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
	}

	var checkBoxListener = function(){

			// Check boxes in modal
		// check box for point flag
		$('input[type="checkbox"]#quest_notification_check').click(function(){
	        if($(this).prop("checked") == true){
	            $(modalNotifMessageInput).prop('readonly', false);
	        }
	        else if($(this).prop("checked") == false){
	            $(modalNotifMessageInput).prop('readonly', true);
	        }
	    });
	
			// Check boxes in modal
		// check box for point flag
		$('input[type="checkbox"]#quest_point_check').click(function(){
	        if($(this).prop("checked") == true){
	            $(modalInputQuestPoint).prop('readonly', false);
	        }
	        else if($(this).prop("checked") == false){
	            $(modalInputQuestPoint).prop('readonly', true);
	        }
	    });
		// check box for quest flag
		$('input[type="checkbox"]#quest_quest_check').click(function(){
			// Disable input text
			// Hide panel completed
	        if($(this).prop("checked") == false){
	            $(modalInputQuestQuestCompleted).prop('readonly', true);
	        	$('#panel_quest_completed').collapse("hide");
	        }
	        else if($(this).prop("checked") == true){
			// Enable input text
			// Show panel completed
	            $(modalInputQuestQuestCompleted).prop('readonly', false);
	            
	        	$('#panel_quest_completed').collapse("show");
	        }

	        // Process the data, get data from quest table
	        $('#quest_completed_list').empty();
	        var questdata = $('table#list_quests tbody tr');
	        console.log(questdata);
			var htmlelement="";

			
			for (i = 0; i < questdata.length; i++) {
				console.log($(questdata[i]).data("row-id"));
				var questidcompleted = $(questdata[i]).data("row-id");
				if(questidcompleted != undefined){
					htmlelement += "<a href=\"#\" id=\""+questidcompleted+"\" class=\"list-group-item\">"+questidcompleted+"</a>";
				}
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
	};

	var submitFormListener = function(){
		$("button#modalquestsubmit").on("click", function(e){
			//disable the default form submission
			e.preventDefault();

			// gather the data to be sent
			var questid = $(modalInputQuestId).val();
			var questname = $(modalInputQuestName).val();
			var questdescription = $(modalInputQuestDescription).val();
			var queststatus = $(modalInputQuestStatus).val();

			// Validate mandatory achievement element
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

			// Point value is needed if point flag is checked
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
			}else{
				questpointvalue = 0;
			}

			// Quest id completed is needed if quest flag is checked
			var questquestflag = $(modalCheckQuestQuestCompleted).prop('checked');
			var questidcompleted = $(modalInputQuestQuestCompleted).val();
			if(questquestflag){
				if(questidcompleted == '' || questidcompleted == null ||questidcompleted==undefined){
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
			if(questidcompleted == '' || questidcompleted == null ||questidcompleted==undefined){
				questidcompleted = null;
			}


			// list of action id of the quest
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
			    var times = $(list[i]).find('span#times').text();

			    actionids.questactionids.push({ 
			        action : action,
			        times  : parseInt(times)
			    });
			}

			// Notification
			var questnotifflag = $(modalNotifCheck).prop('checked');
			var questnotifmessage = $(modalNotifMessageInput).val();
			if(questnotifflag){
				if(questnotifmessage == undefined){
					$.notify({
					// options
						message: "Message notification should be set!"
					},{
						// settings
						type: 'danger'
					});
					return false;
				}
			}

			// Populate data into an object ready to be sent
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
		   	questachievementid: questachievementid,
		   	questnotificationcheck:questnotifflag,
		   	questnotificationmessage:questnotifmessage
		   }
		   

			var questid = $("#modalquestdiv").find("#quest_id").val();
				
			var submitButtonText = $("button#modalquestsubmit").html();


			if(submitButtonText=='Submit'){
				questAccess.createNewQuest(
					content, 
					true, 
					function(data,type){
						$("#modalquestdiv").modal('toggle');
						//reloadActiveTab();
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
						//reloadActiveTab();
					},
					function(status,error){},
					questid
				);
			}
			return false;
		});

	};

	// Event listener for other elements rather than quest itself
	var elementDependenciesListener = function(){
		$("#modalquestdiv").find("#select_achievement").on("click", function(e){

			// var endPointURL = epURL+"games/achievements/"+currentAppId;
			var tableElement = $("table#list_achievements_a");
			achievementAccess.getAchievementsDataToTable(
				tableElement,
			{
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
			function(objectgrid){
				objectgrid.find(".show-badge").popover({
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
			    
			    objectgrid.find(".command-select").on("click", function(e)
			    {	
			    	$("#modalquestdiv").find("#panel_achievement").collapse('toggle')
			    	$("#modalquestdiv").find("#quest_achievement_id").val($(this).data("row-id"));		    	
			    });
			},
			function (response) {}
			);
			
		});


		$("#modalquestdiv").find("#select_action").on("click", function(e){
			
			var tableElement = $("table#list_actions_a");
			actionAccess.getActionsDataToTable(
				tableElement,
				{
					"times": function(column, row)
			    	{
			    		var htmlelement = "<input type=\"number\" class=\"form-control\" value=\"1\" id=\""+row.id+"\" >";
			    		return htmlelement;
			    	},
			    	"commands": function(column, row)
			        {
			        	// The only difference is this is read only
			            return "<button type=\"button\" class=\"btn btn-xs btn-default command-select\" data-row-id=\"" + row.id + "\">select</button> ";
			        }
		    	},
				function(objectgrid){
					/* Executes after data is loaded and rendered */
				    objectgrid.find(".command-select").on("click", function(e)
				    {	
				    	// get selected data
				    	var selectedaction = $(this).data("row-id");
				    	var selectedtimes = $('#list_actions_a tbody tr input#'+selectedaction).val();

				    	// add new if the action is not exist yet
				    	if($('#quest_action_list_group').find('li#' + selectedaction).length == 0){
				    		renderAppendActionListInModal(selectedaction,selectedtimes);
				    		$('#panel_action').collapse('hide');
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
				},
				function (response) {}
			);
		});
	};

	return {
		init : function(){
			initialize();
			loadTable();
			addNewButtonListener();
			dropDownListener();
			checkBoxListener();
			elementDependenciesListener();
			submitFormListener();
		}
	};


})();