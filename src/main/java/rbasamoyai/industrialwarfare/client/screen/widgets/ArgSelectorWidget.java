package rbasamoyai.industrialwarfare.client.screen.widgets;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class ArgSelectorWidget extends AbstractWidget {

	private static final ResourceLocation IW_WIDGETS_LOCATION = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/widgets.png");
	
	private static final int BORDER_SIZE = 4;
	private static final int TEXTURE_HEIGHT = 18;
	
	// The given values point to the top left corner of each corner
	private static final int TEX_TOP_LEFT_X = 0;
	private static final int TEX_TOP_LEFT_Y = 0;
	private static final int TEX_FIELD_X = TEX_TOP_LEFT_X + BORDER_SIZE;
	private static final int TEX_WIDTH = 200;
	private static final int TEX_FIELD_WIDTH = TEX_WIDTH - BORDER_SIZE * 2;
	private static final int TEX_RIGHT_BORDER_X = 196;
	
	private static final int TEXT_COLOR = 0x00FFFFFF;
	
	// Instance constants
	protected final int rightBorderX;
	protected final int fieldWidth;
	protected final int fieldTopLeftX;
	
	protected Font font;
	
	protected MutableComponent shortenedTitle;
	
	protected Optional<ArgSelector<?>> selector;
	
	public ArgSelectorWidget(Minecraft minecraft, int x, int y, int width, Optional<ArgSelector<?>> initialSelector) {
		super(x, y, width, TEXTURE_HEIGHT, TextComponent.EMPTY); // TODO: Add message stuff
		
		this.font = minecraft.font;
		
		this.rightBorderX = this.x + this.width - BORDER_SIZE;
		this.fieldWidth = this.width - BORDER_SIZE * 2;
		this.fieldTopLeftX = this.x + BORDER_SIZE;
		
		this.selector = initialSelector;
		
		// Possibly unsafe if someone adds a text component interface that directly extends ITextComponent and not MutableComponent,
		// but all of the base text components implement MutableComponent.
		MutableComponent tc = (MutableComponent) this.selector.map(ArgSelector::getTitle).orElse(TooltipUtils.NOT_AVAILABLE.copy());
		this.shortenedTitle = TooltipUtils.getShortenedTitle(tc, this.font, this.fieldWidth);
		
		WidgetUtils.setActiveAndVisible(this, this.selector.isPresent());
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDist) {
		if (!this.isMouseOver(mouseX, mouseY)) return false;
		this.selector.ifPresent(as -> {
			as.scrollSelectedArg(scrollDist);
			this.shortenedTitle = TooltipUtils.getShortenedTitle((MutableComponent) as.getTitle(), this.font, this.fieldWidth);
		});
		return true;
	}
	
	@Override
	public void renderButton(PoseStack stack, int x, int y, float partialTicks) {
		if (this.selector.isPresent()) {
			RenderSystem.setShaderTexture(0, IW_WIDGETS_LOCATION);
			
			this.blit(stack, this.x, this.y, TEX_TOP_LEFT_X, TEX_TOP_LEFT_Y, BORDER_SIZE, TEXTURE_HEIGHT);
			this.blit(stack, this.rightBorderX, this.y, TEX_RIGHT_BORDER_X, TEX_TOP_LEFT_Y, BORDER_SIZE, TEXTURE_HEIGHT);
			
			int tileCount = Mth.ceil((float) this.fieldWidth / (float) TEX_FIELD_WIDTH);
			for (int i = 0; i < tileCount; i++) { // This should only loop once - why the hell would you need a long field?
				int x1 = this.fieldTopLeftX + TEX_FIELD_WIDTH * i;
				int texWidth = Mth.clamp(this.fieldWidth - TEX_FIELD_WIDTH * i, 0, TEX_FIELD_WIDTH);
				this.blit(stack, x1, this.y, TEX_FIELD_X, TEX_TOP_LEFT_Y, texWidth, TEXTURE_HEIGHT);
			}
			
			this.font.drawShadow(stack, this.shortenedTitle, this.x + BORDER_SIZE + 1, this.y + BORDER_SIZE + 1, TEXT_COLOR);
		}
	}
	
	public Optional<ArgSelector<?>> getSelector() {
		return this.selector;
	}
	
	@Override public void playDownSound(SoundManager manager) {}
	@Override public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}

}
