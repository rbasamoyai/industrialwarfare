package rbasamoyai.industrialwarfare.common.items.firearms.complete;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.FirearmRenderer;
import rbasamoyai.industrialwarfare.common.containers.attachmentitems.AttachmentsRifleContainer;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.SingleShotFirearmItem;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.AnimUtils;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.GeckoLibUtil;
import software.bernie.geckolib3.util.RenderUtils;

public class MartiniHenryFirearmItem extends SingleShotFirearmItem {

	public MartiniHenryFirearmItem() {
		super(new Item.Properties()
							.stacksTo(1)
							.durability(1200)
							.tab(IWItemGroups.TAB_WEAPONS)
							.setISTER(() -> FirearmRenderer::new),
					new FirearmItem.Properties()
							.ammoPredicate(s -> s.getItem() == ItemInit.AMMO_GENERIC.get())
							.baseDamage(19.0f)
							.headshotMultiplier(3.0f)
							.spread(0.1f)
							.hipfireSpread(5.0f)
							.muzzleVelocity(6.0f)
							.horizontalRecoil(e -> 1.5f * (float) e.getRandom().nextGaussian())
							.verticalRecoil(e -> 6.0f + 2.0f * e.getRandom().nextFloat())
							.cooldownTime(10)
							.drawTime(20)
							.reloadTime(50)
							.projectileRange(100));

	}
	
	@Override public boolean shouldHideCrosshair(ItemStack stack) { return true; }
	@Override public boolean canCustomize(ItemStack stack) { return false; }

