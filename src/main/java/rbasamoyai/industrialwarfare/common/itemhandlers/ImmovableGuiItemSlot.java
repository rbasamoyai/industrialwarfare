package rbasamoyai.industrialwarfare.common.itemhandlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ImmovableGuiItemSlot extends Slot {

	public ImmovableGuiItemSlot(IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPickup(PlayerEntity player) {
		return false;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

}
