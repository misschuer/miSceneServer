package cc.mi.scene.movement;

import cc.mi.core.constance.MovementType;
import cc.mi.scene.element.SceneCreature;

public class MainLoadMovement extends MovementBase {

	@Override
	public void init(SceneCreature creature, int params1) {
		if (!creature.isAlive()) {
			return;
		}
		
//	    //设置路径
//	    wayid = static_cast<uint32>(param);
//		const vector<mt_monsterline_t>& mt_path = creature.GetMap()->GetMapTemp()->m_monsterlines;
//		if(mt_path.size() <= wayid || mt_path[wayid].path.empty())
//		{
//			tea_pwarn("path err");
//		}
//		else
//		{
//			if(t == 0)
//				path = creature.GetMap()->GetMapTemp()->m_monsterlines[wayid].path;
//			else
//			{
//				int size = mt_path[wayid].path.size();
//				for (int i = mt_path[wayid].path.size() - 1; i >= 0; i --)
//				{
//					path.push_back(mt_path[wayid].path[i]);
//				}
//			}
//		}
//		point_index = 0;
//
//	    //开始运行准备
//	    /*
//	    mt_point m_target = path[point_index];
//	    point_index++;
//
//	    creature.MoveTo(float(m_target.pos_x), float(m_target.pos_y));*/
//
//		if(!creature.CanActiveGrid())
//		{
//			creature.SetCanActiveGrid(true);
//			m_revert_active_grid = true;
//		}
	}
	
	@Override
	public void finalize(SceneCreature creature) {
//		if(m_revert_active_grid)
//			creature.SetCanActiveGrid(false);
	}

	@Override
	public boolean update(SceneCreature creature, int diff) {
//		if(path.size() == 0)
//			return false;
//	    //如果被限制移动移动
//	    if(!creature.isCanMove())
//	    {
//	        if(creature.IsMoving())
//	            creature.StopMoving(true);
//
//	        return true;
//	    }
//
//	    //如果已经到达目标
//	    if(!creature.IsMoving())
//	    {
//			if(point_index !=0 && creature.GetDistance(path[point_index-1].pos_x,path[point_index-1].pos_y) > 1.0f)
//			{
//				creature.MoveTo(float(path[point_index-1].pos_x), float(path[point_index-1].pos_y));
//			}
//			else if(point_index < path.size())
//			{
//				creature.MoveTo(float(path[point_index].pos_x), float(path[point_index].pos_y));
//				point_index ++;
//			}
//			else//else if (point_index >= (path.size() + 1) >> 1)
//			{
//				//修改出生点位置，省得走回去
//				creature.SetBornPos(path[point_index - 1].pos_x, path[point_index - 1].pos_y);
//				//触发一下区域通知,不想在这里触发
//				char name[50];
//				memset(name, 0, 50);
//				//sprintf(name, "%u", wayid);
//				OnNotifyArea(creature.GetMap(), &creature, name, path[point_index - 1].pos_x, path[point_index - 1].pos_y);
//				return false;		//走完路线,关掉吧
//			}			
//	    }

	    return true;
	}

	@Override
	public int getMovementType() {
		return MovementType.MAIN_LOAD;
	}
}
