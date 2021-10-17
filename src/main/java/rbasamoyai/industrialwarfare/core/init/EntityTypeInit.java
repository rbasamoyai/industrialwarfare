package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class EntityTypeInit {
	
	public static final EntityType<NPCEntity> NPC = null;
	
	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event) {
		IForgeRegistry<EntityType<?>> registry = event.getRegistry();

		registry.register(EntityType.Builder.<NPCEntity>of(NPCEntity::new, EntityClassification.CREATURE)
				.sized(0.6f, 1.8f)
				.setTrackingRange(8)
				.build(makeId("npc").toString())
				.setRegistryName(IndustrialWarfare.MOD_ID, "npc"));
	}
	
	private static ResourceLocation makeId(String id) {
		return new ResourceLocation(IndustrialWarfare.MOD_ID, id);
	}
	
}
