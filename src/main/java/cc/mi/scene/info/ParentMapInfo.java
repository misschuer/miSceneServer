package cc.mi.scene.info;

public class ParentMapInfo {
	// 地图id
	protected int mapId;
	// 实例id
	protected int instId;
	// 分线号
	protected int lineNo;
	// 创建时间
	protected int createTime;
	// 实例类型
	protected int instType;
	// 参数
	protected String ext;
	
	public ParentMapInfo() {}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getInstId() {
		return instId;
	}

	public void setInstId(int instId) {
		this.instId = instId;
	}

	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public int getInstType() {
		return instType;
	}

	public void setInstType(int instType) {
		this.instType = instType;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}
}
