package rbasamoyai.industrialwarfare.client.screen.widgets;

import net.minecraft.client.gui.widget.Widget;

public class WidgetUtils {

	public static void setActiveAndVisible(Widget widget, boolean bool) {
		widget.active = bool;
		widget.visible = bool;
	}
	
}
