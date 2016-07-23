
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

function getBadgeImage(badgeId){
    var endPoint = epURL + "games/badges/" + currentAppId + "/" + memberId + "/" + badgeId + "/img";
    return useAuthentication(endPoint);
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
    defaults: {
        id:"badgeid",
        name:"badge name",
        description:"badge description",
        pointValue:34,
        badgeId:"badge2"
    }
  });

  var AchievementsCollection = Backbone.Collection.extend({
    model: AchievementModel
  });

  var BadgeModel = Backbone.Model.extend({
    defaults:{
        id:"badgeid",
        name:"badge name",
        description:"badge description",
        imagePath:"badge image path"
        },
    parse: function(resp, xhr) {
        resp.imagePath = useAuthentication(resp.imagePath);
        return resp;
    }

  });
  var BadgesCollection = Backbone.Collection.extend({
    model: BadgeModel
  });
  
    var QuestProgressModel = Backbone.Model.extend({
    // defaults: {
    //    id:"questid",
    //     name:"quest name",
    //     description:"quest description",
    //     pointValue:34,
    //     achievementId:"achievementid",
    //     status:"COMPLETED"
    // }
        fetchWithAuthentication:function(successCallback,errorCallback){
            this.fetch({
                data: {access_token: window.localStorage["access_token"]},
                success:function(model,response){
                    successCallback(model,response);
                },
                error:function(model,e){
                    errorCallback(model,e);
                }
            });
    }
    });

  var QuestModel = Backbone.Model.extend({
    // defaults: {
    //    id:"questid",
    //     name:"quest name",
    //     description:"quest description",
    //     pointValue:34,
    //     achievementId:"achievementid",
    //     status:"COMPLETED"
    // }
  });

  var QuestsCollection = Backbone.Collection.extend({
    model: QuestModel
    // ,
    // parse: function(response,xhr){
    //     console.log(response);
    //     var parsed = _.groupBy(response,'status');
    //     console.log(parsed);
    //     return parsed;
    // }
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
    this.questModel = new QuestModel();

    this.badgesColl = new BadgesCollection({model: this.badgeModel});
    this.achievementsColl = new AchievementsCollection({model: this.achievementModel});
    this.questsColl = new QuestsCollection({model: this.questModel});

    this.pointLevelModel = new PointLevelModel();
    if(useVisualization){
        this.mainStatusView = new MainStatusView({model:this.pointLevelModel});
        this.achievementView = new AchievementView({model:this.achievementModel});
        this.achievementGridView = new AchievementGridView({collection:this.achievementsColl,
                                                            ModelView:AchievementView});
        this.badgeGridView = new BadgeGridView({collection:this.badgesColl,
                                                            ModelView:BadgeView});
        this.questGridView = new QuestGridView({collection:this.questsColl,
                                                            QuestCompletedView:QuestCompletedView,
                                                            QuestRevealedView:QuestRevealedView});
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
Gamification.prototype.getGameElements = function(gameElement,option, successCallback, errorCallback){
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
                this.badgesColl.url = useAuthentication(this.serverUrl + endPoint);
                this.badgesColl.fetch({
                    success: function(response,xhr) {
                        successCallback(response,xhr);
                    },
                    error: function (errorResponse) {
                        errorCallback(errorResponse);
                    }
                });
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
                        successCallback(response,xhr);
                    },
                    error: function (errorResponse) {
                        errorCallback(errorResponse);

                    }
                });
            }else{
                if(splittedOption[1]){

                    endPoint = "games/achievements/"+this.appId+"/"+this.memberId;
                }
            }
        break;
        case "quest":
            if(splittedOption[0] == "all"){
                endPoint = "games/quests/"+this.appId+"/"+this.memberId;
                this.questsColl.url = useAuthentication(this.serverUrl + endPoint +"/status/ALL");
                this.questsColl.fetch({
                    success: function(response,xhr) {
                        console.log("Inside success");
                        console.log(xhr);
                        successCallback(response,xhr);
                    },
                    error: function (errorResponse) {
                        console.log(errorResponse);
                        errorCallback(errorResponse);

                    }
                });

            }else{
                if(splittedOption[1]){

                    endPoint = "games/achievements/"+this.appId+"/"+this.memberId;
                }
            }
        break;
        case "level":
            endPoint = "games/levels/"+currentAppId+"/"+memberId;
            this.pointLevelModel.url = useAuthentication(this.serverUrl + endPoint);
            this.pointLevelModel.fetch({
                    success: function(response,xhr) {
                        successCallback(response,xhr);
                    },
                    error: function (errorResponse) {
                        errorCallback(errorResponse);
                        
                    }
                });
        break;
    }
}

