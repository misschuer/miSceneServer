package cc.mi.scene.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.callback.Callback;
import cc.mi.core.generate.stru.BinlogInfo;
import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;
import cc.mi.scene.element.ScenePlayer;
import cc.mi.scene.server.SceneMap;

public class Grid {
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

	//grid底下的战利品
	protected final LootObject loot = new LootObject();
	//玩家对象系列	
	protected final Set<ScenePlayer> players = new HashSet<>();
	//生物对象系统
	protected final List<SceneCreature> creatures = new ArrayList<>();
	
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
//				//if(wo->BinlogEmpty())//没变化
//				//	return;
				
				BinlogInfo binlogInfo = element.packUpdateBinlogInfo();
				if (binlogInfo != null) {
					self.addUpdateBlock(binlogInfo);
					element.clear();
				}
				
//				ByteArray *bytes = ObjMgr.GridMallocByteArray();
//				if(wo->WriteUpdateBlock(*bytes, wo->GetUIntGuid(),gGridUpdateMask.update_int_mask_,gGridUpdateMask.update_string_mask_))
//				{
//					AddUpdateBlock(bytes);
//					wo->Clear();
//				}
//				else
//				{
//					ObjMgr.GridFreeByteArray(bytes);
//				}
			}
		};
		
		for (SceneElement element : players) {
			callback.invoke(element);
		}
		
		for (SceneElement element : creatures) {
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
		
//		static vector<ByteArray *> ud;
//		ud.clear();
//
//		//创建和离开只通知当前格
//		ud.insert(ud.end(),create_blocks.begin(),create_blocks.end());	
//		ud.insert(ud.end(),out_area_blocks.begin(),out_area_blocks.end());
//		create_blocks.clear();
//
//		//更新块
//		for(auto p:notice_grid)
//			ud.insert(ud.end(),p->update_blocks.begin(),p->update_blocks.end());		
//
//		//如果对象没有变化
//		if(ud.empty())
//			return;
//
//		tea_pdebug("grid %u SendObjectUpdate", this->index);
//
//		ByteArray *byte = ObjMgr.GridMallocByteArray();
//		byte->clear();
//		for (auto bytes:ud)
//		{
//			bytes->position(0);
//			byte->writeBytes(*bytes);
//		}
//		byte->position(0);
//		if(byte->length() == 0)
//		{
//			ObjMgr.GridFreeByteArray(byte);
//		}
//		else if(byte->length() < COMPRESS_MIN_SIZE)
//		{
//			packet *pkt = external_protocol_new_packet(SMSG_GRID_UD_OBJECT_2);
//			packet_write(pkt,(char*)byte->cur_data(),byte->bytesAvailable());
//			update_packet_len(pkt);
//			std::for_each(players.begin(),players.end(),SendFunc(pkt));
//			GridManager* gridMgr = map_inst->GetGridManager();
//			if (gridMgr)
//				gridMgr->BroadcastToWatcher(*pkt);
//			external_protocol_free_packet(pkt);
//			ObjMgr.GridFreeByteArray(byte);
//		}
//		else
//		{
//			vector<int> fds;
//			for (auto player:players)
//			{
//				if(player && player->GetSession())
//					fds.push_back(player->GetSession()->GetFD());
//			}
//			ObjMgr.AsyncCompress(*byte, [this, fds](ByteArray *byte){
//				byte->position(0);
//				packet *pkt = external_protocol_new_packet(SMSG_GRID_UD_OBJECT);
//				packet_write(pkt,(char*)byte->cur_data(),byte->bytesAvailable());
//				update_packet_len(pkt);
//				SendFunc f(pkt);
//				std::for_each(fds.begin(),fds.end(),f);
//				GridManager* gridMgr = map_inst->GetGridManager();
//				if (gridMgr)
//					gridMgr->BroadcastToWatcher(*pkt);
//				external_protocol_free_packet(pkt);	
//				ObjMgr.GridFreeByteArray(byte);
//			});
//		}
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
	
	public void addUpdateBlock(BinlogInfo binlogInfo) {
		
	}
	
	public void addCreateBlock() {
		
	}
	
	public void addOutArea() {
		
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
