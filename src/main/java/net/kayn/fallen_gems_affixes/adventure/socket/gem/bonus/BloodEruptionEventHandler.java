package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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

public class BloodEruptionEventHandler {

    private static final ResourceLocation[] SPELL_POOL = {
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "acupuncture"),
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "blood_needles"),
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "devour")
    };

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (isCurrentlyTriggering(player)) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        BloodEruptionBonus bonus = null;
        LootRarity rarity = null;

        LootCategory cat = LootCategory.forItem(weapon);
        for (GemInstance inst : SocketHelper.getGems(weapon).gems()) {
            if (!inst.isValid()) continue;
            LootRarity r = inst.rarity().get();
            Optional<?> opt = inst.gem().get().getBonus(cat, r);
            if (opt.isPresent() && opt.get() instanceof BloodEruptionBonus b && b.supports(r)) {
                bonus = b;
                rarity = r;
                break;
            }
        }

        if (bonus == null || rarity == null) return;

        if (MiscUtil.isOnCooldown(bonus.getId(), (long) (bonus.getCooldown(rarity) * 20), player)) return;
        MiscUtil.startCooldown(bonus.getId(), player);

        ServerLevel level = (ServerLevel) player.level();
        float radius = bonus.getRadius(rarity);
        int spellLevel = bonus.getSpellLevel(rarity);

        level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM,
                dead.getX(), dead.getY() + 0.5, dead.getZ(), 30, 0.5, 0.4, 0.5, 0.04);
        level.playSound(null, dead.getX(), dead.getY(), dead.getZ(),
                SoundEvents.GENERIC_HURT, player.getSoundSource(), 1.2f, 0.5f);

        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(dead.position(), dead.position()).inflate(radius),
                e -> e != player && e != dead && e.isAlive()
                        && !e.isAlliedTo(player) && !(e instanceof Player));

        if (nearby.isEmpty()) return;

        MagicData magicData = MagicData.getPlayerMagicData(player);

        setTriggering(player, true);
        try {
            for (LivingEntity target : nearby) {
                player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
                player.setYHeadRot(player.getYRot());

                ResourceLocation chosenId = SPELL_POOL[level.random.nextInt(SPELL_POOL.length)];
                AbstractSpell spell = SpellRegistry.REGISTRY.get().getValue(chosenId);
                if (spell != null) {
                    magicData.setAdditionalCastData(new TargetEntityCastData(target));
                    spell.castSpell(level, spellLevel, player, CastSource.MOB, false);
                    magicData.setAdditionalCastData(null);
                }
            }
        } finally {
            magicData.setAdditionalCastData(null);
            var server = player.level().getServer();
            if (server != null) {
                server.execute(() -> setTriggering(player, false));
            } else {
                setTriggering(player, false);
            }
        }
    }
}