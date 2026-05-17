package net.kayn.fallen_gems_affixes.entity;

import net.kayn.fallen_gems_affixes.event.ShadowCloneDeathEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ShadowCloneEntity extends PathfinderMob {

    private static final int DEFAULT_LIFETIME = 30 * 20;

    private UUID ownerUUID = null;
    private boolean dieOnHit = true;
    private int ticksAlive = 0;
    private boolean deathEventFired = false;

    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;

    public ShadowCloneEntity(EntityType<? extends ShadowCloneEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);

        super.tick();

        if (this.isRemoved()) return;

        Player owner = getOwner();

        if (!this.level().isClientSide) {
            ticksAlive++;
            if (ticksAlive >= DEFAULT_LIFETIME) {
                fireDeathEvent();
                this.discard();
                return;
            }
            if (owner == null || owner.isDeadOrDying()) {
                this.discard();
                return;
            }
        }

        if (owner != null) {
            this.setPos(
                    owner.getX() + offsetX,
                    owner.getY() + offsetY,
                    owner.getZ() + offsetZ
            );
            this.setDeltaMovement(Vec3.ZERO);
            this.setYRot(owner.getYRot());
            this.yHeadRot = owner.yHeadRot;
            this.setXRot(owner.getXRot());
        }
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public Optional<UUID> getOwnerUUID() {
        return Optional.ofNullable(ownerUUID);
    }

    @Nullable
    public Player getOwner() {
        if (ownerUUID == null) return null;
        return this.level().getPlayerByUUID(ownerUUID);
    }

    public void setDieOnHit(boolean value) {
        this.dieOnHit = value;
    }

    public boolean isDieOnHit() {
        return dieOnHit;
    }

    public void setOffset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {}

    @Override
    protected void pushEntities() {}

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) return false;
        if (this.isRemoved()) return false;
        if (source.getEntity() instanceof ShadowCloneEntity) return false;

        if (dieOnHit) {
            fireDeathEvent();
            this.discard();
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        fireDeathEvent();
        super.die(cause);
    }

    public void fireDeathEvent() {
        if (!this.level().isClientSide && !deathEventFired) {
            deathEventFired = true;
            MinecraftForge.EVENT_BUS.post(new ShadowCloneDeathEvent(this));
        }
    }

    public void strikeTarget(net.minecraft.world.entity.LivingEntity target, float multiplier) {
        Player owner = getOwner();
        if (owner == null || target.isDeadOrDying() || this.isRemoved()) return;
        float damage = (float) (owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier);
        target.hurt(this.level().damageSources().mobAttack(this), damage);
    }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putBoolean("DieOnHit", dieOnHit);
        tag.putInt("TicksAlive", ticksAlive);
        tag.putDouble("OffsetX", offsetX);
        tag.putDouble("OffsetY", offsetY);
        tag.putDouble("OffsetZ", offsetZ);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
        dieOnHit = tag.getBoolean("DieOnHit");
        ticksAlive = tag.getInt("TicksAlive");
        offsetX = tag.getDouble("OffsetX");
        offsetY = tag.getDouble("OffsetY");
        offsetZ = tag.getDouble("OffsetZ");
    }

    @Override
    public MobType getMobType() { return MobType.UNDEFINED; }

    @Override @Nullable
    protected SoundEvent getHurtSound(DamageSource src) { return null; }

    @Override @Nullable
    protected SoundEvent getDeathSound() { return null; }

    @Override @Nullable
    public SoundEvent getAmbientSound() { return null; }
}