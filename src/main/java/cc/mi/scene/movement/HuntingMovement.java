package cc.mi.scene.movement;

import cc.mi.core.constance.MovementType;
import cc.mi.scene.element.SceneCreature;

/**
 * 追杀模式
 * @author gy
 *
 */
public class HuntingMovement extends TraceMovement {
	@Override
	public void init(SceneCreature creature, int params1) {
		super.init(creature, params1);
	}
	
	@Override
	public void finalize(SceneCreature creature) {
		super.finalize(creature);
	}

	@Override
	public boolean update(SceneCreature creature, int diff) {
		if (creature.getTarget() == null) {
			return false;
		}
		if (this.attackTarget(creature, diff)) {
			return true;
		}
		return super.update(creature, diff);
	}
	
	private boolean attackTarget(SceneCreature creature, int diff) {
		// 攻击逻辑
		return false;
	}

	@Override
	public int getMovementType() {
		return MovementType.HUNTING;
	}
}
