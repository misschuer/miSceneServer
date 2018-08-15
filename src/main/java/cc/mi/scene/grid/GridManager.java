package cc.mi.scene.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.callback.Callback;
import cc.mi.core.constance.SceneElementEnumFields;
import cc.mi.core.generate.msg.UnitBinlogDataModify;
import cc.mi.core.generate.stru.UnitBinlogInfo;
import cc.mi.core.log.CustomLogger;
import cc.mi.scene.element.SceneCreature;
import cc.mi.scene.element.SceneElement;
import cc.mi.scene.element.SceneGameObject;
import cc.mi.scene.element.ScenePlayer;
import cc.mi.scene.server.SceneMap;
import cc.mi.scene.server.SceneServerManager;

public final class GridManager {
	
	static final CustomLogger logger = CustomLogger.getLogger(GridManager.class);

	public static final GridUpdateMask gridUpdateMask = new GridUpdateMask(
		SceneElementEnumFields.ELEMENT_INT_FIELDS_SIZE, 
		SceneElementEnumFields.ELEMENT_STR_FIELDS_SIZE
	);

	protected final Set<Integer> watchers = new HashSet<>();
	
	protected final SceneMap map;
	protected final int gridWidth;
	protected final int gridHeight;
	protected final int gridSides;

	protected final int width;
	protected final int height;

	protected final Grid[] grids;
	protected final Map<Integer, Grid> activeGrid = new HashMap<>();
	
	public GridManager(SceneMap map, int width, int height) {
		this(map, width, height, 15);
	}

	public GridManager(SceneMap map, int width, int height, int gridSides) {

		this.map = map;
		this.gridSides = gridSides;
		this.width = width;
		this.height = height;
		// 初始化grid
		// gridSides个地图网格组成一个敏感区域网格
		// 宽度方向上的敏感区域网格数量
		this.gridWidth = (int) Math.ceil(1.0 * width / gridSides);
		// 高度方向上的敏感区域网格数量
		this.gridHeight = (int) Math.ceil(1.0 * height / gridSides);
		// 网格信息
		int size = this.gridWidth * this.gridHeight;
		this.grids = new Grid[size];

		int index = 0;
		for (int y = 0; y < this.gridHeight; ++y) {
			for (int x = 0; x < this.gridWidth; ++x) {
				int startX = 0;
				int startY = 0;
				int endX = width;
				int endY = height;
				// 更新grid的逻辑坐标
				if (gridSides < 65535) {
					startX = gridSides * x;
					startY = gridSides * y;
					endX = startX + gridSides;
					endY = startY + gridSides;
				}
				Grid grid = new Grid(map, index, x, y, startX, startY, endX, endY);
				grid.setActive(false);

				this.grids[index] = grid;

				// 将相关联的grid存下来,便于
				this.calcNoticeGrid(grid);

				index++;
			}
		}
	}

	public Grid getGrid(int x, int y) {
		return this.grids[y * this.gridWidth + x];
	}

	public Grid gridCoord(float x, float y) {
		return this.gridCoord((int) x, (int) y);
	}

	public Grid gridCoord(int x, int y) {
		int gx = Math.floorDiv(x, gridSides);
		int gy = Math.floorDiv(y, gridSides);
		return this.getGrid(gx, gy);
	}

	// 计算grid附近的敏感grid
	private void calcNoticeGrid(Grid grid) {
		// 最左上角的格子
		int sx = grid.getX() - 1;
		int sy = grid.getY() - 1;

		for (int i = 0; i < 9; ++i) {

			int ix = sx + i % 3;
			int iy = sy + i / 3;

			if (ix < 0 || iy < 0) {
				continue;
			}

			if (ix >= this.gridWidth || iy >= this.gridHeight) {
				continue;
			}
			grid.addGrid(this.getGrid(ix, iy));
		}
	}

