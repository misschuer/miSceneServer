package cc.mi.scene.element;

public class SceneCreature extends SceneElement {

	public SceneCreature() {
		super(SceneElement.ELEMENT_TYPE_CREATURE);
	}
	
	public boolean update(int diff) {
		return super.update(diff);
	}
	
	private void initBase(int entry) {
		// 读模板表
	}
	
	public boolean create(String binlogId, int entry) {
		// TODO:先判断entry是否存在表中
		// 初始化
		super.onInit(binlogId, entry);
		
		this.initBase(entry);
		// 设置属性
		// name 等
		return true;
	}
}
