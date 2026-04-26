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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

import static net.kayn.fallen_gems_affixes.adventure.affix.SpellCastAffix.isCurrentlyTriggering;
import static net.kayn.fallen_gems_affixes.adventure.affix.SpellCastAffix.setTriggering;

public class HolyWrathEventHandler {

    private static final ResourceLocation SUNBEAM_ID =
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "sunbeam");

    private static final int MAX_TARGETS = 8;

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (isCurrentlyTriggering(player)) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        HolyWrathBonus bonus = null;
        LootRarity rarity = null;

        LootCategory cat = LootCategory.forItem(weapon);
        for (GemInstance inst : SocketHelper.getGems(weapon).gems()) {
            if (!inst.isValid()) continue;
            LootRarity r = inst.rarity().get();
            Optional<?> opt = inst.gem().get().getBonus(cat, r);
            if (opt.isPresent() && opt.get() instanceof HolyWrathBonus b && b.supports(r)) {
                bonus = b;
                rarity = r;
                break;
            }
        }

        if (bonus == null || rarity == null) return;
        if (MiscUtil.isOnCooldown(bonus.getId(), (long) (bonus.getCooldown(rarity) * 20), player)) return;

        AbstractSpell sunbeam = SpellRegistry.REGISTRY.get().getValue(SUNBEAM_ID);
        if (sunbeam == null) return;

        ServerLevel level = (ServerLevel) player.level();
        float radius = bonus.getRadius(rarity);
        int spellLevel = bonus.getSpellLevel(rarity);
        int glowTicks = bonus.getGlowDurationTicks(rarity);

        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(dead.position(), dead.position()).inflate(radius),
                e -> e != player && e != dead && e.isAlive()
                        && !e.isAlliedTo(player) && !(e instanceof Player));

        if (nearby.isEmpty()) return;

        MiscUtil.startCooldown(bonus.getId(), player);

        level.sendParticles(ParticleTypes.END_ROD,
                dead.getX(), dead.getY() + 1.0, dead.getZ(), 25, 0.6, 0.6, 0.6, 0.12);

        List<LivingEntity> targets = nearby.subList(0, Math.min(nearby.size(), MAX_TARGETS));

        setTriggering(player, true);
        try {
            for (LivingEntity target : targets) {
                SpellCastUtil.castSpell(player, sunbeam, spellLevel, target);

                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, glowTicks, 0, false, true));
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
}