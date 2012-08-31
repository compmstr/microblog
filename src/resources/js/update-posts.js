var socket = io.connect('http://localhost:1338');
socket.on('update', function(data){
    var update = $.parseJSON(data);
    if(update.message == "blog-added"){
      var oldFirst = $('.post')[0];
      var parent = oldFirst.parentNode;
      console.log(update);
      var newNode = $(update['new-entry']).hide().fadeIn();
      parent.insertBefore(newNode[0], oldFirst);
    }
});