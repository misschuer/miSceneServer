package cc.mi.scene.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;

/**
 * 如果可能的话作为scenecreature的内部静态类 这样不会有循环引用问题
 * @author gy
 *
 */
public class ThreatManager {
	protected int visionRadius = 0;
	protected int actionRadius = 0;
	protected Map<Integer, ThreatValue> targetHash = new HashMap<>();
	
	public void init(int visionRadius, int actionRadius) {
		this.visionRadius = visionRadius;
		this.actionRadius = actionRadius;
	}
	
	/*
	 * 获得target的intid
	 */
	public int getTarget() {
		int targetId = -1;
		int hatred = 0;
		
		for (Entry<Integer, ThreatValue> info : targetHash.entrySet()) {
			if (hatred < info.getValue().getHatred()) {
				hatred = info.getValue().getHatred();
				targetId = info.getKey();
			}
		}
		
		return targetId;
	}
	
	public void addValue(SceneElement self, SceneElement target, int v) {
		// 如果不会加仇恨的return
		if (!targetHash.containsKey(target.getUintId())) {
			if (self.isCanAttack(target)) {
				ThreatValue value = new ThreatValue();
				value.setAttackHatred(v);
				targetHash.put(target.getUintId(), value);
			}
			//初次出现在视野
			self.onInSight(target);
			return;
		}
		ThreatValue value = targetHash.get(target.getUintId());
		value.modify(v);
	}
	
	public int getThreatDistanceHatred(float distance) {
		int dist = (int) distance;
		return this.visionRadius >= dist ? this.visionRadius - dist + 1 : 0;
	}
	
	public void subValue(SceneElement target, int v) {
		int id = target.getUintId();
		if (targetHash.containsKey(id)) {
			ThreatValue tv = targetHash.get(id);
			if (tv.modify(-v) > 0) {
				return;
			}
			targetHash.remove(id);
		}
	}
	
	public void resetRadius(int visionRadius, int actionRadius) {
		this.visionRadius = visionRadius;
		this.actionRadius = actionRadius;
	}
	
	public void delThreatTarget(SceneElement self, SceneElement target) {
		if (targetHash.remove(target.getUintId()) != null) {
			target.delAttackTarget(self.getUintId());
		}
	}
	
	public void clear() {
		this.targetHash.clear();
	}

	public boolean update(SceneCreature self, int diff) {
//		ASSERT(me);	
//		ASSERT(me->isAlive());
//		//目前没有逃脱模式
//		//if(me->GetEvadeState()) return;
//		
//		//如果是防御怪
//		if(me->GetReactState() == REACT_DEFENSIVE)
//			return;
//
//		float x,y;
//		me->GetBornPos(x,y);
//
//		//超出活动范围直接返回
//		if((uint32)me->GetDistance(x,y) > actionradius) 
//		{
//			Clear();
//			//清除归属者
//			me->ResetOwnerShip();
//			//有待商榷
//			//me->ClearAttacker();
//			return;
//		}
//		//更新旧的仇恨列表
//		UpdateThreatMap();
//
//		//如果可能主动攻击其他生物的生物
//		if(me->GetReactState() == REACT_AGGRESSIVE || me->GetReactState() == REACT_AGGRESSIVE_UNIT )
//		{
//			Grid *grid = me->GetGrid();
//			//从周围九大格获得所有玩家
//			Grid::CreatureList::iterator iter;
//			Grid::GridPtrVec::iterator viter;
//			for(viter = grid->notice_grid.begin(); viter != grid->notice_grid.end(); ++viter)
//				for(iter = (*viter)->creatures.begin(); iter != (*viter)->creatures.end(); ++iter)
//					CaluThreatByDistance(*iter);
//		}
//
//		if(me->GetReactState() == REACT_AGGRESSIVE || me->GetReactState() == REACT_AGGRESSIVE_PLAYER )
//		{
//			//如果可能主动攻击玩家的生物
//			Grid *grid = me->GetGrid();
//			//从周围九大格获得所有玩家
//			PlayerSet::iterator iter;
//			Grid::GridPtrVec::iterator viter;
//			for(viter = grid->notice_grid.begin(); viter != grid->notice_grid.end(); ++viter)
//				for(iter = (*viter)->players.begin(); iter != (*viter)->players.end(); ++iter)
//					CaluThreatByDistance(*iter);
//		}
//
//		//走向最大仇恨目标
//		Unit *threatTarget = GetTarget();
//		if (threatTarget != me->GetTarget())
//		{
//			me->SetTarget(threatTarget);
//			if (threatTarget)
//				me->MotionMoveTarget();//移动到目标
//		}
//
//		// 如果找不到目标 则清空攻击列表
//		if (threatTarget == NULL) {
//			if (me->GetHealth() < me->GetMaxHealth()) {
//				// 清空攻击关系列表
//				me->GetMap()->ClearCreatureHitHash(me->GetUIntGuid());
//			}
//		}
		return false;
	}
	
