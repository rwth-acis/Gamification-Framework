
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
		         questModule.init();

		    } else {

		        console.log(result);

		        $('#loginview').show();

		        $('#page-wrapper').hide();
		    }

		}
		$(document).ready(init());
		