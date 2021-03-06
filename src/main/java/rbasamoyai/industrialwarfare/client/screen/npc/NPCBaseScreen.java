package rbasamoyai.industrialwarfare.client.screen.npc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.WidgetUtils;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCMenu;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.SNPCContainerActivateMessage;

public class NPCBaseScreen extends AbstractContainerScreen<NPCMenu> {

	private static final ResourceLocation NPC_SCREEN_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/npcs/npc_base.png");
	
	private static final String TRANSLATION_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".npc";
	private static final Component NPC_MAIN_PAGE_TEXT = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".main_page");
	private static final Component NPC_INVENTORY_TEXT = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".inventory");
	
	private static final int SLOT_SPACING = 18;
	private static final int SLOT_TEX_X = 176;
	private static final int SLOT_TEX_Y = 0;
	private static final int NPC_EQUIPMENT_SLOTS_GUI_COLUMNS = 3;
	
	private static final int PAGE_TITLE_Y = 20;
	private static final int MIN_WIDTH = 60;
	private static final int TITLE_PADDING = 4;
	
	private static final int PAGE_BUTTON_TEX_X = 176;
	private static final int PAGE_BUTTON_GUI_Y = 19;
	private static final int PAGE_BUTTON_WIDTH = 7;
	private static final int PAGE_BUTTON_HEIGHT = 11;
	private static final int PAGE_NEXT_TEX_Y = 162;
	private static final int PAGE_PREV_TEX_Y = 184;
	
	private static final int NPC_INFO_WINDOW_TEX_X = 176;
	private static final int NPC_INFO_WINDOW_TEX_Y = 90;
	private static final int NPC_INFO_WINDOW_GUI_X = 7;
	private static final int NPC_INFO_WINDOW_GUI_Y = 31;
	private static final int NPC_INFO_WINDOW_WIDTH = 54;
	private static final int NPC_INFO_WINDOW_HEIGHT = 72;
	
	private static final int NPC_ENTITY_WINDOW_TEX_X = 176;
	private static final int NPC_ENTITY_WINDOW_TEX_Y = 18;
	private static final int NPC_ENTITY_WINDOW_GUI_X = 63;
	private static final int NPC_ENTITY_WINDOW_GUI_Y = 31;
	private static final int NPC_ENTITY_WINDOW_WIDTH = 50;
	private static final int NPC_ENTITY_WINDOW_HEIGHT = 72;
	
	private static final int SHADE_COLOR = 0x40000000;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	
	public static final int MAIN_PAGE = 0;
	public static final int INVENTORY_PAGE = 1;
	private static final int LAST_PAGE = INVENTORY_PAGE;
	
	private Component pageTitle = NPC_MAIN_PAGE_TEXT;
	private int page = 0;
	
	private Button prevPageButton;
	private Button nextPageButton;
	
	public NPCBaseScreen(NPCMenu container, Inventory playerInv, Component localTitle) {
		super(container, playerInv, localTitle);
		
		this.imageWidth = 176;
		this.imageHeight = 236;
		this.inventoryLabelY = this.imageHeight - 94;
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.prevPageButton = this.addRenderableWidget(new ImageButton(
				this.leftPos,
				this.topPos + PAGE_BUTTON_GUI_Y,
				PAGE_BUTTON_WIDTH,
				PAGE_BUTTON_HEIGHT,
				PAGE_BUTTON_TEX_X,
				PAGE_PREV_TEX_Y,
				PAGE_BUTTON_HEIGHT,
				NPC_SCREEN_GUI,
				this::prevPage
				));
		
		this.nextPageButton = this.addRenderableWidget(new ImageButton(
				this.leftPos,
				this.topPos + PAGE_BUTTON_GUI_Y,
				PAGE_BUTTON_WIDTH,
				PAGE_BUTTON_HEIGHT,
				PAGE_BUTTON_TEX_X,
				PAGE_NEXT_TEX_Y,
				PAGE_BUTTON_HEIGHT,
				NPC_SCREEN_GUI,
				this::nextPage
				));
		
		this.updatePage();
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, NPC_SCREEN_GUI);
		
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		if (this.page == 0) {
			for (int i = 0; i < 12; i++) {
				if (i != 0 && i != 2 && i != 9 && i != 11) { // Ugly, I know but at least I only have to write AbstractGui#blit only once
					int x = this.leftPos + NPCMenu.NPC_EQUIPMENT_SLOTS_LEFT_X + i % NPC_EQUIPMENT_SLOTS_GUI_COLUMNS * SLOT_SPACING - 1;
					int y = this.topPos + NPCMenu.NPC_EQUIPMENT_SLOTS_CENTER_START_Y + i / NPC_EQUIPMENT_SLOTS_GUI_COLUMNS * SLOT_SPACING - 1;
					this.blit(stack, x, y, SLOT_TEX_X, SLOT_TEX_Y, SLOT_SPACING, SLOT_SPACING);
				}
			}
			this.blit(stack, this.leftPos + NPC_INFO_WINDOW_GUI_X, this.topPos + NPC_INFO_WINDOW_GUI_Y, NPC_INFO_WINDOW_TEX_X, NPC_INFO_WINDOW_TEX_Y, NPC_INFO_WINDOW_WIDTH, NPC_INFO_WINDOW_HEIGHT);
			this.blit(stack, this.leftPos + NPC_ENTITY_WINDOW_GUI_X, this.topPos + NPC_ENTITY_WINDOW_GUI_Y, NPC_ENTITY_WINDOW_TEX_X, NPC_ENTITY_WINDOW_TEX_Y, NPC_ENTITY_WINDOW_WIDTH, NPC_ENTITY_WINDOW_HEIGHT);	
		} else if (this.page == 1) {
			for (int i = 0; i < this.menu.getInvSlotCount(); i++) {
				int x = this.leftPos + NPCMenu.NPC_INVENTORY_START_X + i % NPCMenu.NPC_INVENTORY_COLUMNS * SLOT_SPACING - 1;
				int y = this.topPos + NPCMenu.NPC_INVENTORY_START_Y + i / NPCMenu.NPC_INVENTORY_COLUMNS * SLOT_SPACING - 1;
				this.blit(stack, x, y, SLOT_TEX_X, SLOT_TEX_Y, SLOT_SPACING, SLOT_SPACING);
			}
		}
	}
	
	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		this.font.drawShadow(stack, this.pageTitle, (this.imageWidth - this.font.width(this.pageTitle)) / 2, PAGE_TITLE_Y, TEXT_COLOR);
		
		if (this.page == MAIN_PAGE && !this.menu.areArmorSlotsEnabled()) {
			for (int i = 0; i < NPCMenu.NPC_EQUIPMENT_ARMOR_SLOTS_COUNT - 1; i++) {
				int y = NPCMenu.NPC_EQUIPMENT_SLOTS_CENTER_START_Y + i * SLOT_SPACING;
				this.fillGradient(stack, NPCMenu.NPC_EQUIPMENT_SLOTS_CENTER_X, y, NPCMenu.NPC_EQUIPMENT_SLOTS_CENTER_X + 16, y + 16, SHADE_COLOR, SHADE_COLOR);
			}
		}
	}
	
	@Override
	public void onClose() {
		super.onClose();
	}
	
	private void prevPage(Button button) {
		if (this.page > 0) {
			if (this.pageIsValid()) --this.page;
			this.updatePage();
		}
	}
	
	private void nextPage(Button button) {
		if (this.page < LAST_PAGE) {
			if (this.pageIsValid()) ++this.page;
			this.updatePage();
		}
	}
	
	private void updatePage() {
		if (this.page == MAIN_PAGE) this.pageTitle = NPC_MAIN_PAGE_TEXT;
		else if (this.page == INVENTORY_PAGE) this.pageTitle = NPC_INVENTORY_TEXT;
		
		int labelWidth = Math.max(this.font.width(this.pageTitle), MIN_WIDTH);
		int labelX = (this.imageWidth - labelWidth) / 2;
		this.prevPageButton.x = this.leftPos + labelX - TITLE_PADDING - PAGE_BUTTON_WIDTH;
		this.nextPageButton.x = this.leftPos + labelX + labelWidth + TITLE_PADDING;

		WidgetUtils.setActiveAndVisible(this.prevPageButton, this.page > 0);
		WidgetUtils.setActiveAndVisible(this.nextPageButton, this.page < LAST_PAGE);
		
		this.menu.updateActiveSlots(this.page);
		IWNetwork.CHANNEL.sendToServer(new SNPCContainerActivateMessage(this.page));
	}
	
	private boolean pageIsValid() {
		if (this.page < 0 || this.page > LAST_PAGE) {
			IndustrialWarfare.LOGGER.warn("An NPCBaseScreen opened by the client was on page " + this.page + ". The page is not valid and will be switched to page 0 (displayed as 1). Look into it, will ya?");
			this.page = 0;
			return false;
		}
		return true;
	}

}
