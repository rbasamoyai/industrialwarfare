package rbasamoyai.industrialwarfare.core.init.items;

import java.util.function.Supplier;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.IWArmorMaterial;
import rbasamoyai.industrialwarfare.common.items.InfiniteMatchCordItem;
import rbasamoyai.industrialwarfare.common.items.LabelItem;
import rbasamoyai.industrialwarfare.common.items.MatchCoilItem;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;
import rbasamoyai.industrialwarfare.common.items.RecipeItem;
import rbasamoyai.industrialwarfare.common.items.ScheduleItem;
import rbasamoyai.industrialwarfare.common.items.SurveyorsKitItem;
import rbasamoyai.industrialwarfare.common.items.WhistleItem;
import rbasamoyai.industrialwarfare.common.items.armor.AmericanKepiItem;
import rbasamoyai.industrialwarfare.common.items.armor.DragoonHelmetItem;
import rbasamoyai.industrialwarfare.common.items.armor.PickelhaubeHighItem;
import rbasamoyai.industrialwarfare.common.items.armor.PithHelmetItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.ComplaintRemoverItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.DebugOwnerItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.JobSitePointerItem;
import rbasamoyai.industrialwarfare.common.items.debugitems.SetProfessionItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCProfessionInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

/*
 * Item initialization for rbasamoyai's Industrial Warfare.
 */

public class ItemInit {
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<Item> HAMMER = ITEMS.register("hammer", ItemInit::toolItem);
	public static final RegistryObject<Item> WAND = ITEMS.register("wand", ItemInit::toolItem);
	public static final RegistryObject<Item> WHISTLE = ITEMS.register("whistle", WhistleItem::new);
	
	public static final RegistryObject<Item> JOB_SITE_POINTER = ITEMS.register("job_site_pointer", JobSitePointerItem::new);
	public static final RegistryObject<Item> COMPLAINT_REMOVER = ITEMS.register("complaint_remover", ComplaintRemoverItem::new);
	public static final RegistryObject<Item> DEBUG_OWNER = ITEMS.register("debug_owner", DebugOwnerItem::new);
	public static final RegistryObject<Item> NPC_SPAWN_EGG = ITEMS.register("npc_spawn_egg", () ->
			new ForgeSpawnEggItem(EntityTypeInit.NPC, 0x0000afaf, 0x00c69680, new Item.Properties().tab(IWItemGroups.TAB_DEBUG)));
	
	public static final RegistryObject<Item> CURED_FLESH = ITEMS.register("cured_flesh", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> BODY_PART = ITEMS.register("body_part", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> MAKESHIFT_BRAIN = ITEMS.register("makeshift_brain", ItemInit::generalGenericItem);
	public static final RegistryObject<Item> MAKESHIFT_HEAD = ITEMS.register("makeshift_head", ItemInit::generalGenericItem);
	
	public static final RegistryObject<Item> ASSEMBLER_WORKSTATION = registerBlockItem(BlockInit.ASSEMBLER_WORKSTATION);
	public static final RegistryObject<Item> TASK_SCROLL_SHELF = registerBlockItem(BlockInit.TASK_SCROLL_SHELF);
	public static final RegistryObject<Item> SPOOL = registerBlockItem(BlockInit.SPOOL);
	
	public static final RegistryObject<Item> QUARRY = registerBlockItem(BlockInit.QUARRY);
	public static final RegistryObject<Item> TREE_FARM = registerBlockItem(BlockInit.TREE_FARM);
	public static final RegistryObject<Item> FARMING_PLOT = registerBlockItem(BlockInit.FARMING_PLOT);
	public static final RegistryObject<Item> LIVESTOCK_PEN = registerBlockItem(BlockInit.LIVESTOCK_PEN);
	
	public static final RegistryObject<Item> WORKER_SUPPORT = registerBlockItem(BlockInit.WORKER_SUPPORT);
	
	public static final RegistryObject<Item> RECIPE_MANUAL = ITEMS.register("recipe_manual", RecipeItem::new);
	
	public static final RegistryObject<Item> TASK_SCROLL = ITEMS.register("task_scroll", TaskScrollItem::new);
	public static final RegistryObject<Item> LABEL = ITEMS.register("label", LabelItem::new);
	public static final RegistryObject<Item> SCHEDULE = ITEMS.register("schedule", ScheduleItem::new);
	
	public static final RegistryObject<Item> AMMO_GENERIC = ITEMS.register("ammo_generic", ItemInit::generalGenericItem);
	
	public static final RegistryObject<Item> INFINITE_AMMO_GENERIC = ITEMS.register("infinite_ammo_generic",
			() -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).rarity(Rarity.EPIC)) {
				@Override public boolean isFoil(ItemStack stack) { return true; }
			});
	
