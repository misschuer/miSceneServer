package cc.mi.scene.server;

import cc.mi.core.constance.IdentityConst;
import cc.mi.core.server.ServerObjectManager;

public class SceneObjectManager extends ServerObjectManager {
	protected SceneObjectManager() {
		super(IdentityConst.SERVER_TYPE_SCENE);
	}
}
