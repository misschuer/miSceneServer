package cc.mi.scene.server;

import cc.mi.core.constance.PlayerEnumFields;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.server.PlayerBase;
import cc.mi.core.server.SessionStatus;
import cc.mi.scene.element.ScenePlayer;

public class SceneContextPlayer extends PlayerBase {
	static final CustomLogger logger = CustomLogger.getLogger(SceneContextPlayer.class);
	private final SceneContext context;
	
	private ScenePlayer player;

	
	public SceneContextPlayer() {
		super(PlayerEnumFields.PLAYER_INT_FIELDS_SIZE, PlayerEnumFields.PLAYER_STR_FIELDS_SIZE);
		this.context = new SceneContext(0);
	}
	
	public void onTeleportOK(int fd, int mapId, int instId, float x, float y) {
		if (this.player != null) {
			throw new RuntimeException("onTeleportOK ScenePlayer must be null");
		}
		
		//生成玩家对象
		player = new ScenePlayer();
		
		SceneMap inst = SceneMap.findInstance(instId, mapId);
		if (inst == null) {
			throw new RuntimeException("onTeleportOK map inst is not find");
		}
		
		context.changeFd(fd);
		this.setTeleportInfo(0, 0, 0, 0, this.getTeleportExt());
		this.setMapId(mapId);
		this.setMapLineNo(inst.getLineNo());
		this.setPosition(x, y);
		
		//改变状态
		this.player.create(this);
		
		this.context.setStatus(SessionStatus.STATUS_LOGGEDIN);
//		//发个登录场景服完毕的包给客户端
//		Call_join_or_leave_server(m_delegate_sendpkt, 0, SERVER_TYPE_SCENED, getpid(), ScenedApp::g_app->Get_Connection_ID(), uint32(time(nullptr)));
		
		inst.joinPlayer(player);
//		//从玩家下标初始化场次id和跨服类型
//		instance->InitWaridAndKuafuType(this);
//		//发送属性重算 (新建角色才需要重算)
//		if (this->GetForce() == 0) {
//			ScenedApp::g_app->call_recalculate(guid());
//		}
//
		logger.devLog("onTeleportOK %s", player.getGuid());
	}
	
	public SceneContext getContext() {
		return context;
	}
}
