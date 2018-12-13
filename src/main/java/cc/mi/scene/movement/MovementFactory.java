package cc.mi.scene.movement;

import java.util.HashMap;
import java.util.Map;

import cc.mi.core.constance.MovementType;

public class MovementFactory {
	protected static final Map<Integer, MovementBase> hash = new HashMap<>();
	
	static {
		hash.put(MovementType.IDLE, 		new IdleMovement());
		hash.put(MovementType.MAIN_LOAD,	new MainLoadMovement());
		hash.put(MovementType.TRACE,		new TraceMovement());
		hash.put(MovementType.HUNTING,		new HuntingMovement());
		
	}
	
	public static MovementBase getMovement(int moveType) {
		MovementBase base = hash.get(moveType);
		return base != null ? base.newInstance() : new IdleMovement();
	}
}
