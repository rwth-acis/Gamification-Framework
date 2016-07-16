//-------------------------------------------------------------//
// --------------------- BADGES PART ------------------------- //

var levelModule = (function() {
	
	var levelAccess;
	var modalInputId;
	var modalInputName;
	var modalInputPointValue;
	var modalSubmitButton;
	var modalTitle;
	var maxlevel;

	var modalNotifCheck;
	var modalNotifMessageInput;
	var levelCollection;

	var initialize = function(){

		levelAccess = new LevelDAO();
		modalSubmitButton = $("#modallevelsubmit");
		modalInputId = $("#modalleveldiv").find("#level_num");
		modalTitle = $("#modalleveldiv").find(".modal-title");
	 	modalInputName = $("#modalleveldiv").find("#level_name");
	    modalInputPointValue = $("#modalleveldiv").find("#level_point_value");
		modalNotifCheck = $("#modalleveldiv").find("#level_notification_check");
		modalNotifMessageInput = $("#modalleveldiv").find("#level_notification_message");
	};

	var loadTable = function(){
		var tableElement = $("table#list_levels");
		levelAccess.getLevelsDataToTable(
			tableElement,
			{
				"commands": function(column, row)
		        {
		        	console.log(maxlevel);
		        	console.log(row.number);
		        	if(maxlevel-1 <= row.number){
		            	return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-pencil\"></span></button> " + 
		                "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-trash-o\"></span></button>";
		        	}
		        	else{
		        		return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-pencil\"></span></button> ";
		        	}
		        }
	    	},
			function(objectgrid){
				objectgrid.find(".command-edit").on("click", function(e)
			    {	
			    	// assign data from levelCollection
					var levelnum_ = $(this).data("row-id");

			    	var index = 0;
			    	for(var i = 0; i < levelCollection.length; i++){
			    		if(levelCollection[i].number == levelnum_)
			    		{
			    			index = i;
			    			break;
			    		}
			    		if(i == levelCollection.length -1){
			    			throw "Level number not found in collection";
			    			return false;
			    		}
			    	}
			    	// Adapt Modal with update form
			    	var selectedLevelModel = levelCollection[index];
					$(modalSubmitButton).html('Update');
					$(modalTitle).html('Update an Level');
			    	$(modalInputId).val($(this).data("row-id"));
				    $(modalInputId).prop('readonly', true);
				    $(modalInputName).val(selectedLevelModel.name);
				    $(modalInputPointValue).val(selectedLevelModel.pointValue);
				    $(modalNotifCheck).prop('checked',selectedLevelModel.useNotification);
				    $(modalNotifMessageInput).val(selectedLevelModel.notificationMessage);
				    $(modalNotifMessageInput).prop('readonly',true);
				    if(selectedLevelModel.useNotification){
				    	$(modalNotifMessageInput).prop('readonly',false);
				    }
					
				    $("#modalleveldiv").modal('toggle');
			    }).end().find(".command-delete").on("click", function(e)
			    {
			    	var levelnum = $(this).data("row-id");
			    	levelAccess.deleteLevel(
			    		true,
						function(data,type){
							$("#modalbadgediv").modal('toggle');
							reloadActiveTab();
						},
						function(status,error){},
						levelnum
			    	);
			    	console.log($(this).data("row-id"));
			    });
			},
			function(response){
				maxlevel = response.total;
				levelCollection = response.rows;
				console.log(response.rows);
			}
		);
	};

	var addNewButtonListener = function(){
		$("#addnewlevel").on('click', function(event) {
			// count number of rows so the number can be incremented
			var levelindex = $("table#list_levels").find("tr").length - 1;
			console.log($("table#list_levels").find("tr"));
		    // Adapt Modal with add form
			$(modalSubmitButton).html('Submit');
			$(modalInputId).prop('readonly', true);
			$(modalInputId).val(levelindex);
			$(modalTitle).html('Add a New Level');
		    $(modalInputName).val('');
		    $(modalInputPointValue).val('');
			$(modalNotifCheck).prop('checked',false);
			$(modalNotifMessageInput).val('');
		    $("#modalleveldiv").modal('toggle');
			
		});
	};

	var checkBoxListener = function(){
			// Check boxes in modal
	// check box for point flag
		$('input[type="checkbox"]#level_notification_check').click(function(){
	        if($(this).prop("checked") == true){
	            $(modalNotifMessageInput).prop('disabled', false);
	        }
	        else if($(this).prop("checked") == false){
	            $(modalNotifMessageInput).prop('disabled', true);
	        }
	    });
	}

	var submitFormListener = function(){
		$("form#modallevelform").submit(function(e){
			//disable the default form submission
			e.preventDefault();
			var formData = new FormData($(this)[0]);

			var submitButtonText = $(modalSubmitButton).html();

			var levelnum = $(modalInputId).val();

			if(submitButtonText=='Submit'){
				levelAccess.createNewLevel(
					formData, 
					true, 
					function(data,type){
						$("#modalbadgediv").modal('toggle');
						reloadActiveTab();
					},
					function(status,error){},
					levelnum
				);
			}
			else{
				levelAccess.updateLevel(
					formData, 
					true, 
					function(data,type){
						$("#modalbadgediv").modal('toggle');
						reloadActiveTab();
					},
					function(status,error){},
					levelnum
				);
			}
			
			return false;
		});
	};

	return {
		init : function(){
			initialize();
			loadTable();
			addNewButtonListener();
			checkBoxListener();
			submitFormListener();
		}
	};


})();

// var levelTabHandler = (function(){


// 	// Achievement part ---------------------------
// 	currentAppId = Cookies.get("appid");

