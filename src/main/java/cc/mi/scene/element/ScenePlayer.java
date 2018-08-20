package cc.mi.scene.element;

import java.util.Map;

import cc.mi.core.binlog.callback.BinlogUpdateCallback;
import cc.mi.core.binlog.callbackParam.BinlogUpdateCallbackParam;
import cc.mi.core.constance.BinlogOptType;
import cc.mi.scene.info.CharacterStatistics;
import cc.mi.scene.server.SceneContextPlayer;
import cc.mi.scene.server.SceneMap;

public class ScenePlayer extends SceneElement {
	
	private SceneContextPlayer contextPlayer;
	private final CharacterStatistics cs;
	
	public ScenePlayer() {
		super(SceneElement.ELEMENT_TYPE_PLAYER);
		this.cs = new CharacterStatistics();
	}

	public SceneContextPlayer getContextPlayer() {
		return contextPlayer;
	}
	
	public void update(int diff) {
		super.update(diff);
	}

	public CharacterStatistics getCs() {
		return cs;
	}
	
	public void create(SceneContextPlayer contextPlayer) {
		this.contextPlayer = contextPlayer;
		
		//创建guid
		String guid = SceneMap.newSceneElementId();
		guid = String.format("%s.%s", guid, contextPlayer.getGuid());
		this.setElementGuid(guid);
		this.setEntry(0);
		
//		//给对象设置一个ms级别的创建时间
//		const uint32 KAIFU_TIME = 1429322400;
//		struct timeval tmnow;	
//		gettimeofday(&tmnow, NULL);	
//		SetGOData((tmnow.tv_sec-KAIFU_TIME)*1000+tmnow.tv_usec/1000);
		
		this.onAfterPlayerDataUpdate(
			BinlogOptType.OPT_NEW, 
			contextPlayer.getIntCreateHash(null), 
			contextPlayer.getStrCreateHash(null)
		);
		
		ScenePlayer self = this;
		contextPlayer.setUpdateCallback(new BinlogUpdateCallback() {
			public void invoke(BinlogUpdateCallbackParam value) {
				self.onAfterPlayerDataUpdate(value.getFlags(), value.getIntValueHash(), value.getStrValueHash());
			}
		});
	}

	private void onAfterPlayerDataUpdate(byte flags, 
			Map<Integer, Integer> intValueHash, 
			Map<Integer, String> strValueHash) {
		
//		boolean isNew = (BinlogOptType.OPT_NEW & flags) > 0;
	}
}
