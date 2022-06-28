package rbasamoyai.industrialwarfare.common.containers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ImmovableGuiItemSlot extends Slot {

	public ImmovableGuiItemSlot(Container inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPickup(Player player) {
		return false;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

}
