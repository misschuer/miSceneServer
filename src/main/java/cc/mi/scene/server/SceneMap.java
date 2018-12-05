package cc.mi.scene.server;

import java.util.HashMap;
import java.util.Map;

import cc.mi.core.constance.ObjectType;
import cc.mi.core.generate.msg.MapCreateMsg;
import cc.mi.core.generate.msg.MapDeleteMsg;
import cc.mi.core.impl.Tick;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.manager.MapTemplateManager;
import cc.mi.core.server.GuidManager;
import cc.mi.core.utils.Mask;
import cc.mi.core.utils.TimestampUtils;
import cc.mi.core.xlsxData.MapTemplate;
import cc.mi.scene.config.ServerConfig;
import cc.mi.scene.element.ScenePlayer;
import cc.mi.scene.grid.GridManager;
import cc.mi.scene.info.ParentMapInfo;

public class SceneMap implements Tick {
	static final CustomLogger logger = CustomLogger.getLogger(SceneMap.class);
	
	static final Map<Integer, ParentMapInfo> allParentMapInfoHash = new HashMap<>();
	
	static final Map<Integer, Map<Integer, SceneMap>> mapInstHash = new HashMap<>();
	
	private Map<String, ScenePlayer> playerHash = new HashMap<>();
	
	protected ParentMapInfo mapInfo;
	protected SceneMap parentInst;
	
	protected MapTemplate mapTemplate;
	protected int lootSiteWidth ;
	protected int lootSiteHeight;
	protected Mask lootSite;
	
	protected final int mapId;
	protected final int instId;
	protected final int lineNo;
	
	protected GridManager gridManager;
	
	// 是否全图grid
	protected boolean isBroadcastMap = false;
	// 是否关闭复活
	protected boolean isCloseRespawnMap = false;
	
	
	public SceneMap(int mapId, int instId, int lineNo, String ext) {
//		m_parent_map_info(0),m_template(NULL),m_grids(NULL),m_is_can_jump(true)
//		,m_can_recovry(true),m_script_callback_key(0),m_parent(0),m_end_time(0)
//		,m_broadcast_nogrid(false),m_is_close_respawn(false),m_can_castspell(true)
		
//		m_template = MapTemplate::GetMapTempalte(mapid);
//		ASSERT(m_template);	
		
		this.mapId  =  mapId;
		this.instId = instId;
		this.lineNo = lineNo;
		
		this.mapTemplate = MapTemplateManager.INSTANCE.getTemplate(mapId);
		if (this.mapTemplate == null) {
			throw new RuntimeException(String.format("scene map %d is no template", mapId));
		}
		
		if (!allParentMapInfoHash.containsKey(instId)) {
			
			this.mapInfo = new ParentMapInfo();
			this.mapInfo.setBinlogId(GuidManager.INSTANCE.makeNewGuid(ObjectType.MAP));
			this.mapInfo.setMapId(mapId);
			this.mapInfo.setInstId(instId);
			this.mapInfo.setLineNo(lineNo);
			this.mapInfo.setCreateTime(TimestampUtils.now());
			this.mapInfo.setInstType(this.mapTemplate.getBaseInfo().getType());
			
			allParentMapInfoHash.put(instId, this.mapInfo);
		} else {
			
			this.mapInfo = allParentMapInfoHash.get(instId);
		}
		
//		m_parent = this;
//		for (uint8 i = 0;i<MAX_EQUIP_ATTR;i++)
//		{
//			m_mapAttrBonus[i] = 0;
//		}
		this.load();
	}
	
