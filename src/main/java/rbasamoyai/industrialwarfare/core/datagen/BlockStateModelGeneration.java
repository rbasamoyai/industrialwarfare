package rbasamoyai.industrialwarfare.core.datagen;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blocks.MatchCoilBlock;
import rbasamoyai.industrialwarfare.core.init.BlockInit;

public class BlockStateModelGeneration extends BlockStateProvider {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public BlockStateModelGeneration(DataGenerator gen, ExistingFileHelper exFileHelper) {
		super(gen, IndustrialWarfare.MOD_ID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {
		LOGGER.debug("Generating block states and models for rbasamoyai's Industrial Warfare mod");
		
		simpleBlock(BlockInit.ASSEMBLER_WORKSTATION.get(), new UncheckedModelFile(modLoc("block/assembler_workstation")));
		simpleBlockItem(BlockInit.ASSEMBLER_WORKSTATION.get(), new UncheckedModelFile(modLoc("block/assembler_workstation")));
		
		horizontalBlock(BlockInit.QUARRY.get(), new UncheckedModelFile(modLoc("block/quarry")));
		simpleBlockItem(BlockInit.QUARRY.get(), new UncheckedModelFile(modLoc("block/quarry")));
		
		horizontalBlock(BlockInit.TASK_SCROLL_SHELF.get(), new UncheckedModelFile(modLoc("block/task_scroll_shelf")));
		simpleBlockItem(BlockInit.TASK_SCROLL_SHELF.get(), new UncheckedModelFile(modLoc("block/task_scroll_shelf")));
		
		getVariantBuilder(BlockInit.MATCH_COIL.get())
		.forAllStates(state -> {
			int i = state.getValue(MatchCoilBlock.COIL_AMOUNT);
			if (i == 4) {
				return ConfiguredModel.builder()
						.modelFile(new UncheckedModelFile(modLoc("block/spool")))
						.build();
			}
			return ConfiguredModel.builder()
					.modelFile(new UncheckedModelFile(modLoc("block/match_coil" + i)))
					.build();
		});
		
		simpleBlock(BlockInit.SPOOL.get(), new UncheckedModelFile(modLoc("block/spool")));
		simpleBlockItem(BlockInit.SPOOL.get(), new UncheckedModelFile(modLoc("block/spool")));
		
		simpleBlock(BlockInit.WORKER_SUPPORT.get(), new UncheckedModelFile(modLoc("block/worker_support")));
		simpleBlockItem(BlockInit.WORKER_SUPPORT.get(), new UncheckedModelFile(modLoc("block/worker_support")));
	}
	
	@Override
	public void run(DirectoryCache cache) throws IOException {
		super.run(cache);
	}
	
	@Override
	public String getName() {
		return "Industrial Warfare block states";
	}

}
