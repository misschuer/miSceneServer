package cc.mi.scene.config;

import java.io.IOException;
import java.net.URL;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import cc.mi.core.constance.NetConst;

public class ServerConfig {
	private static final String SCENE_CLIENT = "sceneClient";
	private static String ip;
	private static int port;
	
	public static void loadConfig() throws NumberFormatException, Exception {
		Config cfg = new Config();
		URL url = ServerConfig.class.getResource("/config.ini");
		Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
        	// 加载配置文件  
        	ini.load(url);

        	Section section = ini.get(SCENE_CLIENT);
        	ip = section.get(NetConst.IP);
        	port = Integer.parseInt(section.get(NetConst.PORT));
        } catch (IOException e) {
        	e.printStackTrace();
	    }  
	}

	public static String getIp() {
		return ip;
	}

	public static int getPort() {
		return port;
	}
}
