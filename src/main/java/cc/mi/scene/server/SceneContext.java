package cc.mi.scene.server;

import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;

public class SceneContext extends ServerContext {

	public SceneContext(int fd) {
		super(fd);
	}

	@Override
	public void sendToGate(Packet coder) {
		SceneServerManager.getInstance().sendToGate(coder);
	}

	@Override
	public void sendToCenter(Packet coder) {
		SceneServerManager.getInstance().sendToCenter(coder);
	}

	@Override
	public void closeSession(int type) {
		SceneServerManager.getInstance().closeSession(this.getFd(), type);
	}

}
