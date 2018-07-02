package cc.mi.scene.net;

import cc.mi.core.handler.ChannelHandlerGenerator;
import cc.mi.core.packet.Packet;
import cc.mi.scene.server.SceneServerManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SceneHandler extends SimpleChannelInboundHandler<Packet> implements ChannelHandlerGenerator {
	
	public void channelActive(final ChannelHandlerContext ctx) {
		SceneServerManager.getInstance().onCenterConnected(ctx.channel());
	}
	
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final Packet coder) throws Exception {
		
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		SceneServerManager.getInstance().onCenterDisconnected(ctx.channel());
		ctx.fireChannelInactive();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		throwable.printStackTrace();
		ctx.close();
	}

	@Override
	public ChannelHandler newChannelHandler() {
		return new SceneHandler();
	}
}
