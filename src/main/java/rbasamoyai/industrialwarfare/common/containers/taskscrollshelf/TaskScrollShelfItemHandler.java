package rbasamoyai.industrialwarfare.common.containers.taskscrollshelf;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;

public class TaskScrollShelfItemHandler extends ItemStackHandler {

	public TaskScrollShelfItemHandler(int count) {
		super(count);
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack result = stack;
		if (stack.getItem() instanceof TaskScrollItem) result = super.insertItem(slot, stack, simulate);
		return result;
	}
	
	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}
	
}
