package rbasamoyai.industrialwarfare.common.diplomacy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class DiplomacySaveData extends SavedData {

	private static final String DATA_NAME = "diplomacySaveData";
	
	private static final String TAG_DIPLOMACY = "diplomacy";
	private static final String TAG_PLAYER = "player";
	private static final String TAG_STATUSES = "statuses";
	private static final String TAG_STATUS = "status";
	
	private static final String TAG_NPC_FACTION_RELATIONS = "npcFactionRelations";
	private static final String TAG_NPC_FACTION = "npcFaction";
	private static final String TAG_RELATIONS = "relations";
	private static final String TAG_RELATIONSHIP = "relationship";
	
	private Map<PlayerIDTag, Map<PlayerIDTag, DiplomaticStatus>> diplomacyTable = new HashMap<>();
	private Map<UUID, Map<PlayerIDTag, Byte>> npcFactionsRelationsTable = new HashMap<>(); // UUID only as relations only apply to NPCs, whose owner UUID is always OwnerTag#GAIA_UUID
	
	public DiplomacySaveData() {
		super();
	}
	
	public static DiplomacySaveData get(Level world) {
		if (!(world instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get data from client world");
		}
		ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);
		DimensionDataStorage dataManager = overworld.getDataStorage();
		return dataManager.computeIfAbsent(new DiplomacySaveData()::load, DiplomacySaveData::new, DATA_NAME);
	}
	
	/* diplomacyTable methods */
	
	public Map<PlayerIDTag, DiplomaticStatus> getDiplomaticStatusesOf(PlayerIDTag player) {
		Map<PlayerIDTag, DiplomaticStatus> diplomaticStatuses = this.diplomacyTable.get(player);
		if (diplomaticStatuses == null) {
			diplomaticStatuses = new HashMap<>();
			this.diplomacyTable.put(player, diplomaticStatuses);
			this.setDirty();
		}
		return diplomaticStatuses;
	}
	
	public Map<PlayerIDTag, DiplomaticStatus> getDiplomaticStatusesTowards(PlayerIDTag player) {
		Map<PlayerIDTag, DiplomaticStatus> diplomaticStatusesTowards = new HashMap<>();
		
		for (Entry<PlayerIDTag, Map<PlayerIDTag, DiplomaticStatus>> entry : this.diplomacyTable.entrySet()) {
			PlayerIDTag tag = entry.getKey();
			if (tag.equals(player)) continue;
			
			Map<PlayerIDTag, DiplomaticStatus> entryStatuses = entry.getValue();
			if (entryStatuses == null) {
				entryStatuses = new HashMap<>();
				if (tag.isPlayer()) {
					entryStatuses.put(player, DiplomaticStatus.NEUTRAL);
				}
				entry.setValue(entryStatuses);
			}
			DiplomaticStatus status = entryStatuses.get(player);
			if (status == null) continue;
			diplomaticStatusesTowards.put(tag, status);
		}
				
		return diplomaticStatusesTowards;
	}
	
	public Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> getDiplomaticStatusesBothWays(PlayerIDTag player) {
		Map<PlayerIDTag, DiplomaticStatus> diplomaticStatusesOf = this.getDiplomaticStatusesOf(player);
		Map<PlayerIDTag, DiplomaticStatus> diplomaticStatusesTowards = this.getDiplomaticStatusesTowards(player);
			
		return diplomaticStatusesOf
				.entrySet()
				.stream()
				.filter(e -> diplomaticStatusesTowards.containsKey(e.getKey()))
				.collect(Collectors.toMap(Entry::getKey, e -> Pair.of(diplomaticStatusesTowards.get(e.getKey()), e.getValue())));
	}
	
	public DiplomaticStatus getDiplomaticStatus(PlayerIDTag of, PlayerIDTag towards) {
		if (of.equals(towards)) {
			throw new IllegalArgumentException("Cannot get diplomatic status between the same player");
		}
		DiplomaticStatus status = this.getDiplomaticStatusesOf(of).get(towards);
		if (status == null) {
			status = DiplomaticStatus.getDefault(towards.isPlayer());
			this.setDiplomaticStatus(of, towards, status);
		}
		return status;
	}
	
	public Set<PlayerIDTag> getPlayers() {
		return this.diplomacyTable.keySet();
	}
	
	public boolean hasPlayerIdTag(PlayerIDTag player) {
		return this.diplomacyTable.containsKey(player);
	}
	
	public void setDiplomaticStatus(PlayerIDTag setting, PlayerIDTag target, DiplomaticStatus status) {
		if (setting.equals(target)) return;
		Map<PlayerIDTag, DiplomaticStatus> diplomaticStatuses = this.getDiplomaticStatusesOf(setting);
		diplomaticStatuses.put(target, status);
		this.setDirty();
	}
	
	public void initPlayerDiplomacyStatuses(Player player) {
		PlayerIDTag tag = PlayerIDTag.of(player);
		
		for (PlayerIDTag keyTag : this.diplomacyTable.keySet()) {
			if (!keyTag.isPlayer()) continue;
			
			this.setDiplomaticStatus(tag, keyTag, DiplomaticStatus.NEUTRAL);
			this.setDiplomaticStatus(keyTag, tag, DiplomaticStatus.NEUTRAL);
		}
		
		/* DEBUG START
		this.initDebugDiplomaticStatus(tag);
		DEBUG END */
		
		this.setDirty();
	}
	
	/* DEBUG */ public void initDebugDiplomaticStatus(PlayerIDTag tag) {
		// Starring a bunch of notable Mojang employees!
		Stream.of( // Players
				new PlayerIDTag(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), true), // Notch
				new PlayerIDTag(UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6"), true), // jeb_
				new PlayerIDTag(UUID.fromString("61699b2e-d327-4a01-9f1e-0ea8c3f06bc6"), true), // Dinnerbone
				new PlayerIDTag(UUID.fromString("e6b5c088-0680-44df-9e1b-9bf11792291b"), true), // Grumm
				new PlayerIDTag(UUID.fromString("13655ac1-584d-4785-b227-650308195121"), true), // kingbdogz
				new PlayerIDTag(UUID.fromString("a6484c2f-cd05-460f-81d1-36e92d8f8f9e"), true), // Cojomax99
				new PlayerIDTag(UUID.fromString("696a82ce-41f4-4b51-aa31-b8709b8686f0"), true), // Searge
				new PlayerIDTag(UUID.fromString("8eb57c51-5df2-4ad7-9a07-01d67d1e0a41"), true), // Jappaa
				new PlayerIDTag(UUID.fromString("6a085b2c-19fb-4986-b453-231aa942bbec"), true)  // LadyAgnes
		).forEach(debugPlayer -> {
			this.setDiplomaticStatus(tag, debugPlayer, DiplomaticStatus.NEUTRAL);
			this.setDiplomaticStatus(debugPlayer, tag, DiplomaticStatus.NEUTRAL);
		});
		
		Stream.of( // NPCs
				new PlayerIDTag(UUID.fromString("a018e03e-5aa0-44b6-91d6-f63126520f73"), false),
				new PlayerIDTag(UUID.fromString("0c5b80b9-266b-42f9-9f07-3f50d722afcd"), false),
				new PlayerIDTag(UUID.fromString("e69f9e1f-0880-4680-bc64-3d983f942e36"), false),
				new PlayerIDTag(UUID.fromString("e5150739-c3a0-4e65-a449-266ec0ed98ab"), false)
		).forEach(debugPlayer -> {
			this.setDiplomaticStatus(tag, debugPlayer, DiplomaticStatus.UNKNOWN);
			this.setDiplomaticStatus(debugPlayer, tag, DiplomaticStatus.UNKNOWN);
		});
}
	
	/* npcFactionsRelationsTable methods */
	
	public Map<PlayerIDTag, Byte> getRelations(UUID npcFactionUuid) {
		Map<PlayerIDTag, Byte> relations = this.npcFactionsRelationsTable.get(npcFactionUuid);
		if (relations == null) {
			relations = new HashMap<>();
			this.npcFactionsRelationsTable.put(npcFactionUuid, relations);
			this.setDirty();
		}
		return relations;
	}
	
	public Map<UUID, Byte> getRelationsTowards(PlayerIDTag player) {
		Map<UUID, Byte> relations = new HashMap<>();
		
		for (Entry<UUID, Map<PlayerIDTag, Byte>> entry : this.npcFactionsRelationsTable.entrySet()) {
			UUID npcFactionUuid = entry.getKey();
			if (!player.isPlayer() && player.getUUID().equals(npcFactionUuid)) continue;
			
			Map<PlayerIDTag, Byte> relationships = entry.getValue();
			if (relationships == null) {
				relationships = new HashMap<>();
				entry.setValue(relationships);
			}
			Byte relationship = relationships.get(player);
			if (relationship == null) continue;
			relations.put(npcFactionUuid, relationship);
		}
		
		return relations;
	}
	
	public void setRelations(UUID setting, PlayerIDTag target, byte relationship) {
		if (setting.equals(target.getUUID()) && !target.isPlayer()) return;
		Map<PlayerIDTag, Byte> relations = this.getRelations(setting);
		relations.put(target, relationship);
		this.setDirty();
	}
	
	public DiplomacySaveData load(CompoundTag tag) {
		
		
		ListTag diplomacyList = tag.getList(TAG_DIPLOMACY, Tag.TAG_COMPOUND);
		this.diplomacyTable.clear();
		for (int i = 0; i < diplomacyList.size(); i++) {
			CompoundTag e1 = diplomacyList.getCompound(i);
			PlayerIDTag owner = PlayerIDTag.fromNBT(e1.getCompound(TAG_PLAYER));
			
			Map<PlayerIDTag, DiplomaticStatus> statusMap = new HashMap<>();
			ListTag statusList = e1.getList(TAG_STATUSES, Tag.TAG_COMPOUND);
			for (int j = 0; j < statusList.size(); j++) {
				CompoundTag e2 = statusList.getCompound(j);
				PlayerIDTag player = PlayerIDTag.fromNBT(e2.getCompound(TAG_PLAYER));
				DiplomaticStatus status = DiplomaticStatus.fromValue(e2.getByte(TAG_STATUS));
				statusMap.put(player, status);
			}
			this.diplomacyTable.put(owner, statusMap);
		}
		
		ListTag npcFactionsRelationsList = tag.getList(TAG_NPC_FACTION_RELATIONS, Tag.TAG_COMPOUND);
		this.npcFactionsRelationsTable.clear();
		for (int i = 0; i < npcFactionsRelationsList.size(); i++) {
			CompoundTag e1 = npcFactionsRelationsList.getCompound(i);
			UUID npcFaction = e1.getUUID(TAG_NPC_FACTION);
			
			Map<PlayerIDTag, Byte> relationsMap = new HashMap<>();
			ListTag relationsList = e1.getList(TAG_STATUSES, Tag.TAG_COMPOUND);
			for (int j = 0; j < relationsList.size(); j++) {
				CompoundTag e2 = relationsList.getCompound(j);
				PlayerIDTag player = PlayerIDTag.fromNBT(e2.getCompound(TAG_PLAYER));
				byte relationship = e2.getByte(TAG_RELATIONSHIP);
				relationsMap.put(player, relationship);
			}
			this.npcFactionsRelationsTable.put(npcFaction, relationsMap);
		}
		
		return this;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag diplomacyList = new ListTag();
		for (Entry<PlayerIDTag, Map<PlayerIDTag, DiplomaticStatus>> e1 : this.diplomacyTable.entrySet()) {
			CompoundTag entry1 = new CompoundTag();
			entry1.put(TAG_PLAYER, e1.getKey().serializeNBT());
			
			ListTag statusList = new ListTag();
			Map<PlayerIDTag, DiplomaticStatus> statusMap = e1.getValue();
			if (statusMap != null) {
				for (Entry<PlayerIDTag, DiplomaticStatus> e2 : statusMap.entrySet()) {
					CompoundTag entry2 = new CompoundTag();
					entry2.put(TAG_PLAYER, e2.getKey().serializeNBT());
					entry2.putByte(TAG_STATUS, e2.getValue().getValue());
					statusList.add(entry2);
				}
			}
			entry1.put(TAG_STATUSES, statusList);
			
			diplomacyList.add(entry1);
		}
		tag.put(TAG_DIPLOMACY, diplomacyList);
		
		ListTag npcFactionsRelationsList = new ListTag();
		for (Entry<UUID, Map<PlayerIDTag, Byte>> e1 : this.npcFactionsRelationsTable.entrySet()) {
			CompoundTag entry1 = new CompoundTag();
			entry1.putUUID(TAG_NPC_FACTION, e1.getKey());
			
			ListTag relationsList = new ListTag();
			Map<PlayerIDTag, Byte> relationshipMap = e1.getValue();
			if (relationshipMap != null) {
				for (Entry<PlayerIDTag, Byte> e2 : relationshipMap.entrySet()) {
					CompoundTag entry2 = new CompoundTag();
					entry2.put(TAG_PLAYER, e2.getKey().serializeNBT());
					entry2.putByte(TAG_RELATIONSHIP, e2.getValue());
					relationsList.add(entry2);
				}
			}
			entry1.put(TAG_RELATIONS, relationsList);
			
			npcFactionsRelationsList.add(entry1);
		}
		tag.put(TAG_NPC_FACTION_RELATIONS, npcFactionsRelationsList);
		return tag;
	}

}
