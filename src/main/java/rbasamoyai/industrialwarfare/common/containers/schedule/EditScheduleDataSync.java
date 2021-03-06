package rbasamoyai.industrialwarfare.common.containers.schedule;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import rbasamoyai.industrialwarfare.utils.TimeUtils;

public class EditScheduleDataSync implements ContainerData {
	
	private final Level world;
	private final int maxMinutes;
	private final int maxShifts;
	
	public EditScheduleDataSync(Level world, int maxMinutes, int maxShifts) {
		this.world = world;
		this.maxMinutes = maxMinutes;
		this.maxShifts = maxShifts;
	}
	
	@Override
	public int get(int index) {
		switch (index) {
		case 0: return (int)((this.world.getDayTime() % TimeUtils.WEEK_TICKS + TimeUtils.TIME_OFFSET) / TimeUtils.MINUTE_TICKS);
		case 1: return this.maxMinutes;
		case 2: return this.maxShifts;
		default: return 0;
		}
	}

	@Override
	public void set(int index, int value) {
		
	}

	@Override
	public int getCount() {
		return 3;
	}

}
