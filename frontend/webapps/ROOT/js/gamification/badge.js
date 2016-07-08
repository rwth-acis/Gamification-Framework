//-------------------------------------------------------------//
// --------------------- BADGES PART ------------------------- //

var getBadgesData = function(){
	currentAppId = Cookies.get("appid");
	var endPointURL = epURL+"games/badges/"+currentAppId;
	$("table#list_badges").bootgrid("destroy");
	var pointgrid = $("table#list_badges").bootgrid({
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
	            return "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\"></span></button> " + 
	                "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-trash-o\"></span></button>";
	        }
	    }
	}).on("loaded.rs.jquery.bootgrid", function()
	{
	    /* Executes after data is loaded and rendered */
	    pointgrid.find(".command-edit").on("click", function(e)
	    {	
	    	var tdcollection = $(this).parent().parent().find("td");

	    	$("#updatebadgediv").find("#badge_id_name").val($(this).data("row-id"));
		    $("#updatebadgediv").find("#badge_name").val(tdcollection[1].textContent);
		    $("#updatebadgediv").find("#badge_desc").val(tdcollection[2].textContent);
		    $("#updatebadgediv").find("#badgeimageinmodalupdate").attr("src",getBadgeImage($(this).data("row-id")));
		    $("#updatebadgediv").modal('toggle');
	    }).end().find(".command-delete").on("click", function(e)
	    {
	    	
	    	deleteBadgeHandler($(this).data("row-id"));
	    });
	});

}

var badgeTabHandler = (function() {

getBadgesData();
$("form#addbadgeform").submit(function(e){
	//disable the default form submission
	e.preventDefault();
	var formData = new FormData($(this)[0]);
	console.log(formData);
	currentAppId = Cookies.get("appid");
	client.sendRequest(
		"POST",
		"games/badges/"+currentAppId,
		formData,
		false,
		{},
		function(data, type){
			console.log(data);
			$("#addbadgediv").modal('toggle');

			reloadActiveTab();
			$.notify({
				// options
				message: "new badge is added !"
			},{
				// settings
				type: 'success'
			});
			return false;
		},
		function(error) {
	         $.notify({
				// options
				message: "Failed to add new badge !"
			},{
				// settings
				type: 'danger'
			});
	         return false;
		}
	);
	return false;
	});

	$("form#updatebadgeform").submit(function(e){
	//disable the default form submission
		e.preventDefault();
		var formData = new FormData($(this)[0]);
		currentAppId = Cookies.get("appid");
		var badgeid = $("#updatebadgediv").find("#badge_id_name").val();
		console.log(formData);
		client.sendRequest(
			"PUT",
			"games/badges/"+currentAppId+"/"+badgeid,
			formData,
			false,
			{},
			function(data, type){
				console.log(data);
				$("#updatebadgediv").modal('toggle');

				reloadActiveTab();
				$.notify({
				// options
					message: "<b>" + badgeid + "</b> is updated !"
				},{
					// settings
					type: 'success'
				});
				return false;
			},
			function(error) {
		         $.notify({
					// options
					message: "Failed to update <b>" + badgeid + "</b> !"
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
function showImageOnChangeUpdate(input) {
if (input.files && input.files[0]) {
    var reader = new FileReader();

    reader.onload = function (e) {
        $('#badgeimageinmodalupdate')
            .attr('src', e.target.result)
            .width(200)
            .height(200);
    };

    reader.readAsDataURL(input.files[0]);
}
}

function showImageOnChangeAdd(input) {
if (input.files && input.files[0]) {
    var reader = new FileReader();

    reader.onload = function (e) {
        $('#badgeimageinmodaladd')
            .attr('src', e.target.result)
            .width(200)
            .height(200);
    };

    reader.readAsDataURL(input.files[0]);
}
}

function getBadgeImage(badgeid){

currentAppId = Cookies.get("appid");

if(!client.isAnonymous()){
	console.log("Authenticated request");
	var rurl = epURL + "games/badges/"+currentAppId+"/" + badgeid + "/img";
	
	return useAuthentication(rurl);
} else {
	console.log("Anonymous request... ");
	return null;
}
	
}

// Handler when delete button in the row table for a badge is pressed
function deleteBadgeHandler(badgeid){				

	currentAppId = Cookies.get("appid");
	var path_URI = "games/badges/"+currentAppId+"/" + badgeid;
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
				message: "<b>" + badgeid + "</b> is deleted !"
			},{
				// settings
				type: 'success'
			});
			return false;
		},
		function(error) {
	         $.notify({
				// options
				message: "Failed to delete <b>" + badgeid + "</b> !"
			},{
				// settings
				type: 'danger'
			});
	          console.log(error);
	    }
	);
// });
}
