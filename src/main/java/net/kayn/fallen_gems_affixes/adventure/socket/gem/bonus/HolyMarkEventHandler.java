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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
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
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (arrow.getPersistentData().getBoolean("apoth.generated")) return;
        if (arrow.getTags().contains(HM_COUNTED)) return;
        arrow.addTag(HM_COUNTED);

        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) return;
        Entity hitEntity = entityHit.getEntity();
        if (!(hitEntity instanceof LivingEntity target)) return;

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

        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        long cooldownTicks = (long) (bonus.getCooldown(rarity) * 20);

        if (MiscUtil.isOnCooldown(bonus.getId(), cooldownTicks, player)) return;

        Long expiry = MARK_EXPIRY.get(target.getUUID());
        boolean markActive = expiry != null && now < expiry;

        if (markActive) {
            MARK_EXPIRY.remove(target.getUUID());
            MiscUtil.startCooldown(bonus.getId(), player);

            AbstractSpell sunbeam = SpellRegistry.REGISTRY.get().getValue(SUNBEAM_ID);
            if (sunbeam == null) return;

            int spellLevel = bonus.getSpellLevel(rarity);
            float radius = bonus.getRadius(rarity);
            Vec3 center = target.position();

            List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(radius),
                    e -> e != player && e.isAlive() && !e.isAlliedTo(player) && !(e instanceof Player));

            if (!enemies.isEmpty()) {
                List<LivingEntity> targets = enemies.subList(0, Math.min(enemies.size(), MAX_TARGETS));

                level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0, center.z,
                        20, 0.5, 0.5, 0.5, 0.12);
                level.playSound(null, center.x, center.y, center.z,
                        SoundEvents.LIGHTNING_BOLT_THUNDER, player.getSoundSource(), 0.5f, 1.5f);

                setTriggering(player, true);
                try {
                    for (LivingEntity foe : targets) {
                        SpellCastUtil.castSpell(player, sunbeam, spellLevel, foe);
                    }
                } finally {
                    var server = player.level().getServer();
                    if (server != null) {
                        server.execute(() -> setTriggering(player, false));
                    } else {
                        setTriggering(player, false);
                    }
                }
            }
        } else {
            long newExpiry = now + bonus.getMarkDurationTicks(rarity);
            MARK_EXPIRY.put(target.getUUID(), newExpiry);

            Vec3 pos = target.position();
            level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 0.5, pos.z,
                    12, 0.2, 0.4, 0.2, 0.05);
            level.playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.EXPERIENCE_ORB_PICKUP, player.getSoundSource(), 1.0f, 2.0f);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MARK_EXPIRY.clear();
    }
}