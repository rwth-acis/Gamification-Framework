/*
Copyright (c) 2014 Dominik Renzel, Advanced Community Information Systems (ACIS) Group, 
Chair of Computer Science 5 (Databases & Information Systems), RWTH Aachen University, Germany
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the ACIS Group nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
* OpenID Connect Button
* 
* This library realizes An OpenID Connect Button allowing arbitrary browser-based 
* Web applications to authenticate and get access to user information using an 
* external OpenID Connect Provider. The application itself must be registered as 
* client at the OpenID Connect provider. In ./index.html we demonstrate the use
* of the OpenID Connect Button. Developers are advised to follow the included 
* documentation until a full tutorial becomes available.
*/

// Small modifications by Jukka Purma, to make button better fit limited space 
// in navbar of LayersToolTemplate

// Definition of variables relevant to OpenID Connect
// These variables are available to developers for an easy and convenient 
// access to OpenID Connect related information.
 
var oidc_server; // OpenID Connect Provider URL
var oidc_name; // OpenID Connect Provider Name
var oidc_logo; // OpenID Connect Provider Logo URL
var oidc_clientid; // OpenID Connect Client ID
var oidc_scope; // OpenID Connect Scope
var oidc_callback; // OpenID Connect Redirect Callback
var oidc_provider_config; // OpenID Connect Provider Configuration
var oidc_userinfo; // OpenID Connect User Info
var oidc_idtoken; // OpenID Connect ID Token (human-readable)

var nofill; // if true, don't use 'success' style for logged-in button.

var google_server = "https://accounts.google.com/o/oauth2";
var learning_layers_server = "https://api.learning-layers.eu/o/oauth2";

// Exceptions and debug messages are logged to the console.
try{
	
	(function() {
		learningLayerLogin();
	})();
} catch (e){
	console.log(e);
}

function learningLayerLogin(){
	// Learning layer server
	oidc_server = learning_layers_server;

	// with all necessary fields defined, retrieve OpenID Connect Server configuration
	getProviderConfig(oidc_server,function(c){
		if(c === "error"){
			throw("Warning: could not retrieve OpenID Connect server configuration!"); 
		} else {
			oidc_provider_config = c;
			
			// after successful retrieval of server configuration, check auth status

			try{
				checkAuth(function(){
					console.log("Access token fetched. Getting userinfo...");

					getUserInfo(function(u){
						if(u["sub"]){
							oidc_userinfo = u;
							signInCallback("success");
						} else {
							signInCallback("Error: could not retrieve user info! Cause: " + u.error_description);
							console.log("Refreshing token...");

							registerLearningLayer(function(resp,status){
						    	console.log("New token...");
						    	
						    },function(resp,status){
						    	console.log("failed");
						    });
														
						}
					});
				});
			}catch(e){
				console.log("WARNING: " + e);
			}
		}
	});
}

function googleLogin(){
	oidc_server = google_server;
	getProviderConfig(oidc_server,function(c){
		if(c === "error"){
			throw("Warning: could not retrieve OpenID Connect server configuration!"); 
		} else {
			oidc_provider_config = c;
			
			// after successful retrieval of server configuration, check auth status

			try{
				getUserInfo(function(u){
					if(u["sub"]){
						oidc_userinfo = u;
						signInCallback("success");
					} else {
						signInCallback("Error: could not retrieve user info! Cause: " + u.error_description);
					}
				});
			}catch(e){
				console.log("WARNING: " + e);
			}
		}
	});
}


function registerLearningLayer(successCallback,errorCallback){

	///o/oauth2/request?discovery=https://api.learning-layers.eu/o/oauth2/.well-known/openid-configuration&client_id=242c4084-353a-485d-9d3c-80f55ad73e20&client_secret=ALmWqWqzWzYSVvDlLIrIzVXdmxeCgwUjoayzT4kHA3PkQGkZqPisg_t5GZI8A1-_02eX3l1G_FQwhQDfh4OKPFk&return=${returnUriEnc}">
	openapp.resource.get(openapp.param.space(), (function(space){
		openapp.resource.context(space).metadata().get((function(metadata) {
			var properties = openapp.resource.content(metadata).properties();
			var spacename = properties["http://purl.org/dc/terms/title"];
			var returnURL = encodeURI("/spaces/"+spacename);
			var oauth2Endpoint = "/o/oauth2/request?discovery=https://api.learning-layers.eu/o/oauth2/.well-known/openid-configuration&client_id=242c4084-353a-485d-9d3c-80f55ad73e20&client_secret=ALmWqWqzWzYSVvDlLIrIzVXdmxeCgwUjoayzT4kHA3PkQGkZqPisg_t5GZI8A1-_02eX3l1G_FQwhQDfh4OKPFk&return=" + returnURL;
			
		}));

	}));
}

