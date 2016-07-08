
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
		}
//-------------------------------------------------------------//
// --------------------- DASHBOARD PART ------------------------- //
		var dashboardTabHandler = (function(){
				pointTabHandler();
		});

		var pointTabHandler = (function() {
			var endPointPath = "games/points/"+currentAppId+"/name";
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
				var endPointPath = "games/points/"+currentAppId+"/name/"+unitName;
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


//---------------------------------------------------------//
//--------------------MAIN CONTENT PART--------------------//
		function loadMainContentCallback(){
			currentAppId = Cookies.get('appid');
			var texthtml = currentAppId+"<b class='caret'></b>";
			$("#apptitle").html(texthtml);
			$("#backtoappselection").show();
			console.log(currentAppId);
			mainContentHandler();	
		}

		// Dynamically load contents
		var mainContentHandler = (function() {
			$("#menucontent").load("dashboard.html", dashboardTabHandler);
			$('a[data-toggle="tab"]').on('show.bs.tab', function (e) {

			  var target = $(e.target).attr("href") // activated tab
			  	switch(target) {
				    case "#pointtab":
				    	$("#menucontent").load("point.html", pointTabHandler);
				        break;
				    case "#badgetab":
				    	$("#menucontent").load("badge.html", badgeTabHandler);
				        break;
				    case "#achievementtab":
				    	$("#menucontent").load("achievement.html", achievementTabHandler);
				        break;
				    case "#actiontab":
				    	$("#menucontent").load("action.html", actionTabHandler);
				        break;
				    case "#questtab":
				    	$("#menucontent").load("quest.html", questTabHandler);
				        break;
				     case "#leveltab":
				    	$("#menucontent").load("level.html", levelTabHandler);
				        break;
				     case "#leaderboardtab":
				    	$("#menucontent").load("leaderboard.html", leaderboardTabHandler);
				        break;
				}
			});

			$("a#dashboardlink").on('click', function(event) {
				$("#menucontent").load("dashboard.html", dashboardTabHandler);
			});

			


			$('#backtoappselection').on('click', function(event) {
				//$(this).addClass('active').siblings().removeClass('active');
				//Get Value in appidid
				Cookies.remove('appid');
				console.log("a");
				showAppSelection(true);
				getApplicationsData();
				
			});
		});




		function showMainContent(isShown){
			if(isShown == true){

				$("#maincontent").show();
				$("#appselection").hide();

				//$("#settingbutton").show();
				//$("#innerheader").show();
				showLoginView(false);
				$("#maincontent").load("maincontent.html",loadMainContentCallback); 	
					
			}
			else
			{
				$("#maincontent").hide();
				$("#innerheader").hide();
			}
		}

		var showLoginView = function(isShown){
			if(isShown == true){
				$("#loginview").show();
				$("#headerbutton").hide();
				showMainContent(false);
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
				showMainContent(false);
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
		        showMainContent(false);
		        showLoginView(true);

		        // dev
		        // showAppSelection(false);
		        // showMainContent(true);
		        // showLoginView(false);

		        console.log(result);
		    }
		}
		
		function getApplicationsData(){
			// var endPointURL = epURL+"manager/apps/all/"+$("table#list_global_apps_table").bootgrid("getRowCount");
			// console.log(endPointURL)
			// $("table#list_global_apps_table").bootgrid("destroy");
			// var appgrid = $("table#list_global_apps_table").bootgrid({
			// 	ajax: true,
			// 	ajaxSettings: {
			//         method: "GET",
			//         cache: false
			//     },
			//     url: useAuthentication(endPointURL),
			//     formatters: {
			//         "commands": function(column, row)
			//         {
			//             return "<button type=\"button\" class=\"btn btn-xs btn-default command-select\" data-row-id=\"" + row.id + "\"><span class=\"fa fa-pencil\">select</span></button> ";
			//         }
			//     }
			// }).on("loaded.rs.jquery.bootgrid", function()
			// {
			// 	currentAppId = Cookies.get('appid');
		 //    	if(currentAppId == null){
			// 	 	showAppSelection(true);				    		
		 //    	}
		 //    	else
		 //    	{
		 //    		// Here is the point where user get the main content
		 //    		showMainContent(true);
		 //    	}
			//     /* Executes after data is loaded and rendered */
			//     appgrid.find(".command-select").on("click", function(e)
			//     {	

			//     });
			// });
				client.sendRequest("GET",
	    			"games/apps/list/separated",
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
				    	if(currentAppId == null){
						 	showAppSelection(true);				    		
				    	}
				    	else
				    	{

				    		// Here is the point where user get the main content
				    		showMainContent(true);


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
    			"games/apps/data/"+selectedappid+"/"+memberId,
    			"",
    			"application/json",
    			{},
    			function(data,type){
    				// opened app is the selected app
    				if(selectedappid == currentAppId){
	    				Cookies.remove('appid');
	    				showMainContent(false);
	    				showAppSelection(true);
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
    			"games/apps/data/"+selectedappid,
    			"",
    			"application/json",
    			{},
    			function(data,type){
    				// opened app is the selected app
    				if(selectedappid == currentAppId){
	    				Cookies.remove('appid');
	    				showMainContent(false);
	    				showAppSelection(true);
	    				window.location;

						// Notification
						$.notify({
							// options
							message: selectedAppId + "ís deleted !"
						},{
							// settings
							type: 'success'
						});
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
			
			$("#alertdeleteapp").modal('toggle');	
		}

		function checkAndRegisterUserAgentCallback(data,type){
			getApplicationsData();		
		}

		function checkAndRegisterUserAgent(){
			client.sendRequest("POST",
	    			"games/validation",
	    			"",
	    			"application/json",
	    			{},
	    			checkAndRegisterUserAgentCallback,
	    			function(error) {
	    		        $('#appselection').before('<div class="alert alert-danger">Error connecting web services</div>');
	    		    }
	    		);
		}


		// Called when we want to reload the active tab
		function reloadActiveTab(){
			//reload active tab
			var $link = $('li.active a[data-toggle="tab"]');
		    $link.parent().removeClass('active');
		    var tabLink = $link.attr('href');
		    $('#gamemenutab a[href="' + tabLink + '"]').tab('show');
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
					"games/apps/data",
					formData,
					false,
					{},
					function(data, type){
						console.log(data);
						var selectedAppId = $("#createnewapp_appid").val();
						
						Cookies.set('appid', selectedAppId);
						showMainContent(true);
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
	    			"games/apps/data/"+currentAppId+"/"+memberId,
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
				showMainContent(true);

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

				// Notification
				$.notify({
					// options
					message: 'Welcome to <b>' + currentAppId + '</b> !'
				},{
					// settings
					type: 'success'
				});
				$("#alertregisteredapp").modal('toggle');

				showMainContent(true);
			});
			
			// $('#settingbutton').on('click', function(event) {
			// 	client.sendRequest("GET",
	  //   			"manager/apps",
	  //   			"",
	  //   			"application/json",
	  //   			{},
	  //   			function(data,type){
	  //   				$("#registeredappssettingstbody").empty();
			//     		for(var i = 0; i < data.length; i++){
			// 	    		var appData = data[i];
			// 	    		var newRow = "<tr><td id='appidid'>" + appData.id + "</td>";
			// 	    		newRow += "<td id='appnameid'>" + appData.appName + "</td>";
			// 				newRow += "<td id='appcommtypeid'>" + appData.commType + "</td>";
							
										    	
			// 	    		$("#list_global_apps_table tbody").append(newRow);
			// 	    	}

			// 	    	// Stay in Application Selection where there is no specified application selected
			// 	    	currentAppId = Cookies.get('appid');
			// 	    	if(currentAppId == null){
			// 			 	showAppSelection(true);				    		
			// 	    	}
			// 	    	else
			// 	    	{

			// 	    		// Here is the point where user get the main content
			// 	    		showMainContent(true);


			// 	    	}
	  //   			},
	  //   			function(error) {
	  //   		          // this is the error callback
	  //   		          console.log(error);
	  //   		        }
	  //   		);
			// });


			
//--------------------------- Badges -------------------------------//
			// // Buttons in Badge
			// $(".bupdclass").on('click', function(e){
   //          	e.preventDefault();
   //               var id = this.id;

			//      var bidclass = "#"+id+".bidclass";
			//      var bnameclass = "#"+id+".bnameclass";
			//      var bdescclass = "#"+id+".bdescclass";
			//      var bimgclass = "#"+id+".bimgclass";

			     
			//      // console.log(bidclass)
			//      // console.log(bnameclass)
			//      // console.log(bdescclass)
			//      // console.log(bimgclass)

			//      var badge_id = $(bidclass).text();
			//      var badge_name = $(bnameclass).text();
			//      var badge_desc = $(bdescclass).text();
			//      var badge_imgSrc = $(bimgclass).attr("name");


			//      // alert(badge_id);
			//      // alert(badge_name);
			//      // alert(badge_desc);
			//      // alert(badge_imgSrc);
			//      $("#updatebadgediv").find("#badge_id_name").attr("value",badge_id);
			//      $("#updatebadgediv").find("#badge_name").attr("value",badge_name);
			//      $("#updatebadgediv").find("#badge_desc").text(badge_desc);
			//      $("#updatebadgediv").find("#badgeimage").attr("src",badge_imgSrc);
			// });



//-------
		});

		$(document).ready(init());
		