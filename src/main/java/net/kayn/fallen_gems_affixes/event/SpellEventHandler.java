package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.kayn.fallen_gems_affixes.adventure.affix.*;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.SpellEffectBonus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class SpellEventHandler {

    private static LivingEntity getSpellTarget(Optional<SpellCastAffix.TargetType> targetType,
                                               LivingEntity caster, LivingEntity contextTarget) {
        return targetType.filter(type -> type == SpellCastAffix.TargetType.SELF)
                .map(type -> caster).orElse(contextTarget);
    }


    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        LivingEntity caster = event.getEntity();
        if (caster.level().isClientSide()) return;
        if (SpellCastAffix.isCurrentlyTriggering(caster)) return;
        if (event.getCastSource() == CastSource.COMMAND) return;

        String castSpellId = event.getSpellId();

        for (ItemStack stack : caster.getAllSlots()) {
            AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (inst.affix().get() instanceof CooldownResetAffix affix) {
                    affix.onSpellCast(caster, castSpellId, inst.rarity().get(), inst.level());
                } else if (inst.affix().get() instanceof AutocastAffix affix) {
                    boolean isTargetMode = affix.target.filter(t -> t == SpellCastAffix.TargetType.TARGET).isPresent();
                    if (isTargetMode) return;
                    affix.onSpellCast(caster, castSpellId, caster, inst.rarity().get());
                }
            });
        }
    }

    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        SpellDamageSource source = event.getSpellDamageSource();
        Entity entity = source.getEntity();
        if (!(entity instanceof LivingEntity caster)) return;

        if (SpellCastAffix.isCurrentlyTriggering(caster)) return;

        String castSpellId = source.spell().getSpellId();

        for (ItemStack stack : caster.getAllSlots()) {
            AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (inst.affix().get() instanceof SpellEffectAffix affix) {
                    if (affix.target == SpellEffectAffix.Target.SPELL_DAMAGE_TARGET) {
                        affix.applyEffect(target, inst.rarity().get(), inst.level());
                    } else if (affix.target == SpellEffectAffix.Target.SPELL_DAMAGE_SELF) {
                        affix.applyEffect(caster, inst.rarity().get(), inst.level());
                    }
                } else if (inst.affix().get() instanceof SpellCastAffix affix
                        && affix.trigger == SpellCastAffix.TriggerType.SPELL_DAMAGE) {
                    LivingEntity actualTarget = getSpellTarget(affix.target, caster, target);
                    affix.triggerSpell(caster, actualTarget, inst.rarity().get(), (int) inst.level());
                } else if (inst.affix().get() instanceof AutocastAffix affix) {
                    boolean isTargetMode = affix.target.filter(t -> t == SpellCastAffix.TargetType.TARGET).isPresent();
                    if (!isTargetMode) return;
                    affix.onSpellCast(caster, castSpellId, target, inst.rarity().get());
                }

                if (inst.affix().get() instanceof SpellFocusAffix affix) {
                    float mult = affix.onSpellDamage(caster, target, castSpellId, inst.rarity().get(), inst.level());
                    event.setAmount(event.getAmount() * mult);
                }

                if (inst.affix().get() instanceof ManaDamageAffix affix
                        && caster instanceof Player player) {
                    float mult = affix.getDamageMultiplier(player, inst.rarity().get(), inst.level());
                    event.setAmount(event.getAmount() * mult);
                }
            });

            checkGemBonus(stack, (gem, bonus, rarity) -> {
                if (bonus.target == SpellEffectAffix.Target.SPELL_DAMAGE_TARGET) {
                    bonus.applyEffect(gem, target, rarity);
                } else if (bonus.target == SpellEffectAffix.Target.SPELL_DAMAGE_SELF) {
                    bonus.applyEffect(gem, caster, rarity);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onSpellHeal(SpellHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity caster = event.getEntity();
        if (SpellCastAffix.isCurrentlyTriggering(caster)) return;

        LivingEntity healTarget = event.getTargetEntity();

        for (ItemStack stack : caster.getAllSlots()) {
            AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (inst.affix().get() instanceof SpellEffectAffix affix) {
                    if (affix.target == SpellEffectAffix.Target.SPELL_HEAL_TARGET) {
                        affix.applyEffect(healTarget, inst.rarity().get(), inst.level());
                    } else if (affix.target == SpellEffectAffix.Target.SPELL_HEAL_SELF) {
                        affix.applyEffect(caster, inst.rarity().get(), inst.level());
                    }
                } else if (inst.affix().get() instanceof SpellCastAffix affix
                        && affix.trigger == SpellCastAffix.TriggerType.SPELL_HEAL) {
                    LivingEntity actualTarget = getSpellTarget(affix.target, caster, healTarget);
                    affix.triggerSpell(caster, actualTarget, inst.rarity().get(), (int) inst.level());
                }
            });

            checkGemBonus(stack, (gem, bonus, rarity) -> {
                if (bonus.target == SpellEffectAffix.Target.SPELL_HEAL_TARGET) {
                    bonus.applyEffect(gem, healTarget, rarity);
                } else if (bonus.target == SpellEffectAffix.Target.SPELL_HEAL_SELF) {
                    bonus.applyEffect(gem, caster, rarity);
                }
            });
        }
    }

    // Mana Event Handlers

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onChangeMana_Cost(ChangeManaEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getNewMana() >= event.getOldMana()) return;

        Player player = event.getEntity();
        MagicData magicData = event.getMagicData();
        SpellData castingSpell = magicData.getCastingSpell();
        if (castingSpell == null || castingSpell.getSpell() == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        float totalReduction = 0f;
        for (var inst : AffixHelper.getAffixes(mainHand).values()) {
            if (!inst.isValid()) continue;
            if (!(inst.affix().get() instanceof ManaCostAffix affix)) continue;
            totalReduction += affix.getReductionPercent(inst.rarity().get(), inst.level());
        }
        if (totalReduction <= 0f) return;

        totalReduction = Math.min(totalReduction, 0.9f);
        float manaCost    = event.getOldMana() - event.getNewMana();
        float reducedCost = manaCost * (1f - totalReduction);
        event.setNewMana(event.getOldMana() - reducedCost);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChangeMana_Return(ChangeManaEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getNewMana() >= event.getOldMana()) return;

        Player player = event.getEntity();
        MagicData magicData = event.getMagicData();
        SpellData castingSpell = magicData.getCastingSpell();
        if (castingSpell == null || castingSpell.getSpell() == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;
        float actualCost = event.getOldMana() - event.getNewMana();
        if (actualCost <= 0f) return;

        float totalReturn = 0f;
        for (var inst : AffixHelper.getAffixes(mainHand).values()) {
            if (!inst.isValid()) continue;
            if (!(inst.affix().get() instanceof ManaReturnAffix affix)) continue;

            float chance = affix.getChance(inst.rarity().get(), inst.level());
            if (player.getRandom().nextFloat() < chance) {
                totalReturn += actualCost * affix.getReturnPercent(inst.rarity().get(), inst.level());
            }
        }
        if (totalReturn <= 0f) return;

        event.setNewMana(Math.min(event.getNewMana() + totalReturn, event.getOldMana()));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SpellFocusAffix.clearState(event.getEntity().getUUID());
        BerserkerAffix.clearState(event.getEntity().getUUID());
    }

    private static void checkGemBonus(ItemStack itemStack, BonusProcessor processor) {
        if (itemStack.isEmpty()) return;
        LootCategory cat = LootCategory.forItem(itemStack);
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            if (!g.isValid()) continue;
            DynamicHolder<LootRarity> rarityHolder = g.rarity();
            if (!rarityHolder.isBound()) continue;
            LootRarity rarity = rarityHolder.get();
            Gem gem = g.gem().get();
            gem.getBonus(cat, rarity)
                    .filter(b -> b instanceof SpellEffectBonus)
                    .map(b -> (SpellEffectBonus) b)
                    .ifPresent(bonus -> processor.accept(g.gemStack(), bonus, rarity));
        }
    }

    @FunctionalInterface
    public interface BonusProcessor {
        void accept(ItemStack gem, SpellEffectBonus bonus, LootRarity rarity);
    }
}