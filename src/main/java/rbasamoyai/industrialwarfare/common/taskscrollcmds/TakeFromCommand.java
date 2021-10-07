package rbasamoyai.industrialwarfare.common.taskscrollcmds;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.utils.ArgUtils;

public class TakeFromCommand extends TaskScrollCommand {

	private static final int ACCESS_SIDE_ARG_INDEX = 0;
	private static final int ITEM_COUNT_ARG_INDEX = 1;
	
	public TakeFromCommand() {
		super(true, true, TaskScrollCommand.ITEM_TRANSFER_ARGS);
		this.setRegistryName(IndustrialWarfare.MOD_ID, "take_from");
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		// TODO: Add wanted scroll details
		boolean isWorking = brain.getMemory(MemoryModuleTypeInit.WORKING).orElse(false);
		boolean result = order.getPos().closerThan(npc.position(), TaskScrollCommand.MAX_DISTANCE_FROM_POI);
		if (!result) {
			// TODO: do some complaining if result is false
			
		}
		return result && !isWorking;
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		BlockPos targetPos = order.getPos();
		List<BlockPos> list = BlockPos.betweenClosedStream(targetPos.offset(-1, -2, -1), targetPos.offset(1, 0, 1)).map(BlockPos::immutable).collect(Collectors.toList());
		Collections.shuffle(list);
		Optional<BlockPos> accessPos = list.stream()
				.filter(pos -> world.loadedAndEntityCanStandOn(pos, npc))
				.filter(pos -> world.noCollision(npc))
				.findFirst();
		accessPos.ifPresent(pos -> {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
		});
		if (!accessPos.isPresent()) {
			// TODO: Complain that area cannot be accessed
			brain.setMemory(MemoryModuleTypeInit.CANT_INTERFACE, true);
		}
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		PathNavigator nav = npc.getNavigation();
		if (nav.isDone()) {
			BlockPos pos = order.getPos();
			TileEntity te = world.getBlockEntity(pos);
			AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
			if (world.isLoaded(pos) && te != null && box.contains(npc.position())) {
				LazyOptional<IItemHandler> blockInvOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, ArgUtils.getDirection(order.getArg(ACCESS_SIDE_ARG_INDEX)));
				blockInvOptional.ifPresent(blockInv -> {
					ItemStackHandler npcInv = npc.getInventoryItemHandler();
					int count = order.getArg(ITEM_COUNT_ARG_INDEX);
					boolean flag = count == 0;
					for (int i = 0; i < blockInv.getSlots(); i++) {
						if (order.filterMatches(blockInv.getStackInSlot(i))) {
							ItemStack takeItem = blockInv.extractItem(i, flag ? blockInv.getSlotLimit(i) : count, false);
							for (int j = 0; j < npcInv.getSlots(); j++) {
								int stackCount = takeItem.getCount();
								takeItem = npcInv.insertItem(j, takeItem, false);
								count -= stackCount - takeItem.getCount();
								if (takeItem.isEmpty() || count < 1 && !flag) break;
							}
							blockInv.insertItem(i, takeItem, false);
							if (count < 1 && !flag) break;
						}
					}
					brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
				});
				if (!blockInvOptional.isPresent()) {
					// TODO: Complain that there's nothing to access here
					brain.setMemory(MemoryModuleTypeInit.CANT_INTERFACE, true);
				}
			}
		}
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX, brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0) + 1);
		brain.eraseMemory(MemoryModuleTypeInit.CANT_INTERFACE);
	}

	@Override
	public boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		boolean cantInterface = brain.getMemory(MemoryModuleTypeInit.CANT_INTERFACE).orElse(false);
		return !cantInterface;
	}

}
