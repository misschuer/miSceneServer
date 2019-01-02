package cc.mi.scene.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cc.mi.core.constance.ReactType;
import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;
import cc.mi.scene.element.ScenePlayer;
import cc.mi.scene.grid.Grid;

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
	public SceneElement getTarget() {
		SceneElement target = null;
		int hatred = 0;
		
		for (Entry<Integer, ThreatValue> info : targetHash.entrySet()) {
			if (hatred < info.getValue().getHatred()) {
				hatred = info.getValue().getHatred();
				target = info.getValue().getTarget();
			}
		}
		
		return target;
	}
	
	public void addValue(SceneElement self, SceneElement target, int v) {
		// 如果不会加仇恨的return
		if (!targetHash.containsKey(target.getUintId())) {
			if (self.isCanAttack(target)) {
				ThreatValue value = new ThreatValue(target);
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
		if (self.getReactType() == ReactType.REACT_DEFENSIVE) {
			return false;
		}
		
		//超出活动范围直接返回
		if (!self.isInActionSight(this.actionRadius)) {
			this.clear();
			//清除归属者
//			me->ResetOwnerShip();
			//有待商榷
			//me->ClearAttacker();
			return false;
		}
		
		//更新旧的仇恨列表
		this.updateThreadMap(self);

		//如果可能主动攻击其他生物的生物
		if (self.getReactType() == ReactType.REACT_AGGRESSIVE || self.getReactType() == ReactType.REACT_AGGRESSIVE_UNIT) {
			Grid grid = self.getGrid();
			//从周围九大格获得所有玩家
			Iterator<Grid> gridIter = grid.noticeGridIterator();
			for (;gridIter.hasNext();) {
				Grid neib = gridIter.next();
				Iterator<SceneCreature> creatureIter = neib.creatureIterator();
				for (;creatureIter.hasNext();) {
					SceneCreature creature = creatureIter.next();
					this.calcDistanceHatred(self, creature);
				}
			}
		}
		
		//如果可能主动攻击玩家的生物
		if (self.getReactType() == ReactType.REACT_AGGRESSIVE || self.getReactType() == ReactType.REACT_AGGRESSIVE_PLAYER) {
			Grid grid = self.getGrid();
			//从周围九大格获得所有玩家
			Iterator<Grid> gridIter = grid.noticeGridIterator();
			for (;gridIter.hasNext();) {
				Grid neib = gridIter.next();
				Iterator<ScenePlayer> playerIter = neib.playerIterator();
				for (;playerIter.hasNext();) {
					ScenePlayer player = playerIter.next();
					this.calcDistanceHatred(self, player);
				}
			}
		}

		//走向最大仇恨目标
		SceneElement target = this.getTarget();
		if (target != self.getTarget()) {
			self.setTarget(target);
			if (target != null) {
				self.moveToTarget(); //移动到目标
			}
		}

		// 如果找不到目标 则清空攻击列表
		if (target == null) {
//			if (me->GetHealth() < me->GetMaxHealth()) {
//				// 清空攻击关系列表
//				me->GetMap()->ClearCreatureHitHash(me->GetUIntGuid());
//			}
		}
		return true;
	}
	
	protected void calcDistanceHatred(SceneCreature self, SceneElement target) {
		//如果玩家已经存在仇恨则不需要再次增加距离仇恨
		if (this.targetHash.containsKey(target.getUintId())) {
			return;
		}
		
		if (target.isAlive() && self.isCanAttack(target) && self.isInActionSight(actionRadius) && self.isInSight(target, visionRadius)) {
			//初次出现在视野
			if (self.moveInSight(target)) {
				int dist = (int) self.getDistance(target);
				ThreatValue tv = new ThreatValue(target);
				tv.setDistantHatred(dist);
				this.targetHash.put(target.getUintId(), tv);
			}
		}
	}
	
	protected void updateThreadMap(SceneCreature self) {
		List<Integer> removedKey = new LinkedList<>();
		
		SceneElement target = self.getTarget();
		boolean inActionSight = self.isInActionSight(this.actionRadius);
		for (Entry<Integer, ThreatValue> info : targetHash.entrySet()) {
			SceneElement threater = self.getMap().findElement(info.getKey());
			if (threater == null) {
//				me->DelOwnerShip(iter->first);
				removedKey.add(info.getKey());
				continue;
			}
			
			if (threater.isAlive() && inActionSight) {
				if (self.isInSight(threater, this.visionRadius)) {
					float dist = (float) self.getDistance(threater);
					info.getValue().setDistantHatred(this.getThreatDistanceHatred(dist));
				} else {
					info.getValue().setDistantHatred(0);
				}
				continue;
			}
			
			removedKey.add(info.getKey());
			threater.delAttackTarget(info.getKey());
			if (threater.getElementType() == SceneElement.ELEMENT_TYPE_PLAYER) {
//				me->DelOwnerShip(threater->GetUIntGuid());
			}
			if (target == threater) {
				self.setTarget(null);
			}
		}
		
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
