package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.items.models.AmericanKepiModel;

public class AmericanKepiItem extends DyeableArmorItem {

	public AmericanKepiItem(IArmorMaterial material, EquipmentSlotType type, Item.Properties properties) {
		super(material, type, properties);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, A _default) {
		return (A) new AmericanKepiModel(1.0f);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
		return String.format("%s:/textures/models/armor/american_kepi%s.png", IndustrialWarfare.MOD_ID, type == null ? "" : String.format("_%s", type));
	}
	
	@Override
	public int getColor(ItemStack stack) {
		CompoundNBT display = stack.getTagElement("display");
		return display != null && display.contains("color", 99) ? display.getInt("color") : 16777215;
	}
	
}
