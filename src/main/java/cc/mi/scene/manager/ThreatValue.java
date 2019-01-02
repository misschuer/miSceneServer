package cc.mi.scene.manager;

import cc.mi.scene.element.SceneElement;

public class ThreatValue {
	// 攻击仇恨
	protected int attackHatred;
	// 距离仇恨
	protected int distantHatred;
	// 目标元素
	protected final SceneElement target;
	
	public ThreatValue(SceneElement target) {
		this.target = target;
	}

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

	public SceneElement getTarget() {
		return target;
	}
}
