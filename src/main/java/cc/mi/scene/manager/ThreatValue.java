package cc.mi.scene.manager;

public class ThreatValue {
	// 攻击仇恨
	protected int attackHatred;
	// 距离仇恨
	protected int distantHatred;
	
	public ThreatValue() {}

	public void setAttackHatred(int attackHatred) {
		this.attackHatred = attackHatred;
	}

	public void setDistantHatred(int distantHatred) {
		this.distantHatred = distantHatred;
	}
	
	public int modify(int v) {
		this.attackHatred += v;
		return this.attackHatred;
	}
	
	public int getHatred() {
		return this.attackHatred + this.distantHatred;
	}
}
