package rbasamoyai.industrialwarfare.common.items.firearms.complete;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.items.renderers.FirearmRenderer;
import rbasamoyai.industrialwarfare.common.containers.attachmentitems.AttachmentsRifleMenu;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.SingleShotFirearmItem;
import rbasamoyai.industrialwarfare.core.init.SoundEventInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.AnimBroadcastUtils;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class SharpsCartridgeFirearmItem extends SingleShotFirearmItem {

	public SharpsCartridgeFirearmItem() {
		super(new Item.Properties()
							.stacksTo(1)
							.durability(1200)
							.tab(IWItemGroups.TAB_WEAPONS),
					new FirearmItem.Properties()
							.ammoPredicate(s -> s.getItem() == ItemInit.AMMO_GENERIC.get() || s.getItem() == ItemInit.INFINITE_AMMO_GENERIC.get())
							.baseDamage(25.0f)
							.headshotMultiplier(4.0f)
							.spread(0.05f)
							.hipfireSpread(5.0f)
							.muzzleVelocity(8.0f)
							.horizontalRecoil(e -> 2.0f * (float) e.getRandom().nextGaussian())
							.verticalRecoil(e -> 6.5f + 3.0f * e.getRandom().nextFloat())
							.cooldownTime(20)
							.drawTime(10)
							.reloadTime(70)
							.projectileRange(150) // Crank up your server chunk rendering/processing distance
							.fovModifier(0.5f)
							.needsCycle(false));

	}
	
	@Override public boolean shouldHideCrosshair(ItemStack stack) { return super.shouldHideCrosshair(stack); }
	@Override public boolean canOpenScreen(ItemStack stack) { return false; }

	private static final Component TITLE = new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".attachments_rifle");
	@Override 
	public MenuProvider getItemContainerProvider(ItemStack stack) {
		return new SimpleMenuProvider(AttachmentsRifleMenu.getServerContainerProvider(stack), TITLE);
	}
	
	/*
	 * ANIMATION CONTROL METHODS
	 */
	
	private static final float HAMMER_ROT = (float) Math.toRadians(-15.0d);
	
	@Override
	protected void onSelect(ItemStack firearm, LivingEntity shooter) {
		super.onSelect(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnim(firearm, shooter, this, ANIM_SELECT_FIREARM);
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("select_firearm", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	@Override
	protected void doNothing(ItemStack firearm, LivingEntity shooter) {
		super.doNothing(firearm, shooter);
		if (!shooter.level.isClientSide) {
			int animId;
			String animStr;
			if (isAiming(firearm)) {
				animId = ANIM_ADS_AIMING;
				animStr = "ads_aiming";
			} else if (shooter.isSprinting()) {
				animId = ANIM_PORT_ARMS;
				animStr = "port_arms";
			} else {
				animId = ANIM_HIP_AIMING;
				animStr = "hip_aiming";
			}
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, animId);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", animStr, true, 1.0f);
		}
	}
	
	@Override
	public void startSprinting(ItemStack firearm, LivingEntity shooter) {
		super.startSprinting(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_PORT_ARMS);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", "port_arms", true, 1.0f);
		}
	}
	
	@Override
	public void stopSprinting(ItemStack firearm, LivingEntity shooter) {
		super.stopSprinting(firearm, shooter);
		this.doNothing(firearm, shooter);
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		super.shoot(firearm, shooter);
		if (!shooter.level.isClientSide) {
			ServerLevel slevel = (ServerLevel) shooter.level;
			shooter.level.playSound(null, shooter, SoundEventInit.SNIPER_RIFLE_FIRED.get(), SoundSource.MASTER, 5.0f, 1.0f);
			
			Vec3 viewVector = shooter.getViewVector(1.0f);
			Vec3 smokePos = shooter.getEyePosition(1.0f).add(viewVector.scale(2.0d));
			Vec3 smokeDelta = viewVector.scale(0.5d);
			int count = 35 + shooter.getRandom().nextInt(36);
			for (ServerPlayer splayer : slevel.getPlayers(p -> true)) {
				slevel.sendParticles(splayer, ParticleTypes.POOF, true, smokePos.x, smokePos.y, smokePos.z, count, smokeDelta.x, smokeDelta.y, smokeDelta.z, 0.02d);
			}
			
			boolean isAiming = isAiming(firearm);
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, isAiming ? ANIM_ADS_FIRING : ANIM_HIP_FIRING);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(isAiming ? "ads_firing" : "hip_firing", false));
			upperBody.add(new Tuple<>(isAiming ? "ads_aiming" : "hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		if (isAiming(firearm)) return;
		super.startReload(firearm, shooter);
		if (!shooter.level.isClientSide) {			
			boolean fired = isFired(firearm);
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, fired ? ANIM_RELOAD_EXTRACT : ANIM_RELOAD);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(fired ? "reload_extract" : "reload", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	public void startAiming(ItemStack firearm, LivingEntity shooter) {
		super.startAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_ADS_AIMING_START);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("ads_aiming_start", false));
			upperBody.add(new Tuple<>("ads_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	@Override
	public void stopAiming(ItemStack firearm, LivingEntity shooter) {
		super.stopAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_ADS_AIMING_STOP);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("ads_aiming_stop", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	/*
	 * FIRST PERSON ANIMATION METHODS
	 */

	@Override
	public void registerControllers(AnimationData data) {
		AnimationController<?> controller = new AnimationController<>(this, "controller", 1, this::firstPersonPredicate);
		controller.registerSoundListener(this::soundListener);
		controller.registerCustomInstructionListener(this::customInstructionListener);
		controller.registerParticleListener(this::particleListener);
		controller.markNeedsReload();
		data.addAnimationController(controller);
	}
	
	private <E extends Item & IAnimatable> PlayState firstPersonPredicate(AnimationEvent<E> event) {
		return PlayState.CONTINUE;
	}
	
	@Override
	public void setupAnimationState(FirearmRenderer renderer, ItemStack stack, PoseStack matrixStack, float aimProgress) {
		if (renderer.getUniqueID(this).intValue() == -1) return;
		
		getDataHandler(stack).ifPresent(h -> {
			if (h.getAction() == ActionType.NOTHING) {
				renderer.setBoneRotation("hammer", h.hasAmmo() ? 0.0f : HAMMER_ROT, 0.0f, 0.0f);
			}
		});
	}

	public static final int ANIM_PORT_ARMS = 00;
	public static final int ANIM_SELECT_FIREARM = 01;
	public static final int ANIM_HIP_AIMING = 10;
	public static final int ANIM_HIP_FIRING = 11;
	public static final int ANIM_ADS_AIMING = 20;
	public static final int ANIM_ADS_FIRING = 21;
	public static final int ANIM_ADS_AIMING_START = 22;
	public static final int ANIM_ADS_AIMING_STOP = 23;
	public static final int ANIM_RELOAD = 30;
	public static final int ANIM_RELOAD_EXTRACT = 31;
	
	@Override
	public void onAnimationSync(int id, int state) {
		AnimationBuilder builder = new AnimationBuilder();
		switch (state) {
		case ANIM_PORT_ARMS: builder.addAnimation("port_arms", true); break;
		case ANIM_HIP_AIMING: builder.addAnimation("hip_aiming", true); break;
		case ANIM_HIP_FIRING:
			builder
			.addAnimation("hip_firing", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_RELOAD:
			builder
			.addAnimation("reload", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_RELOAD_EXTRACT:
			builder
			.addAnimation("reload_extract", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_ADS_AIMING: builder.addAnimation("ads_aiming", true); break;
		case ANIM_ADS_AIMING_START:
			builder
			.addAnimation("ads_aiming_start", false)
			.addAnimation("ads_aiming", true);
			break;
		case ANIM_ADS_AIMING_STOP:
			builder
			.addAnimation("ads_aiming_stop", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_ADS_FIRING:
			builder
			.addAnimation("ads_firing", false)
			.addAnimation("ads_aiming", true);
			break;
		case ANIM_SELECT_FIREARM:
			builder
			.addAnimation("select_firearm", false)
			.addAnimation("hip_aiming", true);
			break;
		}
		
		final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, "controller");
		controller.markNeedsReload();
		controller.setAnimation(builder);
	}

	@Override
	public boolean shouldSpecialRender(ItemStack stack, LivingEntity entity) {
		return entity instanceof AbstractClientPlayer || entity instanceof NPCEntity;
	}
	
	@Override
	public void onPreRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks,
			PoseStack stack, MultiBufferSource bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {
		super.onPreRender(entity, animatable, entityYaw, partialTicks, stack, bufferIn, packedLightIn, renderer);
		
		ItemStack item = entity.getMainHandItem();
		
		getDataHandler(item).ifPresent(h -> {
			if (h.getAction() == ActionType.NOTHING) {
				renderer.setBoneRotation("hammer", h.hasAmmo() ? 0.0f : HAMMER_ROT, 0.0f, 0.0f);
			}
		});
	}

	private static final ResourceLocation ANIM_FILE_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "animations/third_person/sharps_cartridge_t.animation.json");
	@Override
	public ResourceLocation getAnimationFileLocation(ItemStack stack, LivingEntity entity) {
		return ANIM_FILE_LOC;
	}

	private static final ResourceLocation MODEL_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "geo/third_person/sharps_cartridge_t.geo.json");
	@Override
	public ResourceLocation getModelLocation(ItemStack stack, LivingEntity entity) {
		return MODEL_LOC;
	}

	@Override
	public AnimationBuilder getDefaultAnimation(ItemStack stack, LivingEntity entity, AnimationController<?> controller) {
		return (new AnimationBuilder())
				.addAnimation("select_firearm", false)
				.addAnimation("hip_aiming", true);
	}

}
