package cc.mi.scene.element;

public class SceneCreature extends SceneElement {
	
	private float bornX;
	private float bornY;
	
	// 移动类型
	private int moveType;
	// 复活类型
	private int respawnType;
	// 复活时间
	private int respawnTime;
	
	public SceneCreature() {
		super(SceneElement.ELEMENT_TYPE_CREATURE);
	}
	
	public boolean update(int diff) {
		return super.update(diff);
	}
	
	private void initBase(int entry) {
		// 读模板表
	}
	
	public boolean create(String binlogId, int entry) {
		// TODO:先判断entry是否存在表中
		// 初始化
		super.onInit(binlogId, entry);
		
		this.initBase(entry);
		// 设置属性
		// name 等
		return true;
	}
	
	/**
	 * 初始化其他的 如运动方式, 仇恨管理
	 */
	public void initAction() {
//		//初始化移动控制	
//		MovementGenerator *curr = Tea::SelectMovementGenerator(this);
//		curr->Initialize(*this);
//		m_impl.push(curr);
//
//		//调用脚本初始化,如果失败则重新将脚本置空
//		if (!m_script_name.empty() && DoScriptInit(m_script_name.c_str(),this))		
//			m_script_name = "";
//			
//		//tea_pdebug("%u:[%s] init ai[%s],move_type[%u],reactState[%d]",GetEntry(), GetName(), m_script_name.c_str(), m_move_type, (int)m_reactState);
//		m_timer_say.Reset(urand(2000,240000));	
//
//		//初始化仇恨管理
//		//creature_template& data = GetTemplate();	
//		
//		m_threatMgr.Initialize(this, 0, 0);	
//		creatureInit(this);
	}

	public float getBornX() {
		return bornX;
	}

	public float getBornY() {
		return bornY;
	}

	public void setBornPos(float x, float y) {
		this.bornX = x;
		this.bornY = y;
	}

	public int getMoveType() {
		return moveType;
	}

	public void setMoveType(int moveType) {
		this.moveType = moveType;
	}

	public int getRespawnType() {
		return respawnType;
	}

	public void setRespawnType(int respawnType) {
		this.respawnType = respawnType;
	}

	public int getRespawnTime() {
		return respawnTime;
	}

	public void setRespawnTime(int respawnTime) {
		this.respawnTime = respawnTime;
	}
	
	@Override
	public boolean isCreature() {
		return true;
	}
}
