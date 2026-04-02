package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.network.particles.TeleportParticlesPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.kayn.fallen_gems_affixes.types.common.LivingEntitySetter;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class ArrowTeleSlashEventHandler {

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (arrow.getPersistentData().getBoolean("apoth.generated")) return;
        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;

        ArrowTeleSlashBonus bonus = null;
        LootRarity rarity = null;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        Inventory inv = player.getInventory();

        if (main.isEmpty()) return;
        LootCategory cat = LootCategory.forItem(main);
        for (GemInstance inst : SocketHelper.getGems(main).gems()) {
            if (!inst.isValid()) continue;
            LootRarity r = inst.rarity().get();
            Optional<?> opt = inst.gem().get().getBonus(cat, r);
            if (opt.isPresent() && opt.get() instanceof ArrowTeleSlashBonus atb && atb.supports(r)) {
                bonus = atb;
                rarity = r;
                break;
            }
        }

        ArrowTeleSlashBonus finalBonus = bonus;
        LootRarity finalRarity = rarity;
        if (finalBonus == null || finalRarity == null) return;
        if (MiscUtil.isOnCooldown(finalBonus.getId(), finalBonus.getCooldown(finalRarity) * 20, player)) {
            return;
        }

        Vec3 destination = resolveDestination(event, arrow);
        if (destination == null) return;

        ServerLevel level = (ServerLevel) player.level();
        Vec3 from = player.position();
        Vec3 dir = destination.subtract(from);
        double length = dir.length();
        Vec3 stepDir = dir.normalize();
        double step = 1.0;
        int steps = (int) (length / step);
        if (steps > 50) return;

        List<LivingEntity> targets = new ArrayList<>();
        boolean hasTarget = false;
        outer: for (int i = 0; i <= steps; i++) {

        Vec3 point = from.add(stepDir.scale(i * step));
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(point, point).inflate(finalBonus.getRadius(finalRarity)),
                    e -> e != player && e.isAlive() && !e.isAlliedTo(player) && !(e instanceof Player)
            );
            if (!hasTarget && !entities.isEmpty()) {
                hasTarget = true;
            }
            for (LivingEntity e : entities) {
                if (!e.getTags().contains("fga.ats_counted")) {
                    e.addTag("fga.ats_counted");
                    targets.add(e);
                    if (targets.size() > 20) break outer;
                }
            }
        }

        if (!hasTarget) return;
        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0f, 1.0f);
        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(),
                1.0f, 0.85f + level.random.nextFloat() * 0.3f);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                destination.x, destination.y + 1.0, destination.z,
                20, 0.5, 0.8, 0.5, 0.05);

        for (LivingEntity entity : targets) {
            entity.removeTag("fga.ats_counted");
        }

        Vec3 arrowDir = arrow.getDeltaMovement();
        if (arrowDir.lengthSqr() > 1e-4) {
            player.lookAt(EntityAnchorArgument.Anchor.EYES,
                    destination.add(arrowDir.normalize()));
            player.setYHeadRot(player.getYRot());
        }

        player.teleportTo(destination.x, destination.y, destination.z);
        double d0 = -Mth.sin(player.getYRot() * ((float)Math.PI / 180F));
        double d1 = Mth.cos(player.getYRot() * ((float)Math.PI / 180F));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 4, d0, 0.0D, d1, 0.0D);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new TeleportParticlesPacket(from, destination));

        inv.offhand.set(0, main);
        inv.setItem(inv.selected, off);

        DelayedTaskScheduler.schedule(level, 1, () -> {
            ItemStack main1 = player.getMainHandItem();
            ItemStack off1 = player.getOffhandItem();
            try {
                if (targets.isEmpty()) return;

                for (LivingEntity target : targets) {
                    if (!target.isAlive()) continue;
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                            4, 0.3, 0.2, 0.3, 0.05);
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                            target.getX(), target.getY() + target.getBbHeight() * 0.75, target.getZ(),
                            3, 0.2, 0.2, 0.2, 0.1);
                    target.invulnerableTime = 0;
                    ((LivingEntitySetter) player).FGA$setAttackStrengthTicker(1000);
                    player.attack(target);
                }

                MiscUtil.startCooldown(finalBonus.getId(), player);
            } finally {
                inv.offhand.set(0, main1);
                inv.setItem(inv.selected, off1);
            }
        });
    }

    private static Vec3 resolveDestination(ProjectileImpactEvent event, AbstractArrow arrow) {
        if (event.getRayTraceResult() instanceof EntityHitResult entityHit) {
            Vec3 dir = arrow.getDeltaMovement().normalize();
            return entityHit.getEntity().position().subtract(dir.scale(1.2));
        } else if (event.getRayTraceResult() instanceof BlockHitResult blockHit) {
            Vec3 hitPos = blockHit.getLocation();
            Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
            return hitPos.add(normal.scale(0.6));
        }
        return null;
    }
}