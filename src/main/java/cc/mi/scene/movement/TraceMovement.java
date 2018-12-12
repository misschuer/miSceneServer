package cc.mi.scene.movement;

import cc.mi.core.constance.MovementType;
import cc.mi.core.manager.MapTemplateManager;
import cc.mi.core.utils.Path;
import cc.mi.core.utils.Point2D;
import cc.mi.core.xlsxData.MapTemplate;
import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;

/**
 * 追踪模式
 * @author gy
 *
 */
public class TraceMovement extends MovementBase {
	private Point2D<Float> newTarget;
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
	
	protected boolean mainLoadFindPath(SceneCreature creature) {
		SceneElement target = creature.getTarget();
		if (target != null && creature.getDistance(target) > 50) {
			return false;
		}
		
		int mapId = creature.getMapId();
		MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(mapId);
		Path path = mt.getPath(creature.getPositionX(), creature.getPositionY(), target.getPositionX(), target.getPositionY());
		creature.moveTo(path, false);
		return true;
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
		
		newTarget = this.getTargetRelatedPos(creature, target);
		// 没有障碍物直接移动
		int mapId = creature.getMapId();
		MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(mapId);
		// 起始点和目标点无障碍物
		if (mt.isCanRun(creature.getPositionX(), creature.getPositionY(), newTarget.getX(), newTarget.getY(), true)) {
			creature.moveTo(newTarget.getX(), newTarget.getY());
		} else {
			// 主干道寻路
			if (this.mainLoadFindPath(creature)) {
				return;
			}
			
			// TODO: 这个都寻不到 说明不是同一个地点就不寻路了吧
			creature.setTarget(null);
		}
	}
	
	@Override
	public void finalize(SceneCreature creature) {
		creature.stopMoving(true);
	}

	@Override
	public boolean update(SceneCreature creature, int diff) {
		// 目标不存在了
		if (creature.getTarget() == null) {
			return true;
		}
		
		if (!creature.isCanMove()) {
			creature.setTarget(null);
			return true;
		}
		
		if (!creature.getTarget().isAlive()) {
			creature.setTarget(null);
			return true;
		}
		
		float tx = creature.getTarget().getPositionX();
		float ty = creature.getTarget().getPositionY();
		
		//如果生物是移动状态，并且目标的方向相同则不用处理，目标移动类型设置了目标
		if (newTarget.getX() != tx || newTarget.getY() != ty 
				|| !creature.isMoving()) {
//			if (newTarget.getX() != tx || newTarget.getY() != ty) {
//				
//			}
			int mapId = creature.getMapId();
			MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(mapId);
			if (creature.isMoving() && !mt.isValidPosition((int)creature.getPositionX(), (int)creature.getPositionY())) {
				return true;
			}
			creature.stopMoving(true);
			this.init(creature, 0);
		}
		
		return true;
	}

	@Override
	public int getMovementType() {
		return MovementType.TRACE;
	}
}
