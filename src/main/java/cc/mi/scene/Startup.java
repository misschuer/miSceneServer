package cc.mi.scene;

import cc.mi.core.net.ClientCore;
import cc.mi.scene.config.ServerConfig;
import cc.mi.scene.net.SceneHandler;

public class Startup {
	private static void start() throws NumberFormatException, Exception {
		ServerConfig.loadConfig();
		ClientCore.start(ServerConfig.getIp(), ServerConfig.getPort(), new SceneHandler());
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		start();
	}
}
