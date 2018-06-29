var app = require('express');
var server = require('http').Server(app);
var io = require('socket.io')(server);

// Note: Port 8088 because Jenkins is already running on 8080
server.listen(8088, function() {
    console.log("Server is now running...");
});


// Handle the client connection
io.on('connection', function(socket) {
    console.log("Player connected!");
    
    // Send an event to the client
    socket.emit('socketID', { id: socket.id });
    socket.broadcast.emit('newPlayer', { id: socket.id });
    socket.on('disconnect', function() {
        socket.broadcast.emit('playerDisconnected', { id: socket.id });
        console.log("Player disconnected!");
    })
})