	public void update(int diff) {
		//战利品心跳
		for (Grid grid : this.grids) {
			if (grid.getLoot() != null) {
				grid.getLoot().update(diff);
			}
		}
		
		final List<SceneElement> needRefreshElement = new LinkedList<>();
		Callback<SceneElement> callback = new AbstractCallback<SceneElement>() {
			@Override
			public void invoke(SceneElement element) {
				element.update(diff);
				int x = (int) element.getPositionX();
				int y = (int) element.getPositionY();
				// 场景元素已经不在当前grid
				if (element.getGrid().isNotInThisGrid(x, y)) {
					needRefreshElement.add(element);
				}
			}
		};

		// 处理玩家是否有改变grid
		for (Grid grid : this.activeGrid.values()) {
			Iterator<ScenePlayer> playerIter = grid.playerIterator();
			for (;playerIter.hasNext();) {
				ScenePlayer player = playerIter.next();
				callback.invoke(player);
			}
		}

		// 处理生物是否有改变grid
		for (Grid grid : this.activeGrid.values()) {
			Iterator<SceneCreature> creatureIter = grid.creatureIterator();
			for (;creatureIter.hasNext();) {
				SceneCreature creature = creatureIter.next();
				callback.invoke(creature);
			}
		}

		//根据坐标变化计算出现，需要刷新grid的对象
		for (SceneElement element : needRefreshElement) {
			this.refreshGrid(element);
		}

		//观察对象变化,并且将所有对象的更新包加入到grid的更新数组
		for (Grid grid : this.activeGrid.values()) {
			grid.objectAccess();
		}

		//将各个grid的对象更新包进行发送
		for (Grid grid : this.activeGrid.values()) {
			grid.sendObjectUpdate();
		}

		//发送完成后清空内存
		for (Grid grid : this.activeGrid.values()) {
			grid.clearBlock();
		}

		//战斗结果包
//		for(auto it:m_active_grid)
//		{
//			SendFightingBlocks(it);
//		}
	 }

	public void addPlayer(ScenePlayer player) {

		player.clear(); // 既然是新添加的，就没必要打更新包了
		Grid grid = this.gridCoord(player.getPositionX(), player.getPositionY());

		// 将网格上面的其他玩家信息发给他
		grid.sendAllNoticeGridToNewPlayer(this, player);

		// 激活grid
		this.refreshGridIdle(grid);

		// 通知grid上面的其他玩家
		UnitBinlogInfo info = player.packNewElementBinlogInfo(gridUpdateMask.getCreateIntMask(),
				gridUpdateMask.getCreateStrMask());

		Iterator<Grid> iterGrid = grid.noticeGridIterator();
		for (; iterGrid.hasNext();) {
			Grid grid2 = iterGrid.next();
			this.setGridIdleStatus(grid2, true);
			grid2.addCreateBlock(info);
		}

		// 增加到队列
		grid.playerEnter(player);
		player.setGrid(grid);
		logger.devLog("##################player {} born at grid {}", player.getGuid(), grid.getIndex());
	}

	public void delPlayer(ScenePlayer player) {

		Grid grid = player.getGrid();
		if (grid == null) {
			return;
		}
		//////////////////////////////////////////////////////////////////////////
		// 将九宫格里面的所有对象通知给当前玩家离开视野
		List<UnitBinlogInfo> ud = new ArrayList<>();
		Iterator<Grid> iterGrid = grid.noticeGridIterator();
		for (; iterGrid.hasNext();) {
			Grid grid2 = iterGrid.next();
			grid2.getOutAreaBlockForPlayer(ud);
			// player->InvalidFriendlyCache(g);
		}

		// 发送给
		if (player.getContext() != null) {
			this.sendUpdateData(player.getContext().getFd(), ud);
		}

		//////////////////////////////////////////////////////////////////////////
		// 删掉格子里面的当前玩家
		grid.playerLeave(player.getGuid());

		// 通知这个格子相关联的九宫格里面的所有玩家，当前玩家离开
		iterGrid = grid.noticeGridIterator();
		for (; iterGrid.hasNext();) {
			Grid grid2 = iterGrid.next();
			grid2.addOutArea(player);

			Iterator<SceneCreature> iter = grid2.creatureIterator();
			while (iter.hasNext()) {
				iter.next();
				// TODO: SceneCreature creature = iter.next();
				// 如果怪物正在攻击此玩家，停止怪物攻击
				// if((*citer)->GetTarget() == dynamic_cast<Unit*>(player))
				// {
				// (*citer)->SetTarget(NULL);
				// }
				// (*citer)->m_threatMgr.Del(player, true);
			}
		}

		// 刷新网格状态
		this.refreshGridIdle(grid);
		player.setGrid(null);
	}

