
		var counter = 0;
		var iwcClient = null;
		var epURL = "http://localhost:8081/";
		var client = new TemplateServiceClient(epURL);

		var currentAppId = null;

			// Handler when the form in "Add New Badge" is submitted
		var badgeTabHandler = (function() {
						$("form#addbadgeform").submit(function(e){
				//disable the default form submission
				e.preventDefault();
				var formData = new FormData($(this)[0]);

				currentAppId = Cookies.get("appid");
				client.sendRequest(
					"POST",
					"gamification/badges/items/"+currentAppId,
					formData,
					false,
					{},
					function(data, type){
						console.log(data);
						$("#addbadgediv").modal('toggle');

						reloadActiveTab();

					    // Add alert before table
						$('#list_badges').before('<div class="alert alert-success"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>New Badge Successfully Added</div>');
						return false;
					},
					function(error) {
					      // this is the error callback
					      console.log(error);
					      					    // Add alert before table
						$('#list_badges').before('<div class="alert alert-danger"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>Badge Cannot Be Added</div>');
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
					client.sendRequest(
						"POST",
						"gamification/badges/items/"+currentAppId,
						formData,
						false,
						{},
						function(data, type){
							console.log(data);
							$("#addbadgediv").modal('toggle');

							reloadActiveTab();

						    // Add alert before table
							$('#list_badges').before('<div class="alert alert-success"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>New Badge Successfully Added</div>');
							return false;
						},
						function(error) {
						      // this is the error callback
						      					    // Add alert before table
							$('#list_badges').before('<div class="alert alert-danger"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>Badge Cannot Be Added</div>');
							return false;
						}
					);
					return false;
				});

		});
		// Dynamically load contents
		var mainContentHandler = (function() {
			$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
			  var target = $(e.target).attr("href") // activated tab
			  
			  	switch(target) {
				    case "#hometab":
				        break;
				    case "#pointtab":
				        break;
				    case "#badgetab":
				        getBadgesData();
				        break;
				    case "#achievementtab":
				        break;
				}
			});
			$("#badgetab").load("gamemanagerbadge.html",badgeTabHandler); 
		});

		function showMainContent(isShown){
			if(isShown == true){

				$(".maincontent").show();
				$("#appselection").hide();

				$("#settingbutton").show();
				$("#innerheader").show();
				$(".maincontent").load("gamemaincontent.html",loadMainContentCallback); 	
					
			}
			else
			{
				$(".maincontent").hide();
				$("#innerheader").hide();
			}
		}

		function loadMainContentCallback(){
			currentAppId = Cookies.get('appid');
			$("#apptitle").text("Application ID : " + currentAppId);
	
			mainContentHandler();	
			
			
		}

		var showAppSelection = function(isShown){
			if(isShown == true){
				$("#appselection").show();
				showMainContent(false);
				$("#settingbutton").show();
			}
			else
			{
				$("#appselection").hide();
			}
			
		};


		function signinCallback(result) {
		    if(result === "success"){
		        // after successful sign in, display a welcome string for the user
		        $("#status").html("Hello, " + oidc_userinfo.name + "!");
		        
		        console.log(oidc_userinfo)

		        checkAndRegisterUserAgent();

		    } else {
		        // if sign in was not successful, log the cause of the error on the console
		        showAppSelection(false);
		        showMainContent(false);
		        console.log(result);
		    }
		}
		
		function getAppicationsData(){
				client.sendRequest("GET",
	    			"gamification/apps",
	    			"",
	    			"application/json",
	    			{},
	    			function(data,type){
	    				$("#globalappstbody").empty();
			    		for(var i = 0; i < data.length; i++){
				    		var appData = data[i];
				    		var newRow = "<tr><td id='appidid'>" + appData.id + "</td>";
				    		newRow += "<td id='appnameid'>" + appData.appName + "</td>";
							newRow += "<td id='appcommtypeid'>" + appData.commType + "</td>";
										    	
				    		$("#list_global_apps_table tbody").append(newRow);
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
	    		          // this is the error callback
	    		          console.log(error);
	    		        }
	    		);
		}

		function checkAndRegisterUserAgentCallback(data,type){
			console.log(data);
			if(data.valid){
				getAppicationsData();
			}
			else{
				showAppSelection(false);
				$('#appselection').before('<div class="alert alert-warning"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>'+data.message+'</div>');
			}
	    			
		}

		function checkAndRegisterUserAgent(){
			client.sendRequest("GET",
	    			"gamification/validation",
	    			"",
	    			"application/json",
	    			{},
	    			checkAndRegisterUserAgentCallback,
	    			function(error) {
	    		        $('#appselection').before('<div class="alert alert-warning"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>Error connecting web services</div>');

	    		          console.log(error);
	    		    }
	    		);
		}
//--------------------------- Other -------------------------------//
		function renderIntent(intent){
			var result = "<table class='entry'>";
			
			
			var sender = intent.sender;
			var publisher = "me";
			var source = sender;
			
			if (sender.indexOf("?sender=") > -1){
				var ts = sender.split("?sender=");
				publisher = ts[0].split("/")[0];
				source = ts[1];
			} else {
				publisher = "<b><i>me</i></b>";
				source = sender;
			}
			
			result += "<tr><td class='ui-state-default'>Publisher</td><td class='ui-widget-content'>" + publisher + "</td></tr>";
			result += "<tr><td class='ui-state-default'>Source</td><td class='ui-widget-content'>" + source + "</td></tr>";
			
			
			
			var comp = intent.component;
			if(comp === ""){
				comp = "*";
			}
			
			result += "<tr><td class='ui-state-default'>Component</td><td class='ui-widget-content,value'>" + comp + "</td></tr>";
			result += "<tr><td class='ui-state-default'>Action</td><td class='ui-widget-content'>" + intent.action + "</td></tr>";
			result += "<tr><td class='ui-state-default'>Data</td><td class='ui-widget-content'>" + intent.data + "</td></tr>";
			result += "<tr><td class='ui-state-default'>Datatype</td><td class='ui-widget-content'>" + intent.dataType + "</td></tr>";
			result += "<tr><td class='ui-state-default'>Categories</td><td class='ui-widget-content'>" + intent.categories + "</td></tr>";
			result += "<tr><td class='ui-state-default'>Flags</td><td class='ui-widget-content'>" + intent.flags + "</td></tr>";
			result += "<tr><td class='ui-state-default'>Extras</td><td class='ui-widget-content'> " + JSON.stringify(intent.extras) + "</td></tr>";
			
			result += "</table>";
			
			return result;
		}
		
		function collectIntent(){
			var action = $("#pub_action").val();
			console.log("Action: " + action);
			
			var sender = $("#pub_source").val();
			if(!($("#pub_publisher").val() == 'me')){
				sender = $("#pub_publisher").val() + "?sender=" + sender;
			}
			
			var categories = $("#pub_categories").val().split(",");
			var flags = $("#pub_flags").val().split(",");
			
			try{
				var extras = $.parseJSON($("#pub_extras").val());
			} catch(error) {
				alert("Corrupt JSON specified for extras");
				return;
			}
			var intent = {
				"component": $("#pub_component").val(),
				"sender": sender,
				"data": $("#pub_data").val(), 
				"dataType":$("#pub_datatype").val(),
				"action":$("#pub_action").val(), 
				"categories":categories,
				"flags": flags,
				"extras": extras
			};
			return intent;
		}
		
		function init(){
			
			iwcClient = new iwc.Client();
		
			var iwcCallback = function(intent) {
				var date = new Date();
				var format = "yyyy-MM-dd kk:mm:ss";
				var dates = formatDate(date,format)+"."+date.getMilliseconds();
				$('#accordion').prepend("<h3><a href='#'>#" + counter + " - " + intent.action + " (" + dates + ")</a></h3><div>" + renderIntent(intent) + "</div>").accordion('destroy').accordion();
				counter += 1;
			}
		
			iwcClient.connect(iwcCallback);
			
		}

//--------------------------- Badges -------------------------------//		
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


    	function callbackBadges(data,type){
			$("#badgetbody").empty();
    		for(var i = 0; i < data.length; i++){
	    		var badge = data[i];
	    		var newRow = "<tr><td class='bidclass' id='"+i+"'>" + badge.id + "</td>";
	    		newRow += "<td class='bnameclass' id='"+i+"'>" + badge.name + "</td>";
				newRow += "<td class='bdescclass' id='"+i+"'>" + badge.description + "</td>";
				newRow += "<td><button id='" + i + "' type='button' onclick='viewBadgeImageHandler(this,0)' class='btn btn-info bimgclass' name='"+ badge.imagePath +"' data-toggle='modal' data-target='#modalimage'>View Image</button></td>";
				newRow += "<td>" + "<button id='" + i + "' type='button' onclick='updateBadgeHandler(this)' class='btn btn-warning bupdclass' data-toggle='modal' data-target='#updatebadgediv'>Update</button> ";
				newRow += "<button id='" + i + "' type='button' onclick='deleteBadgeHandler(this)' class='btn btn-danger bdelclass'>Delete</button></td>";
	    	
	    		$("#list_badges_table tbody").append(newRow);
	    	}
    	}

		// Handler when the Badges tab is selected. Retrieve all badges data
    	function getBadgesData(){
	    	client.sendRequest("GET",
	    			"gamification/badges/items/2",
	    			"",
	    			"application/json",
	    			{},
	    			callbackBadges,
	    			function(error) {
	    		          // this is the error callback
	    		        console.log(error);
	    		    }
	    		);
	    }

		// Handler when view image button in the row table for a badge is pressed
	    function viewBadgeImageHandler(element,code){

	    		//reset element
	    		$("#modalimageshow").attr('src','');
	    		$("#badgeimageinmodalupdate").attr('src','');
				var badgeid = element.parentNode.parentNode.getElementsByClassName("bidclass")[0].textContent
				console.log(badgeid);
				currentAppId = Cookies.get("appid");

				if(!client.isAnonymous()){
					console.log("Authenticated request");
					var rurl = epURL + "gamification/badges/items/"+currentAppId+"/" + badgeid + "/img";
					
					if(rurl.indexOf("\?") > 0){	
						rurl += "&access_token=" + window.localStorage["access_token"];
					} else {
						rurl += "?access_token=" + window.localStorage["access_token"];
					}
					if(code==0){
						$("#modalimageshow").attr('src',rurl);
					}
					if(code==1){
						$("#badgeimageinmodalupdate").attr('src',rurl);
					}
				} else {
					console.log("Anonymous request... ");
				}
				
		}

		// Handler when update button in the row table for a badge is pressed
		function updateBadgeHandler(element){

				// $(".bimgclass").on('click', function(e){
				//e.preventDefault();
				var badgeid = element.parentNode.parentNode.getElementsByClassName("bidclass")[0].textContent
				var badgename = element.parentNode.parentNode.getElementsByClassName("bnameclass")[0].textContent
			    var badgedesc = element.parentNode.parentNode.getElementsByClassName("bdescclass")[0].textContent
			    // var badge_imgSrc = $(bimgclass).attr("name");
				viewBadgeImageHandler(element,1);
				currentAppId = Cookies.get("appid");
				var path_URI = "badges/items/"+currentAppId+"/" + badgeid + "/img";
				$("#updatebadgediv").find("#badge_id_name").attr("value",badgeid);
			     $("#updatebadgediv").find("#badge_name").attr("value",badgename);
			     $("#updatebadgediv").find("#badge_desc").text(badgedesc);
			     $("#updatebadgediv").find("#badgeimageinmodal").attr("src",epURL+path_URI);
			// });
		}

		// Handler when delete button in the row table for a badge is pressed
		 function deleteBadgeHandler(element){

				// $(".bimgclass").on('click', function(e){
				//e.preventDefault();
				var badgeid = element.parentNode.parentNode.getElementsByClassName("bidclass")[0].textContent
				

				currentAppId = Cookies.get("appid");
				var path_URI = "gamification/badges/items/"+currentAppId+"/" + badgeid;
	    		client.sendRequest("DELETE",
	    			path_URI,
	    			"",
	    			false,
					{},
	    			function(data, type){
						console.log(data);
						reloadActiveTab();
					    // Add alert before table
						$('#list_badges').before('<div class="alert alert-success"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>Delete Success</div>');
						return false;
					},
	    			function(error) {
	    		          // this is the error callback
	    		          console.log(error);
	    		    }
	    		);
			// });
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

			$("#settingbutton").hide();
//--------------------------- Application selection -------------------------------//
			// Handler when the form in "Create New App" is submitted
			// App ID will be retrieved from the service and will be put on the id attribute in class maincontent
			$("form#createnewappform").submit(function(e){
				//disable the default form submission
				e.preventDefault();
				var formData = new FormData($(this)[0]);
				client.sendRequest(
					"POST",
					"gamification/apps",
					formData,
					false,
					{},
					function(data, type){
						console.log(data);
						var selectedAppId = $("#createnewapp_appid").val();
						
						Cookies.set('appid', selectedAppId);
						showMainContent(true);
						$('#innerheader').before('<div class="alert alert-success"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>New App Created!</div>');

						$("#createnewapp").modal('toggle');
						return false;
					},
					function(error) {
					      // this is the error callback
					      console.log(error);
					      					    // Add alert before table
						$('#appselection').before('<div class="alert alert-danger"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>New App Cannot Be Created</div>');
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
				$('#innerheader').before('<div class="alert alert-success"><a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>Welcome to '+currentAppId+' !</div>');
				$("#alertglobalapp").modal('toggle');
				showMainContent(true);
			});


			$('#backtoappselection').on('click', function(event) {
				//$(this).addClass('active').siblings().removeClass('active');
				//Get Value in appidid
				Cookies.remove('appid');
				showAppSelection(true);
			});
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
		