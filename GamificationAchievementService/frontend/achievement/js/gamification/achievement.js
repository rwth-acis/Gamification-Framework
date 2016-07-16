//---------------------------------------------------------//
//--------------------ACHIEVEMENT PART--------------------//
var achievementModule = (function() {
	
	var achievementAccess,badgeAccess;
	var modalSubmitButton;
	var modalInputId;
	var modalInputName;
	var modalInputDescription;
	var modalInputPointValue;
	var modalInputBadgeId;
	var modalTitle;
	var modalInputBadgeId;
	var modalTitle;

	var modalNotifCheck;
	var modalNotifMessageInput;

	var achievementCollection;

	var initialize = function(){
		achievementAccess = new AchievementDAO();
		badgeAccess = new BadgeDAO();
		modalSubmitButton = $("#modalachievementsubmit").html('Update');
		modalTitle = $("#modalachievementdiv").find(".modal-title");
		modalInputId = $("#modalachievementdiv").find("#achievement_id_name");
	    modalInputName = $("#modalachievementdiv").find("#achievement_name");
	    modalInputDescription = $("#modalachievementdiv").find("#achievement_desc");
	    modalInputPointValue = $("#modalachievementdiv").find("#achievement_point_value");
	    modalInputBadgeId = $("#modalachievementdiv").find("#achievement_badge_id");
		modalNotifCheck = $("#modalachievementdiv").find("#achievement_notification_check");
		modalNotifMessageInput = $("#modalachievementdiv").find("#achievement_notification_message");
	};

	var loadTable = function(){
		var tableElement = $("table#list_achievements");
		achievementAccess.getAchievementsDataToTable(
			tableElement,
			{
				"badgeId": function(column, row)
		    	{
		    		if(row.badgeId!=null){
		    			return "<a href=\"#\" class=\"show-badge\" id=\""+ row.badgeId +"\" data-row-badgeid=\"" + row.badgeId + "\" >"+row.badgeId+"</a>";
					}
		    		else{
		    			return "";
		    		}
		    	},
		        "commands": function(column, row)
		        {
		            return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\"></span></button> " + 
		                "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-trash-o\"></span></button>";
		        }
	    	},
			function(objectgrid){
				objectgrid.find(".show-badge").popover({
			        html : true, 
			        content: function() {
			          return $("#badge-popover-content").html();
			        },
			        title: function() {
			          return $("#badge-popover-title").html();
			        },
			        trigger: 'manual',
			        placement:'right'
			    });
			    objectgrid.find(".command-edit").on("click", function(e)
			    {	
			    	// assign data from achievementCollection
					var achievementid_ = $(this).data("row-id");

			    	var index = 0;
			    	for(var i = 0; i < achievementCollection.length; i++){
			    		if(achievementCollection[i].id == achievementid_)
			    		{
			    			index = i;
			    			break;
			    		}
			    		if(i == achievementCollection.length -1){
			    			throw "Achievement ID not found in collection";
			    			return false;
			    		}
			    	}
			    	var selectedAchievement = achievementCollection[index];
			    	// Adapt Modal with update form
					$(modalSubmitButton).html('Update');
					$(modalTitle).html('Update an Achievement');
			    	$(modalInputId).val($(this).data("row-id"));
			    	$(modalInputId).prop('disabled', true);
				   	$(modalInputName).val(selectedAchievement.name);
				    $(modalInputDescription).val(selectedAchievement.description);
				    $(modalInputPointValue).val(selectedAchievement.pointValue);
				    $(modalInputBadgeId).val(selectedAchievement.badgeId);
				    $(modalNotifCheck).prop('checked',selectedAchievement.useNotification);
					$(modalNotifMessageInput).val(selectedAchievement.notificationMessage);
					$(modalNotifMessageInput).prop('readonly',true);
				    if(selectedAchievement.useNotification){
				    	$(modalNotifMessageInput).prop('readonly',false);
				    }
					
				    $("#modalachievementdiv").modal('toggle');
			    }).end().find(".command-delete").on("click", function(e)
			    {
			    	var achievementid = $(this).data("row-id");
			    	achievementAccess.deleteAchievement(
			    		true,
						function(data,type){
							$("#modalbadgediv").modal('toggle');
							//reloadActiveTab();
						},
						function(status,error){},
			    		achievementid
			    	);
			    }).end().find(".show-badge").on("click", function(e)
			    {
			    	// Get badge data with id
			    	e.preventDefault();
			    	// Get id of the selected element to be attached with popover 
			    	var idelement = "#" + $(this).data("row-badgeid");
			    	badgeAccess.getBadgeDataWithId(
			    		$(this).data("row-badgeid"),
			    		function(data,type){
			    			// Render in popover
			    			console.log(data);
			    			$("#badge-popover-content").find("#badgeidpopover").html(data.id);
			    			$("#badge-popover-content").find("#badgenamepopover").html(data.name);
			    			$("#badge-popover-content").find("#badgedescpopover").html(data.description);
			    			$("#badge-popover-content").find("#badgeimagepopover").attr("src",useAuthentication(data.imagePath))
			    			objectgrid.find(idelement).popover('show');

			    			// Dismiss popover when click anywhere
			    			$(document).click(function() {
							    objectgrid.find(idelement).popover('hide');
							});
			    			$(".popover").on("click", function(e)
						    {
						    	objectgrid.find(idelement).popover('hide');
						    });
			    		},
			    		function(error){

			    		});
			    });
			},
			function(response){
				achievementCollection = response.rows;
				console.log(response.rows);
			}
		);
	};

	var addNewButtonListener = function(){
		$("#addnewachievement").on('click', function(event) {
			// count number of rows so the number can be incremented
		    // Adapt Modal with add form
		modalSubmitButton.html('Submit');
		$(modalTitle).html('Add a New Achievement');
		$(modalInputId).val('');
		$(modalInputId).prop('disabled', false);
	    $(modalInputName).val('');
	    $(modalInputDescription).val('');
	    $(modalInputBadgeId).val('');
	    $(modalInputPointValue).val('0');
		$(modalNotifCheck).prop('checked',false);
		$(modalNotifMessageInput).val('');
	    $("#modalachievementdiv").modal('toggle');
			
		});
	};

	var checkBoxListener = function(){
			// Check boxes in modal
	// check box for point flag
		$('input[type="checkbox"]#achievement_notification_check').click(function(){
	        if($(this).prop("checked") == true){
	            $(modalNotifMessageInput).prop('disabled', false);
	        }
	        else if($(this).prop("checked") == false){
	            $(modalNotifMessageInput).prop('disabled', true);
	        }
	    });
	}

	var submitFormListener = function(){
		$("form#modalachievementform").submit(function(e){
			//disable the default form submission
			e.preventDefault();
			var formData = new FormData($(this)[0]);

			var submitButtonText = $(modalSubmitButton).html();

			var achievementid = $(modalInputId).val();

			if(submitButtonText=='Submit'){
				achievementAccess.createNewAchievement(
					formData, 
					true, 
					function(data,type){
						$("#modalachievementdiv").modal('toggle');
						//reloadActiveTab();
					},
					function(status,error){},
					achievementid
				);
			}
			else{
				achievementAccess.updateAchievement(
					formData, 
					true, 
					function(data,type){
						$("#modalachievementdiv").modal('toggle');
						//reloadActiveTab();
					},
					function(status,error){},
					achievementid
				);
			}
			
			return false;
		});
	};

	var elementDependenciesListener = function(){
		$("#modalachievementdiv").find("#select_badge").on("click", function(e){
				// Retrieve badge data. The only difference is this is read only
			var endPointURL = epURL+"manager/badges/"+currentAppId;
			var tableElement = $("table#list_badges_a");
			badgeAccess.getBadgesDataToTable(
				tableElement,
				{
					"image_path": function(column, row)
			    	{
			    		return "<img class='badgeimagemini' src='"+ badgeAccess.getBadgeImage(row.id) +"' alt='your image' />";
			    	},
			    	"commands": function(column, row)
			        {
			        	// The only difference is this is read only
			            return "<button type=\"button\" class=\"btn btn-xs btn-default command-select\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\">select</span></button> ";
			        }
		    	},
				function(objectgrid){
					objectgrid.find(".command-select").on("click", function(e){	
				    	$("#modalachievementdiv").find("#panel_badge").collapse('toggle')
				    	$("#modalachievementdiv").find("#achievement_badge_id").val($(this).data("row-id"));
				    });
				},
				function(response){}
				);
		});
		// Badge in achievement -----------------------
		$("#modalachievementdiv").find("button.btn#empty_badge").on('click', function(event) {
			$("#modalachievementdiv").find("#achievement_badge_id").val();
		});
	};


	return {
		init : function(){
			initialize();
			loadTable();
			addNewButtonListener();
			elementDependenciesListener();
			checkBoxListener();
			submitFormListener();
		},
		showImageOnChange: function(input) {
			if (input.files && input.files[0]) {
			    var reader = new FileReader();

			    reader.onload = function (e) {
			        $('#badgeimageinmodal')
			            .attr('src', e.target.result)
			            .width(200)
			            .height(200);
			    };

			    reader.readAsDataURL(input.files[0]);
			}
		}
	};


})();