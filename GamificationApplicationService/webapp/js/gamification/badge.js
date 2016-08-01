//-------------------------------------------------------------//
// --------------------- BADGES PART ------------------------- //

var badgeModule = (function() {
	
	var badgeAccess;
	var modalInputId;
	var modalInputName;
	var modalInputDescription;
	var modalSubmitButton;
	var modalTitle;
	var modalNotifCheck;
	var modalNotifMessageInput;

	var badgeCollection;

	var initialize = function(){

		badgeAccess = new BadgeDAO();
		modalInputId = $("#modalbadgediv").find("#badge_id")
		modalInputName = $("#modalbadgediv").find("#badge_name");
		modalInputDescription = $("#modalbadgediv").find("#badge_desc");
		modalImage = $("#modalbadgediv").find("#badgeimageinmodal");
		modalSubmitButton = $("#modalbadgediv").find("#modalbadgesubmit");
		modalNotifCheck = $("#modalbadgediv").find("#badge_notification_check");
		modalNotifMessageInput = $("#modalbadgediv").find("#badge_notification_message");
		modalTitle = $("#modalbadgediv").find(".modal-title");
	};

	var loadTable = function(){
		var badgeCollection;
		// var tableElement = $("table#list_badges");
		// badgeAccess.getBadgesDataToTable(
		// 	tableElement,
		// 	{
		// 		"image_path": function(column, row)
		//     	{
		//     		return "<img class='badgeimagemini' src='"+ badgeAccess.getBadgeImage(row.id) +"' alt='your image' />";
		//     	},
		//         "commands": function(column, row)
		//         {
		//             return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\"></span></button> " + 
		//                 "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-trash-o\"></span></button>";
		//         }
	 //    	},
		// 	function(objectgrid){
		// 		objectgrid.find(".command-edit").on("click", function(e)
		// 	    {	
		// 	    	// assign data from badgeCollection
		// 			var badgeid_ = $(this).data("row-id");

		// 	    	var index = 0;
		// 	    	for(var i = 0; i < badgeCollection.length; i++){
		// 	    		if(badgeCollection[i].id == badgeid_)
		// 	    		{
		// 	    			index = i;
		// 	    			break;
		// 	    		}
		// 	    		if(i == badgeCollection.length -1){
		// 	    			throw "Badge ID not found in collection";
		// 	    			return false;
		// 	    		}
		// 	    	}
		// 	    	// Adapt Modal with update form
		// 	    	var selectedBadge = badgeCollection[index];

		// 	    	$(modalSubmitButton).html('Update');
		// 	    	$(modalInputId).val($(this).data("row-id"));
		// 			$(modalInputId).prop('readonly', true);
		// 			modalTitle.html('Update a Badge');
		// 		    $(modalInputName).val(selectedBadge.name);
		// 		    $(modalInputDescription).val(selectedBadge.description);
		// 		    $(modalImage).attr("src",badgeAccess.getBadgeImage($(this).data("row-id")));
		// 		    $(modalImage).prop('required',false);
		// 		    $(modalNotifCheck).prop('checked',selectedBadge.useNotification);
		// 			$(modalNotifMessageInput).val(selectedBadge.notificationMessage);
		// 			$(modalNotifMessageInput).prop('readonly',true);
		// 		    if(selectedBadge.useNotification){
		// 		    	$(modalNotifMessageInput).prop('readonly',false);
		// 		    }
		// 		    $("#modalbadgediv").modal('toggle');
		// 	    }).end().find(".command-delete").on("click", function(e)
		// 	    {
			    	
		// 	    	var badgeid = $(this).data("row-id");
		// 	    	badgeAccess.deleteBadge(
		// 	    		true,
		// 				function(data,type){
		// 					$("#modalbadgediv").modal('toggle');
		// 					reloadActiveTab();
		// 				},
		// 				function(status,error){},
		// 	    		badgeid
		// 	    	);
		// 	    });
		// 	},
		// 	function(response){
		// 		badgeCollection = response.rows;
		// 		console.log(response.rows);
		// 	}
		// );

	// Handler when update button in the row table for a badge is pressed
		var updateBadgeHandler = function(element){
			if(badgeCollection){

  				var selectedRow =  $(element).parent().parent();
		    	// Adapt Modal with update form
		    	var selectedBadge = badgeCollection[selectedRow.rowIndex];

		    	$(modalSubmitButton).html('Update');
		    	$(modalInputId).val(selectedBadge.id);
				$(modalInputId).prop('readonly', true);
				modalTitle.html('Update a Badge');
			    $(modalInputName).val(selectedBadge.name);
			    $(modalInputDescription).val(selectedBadge.description);
			    $(modalImage).attr("src",badgeAccess.getBadgeImage(selectedBadge.id));
			    $(modalImage).prop('required',false);
			    $(modalNotifCheck).prop('checked',selectedBadge.useNotification);
				$(modalNotifMessageInput).val(selectedBadge.notificationMessage);
				$(modalNotifMessageInput).prop('readonly',true);
			    if(selectedBadge.useNotification){
			    	$(modalNotifMessageInput).prop('readonly',false);
			    }
			    $("#modalbadgediv").modal('toggle');
			}
		}

		// Handler when delete button in the row table for a badge is pressed
		var deleteBadgeHandler = function(element){

				// $(".bimgclass").on('click', function(e){
				//e.preventDefault();
				
			// });
		}

	

				// Handler when the Badges tab is selected. Retrieve all badges data
    	//function getBadgesData(){
		$("table#list_badges").find("tbody").empty();
		badgeAccess.getBadgesData(
			function(data,type){
	    		badgeCollection = data.rows;
				for(var i = 0; i < data.rows.length; i++){
		    		var badge = data.rows[i];
		    		console.log(badge);
		    		var newRow = "<tr><td class='bidclass'>" + badge.id + "</td>";
		    		newRow += "<td class='bnameclass'>" + badge.name + "</td>";
					newRow += "<td class='bdescclass'>" + badge.description + "</td>";
					//newRow += "<td><button id='" + i + "' type='button' onclick='viewBadgeImageHandler(this,0)' class='btn btn-info bimgclass' name='"+ badge.imagePath +"' data-toggle='modal' data-target='#modalimage'>View Image</button></td>";
					newRow += "<td><img class='badgeimage' src='"+ badgeAccess.getBadgeImage(badge.id) +"' alt='your image' /></td>";
					newRow += "<td class='busenotifclass''>" + badge.useNotification + "</td>";
					newRow += "<td class='bmessageclass''>" + badge.notificationMessage + "</td>";
					newRow += "<td>" + "<button type='button' class='btn btn-warning btn-sm bupdclass' >Update</button> ";
					newRow += "<button type='button' class='btn btn-danger btn-sm bdelclass'>Delete</button></td>";
		    		
		    		$("table#list_badges").find("tbody").append(newRow);
		    	}

    			$("table#list_badges").find(".bupdclass").on("click", function(element){
					if(badgeCollection){

		  				var selectedRow =  $(element.target).parent().parent()[0];
				    	var selectedBadge = badgeCollection[selectedRow.rowIndex];

				    	$(modalSubmitButton).html('Update');
				    	$(modalInputId).val(selectedBadge.id);
						$(modalInputId).prop('readonly', true);
						modalTitle.html('Update a Badge');
					    $(modalInputName).val(selectedBadge.name);
					    $(modalInputDescription).val(selectedBadge.description);
					    $(modalImage).attr("src",badgeAccess.getBadgeImage(selectedBadge.id));
					    $(modalImage).prop('required',false);
					    $(modalNotifCheck).prop('checked',selectedBadge.useNotification);
						$(modalNotifMessageInput).val(selectedBadge.notificationMessage);
						$(modalNotifMessageInput).prop('readonly',true);
					    if(selectedBadge.useNotification){
					    	$(modalNotifMessageInput).prop('readonly',false);
					    }
					    $("#modalbadgediv").modal('toggle');
					}
				});

				$("table#list_badges").find(".bdelclass").on("click", function(element){
					var selectedRow =  $(element.target).parent().parent()[0];
				    var selectedBadge = badgeCollection[selectedRow.rowIndex];

			    	badgeAccess.deleteBadge(
			    		true,
			    		function(data,type){},
			    		function(status,error){},
			    		selectedBadge.id);
					});
    		},
    		function(status,error) {
    			console.log(error);
		    }
		);

		


	};

	var addNewButtonListener = function(){
		$("#addnewbadge").on('click', function(event) {
		    // Adapt Modal with add form
		    $(modalSubmitButton).html('Submit');
			$(modalInputId).prop('readonly', false);
			$(modalInputId).val('');
			$(modalTitle).html('Add a New Badge');
		    $(modalInputName).val('');
		   	$(modalInputDescription).val('');
		    $(modalImage).prop('src','');
		    $(modalImage).prop('required',true);				    
			$(modalNotifCheck).prop('checked',false);
			$(modalNotifMessageInput).val('');
		    $("#modalbadgediv").modal('toggle');
			
		});
	};

	var checkBoxListener = function(){
			// Check boxes in modal
	// check box for point flag
		$('input[type="checkbox"]#badge_notification_check').click(function(){
	        if($(this).prop("checked") == true){
	            $(modalNotifMessageInput).prop('disabled', false);
	        }
	        else if($(this).prop("checked") == false){
	            $(modalNotifMessageInput).prop('disabled', true);
	        }
	    });
	}

	var submitFormListener = function(){
		$("form#modalbadgeform").submit(function(e){
			//disable the default form submission
			e.preventDefault();
			var formData = new FormData($(this)[0]);

			var submitButtonText = $(modalSubmitButton).html();

			var badgeid = $(modalInputId).val();

			if(submitButtonText=='Submit'){
				badgeAccess.createNewBadge(
					formData, 
					true, 
					function(data,type){
						$("#modalbadgediv").modal('toggle');
						reloadActiveTab();
					},
					function(status,error){},
					badgeid
				);
			}
			else{
				badgeAccess.updateBadge(
					formData, 
					true, 
					function(data,type){
						$("#modalbadgediv").modal('toggle');
						reloadActiveTab();
					},
					function(status,error){},
					badgeid
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
		},
		loadTable:loadTable,
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
