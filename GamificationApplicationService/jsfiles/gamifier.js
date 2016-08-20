  var memberId, appId = '$Application_Id$',epURL = '$Endpoint_URL$';

  var useAuthentication = function(rurl){
    if(rurl.indexOf("\?") > 0){ 
      rurl += "&access_token=" + window.localStorage["access_token"];
    } else {
      rurl += "?access_token=" + window.localStorage["access_token"];
    }
    return rurl;
  }

  var advice = function(actionId) {
    $.post(
      useAuthentication(epURL + 'visualization/actions/' + appId + '/' + actionId + '/' + memberId), 
      ''
      ).done(function() {
        console.log('Trigger success : ' + actionId)
        alert( "second success" );
      })
      .fail(function() {
        alert( "Trigger failed " + actionId );
      });
  };


  var initGamification = function() {
    $AOP_Script$
  };

  function signInCallback(result) {
    if (result === 'success') {
      memberId = oidc_userinfo.preferred_username;
      console.log(oidc_userinfo);
      console.log('Logged in!');
      initGamification();
    } else {
      console.log(result);
      console.log(window.localStorage['access_token']);
      alert("Failed to log in using Open ID connect. Cannot use gamification feature.");
    }
  }

  $(document).ready(function() {
  });