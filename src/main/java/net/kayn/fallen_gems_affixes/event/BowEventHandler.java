package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.adventure.affix.ChainShotAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.MultiShotAffix;
import net.kayn.fallen_gems_affixes.compat.celestisynth.CelestisynthCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

import java.util.Comparator;
import java.util.List;

public class BowEventHandler {

    private static final boolean HAS_CELESTISYNTH = ModList.get().isLoaded("celestisynth");
    private static boolean firingExtraArrows = false;

    // multi shot
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (firingExtraArrows) return;
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        Player player = event.getEntity();
        ItemStack bow = event.getBow();
        float power   = BowItem.getPowerForTime(event.getCharge());
        if (power < 0.1f) return;

        int totalExtraShots = 0;
        boolean bypass      = false;

        for (var inst : AffixHelper.getAffixes(bow).values()) {
            if (!inst.isValid()) continue;
            if (inst.affix().get() instanceof MultiShotAffix affix) {
                totalExtraShots += affix.getExtraShots(inst.getRarity(), inst.level());
                if (affix.isBypassIframes(inst.getRarity())) bypass = true;
            }
        }
        if (totalExtraShots <= 0) return;

        final boolean bypassIframes = bypass;

        firingExtraArrows = true;
        try {
            for (int i = 1; i <= totalExtraShots; i++) {
                int   side      = (i % 2 == 1) ? 1 : -1;
                int   step      = (i + 1) / 2;
                float yawOffset = side * step * MultiShotAffix.SPREAD_DEGREES;

                AbstractArrow extraArrow = spawnExtraArrow(serverLevel, player, bow);
                if (extraArrow == null) continue;

                extraArrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                extraArrow.shootFromRotation(
                        player,
                        player.getXRot(),
                        player.getYRot() + yawOffset,
                        0.0f,
                        power * 3.0f,
                        1.0f);
                if (power == 1.0f) extraArrow.setCritArrow(true);
                extraArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

                if (bypassIframes) {
                    extraArrow.getPersistentData().putBoolean(MultiShotAffix.KEY_BYPASS_IFRAMES, true);
                }

                serverLevel.addFreshEntity(extraArrow);
            }
        } finally {
            firingExtraArrows = false;
        }

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0f, 1.0f / (serverLevel.getRandom().nextFloat() * 0.4f + 1.2f) + power * 0.5f);
    }

    // chain shot
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onArrowHit(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;

        LivingEntity target = event.getEntity();

        if (arrow.getPersistentData().getBoolean(MultiShotAffix.KEY_BYPASS_IFRAMES)) {
            int savedHurtTime   = target.hurtTime;
            int savedInvulnTime = target.invulnerableTime;
            target.hurtTime         = 0;
            target.invulnerableTime = 0;
            if (event.isCanceled()) {
                target.hurtTime         = savedHurtTime;
                target.invulnerableTime = savedInvulnTime;
            }
        }

        if (arrow.getOwner() instanceof Player player) {
            if (!arrow.getPersistentData().getBoolean(ChainShotAffix.KEY_CHAIN_ARROW)) {
                player.getPersistentData().putFloat(
                        ChainShotAffix.KEY_LAST_ARROW_DAMAGE, event.getAmount());
            } else {
                float stored = player.getPersistentData()
                        .getFloat(ChainShotAffix.KEY_LAST_ARROW_DAMAGE);
                if (stored > 0f) event.setAmount(stored);
            }
        }
    }

    // chain shot
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow killingArrow)) return;
        if (killingArrow.getPersistentData().getBoolean(ChainShotAffix.KEY_CHAIN_ARROW)) return;
        if (!(killingArrow.getOwner() instanceof Player player)) return;
        if (!killingArrow.getPersistentData().contains(ChainShotAffix.KEY_CACHED_RANGE)) return;

        float maxRange = killingArrow.getPersistentData().getFloat(ChainShotAffix.KEY_CACHED_RANGE);
        if (maxRange <= 0f) return;

        LivingEntity deadEntity = event.getEntity();
        final float searchRange = maxRange;
        AABB searchBox = deadEntity.getBoundingBox().inflate(searchRange);
        List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                e -> e != deadEntity
                        && e != player
                        && e.isAlive()
                        && !(e instanceof Player));

        if (candidates.isEmpty()) return;

        LivingEntity chainTarget = candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(deadEntity)))
                .orElse(null);
        if (chainTarget == null) return;

        ItemStack sourceWeapon = AffixHelper.getSourceWeapon(killingArrow);
        AbstractArrow chainArrow = spawnExtraArrow(serverLevel, player,
                sourceWeapon.isEmpty() ? player.getMainHandItem() : sourceWeapon);
        if (chainArrow == null) return;

        if (killingArrow instanceof Arrow killingTyped && chainArrow instanceof Arrow chainTyped) {
            CompoundTag killingNbt = killingTyped.saveWithoutId(new CompoundTag());
            CompoundTag chainNbt   = chainTyped.saveWithoutId(new CompoundTag());
            for (String key : List.of("Potion", "CustomPotionEffects", "CustomPotionColor")) {
                if (killingNbt.contains(key)) chainNbt.put(key, killingNbt.get(key).copy());
            }
            chainTyped.load(chainNbt);
        }

        chainArrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

        double dx = chainTarget.getX() - player.getX();
        double dy = chainTarget.getY() + chainTarget.getBbHeight() * 0.5 - chainArrow.getY();
        double dz = chainTarget.getZ() - player.getZ();
        chainArrow.shoot(dx, dy, dz, 3.0f, 0f);

        chainArrow.setBaseDamage(1.0);

        if (!sourceWeapon.isEmpty()) {
            AffixHelper.copyToProjectile(sourceWeapon, chainArrow);
        }

        chainArrow.getPersistentData().putBoolean(ChainShotAffix.KEY_CHAIN_ARROW, true);
        chainArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

        serverLevel.addFreshEntity(chainArrow);

        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.6f, 1.4f);
    }

    /**
     * Spawns a RainfallArrow if the bow is a Celestisynth Rainfall Serenity (and Celestisynth is loaded),
     * otherwise a vanilla Arrow.
     */
    private static AbstractArrow spawnExtraArrow(ServerLevel level, Player player, ItemStack bow) {
        if (HAS_CELESTISYNTH && isRainfallSerenity(bow)) {
            AbstractArrow arrow = CelestisynthCompat.spawnRainfallArrow(level, player);
            if (arrow != null) return arrow;
        }
        Arrow arrow = new Arrow(EntityType.ARROW, level);
        arrow.setOwner(player);
        return arrow;
    }

    private static boolean isRainfallSerenity(ItemStack bow) {
        if (!HAS_CELESTISYNTH) return false;
        return CelestisynthCompat.isRainfallSerenity(bow);
    }
}