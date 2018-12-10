package cc.mi.scene.movement;

import cc.mi.scene.element.SceneCreature;

public abstract class MovementBase {
	
	public abstract void init(SceneCreature creature, int params1);
	public void finalize(SceneCreature creature) {}
	public abstract boolean update(SceneCreature creature, int diff);
	public abstract int getMovementType();
	
}
