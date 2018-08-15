package cc.mi.scene.info;

import cc.mi.core.utils.TimestampUtils;

public class WaitJoinInfo {
	// 客户端fd
	private final int fd;
	private final int instId;
	private final int mapId;
	private final float x;
	private final float y;
	private final int createTime;
	private final byte sign;
	
	public WaitJoinInfo(int fd, int instId, int mapId, 
			float x, float y, byte sign) {
		
		this.fd 	= 	  fd;
		this.instId = instId;
		this.mapId	=  mapId;
		this.x		=	   x;
		this.y		=	   y;
		this.createTime = TimestampUtils.now();
		this.sign	=	 sign;
	}

	public int getFd() {
		return fd;
	}

	public int getInstId() {
		return instId;
	}

	public int getMapId() {
		return mapId;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public int getCreateTime() {
		return createTime;
	}

	public byte getSign() {
		return sign;
	}
}
