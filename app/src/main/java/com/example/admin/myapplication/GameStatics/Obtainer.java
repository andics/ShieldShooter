package com.example.admin.myapplication.GameStatics;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Obtainer {

	public static ServerItem obtain(String host, int port, int timeout) {
		try {
			InetAddress addr = InetAddress.getByName(host);

			String name, version;
			int players, maxPlayers;

			Socket socket = new Socket(addr, port);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

			name = request("name", writer, reader, timeout);
			version = request("version", writer, reader, timeout);
			players = Integer.parseInt(request("players", writer, reader, timeout));
			maxPlayers = Integer.parseInt(request("maxPlayers", writer, reader, timeout));

			socket.close();

			return new ServerItem(name, version, host, players, maxPlayers, port);
		} catch (Exception e) {
			return ServerItem.blank(host, port);
		}
	}

	public static List<ServerItem> obtainAll(List<String> ips,
			List<Integer> ports, int timeout) {
		List<ServerItem> toReturn = new ArrayList<ServerItem>();

		Socket socket = null;

		String name, version;
		int players, maxPlayers;

		for (int i = 0; i < Math.min(ips.size(), ports.size()); i++) {
			String host = ips.get(i);
			int port = ports.get(i);
            Log.e("Obtaing ip11 ", host);
			try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), timeout*1000);

                Log.e("Obtaing ip2 ", host);

				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);

				name = request("name", writer, reader, timeout);
				version = request("version", writer, reader, timeout);
				players = Integer.parseInt(request("players", writer, reader, timeout));
				maxPlayers = Integer.parseInt(request("maxPlayers", writer, reader, timeout));

                Log.e("Obtaing ip3 ", host);

				socket.close();

				toReturn.add(new ServerItem(name, version, host, players, maxPlayers, port));

			} catch (Exception io) {
				toReturn.add(ServerItem.blank(host, port));
			}
		}

		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return toReturn;
	}

	private static String request(String string, PrintWriter writer,
			BufferedReader reader, int timeoutSec) throws Exception {
		writer.println(string);
		long goal = System.currentTimeMillis() + (timeoutSec * 1000);
		try {
			while (!reader.ready()) {
				if (System.currentTimeMillis() > goal)
					throw new Exception();
			}
			return reader.readLine();
		} catch (IOException e) {
			throw new Exception(e);
		}
	}

}
