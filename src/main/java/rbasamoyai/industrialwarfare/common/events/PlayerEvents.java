package rbasamoyai.industrialwarfare.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE)
public class PlayerEvents {

	@SubscribeEvent
	public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
		Entity e = event.getEntity();
		if (e == null) return;
		if (!(e instanceof PlayerEntity)) return;
		
		World world = event.getWorld();
		if (world == null) return;
		if (!(world instanceof ServerWorld)) return;
		
		PlayerEntity player = (PlayerEntity) e;
		
		DiplomacySaveData diplomacyData = DiplomacySaveData.get(world);
		if (!diplomacyData.hasPlayerIdTag(PlayerIDTag.of(player))) {
			diplomacyData.initPlayerDiplomacyStatuses(player);
			IndustrialWarfare.LOGGER.info("Initialized diplomacy for new player {} ({}) and updated diplomacy data", player.getGameProfile().getName(), player.getUUID());
		}
	}
	
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		PlayerEntity player = event.getPlayer();
		Entity target = event.getTarget();
		Hand hand = event.getHand();
		
		ItemStack stack = player.getItemInHand(hand);
		
		if (event.isCancelable() && stack.getItem() instanceof FirearmItem) {
			if (target instanceof ItemFrameEntity) {
				ItemFrameEntity frame = (ItemFrameEntity) target;
				frame.interact(player, hand);
			}
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player.swingingArm == null) return;
		
		ItemStack stack = player.getItemInHand(player.swingingArm);
		
		if (stack.getItem() instanceof FirearmItem && event.isCancelable()) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		PlayerEntity player = event.getPlayer();
		if (player.swingingArm == null) return;
		
		ItemStack stack = player.getItemInHand(player.swingingArm);
		
		if (stack.getItem() instanceof FirearmItem && event.isCancelable()) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
		PlayerEntity player = event.getPlayer();
		Hand hand = player.getUsedItemHand();
		if (hand == null) return;
		
		ItemStack stack = player.getItemInHand(hand);
		
		if (stack.getItem() instanceof FirearmItem) {
			if (!FirearmItem.isMeleeing(stack) && event.isCancelable()) {
				
			}
		}
	}
	
}
