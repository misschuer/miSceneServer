package cc.mi.scene.element;

public class SceneGameObject extends SceneElement {

	public SceneGameObject() {
		super(SceneElement.ELEMENT_TYPE_GAMEOBJECT);
	}

	@Override
	public boolean update(int diff) {
		return super.update(diff);
	}
	
	public boolean create(String binlogId, int entry) {
		// TODO:先判断entry是否存在表中
		// 初始化
		super.onInit(binlogId, entry);
		// 设置属性
		// name 等
		return true;
	}
	
	public boolean isTeleport() {
		return false;
	}
}
