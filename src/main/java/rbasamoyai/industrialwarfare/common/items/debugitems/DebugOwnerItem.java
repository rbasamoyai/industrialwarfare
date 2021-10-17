package rbasamoyai.industrialwarfare.common.items.debugitems;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class DebugOwnerItem extends Item {

	public DebugOwnerItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_DEBUG));
		this.setRegistryName(IndustrialWarfare.MOD_ID, "debug_owner");
	}
	
	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (player.level.isClientSide) return ActionResultType.SUCCESS;
		if (!entity.isAlive()) return ActionResultType.PASS;
		if (!(entity instanceof NPCEntity)) return ActionResultType.PASS;
		
		((NPCEntity) entity).getDataHandler().ifPresent(h -> h.setOwnerUUID(player.getUUID()));
		return ActionResultType.CONSUME;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
}
