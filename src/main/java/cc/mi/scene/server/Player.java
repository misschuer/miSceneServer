package cc.mi.scene.server;

import cc.mi.core.constance.PlayerEnumFields;
import cc.mi.core.server.PlayerBase;

public class Player extends PlayerBase {
	private final SceneContext context;
	public Player() {
		super(PlayerEnumFields.PLAYER_INT_FIELDS_SIZE, PlayerEnumFields.PLAYER_STR_FIELDS_SIZE);
		this.context = new SceneContext(0);
	}
	public SceneContext getContext() {
		return context;
	}
}
