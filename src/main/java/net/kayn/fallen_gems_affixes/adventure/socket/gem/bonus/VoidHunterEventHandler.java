package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.network.particles.TeleportParticlesPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VoidHunterEventHandler {

    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final double SEARCH_RADIUS = 24.0;
    private static final String ECHOING_STRIKES_ID = "irons_spellbooks:echoing_strikes";

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        VoidHunterBonus bonus = null;
        LootRarity rarity = null;

        for (GemInstance inst : SocketHelper.getGems(weapon).gems()) {
            if (!inst.isValid()) continue;

            for (var b : inst.gem().get().getBonuses()) {
                if (b instanceof VoidHunterBonus vhb) {
                    LootRarity r = inst.rarity().get();
                    if (vhb.supports(r)) {
                        bonus = vhb;
                        rarity = r;
                        break;
                    }
                }
            }

            if (bonus != null) break;
        }

        if (bonus == null || rarity == null) return;

        long now = System.currentTimeMillis();
        long cdMillis = (long) (bonus.getCooldown(rarity) * 1000);

        Long lastTrigger = COOLDOWNS.get(player.getUUID());
        if (lastTrigger != null && now - lastTrigger < cdMillis) return;

        ServerLevel level = (ServerLevel) player.level();
        AABB box = new AABB(
                player.getX() - SEARCH_RADIUS, player.getY() - SEARCH_RADIUS, player.getZ() - SEARCH_RADIUS,
                player.getX() + SEARCH_RADIUS, player.getY() + SEARCH_RADIUS, player.getZ() + SEARCH_RADIUS
        );

        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e != player &&
                        e != dead &&
                        e.isAlive() &&
                        !e.isAlliedTo(player) &&
                        !(e instanceof Player)
        );

        if (candidates.isEmpty()) return;

        LivingEntity target = candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);

        if (target == null) return;

        COOLDOWNS.put(player.getUUID(), now);

        double dx = player.getX() - target.getX();
        double dz = player.getZ() - target.getZ();
        double angle = Math.atan2(dz, dx);

        double tpX = target.getX() + Math.cos(angle) * 1.5;
        double tpY = target.getY();
        double tpZ = target.getZ() + Math.sin(angle) * 1.5;

        Vec3 from = player.position();
        Vec3 to = new Vec3(tpX, tpY, tpZ);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new TeleportParticlesPacket(from, to));

        player.teleportTo(tpX, tpY, tpZ);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        player.setYHeadRot(player.getYRot());

        level.playSound(
                null,
                tpX, tpY, tpZ,
                SoundEvents.ENDERMAN_TELEPORT,
                player.getSoundSource(),
                1.0f,
                1.0f
        );

        player.attack(target);

        MobEffect effectType = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(ECHOING_STRIKES_ID));
        if (effectType != null) {
            int durationTicks = (int) (bonus.getDuration(rarity) * 20);
            target.removeEffect(effectType);
            player.addEffect(new MobEffectInstance(effectType, durationTicks, 0, false, true));
        }
    }
}