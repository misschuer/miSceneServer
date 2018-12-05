package cc.mi.scene.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.callback.Callback;
import cc.mi.core.constance.IdentityConst;
import cc.mi.core.constance.ObjectType;
import cc.mi.core.generate.Opcodes;
import cc.mi.core.generate.stru.BinlogInfo;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.manager.ServerManager;
import cc.mi.core.utils.ServerProcessBlock;
import cc.mi.core.utils.TimestampUtils;
import cc.mi.scene.handler.BinlogDataModifyHandler;
import cc.mi.scene.handler.CreateMapHandler;
import cc.mi.scene.handler.DeleteMapHandler;
import cc.mi.scene.handler.InnerServerConnListHandler;
import cc.mi.scene.handler.JoinMapHandler;
import cc.mi.scene.handler.LeaveMapHandler;
import cc.mi.scene.info.WaitJoinInfo;

public class SceneServerManager extends ServerManager {
	static final CustomLogger logger = CustomLogger.getLogger(SceneServerManager.class);
	private static SceneServerManager instance = new SceneServerManager();
	
	private final Map<String, WaitJoinInfo> waitJoinHash = new HashMap<>();
	
	public static SceneServerManager getInstance() {
		return instance;
	}
	
	@Override
	protected void onOpcodeInit() {
		handlers.put(Opcodes.MSG_BINLOGDATAMODIFY, new BinlogDataModifyHandler());
		handlers.put(Opcodes.MSG_CREATEMAP, new CreateMapHandler());
		handlers.put(Opcodes.MSG_JOINMAPMSG, new JoinMapHandler());
		handlers.put(Opcodes.MSG_INNERSERVERCONNLIST, new InnerServerConnListHandler());
		
		handlers.put(Opcodes.MSG_PLAYERLEAVEMAP, new LeaveMapHandler());
		handlers.put(Opcodes.MSG_DELETEMAP, new DeleteMapHandler());
		
		opcodes.addAll(handlers.keySet());
	}
	
	public SceneServerManager() {
		super(IdentityConst.SERVER_TYPE_SCENE);
	}
	
	@Override
	protected void onProcessInit() {
		this.process = new ServerProcessBlock() {
			@Override
			public void run(int diff) {
				instance.doInit();
			}
		};
	}
	
	/**
	 * 进行帧刷新
	 */
	@Override
	protected void doWork(int diff) {
		// 初始化服务器
		this.doProcess(diff);
		// 处理包信息
		this.dealPacket();
		// 处理更新信息
		this.doUpdate(diff);
	}
	
	private void doInit() {
		if (this.centerChannel == null) {
			return;
		}
		logger.devLog("do init");
		this.addTagWatchAndCall(ObjectType.FACTION_BINLOG_OWNER_STRING);
		this.addTagWatchAndCall(ObjectType.GROUP_BINLOG_OWNER_STRING);
		this.addTagWatchAndCall(ObjectType.GLOBAL_VALUE_OWNER_STRING, new AbstractCallback<Void>() {
			@Override
			public void invoke(Void value) {
				instance.onDataReady();
			}
		});
		instance.process = null;
	}
	
	public void onBinlogDatasUpdated(List<BinlogInfo> binlogInfoList) {
		for (BinlogInfo binlogInfo : binlogInfoList) {
			SceneObjectManager.INSTANCE.parseBinlogInfo(binlogInfo);
		}
	}
	
	private void onDataReady() {
		this.process = new ServerProcessBlock() {
			@Override
			public void run(int diff) {
			}
		};
		this.startReady();
	}
	
	protected void addTagWatchCallback(String ownerTag, Callback<Void> callback) {
		SceneObjectManager.INSTANCE.addOwnerCreateCallback(ownerTag, callback);
	}
	
	private void doUpdate(int diff) {
		this.updateWaitJoin();
	}
	
	public void putWaitJoin(String guid, int fd, int instId, int mapId, float x, float y, byte sign) {
		WaitJoinInfo info = new WaitJoinInfo(fd, instId, mapId, x, y, sign);
		this.waitJoinHash.put(guid, info);
	}
	
	public boolean isInWaitJoin(String guid) {
		return this.waitJoinHash.containsKey(guid);
	}
	
	public void removeWaitJoin(String guid) {
		this.waitJoinHash.remove(guid);
	}
	
	public void updateWaitJoin() {
		
		int now = TimestampUtils.now();
		List<String> removeList = new LinkedList<>();
		
		for (Entry<String, WaitJoinInfo> entryInfo : waitJoinHash.entrySet()) {
			
			String ownerId = entryInfo.getKey();
			WaitJoinInfo info = entryInfo.getValue();
			SceneContextPlayer contextPlayer = null;
			if (SceneObjectManager.INSTANCE.contains(ownerId)) {
				contextPlayer = (SceneContextPlayer)SceneObjectManager.INSTANCE.get(ownerId);
			}
			
			if (contextPlayer != null && contextPlayer.getTeleportSign() == info.getSign()) {
				logger.devLog("player {} join map [{}] BEGIN", ownerId, info.getMapId());
				contextPlayer.onTeleportOK(info.getFd(), info.getMapId(), info.getInstId(), info.getX(), info.getY());
//					//通知网关服
//					ScenedApp::g_app->RegSessionOpts(waitJoining->connection_id);
				logger.devLog("player {} join map [{}]END", ownerId, info.getMapId());
				removeList.add(ownerId);
			} else if (now - info.getCreateTime() > 60) {
				logger.devLog("updateWaitJoin timeout, ownerId:{}, fd:{}, mapid:{} instanceid:{}",
						ownerId, info.getFd(), info.getMapId(), info.getInstId());
				
				if (contextPlayer != null){
					//TODO: 错误的关闭的code
					contextPlayer.getContext().closeSession(0);
				}
				removeList.add(ownerId);
			}
		}
		
		for (String guid : removeList) {
			waitJoinHash.remove(guid);
		}
	}
}