// 	// var deleteLevelHandler = function(levelnum){				

// 	// 	currentAppId = Cookies.get("appid");
// 	// 	var path_URI = "games/levels/"+currentAppId+"/" + levelnum;
// 	// 	client.sendRequest("DELETE",
// 	// 		path_URI,
// 	// 		"",
// 	// 		false,
// 	// 		{},
// 	// 		function(data, type){
// 	// 			console.log(data);
// 	// 			reloadActiveTab();
// 	// 		    $.notify({
// 	// 				// options
// 	// 				message: "<b>" + levelnum + "</b> is deleted !"
// 	// 			},{
// 	// 				// settings
// 	// 				type: 'success'
// 	// 			});
// 	// 			return false;
// 	// 		},
// 	// 		function(error) {
// 	// 	         $.notify({
// 	// 				// options
// 	// 				message: "Failed to delete <b>" + levelnum + "</b> !"
// 	// 			},{
// 	// 				// settings
// 	// 				type: 'danger'
// 	// 			});
// 	// 	          console.log(error);
// 	// 	    }
// 	// 	);
// 	// }

// 	$("table#list_levels").bootgrid("destroy");
// 	var maxlevel; //to store total data from response
// 	var endPointURL = epURL+"games/levels/"+currentAppId;
// 	var achievementgrid = $("table#list_levels").bootgrid({
// 		ajax: true,
// 		columnSelection: false,
// 		ajaxSettings: {
// 	        method: "GET",
// 	        cache: false
// 	    },
// 	    searchSettings: {
// 	        delay: 100,
// 	        characters: 3
// 	    },
// 	    url: useAuthentication(endPointURL),
// 	    formatters: {
// 	        "commands": function(column, row)
// 	        {
// 	        	if(maxlevel == row.number){
// 	            	return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-pencil\"></span></button> " + 
// 	                "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-trash-o\"></span></button>";
// 	        	}
// 	        	else{
// 	        		return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-pencil\"></span></button> ";
// 	        	}
// 	        }
// 	    },
// 	    responseHandler: function (response) { 
// 	    	maxlevel = response.total;

// 	    	console.log($(this).find("button"));
// 	    	return response; 
// 	    }
// 	}).on("loaded.rs.jquery.bootgrid", function()
// 	{
		
// 	    achievementgrid.find(".command-edit").on("click", function(e)
// 	    {	
// 	    	var tdcollection = $(this).parent().parent().find("td");

// 	    	// Adapt Modal with update form
// 			$("#modallevelsubmit").html('Update');
// 			$("#modalleveldiv").find(".modal-title").html('Update an Level');
// 	    	$("#modalleveldiv").find("#level_num").val($(this).data("row-id"));
// 		    $("#modalleveldiv").find("#level_num").prop('readonly', true);
// 		    $("#modalleveldiv").find("#level_name").val(tdcollection[1].textContent);
// 		    $("#modalleveldiv").find("#level_point_value").val(tdcollection[2].textContent);
// 		    $("#modalleveldiv").modal('toggle');
// 	    }).end().find(".command-delete").on("click", function(e)
// 	    {
// 	    	deleteLevelHandler($(this).data("row-id"));
// 	    	console.log($(this).data("row-id"));
// 	    });
// 	});

	
// 	$("#addnewlevel").on('click', function(event) {
// 		// count number of rows so the number can be incremented
// 		var levelindex = $("table#list_levels").find("tr").length - 1;
// 		console.log($("table#list_levels").find("tr"));
// 	    // Adapt Modal with add form
// 		$("#modallevelsubmit").html('Submit');
// 		$("#modalleveldiv").find("#level_num").prop('readonly', true);
// 		$("#modalleveldiv").find("#level_num").val(levelindex);
// 		$("#modalleveldiv").find(".modal-title").html('Add a New Level');
// 	    $("#modalleveldiv").find("#level_name").val('');
// 	    $("#modalleveldiv").find("#level_point_value").val('');
// 	    $("#modalleveldiv").modal('toggle');
		
// 	});

// 	// ----------------------------------------------------------------
// 	// Point in level ------------------------------------


// 	// Retrieve point data. The only difference is this is read only


// 	$("form#modallevelform").submit(function(e){
// 		//disable the default form submission
// 		e.preventDefault();

// 		var formData = new FormData($(this)[0]);
// 		console.log(formData);
// 		currentAppId = Cookies.get("appid");
// 		var levelnum = $("#modalleveldiv").find("#level_num").val();
			
// 		var method;
// 		var endPointPath;
// 		if($(this).find("button#modallevelsubmit").html()=='Submit'){
// 			method = "POST";
// 			endPointPath = "games/levels/"+currentAppId;
// 		}
// 		else{
// 			method = "PUT";
// 			endPointPath = "games/levels/"+currentAppId+"/"+levelnum;
// 		}
// 		client.sendRequest(
// 			method,
// 			endPointPath,
// 			formData,
// 			false,
// 			{},
// 			function(data, type){
// 				console.log(data);
// 				$("#modalleveldiv").modal('toggle');

// 				reloadActiveTab();
// 				$.notify({
// 					// options
// 					message: "<b>" + levelnum + "</b> is added !"
// 				},{
// 					// settings
// 					type: 'success'
// 				});
// 				return false;
// 			},
// 			function(error) {
// 		         $.notify({
// 					// options
// 					message: "Failed to add <b>" + levelnum + "</b> !"
// 				},{
// 					// settings
// 					type: 'danger'
// 				});
// 		         return false;
// 			}
// 		);
// 		return false;
// 	});


// });