	protected void calcDistanceHatred(SceneCreature self) {
//		//如果玩家已经存在仇恨则不需要再次增加距离仇恨	
//		if(m_targetMap.count(_target->GetUIntGuid())>0)
//			return;
//		float x,y;
//		me->GetBornPos(x,y);
//
//		//玩家在视野范围内，并且在怪物活动范围内
//		float distance = 0.0f;
//		if( _target->isAlive() && 
//			!me->IsFriendlyTo(_target) &&
//			_target->GetDistance(x, y) < actionradius && 
//			(distance = me->GetDistance(_target)) < visionradius&&
//			_target->isCanSee())
//		{
//			//初次出现在视野
//			if(me->AI_MoveInLineOfSight(_target) != 0)
//			{
//				m_targetMap.insert(TargetMap::value_type(_target->GetUIntGuid(), ThreatValue(0,  getThreat(distance))));
//			}
//		}
	}
	
	protected void updateThreadMap() {
//		Unit *threater = 0;
//		Unit *tagert = me->GetTarget();
//		float x,y;
//		me->GetBornPos(x,y);
//		float distance =  0.0f;
//		TargetMap::iterator iter = m_targetMap.begin();
//		while(iter != m_targetMap.end())
//		{
//			threater = me->GetMap()->FindUnit(iter->first);
//			if (!threater )
//			{
//				me->DelOwnerShip(iter->first);
//				iter = m_targetMap.erase(iter);
//				continue;
//			}
//			if(	threater->isAlive() &&
//				threater->GetDistance(x,y) < actionradius)
//			{						
//				distance = me->GetDistance(threater);
//				if(distance < visionradius)
//					iter->second.second = getThreat(distance);
//				else 
//					iter->second.second = 0;
//				++iter;
//				continue;
//			}
//			m_targetMap.erase(iter++);
//			threater->DelAttacker(me->GetUIntGuid());
//			if (threater->GetTypeId() == TYPEID_PLAYER)
//				me->DelOwnerShip(threater->GetUIntGuid());
//			//TODO:这里可能出现死循环，修改时要注意
//			if(tagert == threater) me->SetTarget(NULL);
//		}
//
//		// 仇恨列表里面没有目标
//		if (m_targetMap.size() == 0) {
//			string prev = me->GetStr(UNIT_STRING_FIELD_DROP_OWNER_GUID);
//			if (prev != "") {
//				Player* player = me->GetMap()->FindPlayer(prev.c_str());
//				if (player) {
//					player->SetUInt32(UNIT_INT_FIELD_BOSS_OWN_FLAG, 0);
//				}
//				me->SetStr(UNIT_STRING_FIELD_DROP_OWNER_GUID, "");
//				me->SetStr(UNIT_STRING_FIELD_DROP_OWNER_NAME, "");
//			}
//			creature_template *temp = creature_template_db[me->GetEntry()];
//			if (temp && !temp->recure) {
//				me->ModifyHealth(me->GetMaxHealth());
//			}
//		}
	}
}
