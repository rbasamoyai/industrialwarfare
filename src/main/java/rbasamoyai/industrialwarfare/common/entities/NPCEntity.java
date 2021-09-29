package rbasamoyai.industrialwarfare.common.entities;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Brain.BrainCodec;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCContainer;
import rbasamoyai.industrialwarfare.common.entityai.NPCTasks;
import rbasamoyai.industrialwarfare.core.init.ItemInit;

/*
 * Base NPC entity class for rbasamoyai's Industrial Warfare.
 */

public class NPCEntity extends CreatureEntity {
	
	protected static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.MEETING_POINT, MemoryModuleType.PATH, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
	protected static final List<SensorType<? extends Sensor<? super NPCEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS);
	
	public static final String TAG_WORKSTUFFS = "workstuffs";
	private static final String TAG_INVENTORY = "items";
	
	public static final String DEFAULT_NAME = "Incognito";
	
	public static final int MAX_SLOTS_BEFORE_WARN = 15; // May not apply to Transporter NPCs.
	
	public static final UUID GAIA_UUID = new UUID(0L, 0L); // A null player generally means that the NPC belongs to "Gaia" (thanks, Age of Empires!).
	
	protected final ItemStackHandler inventoryItemHandler;
	protected final EquipmentItemHandler equipmentItemHandler;

	protected NPCEntity(EntityType<? extends NPCEntity> type, World worldIn, String occupation, String name, @Nullable PlayerEntity owner, int initialInventoryCount, boolean canWearEquipment) {
		super(type, worldIn);
		
		// TODO: Add code to default to "jobless" if not in pool of occupations
		this.setCustomName(new StringTextComponent(name));
		
		this.inventoryItemHandler = new ItemStackHandler(initialInventoryCount);		
		this.equipmentItemHandler = new EquipmentItemHandler(this, canWearEquipment);
		
		this.getDataHandler().ifPresent(h -> {
			h.setCanWearEquipment(canWearEquipment);
			h.setOccupation(occupation);
			h.setFirstOwnerUUID(owner == null ? GAIA_UUID : owner.getUUID());
			h.setOwnerUUID(owner == null ? GAIA_UUID : owner.getUUID());
		});
	}
	
	public static AttributeModifierMap.MutableAttribute setAttributes() {
		return CreatureEntity.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0d)
				.add(Attributes.MOVEMENT_SPEED, 0.1d)
				.add(Attributes.ATTACK_DAMAGE, 2.0d)
				.add(Attributes.ATTACK_SPEED, 4.0d);
	}
	
	public ItemStackHandler getInventoryItemHandler() {
		return this.inventoryItemHandler;
	}
	
	public EquipmentItemHandler getEquipmentItemHandler() {
		return this.equipmentItemHandler;
	}
	
	public LazyOptional<? extends INPCDataHandler> getDataHandler() {
		return this.getCapability(NPCDataCapability.NPC_DATA_CAPABILITY);
	}
	
	public void resizeInventoryItemHandler(int newSize) {
		if (this.inventoryItemHandler.getSlots() < newSize)
			IndustrialWarfare.LOGGER.warn("Shrinking inventory of NPC entity with UUID " + this.getUUID().toString() + ", this may cause data loss (inventory size went from " + this.inventoryItemHandler.getSlots() + " to " + newSize + ")");
		if (newSize > MAX_SLOTS_BEFORE_WARN)
			IndustrialWarfare.LOGGER.warn("The inventory of an NPC entity with UUID " + this.getUUID().toString() + " has been set past " + MAX_SLOTS_BEFORE_WARN + " slots (set to " + newSize + " slots), this may result in graphical glitches in the GUI.");
		this.inventoryItemHandler.setSize(newSize);
	}
	
	/**
	 * AI METHODS
	 */
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new LookRandomlyGoal(this));
		this.goalSelector.addGoal(1, new LookAtGoal(this, LivingEntity.class, 4.0f));
		
		this.addTargetGoals();
	}
	
	protected void addTargetGoals() {
	
	}
	
	@Override
	protected BrainCodec<NPCEntity> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}
	
	@Override
	protected Brain<?> makeBrain(Dynamic<?> input) {
		Brain<NPCEntity> brain = this.brainProvider().makeBrain(input);
		this.registerBrainGoals(brain);
		return brain;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Brain<NPCEntity> getBrain() {
		return (Brain<NPCEntity>) super.getBrain();
	}
	
	private void registerBrainGoals(Brain<NPCEntity> brain) {
		brain.addActivity(Activity.CORE, NPCTasks.getCorePackage());
		brain.addActivity(Activity.IDLE, NPCTasks.getIdlePackage());
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.setActiveActivityIfPossible(Activity.IDLE);
	}
	
	@Override
	protected void customServerAiStep() {
		this.getBrain().tick((ServerWorld) this.level, this);
		super.customServerAiStep();
	}
	
	/**
	 * INTERFACING METHODS
	 */
	
	@Override
	protected ActionResultType mobInteract(PlayerEntity player, Hand handIn) {
		ActionResultType actionResultType = this.checkAndHandleImportantInteractions(player, handIn);
		if (actionResultType.consumesAction()) {
			return actionResultType;
		} else {
			if (!this.level.isClientSide && player instanceof ServerPlayerEntity) {
				IContainerProvider containerProvider = NPCContainer.getServerContainerProvider(this);
				INamedContainerProvider namedProvider = new SimpleNamedContainerProvider(containerProvider, this.getCustomName());
				NetworkHooks.openGui((ServerPlayerEntity) player, namedProvider, buf -> {
					buf.writeVarInt(this.inventoryItemHandler.getSlots());
					buf.writeBoolean(this.getDataHandler().map(INPCDataHandler::getCanWearEquipment).orElse(false));
					// TODO: implement professions and write job id
				});
				return ActionResultType.CONSUME;
			}
			return super.mobInteract(player, handIn);
		}
	}
	
	protected ActionResultType checkAndHandleImportantInteractions(PlayerEntity player, Hand handIn) {
		ItemStack handStack = player.getItemInHand(handIn);
		if (handStack.getItem() == ItemInit.LABEL) {
			return handStack.interactLivingEntity(player, this, handIn);
		} else {
			return ActionResultType.PASS;
		}
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SOUL_ESCAPE;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundNBT tag) {
		super.addAdditionalSaveData(tag);
		
		tag.put(TAG_WORKSTUFFS, this.equipmentItemHandler.serializeNBT());
		tag.put(TAG_INVENTORY, this.inventoryItemHandler.serializeNBT());
	}
	
	@Override
	public void readAdditionalSaveData(CompoundNBT tag) {
		super.readAdditionalSaveData(tag);
		
		this.equipmentItemHandler.deserializeNBT(tag);
		this.inventoryItemHandler.deserializeNBT(tag);
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
}