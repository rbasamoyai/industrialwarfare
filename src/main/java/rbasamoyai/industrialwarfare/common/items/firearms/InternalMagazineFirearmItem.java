package rbasamoyai.industrialwarfare.common.items.firearms;

import java.util.function.Predicate;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rbasamoyai.industrialwarfare.common.ModTags;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.InternalMagazineDataHandler;
import rbasamoyai.industrialwarfare.common.items.ISpeedloadable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.util.GeckoLibUtil;

public abstract class InternalMagazineFirearmItem extends FirearmItem implements ISpeedloadable {
	
	private final Predicate<ItemStack> speedloaderPredicate;
	
	public InternalMagazineFirearmItem(Item.Properties itemProperties, InternalMagazineFirearmItem.Properties firearmProperties) {
		super(itemProperties, firearmProperties, attachments -> {
			InternalMagazineDataHandler handler = new InternalMagazineDataHandler(attachments);
			handler.setMagazineSize(firearmProperties.magazineSize);
			return handler;
		});
		this.speedloaderPredicate = firearmProperties.speedloaderPredicate;
	}
	
	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return super.getAllSupportedProjectiles().or(this.speedloaderPredicate);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		
		if (!selected && entity instanceof LivingEntity && !level.isClientSide) {
			AnimationController<?> controller = GeckoLibUtil.getControllerForStack(this.factory, stack, "controller");
			controller.markNeedsReload();
		}
	}

	@Override
	protected void reload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			ItemStack ammo = shooter.getProjectile(firearm);
			if (h.isFull() || !this.getAllSupportedProjectiles().test(ammo)) {
				h.setAction(ActionType.NOTHING, 1);
				return;
			}
			
			if (ammo.is(ModTags.Items.CHEAT_AMMO) || shooter instanceof Player && ((Player) shooter).getAbilities().instabuild) {
				ammo = ammo.copy();
			}
			
			boolean ammoMatched = false;
			if (this.speedloaderPredicate.test(ammo) /* TODO: checking to see if can speedload, otherwise load regularly */) {
				// TODO: speedloaders
				ammoMatched = true;
			} else if (super.getAllSupportedProjectiles().test(ammo)) {
				h.insertAmmo(ammo);
				ammoMatched = true;
			}
			
			if (ammoMatched && this.getAllSupportedProjectiles().test(shooter.getProjectile(firearm)) && !h.isFull()) {
				this.midReload(firearm, shooter);
			} else if (h.hasAmmo()) {
				this.endReload(firearm, shooter);
			} else {
				h.setAction(ActionType.NOTHING, 1);
			}
		});
	}
	
	@Override
	protected void actuallyStartReloading(ItemStack firearm, LivingEntity shooter) {
		this.midReload(firearm, shooter);
	}
	
	protected void midReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.RELOADING, getTimeModifiedByEntity(shooter, this.reloadTime));
		});
		// TODO: speedloaders
	}
	
	protected void endReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.reloadEndTime));
		});
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.getCycleTime(firearm, shooter)));
		});
	}
	
	@Override
	protected void endCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setCycled(true);
			h.setFired(false);
			h.setAction(ActionType.NOTHING, 1);
		});
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			if (h.isFull()) return;
			h.setAction(ActionType.START_RELOADING, getTimeModifiedByEntity(shooter, this.getReloadStartTime(firearm, shooter)));
		});
		// TODO: speedloaders
	}
	
	protected int getReloadStartTime(ItemStack firearm, LivingEntity shooter) {
		return this.reloadStartTime;
	}
	
	@Override
	public boolean canSpeedload(ItemStack stack) {
		// TODO: speedloader capability
		return false;
	}
	
	@Override
	public Predicate<ItemStack> getSpeedloaderPredicate() {
		return this.speedloaderPredicate;
	}
	
	public static class Properties extends FirearmItem.AbstractProperties<Properties> {
		private Predicate<ItemStack> speedloaderPredicate = s -> false;
		private int magazineSize;
		
		@Override protected Properties getThis() { return this; }
		
		public Properties speedloaderPredicate(Predicate<ItemStack> speedloaderPredicate) {
			this.speedloaderPredicate = speedloaderPredicate;
			return this;
		}
		
		public Properties magazineSize(int magazineSize) {
			this.magazineSize = magazineSize; 
			return this;
		}
	}
	
}