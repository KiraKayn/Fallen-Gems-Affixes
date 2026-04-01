package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.network.particles.TeleportParticlesPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.types.common.LivingEntitySetter;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
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

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
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

        Vec3 arrowDir = arrow.getDeltaMovement();
        if (arrowDir.lengthSqr() > 1e-4) {
            player.lookAt(EntityAnchorArgument.Anchor.EYES,
                    destination.add(arrowDir.normalize()));
            player.setYHeadRot(player.getYRot());
        }

        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0f, 1.0f);

        Vec3 dir = destination.subtract(from);
        double length = dir.length();
        Vec3 stepDir = dir.normalize();
        double step = 0.5;
        int steps = (int) (length / step);
        List<LivingEntity> targets = new ArrayList<>();
        for (int i = 0; i <= steps; i++) {

            Vec3 point = from.add(stepDir.scale(i * step));
            if (i % 2 == 0) {
                List<LivingEntity> entities = level.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(point, point).inflate(finalBonus.getRadius(finalRarity)),
                        e -> e != player && e.isAlive() && !e.isAlliedTo(player) && !(e instanceof Player)
                );

                for (LivingEntity e : entities) {
                    if (!e.getTags().contains("fga.ats_counted")) {
                        e.addTag("fga.ats_counted");
                        targets.add(e);
                    }
                }
            }
            level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    point.x, point.y + 1, point.z,
                    2, 0, 0.5, 0.5, 1);
//            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
//                    point.x, point.y, point.z,
//                    300, 0.2, 0.2, 0.2, 0.1);

        }
        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(),
                1.0f, 4.0f + level.random.nextFloat() * 0.3f);

        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                destination.x, destination.y + 1.0, destination.z,
                20, 0.5, 0.8, 0.5, 0.05);
        for (LivingEntity entity : targets) {
            entity.removeTag("fga.ats_counted");
        }

        player.teleportTo(destination.x, destination.y, destination.z);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new TeleportParticlesPacket(from, destination));

        inv.offhand.set(0, main);
        inv.setItem(inv.selected, off);

        DelayedTaskScheduler.schedule(player.level(), 1, () -> {
            try {
                if (player.getMainHandItem() != off || player.getOffhandItem() != main) return;

                if (targets.isEmpty()) return;

                for (LivingEntity target : targets) {
                    target.invulnerableTime = 0;
                    ((LivingEntitySetter) player).FGA$setAttackStrengthTicker();
                    player.attack(target);
                }

                MiscUtil.startCooldown(finalBonus.getId(), player);
            } finally {
                inv.offhand.set(0, off);
                inv.setItem(inv.selected, main);
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