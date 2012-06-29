$(function() {

  var msgTemplate = _.template($("#message_template").html()),
      urlParam = function(name) {
        var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href) || 0;
        return results[1];
      },
      bindEvtSrc = function(projectName){
        if(!!window.EventSource) {
          var source = new EventSource("/live/"+projectName);

          source.onmessage =  function(e) {
            var msg = JSON.parse(e.data);
            msg.date = new Date().toLocaleFormat("%Hh%M");
            $("#messages").append( msgTemplate( msg ) )
          };
        }
      },
      initProjectName = urlParam('project');

  if(initProjectName){
    $('#tellMe').hide(); 
    bindEvtSrc(initProjectName);
  }
  else {
    $('#tellMe').submit(function(e){
      e.preventDefault();
      e.stopPropagation();
      var projet = $("#project").val();

      history.pushState({}, document.title, location.href+"?project="+projet);

      bindEvtSrc(projet)

      $('#tellMe').hide(); 
    });
  }

  

});
