package cc.mi.scene.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.binlog.stru.BinlogStruValueInt;
import cc.mi.core.binlog.stru.BinlogStruValueStr;
import cc.mi.core.constance.BinlogOptType;
import cc.mi.core.constance.SceneElementEnumFields;
import cc.mi.core.generate.stru.UnitBinlogInfo;
import cc.mi.core.impl.Tick;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.server.GuidManager;
import cc.mi.core.utils.Mask;
import cc.mi.core.utils.Path;
import cc.mi.core.utils.Point2D;
import cc.mi.scene.grid.Grid;
import cc.mi.scene.sceneMap.SceneMap;

public abstract class SceneElement extends BinlogData implements Tick {
	static final CustomLogger logger = CustomLogger.getLogger(SceneElement.class);
	
	static final int ONE_GRID_PX = 10;
	
	public static final int ELEMENT_TYPE_PLAYER		= 1;
	public static final int ELEMENT_TYPE_CREATURE	= 2;
	public static final int ELEMENT_TYPE_GAMEOBJECT	= 3;
	private final int elementType;
	
	protected float positionX;			//x坐标
	protected float positionY;			//y坐标
	protected float moveSpeed;			//移动速度
	protected float orient;				//朝向
	protected boolean canActiveGrid;	//能否激活Grid
	protected Grid grid;				//当前所属grid
	protected SceneMap map;				//当前所在地图

	protected int movingMills;			//移动完当前点需要消耗,等于0时处于静止状态
	protected Path movingPath;	//移动路径
	protected int movingLastDiff;		//累计时间差,用于减小精度误差
	
	private int intGuid = 0;
	protected boolean posSynchronous;		//是否需要同步坐标到主玩家binlog
	
//	uint32 m_last_victim_target;		//最后被我攻击的生物
//	UnitSet m_attacker_unit;					//生物攻击者
//	UnitSet m_attacker_player;					//玩家攻击者
//	Unit*  m_attacking;	
//	uint32 m_state;						//战斗状态	
//	uint32 m_cur_spell;					//当前魔法
//	uint32 m_cur_spell_lv;				//当前魔法等级
//	uint32 m_auto_spell;				//自动施法技能
//	float m_target_x;					//魔法释放区域
//	float m_target_y;
//	uint32 m_last_jump;					//上次跳跃时间

//	BufferManager *m_buff_manager;		//BUFF管理器

//	TimeTracker			m_live_timer;	//生存状态计时器
//	CycleTimer	m_spell_process;		//施法过程定时器
//
//	BOOL m_isActing;					//是否在表演状态
//	bool m_recalcul_Attr;				//是否需要重算属性
//	//bool m_is_del_threat;				//是否需清楚仇恨
//	
//	bool m_isBlowFly;					//是否被击飞
//
//	uint32 m_update_tick_num;
//
//	uint32 m_target_guid;				//选中目标的guid
//
//	vector<spell_trigger> m_spell_trigger;
//	FriendlyMap			m_friendly_cache;//可以攻击的目标
//
//	PassiveSpellBinLogIndexMap m_index_spellId;	//binlogIndex对应的被动技能
//
//	PassiveSpellLevelMap m_passive_spell_level;	//被动技能和等级

	public SceneElement(int elementType) {
		super(
			SceneElementEnumFields.ELEMENT_INT_FIELDS_SIZE, 
			SceneElementEnumFields.ELEMENT_STR_FIELDS_SIZE
		);
		this.elementType = elementType;
		this.setUInt8(SceneElementEnumFields.ELEMENT_INT_FIELD_ELEMENT_INFO, (short) 0, elementType);
	}
	
	
	public void onInit(String binlogId, int entry) {
		this.setElementGuid(binlogId);
		this.setEntry(entry);
		// 看看还需要怎么处理
	}
	
	public void setElementGuid(String guid) {
		this.setGuid(guid);
		intGuid = GuidManager.INSTANCE.getElementIntGuid(guid);
	}

	public void setEntry(int entry) {
		this.setUInt16(SceneElementEnumFields.ELEMENT_INT_FIELD_ENTRY, (short) 0, entry);
	}
	
	public int getEntry() {
		return this.getUInt16(SceneElementEnumFields.ELEMENT_INT_FIELD_ENTRY, (short) 0);
	}
	
