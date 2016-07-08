//---------------------------------------------------------//
//--------------------ACHIEVEMENT PART--------------------//
var actionTabHandler = (function(){


	// Achievement part ---------------------------
	currentAppId = Cookies.get("appid");

	var deleteActionHandler = function(actionid){				

		currentAppId = Cookies.get("appid");
		var path_URI = "games/actions/"+currentAppId+"/" + actionid;
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
					message: "<b>" + actionid + "</b> is deleted !"
				},{
					// settings
					type: 'success'
				});
				return false;
			},
			function(error) {
		         $.notify({
					// options
					message: "Failed to delete <b>" + actionid + "</b> !"
				},{
					// settings
					type: 'danger'
				});
		          console.log(error);
		    }
		);
	}

	$("table#list_actions").bootgrid("destroy");
	
	var endPointURL = epURL+"games/actions/"+currentAppId;
	var actiongrid = $("table#list_actions").bootgrid({
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
	        "commands": function(column, row)
	        {
	            	return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\"></span></button> " + 
	                "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-trash-o\"></span></button>";
	        }
	    },
	    responseHandler: function(response){
	    	console.log(response);
	    	return response;
	    }
	}).on("loaded.rs.jquery.bootgrid", function()
	{
		
	    actiongrid.find(".command-edit").on("click", function(e)
	    {	
	    	var tdcollection = $(this).parent().parent().find("td");

	    	// Adapt Modal with update form
			$("#modalactionsubmit").html('Update');
			$("#modalactiondiv").find(".modal-title").html('Update an Action');
	    	$("#modalactiondiv").find("#action_id_name").val($(this).data("row-id"));
		    $("#modalactiondiv").find("#action_id_name").prop('readonly', true);
		    $("#modalactiondiv").find("#action_name").val(tdcollection[1].textContent);
		    $("#modalactiondiv").find("#action_desc").val(tdcollection[2].textContent);
		    $("#modalactiondiv").find("#action_point_value").val(tdcollection[3].textContent);
		    $("#modalactiondiv").modal('toggle');
	    }).end().find(".command-delete").on("click", function(e)
	    {
	    	deleteActionHandler($(this).data("row-id"));
	    });
	});

	
	$("#addnewaction").on('click', function(event) {
		// count number of rows so the number can be incremented
	    // Adapt Modal with add form
		$("#modalactionsubmit").html('Submit');
		$("#modalactiondiv").find("#action_id_name").prop('readonly', false);
		$("#modalactiondiv").find("#action_id_name").val('');
		$("#modalactiondiv").find(".modal-title").html('Add a New Action');
	    $("#modalactiondiv").find("#action_name").val('');
	    $("#modalactiondiv").find("#action_point_value").val('');
	    $("#modalactiondiv").modal('toggle');
		
	});


	$("form#modalactionform").submit(function(e){
		//disable the default form submission
		e.preventDefault();

		var formData = new FormData($(this)[0]);
		console.log(formData);
		currentAppId = Cookies.get("appid");
		var actionid = $("#modalactiondiv").find("#action_id_name").val();
			
		var method;
		var endPointPath;
		if($(this).find("button#modalactionsubmit").html()=='Submit'){
			method = "POST";
			endPointPath = "games/actions/"+currentAppId;
		}
		else{
			method = "PUT";
			endPointPath = "games/actions/"+currentAppId+"/"+actionid;
		}
		client.sendRequest(
			method,
			endPointPath,
			formData,
			false,
			{},
			function(data, type){
				console.log(data);
				$("#modalactiondiv").modal('toggle');

				reloadActiveTab();
				$.notify({
					// options
					message: "<b>" + actionid + "</b> is added !"
				},{
					// settings
					type: 'success'
				});
				return false;
			},
			function(error) {
		         $.notify({
					// options
					message: "Failed to add <b>" + actionid + "</b> !"
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