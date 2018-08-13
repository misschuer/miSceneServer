package cc.mi.scene.grid;

import cc.mi.core.utils.Mask;

public class GridUpdateMask {
	private final Mask createIntMask;
	private final Mask createStrMask;
	private final Mask updateIntMask;
	private final Mask updateStrMask;
	
	public GridUpdateMask(int intSize, int strSize) {
		this.createIntMask = new Mask(intSize);
		this.createStrMask = new Mask(strSize);
		this.updateIntMask = new Mask(intSize);
		this.updateStrMask = new Mask(strSize);
	}

	public Mask getCreateIntMask() {
		return createIntMask;
	}

	public Mask getCreateStrMask() {
		return createStrMask;
	}

	public Mask getUpdateIntMask() {
		return updateIntMask;
	}

	public Mask getUpdateStrMask() {
		return updateStrMask;
	}
}
