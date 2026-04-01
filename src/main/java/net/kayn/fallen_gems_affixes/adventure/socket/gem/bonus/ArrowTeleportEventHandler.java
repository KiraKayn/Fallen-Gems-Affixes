package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.network.particles.TeleportParticlesPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

public class ArrowTeleportEventHandler {

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ArrowTeleportBonus bonus = null;
        LootRarity rarity = null;

        outer:
        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (stack.isEmpty()) continue;
            LootCategory cat = LootCategory.forItem(stack);
            for (GemInstance inst : SocketHelper.getGems(stack).gems()) {
                if (!inst.isValid()) continue;
                LootRarity r = inst.rarity().get();
                Optional<?> opt = inst.gem().get().getBonus(cat, r);
                if (opt.isPresent() && opt.get() instanceof ArrowTeleportBonus atb && atb.supports(r)) {
                    bonus = atb;
                    rarity = r;
                    break outer;
                }
            }
        }

        if (bonus == null || rarity == null) return;

        long now = System.currentTimeMillis();
        long cdMillis = (long) (bonus.getCooldown(rarity) * 1000L);
        Long last = COOLDOWNS.get(player.getUUID());
        if (last != null && now - last < cdMillis) return;
        COOLDOWNS.put(player.getUUID(), now);

        Vec3 destination = resolveDestination(event, arrow);
        if (destination == null) return;

        ServerLevel level = (ServerLevel) player.level();
        Vec3 from = player.position();

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new TeleportParticlesPacket(from, destination));

        player.teleportTo(destination.x, destination.y, destination.z);

        Vec3 arrowDir = arrow.getDeltaMovement();
        if (arrowDir.lengthSqr() > 1e-4) {
            player.lookAt(EntityAnchorArgument.Anchor.EYES,
                    destination.add(arrowDir.normalize()));
            player.setYHeadRot(player.getYRot());
        }

        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0f, 1.0f);

        float radius = bonus.getRadius(rarity);
        AABB box = AABB.ofSize(destination, radius * 2, radius * 2, radius * 2);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e != player && e.isAlive() && !e.isAlliedTo(player) && !(e instanceof Player)
        );

        if (targets.isEmpty()) return;

        float damage = resolveOffhandDamage(player);

        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            target.hurt(level.damageSources().playerAttack(player), damage);

            level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    4, 0.3, 0.2, 0.3, 0.05);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    target.getX(), target.getY() + target.getBbHeight() * 0.75, target.getZ(),
                    3, 0.2, 0.2, 0.2, 0.1);
        }

        level.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(),
                1.0f, 0.85f + level.random.nextFloat() * 0.3f);

        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                destination.x, destination.y + 1.0, destination.z,
                20, 0.5, 0.8, 0.5, 0.05);
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

    private static float resolveOffhandDamage(ServerPlayer player) {
        ItemStack offhand = player.getOffhandItem();

        double currentDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        double mainhandBonus = player.getMainHandItem()
                .getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE)
                .stream()
                .filter(m -> m.getOperation() == AttributeModifier.Operation.ADDITION)
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        if (offhand.isEmpty()) {
            return (float) Math.max(1, currentDamage - mainhandBonus);
        }

        double offhandBonus = offhand
                .getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE)
                .stream()
                .filter(m -> m.getOperation() == AttributeModifier.Operation.ADDITION)
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        return (float) Math.max(1, currentDamage - mainhandBonus + offhandBonus);
    }
}