/**
* asynchronously retrieves OpenID Connect provider config according to the OpenID Connect Discovery specification
* (cf. http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationRequest).
*
* * @param cb function(obj) callback function retrieving provider config or an error message in case retrieval failed
**/
function getProviderConfig(provider,cb){
	$.ajax(
		provider + '/.well-known/openid-configuration',
	  {
		type: 'GET',
		dataType: 'json',
		crossdomain: true,
		complete: function (resp,status) {
			cb(resp.responseJSON);
		},
		error: function (resp, status) {
			cb(status);
		}
	  }
	);
}

/**
* asynchronously retrieves OpenID Connect user info according to the OpenID Connect specification
* (cf. http://openid.net/specs/openid-connect-core-1_0.html#UserInfo). Requires the availability of a valid
* OpenID Connect access token in the browser's local storage ("access_token").
*
* @param cb function(obj) callback function retrieving user info or an error message in case retrieval failed
**/	
function getUserInfo(cb){
	$.ajax(
		oidc_provider_config.userinfo_endpoint,
	  {
		type: 'GET',
		dataType: 'json',
		beforeSend: function (xhr) {
		  xhr.setRequestHeader("Authorization", "Bearer " + window.localStorage["access_token"]);
		  if(oidc_server == google_server){
		 		xhr.setRequestHeader("Access-Control-Allow-Origin","*");
		  	}
		},
		success: function (userinfo) {
			cb(userinfo);
		},
		error: function (resp) {
			delete window.localStorage["access_token"];
			cb(resp.responseJSON);
		}
	  }
	);
}
/**
* parses OpenID Connect ID token into human-readable JWS according to the OpenID Connect specification
* (cf. http://openid.net/specs/openid-connect-core-1_0.html#IDToken). Requires the availability of a hashed 
* OpenID Connect ID token in the browser's local storage ("id_token"). Token validity is not checked.
**/
function getIdToken() {
	
	if(!KJUR.jws) {
		throw("Cannot parse OpenID Connect ID token! KJUR.jws not available!");
	} else {
		var jws = new KJUR.jws.JWS();
		var result = 0;
		try {
			result = jws.parseJWS(window.localStorage["id_token"]);
		} catch (ex) {
			//console.log("Warning: " + ex);
		}

		return jws.parsedJWS;
	}
}

function getAccessTokenFromROLE(){
	var access_token;
	var userRes = new openapp.oo.Resource(openapp.param.user());

	openapp.resource.get(openapp.param.user(), (function(user){
	    var userinfo = openapp.resource.content(user).properties();//.type(openapp.ns.foaf+"openid");
	    access_token = userinfo[openapp.ns.foaf+"openid"];

	}));
	return access_token;
}

/**
* checks for the availability of OpenID Connect tokens (access token and ID token). 
* Returns true, if both tokens are available from the browser's local storage ("access_token" and "id_token").
* Token validity is not checked.
**/		
function checkAuth(successCallback){
	var userRes = new openapp.oo.Resource(openapp.param.user());

	openapp.resource.get(openapp.param.user(), (function(user){
	    var userinfo = openapp.resource.content(user).properties();//.type(openapp.ns.foaf+"openid");
	    openapp.resource.context(user).content(openapp.ns.foaf+"openid").get(function(res){
		    console.log(JSON.stringify(res));
	    });
	    console.log(JSON.stringify(user.data));
	    var access_token = userinfo[openapp.ns.foaf+"openid"];
	    if(access_token){
	    	window.localStorage["access_token"] = access_token;
	    	successCallback();
	    }else{
	    	throw "Error cannot authenticate. Access token is missing";
	    }
	}));
}

