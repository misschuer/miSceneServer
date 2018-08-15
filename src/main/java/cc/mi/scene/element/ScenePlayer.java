package cc.mi.scene.element;

import cc.mi.scene.info.CharacterStatistics;
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
	
	public void update(int diff) {
		super.update(diff);
	}

	public CharacterStatistics getCs() {
		return cs;
	}
	
	public void create(SceneContextPlayer contextPlayer) {
		this.contextPlayer = contextPlayer;
		
//		//创建guid
//		string lguid = Map::CreateNewCreatureID();
//		sprintf(m_tmp, "%s.%s", lguid.c_str(), m_session->GetGuid().c_str());
//		SetGuid(m_tmp);
//		_Create(m_tmp, 0);
//		SetTypeId(TYPEID_PLAYER);
//
//		//给对象设置一个ms级别的创建时间
//		const uint32 KAIFU_TIME = 1429322400;
//		struct timeval tmnow;	
//		gettimeofday(&tmnow, NULL);	
//		SetGOData((tmnow.tv_sec-KAIFU_TIME)*1000+tmnow.tv_usec/1000);
//
//		OnAfterPlayerDataUpdate(m_session, OBJ_OPT_NEW, mask_, mask_);
//		m_session->after_update(std::bind(&Player::OnAfterPlayerDataUpdate,this,std::placeholders::_1,std::placeholders::_2,std::placeholders::_3,std::placeholders::_4));
	}
}
