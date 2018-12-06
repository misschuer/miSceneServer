package cc.mi.scene.element;

import java.util.Map;

import cc.mi.core.binlog.callback.BinlogUpdateCallback;
import cc.mi.core.binlog.callbackParam.BinlogUpdateCallbackParam;
import cc.mi.core.constance.BinlogOptType;
import cc.mi.scene.info.CharacterStatistics;
import cc.mi.scene.sceneMap.SceneMap;
import cc.mi.scene.server.SceneContextPlayer;

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
	
	@Override
	public boolean update(int diff) {
		return super.update(diff);
	}

	public CharacterStatistics getCs() {
		return cs;
	}
	
	public void onJoinMap() {
//		GetBuffManager()->UpDateHasBuffId();
//		//清除不该存在这世界上的BUFF
//		DoOnlineClearBuff(this);
//
//		//复活定时器
//		m_live_timer.Reset(GetMap()->GetPlayerAutoRespanTime());
//		if (this->isAlive() && this->GetHealth() == 0) {
//			SetUInt32(UNIT_FIELD_HEALTH, this->GetMaxHealth());
//			tea_pdebug("============================alived but why health == 0");
//		}
//
//		if (!m_pets.empty())
//		{
//			tea_perror("assert error: Player::OnJoinMap ASSERT(m_pets.empty())");
//			GetSession()->Close(PLAYER_CLOSE_OPERTE_SCREND_ONE36,"");
//			return;
//		}
//
//		//主动给客户端同步下服务器时间
//		uint32 time_now = ms_time();
//		packet *_pkt;
//		pack_sync_mstime(&_pkt,time_now,(uint32)time(NULL),ScenedApp::g_app->m_open_time);
//		m_session->SendPacket(*_pkt);
//		external_protocol_free_packet(_pkt);
	}

	public void onLeaveMap() {
//		if(IsMoving())
//			StopMoving(false);
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
	
	// 玩家数据拷贝
	protected void onAfterPlayerDataUpdate(byte flags,
			Map<Integer, Integer> intValueHash,
			Map<Integer, String> strValueHash) {
		
//		boolean isNew = (BinlogOptType.OPT_NEW & flags) > 0;
		//TODO: 玩家对象同步到场景对象
	}
	
	// 玩家数据同步
	protected void syncUnitToPlayerData() {
		if (this.posSynchronous) {
			if (this.contextPlayer.getPositionX() != this.positionX
				|| this.contextPlayer.getPositionY() != this.positionY) {
				this.contextPlayer.setPosition(this.positionX, this.positionY);
				this.contextPlayer.setOrientation(orient);
			}
			this.posSynchronous = false;
		}
		//TODO: 场景对象同步到玩家对象
	}
	
}
