package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellEffectAffix;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.SpellEffectBonus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

        for (ItemStack stack : event.getEntity().getAllSlots()) {
            AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (inst.affix().get() instanceof SpellEffectAffix affix) {
                    if (affix.target == SpellEffectAffix.Target.SPELL_HEAL_TARGET) {
                        affix.applyEffect(event.getTargetEntity(), inst.rarity().get(), inst.level());
                    } else if (affix.target == SpellEffectAffix.Target.SPELL_HEAL_SELF) {
                        affix.applyEffect(event.getEntity(), inst.rarity().get(), inst.level());
                    }
                }
            });
            checkGemBonus(stack, (gem, bonus, rarity) -> {
                if (bonus.target == SpellEffectAffix.Target.SPELL_HEAL_TARGET) {
                    bonus.applyEffect(gem, event.getTargetEntity(), rarity);
                } else if (bonus.target == SpellEffectAffix.Target.SPELL_HEAL_SELF) {
                    bonus.applyEffect(gem, event.getEntity(), rarity);
                }
            });
        }
    }

    public static void checkGemBonus(ItemStack itemStack, BonusProcessor processor) {
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
