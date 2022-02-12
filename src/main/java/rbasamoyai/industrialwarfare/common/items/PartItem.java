package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.IPartItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataProvider;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class PartItem extends QualityItem {
	
	private static final IFormattableTextComponent TOOLTIP_PART_COUNT = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".part.part_count");
	private static final IFormattableTextComponent TOOLTIP_WEIGHT = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".part.weight");
	
	public PartItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_PARTS));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		PartItemDataProvider provider = new PartItemDataProvider();
		CompoundNBT tag = nbt;
		if (nbt == null) tag = defaultNBT(new CompoundNBT());
		else if (nbt.contains("Parent")) tag = nbt.getCompound("Parent");
		provider.deserializeNBT(tag);
		return provider;
	}
	
	public static CompoundNBT defaultNBT(CompoundNBT nbt) {
		QualityItem.defaultNBT(nbt);
		nbt.putFloat(PartItemDataCapability.TAG_PART_COUNT, 1);
		nbt.putFloat(PartItemDataCapability.TAG_WEIGHT, 1);
		return nbt;
	}
	
	public static LazyOptional<IPartItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(PartItemDataCapability.PART_ITEM_DATA_CAPABILITY);
	}
	
	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT tag = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> {
			if (PartItemDataCapability.PART_ITEM_DATA_CAPABILITY != null)
				tag.put("item_cap", PartItemDataCapability.PART_ITEM_DATA_CAPABILITY.writeNBT(h, null));
		});
		return tag;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundNBT nbt) {
		stack.setTag(nbt);
		
		if (nbt == null) return;
		
		if (nbt.contains("creativeData", Constants.NBT.TAG_COMPOUND)) {
			readCreativeData(stack, nbt.getCompound("creativeData"));
			nbt.remove("creativeData");
			return;
		}
		
		getDataHandler(stack).ifPresent(h -> {
			if (PartItemDataCapability.PART_ITEM_DATA_CAPABILITY != null)
				PartItemDataCapability.PART_ITEM_DATA_CAPABILITY.readNBT(h, null, nbt.getCompound("item_cap"));
		});
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		super.appendHoverText(stack, world, tooltip, flag);
		appendHoverTextStatic(stack, world, tooltip, flag);
	}
	
	public static void appendHoverTextStatic(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		LazyOptional<IPartItemDataHandler> optional = getDataHandler(stack);
		
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_PART_COUNT,
				optional
						.map(h -> (IFormattableTextComponent) new StringTextComponent(Integer.toString(h.getPartCount())))
						.orElse(TooltipUtils.NOT_AVAILABLE))
		);
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_WEIGHT, 
				optional
						.map(h -> (IFormattableTextComponent) new StringTextComponent(TooltipUtils.formatFloat(h.getWeight())))
						.orElse(TooltipUtils.NOT_AVAILABLE))
		);

	}

	public static ItemStack setQualityValues(ItemStack stack, float quality, int partCount, float weight) {
		QualityItem.setQualityValues(stack, quality);
		getDataHandler(stack).ifPresent(h -> {
			h.setPartCount(partCount);
			h.setWeight(weight);
		});
		return stack;
	}
	
	public static ItemStack stackOf(Item item, float quality, int partCount, float weight) {
		return setQualityValues(new ItemStack(item), quality, partCount, weight);
	}
	
	/* Creative mode methods to get around issue where capability data is lost */
	
	public static ItemStack creativeStack(Item item, float quality, int partCount, float weight) {
		ItemStack stack = stackOf(item, quality, partCount, weight);
		stack.getOrCreateTag().put("creativeData", getCreativeData(stack));
		return stack;
	}
	
	public static CompoundNBT getCreativeData(ItemStack stack) {
		CompoundNBT tag = QualityItem.getCreativeData(stack);
		getDataHandler(stack).ifPresent(h -> {
			tag.putInt(PartItemDataCapability.TAG_PART_COUNT, h.getPartCount());
			tag.putFloat(PartItemDataCapability.TAG_WEIGHT, h.getWeight());
		});
		return tag;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundNBT nbt) {
		QualityItem.readCreativeData(stack, nbt);
		getDataHandler(stack).ifPresent(h -> {
			h.setPartCount(nbt.getInt(PartItemDataCapability.TAG_PART_COUNT));
			h.setWeight(nbt.getFloat(PartItemDataCapability.TAG_WEIGHT));
		});
	}
	
}