	public int getElementType() {
		return elementType;
	}
	
	public int getUintId() {
		return this.intGuid;
	}
	
	public int getMapId() {
		return this.getInt32(SceneElementEnumFields.ELEMENT_INT_FIELD_MAP_ID);
	}

	public void setInstanceId(int instantId) {
		this.setInt32(SceneElementEnumFields.ELEMENT_INT_FIELD_ENTRY, instantId);
	}

	public int getInstanceId() {
		return this.getInt32(SceneElementEnumFields.ELEMENT_INT_FIELD_INSTANCE_ID);
	}

	public void setLineNo(int lineNo) {
		this.setInt32(SceneElementEnumFields.ELEMENT_INT_FIELD_LINE_NO, lineNo);
	}

	public int getLineNo() {
		return this.getInt32(SceneElementEnumFields.ELEMENT_INT_FIELD_LINE_NO);
	}

	public UnitBinlogInfo packNewElementBinlogInfo() {
		return this.packNewElementBinlogInfo(null, null);
	}

	public UnitBinlogInfo packNewElementBinlogInfo(Mask createIntMask, Mask createStrMask) {
		UnitBinlogInfo data = new UnitBinlogInfo();
		data.setState(BinlogOptType.OPT_NEW);
		data.setBinlogId(this.guid);
		data.setUintId(0);
		
		tmpIntMask.clear();
		int size = this.intValues.intSize();
		List<Integer> newIntList = new ArrayList<>(size);
		for (int i = 0; i < size; ++ i) {
			int value = this.intValues.getInt(i);
			if (value > 0 && (createIntMask == null || createIntMask.isMarked(i))) {
				newIntList.add(value);
				tmpIntMask.mark(i);
			}
		}
		data.setIntMask(tmpIntMask.toNewList());
		data.setIntValues(newIntList);
		
		tmpStrMask.clear();
		List<String> newStrList = new ArrayList<>(strValues.capacity());
		for (int i = 0; i < this.strValues.capacity(); ++ i) {
			String str = this.strValues.get(i);
			if (str != null && !"".equals(str) && (createStrMask == null || createStrMask.isMarked(i))) {
				newStrList.add(str);
				tmpStrMask.mark(i);
			}
		}
		data.setStrMask(tmpStrMask.toNewList());
		data.setStrValues(newStrList);
		
		return data;
	}
	
	public UnitBinlogInfo packUpdateElementBinlogInfo() {
		return this.packUpdateElementBinlogInfo(null, null);
	}
	
	public UnitBinlogInfo packUpdateElementBinlogInfo(Mask updateIntMask, Mask updateStrMask) {
		if (bsIntIndxHash.size() == 0 && bsStrIndxHash.size() == 0) {
			return null;
		}
		
		UnitBinlogInfo data = new UnitBinlogInfo();
		data.setState(BinlogOptType.OPT_UPDATE);
		data.setBinlogId("");
		data.setUintId(this.getUintId());
		
		// 先排序后处理		
		List<Integer> indice = new ArrayList<>(bsIntIndxHash.size());
		for (int indx : bsIntIndxHash.keySet()) {
			indice.add(indx);
		}
		Collections.sort(indice);
		tmpIntMask.clear();
		int size = this.intValues.intSize();
		List<Integer> newIntList = new ArrayList<>(size);
		for (int indx : indice) {
			BinlogStruValueInt bs = bsIntIndxHash.get(indx);
			if (updateIntMask == null || updateIntMask.isMarked(indx)) {
				tmpIntMask.mark(indx);
				newIntList.add(bs.getValue());
			}
		}
		data.setIntMask(tmpIntMask.toNewList());
		data.setIntValues(newIntList);
		
		// 先排序后处理		
		indice = new ArrayList<>(bsStrIndxHash.size());
		for (int indx : bsStrIndxHash.keySet()) {
			indice.add(indx);
		}
		Collections.sort(indice);
		tmpStrMask.clear();
		size = this.strValues.capacity();
		List<String> newStrList = new ArrayList<>(size);
		for (int indx : indice) {
			BinlogStruValueStr bs = bsStrIndxHash.get(indx);
			if (updateStrMask == null || updateStrMask.isMarked(indx)) {
				tmpStrMask.mark(indx);
				newStrList.add(bs.getValue());
			}
		}
		data.setStrMask(tmpStrMask.toNewList());
		data.setStrValues(newStrList);
		
//		bsIntIndxHash.clear();
//		bsStrIndxHash.clear();
		
		return data;
	}
	
