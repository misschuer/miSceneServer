package cc.mi.scene.movement;

import cc.mi.core.utils.Point2D;
import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;

/**
 * 跟随模式
 * @author gy
 *
 */
public class FollowMovement extends MovementBase {

	protected Point2D<Float> getTargetRelatedPos(SceneCreature creature, SceneElement target) {
		return null;
	}
	
	@Override
	public void init(SceneCreature creature, int params1) {
		if (!creature.isAlive() || !creature.isCanMove()) {
			return;
		}
		
		SceneElement target = creature.getTarget();
		if (target == null) {
			return;
		}
		
//		Point2D<Float> newTarget = this.getTargetRelatedPos(creature, target);
		//TODO: 明天继续做
	}
	
	@Override
	public void finalize(SceneCreature creature) {
		
	}

	@Override
	public boolean update(SceneCreature creature, int diff) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMovementType() {
		// TODO Auto-generated method stub
		return 0;
	}

}