var MainStatusView = Backbone.View.extend({
    el: '#user-main-status', // el attaches to existing element
    events: {
      'refresh': 'updateView'
    },
    initialize: function(){
      _.bindAll(this, 'render', 'updateView'); // every function that uses 'this' as the current object should be in here
      //this.pointLevelModel = new PointLevelModel();
      this.model.bind('reset add remove change', _.bind(this.render, this));
      
      this.render();
    },
    render: function(){
     this.$el.find('p#user-name').html(this.model.get('userName'));
     this.$el.find('#user-point').html(this.model.get('memberPoint'));
     this.$el.find('#user-current-level').html('Level ' + this.model.get('memberLevel'));

     this.$el.find('#user-level-progress').css('width',this.model.get('progress')+"%");
     this.$el.find('#user-level-progress').attr('aria-valuenow',this.model.get('progress'));
     this.$el.find('#user-level-progress').html(this.model.get('progress')+'%');

     this.$el.find('#user-next-level').html((this.model.get('nextLevel') ? 'Level ' + this.model.get('nextLevel') : "-"));
     _.each(this.model,function(achModel){

     }, this)
    },
    updateView: function(){
    }
  });

var BadgeView = Backbone.View.extend({
    tagName: 'div', // name of tag to be created        
    className: 'col-lg-3 col-md-4 col-xs-6 thumb',
    events: {
      'click' : 'clickFunction'
    },    

    initialize: function(){
        _.bindAll(this, 'render', 'clickFunction');
        this.model.bind('reset add change', _.bind(this.render, this));
    },

    render: function(){
        var tmpl = _.template($("#badgeTemplate").html()); 
        this.$el.html(tmpl(this.model.toJSON()));
        return this;
    },
    clickFunction: function(){
        //show modal
        var tmpl = _.template($("#badgeModal").html());
        $('#badgeimagemodal').find('.modal-body').html(tmpl(this.model.toJSON()));
        $('#badgeimagemodal').modal('toggle');
    }
    });

  var BadgeGridView = Backbone.View.extend({
    el: '#badge-row-container',
    events: {
      'refresh': 'renderBadge'
    },
    initialize: function(attrs){
        _.bindAll(this, 'render', 'renderBadge');
        this.collection.bind('reset add change', _.bind(this.render, this));
        this.options = attrs;
        this.render();
    },
    render: function(){
        this.$el.empty();
        var that = this;

        _.each(this.collection.models, function (item) {
            that.renderBadge(item);
        });
    },
    renderBadge:function (item) {
        var badgeView = new this.options.ModelView({
            model:item
        });
        this.$el.append(badgeView.render().el);
    }
  });

var AchievementView = Backbone.View.extend({
    tagName: 'div', // name of tag to be created        
    className: 'panel panel-default',
    events: {
      // 'click span.swap':  'swap',
      // 'click span.delete': 'remove',
      'mouseover': 'mouseOverFunc'
    },    

    initialize: function(){
      _.bindAll(this, 'render', 'mouseOverFunc');//, 'unrender', 'swap', 'remove'); // every function that uses 'this' as the current object should be in here

      this.model.bind('reset add change', _.bind(this.render, this));
    },

    render: function(){
        var tmpl = _.template($("#achievementTemplate").html());
        console.log(this.model)
        this.$el.html(tmpl(this.model.toJSON()));
        return this;
    },
    mouseOverFunc: function(){
        console.log("hello");
    }
    });

  var AchievementGridView = Backbone.View.extend({
    el: '.achievement-row-container', // el attaches to existing element
    events: {
      'refresh': 'renderAchievement'
    },
    initialize: function(attrs){
      _.bindAll(this, 'render', 'renderAchievement'); // every function that uses 'this' as the current object should be in here
      this.collection.bind('reset add change', _.bind(this.render, this));
      this.options = attrs;
      this.render();
    },
    render: function(){
        this.$el.empty();
        var that = this;
        console.log(this.collection.models);

        _.each(this.collection.models, function (item) {
            that.renderAchievement(item);
        });
    },
    renderAchievement:function (item) {
        var achievementView = new this.options.ModelView({
            model:item
        });
        this.$el.append(achievementView.render().el);
    }
  });

var QuestCompletedView = Backbone.View.extend({
    tagName: 'div', // name of tag to be created        
    className: 'panel panel-default',
    events: {
      // 'click span.swap':  'swap',
      // 'click span.delete': 'remove',
    },    

    initialize: function(){
      _.bindAll(this, 'render');//, 'unrender', 'swap', 'remove'); // every function that uses 'this' as the current object should be in here

      this.model.bind('reset add change', _.bind(this.render, this));
    },

    render: function(){
        var tmpl = _.template($("#questCompletedTemplate").html());
        console.log(this.model)
        this.$el.html(tmpl(this.model.toJSON()));
        this.$el.prop('id',this.model.get("id"));
        return this;
    }
    });