	public UnitBinlogInfo packRemoveElementBinlogInfo() {
		UnitBinlogInfo data = new UnitBinlogInfo();
		data.setState(BinlogOptType.OPT_DELETE);
		data.setBinlogId("");
		data.setUintId(this.getUintId());
		
		data.setIntMask(new ArrayList<>());
		data.setIntValues(new ArrayList<>());
		
		data.setStrMask(new ArrayList<>());
		data.setStrValues(new ArrayList<>());
		
		return data;
	}

	public float getPositionX() {
		return positionX;
	}

	public void setPositionX(float positionX) {
		this.positionX = positionX;
	}

	public float getPositionY() {
		return positionY;
	}

	public void setPositionY(float positionY) {
		this.positionY = positionY;
	}
	
	public void setPosition(float x, float y) {
		this.setPositionX(x);
		this.setPositionY(y);
	}

	public float getOrient() {
		return orient;
	}

	public void setOrient(float orient) {
		this.orient = orient;
	}

	public boolean isCanActiveGrid() {
		return canActiveGrid;
	}

	public void setCanActiveGrid(boolean canActiveGrid) {
		this.canActiveGrid = canActiveGrid;
	}

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public SceneMap getMap() {
		return map;
	}

	public void setMap(SceneMap map) {
		this.map = map;
	}

	public int getMovingMills() {
		return movingMills;
	}

	public void setMovingMills(int movingMills) {
		this.movingMills = movingMills;
	}

	public int getMovingLastDiff() {
		return movingLastDiff;
	}

	public void setMovingLastDiff(int movingLastDiff) {
		this.movingLastDiff = movingLastDiff;
	}
	
	//如果毫秒数不等于0说明是移动状态
	public boolean isMoving() {
		return this.movingMills != 0;
	}

	public boolean update(int diff) {
		if (this.grid != null) {
//			UpdateSpellTrigger(diff);				//技能触发器更新
//			GetBuffManager()->Update(diff);
//			//if(!ScenedApp::g_app->IsPKServer())
//			UpdateLiveStatus(diff);
			this.updateLocate(diff);
		}
		
		return true;
	}

	public boolean updateLocate(int diff) {
		//非移动状态
		if (!this.isMoving()) {
			return false;
		}

		// 地图不对
		if (this.getMap().getMapId() != this.getMapId()) {
			logger.devLog("updateLocate this.getMap().getMapId() != this.getMapId() {} {} {} {} {} {}"
				, this.getMap().getMapId(), this.getMapId()
				, this.getEntry(), this.elementType, this.getGuid(), this.getName());
			return false;
		}

		//累计时间
		this.movingLastDiff += diff;

		//如果已经超过点了
		if (this.movingMills <= this.movingLastDiff) {
			Point2D<Float> fp = this.movingPath.pop();
			//设置为新坐标并弹出点
			this.setPosition(fp.getX(), fp.getY());
			//到达最后的点
			if (this.movingPath.isEmpty()) {
				//已经到达目标了
				this.movingMills = 0;
				this.movingLastDiff = 0;
				this.posSynchronous = true;
			} else {
				//计算一下上一点消耗了多少时间
				this.movingLastDiff -= this.movingMills;
				//计算下一个点需要消耗的时间
				fp = this.movingPath.front();
				float angle = this.getAngle(fp.getX(), fp.getY());
				float dist = this.getDistance(fp.getX(), fp.getY());
				this.setOrient(angle);
				// 一个格子10像素
				this.movingMills = (int) (dist * ONE_GRID_PX * 1000 / this.moveSpeed);
				if (this.movingMills == 0) {
					this.movingMills = 1;
				}
			}
		} else {
			this.movingMills -= this.movingLastDiff;
			//刷新坐标
			float range = (float)this.movingLastDiff * (float)this.moveSpeed / 1000 / ONE_GRID_PX;
			float distX = (float) (range * Math.cos(this.getOrient()));
			float distY = (float) (range * Math.sin(this.getOrient()));
			
			float x = this.getPositionX() + distX;
			float y = this.getPositionY() + distY;

			//验证坐标是否准确
			if (!this.getMap().isValidPosition((int)x, (int)y)) {
				logger.devLog("刷新 原坐标({},{}) 新坐标({},{})", this.getPositionX(), this.getPositionY(), x, y);
				x = this.getPositionX();
				y = this.getPositionY();
				if (this.isMoving()) {
					this.stopMoving(true);
				}
			}
			
			this.setPosition(x, y);
			this.movingLastDiff = 0;
		}

		return true;
	}

