package cc.mi.scene.net;

import cc.mi.core.handler.ChannelHandlerGenerator;
import cc.mi.core.packet.Packet;
import cc.mi.scene.server.SceneServerManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SceneToGateHandler extends SimpleChannelInboundHandler<Packet> implements ChannelHandlerGenerator {
	
	public void channelActive(final ChannelHandlerContext ctx) {
		SceneServerManager.getInstance().onGateConnected(ctx.channel());
	}
	
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		
	}
	
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final Packet coder) throws Exception {
		//TODO: 这里应该不会有, 我们只做从本服到网关服的单向通信
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		SceneServerManager.getInstance().onGateDisconnected(ctx.channel());
		ctx.fireChannelInactive();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		throwable.printStackTrace();
		ctx.close();
	}

	@Override
	public ChannelHandler newChannelHandler() {
		return new SceneToGateHandler();
	}
}