var QuestRevealedView = Backbone.View.extend({
    tagName: 'div', // name of tag to be created        
    className: 'panel panel-default',
    events: {
      // 'click span.swap':  'swap',
      // 'click span.delete': 'remove',
    },    

    initialize: function(){
      _.bindAll(this, 'render','renderProgress');//, 'unrender', 'swap', 'remove'); // every function that uses 'this' as the current object should be in here
        this.progressModel = new QuestProgressModel({id: this.model.get("id")});
        this.progressModel.bind('reset add change', this.renderProgress);
        this.progressModel.urlRoot = epURL + "games/quests/"+ currentAppId + "/" + memberId + "/progress";
        console.log(this.model);
        this.progressModel.fetchWithAuthentication(function(model,response){
            console.log(response);
            
        },function(model,e){});

      this.model.bind('reset add change', _.bind(this.render, this));
    },

    render: function(){
        var questId = this.model.get("id");
        var tmpl = _.template($("#questRevealedTemplate").html());
        console.log(this.model)
        this.$el.html(tmpl(this.model.toJSON()));
        this.$el.prop('id',questId);

        //this.model.urlRoot = useAuthentication(epURL + "games/quests/"+ currentAppId + "/" + memberId + "/progress");
        
        // function(resp,r){
        //     console.log(r);
        // });

        return this;
    },
    renderProgress:function(){
        var tmpl = _.template($("#questProgressTemplate").html());
        var questViewNode = this.$el.find('.panel-body');
        console.log($(questViewNode));
        $(questViewNode).append(tmpl(this.progressModel.toJSON()));
    }
    });

  var QuestGridView = Backbone.View.extend({
    el: '#quest-row-container', // el attaches to existing element
    events: {
    },
    initialize: function(attrs){
      _.bindAll(this, 'render', 'renderCompletedQuest', 'renderRevealedQuest'); // every function that uses 'this' as the current object should be in here
      this.collection.bind('reset add change', _.bind(this.render, this));
      this.options = attrs;
      this.render();
    },
    render: function(){
        console.log("Render Quest Grid");

        console.log(this.collection.models);
         var groupByStatus = this.collection.where({status: 'COMPLETED'});
        

        var completedModels = this.collection.where({status: 'COMPLETED'});
        var revealedModels = this.collection.where({status: 'REVEALED'});
        console.log(revealedModels);
        // var collectparsed = _.groupBy(this.collection,'status');
        // console.log(collectparsed);
        this.$el.find('#quest-revealed').empty();
        this.$el.find('#quest-completed').empty();
        var that = this;

        _.each(completedModels, function (item) {
            console.log(item);
            that.renderCompletedQuest(item);
        });

        _.each(revealedModels, function (item) {
            that.renderRevealedQuest(item);
        });
    },
    renderCompletedQuest:function (item) {
        var questView = new this.options.QuestCompletedView({
            model:item
        });
        this.$el.find('#quest-completed').append(questView.render().el);
    },
    renderRevealedQuest:function (item) {
        var questView = new this.options.QuestRevealedView({
            model:item
        });
        this.$el.find('#quest-revealed').append(questView.render().el);
    }
  });
// })(jQuery);


var localLeaderboard = function(tableElement,objectFormatters, loadedHandler,callbackResponse){ 
    var endPoint = epURL + "games/leaderboard/local/" + currentAppId + "/" + memberId;
    var objectgrid = $(tableElement).bootgrid({
            ajax: true,
            ajaxSettings: {
                method: "GET",
                cache: false
            },
            searchSettings: {
                delay: 100,
                characters: 3
            },
            url: useAuthentication(endPoint),
            formatters: objectFormatters,
            responseHandler: function(response){
                callbackResponse(response);
                return response;
            }
        }).on("loaded.rs.jquery.bootgrid", function()
        {
            /* Executes after data is loaded and rendered */
            loadedHandler(objectgrid);
            
        });
}

function main(){

    currentAppId = "test";
    memberId = "mabdh";
    console.log(currentAppId);
    console.log(memberId);

    var game = new Gamification(currentAppId, memberId, true);

        /*$.when(
            game.getGameElements("badge","all"),
            game.getGameElements("achievement","all"),
            game.getGameElements("level","all")
        ).then(function(){
        });*/

        game.getGameElements("badge","all", function(response,xhr){

            game.getGameElements("achievement","all",function(response,xhr){
                game.getGameElements("quest","all",function(response,xhr){
                    game.getGameElements("level","all", function(response,xhr){
                         localLeaderboard(
                            $('table#list_leaderboard'),
                            {},
                            function(objectgrid){},
                            function(response){console.log(response.rows)}
                            );
                    }, function(err){});
                }, function(err){});
            }, function(err){});
        }, function(err){});



    // var badges = game.getGameElements("badge","all");
    // console.log(badges);
    // var achievement = game.getGameElements("achievement","all");
    // console.log(achievement);

    // var level = game.getGameElements("level","all");
    // console.log(level);

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
    if(result === "success"){
        $('#container-text').prop('hidden',true);
        $('#vis_container').prop('hidden',false)
    }else{
        $('#container-text').prop('hidden',false);
        $('#vis_container').prop('hidden',true);
    }

}

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
            $(panel).css("width","200px")
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
