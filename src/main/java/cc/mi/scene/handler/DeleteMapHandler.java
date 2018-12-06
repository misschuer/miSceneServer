package cc.mi.scene.handler;

import cc.mi.core.generate.msg.DeleteMap;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;
import cc.mi.scene.sceneMap.SceneMap;
import io.netty.channel.Channel;

public class DeleteMapHandler extends HandlerImpl {

	@Override
	public void handle(ServerContext nil, Channel channel, Packet decoder) {
		DeleteMap packet = (DeleteMap) decoder;
		SceneMap.delMap(packet.getInstId());
	}

}
