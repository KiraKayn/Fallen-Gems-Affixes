package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellEffectAffix;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.SpellEffectBonus;
import net.kayn.fallen_gems_affixes.mixin.SocketHelperMixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class SpellEventHandler {
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        SpellDamageSource source = event.getSpellDamageSource();
        Entity entity = source.getEntity();
        if (!(entity instanceof LivingEntity caster)) return;
        for (ItemStack stack : caster.getAllSlots()) {
            AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (inst.affix().get() instanceof SpellEffectAffix affix) {
                    if (affix.target == SpellEffectAffix.Target.SPELL_DAMAGE_TARGET) {
                        affix.applyEffect(target, inst.rarity().get(), inst.level());
                    } else if (affix.target == SpellEffectAffix.Target.SPELL_DAMAGE_SELF) {
                        affix.applyEffect(caster, inst.rarity().get(), inst.level());
                    }
                }
            });
            checkGemBonus(stack, (gem, bonus) -> {
                if (bonus.target == SpellEffectAffix.Target.SPELL_DAMAGE_TARGET) {
                    bonus.applyEffect(gem, target);
                } else if (bonus.target == SpellEffectAffix.Target.SPELL_DAMAGE_SELF) {
                    bonus.applyEffect(gem, caster);
                }
            });
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void onSpellHeal(SpellHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        for (ItemStack stack : event.getEntity().getAllSlots()) {
            dev.shadowsoffire.apotheosis.affix.AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (inst.affix().get() instanceof SpellEffectAffix affix) {
                    if (affix.target == SpellEffectAffix.Target.SPELL_HEAL_TARGET) {
                        affix.applyEffect(event.getTargetEntity(), inst.rarity().get(), inst.level());
                    } else if (affix.target == SpellEffectAffix.Target.SPELL_HEAL_SELF) {
                        affix.applyEffect(event.getEntity(), inst.rarity().get(), inst.level());
                    }
                }
            });
            checkGemBonus(stack, (gem, bonus) -> {
                if (bonus.target == SpellEffectAffix.Target.SPELL_HEAL_TARGET) {
                    bonus.applyEffect(gem, event.getTargetEntity());
                } else if (bonus.target == SpellEffectAffix.Target.SPELL_HEAL_SELF) {
                    bonus.applyEffect(gem, event.getEntity());
                }
            });
        }
    }

    public static void checkGemBonus(ItemStack itemStack, BonusProcessor processor) {
        if (itemStack.isEmpty()) return;
        LootCategory cat = LootCategory.forItem(itemStack);
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            if (!g.isValid()) continue;
            Purity purity = g.purity();
            Gem gem = g.gem().get();
            gem.getBonus(cat, purity)
                    .filter(b -> b instanceof SpellEffectBonus)
                    .map(b -> (SpellEffectBonus) b)
                    .ifPresent(bonus -> processor.accept(g, bonus));
        }
    }

    @FunctionalInterface
    public interface BonusProcessor {
        void accept(GemInstance gem, SpellEffectBonus bonus);
    }
}
