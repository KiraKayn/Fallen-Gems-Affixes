package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodNovaEventHandler {

    private static final ResourceLocation BLEED_EFFECT =
            ResourceLocation.fromNamespaceAndPath("attributeslib", "bleeding");

    private record NovaVictim(UUID playerUUID, float healAmount) {}
    private static final Map<UUID, NovaVictim> PENDING_HEALS = new HashMap<>();

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (arrow.getPersistentData().getBoolean("apoth.generated")) return;
        if (arrow.getTags().contains("fga.bn_triggered")) return;
        arrow.addTag("fga.bn_triggered");

        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) return;
        Entity hitEntity = entityHit.getEntity();
        if (!(hitEntity instanceof LivingEntity target)) return;

        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;

        ItemStack main = player.getMainHandItem();
        if (main.isEmpty()) return;

        BloodNovaBonus bonus = null;
        LootRarity rarity = null;
        LootCategory cat = LootCategory.forItem(main);
        for (GemInstance inst : SocketHelper.getGems(main).gems()) {
            if (!inst.isValid()) continue;
            LootRarity r = inst.rarity().get();
            Optional<?> opt = inst.gem().get().getBonus(cat, r);
            if (opt.isPresent() && opt.get() instanceof BloodNovaBonus b && b.supports(r)) {
                bonus = b;
                rarity = r;
                break;
            }
        }
        if (bonus == null || rarity == null) return;

        long cooldownTicks = (long) (bonus.getCooldown(rarity) * 20);
        if (MiscUtil.isOnCooldown(bonus.getId(), cooldownTicks, player)) return;
        MiscUtil.startCooldown(bonus.getId(), player);

        Vec3 center = target.position();
        ServerLevel level = (ServerLevel) player.level();
        float radius = bonus.getRadius(rarity);
        float healPercent = bonus.getHealPercent(rarity);
        int bleedTicks = bonus.getBleedDurationTicks(rarity);
        MobEffect bleed = ForgeRegistries.MOB_EFFECTS.getValue(BLEED_EFFECT);

        double arrowBaseDamage = arrow.getBaseDamage();
        float damage = (float) (arrowBaseDamage * bonus.getDamagePercent(rarity));

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(radius),
                e -> e != player && e.isAlive() && !e.isAlliedTo(player) && !(e instanceof Player));

        if (targets.isEmpty()) return;

        level.sendParticles(ParticleHelper.BLOOD, center.x, center.y + 0.25, center.z,
                100, 0.03, 0.4, 0.03, 0.4);
        level.sendParticles(ParticleHelper.BLOOD, center.x, center.y + 0.25, center.z,
                100, 0.03, 0.4, 0.03, 0.4);
        level.sendParticles(new BlastwaveParticleOptions(
                        SchoolRegistry.BLOOD.get().getTargetingColor(), radius),
                center.x, center.y + 0.25, center.z,
                1, 0, 0, 0, 0);
        level.playSound(null, center.x, center.y, center.z,
                SoundRegistry.BLOOD_EXPLOSION.get(), SoundSource.PLAYERS,
                3, player.getRandom().nextFloat() * 0.4f + 0.8f);

        for (LivingEntity victim : targets) {
            victim.invulnerableTime = 0;
            victim.hurt(player.level().damageSources().magic(), damage);
            if (victim.isAlive() && bleed != null) {
                victim.addEffect(new MobEffectInstance(bleed, bleedTicks, 0, false, true));
            }
            float healAmount = victim.getMaxHealth() * healPercent;
            PENDING_HEALS.put(victim.getUUID(), new NovaVictim(player.getUUID(), healAmount));
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        NovaVictim data = PENDING_HEALS.remove(event.getEntity().getUUID());
        if (data == null) return;
        Entity playerEntity = ((ServerLevel) event.getEntity().level()).getEntity(data.playerUUID);
        if (playerEntity instanceof Player player) {
            player.heal(data.healAmount);
            ((ServerLevel) player.level()).sendParticles(ParticleTypes.HEART,
                    player.getX(), player.getY() + player.getBbHeight(), player.getZ(),
                    3, 0.3, 0.3, 0.3, 0.1);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PENDING_HEALS.clear();
    }
}