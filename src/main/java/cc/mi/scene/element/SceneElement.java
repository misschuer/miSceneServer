package cc.mi.scene.element;

import cc.mi.core.binlog.data.BinlogData;

public abstract class SceneElement extends BinlogData {

	public static final int ELEMENT_TYPE_PLAYER		= 1;
	public static final int ELEMENT_TYPE_CREATURE	= 2;
	public static final int ELEMENT_TYPE_GAMEOBJECT	= 3;

	private final int elementType;

	public SceneElement(int elementType) {
		super(64, 64);
		this.elementType = elementType;
	}

	public int getElementType() {
		return elementType;
	}
}
