package cc.mi.scene.handler;

import cc.mi.core.generate.msg.CreateMap;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;
import cc.mi.scene.sceneMap.SceneMap;
import io.netty.channel.Channel;

public class CreateMapHandler extends HandlerImpl {
	
	static final CustomLogger logger = CustomLogger.getLogger(CreateMapHandler.class);
	
	@Override
	public void handle(ServerContext nil, Channel channel, Packet decoder) {
		CreateMap cm = (CreateMap) decoder;
		logger.devLog("CreateMapHandler {} {} {}", cm.getMapId(), cm.getInstId(), cm.getExt());
		if (!SceneMap.containsInstance(cm.getInstId(), cm.getMapId())) {
			SceneMap.createInstance(cm.getInstId(), cm.getMapId(), cm.getLineNo(), cm.getExt());
		}
	}

}