	private static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".attachments_rifle");
	@Override 
	public INamedContainerProvider getAttachmentsContainerProvider(ItemStack stack) {
		return new SimpleNamedContainerProvider(AttachmentsRifleContainer.getServerContainerProvider(stack), TITLE);
	}
	
	/*
	 * BROADCASTING ANIMATION OVERRIDES
	 */
	
	@Override
	protected void doNothing(ItemStack firearm, LivingEntity shooter) {
		super.doNothing(firearm, shooter);
		if (!shooter.level.isClientSide) {
			boolean isAiming = isAiming(firearm);
			AnimUtils.syncItemStackAnimToSelf(firearm, shooter, this, isAiming ? ANIM_ADS_AIMING : ANIM_HIP_AIMING);
			AnimUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", isAiming ? "ads_aiming" : "hip_aiming", true, 1.0f);
		}
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		super.shoot(firearm, shooter);
		if (!shooter.level.isClientSide) {
			boolean isAiming = isAiming(firearm);
			AnimUtils.syncItemStackAnimToSelf(firearm, shooter, this, isAiming ? ANIM_ADS_FIRING : ANIM_HIP_FIRING);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(isAiming ? "ads_firing" : "hip_firing", false));
			upperBody.add(new Tuple<>(isAiming ? "ads_aiming" : "hip_aiming", true));
			AnimUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		if (isAiming(firearm)) return;
		super.startReload(firearm, shooter);
		if (!shooter.level.isClientSide) {
			boolean fired = isFired(firearm);
			AnimUtils.syncItemStackAnimToSelf(firearm, shooter, this, fired ? ANIM_RELOAD_EXTRACT : ANIM_RELOAD);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(fired ? "reload_extract" : "reload", false));
			upperBody.add(new Tuple<>("reload_hold", true));
			AnimUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", fired ? "reload_extract" : "reload", false, getTimeModifier(shooter));
		}
	}
	
	@Override
	protected void startAiming(ItemStack firearm, LivingEntity shooter) {
		super.startAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_ADS_AIMING);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("ads_aiming_start", false));
			upperBody.add(new Tuple<>("ads_aiming", true));
			AnimUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	@Override
	protected void stopAiming(ItemStack firearm, LivingEntity shooter) {
		super.stopAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_HIP_AIMING);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("ads_aiming_stop", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
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
		controller.markNeedsReload();
		data.addAnimationController(controller);
	}
	
	private <E extends Item & IAnimatable> PlayState firstPersonPredicate(AnimationEvent<E> event) {
		return PlayState.CONTINUE;
	}

	public static final int ANIM_PORT_ARMS = 0;
	public static final int ANIM_TRAIL_ARMS = 1;
	public static final int ANIM_HIP_AIMING = 2;
	public static final int ANIM_HIP_FIRING = 3;
	public static final int ANIM_RELOAD = 4;
	public static final int ANIM_RELOAD_EXTRACT = 5;
	public static final int ANIM_ADS_AIMING = 6;
	public static final int ANIM_ADS_AIMING_START = 7;
	public static final int ANIM_ADS_AIMING_END = 8;
	public static final int ANIM_ADS_FIRING = 9;
	
	@Override
	public void onAnimationSync(int id, int state) {
		AnimationBuilder builder = new AnimationBuilder();
		switch (state) {
		case ANIM_PORT_ARMS: builder.addAnimation("port_arms", true); break;
		case ANIM_TRAIL_ARMS: builder.addAnimation("trail_arms", true); break;
		case ANIM_HIP_AIMING: builder.addAnimation("hip_aiming", true); break;
		case ANIM_HIP_FIRING:
			builder
			.addAnimation("hip_firing", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_RELOAD:
			builder
			.addAnimation("reload", false)
			.addAnimation("reload_hold", true);
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
		case ANIM_ADS_AIMING_END:
			builder
			.addAnimation("ads_aiming_stop", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_ADS_FIRING:
			builder
			.addAnimation("ads_firing", false)
			.addAnimation("ads_aiming", true);
			break;
		}
		
		final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, "controller");
		controller.markNeedsReload();
		controller.setAnimation(builder);
	}

	@Override
	public boolean shouldSpecialRender(ItemStack stack, LivingEntity entity) {
		return entity instanceof AbstractClientPlayerEntity || entity instanceof NPCEntity;
	}

	@Override
	public boolean onPreRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks,
			MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn) {
		Minecraft mc = Minecraft.getInstance();
		
		@SuppressWarnings("unchecked")
		LivingRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
				(LivingRenderer<LivingEntity, EntityModel<LivingEntity>>) mc.getEntityRenderDispatcher().getRenderer(entity);
		EntityModel<?> model = renderer.getModel();
		
		if (model instanceof PlayerModel) {
			PlayerModel<?> pmodel = (PlayerModel<?>) model;
			pmodel.setAllVisible(false);
			pmodel.leftLeg.visible = true;
			pmodel.leftPants.visible = true;
			pmodel.rightLeg.visible = true;
			pmodel.rightPants.visible = true;
		}
		
		AnimUtils.hideLayers(HeldItemLayer.class, renderer);
		AnimUtils.hideLayers(BipedArmorLayer.class, renderer);
		
		return false;
	}

	@Override
	public void onPostRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks,
			MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn) {
		Minecraft mc = Minecraft.getInstance();
		@SuppressWarnings("unchecked")
		LivingRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
				(LivingRenderer<LivingEntity, EntityModel<LivingEntity>>) mc.getEntityRenderDispatcher().getRenderer(entity);
		EntityModel<?> model = renderer.getModel();
		
		if (model instanceof PlayerModel) {
			PlayerModel<?> pmodel = (PlayerModel<?>) model;
			pmodel.setAllVisible(true);
		}
		
		AnimUtils.restoreLayers(renderer);
	}

	private static final ResourceLocation ANIM_FILE_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "animations/third_person/martini_henry_t.animation.json");
	@Override
	public ResourceLocation getAnimationFileLocation(ItemStack stack, LivingEntity entity) {
		return ANIM_FILE_LOC;
	}

	private static final ResourceLocation MODEL_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "geo/third_person/martini_henry_t.geo.json");
	@Override
	public ResourceLocation getModelLocation(ItemStack stack, LivingEntity entity) {
		return MODEL_LOC;
	}

	private static final ResourceLocation TEXTURE_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/item/martini_henry.png");
	@Override
	public ResourceLocation getTextureLocation(ItemStack stack, LivingEntity entity) {
		return TEXTURE_LOC;
	}

	@Override
	public AnimationBuilder getDefaultAnimation(ItemStack stack, LivingEntity entity, AnimationController<?> controller) {
		return (new AnimationBuilder()).addAnimation("hip_aiming", true);
	}

	@Override
	public List<AnimationController<ThirdPersonItemAnimEntity>> getAnimationControlllers(ItemStack stack,
			LivingEntity entity) {
		AnimationController<ThirdPersonItemAnimEntity> upperBody = new AnimationController<>(
				new ThirdPersonItemAnimEntity(entity.getUUID(), Hand.MAIN_HAND), "upper_body", 1,
				this::upperBodyPredicate);
		upperBody.registerSoundListener(this::thirdPersonSoundListener);
		upperBody.registerCustomInstructionListener(this::thirdPersonCustomInstructionListener);
		
		List<AnimationController<ThirdPersonItemAnimEntity>> controllers = new ArrayList<>();
		controllers.add(upperBody);
		return controllers;
	}
	
	private <E extends IAnimatable> PlayState upperBodyPredicate(AnimationEvent<E> event) {
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) event.getAnimatable();
		AnimationController<?> controller = event.getController();
		
		AnimationBuilder builder = animEntity.popAndGetAnim(controller.getName());
		if (builder != null) {
			controller.markNeedsReload();
			controller.setAnimation(builder);
			controller.setAnimationSpeed(animEntity.getSpeed());
		}
		
		return PlayState.CONTINUE;
	}
	
	private <E extends IAnimatable> void thirdPersonCustomInstructionListener(CustomInstructionKeyframeEvent<E> event) {
		ThirdPersonItemAnimRenderer.parse(event);
	}

	@Override
	public void renderOverBone(ItemStack item, LivingEntity entity, float partialTicks, GeoBone bone, MatrixStack stack,
			IRenderTypeBuffer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue,
			float alpha) {
		Minecraft mc = Minecraft.getInstance();
		@SuppressWarnings("unchecked")
		LivingRenderer<LivingEntity, EntityModel<LivingEntity>> renderer =
				(LivingRenderer<LivingEntity, EntityModel<LivingEntity>>) mc.getEntityRenderDispatcher().getRenderer(entity);
		EntityModel<?> model = renderer.getModel();
		ResourceLocation textureLoc = renderer.getTextureLocation(entity);
		
		ThirdPersonItemAnimRenderer animRenderer = RenderEvents.RENDERER_CACHE.get(entity.getUUID());
		boolean lockedLimbs = animRenderer.areLimbsLocked();
		
		if (model instanceof PlayerModel<?>) {
			PlayerModel<?> pmodel = (PlayerModel<?>) model;
			
			IVertexBuilder skinBuffer = bufferIn.getBuffer(RenderType.entitySolid(textureLoc));
			IVertexBuilder clothesBuffer = bufferIn.getBuffer(RenderType.entityTranslucent(textureLoc));
			
			stack.pushPose();
			
			RenderUtils.translate(bone, stack);
			RenderUtils.moveToPivot(bone, stack);
			RenderUtils.rotate(bone, stack);
			stack.mulPose(Vector3f.ZP.rotationDegrees(180f));
			RenderUtils.scale(bone, stack);
			RenderUtils.moveBackFromPivot(bone, stack);
			
			String name = bone.getName();
			
			if (name.equals("body")) {
				pmodel.body.visible = true;
				pmodel.jacket.visible = true;
				
				AnimUtils.renderPartOverBone(pmodel.body, bone, stack, skinBuffer, packedLightIn, 1.0f, packedOverlayIn);
				AnimUtils.renderPartOverBone(pmodel.jacket, bone, stack, clothesBuffer, packedLightIn, 1.0f, packedOverlayIn);
				pmodel.body.visible = false;
				pmodel.jacket.visible = false;
			}
			
			if (name.equals("arm_left")) {
				pmodel.leftArm.visible = true;
				pmodel.leftSleeve.visible = true;
				
				/*
				if (!lockedLimbs) {
					stack.mulPose(Vector3f.YN.rotationDegrees(MathHelper.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot)));
					stack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yHeadRotO, entity.yHeadRot)));
					
					RenderUtils.moveToPivot(bone, stack);
					stack.mulPose(Vector3f.XP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot)));
					RenderUtils.moveBackFromPivot(bone, stack);
				}*/
				stack.translate(-0.0625f, 0.0f, 0.0f);
				
				AnimUtils.renderPartOverBone(pmodel.leftArm, bone, stack, skinBuffer, packedLightIn, 1.0f, packedOverlayIn);
				AnimUtils.renderPartOverBone(pmodel.leftSleeve, bone, stack, clothesBuffer, packedLightIn, 1.0f, packedOverlayIn);
				pmodel.leftArm.visible = false;
				pmodel.leftSleeve.visible = false;
			}
			
			if (name.equals("arm_right")) {
				pmodel.rightArm.visible = true;
				pmodel.rightSleeve.visible = true;
				
				/*
				if (!lockedLimbs) {
					stack.mulPose(Vector3f.YN.rotationDegrees(MathHelper.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot)));
					stack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yHeadRotO, entity.yHeadRot)));
					
					RenderUtils.moveToPivot(bone, stack);
					stack.mulPose(Vector3f.XP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot)));
					RenderUtils.moveBackFromPivot(bone, stack);
				}*/
				stack.translate(0.0625f, 0.0f, 0.0f);
				
				AnimUtils.renderPartOverBone(pmodel.rightArm, bone, stack, skinBuffer, packedLightIn, 1.0f, packedOverlayIn);
				AnimUtils.renderPartOverBone(pmodel.rightSleeve, bone, stack, clothesBuffer, packedLightIn, 1.0f, packedOverlayIn);
				pmodel.rightArm.visible = false;
				pmodel.rightSleeve.visible = false;
			}
			
			if (name.equals("head")) {
				pmodel.head.visible = true;
				pmodel.hat.visible = true;
				
				if (!lockedLimbs) {
					stack.mulPose(Vector3f.YN.rotationDegrees(MathHelper.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot)));
					stack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yHeadRotO, entity.yHeadRot)));
					
					RenderUtils.moveToPivot(bone, stack);
					stack.mulPose(Vector3f.XP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot)));
					RenderUtils.moveBackFromPivot(bone, stack);
				}
					
				AnimUtils.renderPartOverBone(pmodel.head, bone, stack, skinBuffer, packedLightIn, 1.0f, packedOverlayIn);
				AnimUtils.renderPartOverBone(pmodel.hat, bone, stack, clothesBuffer, packedLightIn, 1.0f, packedOverlayIn);
				pmodel.head.visible = false;
				pmodel.hat.visible = false;
			}
			
			stack.popPose();
		}
	}

	@Override
	public float getBoneAlpha(ItemStack item, LivingEntity entity, GeoBone bone, float argAlpha) {
		String name = bone.getName();
		return name.equals("body") || name.equals("arm_left") || name.equals("arm_right") || name.equals("head") ? 0.0f : 1.0f;
	}

}
