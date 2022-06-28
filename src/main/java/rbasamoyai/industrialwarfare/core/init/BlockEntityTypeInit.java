package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blockentities.ManufacturingBlockEntity;
import rbasamoyai.industrialwarfare.common.blockentities.MatchCoilBlockEntity;
import rbasamoyai.industrialwarfare.common.blockentities.QuarryBlockEntity;
import rbasamoyai.industrialwarfare.common.blockentities.TaskScrollShelfBlockEntity;
import rbasamoyai.industrialwarfare.common.blockentities.TreeFarmBlockEntity;

public class BlockEntityTypeInit {

	public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<BlockEntityType<ManufacturingBlockEntity>> ASSEMBLER_WORKSTATION = TILE_ENTITY_TYPES.register("assembler_workstation",
			() -> BlockEntityType.Builder.of(ManufacturingBlockEntity::assembler, BlockInit.ASSEMBLER_WORKSTATION.get()).build(null));
	
	public static final RegistryObject<BlockEntityType<MatchCoilBlockEntity>> MATCH_COIL = TILE_ENTITY_TYPES.register("match_coil",
			() -> BlockEntityType.Builder.of(MatchCoilBlockEntity::new, BlockInit.MATCH_COIL.get()).build(null));
	
	public static final RegistryObject<BlockEntityType<QuarryBlockEntity>> QUARRY = TILE_ENTITY_TYPES.register("quarry",
			() -> BlockEntityType.Builder.of(QuarryBlockEntity::new, BlockInit.QUARRY.get()).build(null));
	
	public static final RegistryObject<BlockEntityType<TaskScrollShelfBlockEntity>> TASK_SCROLL_SHELF = TILE_ENTITY_TYPES.register("task_scroll_shelf",
			() -> BlockEntityType.Builder.of(TaskScrollShelfBlockEntity::new, BlockInit.TASK_SCROLL_SHELF.get()).build(null));
	
	public static final RegistryObject<BlockEntityType<TreeFarmBlockEntity>> TREE_FARM = TILE_ENTITY_TYPES.register("tree_farm",
			() -> BlockEntityType.Builder.of(TreeFarmBlockEntity::new, BlockInit.TREE_FARM.get()).build(null));

}
