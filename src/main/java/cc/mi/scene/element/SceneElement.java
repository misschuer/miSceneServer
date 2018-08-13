package cc.mi.scene.element;

public abstract class SceneElement {
	
	public static final int ELEMENT_TYPE_PLAYER		= 1;
	public static final int ELEMENT_TYPE_CREATURE	= 2;
	public static final int ELEMENT_TYPE_GAMEOBJECT	= 3;
	
	private final int elementType;
	
	public SceneElement(int elementType) {
		this.elementType = elementType;
	}

	public int getElementType() {
		return elementType;
	}
}
