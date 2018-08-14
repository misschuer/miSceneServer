package cc.mi.scene.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.callback.Callback;
import cc.mi.core.generate.msg.UnitBinlogDataModify;
import cc.mi.core.generate.stru.SceneElementJumpInfo;
import cc.mi.core.generate.stru.SceneElementMoveInfo;
import cc.mi.core.generate.stru.UnitBinlogInfo;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.packet.Packet;
import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;
import cc.mi.scene.element.SceneGameObject;
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
	//游戏对象(如花草,传送阵这种东西)系统
	protected final Map<String, SceneGameObject> gameObjects = new HashMap<>();
	
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

	private void foreachElementAndInvokeCllback(Callback<SceneElement> callback) {
		for (SceneElement element : players.values()) {
			callback.invoke(element);
		}
		
		for (SceneElement element : creatures.values()) {
			callback.invoke(element);
		}
		
		for (SceneElement element : gameObjects.values()) {
			callback.invoke(element);
		}
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

				UnitBinlogInfo unitBinlogInfo = element.packUpdateElementBinlogInfo(
						GridManager.gridUpdateMask.getUpdateIntMask(),
						GridManager.gridUpdateMask.getUpdateStrMask()
				);
				if (unitBinlogInfo != null) {
					self.addUpdateBlock(unitBinlogInfo);
					element.clear();
				}
			}
		};
		
		this.foreachElementAndInvokeCllback(callback);

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
			
			if (this.getInst().getGridManager() != null) {
//				gridMgr->BroadcastToWatcher(*pkt);
			}
		}
	}
	
	public void clearBlock() {
		this.updateBlocks.clear();
		this.outAreaBlocks.clear();
		this.createBlocks.clear();
	}
	
	public void getCreateBlockForNewPlayer(final List<UnitBinlogInfo> infoList, GridManager gridManader) {
		Callback<SceneElement> callback = new AbstractCallback<SceneElement>() {
			@Override
			public void invoke(SceneElement element) {
				UnitBinlogInfo unitBinlogInfo = element.packNewElementBinlogInfo(
						GridManager.gridUpdateMask.getCreateIntMask(),
						GridManager.gridUpdateMask.getCreateStrMask()
				);
				if (unitBinlogInfo != null) {
					infoList.add(unitBinlogInfo);
				}
			}
		};
		
		this.foreachElementAndInvokeCllback(callback);
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
	
	public void getOutAreaBlockForPlayer(final List<UnitBinlogInfo> infoList) {
		Callback<SceneElement> callback = new AbstractCallback<SceneElement>() {
			@Override
			public void invoke(SceneElement element) {
				infoList.add(element.packRemoveElementBinlogInfo());
			}
		};
		
		this.foreachElementAndInvokeCllback(callback);
		
//		if(loot)
//		{
//			ByteArray *bytes = ObjMgr.GridMallocByteArray();
//			loot->WriteReleaseBlock(*bytes, loot->GetUIntGuid());
//			ar.push_back(bytes);
//		}
	}
	
	public void sendAllNoticeGridToNewPlayer(GridManager gridManader, ScenePlayer player) {
		
		List<UnitBinlogInfo> unitInfoList = new ArrayList<>();
		for (Grid grid : this.noticeGrid) {
			grid.getCreateBlockForNewPlayer(unitInfoList, gridManader);
		}
		
		if (this.inst.getGridManager() != null && player.getContext() != null) {
//			this.inst.getGridManager().SendUpdateData(player.getContext().getFd(), unitInfoList);
		}
	}
	
	
	public void broadcast(Packet packet) {
		this.broadcast(packet, null);
	}
	
	public void broadcast(Packet packet, ScenePlayer player) {
		this.broadcast(packet, player, false);
	}

	public void broadcast(Packet packet, ScenePlayer player, boolean includeSelf) {
		
		for (Grid grid : this.noticeGrid) {
			for (ScenePlayer p : grid.players.values()) {
				if (p == player && !includeSelf) {
					continue;
				}
				p.getContext().sendToGate(packet);
			}
		}
		
		//给观察者也广播一份
		if (this.inst.getGridManager() != null) {
//			this.inst.getGridManager().BroadcastToWatcher(pkt);
		}
	}
	
	public void addUpdateBlock(UnitBinlogInfo binlogInfo) {
		this.updateBlocks.add(binlogInfo);
	}
	
	public void addCreateBlock(UnitBinlogInfo binlogInfo) {
		this.createBlocks.add(binlogInfo);
	}
	
	public void addOutArea(SceneElement element) {
		this.outAreaBlocks.add(element.packRemoveElementBinlogInfo());
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
	
	public Iterator<Grid> noticeGridIterator() {
		return this.noticeGrid.iterator();
	}
	
	
	public void addGrid(Grid grid) {
		this.noticeGrid.add(grid);
	}
	
	public Iterator<ScenePlayer> playerIterator() {
		return this.players.values().iterator();
	}
	
	public void playerEnter(ScenePlayer player) {
		this.players.put(player.getGuid(), player);
	}
	
	public void playerLeave(String guid) {
		this.players.remove(guid);
	}
	
	public boolean isEmptyPlayer() {
		return this.players.isEmpty();
	}
	
	public Iterator<SceneCreature> creatureIterator() {
		return this.creatures.values().iterator();
	}
	
	public void creatureEnter(SceneCreature creature) {
		this.creatures.put(creature.getGuid(), creature);
	}
	
	public void creatureLeave(String guid) {
		this.creatures.remove(guid);
	}
	
	public Iterator<SceneGameObject> gameObjectIterator() {
		return this.gameObjects.values().iterator();
	}
	
	public void gameObjectEnter(SceneGameObject gameObject) {
		this.gameObjects.put(gameObject.getGuid(), gameObject);
	}
	
	public void gameObjectLeave(String guid) {
		this.gameObjects.remove(guid);
	}
	
	public boolean containsGameObject(String guid) {
		return this.gameObjects.containsKey(guid);
	}
	
	public SceneElement getGameObject(String guid) {
		return this.gameObjects.get(guid);
	}
	
	public boolean isNotInThisGrid(int lx, int ly) {
		return lx >= this.endX || lx < this.startX
			|| ly >= this.endY || ly < this.startY;
	}

	public LootObject getLoot() {
		return loot;
	}
}
