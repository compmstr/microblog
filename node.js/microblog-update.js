var net = require('net');

var server = net.createServer(function (socket){
  socket.write('Echo Server\r\n');
  socket.pipe(socket);
  socket.on("end", function(){
    console.log("Connection closed");
  });
  socket.on("data", function(data){
    var msg = data.toString().trim();
    console.log("Message: " + msg);
    if(msg.indexOf("PING") == 0){
      var split = msg.split(/\s+/);
      socket.write("PONG " + split[1] + "\r\n");
      console.log("Pong");
    }else if(msg.indexOf("BROADCAST " == 0)){
      var bcast = msg.substring("BROADCAST ".length);
      console.log("Broadcasting message: " + bcast);
    }
  });
});

server.listen(1337, '127.0.0.1');
