package cc.mi.scene.element;

import cc.mi.scene.info.CharacterStatistics;
import cc.mi.scene.server.SceneContext;

public class ScenePlayer extends SceneElement {
	
	private SceneContext context;
	private final CharacterStatistics cs;
	
	public ScenePlayer() {
		super(SceneElement.ELEMENT_TYPE_PLAYER);
		this.cs = new CharacterStatistics();
	}

	public SceneContext getContext() {
		return context;
	}

	public void setContext(SceneContext context) {
		this.context = context;
	}
	
	public void update(int diff) {
		super.update(diff);
	}

	public CharacterStatistics getCs() {
		return cs;
	}
}