	public boolean addWatcher(int fd, final String ownerId, int mapId, int instId) {
		
		//把需要观察的地图信息告诉客户端
//		packet *pkt = nullptr;
//		pack_notice_watcher_map_info(&pkt, owner_guid.c_str(), map_id, instance_id);
//		SendFunc f(pkt);
//		f(fd);
//		external_protocol_free_packet(pkt);
		
		for (Grid grid : this.activeGrid.values()) {
			Iterator<ScenePlayer> playerIter = grid.playerIterator();
			for (;playerIter.hasNext();) {
				ScenePlayer player = playerIter.next();
				if (player.getContext() == null) {
					continue;
				}
				// 登录的玩家不能作为观察者
				if (player.getContext().getFd() == fd) {
					return false;
				}
			}
		}
		
		this.watchers.add(fd);
		
		//将网格上面的其他玩家信息发给他(ps：支持观察者模式的地图必须是全图广播的)
		List<UnitBinlogInfo> ud = new ArrayList<>();
		for (Grid grid : this.activeGrid.values()) {
			grid.getCreateBlockForNewPlayer(ud, this);
		}

		this.sendUpdateData(fd, ud);

		return true;
	}
	
	public void delWatcher(int fd) {
		this.watchers.remove(fd);
	}

	public void addWorldObject(SceneElement element) {

		element.clear();// 既然是新添加的，就没必要打更新包了
		Grid grid = this.gridCoord(element.getPositionX(), element.getPositionY());
		if (element.getElementType() == SceneElement.ELEMENT_TYPE_CREATURE) {
			grid.creatureEnter((SceneCreature) element);
		} else {
			grid.gameObjectEnter((SceneGameObject) element);
		}
		element.setGrid(grid);

		// 如果可以激活grid
		if (element.isCanActiveGrid()) {
			this.refreshGridIdle(grid);
		}

		if (!grid.isActive()) {
			return;
		}

		UnitBinlogInfo info = element.packNewElementBinlogInfo(gridUpdateMask.getCreateIntMask(),
				gridUpdateMask.getCreateStrMask());

		Iterator<Grid> iterGrid = grid.noticeGridIterator();
		for (; iterGrid.hasNext();) {
			Grid grid2 = iterGrid.next();
			if (!grid2.isActive()) {
				continue;
			}
			grid2.addCreateBlock(info);
		}
	}

	// void GridManager::InitLoot(Grid &grid)
	// {
	// if(grid.loot)
	// return;
	// grid.loot = new LootObject(&grid);
	// grid.loot->Init(grid.map_inst, grid.index);
	//
	// //通知grid上面的其他玩家
	// ByteArray *bytes = ObjMgr.GridMallocByteArray();
	// grid.loot->WriteCreateBlock(*bytes);
	// m_need_free.insert(bytes);
	//
	// Grid::GridPtrVec::iterator it = grid.notice_grid.begin();
	// Grid::GridPtrVec::iterator end = grid.notice_grid.end();
	// for(;it != end;++it)
	// {
	// SetGridIdleStatus(**it,true);
	// (*it)->AddCreateBlock(bytes);
	// }
	// }
	//
	// //清空所有战利品
	// void GridManager::ClearAllLoot()
	// {
	// //战利品心跳
	// for (auto it = m_grids.begin(); it != m_grids.end(); ++it)
	// {
	// if(it->loot)
	// {
	// auto l = it->loot->m_has_loot;
	// for (auto index : l)
	// {
	// it->loot->ClearLoot(index);
	// }
	// it->loot->m_has_loot.clear();
	// }
	// }
	// }

