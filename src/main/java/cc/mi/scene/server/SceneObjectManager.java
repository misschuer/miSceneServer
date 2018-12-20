package cc.mi.scene.server;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.constance.IdentityConst;
import cc.mi.core.server.GuidManager;
import cc.mi.core.server.ServerObjectManager;

public class SceneObjectManager extends ServerObjectManager {
	public static final SceneObjectManager INSTANCE = new SceneObjectManager();
	private SceneObjectManager() {
		super(IdentityConst.SERVER_TYPE_SCENE);
	}

	@Override
	protected BinlogData createBinlogData(String guid) {
		if (GuidManager.INSTANCE.isPlayerGuid(guid)) {
			return new SceneContextPlayer();
		}
		return new BinlogData(1 << 6, 1 << 6);
	}
	
	public boolean update(int diff) {
		return false;
	}
}
