package cc.mi.scene.movement;

import cc.mi.core.constance.MovementType;
import cc.mi.core.utils.TimerInterval;
import cc.mi.scene.element.SceneCreature;

/**
 * 禁止模式
 * @author gy
 *
 */
public class IdleMovement extends MovementBase {
	protected TimerInterval timer;
	
	@Override
	public void init(SceneCreature creature, int params1) {
		this.timer = new TimerInterval(params1);
		this.timer.reset();
	}
	
	public boolean isInfiniteTimer() {
		return timer.getInterval() == 0;
	}

	@Override
	public boolean update(SceneCreature creature, int diff) {
		if (!this.isInfiniteTimer()) {
			return true;
		}
		
		if (this.timer.update(diff)) {
			creature.setTarget(null);
			return false;
		}
		return true;
	}

	@Override
	public int getMovementType() {
		return MovementType.IDLE;
	}

	@Override
	public MovementBase newInstance() {
		return new IdleMovement();
	}
}
