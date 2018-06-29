package com.aeshna.mahjongilly;

import com.aeshna.sprites.Starship;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class Mahjonggilly extends ApplicationAdapter {
	SpriteBatch batch;
	private Socket socket;
	Starship player;
	Texture playerShip, friendlyShip;
	HashMap<String, Starship> friendlyPlayers;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
        playerShip = new Texture("blep.jpg");
        friendlyShip = new Texture("penanggallen.jpg");
        friendlyPlayers = new HashMap<String, Starship>();

		connectSocket();
		configSocketEvents();
	}

	public void handleInput(float dt) {
	    if (player != null) {
	        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
	            player.setPosition(player.getX() + (-200 * dt), player.getY());
            }
            else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                player.setPosition(player.getX() + (200 * dt), player.getY());
            }
        }
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput(Gdx.graphics.getDeltaTime());
		batch.begin();
		if (player != null) {
		    player.draw(batch);
        }
        for (HashMap.Entry<String, Starship> entry : friendlyPlayers.entrySet()) {
		    entry.getValue().draw(batch);
        }
		batch.end();
	}
	
	@Override
	public void dispose () {
	    super.dispose();
	    playerShip.dispose();
	    friendlyShip.dispose();
		batch.dispose();
	}

	public void connectSocket() {
	    try {
	        socket = IO.socket("http://localhost:8088");
	        socket.connect();
        }
        catch (Exception e) {
	        System.out.println(e);
        }
    }

    public void configSocketEvents() {
	    // connection to server
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
                player = new Starship(playerShip);
            }
            }).on("socketID", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                String id;
                try {
                    id = data.getString("id");
                    Gdx.app.log("SocketIO", "My ID: " + id);
                }
                catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting socketID");
                }

            }
        }).on("newPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String id;
                try {
                    id = data.getString("id");
                    Gdx.app.log("SocketIO", "New Player connected with ID: " + id);
                    friendlyPlayers.put(id, new Starship(friendlyShip));
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting new player ID");
                }

            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String id;
                try {
                    id = data.getString("id");
                    Gdx.app.log("SocketIO", "Player with ID " + id + " disconnected");
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting new player ID");
                }

            }
        });
    }
}