$(function() {
	function urlParam(name){
	    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href) || 0;
	    return results[1];
	}

	if(!!window.EventSource) {
		var source = new EventSource("/live/"+urlParam('project'));
		
		source.onmessage =  function(e) {
			var msg = JSON.parse(e.data);
			$("#messages").append("<li>" + JSON.stringify( msg ) + "</li>")
		};
		
	}
});
