package cc.mi.scene.element;

public class SceneTeleport extends SceneGameObject {
	// 需要到达的x
	private final int toX;
	// 需要到达的y
	private final int toY;
	// 需要到达的mapId
	private final int toMapId;
	// 需要到达的name
	private final String toName;
	
	public SceneTeleport(int toX, int toY, int toMapId, String toName) {
		super();
		this.toMapId = toMapId;
		this.toName  = toName;
		this.toX	 = toX;
		this.toY	 = toY;
	}

	@Override
	public boolean update(int diff) {
		return super.update(diff);
	}
	
	public boolean isTeleport() {
		// TODO: 是否是传送阵
		return true;
	}

	public int getToX() {
		return toX;
	}

	public int getToY() {
		return toY;
	}

	public int getToMapId() {
		return toMapId;
	}

	public String getToName() {
		return toName;
	}
}