	public SceneElement findWorldObject(SceneElement centerElement, final String binlogId) {

		Grid grid = this.gridCoord(centerElement.getPositionX(), centerElement.getPositionY());
		if (grid.containsGameObject(binlogId)) {
			return grid.getGameObject(binlogId);
		}

		// 当前格找不到找周围格
		Iterator<Grid> iterGrid = centerElement.getGrid().noticeGridIterator();
		for (; iterGrid.hasNext();) {
			Grid grid2 = iterGrid.next();
			if (grid2.containsGameObject(binlogId)) {
				return grid2.getGameObject(binlogId);
			}
		}
		return null;
	}

	public void delWorldObject(SceneElement element) {

		if (element.getElementType() == SceneElement.ELEMENT_TYPE_CREATURE) {
			element.getGrid().creatureLeave(element.getGuid());
		} else {
			element.getGrid().gameObjectLeave(element.getGuid());
		}

		// 离开视野
		if (element.getGrid().isActive()) {
			Iterator<Grid> iterGrid = element.getGrid().noticeGridIterator();
			for (; iterGrid.hasNext();) {
				Grid grid2 = iterGrid.next();
				grid2.addOutArea(element);
			}
		}

		element.setGrid(null);
	}

	public void refreshGrid(SceneElement element) {

		if (element.getMap().getMapId() != element.getMapId()) {
			logger.devLog("refreshGrid element.getMap().getMapId() != element.getMapId() {} {} {} {} {} {}",
					element.getMap().getMapId(), element.getMapId(), element.getEntry(), element.getElementType(),
					element.getGuid(), element.getName());
			return;
		}

		if (element.getPositionX() >= this.width || element.getPositionX() < 0.0f) {
			logger.devLog(
					"refreshGrid element.getPositionX() >= this.width || element.getPositionX() < 0.0f {} {} {} {} {}",
					element.getPositionX(), element.getEntry(), element.getElementType(), element.getGuid(),
					element.getName());
			return;
		}

		if (element.getPositionY() >= this.height || element.getPositionY() < 0.0f) {
			logger.devLog(
					"refreshGrid element.getPositionY() >= this.height || element.getPositionY() < 0.0f {} {} {} {} {}",
					element.getPositionY(), element.getEntry(), element.getElementType(), element.getGuid(),
					element.getName());
			return;
		}

		Grid oldGrid = element.getGrid();
		Grid newGrid = this.gridCoord(element.getPositionX(), element.getPositionY());
		// -------------------------------------计算两个grid关心区域的集合问题

		// 创建块
		UnitBinlogInfo info = element.packNewElementBinlogInfo(gridUpdateMask.getCreateIntMask(),
				gridUpdateMask.getCreateStrMask());

		// --------------------------------------将新关心的grid中的对象通知该玩家
		// ---------------------------------刷新离开视野那一部分
		// 先从原grid中去掉对象
		if (element.getElementType() == SceneElement.ELEMENT_TYPE_PLAYER) {
			oldGrid.playerLeave(element.getGuid());
		} else if (element.getElementType() == SceneElement.ELEMENT_TYPE_CREATURE) {
			oldGrid.creatureLeave(element.getGuid());
		} else {
			oldGrid.gameObjectLeave(element.getGuid());
		}

		List<UnitBinlogInfo> ud = new ArrayList<>();
		Iterator<Grid> gridOldIter = oldGrid.noticeGridIterator();
		for (; gridOldIter.hasNext();) {
			Grid grid2 = gridOldIter.next();
			int absx = Math.abs(grid2.getX() - newGrid.getX());
			int absy = Math.abs(grid2.getY() - newGrid.getY());
			// 远离的多个grid
			if (absx > 1 || absy > 1) {
				grid2.addOutArea(element);

				// 刷新状态
				if (!element.isCanActiveGrid()) {
					continue;
				}
				// 将其他grid对象离开当前玩家
				if (element.getElementType() == SceneElement.ELEMENT_TYPE_PLAYER) {
					grid2.getOutAreaBlockForPlayer(ud);
				}
				// 刷新grid状态
				this.refreshGridStatus(grid2);

				// //让关联的这几个grid失效
				// wo->InvalidFriendlyCache(*it);
			}
		}

		// ---------------------------------刷新新加入部分
		if (element.isCanActiveGrid() || newGrid.isActive()) {
			Iterator<Grid> gridNewIter = oldGrid.noticeGridIterator();
			for (; gridNewIter.hasNext();) {
				Grid grid2 = gridNewIter.next();
				int absx = Math.abs(grid2.getX() - oldGrid.getX());
				int absy = Math.abs(grid2.getY() - oldGrid.getY());
				// 新接近的多个grid
				if (absx > 1 || absy > 1) {
					// 设置为活动
					if (!grid2.isActive() && element.isCanActiveGrid()) {
						this.setGridIdleStatus(grid2, true);
					}
					// 将这些grid中的对象通知该玩家
					if (element.getElementType() == SceneElement.ELEMENT_TYPE_PLAYER) {
						grid2.getCreateBlockForNewPlayer(ud, null);
					}

					if (grid2.isActive()) {
						grid2.addCreateBlock(info);
					}
				}
				// else {
				// 防止多个对象到 与运动方向垂直的包含new_grid的一条线的grid内, 没法通知到在这条线内的grid中的player的BUG
				// 如grid 8 和grid 10的对象同时走到grid 9
				// if((*it)->active) {
				// (*it)->AddCreateBlock(bytes);
				// }
				// }
			}
		}

		// 加入新的grid
		if (element.getElementType() == SceneElement.ELEMENT_TYPE_PLAYER) {
			newGrid.playerEnter((ScenePlayer) element);
		} else if (element.getElementType() == SceneElement.ELEMENT_TYPE_CREATURE) {
			newGrid.creatureEnter((SceneCreature) element);
		} else {
			newGrid.gameObjectEnter((SceneGameObject) element);
		}

		element.setGrid(newGrid);
		// -----------------------------如果是玩家需要处理grid的状态
		// 并且发送给他新grid的状态
		if (element.getElementType() == SceneElement.ELEMENT_TYPE_PLAYER && !ud.isEmpty()) {
			ScenePlayer player = (ScenePlayer) element;
			if (player.getContext() != null) {
				this.sendUpdateData(player.getContext().getFd(), ud);
			}
		}
	}

