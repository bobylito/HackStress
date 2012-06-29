$(function() {
	if(!!window.EventSource) {
		
		var source = new EventSource("/live");
		
		source.onmessage =  function(e) {
			var msg = JSON.parse(e.data);
			$("#messages").append("<li>" + JSON.stringify( msg ) + "</li>")
		};
		
	}
});