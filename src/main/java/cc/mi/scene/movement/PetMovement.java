package cc.mi.scene.movement;

import cc.mi.scene.element.SceneCreature;

/**
 * 宠物的战斗方式(跟随玩家并攻击玩家所选择的目标)
 * @author gy
 *
 */
public class PetMovement extends HuntingMovement {
	@Override
	public void init(SceneCreature creature, int params1) {
		// TODO Auto-generated method stub

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
