package rbasamoyai.industrialwarfare.common.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tags.IWBlockTags;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages;

public class BulletEntity extends ThrowableEntity {

	private static final String DAMAGE_SOURCE_KEY = IndustrialWarfare.MOD_ID + ".bullet";
	
	private static final String TAG_DAMAGE = "damage";
	private static final String TAG_HEADSHOT_MULTIPLIER = "headshotMultiplier";
	private static final String TAG_ORIGIN = "origin";
	
	private float damage;
	private float headshotMultiplier;
	private Vector3d origin;
	
	public BulletEntity(EntityType<? extends BulletEntity> type, World world) {
		this(type, world, 0.0f, 0.0f, Vector3d.ZERO);
	}
	
	public BulletEntity(World world, LivingEntity owner, float damage, float headshotMultiplier) {
		this(EntityTypeInit.BULLET.get(), world, damage, headshotMultiplier, new Vector3d(owner.getX(), owner.getEyeY(), owner.getZ()));
		this.setOwner(owner);
		this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
	}
	
	public BulletEntity(EntityType<? extends BulletEntity> type, World world, float damage, float headshotMultiplier, Vector3d origin) {
		super(type, world);
		this.damage = damage;
		this.headshotMultiplier = headshotMultiplier;
		this.origin = origin; 
	}
	
	@Override
	protected void defineSynchedData() {
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	private static final int FORMATION_DISTANCE = 5;
	
	@Override
	protected boolean canHitEntity(Entity entity) {
		Entity owner = this.getOwner();
		
		if (owner != null && owner instanceof LivingEntity && entity instanceof LivingEntity) {
			Brain<?> ownerBrain = ((LivingEntity) owner).getBrain();
			Brain<?> hitBrain = ((LivingEntity) entity).getBrain();
			if (ownerBrain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())
				&& hitBrain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())
				&& owner.position().closerThan(entity.position(), FORMATION_DISTANCE)) {
				return false;
			}
		}
		return super.canHitEntity(entity);
	}
	
	@Override
	protected void onHitEntity(EntityRayTraceResult result) {
		super.onHitEntity(result);
		Entity hit = result.getEntity();
		float damage = this.damage;
		if (MathHelper.abs((float)(this.getEyeY() - hit.getEyeY())) < 0.2f) {
			damage *= this.headshotMultiplier;
			if (!this.level.isClientSide) {
				Entity owner = this.getOwner();
				if (owner instanceof ServerPlayerEntity) {
					IWNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) owner), new FirearmActionMessages.CNotifyHeadshot());
				}
			}
		}
		hit.hurt(new IndirectEntityDamageSource(DAMAGE_SOURCE_KEY, this, this.getOwner()), damage);
		hit.invulnerableTime = 0;
		this.remove();
	}
	
	@Override
	protected void onHitBlock(BlockRayTraceResult result) {
		BlockPos pos = result.getBlockPos();
		BlockState blockstate = this.level.getBlockState(pos);
		Block block = blockstate.getBlock();
		
		boolean shouldRemove = true;
		
		if (block == Blocks.TNT) {
			Entity owner = this.getOwner();
			blockstate.catchFire(this.level, pos, result.getDirection(), owner instanceof LivingEntity ? (LivingEntity) owner : null);
			this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		} else if (block.is(IWBlockTags.SHATTERABLE)) {
			this.level.destroyBlock(pos, false);
			shouldRemove = false;
		} else if (block == Blocks.MELON) {
			this.level.destroyBlock(pos, true);
			shouldRemove = false;
		}
		
		if (shouldRemove) this.remove();
	}
	
	@Override
	public void addAdditionalSaveData(CompoundNBT tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat(TAG_DAMAGE, this.damage);
		tag.putFloat(TAG_HEADSHOT_MULTIPLIER, this.headshotMultiplier);
		
		CompoundNBT originTag = new CompoundNBT();
		originTag.putDouble("x", this.origin.x);
		originTag.putDouble("y", this.origin.y);
		originTag.putDouble("z", this.origin.z);
		tag.put(TAG_ORIGIN, originTag);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundNBT tag) {
		this.damage = tag.getFloat(TAG_DAMAGE);
		this.headshotMultiplier = tag.getFloat(TAG_HEADSHOT_MULTIPLIER);
		
		CompoundNBT originTag = tag.getCompound(TAG_ORIGIN);
		this.origin = new Vector3d(originTag.getDouble("x"), originTag.getDouble("y"), originTag.getDouble("z"));
		
		super.readAdditionalSaveData(tag);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	protected float getGravity() {
		return 0.01f;
	}

}
