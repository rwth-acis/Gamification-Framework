
		var counter = 0;
		var iwcClient = null;
		var epURL = "http://localhost:8081/";
		var client = new TemplateServiceClient(epURL);

		var currentAppId = 'test';
		var memberId;
		var useAuthentication = function(rurl){
	    	if(rurl.indexOf("\?") > 0){	
					rurl += "&access_token=" + window.localStorage["access_token"];
				} else {
					rurl += "?access_token=" + window.localStorage["access_token"];
				}
				return rurl;
	    }

	    var pointTabHandler = (function() {
			var endPointPath = "gamification/points/"+currentAppId+"/name";
			client.sendRequest(
				"GET",
				endPointPath,
				{},
				false,
				{},
				function(data, type){
					console.log(data.pointUnitName);
					$("#point_id_container").find("#level_point_id_static").html(data.pointUnitName);
					
					return false;
				},
				function(error) {
			         $.notify({
						// options
						message: "Failed to fetch unit name !"
					},{
						// settings
						type: 'danger'
					});
			         return false;
				}
			);
			$("#point_id_container").find("#select_point").on("click", function(e){
				console.log("asa");
				var unitName = $("#point_id_container").find("#level_point_id").val();
				console.log(unitName);
				var endPointPath = "gamification/points/"+currentAppId+"/name/"+unitName;
				client.sendRequest(
					"PUT",
					endPointPath,
					"",
					false,
					{},
					function(data, type){
						console.log(data);
						$("#point_id_container").find("#level_point_id").val('');
						$("#point_id_container").find("#level_point_id_static").html(unitName);
						$.notify({
							// options
							message: "Unit name updated !"
						},{
							// settings
							type: 'success'
						});
						return false;
					},
					function(error) {
				         $.notify({
							// options
							message: "Failed to update unit name !"
						},{
							// settings
							type: 'danger'
						});
			         return false;
					}
				);
			});
		});

		function init(){

	  //   	$.get("visualization.html", function(data){
			//     //$(this).children("body").html(data);
			//     $('body').append(data);
			//     loadjscssfile();
			//     vis.init();
			// });
		}


		function signinCallback(result) {
		    if(result === "success"){
		    	memberId = oidc_userinfo.preferred_username;
		        // after successful sign in, display a welcome string for the user
		        $("#status").html("Hello, " + memberId + "!");
		        
		        $('#loginview').hide();

		        $('#page-wrapper').show();
		         pointTabHandler();

		    } else {

		        console.log(result);

		        $('#loginview').show();

		        $('#page-wrapper').hide();
		    }

		}
		$(document).ready(init());
		