	private boolean isAvailableLootSite(int start) {
		
		int width  = this.mapTemplate.getBaseInfo().getWidth();
		for (int y = 0; y < ServerConfig.LOOT_AREA; ++ y) {
			for (int x = 0; x < ServerConfig.LOOT_AREA; ++ x) {
				//若可用，置为true，并结束循环
				if (!this.mapTemplate.isValidPosition(start + y * width + x)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private void doGetInitMapData() {
//		// 是否全图grid
//		protected boolean isBroadcastMap = false;
//		// 是否关闭复活
//		protected boolean isCloseRespawnMap = false;
	}
	
	private boolean load() {
		
		//初始化地图坐标的使用状态
		int width  = this.mapTemplate.getBaseInfo().getWidth();
		int heigth = this.mapTemplate.getBaseInfo().getHeight();
		//初始化变量
		this.lootSiteWidth  = Math.floorDiv(width , ServerConfig.LOOT_AREA);
		this.lootSiteHeight = Math.floorDiv(heigth, ServerConfig.LOOT_AREA);
		
		//初始化所有放置战利品的格子，并设置之
		lootSite = new Mask(this.lootSiteWidth * this.lootSiteHeight);
		int bit = 0;
		for (int siteY = 0; siteY < lootSiteHeight; ++ siteY) {
			for (int siteX = 0; siteX < lootSiteWidth; ++ siteX) {
				int start = (siteY * this.lootSiteWidth + siteX) * ServerConfig.LOOT_AREA;
				if (this.isAvailableLootSite(start)) {
					lootSite.mark(bit);
				}
				bit ++;
			}
		}

		//设置地图脚本
//		DoSelect_Instance_Script(this);
		//初始获得是否全图广播配置
		this.doGetInitMapData();
		
		//初始化网格
		if (this.isBroadcastMap) {
			this.gridManager = new GridManager(this, width, heigth, 65535);	//grid超大，相当于全图广播
		} else {
			this.gridManager = new GridManager(this, width, heigth);
		}
		
//		//m_teleports
//		for (auto iter_t = m_template->m_teleport.begin()
//			;iter_t!=m_template->m_teleport.end();++iter_t)
//		{		
//			GameObject *tele = new GameObject;		
//			tele->Create(Map::CreateNewCreatureID(),iter_t->temp_id);
//			ASSERT(tele->IsTeleteport());			
//
//			tele->SetName(iter_t->name);
//			tele->SetMapId(GetMapId());
//			tele->SetInstanceId(GetInstanceID());
//			tele->SetPosition((float)iter_t->x,(float)iter_t->y);
//
//			tele->SetToPositionX(float(iter_t->to_x));
//			tele->SetToPositionY(float(iter_t->to_y));
//			tele->SetToMapId(iter_t->to_mapid);
//
//			AddGameObject(tele);
//		}
//
//		//刷怪	
//		for (auto iter = m_template->m_monsters.begin();
//			iter!=m_template->m_monsters.end();++iter)
//		{
//			creature_template *temp = creature_template_db[iter->second.templateid];
//			uint32 rebornTime = temp->rebornTime;
//			AddCreature(iter->second.templateid,
//				float(iter->second.x),float(iter->second.y),float(iter->second.toward),
//				rebornTime,
//				-1,
//				iter->second.flag,
//				iter->second.alias_name);
//		}
//
//		//刷游戏对象	
//		for (auto iter = m_template->m_gameobjects.begin();
//			iter != m_template->m_gameobjects.end();++iter)
//		{
//			GameObject *go = new GameObject;		
//			bool b = go->Create(Map::CreateNewCreatureID(),iter->templateid);
//			if(b){
//				go->SetPosition(float(iter->x),float(iter->y));	
//				go->SetOrientation(iter->toward);
//				AddGameObject(go);
//			}
//			else
//			{
//				delete go;
//			}
//		}	
//
//		//为了便于脚本取生物对象,这一帧就直接处理玩完加入
//		UpdateRespan(0);
//
//		//初始化地图脚本并且设置地图剧本	
//		DoInitMapScript(this);
//		// 初始化地图对象
//		DoInitMapWorldObject(this);
//		//初始化地图硬盘数据
//		DoInitReadHddMapData(this);
		return true;
	}

	public GridManager getGridManager() {
		return gridManager;
	}

	public int getMapId() {
		return mapId;
	}

//	void Map::Update(uint32 diff)
//	{	
//		ASSERT(m_grids);
//		m_grids->Update(diff);
//
//		UpdateRespan(diff);	
//
//		//跑定时器
//		if(diff < 1000 && !m_script_callbacks.empty() && !m_state_script.empty())
//		{
//			for(ScriptCBList::iterator it = m_script_callbacks.begin(); it != m_script_callbacks.end();)
//			{
//				script_timer_callback *stc = *it;
//
//				if(stc->invalid == 1)//已被删除
//				{
//					m_script_callbacks.erase(it++);
//					free(stc);
//					continue;
//				}
//				if(stc->is_new == 1)//下一帧再执行
//				{
//					stc->is_new = 0;
//				}
//				else
//				{
//					stc->timer.Update(diff);
//					if(stc->timer.Passed())
//					{
//						if(DOTimerTickCallBack(this, stc->s_callback, stc->param1) == 0)
//							stc->invalid = 1;
//						stc->timer.Reset2();
//					}
//				}
//				++it;
//			}
//		}
//
//		//跑定时器
//		if(!m_script_timestamp_callbacks.empty())
//		{
//			for(ScriptTimeStampList::iterator it = m_script_timestamp_callbacks.begin(); it != m_script_timestamp_callbacks.end();)
//			{
//				script_timestamp_timer_callback *stc = *it;
//
//				if(stc->invalid == 1)//已被删除
//				{
//					m_script_timestamp_callbacks.erase(it++);
//					free(stc);
//					continue;
//				}
//				if(stc->is_new == 1)//下一帧再执行
//				{
//					stc->is_new = 0;
//				}
//				else
//				{	
//					if(stc->timestamp > 0 && (uint32)time(NULL) >= stc->timestamp)
//					{
//						DOTimerTickCallBack(this, stc->s_callback, stc->param1);
//						stc->invalid = 1;					
//					}
//					else if (stc->timestamp == 0)
//					{
//						stc->invalid = 1;
//					}
//				}
//				++it;
//			}
//		}
//
//		//处理需要复活的游戏对象，在玩家那跳心跳 好处不必要的可以不进数组
//		for (PlayerRespawnList::iterator it_p = m_player_respawn.begin();
//			it_p != m_player_respawn.end();)
//		{
//			Player *player = FindPlayer(*it_p);
//			if (player && !player->isAlive())
//			{
//				//TODO:这里可以根据策划要求写复活逻辑
//				player->Respawn();
//				player->DoIfNeedAddProtectBuff();
//			}
//			if (player && player->isAlive())
//				it_p = m_player_respawn.erase(it_p);
//			else
//				++it_p;
//		}
//
//		//TODO: 如果效率不行再说 => 处理需要gameobject
//		for (GameObjectMap::iterator iter = m_gameobjects.begin();iter != m_gameobjects.end();++iter)
//		{
//			if (!iter->second) {
//				return;
//			}
//			iter->second->UpdateLiveStatus(diff);
//		}
//	}
//
//	void Map::UpdateRespan(uint32 diff)	
//	{
//		//将待插入grid的
//		if(!m_worldObject_toadd.empty())
//		{	
//			Unit *wo;
//			for (auto it = m_worldObject_toadd.begin();it != m_worldObject_toadd.end();++it)
//			{
//				wo = *it;
//				//如果坐标非法,直接不加入地图
//				if(!IsRightCoordNoCanRun(wo->GetPositionX(),wo->GetPositionY()))
//				{
//					tea_perror("生物非法坐标[%s] :%u,%.2f,%.2f",wo->GetName().c_str(),GetMapId(),wo->GetPositionX(),wo->GetPositionY());
//					continue;
//				}
//
//				wo->SetMap(this);
//				wo->SetMapId(GetMapId());
//				wo->SetInstanceId(GetInstanceID());
//				if(wo->GetTypeId() == TYPEID_UNIT)
//				{
//					m_alive_creatures[wo->GetGuid()] = static_cast<Creature*>(wo);
//				}		
//				//生物别名,用于AI脚本查找对象
//				if(!wo->GetAliasName().empty())
//					AddAliasCreature(wo);	
//				//加入grid
//				m_grids->AddWorldObject(wo);
//			}
//			m_worldObject_toadd.clear();
//		}	
//
//		//处理只刷一次的生物对象
//		if(!m_to_del_creature.empty())
//		{
//			for (CreatureSet::iterator it = m_to_del_creature.begin();
//				it != m_to_del_creature.end();++it)
//			{
//				/*m_grids->DelWorldObject(*it);		
//				m_alive_creatures.erase((*it)->GetGuid());			
//				if(!(*it)->m_alias_name.empty())
//					DelAliasCreature(*it);*/
//				LeaveCreature(*it);		
//
//				delete *it;
//			}
//			m_to_del_creature.clear();	
//		}	
//
//		//处理需要删掉的游戏对象,动态对象全部由此管理,暂时只有战利品
//		for (GameObjectRespawnList::iterator it = m_go_dynamic.begin();
//			it != m_go_dynamic.end();)
//		{
//			it->first -= diff;
//			GameObjectMap::iterator it_go = m_gameobjects.find(it->second);
//			if(it_go == m_gameobjects.end())
//			{
//				it = m_go_dynamic.erase(it);
//				continue;
//			}
//			GameObject *go = it_go->second;
//
//			//超时的
//			if (it->first < 0	|| (!go->IsGEAR()))		
//			{
//				//			
//				DeleteGameObject(go);			
//				it = m_go_dynamic.erase(it);
//			}
//			else ++it;		
//		}
//	}
//
//	void Map::AddRespanPlayer(Player* player)
//	{
//		//如果副本关闭超时复活，不进心跳
//		if(m_is_close_respawn) return;
//		m_player_respawn.push_back(player->GetUIntGuid());
//	}
	
	/**
	 * 发送地图数据
	 * @param contextPlayer
	 */
	public void sendCreateBlock(SceneContextPlayer contextPlayer) {
		this.sendCreateBlock(contextPlayer.getContext().getFd());
	}

	/**
	 * 发送地图数据
	 * @param contextPlayer
	 */
	public void sendCreateBlock(int fd) {
		if (fd == 0) {
			return;
		}
		
		MapCreateMsg packet = new MapCreateMsg();
		packet.setBaseFd(fd);
		packet.setMapInfo(mapInfo.toSendMapInfo());
		SceneServerManager.getInstance().sendToGate(packet);
	}

	public void sendDeleteBlock(SceneContextPlayer player) {
		this.sendDeleteBlock(player.getContext().getFd());
	}

	public void sendDeleteBlock(int fd) {
		if (fd == 0) {
			return;
		}
		
		MapDeleteMsg packet = new MapDeleteMsg();
		packet.setBaseFd(fd);
		packet.setBinlogId(this.mapInfo.getBinlogId());
		SceneServerManager.getInstance().sendToGate(packet);
	}

//	const string Map::CreateNewCreatureID()
//	{
//		string guid = g_GuidMgr.MAKE_NEW_GUID(ObjectTypeUnit);
//		return guid;
//	};
//
//	bool Map::IsCanRun(float x, float y)
//	{
//		////四舍五入先
//		if(x < 0.00f || y < 0.00f) return false;
//		uint16 integer_x = uint16(x); 
//		uint16 integer_y = uint16(y);
//		//首先不能超出地图范围
//		if( x < 0.0f || x >= float(m_template->m_baseinfo.width))
//			return false;
//		if( y < 0.0f || y >= float(m_template->m_baseinfo.height))
//			return false;
//		////也不能是障碍点
//		return m_template->IsCanRun(integer_x,integer_y);
//	}
//
//	bool Map::IsRightCoord(float x,float y)
//	{
//		//首先不能超出地图范围
//		if( x < 0.0f || x >= float(m_template->m_baseinfo.width))
//			return false;
//		if( y < 0.0f || y >= float(m_template->m_baseinfo.height))
//			return false;
//		return true;
//	}
//
//	bool Map::IsRightCoordNoCanRun(float x,float y)
//	{
//		////四舍五入先
//		if(x < 0.00f || y < 0.00f) return false;
//		uint16 integer_x = uint16(x); 
//		uint16 integer_y = uint16(y);
//		//首先不能超出地图范围
//		if( x < 0.0f || x >= float(m_template->m_baseinfo.width))
//			return false;
//		if( y < 0.0f || y >= float(m_template->m_baseinfo.height))
//			return false;
//		return true;
//	}

	public void joinPlayer(ScenePlayer player) {
		
		if (player.getInstanceId() != 0) {
			throw new RuntimeException(String.format("joinPlayer guid = %s instId = %d != 0", player.getGuid(), player.getInstanceId()));
		}
		player.setInstanceId(this.instId);
		player.getContextPlayer().setInstanceId(this.instId);
		player.setMap(this);
		
		this.sendCreateBlock(player.getContextPlayer());
		this.gridManager.addPlayer(player);
		
		playerHash.put(player.getGuid(), player);

		if (player.getInstanceId() != this.instId) {
			throw new RuntimeException(String.format("joinPlayer err instId"));
		}
		
		if (player.getMapId() != this.mapId) {
			throw new RuntimeException(String.format("joinPlayer err mapId"));
		}

		this.onJoinMapBefore(player);
		
		player.onJoinMap();

		this.onAfterJoinPlayer(player);
	}
	
	protected void onJoinMapBefore(ScenePlayer player) {
		
	}
	
	protected void onAfterJoinPlayer(ScenePlayer player) {
		
	}

//	void Map::AddGameObject(GameObject *go)
//	{
//		ASSERT(go);	
//		go->SetMap(this);
//		go->SetMapId(GetMapId());
//		go->SetInstanceId(GetInstanceID());
//
//		//判断是否重复ADD
//		ASSERT(m_gameobjects.find(go->GetGuid()) == m_gameobjects.end() || m_gameobjects.find(go->GetGuid())->second != go);
//		//判断GUID是否重复
//		ASSERT(m_gameobjects.find(go->GetGuid()) == m_gameobjects.end());
//
//		m_grids->AddWorldObject(go);
//		m_gameobjects[go->GetGuid()] = go;
//
//		//if (go->IsDynamic())
//		//{
//		//	m_go_dynamic.push_back(std::make_pair(g_Config.loot_exist_timer + go->GetInt32(GO_LOOT_EXIST_TIME_FLOAT) * 1000, go->GetGuid()));
//		//}	
//	}
//
//	//设置战利品网格
//	void Map::SetMapSite(float x, float y)
//	{
//		int set_bit = int(y / m_loot_area) * m_loot_site_width + int(x / m_loot_area);
//		ASSERT(set_bit < m_all_site.GetCount());
//		if(m_all_site.GetBit(set_bit) == false)
//		{
//			m_all_site.SetBit(set_bit);
//			ASSERT(m_all_enpty_count != 0);
//			m_all_enpty_count--;
//		}
//	}
//
//	//战利品网格设置为未用
//	void Map::UnSetMapSite(float x, float y)
//	{
//		m_all_site.UnSetBit(uint32(y / m_loot_area) * m_loot_site_width + uint32(x / m_loot_area));
//		m_all_enpty_count++;
//	}
//
//	void Map::DeleteGameObject(GameObject *go)
//	{		
//		ASSERT(go && go->GetMap()==this );
//		//ASSERT(go->GetGrid());
//
//		m_gameobjects.erase(go->GetGuid());	
//
//		//因为插入需要心跳处理，有可能未成功加入还没有grid信息
//		if(go->GetGrid())
//			m_grids->DelWorldObject(go);
//
//		//设置战利品网格
//		if(go->CanLoot())
//		{
//			UnSetMapSite(go->GetPositionX(), go->GetPositionY());
//		}
//
//		ASSERT(m_gameobjects.find(go->GetGuid()) == m_gameobjects.end());
//
//		delete go;
//	}
//
//	bool Map::AddCreature(Creature *creature)
//	{
//		if(!IsRightCoordNoCanRun(creature->GetPositionX(),creature->GetPositionY()))
//			return false;
//		m_worldObject_toadd.push_back(creature);
//		return true;
//	}
//
//	Creature *Map::AddCreature(uint32 templateid,float x,float y,float toward/* = 0*/,uint32 respan_time /*= 0*/,uint32 movetype/*=0*/, uint32 npcflag/* = 0*/,const char *alias_name /*= NULL*/,bool active_grid /*= false*/,uint8 faction/*= 0*/,const char* ainame/* = 0*/, uint32 level/* = 0*/, uint32 attackType/* = 0*/, uint32 riskId/* = 0*/)
//	{
//		string lguid = CreateNewCreatureID();
//		Creature *new_creature = new Creature;
//		new_creature->SetUInt32(UNIT_INT_FIELD_RISK_CREATURE_ID, riskId);
//		creature_template *temp = creature_template_db[templateid];
//		if (movetype == uint32(-1)) {
//			movetype = temp->move_type;
//		}
//		//需要确保地图怪物刷新点不删除		
//		if(!new_creature->Create(this,lguid,templateid,respan_time,movetype,ainame,level, attackType))
//		{
//			safe_delete(new_creature);
//			return NULL;
//		}
//
//		
//		if (temp->npcflag == 0 && temp->monster_type == 1) {
//			new_creature->SetUnitFlags(UNIT_FIELD_FLAGS_IS_BOSS_CREATURE);
//		}
//		new_creature->SetBornPos(x,y);
//		new_creature->SetPosition(x,y);
//		new_creature->SetOrientation((float)toward);
//		new_creature->SetBodyMissTime(temp->body_miss);
//
//		if(alias_name != NULL && strlen(alias_name) != 0)
//			new_creature->SetAliasName(alias_name);
//		new_creature->SetFaction(faction);
//		new_creature->Initialize();
//		//激活grid
//		if(active_grid)
//			new_creature->SetCanActiveGrid(true);
//		//新对象清理掉更新包
//		new_creature->Clear();
//		//如果添加失败返回空
//		if(!AddCreature(new_creature))
//		{
//			safe_delete(new_creature);
//			return NULL;
//		}
//		return new_creature;
//	}
//
//	Creature *Map::AddCreatureTravelers(string &data,float x,float y, uint32 movetype,const char *alias_name)
//	{
//
//		Creature *new_creature = new Creature;
//
//		//需要确保地图怪物刷新点不删除		
//		new_creature->Create(0,0,0);
//		string int_str = data.c_str();
//		string str_str = "";
//		new_creature->FromString(int_str, str_str);
//		new_creature->SetBornPos(x,y);
//		new_creature->SetPosition(x,y);
//
//		new_creature->SetMap(this);
//		new_creature->SetMapId(GetMapId());
//		new_creature->SetInstanceId(GetInstanceID());
//
//		new_creature->Initialize();	
//
//		//生物别名,用于AI脚本查找对象
//		if(alias_name != NULL && strlen(alias_name)>0)	
//			new_creature->SetAliasName(alias_name);
//
//		//如果添加失败
//		if(!AddCreature(new_creature))
//			safe_delete(new_creature);
//
//		return new_creature;
//	}
//
//	void Map::ToDelCreature(Creature *creature)
//	{	
//		//要把生物指针还给地图管理
//		if(creature->GetHostGUID())			
//			creature->SetHostGUID(NULL);	
//		m_to_del_creature.insert(creature);
//	}
//
//	Creature* Map::FindCreature(uint32 guid)	
//	{	
//		auto *unit = Unit::FindUnit(guid);
//		if(!unit)
//			return NULL;
//		if(unit->GetTypeId() != TYPEID_UNIT)
//			return NULL;
//		return static_cast<Creature*>(unit);	
//	}
//
//	GameObject* Map::FindWorldObject(uint32 guid)
//	{
//		auto *unit = Unit::FindUnit(guid);
//		if(!unit)
//			return NULL;
//		if(unit->GetTypeId() != TYPEID_GAMEOBJECT)
//			return NULL;
//		return static_cast<GameObject*>(unit);
//	}
//
//	Unit* Map::FindUnit(uint32 guid)
//	{		
//		Unit *unit = Unit::FindUnit(guid);
//		if (!unit)
//			return NULL;
//
//		if(unit->GetMap() == this)
//			return unit;
//
//		return NULL;
//	}
//
//	Player* Map::FindPlayer(uint32 guid)
//	{
//		auto *unit = Unit::FindUnit(guid);
//		if(!unit)
//			return NULL;
//		if(unit->GetTypeId() != TYPEID_PLAYER)
//			return NULL;
//		return static_cast<Player*>(unit);
//	}
//
//	Player* Map::FindPlayer(const char* guid)
//	{
//		ScenedContext* session = dynamic_cast<ScenedContext*>(ObjMgr.Get(guid));	
//		return session?session->GetPlayer():nullptr;
//	}

	public void leavePlayer(ScenePlayer player) {
		//当玩家离开地图时,如果有宠物 应该也要触发一下事件
		logger.devLog("player guid={} leave mapid {},instanceid {}", 
				player.getGuid(), player.getMapId(), player.getInstanceId());
		
		player.onLeaveMap();
		if (this.gridManager != null) {
			this.gridManager.delPlayer(player);
		} else {
			logger.devLog("player guid={} leave mapid {}, no gridManager", 
					player.getGuid(), player.getMapId());
		}
		
		//移除
		playerHash.remove(player.getGuid());
//		player->SyncUnitToPlayerData();
		if (player.getContextPlayer() != null) {
			this.sendDeleteBlock(player.getContextPlayer());
		}
		//地图设为空
		player.setMap(null);
		//如果玩家离开的地图不是副本,则记录为数据库坐标
//		if (player->isAlive()) {
//			if((!GetMapTemp()->IsInstance() || DoIsRiskMap(this->GetMapId())) && !player->GetSession()->IsKuafuPlayer()) {
//				// 如果是从幻境小关卡离开的就记录一个标记就行
//				if (DoIsRiskMap(this->GetMapId())) {
//					player->GetSession()->SetLastRiskFlag(1);
//				} else {
//					player->GetSession()->SetLastRiskFlag(0);
//					player->GetSession()->SetToDBPositon(player->GetMapId(),player->GetPositionX(),player->GetPositionY());
//				}
//			}
//		} else {
//			//TODO: 判断下死亡离开需不需要回城
//			float nx,ny;
//			float x = ZHUCHENG_FUHUO_X, y = ZHUCHENG_FUHUO_Y;
//			auto *mt = MapTemplate::GetMapTempalte(ZHUCHENG_DITU_ID);
//			for (uint8 i=1; i<10; i++)
//			{
//				MovementGenerator::RandomPos(nx,ny,float(x),float(y),float(irand(1,10000)/1000));
//				if(mt->IsCanRun(uint16(nx),uint16(ny)))
//				{
//					x = nx;
//					y = ny;
//					break;
//				}
//			}
//			player->GetSession()->SetToDBPositon(ZHUCHENG_DITU_ID, x, y);
//		}
	}

//	void Map::LeaveCreature(Creature *creature)
//	{
//		ASSERT(creature);
//		if(creature->GetGrid())
//			m_grids->DelWorldObject(creature);		
//		m_alive_creatures.erase(creature->GetGuid());	
//		
//		if(!creature->GetAliasName().empty())
//			DelAliasCreature(creature);	
//	}
//
//	//初始化场次id和跨服id
//	void Map::InitWaridAndKuafuType(ScenedContext* session)
//	{
//		if(session->GetDBKuafuInfo().empty())
//			return;
//
//		m_parent_map_info->SetUInt32(MAP_INT_FIELD_KUAFU_TYPE, session->GetKuafuType());
//		m_parent_map_info->SetUInt32(MAP_INT_FIELD_WARID, session->GetKuafuWarId());
//	}
//
//	//广播数据包
//	void Map::Broadcast(packet& pkt,Unit *wo) const
//	{
//		ASSERT(wo);
//		if(!wo->GetGrid())
//			return;
//
//		wo->GetGrid()->Broadcast(pkt);
//		//m_grids->Broadcast(pkt,wo->GetGrid());
//	}
//
//	void Map::Broadcast(packet& pkt,Player *player,bool self /*= false*/) const
//	{
//		ASSERT(player);
//		if(!player->GetGrid())
//			return;
//
//		//全图广播
//		//if(m_broadcast_nogrid)
//		//{
//		//	for (PlayerSet::iterator it = m_players.begin();
//		//		it != m_players.end();++it)
//		//	{
//		//		if(*it == player && self == false)
//		//			continue;
//		//		(*it)->GetSession()->SendPacket(pkt);
//		//	}
//		//	
//		//}
//		//else
//		player->GetGrid()->Broadcast(pkt,player,self);
//	}
//
//	void Map::BroadcastMap(packet& pkt) const {
//		for (auto it : m_players) {
//			it.second->GetSession()->SendPacket(pkt);
//		}
//	}
//
//	void Map::AddAliasCreature(Unit *unit)
//	{
//		ASSERT(!unit->GetAliasName().empty());
//		m_aliasObjs.insert(std::make_pair(unit->GetAliasName(),unit));
//	}
//	void Map::DelAliasCreature(Unit *unit)
//	{
//		ASSERT(!unit->GetAliasName().empty());
//		AliasObjectMMap::iterator it = m_aliasObjs.lower_bound(unit->GetAliasName());
//		AliasObjectMMap::iterator end = m_aliasObjs.upper_bound(unit->GetAliasName());
//		for(; it != end; ++it)
//		{
//			if(it->second == unit)
//			{
//				m_aliasObjs.erase(it);
//				return;
//			}
//		}
//	}
//
//	// 在怪物丢失目标 或者死了的时候才会清理
//	void Map::ClearCreatureHitHash(uint32 uintguid) {
//		this->creatureHitHash.erase(uintguid);
//	}
//
//	void Map::SendGridUnitFromPlayer(Player* player)
//	{
//
//		Grid& grid = m_grids->GridCoord(player->GetPositionX(),player->GetPositionY());
//
//		//将网格上面的其他玩家信息发给他
//		grid.SendAllNoticeGridToNewPlayer(m_grids, player);
//	}
//
//	void Map::SendMapPkt(uint32 mapid, const string &general_id, uint8 typ, std::function<void(ByteArray&)> fun)
//	{
//		ByteArray *bytes = ObjMgr.GridMallocByteArray();
//		*bytes << 0u << mapid << general_id<< GetInstanceID() << GetMapId() << GetGeneralID() << typ ;
//		if(fun)
//			fun(*bytes);
//		WorldPacket pkt(INTERNAL_OPT_MAP_ROUTER_PKT);
//		bytes->position(0);
//		packet_write(pkt.GetPktPtr(),(char*)bytes->cur_data(),bytes->bytesAvailable());
//		ObjMgr.GridFreeByteArray(bytes);
//		ScenedApp::g_app->SendToLogind(pkt);
//	}
//
//	void Map::HandleMapPkt(ByteArray &bytes)
//	{
//		uint32 src_map_id, src_instance_id;
//		string general_id, src_general_id;
//		uint8 typ;
//		bytes >> general_id >> src_instance_id >> src_map_id >> src_general_id >> typ;
//		switch (typ)
//		{
//		case MAP_PKT_TYPES_CREATURE_TELEPORT:
//			OnTeleportCreature(bytes);
//			break;
//		default:
//			ASSERT(false);
//			break;
//		}
//	}
//
//	void Map::TeleportCreature(const string &guid, uint32 mapid, float x,float y, const string &general_id)
//	{
//		uint32 uguid = atol(SUB_GUID_STR_TO_INT(guid).c_str());
//		Creature *creature = dynamic_cast<Creature*>(FindCreature(uguid));
//		ASSERT(creature);
//		SendMapPkt(mapid, general_id, MAP_PKT_TYPES_CREATURE_TELEPORT, [creature, x, y](ByteArray &bytes){
//			creature->WriteCreateBlock(bytes);
//			bytes << x << y << uint8(creature->CanActiveGrid() ? 1 : 0);
//		});
//		ToDelCreature(creature);
//	}
//
//	void Map::OnTeleportCreature(ByteArray &bytes)
//	{
//		string guid;
//		uint8 flag;
//		bytes >> flag >> guid;
//		Creature *new_creature = new Creature;
//		new_creature->ReadFrom(flag, bytes);
//		string lguid = CreateNewCreatureID();
//		new_creature->SetGuid(lguid);
//
//		//读位置信息开始，位置信息在这里是没用的
//		uint16 path_len = bytes.readUnsignedShort();
//		for (int i = 0; i < path_len; i++)
//		{
//			bytes.readUnsignedShort();
//		}
//		bytes.readFloat();
//		bytes.readFloat();
//		bytes.readFloat();
//		//读位置信息结束
//
//		//位置信息
//		new_creature->SetPosition(bytes.readFloat(), bytes.readFloat());
//		//是否激活grid
//		new_creature->SetCanActiveGrid(bytes.readByte() != 0);
//		//初始化生物
//		new_creature->Initialize();
//		//加入地图
//		AddCreature(new_creature);
//	}
//
//	//获取所有游戏对象
//	void Map::GetAllGameObjectByEntry(vector<GameObject *> &vect, uint32 entry)
//	{
//		for (GameObjectMap::iterator iter = m_gameobjects.begin();iter != m_gameobjects.end();++iter)
//		{
//			if (!iter->second)
//				continue;
//			if(entry != 0 && iter->second->GetEntry() != entry)
//				continue;
//			
//			vect.push_back(iter->second);
//		}
//	}
//
//	GameObject *Map::GetGameObjectByGuid(uint32 target) {
//		char ch[23];
//		sprintf(ch, "%c%u", ObjectTypeUnit, target);
//		string teleGuid = ch;
//		auto it = m_gameobjects.find(teleGuid);
//		if (it == m_gameobjects.end()) {
//			return nullptr;
//		}
//		GameObject* tele = m_gameobjects[teleGuid];
//
//		return tele;
//	}
	
	public static SceneMap createInstance (int instId, int mapId, int lineNo, String ext) {
		
		SceneMap inst = null;
		//判断父级地图是否存在
		MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(mapId);
		int parentId = mt.getBaseInfo().getParentId();
		if (parentId == mapId) {	//如果是父级直接创建
			//如果是副本则创建不同的对象实例
			inst = mt.isInstance() ? new SceneInstanceMap(mapId, instId, lineNo, ext) : new SceneMap(mapId, instId, lineNo, ext);
		} else {
			mt = MapTemplateManager.INSTANCE.getTemplate(parentId);
			SceneMap parent = SceneMap.findInstance(instId, parentId);
			if (!SceneMap.containsInstance(instId, parentId)) {
				parent = SceneMap.createInstance(instId, mapId, lineNo, ext);
			}

			//如果是副本则创建不同的对象实例
			inst = mt.isInstance() ? new SceneInstanceMap(mapId, instId, lineNo, ext) : new SceneMap(mapId, instId, lineNo, ext);
			inst.parentInst = parent; //设置父级地图实例
		}
		
		SceneMap.putInstance(instId, inst);

		return inst;
	}

	//删除地图实例
	public static void delMap(int instId) {
		SceneMap.mapInstHash.remove(instId);
		SceneMap.allParentMapInfoHash.remove(instId);
	}

	public static SceneMap findInstance(int instId, int mapId) {
		if (!mapInstHash.containsKey(instId)) {
			return null;
		}
		return mapInstHash.get(instId).get(mapId);
	}
	
	public static boolean containsInstance(int instId, int mapId) {
		return mapInstHash.containsKey(instId) && mapInstHash.get(instId).containsKey(mapId) ;
	}
	
	private static void putInstance(int instId, SceneMap inst) {
		if (!mapInstHash.containsKey(instId)) {
			mapInstHash.put(instId, new HashMap<>());
		}
		mapInstHash.get(instId).put(inst.getMapId(), inst);
	}
	
	public void clearInstance() {
		mapInstHash.clear();
	}

	public int getLineNo() {
		return lineNo;
	}
	
	public static String newSceneElementId() {
		return GuidManager.INSTANCE.makeNewGuid(ObjectType.UNIT);
	}

//	//检测副本的生存周期
//	void Map::CheckMapInstanceLastTime(TimerHolder& th)
//	{
//		//先跑统计，将所有实例的人数及ID跑出来
//		map<uint32,uint32> all_inst_player_num;
//		for (auto it = map_instances.begin();it != map_instances.end(); ++it)			
//			all_inst_player_num[it->second->GetInstanceID()] += it->second->GetPlayerCount();
//		
//		//普通非副本地图,永久存在
//		//单人副本，玩家为空的情况下保留十分钟，主动发送离开副本不保存
//		//活动地图，时间到了将玩家传送出去并且关闭地图
//		uint32 t = (uint32)time(nullptr);
//		for (auto it = map_instances.begin();it != map_instances.end(); ++it)
//		{
//			//取得相应地图模版,并且再取父级地图信息
//			const MapTemplate *mt = MapTemplate::GetMapTempalte(it->second->GetMapId());
//			//副本类型
//			const uint16 inst_type = mt->GetMapBaseInfo().instance_type;
//			//静态地图就不用释放了
//			if(inst_type == MAP_INST_TYP_NO_INSTANCE)
//				continue;
//
//			//实例在在线人数
//			auto player_num = all_inst_player_num[it->second->GetInstanceID()];
//
//			InstanceMap *inst = dynamic_cast<InstanceMap*>(it->second);
//
//			ASSERT(inst);
//			//m_end_time是一个总控开关，超脱于所有规则之上
//			//当没人的时候，地图十分钟就释放
//			if(inst->m_end_time == 0)
//			{
//				if(player_num == 0)
//					inst->m_end_time = t + 600;
//			}
//			else
//			{
//				if(player_num != 0)
//				{
//					inst->m_end_time = 0;
//				}
//				else if(inst->m_end_time < t)
//				{
//					ScenedApp::g_app->call_del_map(inst->GetInstanceID());
//					continue;
//				}
//			}
//
//			if(inst_type == MAP_INST_TYP_SINGLETON || inst_type == MAP_INST_TYP_ACTIVITY)		//如果是副本
//			{
//				uint32 inst_mapid = inst->GetMapId();	
//
//				// 设置一下(就怕某人忘了写结束时间, 导致内存泄漏)
//				if (inst->GetMapCreateTime() + 180 < t && inst->GetMapEndTime() == 0) {
//					inst->SetMapEndTime(t-1);
//				} 
//				
//				if(inst->GetMapEndTime() != 0 && inst->GetMapEndTime() < t)
//				{
//					if(player_num != 0) {
//						if (isMapTimeLimit(inst->GetMapId())) {
//							inst->ExitInstance();		//先弹出玩家，下一次进来在删除实例
//						}
//					}
//					else {
//						// 散人挂机地图和家族领地 没人也删除
//						ScenedApp::g_app->call_del_map(inst->GetInstanceID());
//					}
//					continue;
//				}
//			}
//			/**
//			else if(inst_type == MAP_INST_TYP_ACTIVITY)		//活动副本的话对GM无效
//			{
//				if(inst->GetMapEndTime() != 0 && inst->GetMapEndTime() < t)
//				{
//					//如果玩家为空则关闭地图,否则将玩家传送为空
//					if(player_num == 0)
//					{
//						ScenedApp::g_app->call_del_map(inst->GetInstanceID());
//						continue;
//					}
//					else
//					{
//						inst->ExitInstance();
//						continue;
//					}
//				}
//			}
//			*/
//		}
//		th._next_time += 60;		//一分钟后继续检查
//	}

//	void Map::UpdateParentInfo()
//	{
//		for (auto it_mi:all_parent_map_info)
//		{
//			static ByteArray byte_buf;
//			byte_buf.clear();
//			if(it_mi.second->WriteUpdateBlock(byte_buf))
//			{
//				byte_buf.position(0);
//				ObjMgr.Compress(byte_buf);
//				byte_buf.position(0);
//				packet *pkt_compress = external_protocol_new_packet(SMSG_MAP_UPDATE_OBJECT);
//				packet_write(pkt_compress,(char*)byte_buf.cur_data(),byte_buf.bytesAvailable());
//				byte_buf.clear();
//				update_packet_len(pkt_compress);
//
//				for(auto it=map_instances.lower_bound(it_mi.first);
//					it != map_instances.upper_bound(it_mi.first);++it)
//				{
//					Map *m = it->second;
//					for (auto it_player:m->m_players)
//					{
//						if(it_player.second && it_player.second->GetSession())
//						{
//							it_player.second->GetSession()->SendPacket(*pkt_compress);
//						}
//					}
//				}
//
//				//外部包发送好可以回收了
//				external_protocol_free_packet(pkt_compress);
//				it_mi.second->Clear();
//			}
//		}
//	}

//	BinLogObject Map::fighting_info_binlog(core_obj::SYNC_NONE);
//	void Map::BroadcastFightingInfo(Unit *unit)
//	{
//		Map *map = unit->GetMap();
//		if(!map) return;
//
//		uint32 caster = unit->GetUIntGuid();
//		auto& info =  Map::fighting_info_binlog;
//		
//		uint32 spellid = 0;
//
//		ByteArray *buff = ObjMgr.GridMallocByteArray();
//		uint32 count = info.GetUInt32(MAX_FIGHTING_INFO_INT_NOW_INDEX);
//		uint16 dstx = 0;
//		uint16 dsty = 0;
//		//写入施法者，写入技能id, 被攻击的人数量
//		*buff << (uint16)SMSG_FIGHTING_INFO_UPDATE_OBJECT << caster << uint8(count);
//
//		for(uint32 i = 0; i < count; i++)
//		{
//			uint32 start = i * MAX_FIGHTING_INFO_INT + MAX_FIGHTING_INFO_INT_START;		
//			
//			uint32 target = info.GetUInt32(start + FIGHTING_INFO_INT_RESERVE_3);		//目标ID
//			uint32 lastHP = info.GetUInt32(start + FIGHTING_INFO_INT_HP);				//剩余血量
//			double damage = info.GetDouble(start + FIGHTING_INFO_INT_VALUES);			//伤害数值
//			
//			uint8 attack_type = info.GetByte(start + FIGHTING_INFO_INT_UINT8, 3);		//类型
//			uint32 newSpellid = info.GetUInt32(start + FIGHTING_INFO_INT_SPELL_ID);		//技能ID
//			if (dstx == 0 && dsty == 0) {
//				dstx = info.GetUInt16(start + FIGHTING_INFO_INT_RESERVE_0, 0);		//技能坐标点x
//				dsty = info.GetUInt16(start + FIGHTING_INFO_INT_RESERVE_0, 1);		//技能坐标点y
//			}
//			if(spellid ==0){
//				//初始化spellid值
//				spellid = newSpellid;
//			//如果技能id已经初始化了，但后面的循环发现id不一致，则断言
//			}
//			//写入isKill，1bit
//			uint8 killBit = info.GetByte(start + FIGHTING_INFO_INT_UINT8, 0);			//是否被杀死
//
//			*buff << target << lastHP << attack_type << killBit << damage;
//		}
//		*buff << spellid << dstx << dsty;
//
//		//info.Reset();
//		Map::FightingInfoBinlogRest();
//
//		unit->GetGrid()->AsyncBroadcast(buff);
//		ObjMgr.GridFreeByteArray(buff);
//		//unit->GetGrid()->fighting_blocks.push_back(buff);
//	}
	
	public boolean isValidPosition(int x, int y) {
		return this.mapTemplate.isValidPosition(x, y);
	}

	@Override
	public boolean update(int diff) {
		
		return false;
	}
}
