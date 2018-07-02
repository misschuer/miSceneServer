package cc.mi.scene.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cc.mi.core.constance.IdentityConst;
import cc.mi.core.handler.Handler;
import cc.mi.core.manager.ServerManager;

public class SceneServerManager extends ServerManager {
	private static SceneServerManager instance;
	// 消息收到以后的回调
	private static final Map<Integer, Handler> handlers = new HashMap<>();
	private static final List<Integer> opcodes;
	
	static {
		
		opcodes = new LinkedList<>();
		opcodes.addAll(handlers.keySet());
	}
	
	public static SceneServerManager getInstance() {
		if (instance == null) {
			instance = new SceneServerManager();
		}
		return instance;
	}
	
	public SceneServerManager() {
		super(IdentityConst.SERVER_TYPE_SCENE, opcodes);
	}
}
