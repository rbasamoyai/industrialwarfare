package rbasamoyai.industrialwarfare.common.containers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.ITaskScrollData;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.MenuInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class TaskScrollMenu extends AbstractContainerMenu {

	private static final Pair<ResourceLocation, ResourceLocation> LABEL_ICON = Pair.of(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(IndustrialWarfare.MOD_ID, "item/label_icon"));
	
	public static final int ROW_COUNT = 5; // Would probably be a good idea to allow task scroll related stuff to access this
	
	private static final int LABEL_SLOT_X = 180;
	private static final int LABEL_SLOT_Y = 4;
	
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 158;
	private static final int INVENTORY_SLOT_SPACING = 18;
	private static final int INVENTORY_ROWS = 3;
	private static final int INVENTORY_COLUMNS = 9;
	private static final int HOTBAR_SLOT_Y = 216;
	private static final int HOTBAR_COUNT = INVENTORY_COLUMNS;
	
	private static final int STARTING_ORDER_INDEX = 0;
	
	private final Player player;
	
	private final List<TaskScrollOrder> orderList;
	private final List<TaskScrollCommand> validCmdList;
	private final int stackIndex;
	private final int maxOrders;
	private final InteractionHand hand;
	private final Slot labelItemSlot;
	
	private int topIndex = STARTING_ORDER_INDEX;
	
	private static final Supplier<ItemStackHandler> ANONYMOUS_HANDLER_SUPPLIER = () -> new ItemStackHandler(1) {
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			ItemStack result = stack;
			if (stack.getItem() == ItemInit.LABEL.get()) result = super.insertItem(slot, stack, simulate);
			return result;
		}
		
		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}
	};
	
	public static TaskScrollMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		int stackIndex = buf.readVarInt();
		int maxOrderCount = buf.readVarInt();
		
		int validCmdCount = buf.readVarInt();
		List<TaskScrollCommand> validCmdSet = new ArrayList<>();
		for (int i = 0; i < validCmdCount; i++) validCmdSet.add(IWModRegistries.TASK_SCROLL_COMMANDS.get().getValue(buf.readResourceLocation()));
		
		InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		
		int sz = buf.readVarInt();
		List<TaskScrollOrder> orderList = new ArrayList<>(sz);
		for (int i = 0; i < sz; i++) {
			orderList.add(TaskScrollOrder.fromNetwork(buf));
		}
		
		ItemStackHandler labelItemHandler = ANONYMOUS_HANDLER_SUPPLIER.get();
		labelItemHandler.setStackInSlot(0, buf.readItem());
		
		return new TaskScrollMenu(MenuInit.TASK_SCROLL.get(), windowId, playerInv, labelItemHandler, validCmdSet, orderList, maxOrderCount, stackIndex, hand);
	}
	
	public static MenuConstructor getServerContainerProvider(ItemStack stack, List<TaskScrollCommand> validCmdSet, int stackIndex, InteractionHand hand) {
		LazyOptional<ITaskScrollData> optional = TaskScrollItem.getDataHandler(stack);
		List<TaskScrollOrder> orderList = optional.map(ITaskScrollData::getList).orElseGet(LinkedList::new);
		int maxOrderCount = optional.map(ITaskScrollData::getMaxListSize).orElse(0);
		
		ItemStack labelItem = optional.map(ITaskScrollData::getLabel).orElse(ItemStack.EMPTY);
		ItemStackHandler labelItemHandler = ANONYMOUS_HANDLER_SUPPLIER.get();
		labelItemHandler.setStackInSlot(0, labelItem);
		
		return (windowId, playerInv, data) -> new TaskScrollMenu(MenuInit.TASK_SCROLL.get(), windowId, playerInv, labelItemHandler, validCmdSet, orderList, maxOrderCount, stackIndex, hand);
	}
	
	protected TaskScrollMenu(MenuType<?> type, int windowId, Inventory playerInv, IItemHandler labelItemHandler,
			List<TaskScrollCommand> validCmdSet, List<TaskScrollOrder> orderList, int maxOrders, int stackIndex,
			InteractionHand hand) {
		super(type, windowId);
		
		this.player = playerInv.player;
		this.orderList = orderList;
		this.validCmdList = validCmdSet;
		if (this.validCmdList.size() < 1)
			throw new IllegalStateException("Container with no valid TaskScrollCommands opened, check TaskScrollItem#getValidCommands and subclasses that implement the method");
		this.maxOrders = maxOrders;
		this.stackIndex = stackIndex;
		this.hand = hand;
		
		this.labelItemSlot = this.addSlot(new SlotItemHandler(labelItemHandler, 0, LABEL_SLOT_X, LABEL_SLOT_Y) {
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return LABEL_ICON;
			}
		});
		
		for (int i = 0; i < INVENTORY_ROWS; i++) {
			for (int j = 0; j < INVENTORY_COLUMNS; j++) {
				int x = INVENTORY_START_X + j * INVENTORY_SLOT_SPACING;
				int y = INVENTORY_START_Y + i * INVENTORY_SLOT_SPACING;
				int index = i * INVENTORY_COLUMNS + j + HOTBAR_COUNT;
				this.addSlot(new Slot(playerInv, index, x, y));
			}
		}
		
		for (int i = 0; i < HOTBAR_COUNT; i++) {
			int x = INVENTORY_START_X + i * INVENTORY_SLOT_SPACING;
			if (i == this.stackIndex) this.addSlot(new ImmovableGuiItemSlot(playerInv, i, x, HOTBAR_SLOT_Y));
			else this.addSlot(new Slot(playerInv, i, x, HOTBAR_SLOT_Y));
		}
	}
	
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
	
	public List<TaskScrollCommand> getCommands() {
		return this.validCmdList;
	}
	
	public List<TaskScrollOrder> getOrderList() {
		return this.orderList;
	}
	
	public int getOrderListSize() {
		return this.getOrderList().size();
	}
	
	public int getTopIndex() {
		return this.topIndex;
	}
	
	public void setTopIndex(int index) {
		this.topIndex = index;
	}
	
	public int getMaxOrders() {
		return this.maxOrders;
	}
	
	public boolean isOrderListFull() {
		return this.getOrderListSize() >= this.getMaxOrders();
	}
	
	public boolean isValidSlotOffs(int i) {
		return i >= 0 && i < this.getOrderListSize();
	}
	
	public int getVisibleRowCount() {
		return Mth.clamp(this.getOrderListSize() - this.getTopIndex(), 0, TaskScrollMenu.ROW_COUNT);
	}
	
	public Optional<TaskScrollOrder> getOrder(int i) {
		return Optional.ofNullable(this.isValidSlotOffs(i) ? this.getOrderList().get(i) : null); 
	}
	
	public InteractionHand getHand() {
		return this.hand;
	}
	
	public ItemStack getLabelItem() {
		return this.labelItemSlot.getItem();
	}
	
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

}
