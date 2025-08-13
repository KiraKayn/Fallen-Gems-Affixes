package net.kayn.fallen_gems_affixes.util;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.network.ClientboundUpdateCastingState;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnCastStarted;
import io.redspace.ironsspellbooks.network.spell.ClientboundOnClientCast;
import io.redspace.ironsspellbooks.network.spell.ClientboundSyncTargetingData;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.entity.PartEntity;

import java.util.function.Predicate;

public class SpellCastUtil {

    public static void castSpell(LivingEntity caster, AbstractSpell spell, int spellLevel, LivingEntity target) {
        if (caster.level().isClientSide()) {
            return;
        }

        MagicData magicData = MagicData.getPlayerMagicData(caster);
        if (magicData.isCasting()) {
            FallenGemsAffixes.LOGGER.debug(
                    "SpellCastAffix: Entity is still casting {}, forcing spell completion",
                    magicData.getCastingSpellId()
            );
            AbstractSpell oldSpell = magicData.getCastingSpell().getSpell();
            oldSpell.onCast(caster.level(), magicData.getCastingSpellLevel(), caster, magicData.getCastSource(), magicData);
            oldSpell.onServerCastComplete(caster.level(), magicData.getCastingSpellLevel(), caster, magicData, false);
            magicData.resetCastingState();
            magicData = MagicData.getPlayerMagicData(caster);
        }

        FallenGemsAffixes.LOGGER.debug("SpellCastAffix: Merging target data, target: {}", target.getName().getString());
        updateTargetData(caster, target, magicData, spell, x -> true);

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

        CastResult castResult = spell.canBeCastedBy(spellLevel, CastSource.COMMAND, magicData, serverPlayer);
        if (castResult.message != null) {
            Messages.sendToPlayer(new ClientboundSetActionBarTextPacket(castResult.message), serverPlayer);
        }

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

        Messages.sendToPlayer(new ClientboundUpdateCastingState(
                spell.getSpellId(), spellLevel, effectiveCastTime, CastSource.COMMAND, "command"
        ), serverPlayer);

        Messages.sendToPlayersTrackingEntity(new ClientboundOnCastStarted(
                serverPlayer.getUUID(), spell.getSpellId(), spellLevel
        ), serverPlayer, true);

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
            Messages.sendToPlayer(new ClientboundOnClientCast(
                    spell.getSpellId(), spellLevel, CastSource.COMMAND, magicData.getAdditionalCastData()
            ), serverPlayer);
        }
    }

    public static void updateTargetData(LivingEntity caster, Entity entityHit, MagicData playerMagicData, AbstractSpell spell, Predicate<LivingEntity> filter) {
        LivingEntity livingTarget = null;
        if (entityHit instanceof LivingEntity livingEntity && filter.test(livingEntity)) {
            livingTarget = livingEntity;
        } else if (entityHit instanceof PartEntity<?> partEntity &&
                partEntity.getParent() instanceof LivingEntity livingParent &&
                filter.test(livingParent)) {
            livingTarget = livingParent;
        }

        if (livingTarget != null) {
            playerMagicData.setAdditionalCastData(new TargetEntityCastData(livingTarget));
            if (caster instanceof ServerPlayer serverPlayer) {
                if (spell.getCastType() != CastType.INSTANT) {
                    Messages.sendToPlayer(new ClientboundSyncTargetingData(livingTarget, spell), serverPlayer);
                }
            }
        } else if (caster instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayer(new ClientboundSetActionBarTextPacket(
                    Component.translatable("ui.irons_spellbooks.cast_error_target").withStyle(ChatFormatting.RED)
            ), serverPlayer);
        }
    }
}