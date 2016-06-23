package com.example.admin.myapplication.GameStatics;

public class ServerItem {
	
	private String name, version, ip;
	private int players, maxPlayers, port;
	
	public static ServerItem blank(String ip, int port){
		return new ServerItem("noServer", "0", ip, 0, 0, port);
	}
	
	public ServerItem(String name, String version, String ip, int players,
			int maxPlayers, int port) {
		this.name = name;
		this.version = version;
		this.ip = ip;
		this.players = players;
		this.maxPlayers = maxPlayers;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getIp() {
		return ip;
	}

	public int getPlayers() {
		return players;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getPort() {
		return port;
	}
	
	@Override
	public String toString() {
		return name+"[v." + version + ", " + players + "/" + maxPlayers + "]";
	}

}
