package cc.mi.scene.server;

import cc.mi.core.constance.IdentityConst;
import cc.mi.core.manager.ServerManager;

public class SceneServerManager extends ServerManager {
	private static SceneServerManager instance;
	
	public static SceneServerManager getInstance() {
		if (instance == null) {
			instance = new SceneServerManager();
		}
		return instance;
	}
	
	public SceneServerManager() {
		super(IdentityConst.SERVER_TYPE_SCENE);
	}
}