	public void refreshGridStatus(Grid grid) {
		// 刷新grid状态
		boolean result = false;

		Iterator<Grid> iterGrid = grid.noticeGridIterator();
		for (; iterGrid.hasNext();) {
			Grid grid2 = iterGrid.next();
			if (!grid2.isEmptyPlayer()) {
				result = true;
				break;
			}

			Iterator<SceneCreature> iter = grid2.creatureIterator();
			while (iter.hasNext()) {
				SceneCreature creature = iter.next();
				if (creature.isCanActiveGrid()) {
					result = true;
					break;
				}
			}

			if (result) {
				break;
			}
		}

		this.setGridIdleStatus(grid, result);
	}

	public void refreshGridIdle(Grid grid) {
		// 如果网格附近没人则进入休眠状态
		Iterator<Grid> iterGrid = grid.noticeGridIterator();
		for (; iterGrid.hasNext();) {
			Grid grid2 = iterGrid.next();
			this.refreshGridStatus(grid2);
		}
	}

	public void setGridIdleStatus(Grid grid, boolean active) {
		if (active) {
			activeGrid.put(grid.getIndex(), grid);
		} else {
			activeGrid.remove(grid.getIndex());
			grid.clearBlock();
		}
		grid.setActive(active);
	}

	public Grid gridCoordPtr(int x, int y) {
		
		int gx = Math.floorDiv(x, this.gridWidth);
		int gy = Math.floorDiv(y, this.gridWidth);
		
		if (gx < 0 || gx >= this.gridWidth) {
			return null;
		}
		if (gy < 0 || gy >= this.gridHeight) {
			return null;
		}
		
		return this.grids[gy*gridWidth+gx];
	}
	
