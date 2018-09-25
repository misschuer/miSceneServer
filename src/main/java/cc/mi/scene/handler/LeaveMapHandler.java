package cc.mi.scene.handler;

import cc.mi.core.generate.msg.PlayerLeaveMap;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;
import cc.mi.scene.server.SceneContextPlayer;
import cc.mi.scene.server.SceneServerManager;
import io.netty.channel.Channel;

public class LeaveMapHandler extends HandlerImpl {
	static final CustomLogger logger = CustomLogger.getLogger(LeaveMapHandler.class);
	
	@Override
	public void handle(ServerContext nil, Channel channel, Packet decoder) {
		
		PlayerLeaveMap packet = (PlayerLeaveMap) decoder;
		String guid = packet.getGuid();
		int fd = packet.getClientFd();
		logger.devLog("on_leave_map guid = {} fd = {}", guid, fd);
		
		//因为有可能是登录服崩溃期间下线的玩家，登录服无法知道他的fd
		//所以，就用guid来获取玩家session对象
		SceneContextPlayer contextPlayer = (SceneContextPlayer)SceneServerManager.getInstance().getObjManager().get(guid);
		//玩家传送完毕
		if (contextPlayer != null && contextPlayer.getContext() != null && contextPlayer.getContext().getFd() > 0) {
			//登录服崩溃重启的时候，玩家离线会发一个fd为0的过来，因为确实不知道他之前fd多少
			contextPlayer.leaveScene();
		} else {
			//要离开地图了，但是数据还没到
			logger.devLog("on_leave_map guid = {} fd = {}, but data not found", guid, fd);
			SceneServerManager.getInstance().removeWaitJoin(guid);
		}
		
		if (contextPlayer != null) {
//			ObjMgr.SendPlayerBinlog(session);
		}

//		ObjMgr.CallDelWatch(guid);
		//为了防止场景切换
//		ObjMgr.ReleaseObject(guid);
	}

}
