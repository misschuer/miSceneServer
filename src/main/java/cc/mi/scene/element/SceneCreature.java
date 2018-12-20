package cc.mi.scene.element;

import java.util.LinkedList;
import java.util.Queue;

import cc.mi.core.gameData.TableCreature;
import cc.mi.scene.manager.ThreatManager;
import cc.mi.scene.movement.MovementBase;
import cc.mi.scene.movement.MovementFactory;

public class SceneCreature extends SceneElement {
	
	private float bornX;
	private float bornY;
	
	// 移动类型
	private int moveType;
	// 复活类型
	private int respawnType;
	// 复活时间
	private int respawnTime;
	// 主人guid
	private String host = null;
	// 攻击距离
	private int attackRange = 0;
	
	// 攻击的目标
	private SceneElement target = null;
	
	// 仇恨管理
	private ThreatManager threat = new ThreatManager();
	
	// 活动列表
	protected Queue<MovementBase> movementList = new LinkedList<>();
	
	public SceneCreature() {
		super(SceneElement.ELEMENT_TYPE_CREATURE);
	}
	
	public boolean update(int diff) {
		boolean ret = super.update(diff);
		if (!ret) return ret;

//		if(m_threat_move_type != IDLE_MOTION_TYPE)
//		{
//			m_timer_threat.Update(diff);
//			if(m_timer_threat.Passed())
//			{
//				m_threatMgr.Update(UPDATE_THREAT_TIMER);
//				m_timer_threat.Reset(UPDATE_THREAT_TIMER);
//			}	
//		}	
//
//		m_motion_timer_diff += diff;
//		m_motion_timer.Update(diff);//生物智能移动定时器
//		if(m_motion_timer.Passed())
//		{
//			m_motion_timer.Reset();	
//			UpdateMotion(m_motion_timer_diff);
//			m_motion_timer_diff = 0;
//		}

		return true;
	}
	
	public void updateMotion(int diff) {
		// 至少有一个元素
		if (!movementList.peek().update(this, diff) && movementList.size() > 1) {
			MovementBase curr = movementList.poll();
			curr.finalize(this);
		}
	}
	
	
	private boolean initBase(int entry) {
		// 先判断entry是否存在表中
		if (!TableCreature.INSTANCE.isDataExist(entry)) {
			return false;
		}
		
		// 设置属性
		
		// name 等
		this.setName(TableCreature.INSTANCE.getName(entry));
		return true;
	}
	
	public boolean create(String binlogId, int entry) {
		super.onInit(binlogId, entry);
		if (!this.initBase(entry)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 初始化其他的 如运动方式, 仇恨管理
	 */
	public void initAction() {
		MovementBase curr = MovementFactory.getMovement(this.getMoveType());
		curr.init(this, 0);
		this.movementList.add(curr);

		//初始化仇恨管理
		int visionRadius = TableCreature.INSTANCE.getVisionRadius(this.getEntry());
		int actionRadius = TableCreature.INSTANCE.getActionRadius(this.getEntry());
		threat.init(visionRadius, actionRadius);
	}
	
//	//走向目标
//	void Creature::MotionMoveTarget()
//	{
//		MovementGenerator *curr = NULL;
//
//		//如果栈顶已经是朝目标走,或者是idle(用于占位)
//		if((m_impl.top()->GetMovementGeneratorType() == m_threat_move_type) 
//			|| (m_impl.top()->GetMovementGeneratorType() == IDLE_MOTION_TYPE 
//				&& static_cast<IdleMovementGenerator*>(m_impl.top())->m_timer.GetInterval()>0))
//			return;
//
//		//如果生物的行动栈中是初始化的，那么先插入一个回家的
//		if(m_impl.size() == 1)
//		{
//			curr = Tea::SelectMovementGenerator(this,HOME_MOTION_TYPE);
//			curr->Initialize(*this);
//			m_impl.push(curr);
//		}
//
//		//向目标寻路追杀
//		curr = Tea::SelectMovementGenerator(this,MovementGeneratorType(m_threat_move_type));
//		//curr = Tea::SelectMovementGenerator(this,TARGET_MOTION_TYPE);
//		curr->Initialize(*this);	
//		m_impl.push(curr);
//	}

	public float getBornX() {
		return bornX;
	}

	public float getBornY() {
		return bornY;
	}

	public void setBornPos(float x, float y) {
		this.bornX = x;
		this.bornY = y;
	}

	public int getMoveType() {
		return moveType;
	}

	public void setMoveType(int moveType) {
		this.moveType = moveType;
	}

	public int getRespawnType() {
		return respawnType;
	}

	public void setRespawnType(int respawnType) {
		this.respawnType = respawnType;
	}

	public int getRespawnTime() {
		return respawnTime;
	}

	public void setRespawnTime(int respawnTime) {
		this.respawnTime = respawnTime;
	}
	
	@Override
	public boolean isCreature() {
		return true;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public SceneElement getTarget() {
		return target;
	}

	public void setTarget(SceneElement target) {
		this.target = target;
	}

	public int getAttackRange() {
		return attackRange;
	}

	public void setAttackRange(int attackRange) {
		this.attackRange = attackRange;
	}
}
