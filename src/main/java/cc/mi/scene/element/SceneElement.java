package cc.mi.scene.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.binlog.stru.BinlogStruValueInt;
import cc.mi.core.binlog.stru.BinlogStruValueStr;
import cc.mi.core.constance.BinlogOptType;
import cc.mi.core.generate.stru.UnitBinlogInfo;
import cc.mi.core.utils.Mask;

public abstract class SceneElement extends BinlogData {

	public static final int ELEMENT_TYPE_PLAYER		= 1;
	public static final int ELEMENT_TYPE_CREATURE	= 2;
	public static final int ELEMENT_TYPE_GAMEOBJECT	= 3;

	private final int elementType;

	public SceneElement(int elementType) {
		super(64, 64);
		this.elementType = elementType;
	}

	public int getElementType() {
		return elementType;
	}
	
	public int getUintId() {
		//TODO: guid的uint表示
		return 0;
	}
	
	public UnitBinlogInfo packNewElementBinlogInfo() {
		return this.packNewElementBinlogInfo(null, null);
	}
	
	public UnitBinlogInfo packNewElementBinlogInfo(Mask createIntMask, Mask createStrMask) {
		UnitBinlogInfo data = new UnitBinlogInfo();
		data.setState(BinlogOptType.OPT_NEW);
		data.setBinlogId(this.guid);
		data.setUintId(0);
		
		tmpIntMask.clear();
		int size = this.intValues.intSize();
		List<Integer> newIntList = new ArrayList<>(size);
		for (int i = 0; i < size; ++ i) {
			int value = this.intValues.getInt(i);
			if (value > 0 && (createIntMask == null || createIntMask.isMarked(i))) {
				newIntList.add(value);
				tmpIntMask.mark(i);
			}
		}
		data.setIntMask(tmpIntMask.toNewList());
		data.setIntValues(newIntList);
		
		tmpStrMask.clear();
		List<String> newStrList = new ArrayList<>(strValues.capacity());
		for (int i = 0; i < this.strValues.capacity(); ++ i) {
			String str = this.strValues.get(i);
			if (str != null && !"".equals(str) && (createStrMask == null || createStrMask.isMarked(i))) {
				newStrList.add(str);
				tmpStrMask.mark(i);
			}
		}
		data.setStrMask(tmpStrMask.toNewList());
		data.setStrValues(newStrList);
		
		return data;
	}
	
	public UnitBinlogInfo packUpdateElementBinlogInfo() {
		return this.packUpdateElementBinlogInfo(null, null);
	}
	
	public UnitBinlogInfo packUpdateElementBinlogInfo(Mask updateIntMask, Mask updateStrMask) {
		UnitBinlogInfo data = new UnitBinlogInfo();
		data.setState(BinlogOptType.OPT_UPDATE);
		data.setBinlogId("");
		data.setUintId(this.getUintId());
		
		// 先排序后处理		
		List<Integer> indice = new ArrayList<>(bsIntIndxHash.size());
		for (int indx : bsIntIndxHash.keySet()) {
			indice.add(indx);
		}
		Collections.sort(indice);
		tmpIntMask.clear();
		int size = this.intValues.intSize();
		List<Integer> newIntList = new ArrayList<>(size);
		for (int indx : indice) {
			BinlogStruValueInt bs = bsIntIndxHash.get(indx);
			if (updateIntMask == null || updateIntMask.isMarked(indx)) {
				tmpIntMask.mark(indx);
				newIntList.add(bs.getValue());
			}
		}
		data.setIntMask(tmpIntMask.toNewList());
		data.setIntValues(newIntList);
		
		// 先排序后处理		
		indice = new ArrayList<>(bsStrIndxHash.size());
		for (int indx : bsStrIndxHash.keySet()) {
			indice.add(indx);
		}
		Collections.sort(indice);
		tmpStrMask.clear();
		size = this.strValues.capacity();
		List<String> newStrList = new ArrayList<>(size);
		for (int indx : indice) {
			BinlogStruValueStr bs = bsStrIndxHash.get(indx);
			if (updateStrMask == null || updateStrMask.isMarked(indx)) {
				tmpStrMask.mark(indx);
				newStrList.add(bs.getValue());
			}
		}
		data.setStrMask(tmpStrMask.toNewList());
		data.setStrValues(newStrList);
		
		bsIntIndxHash.clear();
		bsStrIndxHash.clear();
		
		return data;
	}
}
