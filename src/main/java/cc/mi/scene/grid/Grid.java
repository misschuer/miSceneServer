package cc.mi.scene.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.callback.Callback;
import cc.mi.core.generate.msg.UnitBinlogDataModify;
import cc.mi.core.generate.stru.SceneElementJumpInfo;
import cc.mi.core.generate.stru.SceneElementMoveInfo;
import cc.mi.core.generate.stru.UnitBinlogInfo;
import cc.mi.core.log.CustomLogger;
import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;
import cc.mi.scene.element.ScenePlayer;
import cc.mi.scene.server.SceneMap;

public class Grid {
	static final CustomLogger logger = CustomLogger.getLogger(Grid.class);
	
	// 是否是激活状态
	private boolean active;
	
	//在地图的第几个位置
	private final int index;
	// 这个grid在已grid构成的地图中的坐标
	private final int x;
	private final int y;
	//该grid的坐标范围
	private final int startX;
	private final int startY;
	private final int endX;
	private final int endY;
	//所属地图实例
	private final SceneMap inst;
	// 相邻的grid
	private final List<Grid> noticeGrid;

	//更新块
	protected List<UnitBinlogInfo> updateBlocks;
	//创建块
	protected List<UnitBinlogInfo> createBlocks;
	//离开视野
	protected List<UnitBinlogInfo> outAreaBlocks;
	//生物移动包
	protected List<SceneElementMoveInfo> elementMoveBlocks;
	//生物跳跃包
	protected List<SceneElementJumpInfo> elementJumpBlocks;

	//grid底下的战利品
	protected final LootObject loot = new LootObject();
	//玩家对象系列	
	protected final Map<String, ScenePlayer> players = new HashMap<>();
	//生物对象系统
	protected final Map<String, SceneCreature> creatures = new HashMap<>();
	
	public Grid(SceneMap inst, 
			int index, int x, int y, 
			int startX, int startY, int endX, int endY) {

		this.inst	=	inst;
		this.index	=	index;
		this.x		=		x;
		this.y		=		y;
		this.startX =	startX;
		this.startY =	startY;
		this.endX	=	endX;
		this.endY	=	endY;

		this.noticeGrid = new ArrayList<>(9);
	}

	/**
	 * //对象更新
	 */
	public void objectAccess() {

		Grid self = this;
		Callback<SceneElement> callback = new AbstractCallback<SceneElement>() {
			@Override
			public void invoke(SceneElement element) {
//				if(wo->GetTypeId() == TYPEID_PLAYER)
//					static_cast<Player*>(wo)->SyncUnitToPlayerData();

				UnitBinlogInfo unitBinlogInfo = element.packNewElementBinlogInfo(
						GridManager.gridUpdateMask.getUpdateIntMask(),
						GridManager.gridUpdateMask.getUpdateStrMask()
				);
				if (unitBinlogInfo != null) {
					self.addUpdateBlock(unitBinlogInfo);
					element.clear();
				}
			}
		};
		
		for (SceneElement element : players.values()) {
			callback.invoke(element);
		}
		
		for (SceneElement element : creatures.values()) {
			callback.invoke(element);
		}

//		//Grid信息更新
//		if(loot)
//		{
//			ByteArray *bytes = ObjMgr.GridMallocByteArray();
//			if(loot->WriteUpdateBlock(*bytes, loot->GetUIntGuid()))
//			{
//				AddUpdateBlock(bytes);
//				loot->Clear();
//			}
//			else
//			{
//				ObjMgr.GridFreeByteArray(bytes);
//			}
//		}
	}
	
	/**
	 * //发送对象
	 */
	public void sendObjectUpdate() {
		
		if (players.size() == 0) {
			return;
		}
		
		List<UnitBinlogInfo> binlogInfoList = new ArrayList<>();
		//创建和离开只通知当前格
		binlogInfoList.addAll(this.createBlocks);
		binlogInfoList.addAll(this.outAreaBlocks);
		this.createBlocks.clear();
		this.outAreaBlocks.clear();
		
		//更新块
		for (Grid grid : this.noticeGrid) {
			binlogInfoList.addAll(grid.updateBlocks);
		}
		//如果对象没有变化
		if (binlogInfoList.isEmpty()) {
			return;
		}
		logger.devLog("grid {} SendObjectUpdate", this.index);
		
		for (ScenePlayer player : players.values()) {
			
			UnitBinlogDataModify ubdm = new UnitBinlogDataModify();
			ubdm.setFD(player.getContext().getFd());
			ubdm.setUnitBinlogInfoList(binlogInfoList);
			
			player.getContext().sendToGate(ubdm);
		}
	}
	
