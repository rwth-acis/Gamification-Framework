
var actionModule = (function() {
	
	var actionAccess;
	var modalSubmitButton;
	var modalInputId;
	var modalInputName;
	var modalInputDescription;
	var modalInputPointValue;
	var modalInputBadgeId;
	var modalTitle;

	var modalNotifCheck;
	var modalNotifMessageInput;

	var actionCollection;

	var initialize = function(){

		actionAccess = new ActionDAO();
		modalSubmitButton = $("#modalactionsubmit");
		modalTitle = $("#modalactiondiv").find(".modal-title");
    	modalInputId = $("#modalactiondiv").find("#action_id_name");
	    modalInputName = $("#modalactiondiv").find("#action_name");
	    modalInputDescription = $("#modalactiondiv").find("#action_desc");
	    modalInputPointValue = $("#modalactiondiv").find("#action_point_value");
		modalNotifCheck = $("#modalactiondiv").find("#action_notification_check");
		modalNotifMessageInput = $("#modalactiondiv").find("#action_notification_message");
	};

	var loadTable = function(){
		var tableElement = $("table#list_actions");
		actionAccess.getActionsDataToTable(
			tableElement,
			{
				"commands": function(column, row)
		        {
		            	return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\"></span></button> " + 
		                "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-trash-o\"></span></button>";
		        }
	    	},
			function(objectgrid){
				objectgrid.find(".command-edit").on("click", function(e)
			    {	
			    	// assign data from actionCollection
					var actionid_ = $(this).data("row-id");

			    	var index = 0;
			    	for(var i = 0; i < actionCollection.length; i++){
			    		if(actionCollection[i].id == actionid_)
			    		{
			    			index = i;
			    			break;
			    		}
			    		if(i == actionCollection.length -1){
			    			throw "Action ID not found in collection";
			    			return false;
			    		}
			    	}
			    	// Adapt Modal with update form
			    	var selectedAction = actionCollection[index];
					$(modalSubmitButton).html('Update');
					$(modalTitle).html('Update an Action');
			    	$(modalInputId).val($(this).data("row-id"));
				    $(modalInputId).prop('readonly', true);
				    $(modalInputName).val(selectedAction.name);
				    $(modalInputDescription).val(selectedAction.description);
				    $(modalInputPointValue).val(selectedAction.pointValue);
				    $(modalNotifCheck).prop('checked',selectedAction.useNotification);
					$(modalNotifMessageInput).val(selectedAction.notificationMessage);
					$(modalNotifMessageInput).prop('readonly',true);
				    if(selectedAction.useNotification){
				    	$(modalNotifMessageInput).prop('readonly',false);
				    }
				    $("#modalactiondiv").modal('toggle');
			    }).end().find(".command-delete").on("click", function(e)
			    {
			    	var actionid = $(this).data("row-id");
			    	actionAccess.deleteAction(
			    		true,
						function(data,type){
							$("#modalbadgediv").modal('toggle');
							//reloadActiveTab();
						},
						function(status,error){},
			    		actionid
			    	);
			    });
			},
			function(response){
				actionCollection = response.rows;
				console.log(response.rows);
			}
		);
	};

	var addNewButtonListener = function(){
		$("#addnewaction").on('click', function(event) {
			console.log("THIS IS ADD ACTION");
			$(modalSubmitButton).html('Submit');
			$(modalInputId).prop('readonly', false);
			$(modalInputId).val('');
			$(modalTitle).html('Add a New Action');
		    $(modalInputName).val('');
		    $(modalInputPointValue).val('');
			$(modalNotifCheck).prop('checked',false);
			$(modalNotifMessageInput).val('');
		    $("#modalactiondiv").modal('toggle');
			
		});
	};

	var checkBoxListener = function(){
			// Check boxes in modal
	// check box for point flag
		$('input[type="checkbox"]#action_notification_check').click(function(){
	        if($(this).prop("checked") == true){
	            $(modalNotifMessageInput).prop('readonly', false);
	        }
	        else if($(this).prop("checked") == false){
	            $(modalNotifMessageInput).prop('readonly', true);
	        }
	    });
	}

	var submitFormListener = function(){
		$("form#modalactionform").submit(function(e){
			//disable the default form submission
			e.preventDefault();
			var formData = new FormData($(this)[0]);

			var submitButtonText = $(modalSubmitButton).html();

			var actionid = $(modalInputId).val();

			if(submitButtonText=='Submit'){
				actionAccess.createNewAction(
					formData, 
					true, 
					function(data,type){
						$("#modalactiondiv").modal('toggle');
						//reloadActiveTab();
					},
					function(status,error){},
					actionid
				);
			}
			else{
				actionAccess.updateAction(
					formData, 
					true, 
					function(data,type){
						$("#modalactiondiv").modal('toggle');
						//reloadActiveTab();
					},
					function(status,error){},
					actionid
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
