
var memberId;
var epURL = "http://localhost:8081/";
var client;


function useAuthentication(rurl){
            if(rurl.indexOf("\?") > 0){ 
                    rurl += "&access_token=" + window.localStorage["access_token"];
                } else {
                    rurl += "?access_token=" + window.localStorage["access_token"];
                }
                return rurl;
}
// (function($){

  // Backbone.sync = function(method, model, success, error){
  //   success();
  // }

// Model

  var PointLevelModel = Backbone.Model.extend({
    
    // defaults: {
    //     memberLevel:0,
    //     memberPoint:0,
    //     nextLevel:1,
    //     nextLevelPoint:1,
    //     point:0,
    //     progress:0,
    //     userName:"user"
    // }
  });

  var AchievementModel = Backbone.Model.extend({
    // defaults: {
    //   part1: 'hello',
    //   part2: 'world'
    // }
  });

  var AchievementsCollection = Backbone.Collection.extend({
    model: AchievementModel
  });

  var BadgeModel = Backbone.Model.extend({
    // defaults: {
    //   part1: 'hello',
    //   part2: 'world'
    // }
  });

  var QuestModel = Backbone.Model.extend({
    // defaults: {
    //   part1: 'hello',
    //   part2: 'world'
    // }
  });

  var QuestsCollection = Backbone.Collection.extend({
    model: QuestModel
  });
  
var Gamification = function(appId,memberId, useVisualization){
    this.memberId = memberId;
    this.appId = appId;
    this.url = "";
    this.serverUrl = "http://localhost:8081/";
    this.useAuthentication = function(rurl){
    if(rurl.indexOf("\?") > 0){ 
            rurl += "&access_token=" + window.localStorage["access_token"];
        } else {
            rurl += "?access_token=" + window.localStorage["access_token"];
        }
        return rurl;
    };
    this.badgeModel = new BadgeModel();    
    this.achievementModel = new AchievementModel();
    this.achievementsColl = new AchievementsCollection({model: this.achievementModel});
    this.questModel = new QuestModel();
    this.questsColl = new QuestsCollection();

    this.pointLevelModel = new PointLevelModel();
    this.pointLevelModel.url = useAuthentication(epURL+"games/levels/"+currentAppId+"/"+memberId);
    // this.pointLevelModel.fetch(
    //     {
    //     success: function(response,xhr) {
    //         console.log("Inside success");
    //         console.log(response);
    //     },
    //     error: function (errorResponse) {
    //         console.log(errorResponse)
    //     }
    // });
    if(useVisualization){
        this.mainStatusView = new MainStatusView({model:this.pointLevelModel});
        this.achievementView = new AchievementView({model:this.achievementModel});
        this.achievementGridView = new AchievementGridView({collection:this.achievementsColl});
    }
};  

Gamification.prototype.getApplicationId = function(){
    return this.appId;
}
Gamification.prototype.getMemberId = function(){
    return this.memberId;
}
Gamification.prototype.getServerURL = function(){
    return this.serverUrl;
}
Gamification.prototype.setApplicationId = function(appId){
    this.appId = appId;
}
Gamification.prototype.setMemberId = function(memberId){
    this.memberId = memberId;
}
Gamification.prototype.setServerURL = function(serverUrl){
    this.serverUrl = serverUrl;
}
Gamification.prototype.getGameElements = function(gameElement,option){
    var lowerCaseGameElement = gameElement.toLowerCase();
    var endPoint;
    if(!this.appId || !this.memberId || !gameElement || !option){
        console.log('application id value: ' + this.appId);
        console.log('member id value: ' + this.memberId);
        console.log('game element : ' + gameElement);
        console.log('option : ' + option);
        throw "Missing configuration";
        return false;
    }
    var splittedOption = option.split(" ");
    if(splittedOption.length > 2){
       throw "too much option argument";
    }
    switch(lowerCaseGameElement){
        case "badge":
            if(splittedOption[0] == "all"){
                endPoint = "games/badges/"+this.appId+"/"+this.memberId;
            }
            else{
                if(splittedOption.length == 1){
                    endPoint = "games/badges/"+this.appId+"/"+this.memberId;
                }
            }
        break;
        case "achievement":
            if(splittedOption[0] == "all"){
                endPoint = "games/achievements/"+this.appId+"/"+this.memberId;
                this.achievementsColl.url = useAuthentication(this.serverUrl + endPoint);
                this.achievementsColl.fetch({
                    success: function(response,xhr) {
                        console.log("Inside success");
                        console.log(response);
                    },
                    error: function (errorResponse) {
                        console.log(errorResponse)
                    }
                });
            }else{
                if(splittedOption[1]){

                    endPoint = "games/achievements/"+this.appId+"/"+this.memberId;
                }
            }
        break;
    }
    //this.url = this.useAuthentication(this.serverUrl + endPoint);
}

