package cc.mi.scene.handler;

import cc.mi.core.generate.msg.JoinMapMsg;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.scene.server.SceneServerManager;
import io.netty.channel.Channel;

public class JoinMapHandler extends HandlerImpl {
	@Override
	public void handle(ServerContext nil, Channel channel, Packet decoder) {
		
		JoinMapMsg jmm = (JoinMapMsg) decoder;
		// 加入地图的时候不能有context
		if (ContextManager.getContext(jmm.getFd()) != null) {
			throw new RuntimeException("scene context must be null");
		}
		
		// 加入地图的时候不能有binlog
		if (SceneServerManager.getInstance().getObjManager().get(jmm.getOwnerId()) != null) {
			throw new RuntimeException("owner binlogdata must be null");
		}
		
		// 已经在等待加入列表了
		if (SceneServerManager.getInstance().isInWaitJoin(jmm.getOwnerId())) {
			return;
		}
		
		SceneServerManager.getInstance().putWaitJoin(
			jmm.getOwnerId(), jmm.getFd(), jmm.getInstId(), 
			jmm.getTeleMapId(), jmm.getX(), jmm.getY(), jmm.getSign()
		);
		
		SceneServerManager.getInstance().addWatchAndCall(jmm.getOwnerId());
	}
}
