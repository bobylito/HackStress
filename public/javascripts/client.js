$(function() {
	$.urlParam = function(name){
	    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
	    return results[1] || 0;
	}

	if(!!window.EventSource) {
		
		var source = new EventSource("/live/"+$.urlParam('project'));
		
		source.onmessage =  function(e) {
			var msg = JSON.parse(e.data);
			$("#messages").append("<li>" + JSON.stringify( msg ) + "</li>")
		};
		
	}
});