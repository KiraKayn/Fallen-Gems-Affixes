package net.kayn.fallen_gems_affixes.adventure.set.colossus;

import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffixHelper;
import net.kayn.fallen_gems_affixes.adventure.set.SetBonusHandler;
import net.kayn.fallen_gems_affixes.event.PlayerCriticalHitEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class ColossusSetAffixEventHandler {

    static final ThreadLocal<Boolean> IN_SHOCKWAVE = ThreadLocal.withInitial(() -> false);

    private static final UUID SPEED_UUID = UUID.fromString("c0105505-b00b-0001-cafe-000000000001");

    @SubscribeEvent
    public static void onCriticalHit(PlayerCriticalHitEvent event) {
        try {
            Player player = event.getPlayer();
            if (player.level().isClientSide) return;

            SetAffix affix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.HEAD));
            if (!(affix instanceof ColossusHelmetAffix ha)) return;

            int orbs = BastionOrbManager.getOrbs(player);
            if (orbs == 0) return;

            int consumed = BastionOrbManager.consumeOrbs(player, 1);
            if (consumed == 0) return;

            float damage = ha.getShockwaveBaseDamage() + ha.getShockwaveDamagePerOrb() * orbs;
            LivingEntity target = event.getTarget();

            if (!IN_SHOCKWAVE.get()) {
                IN_SHOCKWAVE.set(true);
                try {
                    ColossusShockwaveHelper.release(player, target.getX(), target.getY() + 0.5, target.getZ(),
                            damage, ha.getShockwaveRadius(), player.level());
                } finally {
                    IN_SHOCKWAVE.set(false);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTakeDamage(LivingHurtEvent event) {
        try {
            if (event.isCanceled()) return;
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;

            float damage = event.getAmount();

            SetAffix chestAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.CHEST));
            if (chestAffix instanceof ColossusChestplateAffix ca) {
                float maxHP = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
                int orbsToGrant = ca.getOrbsPerHit();
                if (damage >= maxHP * ca.getHeavyHitThreshold()) {
                    orbsToGrant += ca.getHeavyHitBonusOrbs();
                }
                if (BastionOrbManager.isStandingBonusReady(player)) {
                    orbsToGrant++;
                    BastionOrbManager.setStandingBonusReady(player, false);
                }
                BastionOrbManager.addOrbs(player, orbsToGrant);
            }

            if (BastionOrbManager.getOrbs(player) > 0) {
                SetAffix legsAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.LEGS));
                if (legsAffix instanceof ColossusLeggingsAffix) {
                    BastionOrbManager.addAccumulatedDamage(player, damage);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onOrbConsumed(BastionOrbConsumedEvent event) {
        try {
            Player player = event.getPlayer();
            if (player.level().isClientSide) return;

            SetAffix legsAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.LEGS));
            if (!(legsAffix instanceof ColossusLeggingsAffix la)) return;

            float accum = BastionOrbManager.getAccumulatedDamage(player);
            float damage = la.getBaseShockwaveDamage() + accum * la.getDamageScaleFraction();
            double radius = la.getShockwaveRadius();

            boolean wasMaxOrbs = event.getOrbsBefore() >= ColossusSetConstants.MAX_ORBS
                    && event.getConsumed() >= ColossusSetConstants.MAX_ORBS;
            if (wasMaxOrbs) {
                damage *= la.getMaxOrbDamageMultiplier();
                radius *= la.getMaxOrbRadiusMultiplier();
            }

            if (!IN_SHOCKWAVE.get()) {
                IN_SHOCKWAVE.set(true);
                try {
                    if (wasMaxOrbs) {
                        ColossusShockwaveHelper.releaseMega(player, damage, radius, player.level());
                    } else {
                        ColossusShockwaveHelper.release(player, player.getX(), player.getY() + 0.5, player.getZ(),
                                damage, radius, player.level());
                    }
                } finally {
                    IN_SHOCKWAVE.set(false);
                }
            }

            if (event.getOrbsAfter() == 0) {
                BastionOrbManager.resetAccumulatedDamage(player);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        try {
            if (!(event.getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;

            SetAffix shieldAffix = findShieldAffix(player);
            if (!(shieldAffix instanceof ColossusShieldAffix sa)) return;

            if (BastionOrbManager.isStandingBonusReady(player)) {
                BastionOrbManager.addOrbs(player, 2);
                BastionOrbManager.setStandingBonusReady(player, false);
            } else {
                BastionOrbManager.addOrbs(player, 1);
            }

            if (BastionOrbManager.isUnstoppable(player)) {
                Entity attackerEntity = event.getDamageSource().getEntity();
                if (attackerEntity instanceof LivingEntity attacker) {
                    float reflected = event.getBlockedDamage() * sa.getReflectFraction();
                    attacker.hurt(player.level().damageSources().thorns(player), reflected);

                    AABB area = attacker.getBoundingBox().inflate(sa.getStunRadius());
                    player.level().getEntitiesOfClass(LivingEntity.class, area,
                            e -> e != player && !e.isDeadOrDying()).forEach(e ->
                            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, sa.getStunDurationTicks(), 10)));

                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.EXPLOSION, attacker.getX(), attacker.getY() + 1, attacker.getZ(),
                                8, sa.getStunRadius() * 0.5, 0.5, sa.getStunRadius() * 0.5, 0.05);
                        sl.sendParticles(ParticleTypes.CRIT, attacker.getX(), attacker.getY() + 1, attacker.getZ(),
                                15, sa.getStunRadius() * 0.4, 0.3, sa.getStunRadius() * 0.4, 0.1);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        try {
            if (!(event.getEntity() instanceof Player player)) return;
            if (!BastionOrbManager.isUnstoppable(player)) return;
            if (SetBonusHandler.getSetPieceCount(player, ColossusSetConstants.SET_ID) < 1) return;
            event.setCanceled(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerAttack(LivingHurtEvent event) {
        try {
            if (event.isCanceled()) return;
            if (IN_SHOCKWAVE.get()) return;
            if (!(event.getSource().getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;
            if (!BastionOrbManager.isUnstoppable(player)) return;

            int pieces = SetBonusHandler.getSetPieceCount(player, ColossusSetConstants.SET_ID);
            if (pieces < 1) return;

            LivingEntity target = event.getEntity();

            IN_SHOCKWAVE.set(true);
            try {
                AABB cleaveArea = target.getBoundingBox().inflate(3.0);
                float cleaveDamage = event.getAmount() * 0.35f;
                player.level().getEntitiesOfClass(LivingEntity.class, cleaveArea,
                        e -> e != player && e != target && !(e instanceof Player) && !e.isDeadOrDying()).forEach(e ->
                        e.hurt(player.level().damageSources().playerAttack(player), cleaveDamage));

                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(),
                            6, 1.5, 0.1, 1.5, 0);
                }

                SetAffix bootsAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.FEET));
                if (bootsAffix instanceof ColossusBootsAffix ba) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ba.getRootDurationTicks(), 10));
                }

                if (pieces >= 5) {
                    float swDamage = event.getAmount() * 0.25f;
                    ColossusShockwaveHelper.release(player, player.getX(), player.getY() + 0.5, player.getZ(),
                            swDamage, 2.5, player.level());
                }
            } finally {
                IN_SHOCKWAVE.set(false);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase != TickEvent.Phase.END) return;
            Player player = event.player;
            if (player.level().isClientSide) return;

            if (player.tickCount % 5 == 0) BastionOrbManager.spawnOrbVisuals(player);

            updateBootsEffects(player);

            if (player.tickCount % 20 == 0) updateChestplateMaxHealthBonus(player);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void updateBootsEffects(Player player) {
        SetAffix bootsAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.FEET));
        if (!(bootsAffix instanceof ColossusBootsAffix ba)) {
            removeSpeedModifier(player);
            return;
        }

        int orbs = BastionOrbManager.getOrbs(player);

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_UUID);
            if (orbs > 0) {
                double bonus = orbs * ba.getSpeedPerOrb();
                speedAttr.addTransientModifier(new AttributeModifier(SPEED_UUID,
                        "colossus_momentum", bonus, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }

        if (orbs >= ColossusSetConstants.MAX_ORBS) {
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }

        net.minecraft.world.phys.Vec3 dm = player.getDeltaMovement();
        boolean standing = Math.abs(dm.x) < 0.01 && Math.abs(dm.z) < 0.01 && player.onGround();
        if (standing) {
            int ticks = BastionOrbManager.getStandingTicks(player) + 1;
            BastionOrbManager.setStandingTicks(player, ticks);
            if (!BastionOrbManager.isStandingBonusReady(player) && ticks >= ba.getStandingStillThresholdTicks()) {
                BastionOrbManager.setStandingBonusReady(player, true);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 1, player.getZ(),
                            12, 0.3, 0.5, 0.3, 0.04);
                }
            }
        } else {
            BastionOrbManager.setStandingTicks(player, 0);
        }
    }

    private static void updateChestplateMaxHealthBonus(Player player) {
        SetAffix chestAffix = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.CHEST));
        if (!(chestAffix instanceof ColossusChestplateAffix ca)) return;

        AttributeInstance maxHP = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHP == null) return;

        UUID healthUUID = UUID.fromString("c0105505-b00b-0002-cafe-000000000002");
        maxHP.removeModifier(healthUUID);

        int orbs = BastionOrbManager.getOrbs(player);
        if (orbs == 0) return;

        float baseMax = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        float bonus = ca.getMaxHealthPerOrbFraction() * orbs * baseMax;

        if (bonus > 0) {
            maxHP.addTransientModifier(new AttributeModifier(healthUUID,
                    "colossus_bastion", bonus, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeSpeedModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) attr.removeModifier(SPEED_UUID);
    }

    private static SetAffix findShieldAffix(Player player) {
        SetAffix a = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.OFFHAND));
        if (a instanceof ColossusShieldAffix) return a;
        a = SetAffixHelper.getSetAffix(player.getItemBySlot(EquipmentSlot.MAINHAND));
        if (a instanceof ColossusShieldAffix) return a;
        return null;
    }
}