	public void clearBlock() {
//		for (auto ptr:update_blocks)
//		{
//			ObjMgr.GridFreeByteArray(ptr);
//		}
//		for (auto ptr:out_area_blocks)
//			ObjMgr.GridFreeByteArray(ptr);	
////		for_each(create_blocks.begin(),create_blocks.end(),safe_delete); 创建包内存可能存于多处，需要统一管理m_need_free
//		update_blocks.clear();
//		create_blocks.clear();
//		out_area_blocks.clear();
	}
	
	public void getCreateBlockForNewPlayer() {

//		auto f = [&mgr, &ar](Unit* wb){
//			ByteArray *bytes = ObjMgr.GridMallocByteArray();
//			wb->WriteCreateBlock(*bytes,gGridUpdateMask.create_int_mask_,gGridUpdateMask.create_string_mask_);
//			ar.push_back(bytes);
//			if(mgr)
//				mgr->PushNeedFree(bytes);
//		};
//		for_each(players.begin(),players.end(),f);
//		for_each(creatures.begin(),creatures.end(),f);
//		for_each(worldobjs.begin(),worldobjs.end(),f);
//		
//		if(loot)
//		{//将所有的Grid的信息也一起打包下去
//			ByteArray *bytes = ObjMgr.GridMallocByteArray();
//			loot->WriteCreateBlock(*bytes);
//			ar.push_back(bytes);
//			if(mgr)
//				mgr->PushNeedFree(bytes);
//		}
	}
	
	public void getOutAreaBlockForPlayer() {

//		auto f = GetOutAreaBlockForPlayer_F(ar);
//		for_each(players.begin(),players.end(),f);
//		for_each(creatures.begin(),creatures.end(),f);
//		for_each(worldobjs.begin(),worldobjs.end(),f);
//		//TODO:f(loot->GetPublicObj());
//
//		if(loot)
//		{
//			ByteArray *bytes = ObjMgr.GridMallocByteArray();
//			loot->WriteReleaseBlock(*bytes, loot->GetUIntGuid());
//			ar.push_back(bytes);
//		}
	}
	
	public void sendAllNoticeGridToNewPlayer() {

//		//UpdateData ud;
//		vector<ByteArray*> ud;
//
//		for(GridPtrVec::iterator it = notice_grid.begin();it != notice_grid.end();++it)
//			(*it)->GetCreateBlocForNewPlayer(ud, mgr);
//
//
//		GridManager* gridMgr = map_inst->GetGridManager();
//		if (gridMgr && player->GetSession())
//		{
//			gridMgr->SendUpdateDate(player->GetSession()->GetFD(), ud);
//		}	
	}

	public void broadcast() {

//		GridPtrVec::const_iterator _it = notice_grid.begin();
//		GridPtrVec::const_iterator _end = notice_grid.end();
//		SendFunc f(&pkt);
//
//		//先简单广播, 恶心的嵌套循环
//		for(;_it != _end;++_it)
//		{
//			for(PlayerSet::const_iterator it = (*_it)->players.begin();
//				it != (*_it)->players.end();++it)
//			{
//				Player *p = *it;
//				if(!p)
//					continue;
//				if(p == player && self == false)
//					continue;			
//				f(p);		
//			}
//
//		}
//
//		//给观察者也广播一份
//		GridManager* gridMgr = map_inst->GetGridManager();
//		if (gridMgr)
//		{
//			gridMgr->BroadcastToWatcher(pkt);
//		}
	}
	
	public void addUpdateBlock(UnitBinlogInfo binlogInfo) {
		this.updateBlocks.add(binlogInfo);
	}
	
	public void addCreateBlock(UnitBinlogInfo binlogInfo) {
		this.createBlocks.add(binlogInfo);
	}
	
	public void addOutArea(UnitBinlogInfo binlogInfo) {
		this.outAreaBlocks.add(binlogInfo);
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getIndex() {
		return index;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getStartX() {
		return startX;
	}

	public int getStartY() {
		return startY;
	}

	public int getEndX() {
		return endX;
	}

	public int getEndY() {
		return endY;
	}

	public SceneMap getInst() {
		return inst;
	}
	
	public int getNoticeGridSize() {
		return this.noticeGrid.size();
	}
	
	public Grid getNoticeGrid(int index) {
		return this.noticeGrid.get(index);
	}
	
	public void addGrid(Grid grid) {
		this.noticeGrid.add(grid);
	}
}