	public static final RegistryObject<Item> CARTRIDGE_CASE = ITEMS.register("cartridge_case", ItemInit::generalGenericItem);
	
	public static final RegistryObject<Item> PAPER_CARTRIDGE = ITEMS.register("paper_cartridge",
			() -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)));
	
	public static final RegistryObject<Item> INFINITE_PAPER_CARTRIDGE = ITEMS.register("infinite_paper_cartridge",
			() -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).rarity(Rarity.EPIC)) {
				@Override public boolean isFoil(ItemStack stack) { return true; }
			});
	
	public static final RegistryObject<Item> MATCH_CORD = ITEMS.register("match_cord", MatchCordItem::new);
	public static final RegistryObject<Item> INFINITE_MATCH_CORD = ITEMS.register("infinite_match_cord", InfiniteMatchCordItem::new);
	public static final RegistryObject<Item> MATCH_COIL = ITEMS.register("match_coil", MatchCoilItem::new);
	
	public static final RegistryObject<Item> PITH_HELMET = ITEMS.register("pith_helmet",
			() -> new PithHelmetItem(IWArmorMaterial.WOOD, EquipmentSlot.HEAD, new Item.Properties().tab(IWItemGroups.TAB_ARMOR)));
	
	public static final RegistryObject<Item> AMERICAN_KEPI = ITEMS.register("american_kepi",
			() -> new AmericanKepiItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new Item.Properties().tab(IWItemGroups.TAB_ARMOR)));
	
	public static final RegistryObject<Item> PICKELHAUBE_HIGH = ITEMS.register("pickelhaube_high",
			() -> new PickelhaubeHighItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new Item.Properties().tab(IWItemGroups.TAB_ARMOR)));
	
	public static final RegistryObject<Item> DRAGOON_HELMET = ITEMS.register("dragoon_helmet",
			() -> new DragoonHelmetItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new Item.Properties().tab(IWItemGroups.TAB_ARMOR)));
	
	public static final RegistryObject<Item> SET_PROFESSION_JOBLESS = ITEMS.register("set_profession_jobless", () -> setProfessionItem(NPCProfessionInit.JOBLESS));
	public static final RegistryObject<Item> SET_PROFESSION_ASSEMBLER = ITEMS.register("set_profession_assembler", () -> setProfessionItem(NPCProfessionInit.ASSEMBLER));
	public static final RegistryObject<Item> SET_PROFESSION_QUARRIER = ITEMS.register("set_profession_quarrier", () -> setProfessionItem(NPCProfessionInit.QUARRIER));
	public static final RegistryObject<Item> SET_PROFESSION_LOGGER = ITEMS.register("set_profession_logger", () -> setProfessionItem(NPCProfessionInit.LOGGER));
	public static final RegistryObject<Item> SET_PROFESSION_FARMER = ITEMS.register("set_profession_farmer", () -> setProfessionItem(NPCProfessionInit.FARMER));
	public static final RegistryObject<Item> SET_PROFESSION_RANCHER = ITEMS.register("set_profession_rancher", () -> setProfessionItem(NPCProfessionInit.RANCHER));
	
	public static final RegistryObject<Item> SURVEYORS_KIT = ITEMS.register("surveyors_kit", SurveyorsKitItem::new);
	
	public static final RegistryObject<Item> ANIMAL_FEED = ITEMS.register("animal_feed", () -> new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL)));
	
	private static Item toolItem() {
		return new Item(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_GENERAL));
	}
	
	private static Item generalGenericItem() {
		return new Item(new Item.Properties().tab(IWItemGroups.TAB_GENERAL));
	}
	
	private static RegistryObject<Item> registerBlockItem(RegistryObject<Block> blockObject) {
		return ITEMS.register(blockObject.getId().getPath(), () -> new BlockItem(blockObject.get(), new Item.Properties().tab(IWItemGroups.TAB_BLOCKS)));
	}
	
	private static Item setProfessionItem(Supplier<NPCProfession> sup) {
		return new SetProfessionItem(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_DEBUG), sup);
	}
}
