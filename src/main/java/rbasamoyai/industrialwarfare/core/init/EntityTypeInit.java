package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.debugitems.ModSpawnEggItem;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class EntityTypeInit {
	
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<EntityType<NPCEntity>> NPC = ENTITY_TYPES.register("npc",
			() -> EntityType.Builder.<NPCEntity>of(NPCEntity::new, EntityClassification.CREATURE)
					.sized(0.6f, 1.8f)
					.setTrackingRange(8)
					.build(makeId("npc").toString()));
	
	@SubscribeEvent
	public static void registerSpawnEggs(RegistryEvent.Register<EntityType<?>> event) {
		ModSpawnEggItem.registerSpawnEggs();
	}
	
	private static ResourceLocation makeId(String id) {
		return new ResourceLocation(IndustrialWarfare.MOD_ID, id);
	}
	
}
