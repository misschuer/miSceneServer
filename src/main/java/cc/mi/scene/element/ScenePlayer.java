package cc.mi.scene.element;

import cc.mi.scene.server.SceneContext;

public class ScenePlayer extends SceneElement {
	
	private SceneContext context;
	
	public ScenePlayer() {
		super(SceneElement.ELEMENT_TYPE_PLAYER);
	}

	public SceneContext getContext() {
		return context;
	}

	public void setContext(SceneContext context) {
		this.context = context;
	}
	
}