/*
  var Item = Backbone.Model.extend({
    defaults: {
      part1: 'hello',
      part2: 'world'
    }
  });

  var List = Backbone.Collection.extend({
    model: Item
  });

  var ItemView = Backbone.View.extend({
    tagName: 'li', // name of tag to be created        

    events: {
      'click span.swap':  'swap',
      'click span.delete': 'remove'
    },    

    initialize: function(){
      _.bindAll(this, 'render', 'unrender', 'swap', 'remove'); // every function that uses 'this' as the current object should be in here

      this.model.bind('change', this.render);
      this.model.bind('remove', this.unrender);
    },

    render: function(){
      $(this.el).html('<span style="color:black;">'+this.model.get('part1')+' '+this.model.get('part2')+'</span> &nbsp; &nbsp; <span class="swap" style="font-family:sans-serif; color:blue; cursor:pointer;">[swap]</span> <span class="delete" style="cursor:pointer; color:red; font-family:sans-serif;">[delete]</span>');
      return this; // for chainable calls, like .render().el
    },

    unrender: function(){
      $(this.el).remove();
    },
    swap: function(){
      var swapped = {
        part1: this.model.get('part2'),
        part2: this.model.get('part1')
      };
      this.model.set(swapped);
    },
    remove: function(){
      this.model.destroy();
    }
  });*/
  var MainStatusView = Backbone.View.extend({
    el: '#user-main-status', // el attaches to existing element
    events: {
      'refresh': 'updateView'
    },
    initialize: function(){
      _.bindAll(this, 'render', 'updateView'); // every function that uses 'this' as the current object should be in here
      //this.pointLevelModel = new PointLevelModel();
      this.model.bind('change', _.bind(this.render, this));
      
      this.render();
    },
    render: function(){
     this.$el.find('p#user-name').html(this.model.get('userName'));
     this.$el.find('#user-point').html(this.model.get('memberPoint'));
     this.$el.find('#user-current-level').html('Level ' + this.model.get('memberLevel'));
     this.$el.find('#user-level-progress').prop('aria-valuenow',this.model.get('progress')).html(this.model.get('progress')+'%');
     this.$el.find('#user-next-level').html('Level' + this.model.get('nextLevel'));
     _.each(this.model,function(achModel){

     }, this)
    },
    updateView: function(){
      // this.counter++;
      // var item = new Item();
      // item.set({
      //   part2: item.get('part2') + this.counter // modify item defaults
      // });
      // this.collection.add(item);
    }
  });

    var AchievementView = Backbone.View.extend({
    tagName: 'div', // name of tag to be created        
    className: 'col-lg-3 col-md-4 col-xs-6 thumb',
    events: {
      // 'click span.swap':  'swap',
      // 'click span.delete': 'remove',
      'mouseover': 'mouseOverFunc'
    },    

    initialize: function(){
      _.bindAll(this, 'render', 'mouseOverFunc');//, 'unrender', 'swap', 'remove'); // every function that uses 'this' as the current object should be in here

      this.model.bind('change', _.bind(this.render, this));
      //this.model.bind('change', this.render);
    },

    render: function(){
      $(this.el).html('<span style="color:black;">'+this.model.get('part1')+' '+this.model.get('part2')+'</span> &nbsp; &nbsp; <span class="swap" style="font-family:sans-serif; color:blue; cursor:pointer;">[swap]</span> <span class="delete" style="cursor:pointer; color:red; font-family:sans-serif;">[delete]</span>');
      return this; // for chainable calls, like .render().el
    },

    // unrender: function(){
    //   $(this.el).remove();
    // },
    // swap: function(){
    //   var swapped = {
    //     part1: this.model.get('part2'),
    //     part2: this.model.get('part1')
    //   };
    //   this.model.set(swapped);
    // },
    // remove: function(){
    //   this.model.destroy();
    // },
    mouseOverFunc: function(){
        console.log("hello");
    }
    });

  var AchievementGridView = Backbone.View.extend({
    el: '#achievement-row-container', // el attaches to existing element
    events: {
      'refresh': 'updateView'
    },
    initialize: function(){
      _.bindAll(this, 'render', 'appendItem'); // every function that uses 'this' as the current object should be in here
      //this.pointLevelModel = new PointLevelModel();
      this.collection.bind('change', _.bind(this.render, this));
      
      this.render();
    },
    render: function(){
        this.$el.append("<button id='add'>Add list item</button>");
        this.$el.append("<ul></ul>");
        _(this.collection.models).each(function(item){ // in case collection is not empty
            this.appendItem(item);
        }, this);
    },
    appendItem: function(item){
      var achievementView = new AchievementView({
        model: item
      });
      //$('ul', this.el).append(itemView.render().el);
    },
    updateView: function(){

    }
  });

// })(jQuery);


function main(){

console.log(currentAppId);
console.log(memberId);
memberId = "mabdh";
  var game = new Gamification(currentAppId, memberId, true);
  game.getGameElements("achievement","all");

}




