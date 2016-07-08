//---------------------------------------------------------//
//--------------------ACHIEVEMENT PART--------------------//
var levelTabHandler = (function(){


	// Achievement part ---------------------------
	currentAppId = Cookies.get("appid");

	var deleteLevelHandler = function(levelnum){				

		currentAppId = Cookies.get("appid");
		var path_URI = "games/levels/"+currentAppId+"/" + levelnum;
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
					message: "<b>" + levelnum + "</b> is deleted !"
				},{
					// settings
					type: 'success'
				});
				return false;
			},
			function(error) {
		         $.notify({
					// options
					message: "Failed to delete <b>" + levelnum + "</b> !"
				},{
					// settings
					type: 'danger'
				});
		          console.log(error);
		    }
		);
	}

	$("table#list_levels").bootgrid("destroy");
	var maxlevel; //to store total data from response
	var endPointURL = epURL+"games/levels/"+currentAppId;
	var achievementgrid = $("table#list_levels").bootgrid({
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
	        "commands": function(column, row)
	        {
	        	if(maxlevel == row.number){
	            	return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-pencil\"></span></button> " + 
	                "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-trash-o\"></span></button>";
	        	}
	        	else{
	        		return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.number + "\"><span class=\"fa fa-pencil\"></span></button> ";
	        	}
	        }
	    },
	    responseHandler: function (response) { 
	    	maxlevel = response.total;

	    	console.log($(this).find("button"));
	    	return response; 
	    }
	}).on("loaded.rs.jquery.bootgrid", function()
	{
		
	    achievementgrid.find(".command-edit").on("click", function(e)
	    {	
	    	var tdcollection = $(this).parent().parent().find("td");

	    	// Adapt Modal with update form
			$("#modallevelsubmit").html('Update');
			$("#modalleveldiv").find(".modal-title").html('Update an Level');
	    	$("#modalleveldiv").find("#level_num").val($(this).data("row-id"));
		    $("#modalleveldiv").find("#level_num").prop('readonly', true);
		    $("#modalleveldiv").find("#level_name").val(tdcollection[1].textContent);
		    $("#modalleveldiv").find("#level_point_value").val(tdcollection[2].textContent);
		    $("#modalleveldiv").modal('toggle');
	    }).end().find(".command-delete").on("click", function(e)
	    {
	    	deleteLevelHandler($(this).data("row-id"));
	    	console.log($(this).data("row-id"));
	    });
	});

	
	$("#addnewlevel").on('click', function(event) {
		// count number of rows so the number can be incremented
		var levelindex = $("table#list_levels").find("tr").length - 1;
		console.log($("table#list_levels").find("tr"));
	    // Adapt Modal with add form
		$("#modallevelsubmit").html('Submit');
		$("#modalleveldiv").find("#level_num").prop('readonly', true);
		$("#modalleveldiv").find("#level_num").val(levelindex);
		$("#modalleveldiv").find(".modal-title").html('Add a New Level');
	    $("#modalleveldiv").find("#level_name").val('');
	    $("#modalleveldiv").find("#level_point_value").val('');
	    $("#modalleveldiv").modal('toggle');
		
	});

	// ----------------------------------------------------------------
	// Point in level ------------------------------------


	// Retrieve point data. The only difference is this is read only


	$("form#modallevelform").submit(function(e){
		//disable the default form submission
		e.preventDefault();

		var formData = new FormData($(this)[0]);
		console.log(formData);
		currentAppId = Cookies.get("appid");
		var levelnum = $("#modalleveldiv").find("#level_num").val();
			
		var method;
		var endPointPath;
		if($(this).find("button#modallevelsubmit").html()=='Submit'){
			method = "POST";
			endPointPath = "games/levels/"+currentAppId;
		}
		else{
			method = "PUT";
			endPointPath = "games/levels/"+currentAppId+"/"+levelnum;
		}
		client.sendRequest(
			method,
			endPointPath,
			formData,
			false,
			{},
			function(data, type){
				console.log(data);
				$("#modalleveldiv").modal('toggle');

				reloadActiveTab();
				$.notify({
					// options
					message: "<b>" + levelnum + "</b> is added !"
				},{
					// settings
					type: 'success'
				});
				return false;
			},
			function(error) {
		         $.notify({
					// options
					message: "Failed to add <b>" + levelnum + "</b> !"
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