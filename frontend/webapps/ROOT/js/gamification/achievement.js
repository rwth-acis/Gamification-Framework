//---------------------------------------------------------//
//--------------------ACHIEVEMENT PART--------------------//
var getBadgeDataWithId = function(badgeid, successCallback, errorCallback){
	currentAppId = Cookies.get("appid");
	client.sendRequest(
		"GET",
		"games/badges/"+currentAppId+"/"+badgeid,
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

var achievementTabHandler = (function(){
	badgeTabHandler();

	// Achievement part ---------------------------
	currentAppId = Cookies.get("appid");



		// Handler when delete button in the row table for a badge is pressed
	var deleteAchievementHandler = function(achievementid){				

		currentAppId = Cookies.get("appid");
		var path_URI = "games/achievements/"+currentAppId+"/" + achievementid;
		client.sendRequest("DELETE",
			path_URI,
			"",
			false,
			{},
			function(data, type){
				console.log(data);
				reloadActiveTab();
			    $.notify({
					// options
					message: "<b>" + achievementid + "</b> is deleted !"
				},{
					// settings
					type: 'success'
				});
				return false;
			},
			function(error) {
		         $.notify({
					// options
					message: "Failed to delete <b>" + achievementid + "</b> !"
				},{
					// settings
					type: 'danger'
				});
		          console.log(error);
		    }
		);
	// });
	}

	$("table#list_achievements").bootgrid("destroy");

	var endPointURL = epURL+"games/achievements/"+currentAppId;
	var achievementgrid = $("table#list_achievements").bootgrid({
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
	    responseHandler:function(response){
	    	console.log(response);
	    	return response;
	    }
	}).on("loaded.rs.jquery.bootgrid", function()
	{
		    // Show badge data as popover
	    achievementgrid.find(".show-badge").popover({
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
	    achievementgrid.find(".command-edit").on("click", function(e)
	    {	
	    	var tdcollection = $(this).parent().parent().find("td");

	    	// Adapt Modal with update form
			$("#modalachievementsubmit").html('Update');
			$("#modalachievementdiv").find(".modal-title").html('Update an Achievement');
	    	$("#modalachievementdiv").find("#achievement_id_name").val($(this).data("row-id"));
	    	$("#modalachievementdiv").find("#achievement_id_name").prop('disabled', true);
		    $("#modalachievementdiv").find("#achievement_name").val(tdcollection[1].textContent);
		    console.log(tdcollection[4].textContent);
		    $("#modalachievementdiv").find("#achievement_desc").val(tdcollection[2].textContent);
		    $("#modalachievementdiv").find("#achievement_point_value").val(tdcollection[3].textContent);
		    $("#modalachievementdiv").find("#achievement_badge_id").val(tdcollection[4].textContent);
		    $("#modalachievementdiv").modal('toggle');
	    }).end().find(".command-delete").on("click", function(e)
	    {
	    	deleteAchievementHandler($(this).data("row-id"));
	    }).end().find(".show-badge").on("click", function(e)
	    {
	    	// Get badge data with id
	    	e.preventDefault();
	    	// Get id of the selected element to be attached with popover 
	    	var idelement = "#" + $(this).data("row-badgeid");
	    	getBadgeDataWithId(
	    		$(this).data("row-badgeid"),
	    		function(data,type){
	    			// Render in popover
	    			console.log(data);
	    			$("#badge-popover-content").find("#badgeidpopover").html(data.id);
	    			$("#badge-popover-content").find("#badgenamepopover").html(data.name);
	    			$("#badge-popover-content").find("#badgedescpopover").html(data.description);
	    			$("#badge-popover-content").find("#badgeimagepopover").attr("src",useAuthentication(data.imagePath))
	    			achievementgrid.find(idelement).popover('show');

	    			// Dismiss popover when click anywhere
	    			$(document).click(function() {
					    achievementgrid.find(idelement).popover('hide');
					});
	    			$(".popover").on("click", function(e)
				    {
				    	achievementgrid.find(idelement).popover('hide');
				    });
	    		},
	    		function(error){

	    		});
	    });
	});

	
	$("#addnewachievement").on('click', function(event) {

	    // Adapt Modal with add form
		$("#modalachievementsubmit").html('Submit');
		$("#modalachievementdiv").find(".modal-title").html('Add a New Achievement');
		$("#modalachievementdiv").find("#achievement_id_name").val('');
		$("#modalachievementdiv").find("#achievement_id_name").prop('disabled', false);
	    $("#modalachievementdiv").find("#achievement_name").val('');
	    $("#modalachievementdiv").find("#achievement_desc").val('');
	    $("#modalachievementdiv").find("#achievement_badge_id").val('');
	    $("#modalachievementdiv").find("#achievement_point_value").val('0');
	    $("#modalachievementdiv").modal('toggle');
		
	});


	$("#modalachievementdiv").find("#select_badge").on("click", function(e){
			// Retrieve badge data. The only difference is this is read only
		var endPointURL = epURL+"games/badges/"+currentAppId;
		$("#modalachievementdiv").find("table#list_badges_a").bootgrid("destroy");
		var badgegrid = $("#modalachievementdiv").find("table#list_badges_a").bootgrid({
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
		    	"image_path": function(column, row)
		    	{
		    		return "<img class='badgeimagemini' src='"+ getBadgeImage(row.id) +"' alt='your image' />";
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
		    badgegrid.find(".command-select").on("click", function(e)
		    {	
		    	$("#modalachievementdiv").find("#panel_badge").collapse('toggle')
		    	$("#modalachievementdiv").find("#achievement_badge_id").val($(this).data("row-id"));
		    });
		});
	});

	// Badge in achievement -----------------------
	$("#modalachievementdiv").find("button.btn#empty_badge").on('click', function(event) {
		$("#modalachievementdiv").find("#achievement_badge_id").val();
	});

	$("form#modalachievementform").submit(function(e){
		//disable the default form submission
		e.preventDefault();
		var formData = new FormData($(this)[0]);
		console.log(formData);
		currentAppId = Cookies.get("appid");
		var achievementid = $("#modalachievementdiv").find("#achievement_id_name").val();
			
		var method;
		var endPointPath;
		if($(this).find("button#modalachievementsubmit").html()=='Submit'){
			method = "POST";
			endPointPath = "games/achievements/"+currentAppId;
		}
		else{
			method = "PUT";
			endPointPath = "games/achievements/"+currentAppId+"/"+achievementid;
		}
		client.sendRequest(
			method,
			endPointPath,
			formData,
			false,
			{},
			function(data, type){
				console.log(data);
				$("#modalachievementdiv").modal('toggle');

				reloadActiveTab();
				$.notify({
					// options
					message: "<b>" + achievementid + "</b> is added !"
				},{
					// settings
					type: 'success'
				});
				return false;
			},
			function(error) {
		         $.notify({
					// options
					message: "Failed to add <b>" + achievementid + "</b> !"
				},{
					// settings
					type: 'danger'
				});
		         return false;
			}
		);
		return false;
	});


});
