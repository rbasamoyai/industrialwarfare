package rbasamoyai.industrialwarfare.common.entityai;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.HasDiplomaticOwner;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.tasks.BlockInteractionTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.EndDiplomacyAttackTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.EndPatrolAttackTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.EndWhistleAttackTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.ExtendedShootTargetTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.FinishMovementCommandTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.GoToWorkTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.JoinNearbyFormationTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.LeaveWorkTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.PreciseWalkToPositionTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.PrepareForShootingTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.ReturnToWorkIfPatrollingTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.RunCommandFromTaskScrollTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.ShootPositionTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.StartSelfDefenseTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.StopSelfDefenseTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.WalkToTargetSpecialTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.WalkTowardsPosNoDelayTask;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class NPCTasks {

	public static ImmutableList<Pair<Integer, ? extends Behavior<? super NPCEntity>>> getCorePackage() {
		return ImmutableList.of(
				Pair.of(0, new GoToWorkTask(MemoryModuleType.JOB_SITE, 3.0f, 1, 100)),
				Pair.of(0, new LeaveWorkTask(MemoryModuleType.JOB_SITE, 3.0f, 1, 100)),
				Pair.of(0, new WalkToTargetSpecialTask()),
				Pair.of(0, new PreciseWalkToPositionTask(1.5f, 1.0d, 0.07d, false)),
				Pair.of(0, new InteractWithDoor()),
				Pair.of(0, new Swim(0.8f)),
				Pair.of(1, new LookAtTargetSink(45, 90)),
				Pair.of(1, new JoinNearbyFormationTask<>()),
				Pair.of(2, new FinishMovementCommandTask(MemoryModuleType.MEETING_POINT))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Behavior<? super NPCEntity>>> getIdlePackage() {
		return ImmutableList.of(
				Pair.of(0, new WalkTowardsPosNoDelayTask(MemoryModuleType.MEETING_POINT, 3.0f, 1, 100)),
				Pair.of(1, new SetLookAndInteract(EntityType.PLAYER, 4)),
				Pair.of(2, new StartSelfDefenseTask<>())
				//Pair.of(2, new SetWalkTargetFromLookTarget(2.5f, 2))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Behavior<? super NPCEntity>>> getWorkPackage() {
		return ImmutableList.of(
				Pair.of(0, new RunCommandFromTaskScrollTask()),
				Pair.of(1, new StartAttacking<>(NPCTasks::onPatrol, NPCTasks::findNearestValidAttackTarget)),
				Pair.of(1, new BlockInteractionTask()),
				Pair.of(2, new GoToWantedItem<>(NPCTasks::pickUpPredicate, 3.0f, true, 32))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Behavior<? super NPCEntity>>> getRestPackage() {
		return ImmutableList.of(Pair.of(0, new StrollToPoi(MemoryModuleType.HOME, 3.0f, 1, 100)));
	}
	
	public static ImmutableList<Pair<Integer, ? extends Behavior<? super NPCEntity>>> getFightPackage() {
		return ImmutableList.of(
				Pair.of(0, new WalkTowardsPosNoDelayTask(MemoryModuleType.MEETING_POINT, 3.0f, 1, 100)),
				Pair.of(1, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(3.0f)),
				Pair.of(1, new PrepareForShootingTask<>(MemoryModuleTypeInit.SHOOTING_POS.get())),
				Pair.of(2, new ExtendedShootTargetTask<>()),
				Pair.of(2, new ShootPositionTask<>(MemoryModuleTypeInit.SHOOTING_POS.get())),
				Pair.of(2, new StartAttacking<>(NPCTasks::canFindNewTarget, NPCTasks::findNearestValidAttackTarget)),
				Pair.of(3, new StartCelebratingIfTargetDead(0, (e1, e2) -> false)),
				Pair.of(3, new EndWhistleAttackTask()),
				Pair.of(3, new EndDiplomacyAttackTask<>()),
				Pair.of(3, new EndPatrolAttackTask()),
				Pair.of(4, new MeleeAttack(20)),
				Pair.of(5, new ReturnToWorkIfPatrollingTask()),
				Pair.of(5, new StopSelfDefenseTask(Activity.IDLE))
				);
	}
	
	private static boolean onPatrol(NPCEntity npc) {
		return npc.getBrain().hasMemoryValue(MemoryModuleTypeInit.ON_PATROL.get());
	}
	
	private static boolean canFindNewTarget(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		
		if (brain.hasMemoryValue(MemoryModuleTypeInit.DEFENDING_SELF.get())) return false;
		
		CombatMode mode = brain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.DONT_ATTACK);
		if (mode == CombatMode.DONT_ATTACK) {
			return false;
		}
		
		// Targets will be assigned in formation code
		if (brain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get()) && !brain.hasMemoryValue(MemoryModuleTypeInit.CAN_ATTACK.get())) {
			return false;
		}
		
		if (!brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return true;
		LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
		if (target instanceof Player && (((Player) target).isCreative() || ((Player) target).isSpectator())) {
			return true;
		}
		
		if (target instanceof HasDiplomaticOwner) {
			PlayerIDTag npcOwner = npc.getDiplomaticOwner();
			PlayerIDTag otherOwner = ((HasDiplomaticOwner) target).getDiplomaticOwner();
			if (npcOwner.equals(otherOwner)) return !npc.hasOwner();
			DiplomacySaveData saveData = DiplomacySaveData.get(npc.level);
			if (saveData.getDiplomaticStatus(npcOwner, otherOwner) == DiplomaticStatus.ALLY) return true;
		}
		
		if (mode == CombatMode.STAND_GROUND && !BehaviorUtils.isWithinAttackRange(npc, target, 0)) return true;
		
		if (mode == CombatMode.DEFEND) {
			if (!brain.hasMemoryValue(MemoryModuleTypeInit.CACHED_POS.get())
				|| !brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get()).get().dimension().equals(npc.level.dimension()))
				brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(npc.level.dimension(), npc.blockPosition()));
			BlockPos pos = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get()).get().pos();
			if (!pos.closerToCenterThan(npc.position(), 10) || !BehaviorUtils.isWithinAttackRange(npc, target, 0)) return true;
		}
		
		return target.isDeadOrDying();
	}
	
	private static Optional<? extends LivingEntity> findNearestValidAttackTarget(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();

		PlayerIDTag npcOwner = npc.getDiplomaticOwner();
		DiplomacySaveData saveData = DiplomacySaveData.get(npc.level);
		
		Optional<Integer> pursuitOptional = brain.getMemory(MemoryModuleTypeInit.ON_PATROL.get());
		double pursuitDistance = npc.getAttributeValue(Attributes.FOLLOW_RANGE);
		if (pursuitOptional.isPresent()) {
			pursuitDistance = (double) pursuitOptional.get();
		}
		
		Optional<GlobalPos> gpop = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get());
		BlockPos pos = gpop.isPresent() && gpop.get().dimension() == npc.level.dimension() ? gpop.get().pos() : npc.blockPosition();
		
		Optional<LivingEntity> targetOptional = Optional.empty();
		
		for (LivingEntity e : brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).map(nvle -> nvle.findAll(e -> true)).orElseGet(Lists::newArrayList)) {		
			if (!e.blockPosition().closerThan(pos, pursuitDistance)) continue;
			
			if (e instanceof HasDiplomaticOwner) {
				PlayerIDTag otherOwner = ((HasDiplomaticOwner) e).getDiplomaticOwner();
				if (npcOwner.equals(otherOwner)) continue;
				DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, otherOwner);
				if (status == DiplomaticStatus.ENEMY || status != DiplomaticStatus.ALLY && isViableNonEnemyTarget(e)) {
					targetOptional = Optional.of(e);
					break;
				}
			}
			
			Brain<?> targetBrain = e.getBrain();
			if (targetBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && targetBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() == npc) {
				targetOptional = Optional.of(e);
				break;
			}
			if (e instanceof Mob && ((Mob) e).getTarget() == npc && isViableTargetingTarget((Mob) e)) {
				targetOptional = Optional.of(e);
				break;
			}
			
			if (e instanceof Player && !((Player) e).isCreative()) {
				PlayerIDTag otherPlayerTag = PlayerIDTag.of((Player) e);
				if (npcOwner.equals(otherPlayerTag)) continue;
				DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, PlayerIDTag.of((Player) e));
				
				if (status == DiplomaticStatus.ENEMY || status != DiplomaticStatus.ALLY && isViableNonEnemyTarget(e)) {
					targetOptional = Optional.of(e);
					break;
				}
			}
		}
		
		if (targetOptional.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(npc.level.dimension(), npc.blockPosition()));
			return targetOptional;
		}
		
		// Assisting nearby allies with their attack targets
		List<LivingEntity> nearbyEntities = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(Arrays.asList());
		for (LivingEntity e : nearbyEntities) {
			if (!(e instanceof HasDiplomaticOwner)) continue;
			PlayerIDTag otherOwner = ((HasDiplomaticOwner) e).getDiplomaticOwner();
			if (!npcOwner.equals(otherOwner) && saveData.getDiplomaticStatus(npcOwner, otherOwner) != DiplomaticStatus.ALLY) continue;
			Brain<?> allyBrain = e.getBrain();
			if (allyBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
				LivingEntity target = allyBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
				if (BehaviorUtils.canSee(npc, target) && BehaviorUtils.isWithinAttackRange(npc, target, 0)) {
					targetOptional = Optional.of(e);
					break;
				}
			}
			if (e instanceof Mob) {
				LivingEntity target = ((Mob) e).getTarget();
				if (target != null && BehaviorUtils.canSee(npc, target) && BehaviorUtils.isWithinAttackRange(npc, target, 0)) {
					targetOptional = Optional.of(e);
					break;
				}
			}
		}
		
		if (targetOptional.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(npc.level.dimension(), npc.blockPosition()));
		}
		return targetOptional;
	}
	
	// TODO: neutral/unknown modifiers (e.g. non-military)
	private static boolean isViableNonEnemyTarget(LivingEntity target) {
		return false;
	}
	
	private static boolean isViableTargetingTarget(Mob targeter) {
		return true;
	}
	
	private static boolean pickUpPredicate(LivingEntity entity) {
		Brain<?> brain = entity.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)) {
			return false;
		}
		ItemEntity item = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
		return item != null && !item.isRemoved() && !item.getItem().isEmpty();
	}
	
}
