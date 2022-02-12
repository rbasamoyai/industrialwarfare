package rbasamoyai.industrialwarfare.common.entityai;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.AttackTargetTask;
import net.minecraft.entity.ai.brain.task.EndAttackTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.InteractWithDoorTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.SwimTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.IHasDiplomaticOwner;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.tasks.EndDiplomacyAttackTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.EndPatrolAttackTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.ExtendedShootTargetTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.FinishMovementCommandTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.GoToWorkTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.LeaveWorkTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.PreciseWalkToPositionTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.ReturnToWorkIfPatrollingTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.RunCommandFromTaskScrollTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.WalkToTargetSpecialTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.WalkTowardsPosNoDelayTask;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class NPCTasks {

	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getCorePackage() {
		return ImmutableList.of(
				Pair.of(0, new GoToWorkTask(MemoryModuleType.JOB_SITE, 3.0f, 1, 100)),
				Pair.of(0, new LeaveWorkTask(MemoryModuleType.JOB_SITE, 3.0f, 1, 100)),
				Pair.of(0, new WalkToTargetSpecialTask()),
				Pair.of(0, new PreciseWalkToPositionTask(1.5f, 1.0d, 0.07d)),
				Pair.of(0, new InteractWithDoorTask()),
				Pair.of(0, new SwimTask(0.8f)),
				Pair.of(1, new LookTask(45, 90)),
				Pair.of(2, new FinishMovementCommandTask(MemoryModuleType.MEETING_POINT))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getIdlePackage() {
		return ImmutableList.of(
				Pair.of(0, new WalkTowardsPosNoDelayTask(MemoryModuleType.MEETING_POINT, 3.0f, 1, 100)),
				Pair.of(1, new LookAtEntityTask(4.0f))//,
				//Pair.of(2, new WalkTowardsLookTargetTask(2.5f, 2))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getWorkPackage() {
		return ImmutableList.of(
				Pair.of(0, new RunCommandFromTaskScrollTask()),
				Pair.of(1, new ForgetAttackTargetTask<>(NPCTasks::onPatrol, NPCTasks::findNearestValidAttackTarget))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getRestPackage() {
		return ImmutableList.of(Pair.of(0, new WalkTowardsPosTask(MemoryModuleType.HOME, 3.0f, 1, 100)));
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getFightPackage() {
		return ImmutableList.of(
				Pair.of(0, new WalkTowardsPosNoDelayTask(MemoryModuleType.MEETING_POINT, 3.0f, 1, 100)),
				Pair.of(1, new MoveToTargetTask(3.0f)),
				Pair.of(1, new ExtendedShootTargetTask<>()),
				Pair.of(1, new ForgetAttackTargetTask<>(NPCTasks::canFindNewTarget, NPCTasks::findNearestValidAttackTarget)),
				Pair.of(2, new EndAttackTask(0, (e1, e2) -> false)),
				Pair.of(2, new EndDiplomacyAttackTask<>()),
				Pair.of(2, new EndPatrolAttackTask()),
				Pair.of(3, new AttackTargetTask(20)),
				Pair.of(4, new ReturnToWorkIfPatrollingTask())
				);
	}
	
	private static boolean onPatrol(NPCEntity npc) {
		return npc.getBrain().hasMemoryValue(MemoryModuleTypeInit.ON_PATROL.get());
	}
	
	private static boolean canFindNewTarget(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		CombatMode mode = brain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.DONT_ATTACK);
		if (mode == CombatMode.DONT_ATTACK) return true;
		
		if (!brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return true;
		LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
		if (target instanceof IHasDiplomaticOwner) {
			PlayerIDTag npcOwner = npc.getDiplomaticOwner();
			PlayerIDTag otherOwner = ((IHasDiplomaticOwner) target).getDiplomaticOwner();
			if (npcOwner.equals(otherOwner)) return true;
			DiplomacySaveData saveData = DiplomacySaveData.get(npc.level);
			if (saveData.getDiplomaticStatus(npcOwner, otherOwner) == DiplomaticStatus.ALLY) return true;
		}
		
		if (mode == CombatMode.STAND_GROUND && !BrainUtil.isWithinAttackRange(npc, target, 0)) return true;
		
		if (mode == CombatMode.DEFEND) {
			if (!brain.hasMemoryValue(MemoryModuleTypeInit.CACHED_POS.get())
				|| !brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get()).get().dimension().equals(npc.level.dimension()))
				brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(npc.level.dimension(), npc.blockPosition()));
			BlockPos pos = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get()).get().pos();
			if (!pos.closerThan(npc.position(), 10) || !BrainUtil.isWithinAttackRange(npc, target, 0)) return true;
		}
		
		return target.isDeadOrDying();
	}
	
	private static Optional<? extends LivingEntity> findNearestValidAttackTarget(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		CombatMode mode = brain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.DONT_ATTACK);
		if (mode == CombatMode.DONT_ATTACK) return Optional.empty();

		PlayerIDTag npcOwner = npc.getDiplomaticOwner();
		DiplomacySaveData saveData = DiplomacySaveData.get(npc.level);
		
		Optional<Integer> pursuitOptional = brain.getMemory(MemoryModuleTypeInit.ON_PATROL.get());
		double pursuitDistance = npc.getAttributeValue(Attributes.FOLLOW_RANGE);
		if (pursuitOptional.isPresent()) {
			pursuitDistance = (double) pursuitOptional.get();
		}
		
		Optional<GlobalPos> gpop = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get());
		BlockPos pos = gpop.isPresent() && gpop.get().dimension() == npc.level.dimension() ? gpop.get().pos() : npc.blockPosition();
		
		List<LivingEntity> visibleEntities = brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Arrays.asList());
		for (LivingEntity e : visibleEntities) {		
			if (!e.blockPosition().closerThan(pos, pursuitDistance)) continue;
			
			if (e instanceof IHasDiplomaticOwner) {
				PlayerIDTag otherOwner = ((IHasDiplomaticOwner) e).getDiplomaticOwner();
				if (npcOwner.equals(otherOwner)) continue;
				DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, otherOwner);
				if (status == DiplomaticStatus.ENEMY || status != DiplomaticStatus.ALLY && isViableNonEnemyTarget(e)) {
					return Optional.of(e);
				}
			}
			
			Brain<?> targetBrain = e.getBrain();
			if (targetBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && targetBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() == npc) {
				return Optional.of(e);
			}
			if (e instanceof MobEntity && ((MobEntity) e).getTarget() == npc) {
				return Optional.of(e);
			}
			
			if (e instanceof PlayerEntity) {
				PlayerIDTag otherPlayerTag = PlayerIDTag.of((PlayerEntity) e);
				if (npcOwner.equals(otherPlayerTag)) continue;
				DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, PlayerIDTag.of((PlayerEntity) e));
				// TODO: stand down modifier (e.g. diplomatic meeting in game or sumfin)
				if (status == DiplomaticStatus.ENEMY || status != DiplomaticStatus.ALLY && isViableNonEnemyTarget(e)) {
					return Optional.of(e);
				}
			}
		}
		
		return Optional.empty();
	}
	
	// TODO: neutral/unknown modifiers (e.g. non-military/attacking)
	private static boolean isViableNonEnemyTarget(LivingEntity target) {
		return true;
	}
	
}
