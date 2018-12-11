package cc.mi.scene.movement;

import cc.mi.core.manager.MapTemplateManager;
import cc.mi.core.utils.Point2D;
import cc.mi.core.xlsxData.MapTemplate;
import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;

/**
 * 跟随模式
 * @author gy
 *
 */
public class FollowMovement extends MovementBase {

	/**
	 * 要不要围成一圈什么的 再说
	 * @param creature
	 * @param target
	 * @return
	 */
	protected Point2D<Float> getTargetRelatedPos(SceneCreature creature, SceneElement target) {
		
		float dist = (float) creature.getDistance(target);
		float attackRange = creature.getAttackRange();
		if (dist <= attackRange) {
			return new Point2D<Float>(creature.getPositionX(), creature.getPositionY());
		}
		
		float angle = creature.getAngle(target);
		float d = dist - attackRange;
		float nx = (float) (creature.getPositionX() + d * Math.cos(angle));
		float ny = (float) (creature.getPositionY() + d * Math.sin(angle));
		return new Point2D<Float>(nx, ny);
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
		
		Point2D<Float> newTarget = this.getTargetRelatedPos(creature, target);
		// 没有障碍物直接移动
		int mapId = creature.getMapId();
		MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(mapId);
		// 起始点和目标点无障碍物
		if (mt.isCanRun(creature.getPositionX(), creature.getPositionY(), newTarget.getX(), newTarget.getY(), true)) {
			
		} else {
			// 主干道寻路
		}
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
