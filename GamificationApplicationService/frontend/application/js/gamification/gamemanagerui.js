
		var counter = 0;
		var iwcClient = null;
		var epURL = "http://localhost:8081/";
		var client = new TemplateServiceClient(epURL);

		var currentAppId = null;
		var memberId;
		var useAuthentication = function(rurl){
	    	if(rurl.indexOf("\?") > 0){	
					rurl += "&access_token=" + window.localStorage["access_token"];
				} else {
					rurl += "?access_token=" + window.localStorage["access_token"];
				}
				return rurl;
	    }
		function init(){

	  //   	$.get("visualization.html", function(data){
			//     //$(this).children("body").html(data);
			//     $('body').append(data);
			//     loadjscssfile();
			//     vis.init();
			// });
		}
//-------------------------------------------------------------//
// --------------------- DASHBOARD PART ------------------------- //
		


		var showLoginView = function(isShown){
			if(isShown == true){
				$("#loginview").show();
				$("#headerbutton").hide();
				showAppSelection(false);
			}
			else
			{
				$("#loginview").hide();
				$("#headerbutton").show();
			}
			
		};
		var showAppSelection = function(isShown){
			if(isShown == true){
				$("#appselection").show();
				showLoginView(false);
				// Hide app id name and go to app selection option
				var texthtml = "App ID<b class='caret'></b>";
				$("#apptitle").html(texthtml);
				$("#backtoappselection").hide();
				//$("#settingbutton").show();
			}
			else
			{
				$("#appselection").hide();
			}
			
		};


		function checkAndRegisterUserAgent(){
			client.sendRequest("POST",
	    			"gamification/applications/validation",
	    			"",
	    			"application/json",
	    			{},
	    			function(data,type){
							getApplicationsData();
					},
	    			function(error) {
	    		        $('#appselection').before('<div class="alert alert-danger">Error connecting web services</div>');
	    		    }
	    		);
		}


		function signinCallback(result) {
		    if(result === "success"){
		    	memberId = oidc_userinfo.preferred_username;
		        // after successful sign in, display a welcome string for the user
		        $("#status").html("Hello, " + memberId + "!");
		        
		        console.log(oidc_userinfo)

		        checkAndRegisterUserAgent();
		        showLoginView(false);

		    } else {
		        // if sign in was not successful, log the cause of the error on the console
		        showAppSelection(false);
		        showLoginView(true);

		        // dev
		        // showAppSelection(false);
		        // showLoginView(false);

		        console.log(result);
		    }
		}
		
		function getApplicationsData(){
				client.sendRequest("GET",
	    			"gamification/applications/list/separated",
	    			"",
	    			"application/json",
	    			{},
	    			function(data,type){

	    				console.log(data);
	    				//Global apps
	    				$("#globalappstbody").empty();
			    		for(var i = 0; i < data[0].length; i++){
				    		var appData = data[0][i];
				    		var newRow = "<tr><td id='appidid'>" + appData.id + "</td>";
				    		newRow += "<td id='appdescid'>" + appData.description + "</td>";
							newRow += "<td id='appcommtypeid'>" + appData.commType + "</td>";
										    	
				    		$("#list_global_apps_table tbody").append(newRow);
				    	}

				    	//User apps
				    	$("#registeredappstbody").empty();
			    		for(var i = 0; i < data[1].length; i++){
				    		var appData = data[1][i];
				    		var newRow = "<tr><td id='appidid'>" + appData.id + "</td>";
				    		newRow += "<td id='appdescid'>" + appData.description + "</td>";
							newRow += "<td id='appcommtypeid'>" + appData.commType + "</td>";
										    	
				    		$("#list_registered_apps_table tbody").append(newRow);
				    	}

						//Settings modal
						$("#registeredappssettingstbody").empty();
			    		for(var i = 0; i < data[1].length; i++){
				    		var appData = data[1][i];
				    		var newRow = "<tr><td id='appidid' class='appidclass'>" + appData.id + "</td>";
				    		newRow += "<td id='appdescid'>" + appData.description + "</td>";
							newRow += "<td id='appcommtypeid'>" + appData.commType + "</td>";
							newRow += "<td><button type='button' onclick='removeApplicationHandler(this)' data-dismiss='modal' data-toggle='modal' data-target='#alertremoveapp' class='btn btn-danger bdelclass'>Remove</button></td>";
							newRow += "<td><button type='button' onclick='deleteApplicationHandler(this)' data-dismiss='modal' data-toggle='modal' data-target='#alertdeleteapp' class='btn btn-danger bdelclass'>Delete</button></td>";

				    		$("#list_registered_apps_settings_table tbody").append(newRow);
				    	}



				    	// Stay in Application Selection where there is no specified application selected
				    	currentAppId = Cookies.get('appid');
				    	if(currentAppId == null || currentAppId == '' || currentAppId == undefined){
						 	showAppSelection(true);				    		
				    	}
				    	else
				    	{

				    		// Here is the point where user get the main content


				    	}
	    			},
	    			function(error) {
	    		        $.notify({
							// options
							message: error.message
						},{
							// settings
							type: 'danger'
						});
						console.log(error);
	    		   }
	    		);

		}

		function removeApplicationHandler(element){
			var selectedappid = element.parentNode.parentNode.getElementsByClassName("appidclass")[0].textContent
			$('#alertremoveapp').find('button.btn').attr('id',selectedappid);
			$('#alertremoveapp_text').text('Are you sure you want to remove ' + selectedappid +"?");
		}
		function deleteApplicationHandler(element){
			var selectedappid = element.parentNode.parentNode.getElementsByClassName("appidclass")[0].textContent
			$('#alertdeleteapp').find('button.btn').attr('id',selectedappid);
			$('#alertdeleteapp_text').text('Are you sure you want to delete ' + selectedappid +"?");
		}

		function removeApplicationAlertHandler(){
			console.log('clicked');
			currentAppId = Cookies.get('appid');
			var selectedappid = $('#alertremoveapp').find('button.btn').attr('id');
			client.sendRequest("DELETE",
    			"gamification/applications/data/"+selectedappid+"/"+memberId,
    			"",
    			"application/json",
    			{},
    			function(data,type){
    				// opened app is the selected app
    				if(selectedappid == currentAppId){
	    				Cookies.remove('appid');
	    				getApplicationsData();
    				}
    				else{
	    				getApplicationsData();
    				}
    				console.log(data);
    			},
    			function(error) {
    		        $.notify({
							// options
							message: error.message
						},{
							// settings
							type: 'danger'
						});
		        }
    		);
			
			$("#alertremoveapp").modal('toggle');	
		}


		function deleteApplicationAlertHandler(){
			console.log('clicked');
			currentAppId = Cookies.get('appid');
			var selectedappid = $('#alertdeleteapp').find('button.btn').attr('id');
			client.sendRequest("DELETE",
    			"gamification/applications/data/"+selectedappid,
    			"",
    			"application/json",
    			{},
    			function(data,type){
    				// opened app is the selected app
    				if(selectedappid == currentAppId){
	    				Cookies.remove('appid');
	    				

						// Notification
						$.notify({
							// options
							message: selectedappid + " is deleted !"
						},{
							// settings
							type: 'success'
						});
    				}
	    			getApplicationsData();
    				
    				console.log(data);
    			},
    			function(error) {
    		         $.notify({
							// options
							message: error.message
						},{
							// settings
							type: 'danger'
						});
		        }
    		);
			
			$("#alertdeleteapp").modal('toggle');	
		}

		




		$(function() {
			$.notifyDefaults({
				placement: {
					from: "top",
					align: "left"
				},
				delay: 2500,
				timer: 500,
				allow_dismiss: true,
				z_index: 9001,
			});
			
			//$("#settingbutton").hide();
//--------------------------- Application selection -------------------------------//
			// Handler when the form in "Create New App" is submitted
			// App ID will be retrieved from the service and will be put on the id attribute in class maincontent
			$("form#createnewappform").submit(function(e){
				//disable the default form submission
				e.preventDefault();
				var formData = new FormData($(this)[0]);
				client.sendRequest(
					"POST",
					"gamification/applications/data",
					formData,
					false,
					{},
					function(data, type){
						console.log(data);
						var selectedAppId = $("#createnewapp_appid").val();
						
						Cookies.set('appid', selectedAppId);
						//$('#innerheader').before('<div class="alert alert-success"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>New App Created!</div>');

						$("#createnewapp").modal('toggle');

						// Notification
						// $.notify({
						// 	// options
						// 	message: "New Application " + selectedAppId + " Created"
						// },{
						// 	// settings
						// 	type: 'success'
						// });
						return false;
					},
					function(error) {
						$.notify({
							// options
							message: error.message
						},{
							// settings
							type: 'danger'
						});
					}
				);
				return false;
			});
			
			$('#list_global_apps_table').on('click','tbody tr', function(event) {
				//$(this).addClass('active').siblings().removeClass('active');
				//Get Value in appidid
				var selectedAppId = $(this).find("td#appidid")[0].textContent;
				$('#alertglobalapp_text').text('Are you sure you want to open ' + selectedAppId +"?. You will be registered to selected application.");
				$('#alertglobalapp').find('button').attr('id',selectedAppId);
				$("#alertglobalapp").modal('toggle');
			});

			$('#alertglobalapp').find('button.btn').on('click', function(event) {

				Cookies.set('appid',$(this).attr('id'));
				currentAppId = Cookies.get('appid')
				//$('#innerheader').before('<div class="alert alert-success"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>Welcome to '+currentAppId+' !</div>');
				$("#alertglobalapp").modal('toggle');
				// Implicitly store user to app
				// add member to app
				client.sendRequest("POST",
	    			"gamification/applications/data/"+currentAppId+"/"+memberId,
	    			"",
	    			"application/json",
	    			{},
	    			function(data,type){
	    				console.log(data);
	    				getApplicationsData();
				
	    			},
	    			function(error) {
	    		        $.notify({
							// options
							message: error.message
						},{
							// settings
							type: 'danger'
						});
	    		    }
	    		);

			});

			$('#list_registered_apps_table').on('click','tbody tr', function(event) {
				//$(this).addClass('active').siblings().removeClass('active');
				//Get Value in appidid
				var selectedAppId = $(this).find("td#appidid")[0].textContent;
				$('#alertregisteredapp_text').text('Are you sure you want to open ' + selectedAppId +"?");
				$('#alertregisteredapp').find('button').attr('id',selectedAppId);
				$("#alertregisteredapp").modal('toggle');
			});

			$('#alertregisteredapp').find('button.btn').on('click', function(event) {

				Cookies.set('appid',$(this).attr('id'));
				currentAppId = Cookies.get('appid')
				getApplicationsData();
				// Notification
				$.notify({
					// options
					message: 'Welcome to <b>' + currentAppId + '</b> !'
				},{
					// settings
					type: 'success'
				});
				$("#alertregisteredapp").modal('toggle');
			});


	
		});

		$(document).ready(init());
		