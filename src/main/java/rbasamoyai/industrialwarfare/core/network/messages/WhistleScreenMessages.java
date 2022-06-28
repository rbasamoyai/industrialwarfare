package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.WhistleMenu;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.FormationCategory;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.Interval;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;

public class WhistleScreenMessages {
	
	public static class SWhistleScreenSync {
		private Interval interval;
		private CombatMode mode;
		private UnitFormationType<?> type;
		private Map<FormationCategory, UnitFormationType<?>> formationCategories;
		private Map<FormationCategory, FormationAttackType> attackTypes;
		
		public SWhistleScreenSync() {}
		
		public SWhistleScreenSync(Interval interval, CombatMode mode, UnitFormationType<?> type,
				Map<FormationCategory, UnitFormationType<?>> formationCategories,
				Map<FormationCategory, FormationAttackType> attackTypes) {
			this.interval = interval;
			this.mode = mode;
			this.type = type;
			this.formationCategories = formationCategories;
			this.attackTypes = attackTypes;
		}
		
		public static void encode(SWhistleScreenSync msg, FriendlyByteBuf buf) {
			buf
			.writeVarInt(msg.interval.getId())
			.writeVarInt(msg.mode.getId())
			.writeRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES.get(), msg.type);
			
			buf.writeVarInt(msg.formationCategories.size());
			msg.formationCategories.forEach((k, v) -> buf.writeVarInt(k.getId()).writeRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES.get(), v));
			
			buf.writeVarInt(msg.attackTypes.size());
			msg.attackTypes.forEach((k, v) -> buf.writeVarInt(k.getId()).writeRegistryIdUnsafe(IWModRegistries.FORMATION_ATTACK_TYPES.get(), v));
		}
		
		public static SWhistleScreenSync decode(FriendlyByteBuf buf) {
			Interval interval = Interval.fromId(buf.readVarInt());
			CombatMode mode = CombatMode.fromId(buf.readVarInt());
			UnitFormationType<?> type = buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES.get());
			
			Map<FormationCategory, UnitFormationType<?>> formationCategories = new HashMap<>();
			int sz = buf.readVarInt();
			for (int i = 0; i < sz; ++i) {
				FormationCategory cat = FormationCategory.fromId(buf.readVarInt());
				UnitFormationType<?> catType = buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES.get());
				formationCategories.put(cat, catType);
			}
			
			Map<FormationCategory, FormationAttackType> attackTypes = new HashMap<>();
			int sz1 = buf.readVarInt();
			for (int i = 0; i < sz1; ++i) {
				FormationCategory cat = FormationCategory.fromId(buf.readVarInt());
				FormationAttackType attackType = buf.readRegistryIdUnsafe(IWModRegistries.FORMATION_ATTACK_TYPES.get());
				attackTypes.put(cat, attackType);
			}
			
			return new SWhistleScreenSync(interval, mode, type, formationCategories, attackTypes);
		}
		
		public static void handle(SWhistleScreenSync msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayer player = ctx.getSender();
				AbstractContainerMenu ct = player.containerMenu;
				if (!(ct instanceof WhistleMenu)) return;
				WhistleMenu whistleCt = (WhistleMenu) ct;
				whistleCt.setInterval(msg.interval);
				whistleCt.setMode(msg.mode);
				whistleCt.setFormation(msg.type);
				msg.formationCategories.forEach(whistleCt::setCategoryType);
				msg.attackTypes.forEach(whistleCt::setCategoryAttackType);
				whistleCt.updateItem(player);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class SStopAction {
		public SStopAction() {}
		
		public static void encode(SStopAction msg, FriendlyByteBuf buf) {}
		public static SStopAction decode(FriendlyByteBuf buf) { return new SStopAction(); }
		
		public static void handle(SStopAction msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayer player = ctx.getSender();
				AbstractContainerMenu ct = player.containerMenu;
				if (!(ct instanceof WhistleMenu)) return;
				WhistleMenu whistleCt = (WhistleMenu) ct;
				whistleCt.stopWhistle(player);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class SStopAllFormationLeaders {
		public SStopAllFormationLeaders() {}
		
		public static void encode(SStopAllFormationLeaders msg, FriendlyByteBuf buf) {}
		public static SStopAllFormationLeaders decode(FriendlyByteBuf buf) { return new SStopAllFormationLeaders(); }
		
		public static void handle(SStopAllFormationLeaders msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayer player = ctx.getSender();
				if (player.level.isClientSide) return;
				PlayerIDTag owner = PlayerIDTag.of(player);
				player.level.getEntities(EntityTypeInit.FORMATION_LEADER.get(), player.getBoundingBox().inflate(2.5d, 2.5d, 2.5d), e -> {
					PlayerIDTag leaderOwner = ((FormationLeaderEntity) e).getOwner();
					return leaderOwner.equals(owner);
				}).forEach(Entity::kill);
			});
			ctx.setPacketHandled(true);
		}
	}
	
}
