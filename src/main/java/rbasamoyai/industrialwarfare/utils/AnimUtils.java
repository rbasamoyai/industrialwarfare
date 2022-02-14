package rbasamoyai.industrialwarfare.utils;

import java.lang.reflect.Field;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import rbasamoyai.industrialwarfare.client.rendering.NothingLayer;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.RenderUtils;

public class AnimUtils {

	public static void renderPartOverBone(ModelRenderer model, GeoBone bone, MatrixStack stack, IVertexBuilder buffer,
			int packedLightIn, float alpha, int packedOverlayIn) {
		model.setPos(bone.rotationPointX, bone.rotationPointY, bone.rotationPointZ);
		model.xRot = 0.0f;
		model.yRot = 0.0f;
		model.zRot = 0.0f;
		model.render(stack, buffer, packedLightIn, packedOverlayIn, 1.0f, 1.0f, 1.0f, alpha);
	}

	public static void hideLayers(Class<?> clazz, LivingRenderer<?, ?> renderer) {
		// I want to throw up after this
		Field layerField = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
		try {
			@SuppressWarnings("unchecked")
			List<LayerRenderer<?, ?>> layers = (List<LayerRenderer<?, ?>>) layerField.get(renderer);
			for (int i = 0; i < layers.size(); ++i) {
				LayerRenderer<?, ?> layer = layers.get(i);
				Class<?> layerClass = layer.getClass();
				if (layerClass.equals(clazz)) layers.set(i, new NothingLayer<>(renderer, layer));
			}
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong");
		}		
	}
	
	public static void restoreLayers(LivingRenderer<?, ?> renderer) {
		// I also want to throw up after this
		Field layerField = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
		try {
			@SuppressWarnings("unchecked")
			List<LayerRenderer<?, ?>> layers = (List<LayerRenderer<?, ?>>) layerField.get(renderer);
			for (int i = 0; i < layers.size(); ++i) {
				LayerRenderer<?, ?> layer = layers.get(i);
				if (layer instanceof NothingLayer) {
					layers.set(i, ((NothingLayer<?, ?>) layer).getReplacedLayer());  
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong");
		}
	}
	
	public static void renderOverPlayerModel(
			ItemStack item,
			LivingEntity entity,
			float partialTicks,
			GeoBone bone,
			PlayerModel<?> model,
			ResourceLocation textureLoc,
			boolean lockedLimbs,
			MatrixStack stack,
			IRenderTypeBuffer bufferIn,
			int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityTranslucent(textureLoc));
		String name = bone.getName();
		
		stack.pushPose();
		
		if (!lockedLimbs && !name.equals("body")) {
			RenderUtils.moveToPivot(bone, stack);
			stack.mulPose(Vector3f.XN.rotationDegrees(MathHelper.rotLerp(partialTicks, entity.xRotO, entity.xRot)));
			RenderUtils.moveBackFromPivot(bone, stack);
		}
		
		RenderUtils.translate(bone, stack);
		RenderUtils.moveToPivot(bone, stack);
		RenderUtils.rotate(bone, stack);
		stack.mulPose(Vector3f.ZP.rotationDegrees(180f));
		RenderUtils.scale(bone, stack);
		RenderUtils.moveBackFromPivot(bone, stack);
		
		ModelRenderer part = null;
		ModelRenderer clothes = null;
		
		if (name.equals("body")) {
			part = model.body;
			clothes = model.jacket;
			stack.translate(0.0f, -0.75f, 0.0f);
		}
		
		if (name.equals("arm_left")) {
			part = model.leftArm;
			clothes = model.leftSleeve;
			stack.translate(-0.0625f, 0.0f, 0.0f);
		}
		
		if (name.equals("arm_right")) {
			part = model.rightArm;
			clothes = model.rightSleeve;
			stack.translate(0.0625f, 0.0f, 0.0f);
		}
		
		if (name.equals("head")) {
			part = model.head;
			clothes = model.hat;
		}
		
		if (part != null) {
			part.visible = true;
			AnimUtils.renderPartOverBone(part, bone, stack, builder, packedLightIn, 1.0f, packedOverlayIn);
			part.visible = false;
		}
		if (clothes != null) {
			clothes.visible = true;
			AnimUtils.renderPartOverBone(clothes, bone, stack, builder, packedLightIn, 1.0f, packedOverlayIn);
			clothes.visible = false;
		}
		
		stack.popPose();
	}
	
}