function signinCallback(result) {
    if(result === "success"){
        
        memberId = oidc_userinfo.preferred_username;
        // after successful sign in, display a welcome string for the user
        $("#status").html("Hello, " + memberId + "!");
        client = new TemplateServiceClient(epURL);

        main();
    } else {
        // if sign in was not successful, log the cause of the error on the console
         
        main();
        console.log(result);
    }
    // if(result === "success"){
    //     $('#container-text').prop('hidden',true);
    //     $('#vis_container').prop('hidden',false)
    // }else{
    //     $('#container-text').prop('hidden',false);
    //     $('#vis_container').prop('hidden',true);
    // }

}


var MemberDAO = function(){

}

window.localStorage['app_id'] = 'test';

currentAppId = window.localStorage["app_id"];

function getLevel(){
var endPointURL = "games/quests/"+currentAppId+"/"+memberId+"/status/REVEALED";
ClientHelper.getData(
    endPointURL,
    function(data,type){
        console.log(data);
    },
    function(status,error){
    }
    );
}

MemberDAO.prototype.getBadges = function(tableElement, objectFormatters, loadedHandler,callbackResponse){
    currentAppId = window.localStorage["app_id"];
    var endPointURL = "games/badges/"+currentAppId;
    ClientHelper.getDataToTable(endPointURL, tableElement, objectFormatters, loadedHandler, callbackResponse);
}

MemberDAO.prototype.getLevel = function(successCallback, errorCallback){
    currentAppId = window.localStorage["app_id"];

    var endPointURL = "games/levels/"+currentAppId+"/"+memberId;
    ClientHelper.getData(
        endPointURL,
        function(data,type){
            console.log(data);
            successCallback(data,type);
        },
        function(status,error){
            errorCallback(status,error);
        }
        );
}

var ClientHelper = {
     getData : function(endPointURL, successCallback, errorCallback){
        client.sendRequest(
            "GET",
            endPointURL,
            "",
            "application/json",
            {},
            function(data, type){
                
                successCallback(data,type);
                
                return false;
            },
            function(status,error) {
                errorCallback(status,error);
                return false;
            }
        );
    }

};


$(document).ready(function() {

    // Add drag and resize option to panel
    $("#toolbox-tools").draggable({
        handle: ".panel-heading",
        stop: function(evt, el) {
            // Save size and position in cookie
            /*
            $.cookie($(evt.target).attr("id"), JSON.stringify({
                "el": $(evt.target).attr("id"),
                "left": el.position.left,
                "top": el.position.top,
                "width": $(evt.target).width(),
                "height": $(evt.target).height()
            }));
            */
        }
    }).resizable({
        handles: "e, w, s, se",
        stop: function(evt, el) {
            // Save size and position in cookie
            
            $.cookie($(evt.target).attr("id"), JSON.stringify({
                "el": $(evt.target).attr("id"),
                "left": el.position.left,
                "top": el.position.top,
                "width": el.size.width,
                "height": el.size.height
            }));
            
        }
    });
    
    
    // Expand and collaps the toolbar
    $("#toggle-toolbox-tools").on("click", function() {
        var panel = $("#toolbox-tools");
        
        if ($(panel).data("org-height") == undefined) {
            $(panel).data("org-height", $(panel).css("height"));
            $(panel).data("org-width", $(panel).css("width"));
            $(panel).css("height","41px");
            $(panel).css("width","150px")
        } else {
            $(panel).css("height", $(panel).data("org-height"));
            $(panel).css("width", $(panel).data("org-width"));
            $(panel).removeData("org-height");
            $(panel).removeData("org-width");
        }
                
        $(this).toggleClass('fa-chevron-down').toggleClass('fa-chevron-right');
    });


    // Make toolbar groups sortable
/*    $( "#sortable" ).sortable({
        stop: function (event, ui) {
            var ids = [];
            $.each($(".draggable-group"), function(idx, grp) {
                ids.push($(grp).attr("id"));
            });
            
            // Save order of groups in cookie
            //$.cookie("group_order", ids.join());
        }
    });
    $( "#sortable" ).disableSelection();*/


  /*  // Make Tools panel group minimizable
    $.each($(".draggable-group"), function(idx, grp) {
        var tb = $(grp).find(".toggle-button-group");

        $(tb).on("click", function() {
            $(grp).toggleClass("minimized");
            $(this).toggleClass("fa-caret-down").toggleClass("fa-caret-up");

            // Save draggable groups to cookie (frue = Minimized, false = Not Minimized)
            var ids = [];
            $.each($(".draggable-group"), function(iidx, igrp) {
                var itb = $(igrp).find(".toggle-button-group");
                var min = $(igrp).hasClass("minimized");

                ids.push($(igrp).attr("id") + "=" + min);
            });

            $.cookie("group_order", ids.join());
        });
    });*/


    // Close thr panel
    $(".close-panel").on("click", function() {
        $(this).parent().parent().hide();
    });

    
    // Add Tooltips
    $('button').tooltip();
    $('.toggle-button-group').tooltip();


    var singInFunction = $('.oidc-signin').data('callback');
    console.log(singInFunction);


    
});
