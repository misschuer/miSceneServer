package cc.mi.scene.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.callback.Callback;
import cc.mi.core.constance.IdentityConst;
import cc.mi.core.constance.ObjectType;
import cc.mi.core.generate.Opcodes;
import cc.mi.core.generate.stru.BinlogInfo;
import cc.mi.core.handler.Handler;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.manager.ServerManager;
import cc.mi.core.packet.Packet;
import cc.mi.core.utils.ServerProcessBlock;
import cc.mi.core.utils.TimestampUtils;
import cc.mi.scene.handler.BinlogDataModifyHandler;
import cc.mi.scene.handler.CreateMapHandler;
import cc.mi.scene.handler.InnerServerConnListHandler;
import cc.mi.scene.handler.JoinMapHandler;
import cc.mi.scene.info.WaitJoinInfo;

public class SceneServerManager extends ServerManager {
	static final CustomLogger logger = CustomLogger.getLogger(SceneServerManager.class);
	
	private static SceneServerManager instance;
	// 消息收到以后的回调
	private static final Map<Integer, Handler> handlers = new HashMap<>();
	private static final List<Integer> opcodes;
	
	// 帧刷新
	private final ScheduledExecutorService excutor = Executors.newScheduledThreadPool(1);
	// 消息包队列
	private final Queue<Packet> packetQueue = new LinkedList<>();
	// 当前帧刷新执行的代码逻辑
	protected ServerProcessBlock process;
	// 最后一次执行帧刷新的时间戳
	protected long timestamp = 0;
	// 对象管理
	private final SceneObjectManager objManager = new SceneObjectManager();
	
	private final Map<String, WaitJoinInfo> waitJoinHash = new HashMap<>();
	
	static {
		handlers.put(Opcodes.MSG_BINLOGDATAMODIFY, new BinlogDataModifyHandler());
		handlers.put(Opcodes.MSG_CREATEMAP, new CreateMapHandler());
		handlers.put(Opcodes.MSG_JOINMAPMSG, new JoinMapHandler());
		handlers.put(Opcodes.MSG_INNERSERVERCONNLIST, new InnerServerConnListHandler());
		
		opcodes = new LinkedList<>();
		opcodes.addAll(handlers.keySet());
	}	
	
	public static SceneServerManager getInstance() {
		if (instance == null) {
			instance = new SceneServerManager();
			instance.process = new ServerProcessBlock() {
				@Override
				public void run(int diff) {
					instance.doInit();
				}
			};
		}
		return instance;
	}
	
	public SceneServerManager() {
		super(IdentityConst.SERVER_TYPE_SCENE, opcodes);
		excutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				long prev = instance.timestamp;
				long now = System.currentTimeMillis();
				int diff = 0;
				if (prev > 0) diff = (int) (now - prev);
				instance.timestamp = now;
				if (diff < 0 || diff > 1000) {
					logger.warnLog("too heavy logical that execute");
				}
				try {
					instance.doWork(diff);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}, 1000, 100, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 进行帧刷新
	 */
	private void doWork(int diff) {
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
			objManager.parseBinlogInfo(binlogInfo);
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
	
	private void doProcess(int diff) {
		if (this.process != null) {
			this.process.run(diff);
		}
	}
	
	private void dealPacket() {
		while (!packetQueue.isEmpty()) {
			Packet packet = packetQueue.poll();
			this.invokeHandler(packet);
		}
	}
	
	private void invokeHandler(Packet packet) {
		int opcode = packet.getOpcode();
		Handler handle = handlers.get(opcode);
		if (handle != null) {
			handle.handle(null, this.centerChannel, packet);
		}
	}
	
	public void pushPacket(Packet packet) {
		synchronized (this) {
			packetQueue.add(packet);
		}
	}
	
	protected void addTagWatchCallback(String ownerTag, Callback<Void> callback) {
		objManager.addCreateCallback(ownerTag, callback);
	}

	public SceneObjectManager getObjManager() {
		return objManager;
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
	
	public void updateWaitJoin() {
		
		int now = TimestampUtils.now();
		List<String> removeList = new LinkedList<>();
		
		for (Entry<String, WaitJoinInfo> entryInfo : waitJoinHash.entrySet()) {
			
			String ownerId = entryInfo.getKey();
			WaitJoinInfo info = entryInfo.getValue();
			SceneContextPlayer contextPlayer = null;
			if (this.objManager.contains(ownerId)) {
				contextPlayer = (SceneContextPlayer)this.objManager.get(ownerId);
			}
			
			if (contextPlayer.getTeleportSign() == info.getSign()) {
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
