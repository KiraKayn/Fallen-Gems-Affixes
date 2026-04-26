package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.kayn.fallen_gems_affixes.util.SpellCastUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.kayn.fallen_gems_affixes.adventure.affix.SpellCastAffix.isCurrentlyTriggering;
import static net.kayn.fallen_gems_affixes.adventure.affix.SpellCastAffix.setTriggering;

public class HolyMarkEventHandler {

    private static final String HM_COUNTED = "fga.hm_counted";
    private static final ResourceLocation SUNBEAM_ID =
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "sunbeam");
    private static final int MAX_TARGETS = 8;

    public static final Map<UUID, Long> MARK_EXPIRY = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow)) return;
        if (arrow.getPersistentData().getBoolean("apoth.generated")) return;
        if (arrow.getTags().contains(HM_COUNTED)) return;
        arrow.addTag(HM_COUNTED);
        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;
        if (isCurrentlyTriggering(player)) return;

        ItemStack main = player.getMainHandItem();
        if (main.isEmpty()) return;

        HolyMarkBonus bonus = null;
        LootRarity rarity = null;

        LootCategory cat = LootCategory.forItem(main);
        for (GemInstance inst : SocketHelper.getGems(main).gems()) {
            if (!inst.isValid()) continue;
            LootRarity r = inst.rarity().get();
            Optional<?> opt = inst.gem().get().getBonus(cat, r);
            if (opt.isPresent() && opt.get() instanceof HolyMarkBonus b && b.supports(r)) {
                bonus = b;
                rarity = r;
                break;
            }
        }

        if (bonus == null || rarity == null) return;

        Vec3 impactPos = resolveImpactPosition(event);
        if (impactPos == null) return;

        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        Long expiry = MARK_EXPIRY.get(player.getUUID());
        boolean markActive = expiry != null && now < expiry;

        if (markActive) {
            MARK_EXPIRY.remove(player.getUUID());
            MiscUtil.startCooldown(bonus.getId(), player);

            AbstractSpell sunbeam = SpellRegistry.REGISTRY.get().getValue(SUNBEAM_ID);
            if (sunbeam == null) return;

            float radius = bonus.getRadius(rarity);
            int spellLevel = bonus.getSpellLevel(rarity);

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(impactPos, impactPos).inflate(radius),
                    e -> e != player && e.isAlive() && !e.isAlliedTo(player) && !(e instanceof Player));

            if (targets.isEmpty()) return;

            level.sendParticles(ParticleTypes.END_ROD,
                    impactPos.x, impactPos.y + 1.0, impactPos.z, 20, 0.5, 0.5, 0.5, 0.12);
            level.playSound(null, impactPos.x, impactPos.y, impactPos.z,
                    SoundEvents.LIGHTNING_BOLT_THUNDER, player.getSoundSource(), 0.5f, 1.5f);

            List<LivingEntity> capped = targets.subList(0, Math.min(targets.size(), MAX_TARGETS));

            setTriggering(player, true);
            try {
                for (LivingEntity target : capped) {
                    SpellCastUtil.castSpell(player, sunbeam, spellLevel, target);
                }
            } finally {
                var server = player.level().getServer();
                if (server != null) {
                    server.execute(() -> setTriggering(player, false));
                } else {
                    setTriggering(player, false);
                }
            }

        } else {
            if (MiscUtil.isOnCooldown(bonus.getId(), (long) (bonus.getCooldown(rarity) * 20), player)) return;

            long newExpiry = now + bonus.getMarkDurationTicks(rarity);
            MARK_EXPIRY.put(player.getUUID(), newExpiry);

            level.sendParticles(ParticleTypes.END_ROD,
                    impactPos.x, impactPos.y + 0.5, impactPos.z, 12, 0.2, 0.4, 0.2, 0.05);
            level.playSound(null, impactPos.x, impactPos.y, impactPos.z,
                    SoundEvents.EXPERIENCE_ORB_PICKUP, player.getSoundSource(), 1.0f, 2.0f);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MARK_EXPIRY.clear();
    }

    private static Vec3 resolveImpactPosition(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult entityHit) {
            return entityHit.getEntity().position();
        } else if (event.getRayTraceResult() instanceof BlockHitResult blockHit) {
            return blockHit.getLocation();
        }
        return null;
    }
}