	// void GridManager::SendFightingBlocks(Grid *grid)
	// {
	// auto fun = [&](int opcode, int not_compree_opcode, vector<ByteArray*>&
	// blocks){
	// if(blocks.empty())
	// return;
	//
	// ByteArray *buff = ObjMgr.GridMallocByteArray();
	// buff->writeShort(blocks.size());
	// for (auto it:blocks)
	// {
	// it->position(0);
	// buff->writeBytes(*it);
	// ObjMgr.GridFreeByteArray(it);
	// }
	// blocks.clear();
	// buff->position(0);
	//
	//
	// if(buff->length() < COMPRESS_MIN_SIZE)
	// {
	// ByteArray *pkt = ObjMgr.GridMallocByteArray();
	// pkt->writeShort(not_compree_opcode);
	// pkt->writeBytes(buff->cur_data(),buff->bytesAvailable());
	// ObjMgr.GridFreeByteArray(buff);
	// grid->AsyncBroadcast(pkt);
	// ObjMgr.GridFreeByteArray(pkt);
	// }
	// else
	// {
	// int a= 0;
	// ObjMgr.AsyncCompress(*buff, [opcode, grid, this](ByteArray *buff){
	// buff->position(0);
	// packet *spkt = internal_protocol_new_packet(opcode);
	// packet_write(spkt, (char*)buff->cur_data(), buff->bytesAvailable());
	// update_packet_len(spkt);
	// grid->Broadcast(*spkt);
	// internal_protocol_free_packet(spkt);
	// });
	// }
	// };
	// //fun(SMSG_FIGHTING_INFO_UPDATE_OBJECT_2, SMSG_FIGHTING_INFO_UPDATE_OBJECT,
	// grid->fighting_blocks);
	// fun(SMSG_GRID_UNIT_MOVE_2, SMSG_GRID_UNIT_MOVE, grid->unit_move_blocks);
	// fun(SMSG_GRID_UNIT_JUMP, SMSG_GRID_UNIT_JUMP, grid->unit_jump_blocks);
	// }
	//
	// //广播给观察者
	// void GridManager::BroadcastToWatcher(packet& pkt)
	// {
	// if (m_watchers.empty())
	// return;
	//
	// SendFunc f(&pkt);
	// //给观察者也广播一份
	// std::for_each(m_watchers.begin(), m_watchers.end(), [&](uint32 fd){
	// f(fd);
	// });
	// }
	//
	// void GridManager::BroadcastToWatcher(ByteArray& pkt)
	// {
	// if (m_watchers.empty())
	// return;
	//
	// SendFunc f(pkt);
	// //给观察者也广播一份
	// std::for_each(m_watchers.begin(), m_watchers.end(), [&](uint32 fd){
	// f(fd);
	// });
	// }
	//

	public void sendUpdateData(int fd, List<UnitBinlogInfo> ud) {

		if (fd == 0) {
			return;
		}

		UnitBinlogDataModify ubdm = new UnitBinlogDataModify();
		ubdm.setFD(fd);
		ubdm.setUnitBinlogInfoList(ud);

		SceneServerManager.getInstance().sendToGate(ubdm);
	}

	public SceneMap getMap() {
		return map;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public int getGridHeight() {
		return gridHeight;
	}
}
