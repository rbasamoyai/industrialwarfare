package rbasamoyai.industrialwarfare.core.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.network.messages.CNPCBrainDataSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SEditLabelSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SEditScheduleSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SNPCContainerActivateMessage;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;
import rbasamoyai.industrialwarfare.core.network.messages.STaskScrollSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SWorkstationPlayerActionMessage;

public class IWNetwork {

	public static final String NETWORK_VERSION = "0.5.0";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(IndustrialWarfare.MOD_ID, "network"), () -> NETWORK_VERSION, NETWORK_VERSION::equals,
			NETWORK_VERSION::equals);

	public void init() {
		int id = 0;
		CHANNEL.registerMessage(id++, SWorkstationPlayerActionMessage.class, SWorkstationPlayerActionMessage::encode, SWorkstationPlayerActionMessage::decode, SWorkstationPlayerActionMessage::handle);
		CHANNEL.registerMessage(id++, STaskScrollSyncMessage.class, STaskScrollSyncMessage::encode, STaskScrollSyncMessage::decode, STaskScrollSyncMessage::handle);
		CHANNEL.registerMessage(id++, SEditLabelSyncMessage.class, SEditLabelSyncMessage::encode, SEditLabelSyncMessage::decode, SEditLabelSyncMessage::handle);
		CHANNEL.registerMessage(id++, SEditScheduleSyncMessage.class, SEditScheduleSyncMessage::encode, SEditScheduleSyncMessage::decode, SEditScheduleSyncMessage::handle);
		CHANNEL.registerMessage(id++, SNPCContainerActivateMessage.class, SNPCContainerActivateMessage::encode, SNPCContainerActivateMessage::decode, SNPCContainerActivateMessage::handle);
		CHANNEL.registerMessage(id++, CNPCBrainDataSyncMessage.class, CNPCBrainDataSyncMessage::encode, CNPCBrainDataSyncMessage::decode, CNPCBrainDataSyncMessage::handle);
		CHANNEL.registerMessage(id++, DiplomacyScreenMessages.SOpenScreen.class, DiplomacyScreenMessages.SOpenScreen::encode, DiplomacyScreenMessages.SOpenScreen::decode, DiplomacyScreenMessages.SOpenScreen::handle);
		CHANNEL.registerMessage(id++, DiplomacyScreenMessages.SRequestUpdate.class, DiplomacyScreenMessages.SRequestUpdate::encode, DiplomacyScreenMessages.SRequestUpdate::decode, DiplomacyScreenMessages.SRequestUpdate::handle);
		CHANNEL.registerMessage(id++, DiplomacyScreenMessages.CBroadcastChanges.class, DiplomacyScreenMessages.CBroadcastChanges::encode, DiplomacyScreenMessages.CBroadcastChanges::decode, DiplomacyScreenMessages.CBroadcastChanges::handle);
		CHANNEL.registerMessage(id++, DiplomacyScreenMessages.SDiplomaticStatusChangeSync.class, DiplomacyScreenMessages.SDiplomaticStatusChangeSync::encode, DiplomacyScreenMessages.SDiplomaticStatusChangeSync::decode, DiplomacyScreenMessages.SDiplomaticStatusChangeSync::handle);
	}

}