	protected float getAngle(float x, float y) {
		float dx = x - this.getPositionX();
		float dy = y - this.getPositionY();
		float angle = (float) Math.atan2(dy, dx);
		angle = (float) (angle >= 0 ? angle : Math.PI * 2 + angle);

		return angle;
	}
	
	protected float getDistance(float x, float y) {
		float dx = x - this.getPositionX();
		float dy = y - this.getPositionY();

		return (float) Math.sqrt(dx * dx + dy * dy);
	}
	
	public void stopMoving(boolean needSend) {
		this.posSynchronous = true;
		if (needSend) {		
//			WorldPacket pkt(MSG_MOVE_STOP);
//			pkt << GetUIntGuid() << uint16(GetPositionX()) << uint16(GetPositionY());
//			update_packet_len(pkt.GetPktPtr());
//			GetMap()->Broadcast(pkt.GetPkt(), this);		
		}
		this.movingMills = 0;
		this.movingPath = null;
	}
	
	public boolean isCreature() {
		return false;
	}
	
	public boolean isPlayer() {
		return false;
	}
	
	public boolean isGameObject() {
		return false;
	}
	
	public boolean isAlive() {
		// TODO:
		return false;
	}
	
	public boolean isCanMove() {
		// TODO:
		return false;
	}
	
	// 多少格每秒
	public float getMoveSpeed() {
		// 根据读表来的
		return 100;
	}
	
	// 是否有真视视野
	protected boolean hasRealSight() {
		return true;
	}
	
	public double getDistance(SceneElement target) {
		return Math.sqrt(Math.pow(this.positionX - target.positionX, 2) + Math.pow(this.positionY - target.positionY, 2));
	}
	
	public float getAngle(SceneElement target) {
		return this.getAngle(target.positionX, target.positionY);
	}
	
	public void moveTo(float x, float y) {
		Path path = new Path();
		path.addPath(new Point2D<Float>(x, y));
		this.moveTo(path, true);
	}
	
	/**
	 * 
	 * @param path
	 * @param stopForceSync	如果起点和终点一致, 是否需要强制发送停止移动包 (一般没什么用处, 就让它从a到a好了)
	 */
	public void moveTo(Path path, boolean stopMoveForceSync) {
		if (path.isEmpty()) {
			return;
		}
		
		if (this.movingPath == null || this.movingPath != path) {
			this.movingPath = path;
		}

		Point2D<Float> toPoint = this.movingPath.front();
		float angle = this.getAngle(toPoint.getX(), toPoint.getY());
		float range = this.getDistance(toPoint.getX(), toPoint.getY());
		
		this.movingMills = (int)(range * 10 * 1000 / this.getMoveSpeed());
		
		this.movingLastDiff = 0;
		// TODO:发送移动包
//		GetGrid()->unit_move_blocks.push_back(buff);
		if (!this.isMoving()) {
			this.movingPath = null;
		} else {
			this.setOrient(angle);
		}
	}
	
	public boolean isCanAttack(SceneElement target) {
		// TODO: 所有人可攻击
		return true;
	}
	
	public void onInSight(SceneElement target) {
		
	}
	
	public void onOffSight(SceneElement target) {
		
	}
	
	// 删除攻击目标
	public void delAttackTarget(int id) {
//		this->removePositionInfo(unit);
//
//		if(m_attacker_player.find(unit) != m_attacker_player.end())
//		{
//			m_attacker_player.erase(unit);
//			return;
//		}
//		if(m_attacker_unit.find(unit) != m_attacker_unit.end())
//		{
//			if (isDelOwnerShip)
//			{
//				Creature *attacker = dynamic_cast<Creature*>(m_map->FindUnit(unit));
//				if (attacker && GetTypeId() == TYPEID_PLAYER)
//					attacker->DelOwnerShip(GetUIntGuid());
//			}		
//			m_attacker_unit.erase(unit);
//			return;
//		}
	}
}
