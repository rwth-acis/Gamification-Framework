//$(document).ready(function() {

var vis = function(){
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
            /*
            $.cookie($(evt.target).attr("id"), JSON.stringify({
                "el": $(evt.target).attr("id"),
                "left": el.position.left,
                "top": el.position.top,
                "width": el.size.width,
                "height": el.size.height
            }));
            */
        }
    });	
	
    
    // Expand and collaps the toolbar
    $("#toggle-toolbox-tools").on("click", function() {
        var panel = $("#toolbox-tools");
    	
    	if ($(panel).data("org-height") == undefined) {
    		$(panel).data("org-height", $(panel).css("height"));
    		$(panel).css("height","41px");
    	} else {
    		$(panel).css("height", $(panel).data("org-height"));
    		$(panel).removeData("org-height");
    	}
    	    	
    	$(this).toggleClass('fa-chevron-down').toggleClass('fa-chevron-right');
    });


    // Make toolbar groups sortable
    $( "#sortable" ).sortable({
		stop: function (event, ui) {
            var ids = [];
			$.each($(".draggable-group"), function(idx, grp) {
				ids.push($(grp).attr("id"));
			});
			
            // Save order of groups in cookie
			//$.cookie("group_order", ids.join());
		}
	});
	$( "#sortable" ).disableSelection();


    // Make Tools panel group minimizable
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
	});



    // Close thr panel
    $(".close-panel").on("click", function() {
		$(this).parent().parent().hide();
	});

    
    // Add Tooltips
    $('button').tooltip();
    $('.toggle-button-group').tooltip();
  };

//});
