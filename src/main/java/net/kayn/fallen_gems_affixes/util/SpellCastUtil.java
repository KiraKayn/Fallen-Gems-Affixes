package net.kayn.fallen_gems_affixes.util;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.network.casting.OnCastStartedPacket;
import io.redspace.ironsspellbooks.network.casting.OnClientCastPacket;
import io.redspace.ironsspellbooks.network.casting.UpdateCastingStatePacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import static net.kayn.fallen_gems_affixes.event.SpellEventHandler.updateTargetData;

public class SpellCastUtil {

    public static void castSpell(LivingEntity caster, AbstractSpell spell, int spellLevel, LivingEntity target) {
        if (caster.level().isClientSide()) {
            return;
        }

        MagicData magicData = MagicData.getPlayerMagicData(caster);
        if (magicData.isCasting()) {
            FallenGemsAffixes.LOGGER.debug(
                    "SpellCastAffix: Entity is still casting {}, resetting state for new cast",
                    magicData.getCastingSpellId()
            );
            AbstractSpell oldSpell = magicData.getCastingSpell().getSpell();
            oldSpell.onServerCastComplete(caster.level(), magicData.getCastingSpellLevel(), caster, magicData, true);

            magicData.resetCastingState();
            magicData = MagicData.getPlayerMagicData(caster);
        }
        updateTargetData(caster, target, magicData, spell, x -> true);

        FallenGemsAffixes.LOGGER.debug("SpellCastAffix: Merging target data, target: {}", target.getName().getString());

        if (caster instanceof ServerPlayer serverPlayer) {
            FallenGemsAffixes.LOGGER.debug("Casting SPELL FOR SERVERPLAYA");
            castSpellForPlayer(spell, spellLevel, serverPlayer, magicData);
        } else if (caster instanceof IMagicEntity magicEntity) {
            magicEntity.initiateCastSpell(spell, spellLevel);
        } else {
            if (spell.checkPreCastConditions(caster.level(), spellLevel, caster, magicData)) {
                spell.onCast(caster.level(), spellLevel, caster, CastSource.COMMAND, magicData);
                spell.onServerCastComplete(caster.level(), spellLevel, caster, magicData, false);
            }
        }
    }

    private static void castSpellForPlayer(AbstractSpell spell, int spellLevel, ServerPlayer serverPlayer, MagicData magicData) {

        if (magicData.isCasting()) {
            FallenGemsAffixes.LOGGER.warn("Attempted to trigger affix-cast while player was already casting");
            return;
        }

        if (serverPlayer.isUsingItem()) {
            serverPlayer.stopUsingItem();
        }

        int effectiveCastTime = 0;
        if (spell.getCastType() == CastType.CONTINUOUS) {
            effectiveCastTime = spell.getEffectiveCastTime(spellLevel, serverPlayer);
        }
        magicData.initiateCast(spell, spellLevel, effectiveCastTime, CastSource.COMMAND, "command");
        magicData.setPlayerCastingItem(ItemStack.EMPTY);

        spell.onServerPreCast(serverPlayer.level(), spellLevel, serverPlayer, magicData);

        PacketDistributor.sendToPlayer(serverPlayer, new UpdateCastingStatePacket(
                spell.getSpellId(), spellLevel, effectiveCastTime, CastSource.COMMAND, "command"
        ));

        PacketDistributor.sendToPlayer(serverPlayer, new OnCastStartedPacket(
                serverPlayer.getUUID(), spell.getSpellId(), spellLevel
        ));

        if (magicData.getAdditionalCastData() instanceof TargetEntityCastData targetingData) {
            var target = targetingData.getTarget((ServerLevel) serverPlayer.level());
            if (target != null) {
                FallenGemsAffixes.LOGGER.debug("Casting Spell {} with target {}",
                        magicData.getCastingSpellId(),
                        target.getName().getString()
                );
            } else {
                FallenGemsAffixes.LOGGER.warn("Casting Spell {} but target was null",
                        magicData.getCastingSpellId()
                );
            }
        } else {
            var data = magicData.getAdditionalCastData();
            FallenGemsAffixes.LOGGER.warn(
                    "Tried to merge Targeting Data but was overridden. Current cast data for spell {}: {}",
                    magicData.getCastingSpellId(),
                    (data != null ? data.getClass().getName() : "null")
            );
        }

        if (effectiveCastTime == 0) {
            spell.onCast(serverPlayer.level(), spellLevel, serverPlayer, CastSource.COMMAND, magicData);
            PacketDistributor.sendToPlayer(serverPlayer, new OnClientCastPacket(
                    spell.getSpellId(), spellLevel, CastSource.COMMAND, magicData.getAdditionalCastData()
            ));
        }
    }
}