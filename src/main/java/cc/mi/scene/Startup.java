package cc.mi.scene;

import java.io.IOException;
import java.net.URL;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import cc.mi.core.serverClient.ServerClient;
import cc.mi.scene.sceneClient.SceneClientHandler;

public class Startup {
	private static final String SCENE_CLIENT = "sceneClient";
	private static final String IP = "ip";
	private static final String PORT = "port";
	
	private static void loadConfig() throws NumberFormatException, Exception {
		Config cfg = new Config();
		URL url = Startup.class.getResource("/config.ini");
		Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
        	// 加载配置文件  
        	ini.load(url);

        	Section section = ini.get(SCENE_CLIENT);
        	ServerClient.start(section.get(IP), Integer.parseInt(section.get(PORT)), new SceneClientHandler());
        	
        } catch (IOException e) {
        	e.printStackTrace();
	    }  
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		//TODO: 这里传参数 来确定是第几个场景服
		loadConfig();
	}

}
