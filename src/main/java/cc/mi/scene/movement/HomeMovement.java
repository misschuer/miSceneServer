package cc.mi.scene.movement;

import cc.mi.core.constance.MovementType;
import cc.mi.scene.element.SceneCreature;

public class HomeMovement extends MovementBase {

	@Override
	public void init(SceneCreature creature, int params1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean update(SceneCreature creature, int diff) {
		
		if (!creature.isAlive()) {
            return false;
		}

        //如果被限制移动移动
        if (!creature.isCanMove()) {
            if (creature.isMoving()) {
                creature.stopMoving(true);
            }
            return true;
        }

        //如果已经停下来了
        if (creature.isMoving()) {
            return true;
        }

       return creature.backHomeIfEnabled();
	}

	@Override
	public int getMovementType() {
		return MovementType.HOME;
	}

	@Override
	public MovementBase newInstance() {
		return new HomeMovement();
	}

}
