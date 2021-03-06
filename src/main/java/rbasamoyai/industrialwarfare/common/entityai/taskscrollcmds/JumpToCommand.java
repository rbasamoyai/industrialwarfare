package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.ITaskScrollData;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class JumpToCommand extends TaskScrollCommand {

	private static final int JUMP_POS_ARG_INDEX = 0;
	private static final int CONDITION_TYPE_INDEX = 1;
	
	public JumpToCommand() {
		super(CommandTrees.JUMP_TO, () -> ImmutableMap.of(
				MemoryModuleType.HEARD_BELL_TIME, MemoryStatus.REGISTERED,
				MemoryModuleTypeInit.JUMP_TO.get(), MemoryStatus.REGISTERED
				));
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerLevel level, NPCEntity npc, TaskScrollOrder order) {
		int jumpPos = order.getWrappedArg(JUMP_POS_ARG_INDEX).getArgNum();
		ItemStack scroll = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.TASK_ITEM_INDEX);
		LazyOptional<ITaskScrollData> lzop = TaskScrollItem.getDataHandler(scroll);
		
		if (!lzop.map(h -> 0 <= jumpPos || jumpPos < h.getList().size()).orElse(false)) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_ORDER.get(), 200L);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void start(ServerLevel level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		int condition = order.getWrappedArg(CONDITION_TYPE_INDEX).getArgNum();
		
		int dayTime = (int)(level.getDayTime() % 24000L);
		int dayTimeCondition = order.getWrappedArg(2).getArgNum();
		
		ItemStack filter = order.getWrappedArg(2).getItem().orElse(ItemStack.EMPTY);
		int itemCount = 0;
		boolean hasItems = false;
		
		ItemStack mainhand = npc.getMainHandItem();
		ItemStack offhand = npc.getOffhandItem();
		if (CommandUtils.filterMatches(filter, mainhand)) {
			itemCount += mainhand.getCount();
			hasItems = true;
		}
		if (CommandUtils.filterMatches(filter, offhand)) {
			itemCount += offhand.getCount();
			hasItems = true;
		}
		
		if (!hasItems) {
			IItemHandler npcInv = npc.getInventoryItemHandler();
			for (int i = 0; i < npcInv.getSlots(); i++) {
				ItemStack stack = npcInv.getStackInSlot(i);
				if (!CommandUtils.filterMatches(filter, stack)) continue;
				hasItems = true;
				itemCount += stack.getCount();
			}
		}
		
		int itemCondition = order.getWrappedArg(3).getArgNum();
		int itemCountCondition = order.getWrappedArg(4).getArgNum();
		
		boolean heardBell = brain.hasMemoryValue(MemoryModuleType.HEARD_BELL_TIME);
		
		if (condition == BaseCondition.UNCONDITIONAL
				|| condition == BaseCondition.DAY_TIME &&
						(dayTimeCondition == DayTimeCondition.BEFORE && dayTime < dayTimeCondition
						|| dayTimeCondition == DayTimeCondition.AFTER && dayTime >= dayTimeCondition)
				|| condition == BaseCondition.HAS_ITEMS && hasItems &&
						(itemCondition == ItemCondition.UNCONDITIONAL
						|| itemCondition == ItemCondition.MORE_THAN && itemCount > itemCountCondition
						|| itemCondition == ItemCondition.LESS_THAN && itemCount < itemCountCondition
						|| itemCondition == ItemCondition.EQUAL_TO && itemCount == itemCountCondition)
				|| condition == BaseCondition.HEARD_BELL && heardBell) {
			brain.setMemory(MemoryModuleTypeInit.JUMP_TO.get(), order.getWrappedArg(JUMP_POS_ARG_INDEX).getArgNum());
		} else {
			int index = brain.getMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get()).orElse(0);
			brain.setMemory(MemoryModuleTypeInit.JUMP_TO.get(), index + 1);
		}
		brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
	}

	@Override
	public void tick(ServerLevel level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
	}

	@Override
	public void stop(ServerLevel level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();

		int jumpIndex = brain.getMemory(MemoryModuleTypeInit.JUMP_TO.get()).orElse(0);
		brain.setMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get(), jumpIndex);
		
		brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
		brain.eraseMemory(MemoryModuleTypeInit.JUMP_TO.get());
	}
	
	public static class BaseCondition {
		public static final int UNCONDITIONAL = 0; 
		public static final int DAY_TIME = 1;
		public static final int HAS_ITEMS = 2;
		public static final int HEARD_BELL = 3;
		
		public static final int[] VALUES = new int[] {UNCONDITIONAL, DAY_TIME, HAS_ITEMS, HEARD_BELL};
	}
	
	public static class DayTimeCondition {
		public static final int BEFORE = 0;
		public static final int AFTER = 1;
		
		public static final int[] VALUES = new int[] {BEFORE, AFTER};
	}
	
	public static class ItemCondition {
		public static final int UNCONDITIONAL = 0;
		public static final int MORE_THAN = 1;
		public static final int LESS_THAN = 2;
		public static final int EQUAL_TO = 3;
		
		public static final int[] VALUES = new int[] {UNCONDITIONAL, MORE_THAN, LESS_THAN, EQUAL_TO};
	}

}
