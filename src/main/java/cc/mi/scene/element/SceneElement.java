package cc.mi.scene.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.binlog.stru.BinlogStruValueInt;
import cc.mi.core.binlog.stru.BinlogStruValueStr;
import cc.mi.core.constance.BinlogOptType;
import cc.mi.core.constance.SceneElementEnumFields;
import cc.mi.core.generate.stru.UnitBinlogInfo;
import cc.mi.core.utils.Mask;
import cc.mi.scene.grid.Grid;
import cc.mi.scene.server.SceneMap;

public abstract class SceneElement extends BinlogData {

	public static final int ELEMENT_TYPE_PLAYER		= 1;
	public static final int ELEMENT_TYPE_CREATURE	= 2;
	public static final int ELEMENT_TYPE_GAMEOBJECT	= 3;
	private final int elementType;
	
	protected float positionX;			//x坐标
	protected float positionY;			//y坐标
	protected float orient;				//朝向
	protected boolean canActiveGrid;	//能否激活Grid
	protected Grid grid;				//当前所属grid
	protected SceneMap map;				//当前所在地图

	protected int movingMills;			//移动完当前点需要消耗,等于0时处于静止状态
	protected Queue<Float> movingPath;	//移动路径
	protected int  movingLastDiff;		//累计时间差,用于减小精度误差

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
//	bool m_need_sync_pos;				//是否需要同步坐标到主玩家binlog
//
//	vector<spell_trigger> m_spell_trigger;
//	FriendlyMap			m_friendly_cache;//可以攻击的目标
//
//	PassiveSpellBinLogIndexMap m_index_spellId;	//binlogIndex对应的被动技能
//
//	PassiveSpellLevelMap m_passive_spell_level;	//被动技能和等级

	public SceneElement(int elementType) {
		//TODO: 这里填元素数据的长度
		super(
			SceneElementEnumFields.ELEMENT_INT_FIELDS_SIZE, 
			SceneElementEnumFields.ELEMENT_STR_FIELDS_SIZE
		);
		this.elementType = elementType;
	}

	public int getElementType() {
		return elementType;
	}
	
	public int getUintId() {
		//TODO: guid的uint表示
		return 0;
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

	public Queue<Float> getMovingPath() {
		return movingPath;
	}

	public void setMovingPath(Queue<Float> movingPath) {
		this.movingPath = movingPath;
	}

	public int getMovingLastDiff() {
		return movingLastDiff;
	}

	public void setMovingLastDiff(int movingLastDiff) {
		this.movingLastDiff = movingLastDiff;
	}
	
	public int getMapId() {
		//TODO:
		return 0;
	}
	
	public int getEntry() {
		//TODO:
		return 0;
	}
	
	public void update(int diff) {
		
	}
}
