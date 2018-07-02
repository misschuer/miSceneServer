package cc.mi.scene;

import cc.mi.core.net.ClientCore;
import cc.mi.scene.config.ServerConfig;
import cc.mi.scene.net.SceneHandler;
import cc.mi.scene.net.SceneToGateHandler;

public class Startup {
	private static void start() throws NumberFormatException, Exception {
		ServerConfig.loadConfig();
//		ClientCore.start(ServerConfig.getGateIp(), ServerConfig.getGatePort(), new SceneToGateHandler(), false);
//		ClientCore.start(ServerConfig.getCenterIp(), ServerConfig.getCenterPort(), new SceneHandler());
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		start();
	}
}
